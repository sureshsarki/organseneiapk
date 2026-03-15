package com.organsensei.earsensei

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.*

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    
    companion object {
        private const val DATABASE_NAME = "ear_sensei.db"
        private const val DATABASE_VERSION = 1
        
        private const val TABLE_SESSIONS = "listening_sessions"
        private const val COLUMN_ID = "id"
        private const val COLUMN_TIMESTAMP = "timestamp"
        private const val COLUMN_VOLUME = "volume_percent"
        private const val COLUMN_DB_LEVEL = "db_level"
        private const val COLUMN_DAMAGE_SCORE = "damage_score"
        private const val COLUMN_DATE = "date"
    }
    
    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_SESSIONS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TIMESTAMP INTEGER NOT NULL,
                $COLUMN_VOLUME INTEGER NOT NULL,
                $COLUMN_DB_LEVEL REAL NOT NULL,
                $COLUMN_DAMAGE_SCORE REAL NOT NULL,
                $COLUMN_DATE TEXT NOT NULL
            )
        """.trimIndent()
        
        db.execSQL(createTable)
        
        // Create index on date for faster queries
        db.execSQL("CREATE INDEX idx_date ON $TABLE_SESSIONS($COLUMN_DATE)")
    }
    
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SESSIONS")
        onCreate(db)
    }
    
    /**
     * Add a new listening session to the database
     */
    fun addListeningSession(volumePercent: Int, dbLevel: Double, damageScore: Double): Long {
        val db = writableDatabase
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        
        val values = ContentValues().apply {
            put(COLUMN_TIMESTAMP, System.currentTimeMillis())
            put(COLUMN_VOLUME, volumePercent)
            put(COLUMN_DB_LEVEL, dbLevel)
            put(COLUMN_DAMAGE_SCORE, damageScore)
            put(COLUMN_DATE, currentDate)
        }
        
        return db.insert(TABLE_SESSIONS, null, values)
    }
    
    /**
     * Get statistics for today
     */
    fun getTodayStats(): DailyStats {
        val db = readableDatabase
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        
        val query = """
            SELECT 
                SUM($COLUMN_DAMAGE_SCORE) as total_damage,
                COUNT(*) as session_count,
                AVG($COLUMN_VOLUME) as avg_volume,
                MAX($COLUMN_VOLUME) as max_volume
            FROM $TABLE_SESSIONS
            WHERE $COLUMN_DATE = ?
        """.trimIndent()
        
        val cursor = db.rawQuery(query, arrayOf(today))
        
        val stats = if (cursor.moveToFirst()) {
            val totalDamage = cursor.getDouble(0)
            val sessionCount = cursor.getInt(1)
            val avgVolume = cursor.getInt(2)
            val maxVolume = cursor.getInt(3)
            
            // Each session is 10 seconds, calculate total minutes
            val totalMinutes = (sessionCount * 10) / 60
            
            DailyStats(
                dailyDose = totalDamage,
                totalMinutes = totalMinutes,
                currentVolume = avgVolume,
                peakVolume = maxVolume
            )
        } else {
            DailyStats(0.0, 0, 0, 0)
        }
        
        cursor.close()
        return stats
    }
    
    /**
     * Get weekly summary
     */
    fun getWeeklyStats(): List<Pair<String, Double>> {
        val db = readableDatabase
        val weekAgo = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -7)
        }
        val weekAgoDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(weekAgo.time)
        
        val query = """
            SELECT 
                $COLUMN_DATE,
                SUM($COLUMN_DAMAGE_SCORE) as daily_dose
            FROM $TABLE_SESSIONS
            WHERE $COLUMN_DATE >= ?
            GROUP BY $COLUMN_DATE
            ORDER BY $COLUMN_DATE
        """.trimIndent()
        
        val cursor = db.rawQuery(query, arrayOf(weekAgoDate))
        val results = mutableListOf<Pair<String, Double>>()
        
        while (cursor.moveToNext()) {
            val date = cursor.getString(0)
            val dose = cursor.getDouble(1)
            results.add(Pair(date, dose))
        }
        
        cursor.close()
        return results
    }
    
    /**
     * Clear old data (older than 30 days)
     */
    fun cleanOldData() {
        val db = writableDatabase
        val thirtyDaysAgo = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -30)
        }
        val cutoffDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(thirtyDaysAgo.time)
        
        db.delete(TABLE_SESSIONS, "$COLUMN_DATE < ?", arrayOf(cutoffDate))
    }
}

/**
 * Data class for daily statistics
 */
data class DailyStats(
    val dailyDose: Double,      // Total damage score (0.0 to 1.0+)
    val totalMinutes: Int,      // Total listening time in minutes
    val currentVolume: Int,     // Current/average volume
    val peakVolume: Int         // Peak volume today
)
