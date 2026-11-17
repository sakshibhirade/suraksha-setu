package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.AwarenessDao
import com.example.data.dao.IncidentDao
import com.example.data.dao.QuizDao
import com.example.data.dao.ThreatCheckDao
import com.example.data.dao.ThreatRegisterDao
import com.example.data.dao.UserDao
import com.example.data.model.AwarenessContentEntity
import com.example.data.model.IncidentEntity
import com.example.data.model.QuizQuestionEntity
import com.example.data.model.ThreatCheckEntity
import com.example.data.model.ThreatRegisterEntity
import com.example.data.model.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        ThreatCheckEntity::class,
        IncidentEntity::class,
        ThreatRegisterEntity::class,
        AwarenessContentEntity::class,
        QuizQuestionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun threatCheckDao(): ThreatCheckDao
    abstract fun incidentDao(): IncidentDao
    abstract fun threatRegisterDao(): ThreatRegisterDao
    abstract fun awarenessDao(): AwarenessDao
    abstract fun quizDao(): QuizDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "suraksha_sutra.db"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database)
                }
            }
        }
    }
}

suspend fun populateInitialData(database: AppDatabase) {
    val threatRegisterDao = database.threatRegisterDao()
    val awarenessDao = database.awarenessDao()
    val quizDao = database.quizDao()

    // 1. Seed Threat Register Entries
    if (threatRegisterDao.getCount() == 0) {
        val registerEntries = listOf(
            ThreatRegisterEntity(
                title = "Fake KYC Update SMS & APK Campaign",
                category = "phishing",
                severity = "HIGH",
                status = "under_review",
                description = "Scammers sending bulk SMS claiming bank accounts or SIM will be blocked unless user clicks on an unverified link (e.g. sbi-kyc-verify.xyz) to download a malicious APK."
            ),
            ThreatRegisterEntity(
                title = "Part-Time Telegram Video Liking Job Scam",
                category = "job_scam",
                severity = "MEDIUM",
                status = "reported",
                description = "Victims are added to Telegram groups promising Rs 3000/day for liking YouTube videos. After initial small payouts, scammers demand Rs 10,000+ advance 'prepaid task deposit'."
            ),
            ThreatRegisterEntity(
                title = "Electricity Bill Disconnection UPI Refund Scam",
                category = "financial_scam",
                severity = "HIGH",
                status = "verified",
                description = "Fraudulent SMS claiming electricity supply will be disconnected tonight. Victims calling the provided phone number are tricked into entering their UPI PIN on a request link."
            ),
            ThreatRegisterEntity(
                title = "Customs Parcel Impersonation Call Scam",
                category = "impersonation",
                severity = "HIGH",
                status = "verified",
                description = "Automated voice calls claiming an illegal package containing contraband in complainant's name has been intercepted by customs, demanding immediate digital police verification."
            )
        )
        threatRegisterDao.insertAll(registerEntries)
    }

    // 3. Seed Awareness Content
    if (awarenessDao.getCount() == 0) {
        val awarenessList = listOf(
            AwarenessContentEntity(
                topic = "Phishing & Fake URLs",
                title = "Spotting Malicious Links & Lookalike Domains",
                body = "Phishing is the most common cyber attack where criminals impersonate trusted institutions like banks, courier services, or government bodies to steal confidential data.\n\nKey Red Flags:\n1. Lookalike domain names (e.g. 'sbi-update.xyz' instead of 'onlinesbi.sbi').\n2. Use of generic URL shorteners (bit.ly, tinyurl) in official bank alerts.\n3. Spelling errors or urgent threats of account suspension.\n4. Non-HTTPS links asking for confidential credentials.",
                category = "phishing",
                keyTips = "Never click links received via SMS. Always type official bank URLs manually in your browser."
            ),
            AwarenessContentEntity(
                topic = "UPI & Payment Scams",
                title = "Golden Rule: PIN is Only for Sending Money",
                body = "UPI frauds exploit a basic misunderstanding: Entering your UPI PIN or scanning a QR code ALWAYS transfers money OUT of your account.\n\nCommon Traps:\n1. Scammer sends a 'Collect Request' claiming it is a cashback/refund.\n2. Seller on marketplace says 'Scan this QR to receive advance payment'.\n3. Fake customer care numbers on Google Maps asking you to install AnyDesk/TeamViewer.",
                category = "financial_scam",
                keyTips = "You NEVER need to enter your UPI PIN or scan a QR code to receive money."
            ),
            AwarenessContentEntity(
                topic = "Work From Home & Job Scams",
                title = "The Trap of Easy Online Part-Time Earnings",
                body = "With the rise of gig platforms, fraudsters lure students and job-seekers through WhatsApp/Telegram job offers that promise high daily returns for minimal work.\n\nHow the Scam Unfolds:\n1. Stage 1: Simple tasks like reviewing Google maps or liking YouTube videos for small Rs 100-200 payouts.\n2. Stage 2: Victims are invited to 'VIP tasks' requiring upfront investments of Rs 5,000 to Rs 50,000.\n3. Stage 3: The platform shows fake massive profits but freezes withdrawals demanding more 'clearance fees'.",
                category = "job_scam",
                keyTips = "A legitimate company will NEVER ask you to pay money or deposit security fees to get a job."
            ),
            AwarenessContentEntity(
                topic = "Password & Device Security",
                title = "Two-Factor Authentication & Screen Share Safety",
                body = "Securing your digital footprint requires proactive device hygiene.\n\nEssential Practices:\n1. Enable App-based Two-Factor Authentication (2FA) wherever supported.\n2. Never install screen-sharing apps (AnyDesk, TeamViewer, RustDesk) on instructions from unknown callers.\n3. Never reuse passwords across email, social media, and banking portals.\n4. Keep your smartphone OS and apps updated to patch known vulnerabilities.",
                category = "security_hygiene",
                keyTips = "If a caller asks you to install AnyDesk or share a 9-digit code, hang up immediately."
            )
        )
        awarenessDao.insertAll(awarenessList)
    }

    // 4. Seed Quiz Questions
    if (quizDao.getCount() == 0) {
        val quizQuestions = listOf(
            QuizQuestionEntity(
                topic = "Phishing & Fake URLs",
                question = "You receive an SMS: 'Your bank account will be suspended in 24 hours. Click bit.ly/kyc-sbi to update PAN.' What should you do?",
                optionsJson = "Click the link immediately to prevent suspension|Forward the SMS to friends|Ignore the link and check via official bank app or branch|Reply to SMS with your PAN details",
                correctIndex = 2,
                explanation = "Banks never send shortened bit.ly links or threaten sudden account suspension via generic SMS. Always verify via the official banking app."
            ),
            QuizQuestionEntity(
                topic = "UPI & Payment Scams",
                question = "A buyer on an online marketplace asks you to scan a QR code on WhatsApp to receive a Rs 500 advance payment. What happens if you scan and enter your PIN?",
                optionsJson = "Rs 500 will be credited to your account|Rs 500 will be deducted from your account|The transaction will be held in escrow|Nothing will happen without biometric authentication",
                correctIndex = 1,
                explanation = "Entering your UPI PIN authorizes a DEBIT. You never need to enter a PIN or scan a QR code to receive money."
            ),
            QuizQuestionEntity(
                topic = "Work From Home & Job Scams",
                question = "A recruiter on Telegram offers Rs 4000 daily for liking YouTube videos, but asks for a refundable registration fee of Rs 1500 first. Is this legitimate?",
                optionsJson = "Yes, registration fees are normal for digital marketing agencies|No, legitimate employers never charge job seekers fees to work|Yes, provided they show an official certificate on Telegram|Only if they promise double returns within 2 hours",
                correctIndex = 1,
                explanation = "Demanding upfront registration or task deposit fees is the hallmark signature of task-based employment scams."
            ),
            QuizQuestionEntity(
                topic = "Password & Device Security",
                question = "A caller claiming to be from your telecom company asks you to install 'QuickSupport' to fix your slow 5G internet. What is the danger?",
                optionsJson = "It will consume excessive mobile data|It gives the caller complete remote access to view your screen, SMS, and OTPs|It will uninstall your existing apps|It will automatically reboot your phone",
                correctIndex = 1,
                explanation = "Remote support apps like QuickSupport, AnyDesk, or TeamViewer allow fraudsters to view your active screen and capture incoming OTPs in real-time."
            ),
            QuizQuestionEntity(
                topic = "Phishing & Fake URLs",
                question = "Which of the following website URLs is the most secure and authentic for banking transactions?",
                optionsJson = "http://secure-sbi-login.net|https://www.onlinesbi.sbi|https://sbi-verification.xyz/login|http://192.168.1.10/sbi",
                correctIndex = 1,
                explanation = "Official banking domains use verified top-level domains (e.g. .sbi or official .com/.co.in) and encrypted HTTPS connections."
            )
        )
        quizDao.insertAll(quizQuestions)
    }
}
