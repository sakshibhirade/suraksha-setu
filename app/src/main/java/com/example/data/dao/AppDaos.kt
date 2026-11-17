package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AwarenessContentEntity
import com.example.data.model.IncidentEntity
import com.example.data.model.QuizQuestionEntity
import com.example.data.model.ThreatCheckEntity
import com.example.data.model.ThreatRegisterEntity
import com.example.data.model.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Int): UserEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserEntity): Long

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int
}

@Dao
interface ThreatCheckDao {
    @Query("SELECT * FROM threat_checks ORDER BY created_at DESC")
    fun getAllThreatChecks(): Flow<List<ThreatCheckEntity>>

    @Query("SELECT * FROM threat_checks WHERE user_id = :userId ORDER BY created_at DESC")
    fun getThreatChecksForUser(userId: Int): Flow<List<ThreatCheckEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertThreatCheck(check: ThreatCheckEntity): Long

    @Query("SELECT * FROM threat_checks WHERE id = :id LIMIT 1")
    suspend fun getThreatCheckById(id: Int): ThreatCheckEntity?

    @Query("DELETE FROM threat_checks WHERE id = :id")
    suspend fun deleteThreatCheckById(id: Int)

    @Query("DELETE FROM threat_checks WHERE user_id = :userId")
    suspend fun clearThreatChecksForUser(userId: Int)

    @Query("DELETE FROM threat_checks")
    suspend fun clearAllThreatChecks()
}

@Dao
interface IncidentDao {
    @Query("SELECT * FROM incidents ORDER BY created_at DESC")
    fun getAllIncidents(): Flow<List<IncidentEntity>>

    @Query("SELECT * FROM incidents WHERE user_id = :userId ORDER BY created_at DESC")
    fun getIncidentsForUser(userId: Int): Flow<List<IncidentEntity>>

    @Query("SELECT * FROM incidents WHERE id = :id LIMIT 1")
    suspend fun getIncidentById(id: Int): IncidentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncident(incident: IncidentEntity): Long

    @Update
    suspend fun updateIncident(incident: IncidentEntity)

    @Query("DELETE FROM incidents WHERE id = :id")
    suspend fun deleteIncidentById(id: Int)

    @Query("DELETE FROM incidents WHERE user_id = :userId")
    suspend fun clearIncidentsForUser(userId: Int)

    @Query("DELETE FROM incidents")
    suspend fun clearAllIncidents()
}

@Dao
interface ThreatRegisterDao {
    @Query("SELECT * FROM threat_register ORDER BY created_at DESC")
    fun getAllRegisterEntries(): Flow<List<ThreatRegisterEntity>>

    @Query("SELECT * FROM threat_register WHERE severity = :severity ORDER BY created_at DESC")
    fun getBySeverity(severity: String): Flow<List<ThreatRegisterEntity>>

    @Query("SELECT * FROM threat_register WHERE status = :status ORDER BY created_at DESC")
    fun getByStatus(status: String): Flow<List<ThreatRegisterEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRegisterEntry(entry: ThreatRegisterEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<ThreatRegisterEntity>)

    @Query("UPDATE threat_register SET severity = :severity WHERE source_incident_id = :sourceIncidentId")
    suspend fun updateSeverityBySourceIncident(sourceIncidentId: Int, severity: String)

    @Query("DELETE FROM threat_register WHERE source_incident_id = :sourceIncidentId")
    suspend fun deleteBySourceIncidentId(sourceIncidentId: Int)

    @Query("SELECT COUNT(*) FROM threat_register")
    suspend fun getCount(): Int
}

@Dao
interface AwarenessDao {
    @Query("SELECT * FROM awareness_content")
    fun getAllAwarenessContent(): Flow<List<AwarenessContentEntity>>

    @Query("SELECT * FROM awareness_content WHERE topic = :topic")
    fun getByTopic(topic: String): Flow<List<AwarenessContentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(contentList: List<AwarenessContentEntity>)

    @Query("SELECT COUNT(*) FROM awareness_content")
    suspend fun getCount(): Int
}

@Dao
interface QuizDao {
    @Query("SELECT * FROM quiz_questions")
    fun getAllQuizQuestions(): Flow<List<QuizQuestionEntity>>

    @Query("SELECT * FROM quiz_questions WHERE topic = :topic")
    fun getQuestionsByTopic(topic: String): Flow<List<QuizQuestionEntity>>

    @Query("SELECT * FROM quiz_questions WHERE id = :id LIMIT 1")
    suspend fun getQuestionById(id: Int): QuizQuestionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(questions: List<QuizQuestionEntity>)

    @Query("SELECT COUNT(*) FROM quiz_questions")
    suspend fun getCount(): Int
}
