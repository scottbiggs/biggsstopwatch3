package com.sleepfuriously.biggsstopwatch3

import android.media.SoundPool
import android.os.Bundle
import android.os.Vibrator
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.ViewModelProvider
import com.sleepfuriously.biggsstopwatch3.ui.MainScreen


/**
 * This is my stopwatch app for the year 2024.  It uses jetpack compose,
 * a viewmodel, and stateflow--an exercise.
 */
class MainActivity : ComponentActivity() {

    //-------------------
    //  properties
    //-------------------

    /** access to the view model */
    private lateinit var mainViewModel: MainViewModel

    /** another way of playing sounds */
    private var soundPool : SoundPool? = null

    /** when TRUE, this device is able to vibrate */
    private var hasVibe = true


    //-------------------
    //  class functions
    //-------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // get viewmodel instance
        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]

        // set the sound system
        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .build()
        soundPool!!.load(baseContext, R.raw.button_click, 1)

        // does this device have a vibrator?
        val vibrator = getSystemService(this, Vibrator::class.java)
        hasVibe = vibrator?.hasVibrator() ?: false

        setContent {
            MainScreen(mainViewModel = mainViewModel, soundPool = soundPool!!)
        }
    }


    override fun onResume() {
        super.onResume()
        if (mainViewModel.stayAwake) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

    }
}


//-------------------------
//  constants
//-------------------------

private const val TAG = "MainActivity"
