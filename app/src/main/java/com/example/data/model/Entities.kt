package com.example.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    @ColumnInfo(name = "email", index = true)
    val email: String,
    @ColumnInfo(name = "password_hash")
    val passwordHash: String,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "threat_checks")
data class ThreatCheckEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "user_id")
    val userId: Int? = null,
    @ColumnInfo(name = "input_text")
    val inputText: String,
    @ColumnInfo(name = "input_type")
    val inputType: String = "message", // message, url, screenshot_text
    @ColumnInfo(name = "threat_category")
    val threatCategory: String, // phishing, financial_scam, job_scam, impersonation, other
    @ColumnInfo(name = "threat_type")
    val threatType: String,
    val severity: String, // LOW, MEDIUM, HIGH
    @ColumnInfo(name = "risk_score")
    val riskScore: Int,
    val confidence: Double,
    val indicators: String, // Delimited or JSON array
    val recommendations: String, // Delimited or JSON array
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "incidents")
data class IncidentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "user_id")
    val userId: Int? = null,
    val category: String,
    val description: String,
    val url: String? = null,
    val platform: String? = null,
    @ColumnInfo(name = "incident_datetime")
    val incidentDatetime: String,
    @ColumnInfo(name = "transaction_info")
    val transactionInfo: String? = null,
    @ColumnInfo(name = "evidence_note")
    val evidenceNote: String? = null,
    @ColumnInfo(name = "risk_level")
    val riskLevel: String = "MEDIUM",
    val status: String = "reported", // reported, under_review, resolved
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "threat_register")
data class ThreatRegisterEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val category: String,
    val severity: String, // LOW, MEDIUM, HIGH
    val status: String = "reported", // reported, under_review, verified
    @ColumnInfo(name = "source_incident_id")
    val sourceIncidentId: Int? = null,
    val description: String = "",
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "awareness_content")
data class AwarenessContentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val topic: String,
    val title: String,
    val body: String,
    val category: String,
    val keyTips: String = ""
)

@Entity(tableName = "quiz_questions")
data class QuizQuestionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val topic: String,
    val question: String,
    @ColumnInfo(name = "options_json")
    val optionsJson: String, // Pipe or JSON-separated options
    @ColumnInfo(name = "correct_index")
    val correctIndex: Int,
    val explanation: String
)

data class IncidentSummary(
    val incidentId: Int,
    val whatHappened: String,
    val riskLevel: String,
    val evidence: String,
    val timeline: String,
    val impact: String,
    val nextSteps: List<String>
)
