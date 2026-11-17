package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.heuristic.ThreatAssessment
import com.example.data.heuristic.ThreatScoringEngine
import com.example.data.model.AwarenessContentEntity
import com.example.data.model.IncidentEntity
import com.example.data.model.IncidentSummary
import com.example.data.model.QuizQuestionEntity
import com.example.data.model.ThreatCheckEntity
import com.example.data.model.ThreatRegisterEntity
import com.example.data.model.UserEntity
import com.example.data.repository.QuizAnswerResult
import com.example.data.repository.SurakshaRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppTab(val title: String) {
    HOME("Home"),
    REPORT("Report"),
    REGISTER("Register"),
    LEARN("Learn")
}

data class AuthUiState(
    val currentUser: UserEntity? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

data class ThreatCheckUiState(
    val inputText: String = "",
    val inputType: String = "message", // message, url, screenshot_text
    val isScanning: Boolean = false,
    val latestAssessment: ThreatAssessment? = null,
    val latestCheckId: Long? = null
)

data class ReportFormUiState(
    val category: String = "Phishing & Fake Link",
    val description: String = "",
    val url: String = "",
    val platform: String = "SMS / Text Message",
    val incidentDatetime: String = "Today, 10:30 AM",
    val transactionInfo: String = "",
    val evidenceNote: String = "",
    val riskLevel: String = "MEDIUM",
    val isSubmitting: Boolean = false,
    val activeSummary: IncidentSummary? = null,
    val complaintDraft: String? = null,
    val errorMessage: String? = null,
    val isShowingDraftModal: Boolean = false
)

data class QuizUiState(
    val currentQuestionIndex: Int = 0,
    val selectedOptionIndex: Int? = null,
    val answerResult: QuizAnswerResult? = null,
    val score: Int = 0,
    val isCompleted: Boolean = false,
    val activeTopicFilter: String? = null
)

class SurakshaViewModel(
    private val repository: SurakshaRepository
) : ViewModel() {

    // Global Theme (null = follow system theme, true/false = manual override) & Tab Navigation
    private val _userThemePreference = MutableStateFlow<Boolean?>(null)
    val userThemePreference: StateFlow<Boolean?> = _userThemePreference.asStateFlow()

    // About Section Modal State
    private val _isAboutModalOpen = MutableStateFlow(false)
    val isAboutModalOpen: StateFlow<Boolean> = _isAboutModalOpen.asStateFlow()

    private val _currentTab = MutableStateFlow(AppTab.HOME)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    // Auth State
    private val _authState = MutableStateFlow(AuthUiState())
    val authState: StateFlow<AuthUiState> = _authState.asStateFlow()

    // Threat Check State
    private val _threatCheckState = MutableStateFlow(ThreatCheckUiState())
    val threatCheckState: StateFlow<ThreatCheckUiState> = _threatCheckState.asStateFlow()

    val scanHistory: StateFlow<List<ThreatCheckEntity>> = repository.getAllThreatChecks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Report Form State
    private val _reportState = MutableStateFlow(ReportFormUiState())
    val reportState: StateFlow<ReportFormUiState> = _reportState.asStateFlow()

    val myIncidents: StateFlow<List<IncidentEntity>> = repository.getAllIncidents()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Threat Register State
    private val _registerSeverityFilter = MutableStateFlow("ALL")
    val registerSeverityFilter: StateFlow<String> = _registerSeverityFilter.asStateFlow()

    private val _registerTabSelection = MutableStateFlow(0) // 0: Public Register, 1: My Incidents
    val registerTabSelection: StateFlow<Int> = _registerTabSelection.asStateFlow()

    val threatRegisterEntries: StateFlow<List<ThreatRegisterEntity>> = combine(
        repository.getThreatRegister(),
        _registerSeverityFilter
    ) { entries, filter ->
        if (filter == "ALL") entries else entries.filter { it.severity.equals(filter, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Awareness & Quiz
    val awarenessList: StateFlow<List<AwarenessContentEntity>> = repository.getAllAwareness()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allQuizQuestions: StateFlow<List<QuizQuestionEntity>> = repository.getAllQuizQuestions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _quizState = MutableStateFlow(QuizUiState())
    val quizState: StateFlow<QuizUiState> = _quizState.asStateFlow()

    init {
        // Automatically check if Firebase authenticated session exists
        viewModelScope.launch {
            val initialUser = repository.getInitialUser()
            if (initialUser != null) {
                _authState.value = _authState.value.copy(currentUser = initialUser)
            }
        }
    }

    fun toggleDarkMode(currentEffectiveDark: Boolean) {
        _userThemePreference.value = !currentEffectiveDark
    }

    fun openAboutModal() {
        _isAboutModalOpen.value = true
    }

    fun closeAboutModal() {
        _isAboutModalOpen.value = false
    }

    fun selectTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun setRegisterTabSelection(index: Int) {
        _registerTabSelection.value = index
    }

    // Auth Actions
    fun signInWithGoogle(context: Context, onComplete: ((Boolean) -> Unit)? = null) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, errorMessage = null)
            val result = repository.signInWithGoogle(context)
            result.onSuccess { user ->
                _authState.value = _authState.value.copy(
                    currentUser = user,
                    isLoading = false,
                    successMessage = "Google Sign-In Successful!"
                )
                onComplete?.invoke(true)
            }.onFailure { err ->
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    errorMessage = err.message ?: "Google Sign-In failed"
                )
                onComplete?.invoke(false)
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, errorMessage = null)
            val result = repository.login(email, password)
            result.onSuccess { user ->
                _authState.value = _authState.value.copy(currentUser = user, isLoading = false, successMessage = "Logged in successfully!")
            }.onFailure { err ->
                _authState.value = _authState.value.copy(isLoading = false, errorMessage = err.message)
            }
        }
    }

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, errorMessage = null)
            val result = repository.register(name, email, password)
            result.onSuccess { user ->
                _authState.value = _authState.value.copy(currentUser = user, isLoading = false, successMessage = "Account created!")
            }.onFailure { err ->
                _authState.value = _authState.value.copy(isLoading = false, errorMessage = err.message)
            }
        }
    }

    fun logout(context: Context? = null) {
        viewModelScope.launch {
            if (context != null) {
                repository.logout(context)
            }
            _authState.value = AuthUiState(currentUser = null)
        }
    }

    fun deleteIncident(incidentId: Int) {
        viewModelScope.launch {
            repository.deleteIncident(incidentId)
        }
    }

    fun clearMyIncidents() {
        viewModelScope.launch {
            val user = _authState.value.currentUser
            repository.clearIncidents(user?.id)
        }
    }

    fun deleteThreatCheck(checkId: Int) {
        viewModelScope.launch {
            repository.deleteThreatCheck(checkId)
        }
    }

    fun clearThreatChecks() {
        viewModelScope.launch {
            val user = _authState.value.currentUser
            repository.clearThreatChecks(user?.id)
        }
    }

    // Threat Scanner Actions
    fun updateScannerInput(text: String) {
        _threatCheckState.value = _threatCheckState.value.copy(inputText = text)
    }

    fun setScannerInputType(type: String) {
        _threatCheckState.value = _threatCheckState.value.copy(inputType = type)
    }

    fun loadSampleThreat(sampleType: String) {
        val sample = when (sampleType) {
            "kyc" -> Triple(
                "Dear SBI customer, your SBI NetBanking account will be blocked within 24 hours. Update your KYC and verify Aadhaar immediately at http://sbi-kyc-update.xyz/login",
                "message",
                "SBI Fake KYC SMS"
            )
            "job" -> Triple(
                "Exclusive Part-time Job Offer! Like YouTube videos and earn Rs 3000 to 8000 daily from home. Join our Telegram channel: bit.ly/wfh-task-earn",
                "message",
                "Telegram Task Scam"
            )
            "electricity" -> Triple(
                "Electricity power supply will be disconnected at 9:30 PM tonight due to unpaid bill. Immediately contact our electricity officer at 98200XXXXX or pay via link: http://192.168.1.55/pay",
                "message",
                "Electricity Bill Fraud"
            )
            "safe" -> Triple(
                "Your package from Amazon order #402-9871 has been shipped via BlueDart. Track progress in the official Amazon mobile app.",
                "message",
                "Legitimate Shipping Notice"
            )
            // URL Specific Presets
            "url_kyc" -> Triple(
                "http://sbi-kyc-update.xyz/login-verify",
                "url",
                "Insecure Fake Banking Portal"
            )
            "url_recharge" -> Triple(
                "http://free-5g-recharge-gift.top/claim-free-pack",
                "url",
                "Suspicious Free Gift TLD"
            )
            "url_job" -> Triple(
                "http://bit.ly/wfh-task-telegram-earn",
                "url",
                "Shortened Telegram Phishing Link"
            )
            "url_safe" -> Triple(
                "https://retail.onlinesbi.sbi/retail/login.htm",
                "url",
                "Legitimate Bank URL"
            )
            // OCR Screenshot Presets
            "ocr_warrant" -> Triple(
                "[OCR Scanned from Notice Image]\nCYBER POLICE & CBI ARREST NOTICE\nCase File: #CY-8491-2026\nYou are implicated in an illegal money laundering case. Immediate arrest warrant issued. Contact WhatsApp officer at +91-9988221100 within 2 hours or pay ₹45,000 security bail.",
                "screenshot_text",
                "Fake Police Digital Arrest Notice"
            )
            "ocr_upi" -> Triple(
                "[OCR Scanned from Payment Receipt]\nPayment Request: ₹15,000\nSender: Customer Support\nNote: To receive cash reward into your bank account, please approve request and enter your UPI PIN in Google Pay / PhonePe.",
                "screenshot_text",
                "Fake UPI Receive Request Trap"
            )
            "ocr_lottery" -> Triple(
                "[OCR Scanned from Prize Letter]\nKAUN BANEGA CROREPATI LUCKY DRAW 2026\nCongratulations! Your SIM has won ₹25 Lakhs cash prize. Deposit ₹12,500 tax registration fee to State Bank A/C to claim cheque.",
                "screenshot_text",
                "Fake Lottery Winner Certificate"
            )
            "ocr_safe_invoice" -> Triple(
                "[OCR Scanned from Official Bill]\nBESCOM Electricity Department - Official BBPS Receipt\nConsumer ID: 09283410\nBill Amount: ₹1,280.00\nPayment Status: COMPLETED via Bharat BillPay System (BBPS).",
                "screenshot_text",
                "Verified Utility Bill Receipt"
            )
            else -> Triple("", "message", "")
        }
        _threatCheckState.value = _threatCheckState.value.copy(
            inputText = sample.first,
            inputType = sample.second
        )
    }

    fun clearScanHistory() {
        viewModelScope.launch {
            repository.clearScanHistory()
        }
    }

    fun scanInput() {
        val input = _threatCheckState.value.inputText.trim()
        if (input.isBlank()) return

        viewModelScope.launch {
            _threatCheckState.value = _threatCheckState.value.copy(isScanning = true)
            delay(350) // smooth scanning animation effect
            val (assessment, id) = repository.performThreatCheck(
                inputText = input,
                inputType = _threatCheckState.value.inputType,
                userId = _authState.value.currentUser?.id
            )
            _threatCheckState.value = _threatCheckState.value.copy(
                isScanning = false,
                latestAssessment = assessment,
                latestCheckId = id
            )
        }
    }

    fun clearScanResult() {
        _threatCheckState.value = _threatCheckState.value.copy(
            latestAssessment = null,
            latestCheckId = null
        )
    }

    fun transferScanToReport() {
        val scan = _threatCheckState.value.latestAssessment ?: return
        val text = _threatCheckState.value.inputText
        _reportState.value = _reportState.value.copy(
            description = text,
            riskLevel = scan.severity,
            category = when (scan.threatCategory) {
                "phishing" -> "Phishing & Fake Link"
                "job_scam" -> "Fake Job / Work From Home Scam"
                "financial_scam" -> "UPI / Banking Scam"
                "impersonation" -> "Identity & Authority Impersonation"
                else -> "Other Cyber Offense"
            },
            evidenceNote = "Indicators detected: ${scan.indicators.joinToString("; ")}"
        )
        _currentTab.value = AppTab.REPORT
    }

    // Report Actions
    fun updateReportField(
        category: String? = null,
        description: String? = null,
        url: String? = null,
        platform: String? = null,
        datetime: String? = null,
        transactionInfo: String? = null,
        evidenceNote: String? = null
    ) {
        val newCategory = category ?: _reportState.value.category
        val newDescription = description ?: _reportState.value.description
        val newUrl = url ?: _reportState.value.url
        val newTransaction = transactionInfo ?: _reportState.value.transactionInfo

        val textToAssess = listOfNotNull(newDescription.ifBlank { null }, newUrl.ifBlank { null }).joinToString(" ")
        val assessment = if (textToAssess.isNotBlank()) ThreatScoringEngine.assess(textToAssess) else null

        val computedRisk = when {
            assessment?.severity == "HIGH" || newTransaction.isNotBlank() || newCategory.contains("UPI", ignoreCase = true) || newCategory.contains("Banking", ignoreCase = true) -> "HIGH"
            _reportState.value.riskLevel == "HIGH" && (description == null && url == null) -> "HIGH"
            assessment?.severity == "MEDIUM" -> "MEDIUM"
            _reportState.value.riskLevel == "MEDIUM" -> "MEDIUM"
            else -> assessment?.severity ?: "MEDIUM"
        }

        _reportState.value = _reportState.value.copy(
            category = newCategory,
            description = newDescription,
            url = newUrl,
            platform = platform ?: _reportState.value.platform,
            incidentDatetime = datetime ?: _reportState.value.incidentDatetime,
            transactionInfo = newTransaction,
            evidenceNote = evidenceNote ?: _reportState.value.evidenceNote,
            riskLevel = computedRisk
        )
    }

    fun submitReport() {
        val state = _reportState.value
        if (state.description.isBlank()) {
            _reportState.value = _reportState.value.copy(errorMessage = "Please provide an incident description.")
            return
        }

        viewModelScope.launch {
            _reportState.value = _reportState.value.copy(isSubmitting = true, errorMessage = null)
            val incident = repository.reportIncident(
                userId = _authState.value.currentUser?.id,
                category = state.category,
                description = state.description,
                url = state.url,
                platform = state.platform,
                incidentDatetime = state.incidentDatetime,
                transactionInfo = state.transactionInfo,
                evidenceNote = state.evidenceNote,
                riskLevel = state.riskLevel
            )
            val summary = repository.getIncidentSummary(incident.id)
            _reportState.value = _reportState.value.copy(
                isSubmitting = false,
                activeSummary = summary
            )
        }
    }

    fun openIncidentSummary(incident: IncidentEntity) {
        viewModelScope.launch {
            val summary = repository.getIncidentSummary(incident.id)
            _reportState.value = _reportState.value.copy(activeSummary = summary)
            _currentTab.value = AppTab.REPORT
        }
    }

    fun clearActiveSummary() {
        _reportState.value = _reportState.value.copy(activeSummary = null, complaintDraft = null)
    }

    fun generateComplaintDraft() {
        val summary = _reportState.value.activeSummary ?: return
        viewModelScope.launch {
            val user = _authState.value.currentUser
            val draft = repository.generateComplaintDraft(
                incidentId = summary.incidentId,
                userName = user?.name ?: "Aditya Sharma",
                userEmail = user?.email ?: "citizen@suraksha.in"
            )
            _reportState.value = _reportState.value.copy(
                complaintDraft = draft,
                isShowingDraftModal = true
            )
        }
    }

    fun updateComplaintDraftText(text: String) {
        _reportState.value = _reportState.value.copy(complaintDraft = text)
    }

    fun dismissComplaintModal() {
        _reportState.value = _reportState.value.copy(isShowingDraftModal = false)
    }

    // Threat Register Filter
    fun setSeverityFilter(filter: String) {
        _registerSeverityFilter.value = filter
    }

    // Quiz Actions
    fun filterQuizByTopic(topic: String?) {
        _quizState.value = _quizState.value.copy(
            activeTopicFilter = topic,
            currentQuestionIndex = 0,
            selectedOptionIndex = null,
            answerResult = null,
            score = 0,
            isCompleted = false
        )
    }

    fun selectQuizOption(index: Int) {
        if (_quizState.value.answerResult != null) return // Already submitted
        _quizState.value = _quizState.value.copy(selectedOptionIndex = index)
    }

    fun submitQuizAnswer(question: QuizQuestionEntity) {
        val selected = _quizState.value.selectedOptionIndex ?: return
        viewModelScope.launch {
            val result = repository.submitQuizAnswer(question.id, selected)
            val newScore = if (result.isCorrect) _quizState.value.score + 1 else _quizState.value.score
            _quizState.value = _quizState.value.copy(
                answerResult = result,
                score = newScore
            )
        }
    }

    fun nextQuizQuestion(totalQuestions: Int) {
        val nextIdx = _quizState.value.currentQuestionIndex + 1
        if (nextIdx >= totalQuestions) {
            _quizState.value = _quizState.value.copy(isCompleted = true)
        } else {
            _quizState.value = _quizState.value.copy(
                currentQuestionIndex = nextIdx,
                selectedOptionIndex = null,
                answerResult = null
            )
        }
    }

    fun resetQuiz() {
        _quizState.value = _quizState.value.copy(
            currentQuestionIndex = 0,
            selectedOptionIndex = null,
            answerResult = null,
            score = 0,
            isCompleted = false
        )
    }
}

class SurakshaViewModelFactory(
    private val repository: SurakshaRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SurakshaViewModel::class.java)) {
            return SurakshaViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
