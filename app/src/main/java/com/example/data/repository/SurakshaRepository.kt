package com.example.data.repository

import com.example.data.api.SupabaseClient
import com.example.data.auth.AuthService
import com.example.data.complaint.ComplaintDraftGenerator
import com.example.data.database.AppDatabase
import com.example.data.heuristic.ThreatAssessment
import com.example.data.heuristic.ThreatScoringEngine
import com.example.data.model.AwarenessContentEntity
import com.example.data.model.IncidentEntity
import com.example.data.model.IncidentSummary
import com.example.data.model.QuizQuestionEntity
import com.example.data.model.ThreatCheckEntity
import com.example.data.model.ThreatRegisterEntity
import com.example.data.model.UserEntity
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

data class QuizAnswerResult(
    val questionId: Int,
    val selectedIndex: Int,
    val correctIndex: Int,
    val isCorrect: Boolean,
    val explanation: String
)

class SurakshaRepository(
    private val database: AppDatabase,
    val authService: AuthService = AuthService(database)
) {
    private val userDao = database.userDao()
    private val threatCheckDao = database.threatCheckDao()
    private val incidentDao = database.incidentDao()
    private val threatRegisterDao = database.threatRegisterDao()
    private val awarenessDao = database.awarenessDao()
    private val quizDao = database.quizDao()

    // Auth Operations
    suspend fun getInitialUser(): UserEntity? = authService.getInitialUser()

    suspend fun signInWithGoogle(context: Context, serverClientId: String? = null): Result<UserEntity> {
        return authService.signInWithGoogle(context, serverClientId)
    }

    suspend fun login(email: String, password: String): Result<UserEntity> {
        return authService.signInWithEmail(email, password)
    }

    suspend fun register(name: String, email: String, password: String): Result<UserEntity> {
        return authService.registerWithEmail(name, email, password)
    }

    suspend fun logout(context: Context) {
        authService.signOut(context)
    }

    // Threat Check Engine
    suspend fun performThreatCheck(
        inputText: String,
        inputType: String,
        userId: Int?
    ): Pair<ThreatAssessment, Long> = withContext(Dispatchers.IO) {
        val assessment = ThreatScoringEngine.assess(inputText, inputType)
        val entity = ThreatCheckEntity(
            userId = userId,
            inputText = inputText,
            inputType = inputType,
            threatCategory = assessment.threatCategory,
            threatType = assessment.threatType,
            severity = assessment.severity,
            riskScore = assessment.riskScore,
            confidence = assessment.confidence,
            indicators = assessment.indicators.joinToString("||"),
            recommendations = assessment.recommendations.joinToString("||")
        )
        val checkId = threatCheckDao.insertThreatCheck(entity)

        // Async sync to Supabase public.url_analyses
        try {
            val userEmail = if (userId != null) userDao.getUserById(userId)?.email else null
            SupabaseClient.syncUrlAnalysis(
                userEmail = userEmail,
                rawInput = inputText,
                riskScore = assessment.riskScore,
                classification = assessment.threatType,
                redFlags = assessment.indicators
            )
        } catch (e: Exception) {
            // Non-blocking offline-first fallback
        }

        Pair(assessment, checkId)
    }

    fun getAllThreatChecks(): Flow<List<ThreatCheckEntity>> = threatCheckDao.getAllThreatChecks()

    suspend fun clearScanHistory() = withContext(Dispatchers.IO) {
        threatCheckDao.clearAllThreatChecks()
    }

    // Incident Reporting
    suspend fun reportIncident(
        userId: Int?,
        category: String,
        description: String,
        url: String?,
        platform: String?,
        incidentDatetime: String,
        transactionInfo: String?,
        evidenceNote: String?,
        riskLevel: String
    ): IncidentEntity = withContext(Dispatchers.IO) {
        val textToEvaluate = listOfNotNull(description, url?.ifBlank { null }).joinToString(" ")
        val assessment = ThreatScoringEngine.assess(
            inputText = textToEvaluate,
            inputType = if (!url.isNullOrBlank() && description.isBlank()) "url" else "message"
        )
        val finalRiskLevel = when {
            riskLevel.equals("HIGH", ignoreCase = true) || assessment.severity == "HIGH" -> "HIGH"
            !transactionInfo.isNullOrBlank() || category.contains("UPI", ignoreCase = true) || category.contains("Banking", ignoreCase = true) -> "HIGH"
            riskLevel.equals("MEDIUM", ignoreCase = true) || assessment.severity == "MEDIUM" -> "MEDIUM"
            else -> assessment.severity
        }

        val incident = IncidentEntity(
            userId = userId,
            category = category,
            description = description,
            url = url?.ifBlank { null },
            platform = platform?.ifBlank { null },
            incidentDatetime = incidentDatetime,
            transactionInfo = transactionInfo?.ifBlank { null },
            evidenceNote = evidenceNote?.ifBlank { null } ?: "Indicators detected: ${assessment.indicators.joinToString("; ")}",
            riskLevel = finalRiskLevel,
            status = "reported"
        )
        val id = incidentDao.insertIncident(incident)
        val saved = incident.copy(id = id.toInt())

        // Also add to public threat register if high/medium severity
        if (finalRiskLevel == "HIGH" || finalRiskLevel == "MEDIUM") {
            val registerEntry = ThreatRegisterEntity(
                title = "Reported: $category via ${platform ?: "Digital Channel"}",
                category = when {
                    category.contains("Phishing", ignoreCase = true) -> "phishing"
                    category.contains("Job", ignoreCase = true) -> "job_scam"
                    category.contains("UPI", ignoreCase = true) || category.contains("Banking", ignoreCase = true) -> "financial_scam"
                    else -> "other"
                },
                severity = finalRiskLevel,
                status = "reported",
                sourceIncidentId = saved.id,
                description = description.take(200) + if (description.length > 200) "..." else ""
            )
            threatRegisterDao.insertRegisterEntry(registerEntry)
        }

        // Sync incident to Supabase cloud database
        try {
            val userEmail = if (userId != null) userDao.getUserById(userId)?.email else "citizen@surakshasetu.in"
            SupabaseClient.syncIncident(
                reporterEmail = userEmail ?: "citizen@surakshasetu.in",
                title = "Incident Report: $category (${platform ?: "Online"})",
                description = "$description\nEvidence: ${incident.evidenceNote}\nFinancial info: ${transactionInfo ?: "None"}",
                severity = finalRiskLevel,
                category = category,
                status = "new"
            )
        } catch (e: Exception) {
            // Non-blocking offline-first fallback
        }

        saved
    }

    fun getAllIncidents(): Flow<List<IncidentEntity>> = incidentDao.getAllIncidents()
    fun getIncidentsForUser(userId: Int): Flow<List<IncidentEntity>> = incidentDao.getIncidentsForUser(userId)

    fun getThreatRegister(): Flow<List<ThreatRegisterEntity>> = threatRegisterDao.getAllRegisterEntries()

    suspend fun getIncidentById(id: Int): IncidentEntity? = withContext(Dispatchers.IO) {
        incidentDao.getIncidentById(id)
    }

    suspend fun getIncidentSummary(incidentId: Int): IncidentSummary? = withContext(Dispatchers.IO) {
        val incident = incidentDao.getIncidentById(incidentId) ?: return@withContext null
        val textToEvaluate = listOfNotNull(incident.description, incident.url).joinToString(" ")
        val assessment = ThreatScoringEngine.assess(textToEvaluate)

        val resolvedRiskLevel = when {
            incident.riskLevel.equals("HIGH", ignoreCase = true) || assessment.severity == "HIGH" -> "HIGH"
            !incident.transactionInfo.isNullOrBlank() || incident.category.contains("UPI", ignoreCase = true) || incident.category.contains("Banking", ignoreCase = true) -> "HIGH"
            incident.riskLevel.equals("MEDIUM", ignoreCase = true) || assessment.severity == "MEDIUM" -> "MEDIUM"
            else -> assessment.severity
        }

        // If DB has outdated lower risk level, sync it
        if (resolvedRiskLevel != incident.riskLevel) {
            val updated = incident.copy(riskLevel = resolvedRiskLevel)
            incidentDao.updateIncident(updated)
            threatRegisterDao.updateSeverityBySourceIncident(incident.id, resolvedRiskLevel)
        }

        val nextSteps = mutableListOf<String>()

        if (!incident.transactionInfo.isNullOrBlank()) {
            nextSteps.add("Call national cyber financial helpline 1930 immediately to request transaction lien.")
            nextSteps.add("Contact your bank's fraud desk to block compromised cards/accounts.")
        }
        if (incident.category.contains("Phishing", ignoreCase = true) || incident.category.contains("Identity", ignoreCase = true)) {
            nextSteps.add("Change passwords for your banking and primary email accounts immediately.")
            nextSteps.add("Revoke suspicious active sessions from your Google / Apple account security settings.")
        }
        nextSteps.add("File this formal complaint draft on the National Cyber Crime Portal (cybercrime.gov.in).")
        nextSteps.add("Preserve SMS, call logs, and transaction screenshots as digital evidence.")

        val impact = if (!incident.transactionInfo.isNullOrBlank()) {
            "Direct Financial Impact noted: ${incident.transactionInfo}. Potential exposure of payment credentials."
        } else {
            "Exposure of personal data / contact channels. Risk of targeted follow-up social engineering."
        }

        IncidentSummary(
            incidentId = incident.id,
            whatHappened = incident.description,
            riskLevel = resolvedRiskLevel,
            evidence = incident.evidenceNote ?: "Digital log and communication trail preserved by victim.",
            timeline = "Occurred: ${incident.incidentDatetime} via ${incident.platform ?: "Online"}",
            impact = impact,
            nextSteps = nextSteps
        )
    }

    suspend fun generateComplaintDraft(
        incidentId: Int,
        userName: String,
        userEmail: String
    ): String = withContext(Dispatchers.IO) {
        val incident = incidentDao.getIncidentById(incidentId) ?: throw Exception("Incident not found")
        ComplaintDraftGenerator.generateComplaint(
            incident = incident,
            victimName = userName,
            victimEmail = userEmail
        )
    }

    suspend fun deleteThreatCheck(checkId: Int) = withContext(Dispatchers.IO) {
        threatCheckDao.deleteThreatCheckById(checkId)
    }

    suspend fun clearThreatChecks(userId: Int? = null) = withContext(Dispatchers.IO) {
        if (userId != null) {
            threatCheckDao.clearThreatChecksForUser(userId)
        } else {
            threatCheckDao.clearAllThreatChecks()
        }
    }

    suspend fun deleteIncident(incidentId: Int) = withContext(Dispatchers.IO) {
        incidentDao.deleteIncidentById(incidentId)
        threatRegisterDao.deleteBySourceIncidentId(incidentId)
    }

    suspend fun clearIncidents(userId: Int? = null) = withContext(Dispatchers.IO) {
        if (userId != null) {
            incidentDao.clearIncidentsForUser(userId)
        } else {
            incidentDao.clearAllIncidents()
        }
    }

    // Awareness
    fun getAllAwareness(): Flow<List<AwarenessContentEntity>> = awarenessDao.getAllAwarenessContent()

    // Quiz
    fun getAllQuizQuestions(): Flow<List<QuizQuestionEntity>> = quizDao.getAllQuizQuestions()

    suspend fun submitQuizAnswer(questionId: Int, selectedOptionIndex: Int): QuizAnswerResult = withContext(Dispatchers.IO) {
        val question = quizDao.getQuestionById(questionId) ?: throw Exception("Question not found")
        val isCorrect = question.correctIndex == selectedOptionIndex
        QuizAnswerResult(
            questionId = question.id,
            selectedIndex = selectedOptionIndex,
            correctIndex = question.correctIndex,
            isCorrect = isCorrect,
            explanation = question.explanation
        )
    }
}
