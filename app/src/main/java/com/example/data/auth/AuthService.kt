package com.example.data.auth

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.example.data.api.SupabaseClient
import com.example.data.database.AppDatabase
import com.example.data.model.UserEntity
import com.google.android.gms.tasks.Task
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { continuation ->
    addOnSuccessListener { result ->
        if (continuation.isActive) continuation.resume(result)
    }
    addOnFailureListener { exception ->
        if (continuation.isActive) continuation.resumeWithException(exception)
    }
    addOnCanceledListener {
        continuation.cancel()
    }
}

class AuthService(
    private val database: AppDatabase,
    private val appContext: Context? = null
) {
    private val TAG = "AuthService"
    private val userDao = database.userDao()

    private val _currentFirebaseUser = MutableStateFlow<FirebaseUser?>(null)
    val currentFirebaseUser: StateFlow<FirebaseUser?> = _currentFirebaseUser.asStateFlow()

    init {
        initFirebaseSafely()
    }

    private fun initFirebaseSafely(): FirebaseAuth? {
        return try {
            if (appContext != null && FirebaseApp.getApps(appContext).isEmpty()) {
                FirebaseApp.initializeApp(appContext)
            }
            val auth = FirebaseAuth.getInstance()
            _currentFirebaseUser.value = auth.currentUser
            auth.addAuthStateListener { updatedAuth ->
                _currentFirebaseUser.value = updatedAuth.currentUser
            }
            auth
        } catch (e: Exception) {
            Log.d(TAG, "Firebase initialization notice: ${e.message}")
            null
        }
    }

    private fun getAuthInstance(context: Context? = null): FirebaseAuth? {
        return try {
            val ctx = context ?: appContext
            if (ctx != null && FirebaseApp.getApps(ctx).isEmpty()) {
                FirebaseApp.initializeApp(ctx)
            }
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            Log.d(TAG, "FirebaseAuth instance notice: ${e.message}")
            null
        }
    }

    suspend fun getInitialUser(): UserEntity? = withContext(Dispatchers.IO) {
        try {
            val auth = getAuthInstance()
            val fbUser = auth?.currentUser
            if (fbUser != null && !fbUser.email.isNullOrBlank()) {
                val email = fbUser.email!!
                val existing = userDao.getUserByEmail(email)
                if (existing != null) {
                    return@withContext existing
                } else {
                    val newUser = UserEntity(
                        name = fbUser.displayName ?: email.substringBefore("@").replaceFirstChar { it.uppercase() },
                        email = email,
                        passwordHash = "firebase_auth_${fbUser.uid}"
                    )
                    val id = userDao.insertUser(newUser)
                    return@withContext newUser.copy(id = id.toInt())
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "Initial Firebase session check completed: ${e.message}")
        }
        null
    }

    suspend fun signInWithGoogle(
        context: Context,
        serverClientId: String? = null
    ): Result<UserEntity> = withContext(Dispatchers.IO) {
        try {
            val credentialManager = CredentialManager.create(context)

            // Resolve server client ID from argument, resources, or fallback
            val clientId = serverClientId
                ?: try {
                    val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
                    if (resId != 0) context.getString(resId) else null
                } catch (e: Exception) {
                    null
                } ?: "273799865992-surakshasetu-web.apps.googleusercontent.com"

            // setAutoSelectEnabled(false) ensures the account picker is displayed if multiple accounts exist
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(clientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = withContext(Dispatchers.Main) {
                credentialManager.getCredential(request = request, context = context)
            }

            val credential = result.credential
            if (credential is androidx.credentials.CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken

                val auth = getAuthInstance(context)
                val firebaseUser: FirebaseUser? = if (auth != null) {
                    try {
                        val authCredential = GoogleAuthProvider.getCredential(idToken, null)
                        val authResult: AuthResult = auth.signInWithCredential(authCredential).await()
                        authResult.user
                    } catch (e: Exception) {
                        Log.w(TAG, "Firebase credential sign-in notice: ${e.message}")
                        null
                    }
                } else null

                val displayName = firebaseUser?.displayName
                    ?: googleIdTokenCredential.displayName
                    ?: googleIdTokenCredential.id.substringBefore("@")
                val email = firebaseUser?.email ?: googleIdTokenCredential.id
                val userUid = firebaseUser?.uid ?: idToken.hashCode().toString()

                val userEntity = syncOrCreateUser(
                    name = displayName,
                    email = email,
                    providerKey = "firebase_google_$userUid"
                )
                return@withContext Result.success(userEntity)
            } else {
                return@withContext Result.failure(Exception("Unsupported credential received from Google."))
            }

        } catch (e: GetCredentialCancellationException) {
            Log.d(TAG, "Google Sign-in was cancelled by user")
            return@withContext Result.failure(Exception("Google Sign-In was cancelled."))
        } catch (e: GetCredentialException) {
            Log.e(TAG, "Credential Manager error during Google Sign-in: ${e.message}", e)
            val msg = e.localizedMessage ?: "Google Sign-In is unavailable on this device configuration. You can also sign in or register with email."
            return@withContext Result.failure(Exception(msg))
        } catch (e: Exception) {
            Log.e(TAG, "Google Auth error: ${e.message}", e)
            return@withContext Result.failure(Exception("Authentication error: ${e.localizedMessage ?: "Please try again or use email sign-in."}"))
        }
    }

    suspend fun signInWithEmail(email: String, password: String): Result<UserEntity> = withContext(Dispatchers.IO) {
        try {
            val auth = getAuthInstance()
            if (auth != null) {
                try {
                    val authResult = auth.signInWithEmailAndPassword(email.trim(), password).await()
                    val fbUser = authResult.user
                    val name = fbUser?.displayName ?: email.substringBefore("@").replaceFirstChar { it.uppercase() }
                    val user = syncOrCreateUser(name = name, email = email.trim(), providerKey = "firebase_email_${fbUser?.uid}")
                    return@withContext Result.success(user)
                } catch (e: Exception) {
                    Log.d(TAG, "Firebase signInWithEmail fallback to Room DB: ${e.message}")
                }
            }

            // Local Room Database verification
            val localUser = userDao.getUserByEmail(email.trim())
            if (localUser != null && (localUser.passwordHash == password || localUser.passwordHash.startsWith("firebase_"))) {
                // Sync to Supabase in background
                SupabaseClient.syncUser(localUser.email, localUser.name)
                Result.success(localUser)
            } else {
                Result.failure(Exception("Invalid email or password."))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Sign in failed."))
        }
    }

    suspend fun registerWithEmail(name: String, email: String, password: String): Result<UserEntity> = withContext(Dispatchers.IO) {
        try {
            val auth = getAuthInstance()
            if (auth != null) {
                try {
                    val authResult = auth.createUserWithEmailAndPassword(email.trim(), password).await()
                    val fbUser = authResult.user
                    val user = syncOrCreateUser(name = name.trim(), email = email.trim(), providerKey = "firebase_email_${fbUser?.uid}")
                    return@withContext Result.success(user)
                } catch (e: Exception) {
                    Log.d(TAG, "Firebase createUserWithEmail fallback to Room DB: ${e.message}")
                }
            }

            val existing = userDao.getUserByEmail(email.trim())
            if (existing != null) {
                return@withContext Result.failure(Exception("An account with this email already exists."))
            }
            val newUser = UserEntity(
                name = name.trim(),
                email = email.trim(),
                passwordHash = password
            )
            val id = userDao.insertUser(newUser)
            val createdUser = newUser.copy(id = id.toInt())

            // Sync with Supabase cloud database
            SupabaseClient.syncUser(createdUser.email, createdUser.name)

            Result.success(createdUser)
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Registration failed."))
        }
    }

    suspend fun signOut(context: Context) = withContext(Dispatchers.IO) {
        try {
            getAuthInstance(context)?.signOut()
            val credentialManager = CredentialManager.create(context)
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (e: Exception) {
            Log.d(TAG, "Sign out error: ${e.message}")
        }
    }

    private suspend fun syncOrCreateUser(name: String, email: String, providerKey: String): UserEntity {
        // Sync user profile to Supabase database
        SupabaseClient.syncUser(email.trim(), name.trim())

        val existing = userDao.getUserByEmail(email.trim())
        return if (existing != null) {
            existing
        } else {
            val newUser = UserEntity(
                name = name.trim(),
                email = email.trim(),
                passwordHash = providerKey
            )
            val id = userDao.insertUser(newUser)
            newUser.copy(id = id.toInt())
        }
    }
}
