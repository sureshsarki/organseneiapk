package com.organsensei.earsensei

import kotlin.math.pow

class HearingDamageCalculator {
    
    companion object {
        // NIOSH safe exposure times at different dB levels
        private val SAFE_EXPOSURE_MAP = mapOf(
            85.0 to 8.0 * 3600,    // 8 hours in seconds
            88.0 to 4.0 * 3600,    // 4 hours
            91.0 to 2.0 * 3600,    // 2 hours
            94.0 to 1.0 * 3600,    // 1 hour
            97.0 to 0.5 * 3600,    // 30 minutes
            100.0 to 0.25 * 3600,  // 15 minutes
            103.0 to 0.125 * 3600, // 7.5 minutes
            106.0 to 0.0625 * 3600 // 3.75 minutes
        )
        
        private const val REFERENCE_DB = 85.0
        private const val REFERENCE_TIME = 28800.0 // 8 hours in seconds
        private const val EXCHANGE_RATE = 3.0 // NIOSH uses 3 dB exchange rate
    }
    
    /**
     * Calculate hearing damage contribution for a given exposure.
     * Uses NIOSH formula: For every 3 dB increase, safe time halves
     * 
     * @param dBLevel Sound pressure level in decibels
     * @param durationSeconds Exposure duration in seconds
     * @return Fractional dose (0.0 to 1.0, where 1.0 = 100% daily dose)
     */
    fun calculateDamageForInterval(dBLevel: Double, durationSeconds: Double): Double {
        if (dBLevel < 80.0) {
            // Below 80 dB is generally considered safe
            return 0.0
        }
        
        // Calculate safe time at this dB level
        val safeTimeSeconds = calculateSafeTime(dBLevel)
        
        // Calculate fractional dose
        val dose = durationSeconds / safeTimeSeconds
        
        return dose.coerceAtLeast(0.0)
    }
    
    /**
     * Calculate safe exposure time at a given dB level using NIOSH formula
     * Formula: T_safe = T_ref * 2^((L_ref - L) / ER)
     * where:
     *   T_ref = reference time (8 hours for 85 dB)
     *   L_ref = reference level (85 dB)
     *   L = current level
     *   ER = exchange rate (3 dB for NIOSH)
     */
    private fun calculateSafeTime(dBLevel: Double): Double {
        val exponent = (REFERENCE_DB - dBLevel) / EXCHANGE_RATE
        return REFERENCE_TIME * 2.0.pow(exponent)
    }
    
    /**
     * Estimate dB SPL from device volume percentage
     * This is a rough approximation - actual dB depends on headphone model
     * 
     * @param volumePercent Volume level from 0-100
     * @return Estimated dB SPL
     */
    fun estimateDbFromVolume(volumePercent: Int): Double {
        // Typical smartphone/headphone output ranges from 50 dB (low) to 100+ dB (max)
        // This is a linear approximation - real curve is usually logarithmic
        return 50.0 + (volumePercent * 0.5)
    }
    
    /**
     * Get risk level description based on daily dose
     */
    fun getRiskLevel(dailyDose: Double): RiskLevel {
        return when {
            dailyDose >= 1.0 -> RiskLevel.CRITICAL
            dailyDose >= 0.75 -> RiskLevel.HIGH
            dailyDose >= 0.50 -> RiskLevel.MODERATE
            dailyDose >= 0.25 -> RiskLevel.LOW
            else -> RiskLevel.SAFE
        }
    }
    
    /**
     * Calculate recommended rest time based on exposure
     */
    fun calculateRestTime(dailyDose: Double): Int {
        return when {
            dailyDose >= 1.0 -> 120 // 2 hours rest
            dailyDose >= 0.75 -> 60  // 1 hour rest
            dailyDose >= 0.50 -> 30  // 30 min rest
            else -> 0
        }
    }
}

enum class RiskLevel(val description: String, val color: Int) {
    SAFE("Safe", android.R.color.holo_green_dark),
    LOW("Low Risk", android.R.color.holo_green_light),
    MODERATE("Moderate Risk", android.R.color.holo_orange_light),
    HIGH("High Risk", android.R.color.holo_orange_dark),
    CRITICAL("Critical - Rest Needed", android.R.color.holo_red_dark)
}
