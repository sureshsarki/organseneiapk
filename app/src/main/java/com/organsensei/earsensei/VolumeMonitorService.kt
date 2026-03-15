package com.organsensei.earsensei

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat

class VolumeMonitorService : Service() {
    
    private lateinit var audioManager: AudioManager
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var calculator: HearingDamageCalculator
    private val handler = Handler(Looper.getMainLooper())
    private var isRunning = false
    
    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "ear_sensei_monitor"
        private const val MONITORING_INTERVAL = 10000L // 10 seconds
    }
    
    private val monitoringRunnable = object : Runnable {
        override fun run() {
            if (isRunning) {
                monitorVolume()
                handler.postDelayed(this, MONITORING_INTERVAL)
            }
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        dbHelper = DatabaseHelper(this)
        calculator = HearingDamageCalculator()
        
        createNotificationChannel()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification("Monitoring your hearing..."))
        
        isRunning = true
        handler.post(monitoringRunnable)
        
        return START_STICKY
    }
    
    private fun monitorVolume() {
        // Get current volume level
        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val volumePercent = (currentVolume.toFloat() / maxVolume * 100).toInt()
        
        // Estimate dB SPL (rough approximation)
        // Typical max headphone volume is around 100-110 dB
        val estimatedDb = 50 + (volumePercent * 0.5) // Maps 0-100% to 50-100 dB
        
        // Calculate damage contribution
        val damageScore = calculator.calculateDamageForInterval(
            estimatedDb.toDouble(),
            MONITORING_INTERVAL / 1000.0 // Convert to seconds
        )
        
        // Store in database
        dbHelper.addListeningSession(volumePercent, estimatedDb.toDouble(), damageScore)
        
        // Check if we need to alert
        val todayStats = dbHelper.getTodayStats()
        val dosePercent = (todayStats.dailyDose * 100).toInt()
        
        when {
            dosePercent >= 100 -> {
                showAlert("⚠️ Daily Limit Reached", "You've exceeded safe listening time today. Give your ears a rest!")
                updateNotification("⚠️ LIMIT REACHED: ${dosePercent}%")
            }
            dosePercent >= 75 -> {
                showAlert("⚠️ Approaching Limit", "You're at ${dosePercent}% of daily safe listening time")
                updateNotification("⚠️ High Exposure: ${dosePercent}%")
            }
            dosePercent >= 50 -> {
                updateNotification("⚡ Moderate: ${dosePercent}%")
            }
            else -> {
                updateNotification("✅ Safe: ${dosePercent}%")
            }
        }
    }
    
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Ear Sensei Monitoring",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Continuous hearing protection monitoring"
        }
        
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }
    
    private fun createNotification(content: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Ear Sensei")
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }
    
    private fun updateNotification(content: String) {
        val notification = createNotification(content)
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    
    private fun showAlert(title: String, message: String) {
        val alertNotification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(System.currentTimeMillis().toInt(), alertNotification)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        handler.removeCallbacks(monitoringRunnable)
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
}
