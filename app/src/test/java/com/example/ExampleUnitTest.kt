package com.example

import com.example.data.heuristic.ThreatScoringEngine
import com.example.data.model.IncidentEntity
import com.example.data.complaint.ComplaintDraftGenerator
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testPhishingKycThreatScore() {
        val testMessage = "Dear SBI customer, your SBI NetBanking account will be blocked within 24 hours. Update your KYC and verify Aadhaar immediately at http://sbi-kyc-update.xyz/login"
        val assessment = ThreatScoringEngine.assess(testMessage, "message")

        assertEquals("HIGH", assessment.severity)
        assertTrue(assessment.riskScore >= 70)
        assertEquals("phishing", assessment.threatCategory)
        assertTrue(assessment.indicators.isNotEmpty())
        assertTrue(assessment.recommendations.isNotEmpty())
    }

    @Test
    fun testJobScamThreatScore() {
        val testMessage = "Exclusive Part-time Job Offer! Like YouTube videos and earn Rs 5000 daily from home. Join Telegram: bit.ly/wfh-job"
        val assessment = ThreatScoringEngine.assess(testMessage, "message")

        assertTrue(assessment.riskScore >= 40)
        assertEquals("job_scam", assessment.threatCategory)
    }

    @Test
    fun testLegitimateMessageScore() {
        val testMessage = "Hello Mom, I have arrived safely at the train station. Will see you soon."
        val assessment = ThreatScoringEngine.assess(testMessage, "message")

        assertEquals("LOW", assessment.severity)
        assertTrue(assessment.riskScore < 40)
    }

    @Test
    fun testComplaintDraftGenerator() {
        val incident = IncidentEntity(
            id = 101,
            category = "UPI Banking Fraud",
            description = "Fraudulent caller pretending to be bank manager tricked me into entering UPI PIN.",
            platform = "Phone Call",
            incidentDatetime = "24-08-2026, 11:00 AM",
            transactionInfo = "UPI Ref: 4982910291, Amount: Rs 20,000",
            evidenceNote = "Caller Number: +91 98200XXXXX",
            riskLevel = "HIGH"
        )

        val draft = ComplaintDraftGenerator.generateComplaint(
            incident = incident,
            victimName = "Aditya Sharma",
            victimPhone = "+91 9876543210",
            victimEmail = "aditya@example.com"
        )

        assertTrue(draft.contains("FORMAL CYBERCRIME INCIDENT COMPLAINT"))
        assertTrue(draft.contains("Aditya Sharma"))
        assertTrue(draft.contains("UPI Ref: 4982910291"))
        assertTrue(draft.contains("Helpline 1930"))
    }
}
