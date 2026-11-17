package com.example.data.complaint

import com.example.data.model.IncidentEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ComplaintDraftGenerator {

    fun generateComplaint(
        incident: IncidentEntity,
        victimName: String = "Concerned Citizen",
        victimPhone: String = "+91 98XXXXXXXX",
        victimEmail: String = "citizen@suraksha.in"
    ): String {
        val currentDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())

        return buildString {
            appendLine("FORMAL CYBERCRIME INCIDENT COMPLAINT")
            appendLine("=====================================")
            appendLine("To,")
            appendLine("The Officer-in-Charge / Cyber Crime Cell,")
            appendLine("National Cyber Crime Reporting Portal (cybercrime.gov.in / Helpline 1930)")
            appendLine()
            appendLine("Date of Filing: $currentDate")
            appendLine("Subject: Complaint regarding Cyber Incident: [${incident.category.uppercase(Locale.ROOT)}]")
            appendLine("Risk Assessment Level: ${incident.riskLevel}")
            appendLine()
            appendLine("1. COMPLAINANT DETAILS:")
            appendLine("   - Name: $victimName")
            appendLine("   - Contact Phone: $victimPhone")
            appendLine("   - Contact Email: $victimEmail")
            appendLine()
            appendLine("2. INCIDENT SUMMARY & PARTICULARS:")
            appendLine("   - Category of Cyber Offense: ${incident.category}")
            appendLine("   - Platform / Medium Used: ${incident.platform ?: "Online / Messaging"}")
            appendLine("   - Approximate Date & Time: ${incident.incidentDatetime}")
            if (!incident.url.isNullOrBlank()) {
                appendLine("   - Suspicious Link / Domain: ${incident.url}")
            }
            if (!incident.transactionInfo.isNullOrBlank()) {
                appendLine("   - Financial Transaction / UPI Reference: ${incident.transactionInfo}")
            }
            appendLine()
            appendLine("3. DETAILED STATEMENT OF FACTS:")
            appendLine("   ${incident.description.trim()}")
            appendLine()
            if (!incident.evidenceNote.isNullOrBlank()) {
                appendLine("4. EVIDENCE & TECHNICAL PARTICULARS:")
                appendLine("   ${incident.evidenceNote.trim()}")
                appendLine()
            }
            appendLine("5. PRAYER / RELIEF SOUGHT:")
            appendLine("   I earnestly request the cyber law enforcement authority to:")
            appendLine("   a) Register this formal grievance and initiate necessary investigative proceedings.")
            appendLine("   b) Issue urgent notices to trace the perpetrator IP / phone / wallet / bank accounts.")
            if (!incident.transactionInfo.isNullOrBlank()) {
                appendLine("   c) Direct the concerned payment gateway / bank to place a lien/freeze on the fraudulent transaction.")
            }
            appendLine("   d) Take down the malicious URL/domain to prevent other citizens from being targeted.")
            appendLine()
            appendLine("I hereby declare that the facts stated above are true and correct to the best of my knowledge.")
            appendLine()
            appendLine("Yours sincerely,")
            appendLine(victimName)
            appendLine("Generated via Suraksha Sutra Incident Response System")
        }
    }
}
