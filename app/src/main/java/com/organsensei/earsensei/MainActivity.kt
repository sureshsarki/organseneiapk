package com.organsensei.earsensei

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {
    
    private lateinit var dailyDoseText: TextView
    private lateinit var dailyDoseProgress: ProgressBar
    private lateinit var listeningTimeText: TextView
    private lateinit var currentVolumeText: TextView
    private lateinit var statusText: TextView
    private lateinit var startButton: Button
    private lateinit var stopButton: Button
    
    private lateinit var dbHelper: DatabaseHelper
    private var isMonitoring = false
    
    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Initialize database
        dbHelper = DatabaseHelper(this)
        
        // Initialize views
        initializeViews()
        
        // Check permissions
        checkPermissions()
        
        // Update UI with current stats
        updateStats()
        
        // Set up button listeners
        setupButtons()
    }
    
    private fun initializeViews() {
        dailyDoseText = findViewById(R.id.dailyDoseText)
        dailyDoseProgress = findViewById(R.id.dailyDoseProgress)
        listeningTimeText = findViewById(R.id.listeningTimeText)
        currentVolumeText = findViewById(R.id.currentVolumeText)
        statusText = findViewById(R.id.statusText)
        startButton = findViewById(R.id.startButton)
        stopButton = findViewById(R.id.stopButton)
    }
    
    private fun setupButtons() {
        startButton.setOnClickListener {
            startMonitoring()
        }
        
        stopButton.setOnClickListener {
            stopMonitoring()
        }
    }
    
    private fun checkPermissions() {
        val permissions = arrayOf(
            Manifest.permission.MODIFY_AUDIO_SETTINGS,
            Manifest.permission.POST_NOTIFICATIONS
        )
        
        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        
        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                PERMISSION_REQUEST_CODE
            )
        }
    }
    
    private fun startMonitoring() {
        val serviceIntent = Intent(this, VolumeMonitorService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)
        
        isMonitoring = true
        updateButtonStates()
        statusText.text = "Monitoring Active"
        statusText.setTextColor(getColor(android.R.color.holo_green_dark))
    }
    
    private fun stopMonitoring() {
        val serviceIntent = Intent(this, VolumeMonitorService::class.java)
        stopService(serviceIntent)
        
        isMonitoring = false
        updateButtonStates()
        statusText.text = "Monitoring Stopped"
        statusText.setTextColor(getColor(android.R.color.holo_red_dark))
    }
    
    private fun updateButtonStates() {
        startButton.isEnabled = !isMonitoring
        stopButton.isEnabled = isMonitoring
    }
    
    private fun updateStats() {
        val stats = dbHelper.getTodayStats()
        
        // Update daily dose percentage
        val dosePercent = (stats.dailyDose * 100).roundToInt()
        dailyDoseText.text = "${dosePercent}%"
        dailyDoseProgress.progress = dosePercent
        
        // Change color based on dose level
        val color = when {
            dosePercent < 50 -> android.R.color.holo_green_dark
            dosePercent < 75 -> android.R.color.holo_orange_dark
            else -> android.R.color.holo_red_dark
        }
        dailyDoseProgress.progressTintList = ContextCompat.getColorStateList(this, color)
        
        // Update listening time
        val hours = stats.totalMinutes / 60
        val minutes = stats.totalMinutes % 60
        listeningTimeText.text = if (hours > 0) {
            "${hours}h ${minutes}m"
        } else {
            "${minutes}m"
        }
        
        // Update current volume
        currentVolumeText.text = "${stats.currentVolume}%"
    }
    
    override fun onResume() {
        super.onResume()
        updateStats()
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // Permissions granted
                statusText.text = "Ready to Monitor"
            } else {
                statusText.text = "Permissions Required"
                statusText.setTextColor(getColor(android.R.color.holo_red_dark))
            }
        }
    }
}
