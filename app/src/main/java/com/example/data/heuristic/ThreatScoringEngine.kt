package com.example.data.heuristic

import java.util.Locale
import kotlin.math.min

data class ThreatAssessment(
    val riskScore: Int,
    val severity: String, // LOW, MEDIUM, HIGH
    val confidence: Double,
    val threatCategory: String, // phishing, financial_scam, job_scam, impersonation, other
    val threatType: String,
    val indicators: List<String>,
    val recommendations: List<String>
)

object ThreatScoringEngine {

    // Regex and keyword definitions
    private val SHORTENED_URL_PATTERN = Regex(
        "(?:bit\\.ly|tinyurl\\.com|t\\.co|is\\.gd|cutt\\.ly|goo\\.gl|rb\\.gy|shorturl\\.at|ow\\.ly|wa\\.me|t\\.me|ngrok\\.io|serveo\\.net)",
        RegexOption.IGNORE_CASE
    )
    private val RAW_IP_URL_PATTERN = Regex(
        "(?:http[s]?://)?(?:\\d{1,3}\\.){3}\\d{1,3}(?::\\d+)?",
        RegexOption.IGNORE_CASE
    )
    private val SUSPICIOUS_TLD_PATTERN = Regex(
        "\\b[a-zA-Z0-9-]+\\.(?:xyz|top|work|online|site|club|buzz|click|info|cc|tk|ml|ga|cf|gq|live|shop)\\b",
        RegexOption.IGNORE_CASE
    )
    private val GENERAL_URL_PATTERN = Regex(
        "(https?://|www\\.)[a-zA-Z0-9./?=_%&:-]+",
        RegexOption.IGNORE_CASE
    )
    private val NON_HTTPS_LINK_PATTERN = Regex(
        "http://[a-zA-Z0-9./?=_%&:-]+",
        RegexOption.IGNORE_CASE
    )

    private val URGENCY_KEYWORDS = listOf(
        "immediately", "within 24 hours", "24 hours", "account will be blocked",
        "suspended today", "urgent action required", "last notice", "immediate action",
        "electricity will be disconnected", "service cutoff", "act now", "expires in",
        "final warning", "deactivation notice", "sim will be blocked", "kyc expired"
    )

    private val SENSITIVE_INFO_KEYWORDS = listOf(
        "otp", "pin", "cvv", "password", "verify your kyc", "kyc verification",
        "update pan", "aadhaar", "debit card details", "credit card number",
        "enter secret code", "net banking password", "upi pin", "mpin",
        "share screen", "anydesk", "teamviewer", "rustdesk", "quicksupport"
    )

    private val BRAND_BANK_KEYWORDS = listOf(
        "sbi", "state bank", "hdfc", "icici", "axis bank", "punjab national", "pnb",
        "paytm", "phonepe", "google pay", "gpay", "bhim", "income tax department",
        "department of telecommunications", "dot", "trai", "police department",
        "cbi", "customs department", "amazon customer care", "flipkart support",
        "netflix account", "fedex parcel", "speed post"
    )

    private val GENERIC_SCAM_KEYWORDS = listOf(
        "lottery", "congratulations you won", "cashback of", "unclaimed refund",
        "part-time job", "work from home", "earn 5000 daily", "earn money daily",
        "like youtube videos and earn", "instant loan approved", "zero interest loan",
        "crypto trading guaranteed", "double your money", "free recharge",
        "gift card winner", "survey rewards", "lucky draw", "claim prize"
    )

    fun assess(inputText: String, inputType: String = "message"): ThreatAssessment {
        val lowerText = inputText.lowercase(Locale.ROOT)
        var totalScore = 0
        val matchedIndicators = mutableListOf<String>()
        val recommendations = mutableListOf<String>()
        var categoriesFiredCount = 0

        var hasUrlPattern = false
        var hasUrgency = false
        var hasSensitiveInfo = false
        var hasBrandWithLink = false
        var hasGenericScam = false

        // 1. Check Suspicious URL Patterns (+25)
        val hasShortened = SHORTENED_URL_PATTERN.containsMatchIn(inputText)
        val hasRawIp = RAW_IP_URL_PATTERN.containsMatchIn(inputText)
        val hasSuspiciousTld = SUSPICIOUS_TLD_PATTERN.containsMatchIn(inputText)
        val hasNonHttps = NON_HTTPS_LINK_PATTERN.containsMatchIn(inputText)
        val hasAnyUrl = GENERAL_URL_PATTERN.containsMatchIn(inputText) || inputType == "url"

        if (hasShortened || hasRawIp || hasSuspiciousTld || hasNonHttps) {
            hasUrlPattern = true
            totalScore += 25
            categoriesFiredCount++
            if (hasShortened) matchedIndicators.add("Obfuscated/Shortened URL redirection detected")
            if (hasRawIp) matchedIndicators.add("Direct IP address URL used instead of legitimate domain")
            if (hasSuspiciousTld) matchedIndicators.add("High-risk atypical domain extension (.xyz/.online/.top)")
            if (hasNonHttps) matchedIndicators.add("Insecure unencrypted HTTP connection")
            recommendations.add("Do not click the link or enter credentials on the landing page.")
            recommendations.add("Check domain authenticity on official portals (e.g. check domain whois).")
        } else if (hasAnyUrl && inputType == "url") {
            // General URL scan
            matchedIndicators.add("External hyperlink provided for analysis")
        }

        // 2. Check Urgency / Pressure Language (+20)
        val matchedUrgencyWords = URGENCY_KEYWORDS.filter { lowerText.contains(it) }
        if (matchedUrgencyWords.isNotEmpty()) {
            hasUrgency = true
            totalScore += 20
            categoriesFiredCount++
            matchedIndicators.add("High-pressure urgency tactics: \"${matchedUrgencyWords.take(2).joinToString("\", \"")}\"")
            recommendations.add("Legitimate institutions never demand emergency action within short countdown windows.")
            recommendations.add("Take a deep breath and verify directly via known official numbers.")
        }

        // 3. Check Requests for Sensitive Info (+30)
        val matchedSensitiveWords = SENSITIVE_INFO_KEYWORDS.filter { lowerText.contains(it) }
        if (matchedSensitiveWords.isNotEmpty()) {
            hasSensitiveInfo = true
            totalScore += 30
            categoriesFiredCount++
            matchedIndicators.add("Requests for confidential auth/KYC data: \"${matchedSensitiveWords.take(2).joinToString("\", \"")}\"")
            recommendations.add("NEVER share OTP, UPI PIN, passwords, or install remote access apps (AnyDesk/TeamViewer).")
            recommendations.add("Banks and government agencies NEVER ask for full credentials or screen sharing.")
        }

        // 4. Bank / Brand Impersonation Keywords + Link Present (+15)
        val matchedBrandWords = BRAND_BANK_KEYWORDS.filter { lowerText.contains(it) }
        if (matchedBrandWords.isNotEmpty() && (hasAnyUrl || lowerText.contains("click") || lowerText.contains("link") || lowerText.contains("call"))) {
            hasBrandWithLink = true
            totalScore += 15
            categoriesFiredCount++
            matchedIndicators.add("Brand/Authority impersonation targeting \"${matchedBrandWords.first()}\" with call-to-action link")
            recommendations.add("Contact the official customer support through their verified app or website directly.")
        }

        // 5. Generic Scam Markers (+10)
        val matchedScamWords = GENERIC_SCAM_KEYWORDS.filter { lowerText.contains(it) }
        if (matchedScamWords.isNotEmpty()) {
            hasGenericScam = true
            totalScore += 10
            categoriesFiredCount++
            matchedIndicators.add("Enticement/Financial bait marker: \"${matchedScamWords.take(2).joinToString("\", \"")}\"")
            recommendations.add("Be skeptical of unsolicited job offers, unexpected lottery claims, or effortless daily income.")
        }

        // If zero indicators matched but user provided text, provide baseline safe assessment
        if (matchedIndicators.isEmpty()) {
            matchedIndicators.add("No blatant phishing indicators, urgency triggers, or malicious signatures detected.")
            recommendations.add("Always maintain caution before downloading unknown files or authorizing financial transfers.")
            recommendations.add("Ensure 2-factor authentication is active on all personal accounts.")
        }

        val riskScore = min(100, totalScore)

        val severity = when {
            riskScore >= 70 -> "HIGH"
            riskScore >= 40 -> "MEDIUM"
            else -> "LOW"
        }

        // Confidence formula: scales with how many categories fired (bounded between 0.60 and 0.95)
        val baseConfidence = if (categoriesFiredCount == 0) 0.80 else 0.55 + (categoriesFiredCount * 0.10)
        val confidence = min(0.95, baseConfidence)

        // Classify threat category & type
        val (threatCategory, threatType) = when {
            (hasSensitiveInfo || hasBrandWithLink) && hasUrlPattern -> {
                Pair("phishing", "Credential Harvesting Phishing")
            }
            hasSensitiveInfo && lowerText.contains("upi") || lowerText.contains("paytm") || lowerText.contains("refund") || lowerText.contains("bill") -> {
                Pair("financial_scam", "UPI / Banking Payment Scam")
            }
            lowerText.contains("job") || lowerText.contains("part-time") || lowerText.contains("task") || lowerText.contains("youtube") -> {
                Pair("job_scam", "Fake Part-Time Job Scam")
            }
            hasBrandWithLink || lowerText.contains("customs") || lowerText.contains("trai") || lowerText.contains("police") -> {
                Pair("impersonation", "Executive / Authority Impersonation")
            }
            hasUrlPattern -> {
                Pair("phishing", "Suspicious Link Redirection")
            }
            hasGenericScam -> {
                Pair("financial_scam", "Lottery / Prize Bait")
            }
            else -> {
                if (riskScore >= 40) Pair("phishing", "Unverified Suspicious Message")
                else Pair("other", "Safe / Low Risk Communication")
            }
        }

        // Universal final recommendation if severity is medium/high
        if (severity == "HIGH") {
            recommendations.add("Report this incident to the National Cybercrime Portal (cybercrime.gov.in or helpline 1930).")
        }

        return ThreatAssessment(
            riskScore = riskScore,
            severity = severity,
            confidence = (confidence * 100).toInt() / 100.0,
            threatCategory = threatCategory,
            threatType = threatType,
            indicators = matchedIndicators,
            recommendations = recommendations.distinct()
        )
    }
}
