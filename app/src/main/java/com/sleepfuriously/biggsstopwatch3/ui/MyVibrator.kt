package com.sleepfuriously.biggsstopwatch3.ui

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.content.ContextCompat.getSystemService

/**
 * Does vibrations.  Takes care of initializations etc.
 */
object MyVibrator {

    /**
     * When not null, this tells us if we have a vibrator on this device.
     * Initialized on first use.
     */
    private var hasVibe : Boolean? = null

    /** Holder for Vibrator class instance. Null until used. */
    private var vibrator : Vibrator? = null


    /**
     * Returns TRUE iff this device does vibration.
     */
    fun hasVibrator(ctx: Context) : Boolean {
        if (hasVibe == null) {
            if (vibrator == null) {
                vibrator = getSystemService(ctx, Vibrator::class.java)
            }
            hasVibe = vibrator?.hasVibrator() ?: false
        }
        return hasVibe!!
    }


    /**
     * Guess what this does?  And can you guess what happens if vibration is
     * turned off?  Yep, I thought you could!
     *
     * @param   ctx         Context for playing this sound
     *
     * @param   millis      Number of milliseconds to vibrate
     *
     */
    fun vibrate(ctx: Context, millis: Long) {

        if (vibrator == null) {
           vibrator = getSystemService(ctx, Vibrator::class.java)
        }

        // The ?.let allows us to continue only if vibrator is not null.
        // It can be null on devices that don't vibrate.
        vibrator?.let {
            if (Build.VERSION.SDK_INT >= 26) {
                it.vibrate(VibrationEffect.createOneShot(millis, VibrationEffect.DEFAULT_AMPLITUDE))
            }
            else {
                @Suppress("DEPRECATION")
                it.vibrate(millis)
            }
        }
    }

}