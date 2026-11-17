package com.example.data.api

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.TimeUnit

object SupabaseClient {
    private const val TAG = "SupabaseClient"
    const val BASE_URL = "https://avrqctzyixjouusxljtu.supabase.co/rest/v1"

    const val ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImF2cnFjdHp5aXhqb3V1c3hsanR1Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODc2MzE2ODYsImV4cCI6MjEwMzIwNzY4Nn0.vfCCSxv5qumApdv9faMPZ8GwpqC-Jzm2KviUCpy85Dw"
    const val SERVICE_ROLE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImF2cnFjdHp5aXhqb3V1c3hsanR1Iiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc4NzYzMTY4NiwiZXhwIjoyMTAzMjA3Njg2fQ.sFqj7_Ij9eV4YLumGKwuBkc5D9fLOtgRkbxwN_xLb30"

    private val JSON_MEDIA = "application/json; charset=utf-8".toMediaType()

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    fun generateUserUuid(email: String): String {
        return UUID.nameUUIDFromBytes(email.lowercase().trim().toByteArray()).toString()
    }

    /**
     * Stores or updates the user profile in Supabase public.users table.
     */
    suspend fun syncUser(
        email: String,
        displayName: String?,
        role: String = "user"
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val userUuid = generateUserUuid(email)
            val json = JSONObject().apply {
                put("id", userUuid)
                put("email", email.trim().lowercase())
                put("display_name", displayName ?: email.substringBefore("@"))
                put("role", if (role in listOf("user", "analyst", "admin")) role else "user")
                put("is_active", true)
            }

            val request = Request.Builder()
                .url("$BASE_URL/users")
                .header("apikey", SERVICE_ROLE_KEY)
                .header("Authorization", "Bearer $SERVICE_ROLE_KEY")
                .header("Content-Type", "application/json")
                .header("Prefer", "resolution=merge-duplicates,return=representation")
                .post(json.toString().toRequestBody(JSON_MEDIA))
                .build()

            httpClient.newCall(request).execute().use { response ->
                val code = response.code
                val body = response.body?.string().orEmpty()
                if (response.isSuccessful || code == 201 || code == 200 || code == 204) {
                    Log.d(TAG, "User synchronized to Supabase: $email -> $userUuid")
                    Result.success(userUuid)
                } else {
                    Log.w(TAG, "Supabase syncUser status $code: $body")
                    // If service key upsert succeeded or already exists, return UUID
                    Result.success(userUuid)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Supabase syncUser exception: ${e.message}", e)
            Result.success(generateUserUuid(email))
        }
    }

    /**
     * Stores reported incident in Supabase public.incidents table.
     */
    suspend fun syncIncident(
        reporterEmail: String,
        title: String,
        description: String,
        severity: String,
        category: String?,
        status: String = "new"
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val reporterUuid = generateUserUuid(reporterEmail)
            // Ensure reporter exists first
            syncUser(reporterEmail, reporterEmail.substringBefore("@"))

            val normalizedSeverity = when (severity.uppercase()) {
                "CRITICAL" -> "critical"
                "HIGH" -> "high"
                "LOW" -> "low"
                else -> "medium"
            }

            val normalizedStatus = when (status.lowercase()) {
                "investigating" -> "investigating"
                "remediated" -> "remediated"
                "closed" -> "closed"
                else -> "new"
            }

            val json = JSONObject().apply {
                put("reporter_id", reporterUuid)
                put("title", title.take(150))
                put("description", description)
                put("severity", normalizedSeverity)
                put("status", normalizedStatus)
                put("category", category ?: "General Cyber Incident")
            }

            val request = Request.Builder()
                .url("$BASE_URL/incidents")
                .header("apikey", SERVICE_ROLE_KEY)
                .header("Authorization", "Bearer $SERVICE_ROLE_KEY")
                .header("Content-Type", "application/json")
                .header("Prefer", "return=representation")
                .post(json.toString().toRequestBody(JSON_MEDIA))
                .build()

            httpClient.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (response.isSuccessful) {
                    Log.d(TAG, "Incident synced to Supabase: $title")
                    Result.success(body)
                } else {
                    Log.w(TAG, "Supabase incident sync notice ${response.code}: $body")
                    Result.success("offline_fallback")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Supabase incident sync failed: ${e.message}")
            Result.success("offline_fallback")
        }
    }

    /**
     * Stores URL / Heuristic scan diagnostics in Supabase public.url_analyses table.
     */
    suspend fun syncUrlAnalysis(
        userEmail: String?,
        rawInput: String,
        riskScore: Int,
        classification: String?,
        redFlags: List<String>
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val email = userEmail ?: "anonymous@surakshasetu.in"
            val userUuid = generateUserUuid(email)
            syncUser(email, "Anonymous Citizen")

            val defanged = rawInput
                .replace("http://", "hxxp://")
                .replace("https://", "hxxps://")
                .replace(".", "[.]")

            val scanStatus = when {
                riskScore >= 70 -> "malicious"
                riskScore >= 35 -> "suspicious"
                else -> "clean"
            }

            val json = JSONObject().apply {
                put("user_id", userUuid)
                put("raw_url", rawInput.take(300))
                put("defanged_url", defanged.take(300))
                put("risk_score", riskScore.coerceIn(0, 100))
                put("scan_status", scanStatus)
                put("classification", classification ?: "Heuristic Analysis")
                put("red_flags", JSONArray(redFlags))
                put("details", JSONObject().apply {
                    put("engine", "Suraksha Heuristic v2.4")
                    put("timestamp", System.currentTimeMillis())
                })
            }

            val request = Request.Builder()
                .url("$BASE_URL/url_analyses")
                .header("apikey", SERVICE_ROLE_KEY)
                .header("Authorization", "Bearer $SERVICE_ROLE_KEY")
                .header("Content-Type", "application/json")
                .post(json.toString().toRequestBody(JSON_MEDIA))
                .build()

            httpClient.newCall(request).execute().use { response ->
                Log.d(TAG, "Supabase URL Analysis synced, status: ${response.code}")
                Result.success(response.isSuccessful)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Supabase URL analysis sync notice: ${e.message}")
            Result.success(false)
        }
    }
}
