package com.sleepfuriously.biggsstopwatch3

import android.app.Application
import android.content.Context
import android.os.CountDownTimer
import android.os.SystemClock
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel


/**
 * All logic goes through here.
 *
 * Uses LiveData to communicate w MainActivity (it's an observable
 * that respects life cycles).  The LiveData lives within the
 * ViewModel class.
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {

    //-------------------------
    //  vars
    //-------------------------

    /**
     * This will trigger anything watching it (observed) when it changes.
     *
     * The declaration sets the state to its default state or the last state
     * if app was closed unexpected
     *
     * mutable state (compose state) which was designed specifically for jetpack compose
     */
    var stopwatchState by mutableIntStateOf(START_STATE)
        private set


    /**
     * The time (in millis since jan 1, 1970) the start button was last pushed.
     */
    private var stopwatchStart = 0L

    /**
     * Whenever the STOP button is pushed, the current time minus [stopwatchStart]
     * is added to elapsedTime.  It essentially keeps count of all the milliseconds
     * that have been timed and completed.  It is only reset when the CLEAR button
     * is pressed.
     */
    private var elapsedTime by mutableLongStateOf(0L)

    /**
     * The time the split button was pushed.
     */
    var stopwatchSplit by mutableLongStateOf(0L)
        private set

    /**
     * When TRUE, clicks are played on button taps.
     */
    var clickOn by mutableStateOf(true)
        private set

    /**
     * When TRUE, this app will keep the screen from turning off.
     * Default is FALSE.
     */
    var stayAwake by mutableStateOf(false)
        private set

    /**
     * When TRUE, the phone will vibrate when a button is pressed.
     */
    var vibrateOn by mutableStateOf(true)
        private set

    /**
     * Signals to the Activity that a tick has occurred.  It will actually
     * contain the number of milliseconds since [stopwatchStart].
     */
    var tick by mutableLongStateOf(0L)
        private set


    /**
     * This is what makes the stopwatch tick.
     */
    private val timer = object : CountDownTimer(Long.MAX_VALUE, 30) {
        override fun onTick(millisUntilFinished: Long) {
            if ((stopwatchState == RUNNING_STATE) ||
                (stopwatchState == SPLIT_RUNNING_STATE)) {
                // this should activate the observer in the main view
                tick = (SystemClock.elapsedRealtime()
                        - stopwatchStart
                        + elapsedTime)
            }
        }

        override fun onFinish() {
            // not used
        }
    }


    //-------------------------
    //  functions
    //-------------------------

    init {
        // load the values from shared prefs
        val prefs = getApplication<Application>().getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
        clickOn = prefs.getBoolean(SOUND_ON_PREF, true)
        stayAwake = prefs.getBoolean(DISABLE_SCREENSAVE_PREF, false)
        vibrateOn = prefs.getBoolean(VIBRATE_ON_PREF, true)

        Log.d(TAG, "init() -> clickOn = $clickOn, stayOn = $stayAwake, vibrateOn = $vibrateOn")
    }


    /**
     * Here is the table for state changes:
     *
     *  - START_STATE (0)
     *      - BUTTON_START_STOP (START) --> RUNNING_STATE (1)
     *      - BUTTON_SPLIT_CLEAR --> n/a
     *
     * - RUNNING_STATE (1)
     *      (timer running)
     *      - BUTTON_START_STOP (STOP) --> STOPPED_STATE (2)
     *      - BUTTON_SPLIT_CLEAR (SPLIT) --> SPLIT_RUNNING_STATE (3)
     *
     * - STOPPED_STATE (2)
     *      - BUTTON_START_STOP (START) --> RUNNING_STATE (1)
     *      - BUTTON_SPLIT_CLEAR (CLEAR) --> START_STATE (0)
     *
     * - SPLIT_RUNNING_STATE (3)
     *      - (timer running)
     *      - BUTTON_START_STOP (STOP) --> STOPPED_STATE (2)
     *      - BUTTON_SPLIT_CLEAR (SPLIT) --> SPLIT_RUNNING_STATE (3, no state change)
     *
     * - SPLIT_STOPPED_STATE (4)
     *      - BUTTON_START_STOP (START) --> SPLIT_RUNNING_STATE (3)
     *      - BUTTON_SPLIT_CLEAR (CLEAR) --> START_STATE (0)
     *
     * @param   buttonId    The id of the button that was hit (see {BUTTON_START_STOP}
     *                      or {BUTTON_SPLIT_CLEAR} above.
     *
     * @return  The new state
     */
    fun nextState(buttonId : Int) : Int {
        Log.d(TAG, "nextState($buttonId)")

        // check for invalid button id
        if ((buttonId != BUTTON_SPLIT_CLEAR) and (buttonId != BUTTON_START_STOP)) {
            Log.e(TAG, "unknown button in nextState(button = $buttonId)!")
            stopwatchState = ERROR_STATE
            return stopwatchState
        }


        // At the end of this function, the current state may change. Save the
        // old value for our debug statement.
        val prevState = stopwatchState

        // Depending on the current state and which button was hit, we move
        // to the next state.
        when (stopwatchState) {

            START_STATE -> {
                if (buttonId == BUTTON_START_STOP) {    // START -> RUNNING_STATE
                    stopwatchState = RUNNING_STATE
                    stopwatchStart = SystemClock.elapsedRealtime()
                    timer.start()
                    Log.d(TAG, "START_STATE -> START, timer starting, stopwatchState = ${stopwatchState}, stopwatchStart = $stopwatchStart")
                }
                else {  // BUTTON_SPLIT_CLEAR           N/A
                    Log.e(TAG, "Error: split/clear button active in START_STATE!!!")
                    stopwatchState = ERROR_STATE
                    timer.cancel()
                }
            }

            RUNNING_STATE -> {
                if (buttonId == BUTTON_START_STOP) {    // STOP -> STOPPED_STATE
                    stopwatchState = STOPPED_STATE
                    elapsedTime += SystemClock.elapsedRealtime() - stopwatchStart   // add latest run batch
                    timer.cancel()
                    Log.d(TAG, "RUNNING_STATE -> STOP, timer canceling, stopwatchState = ${stopwatchState}, stopwatchStart = $stopwatchStart")
                }
                else {  // BUTTON_SPLIT_CLEAR           SPLIT -> SPLIT_RUNNING_STATE
                    stopwatchState = SPLIT_RUNNING_STATE
                    stopwatchSplit = elapsedTime + (SystemClock.elapsedRealtime() - stopwatchStart)
                    Log.d(TAG, "RUNNING_STATE -> SPLIT, stopwatchState = ${stopwatchState}, stopwatchStart = $stopwatchStart")
                }
            }

            STOPPED_STATE -> {
                if (buttonId == BUTTON_START_STOP) {    // START -> RUNNING_STATE
                    stopwatchState = RUNNING_STATE
                    stopwatchStart = SystemClock.elapsedRealtime()
                    timer.start()
                    Log.d(TAG, "STOPPED_STATE -> START, timer starting, stopwatchState = ${stopwatchState}, stopwatchStart = $stopwatchStart")
                }
                else {  // BUTTON_SPLIT_CLEAR           CLEAR -> START_STATE
                    stopwatchState = START_STATE
                    stopwatchSplit = 0L
                    elapsedTime = 0L
                    tick = 0L
                    Log.d(TAG, "STOPPED_STATE -> CLEAR, stopwatchState = ${stopwatchState}, stopwatchStart = $stopwatchStart")
                }
            }

            SPLIT_RUNNING_STATE -> {
                if (buttonId == BUTTON_START_STOP) {    // STOP -> SPLIT_STOPPED_STATE
                    stopwatchState = SPLIT_STOPPED_STATE
                    elapsedTime += SystemClock.elapsedRealtime() - stopwatchStart
                    timer.cancel()
                    Log.d(TAG, "SPLIT_RUNNING_STATE -> START, timer canceling, stopwatchState = ${stopwatchState}, stopwatchStart = $stopwatchStart")
                }
                else {  // BUTTON_SPLIT_CLEAR           // SPLIT -> SPLIT_RUNNING_STATE (no change)
                    Log.d(TAG, "hitting split button again")
                    stopwatchSplit = elapsedTime + (SystemClock.elapsedRealtime() - stopwatchStart)
                    Log.d(TAG, "SPLIT_RUNNING_STATE -> SPLIT, stopwatchState = ${stopwatchState}, stopwatchStart = $stopwatchStart")
                }
            }

            SPLIT_STOPPED_STATE -> {
                if (buttonId == BUTTON_START_STOP) {    // START -> SPLIT_RUNNING_STATE
                    stopwatchState = SPLIT_RUNNING_STATE
                    stopwatchStart = SystemClock.elapsedRealtime()
                    timer.start()
                    Log.d(TAG, "SPLIT_STOPPED_STATE -> START, timer starting, stopwatchState = ${stopwatchState}, stopwatchStart = $stopwatchStart")
                }
                else {  // BUTTON_SPLIT_CLEAR           CLEAR -> START_STATE
                    stopwatchState = START_STATE
                    elapsedTime = 0L
                    tick = 0L
                    stopwatchSplit = 0L
                    Log.d(TAG, "SPLIT_STOPPED_STATE -> CLEAR, stopwatchState = ${stopwatchState}, stopwatchStart = $stopwatchStart")
                }
            }

            else -> {
                Log.e(TAG, "Unknown state of $stopwatchState in nextState()!")
                stopwatchState = ERROR_STATE
                timer.cancel()
            }
        }

        Log.d(TAG, "nextState() moved from ${STATE_NAMES[prevState]} to ${STATE_NAMES[stopwatchState]}")
        Log.d(TAG, "   start time = ${stopwatchStart}, elapsed time = ${elapsedTime}, split time = $stopwatchSplit")
        return stopwatchState
    }

    /**
     * If the sound is disabled, enable it.  And vice-versa.
     */
    fun toggleSound() {
        clickOn = !clickOn
        save()
    }

    fun toggleStayOn() {
        stayAwake = !stayAwake
        save()
    }

    fun toggleVibrateOn() {
        vibrateOn = !vibrateOn
        save()
    }


    /**
     * Saves the current state to SharedPrefs.  It's probably a good idea
     * to call this any time the properties change (just in case the app
     * is killed).
     *
     * NOTE:  The save is asynchronous, so it shouldn't cause any performance
     * problems.
     *
     * side effects
     *      clickOn         will be written to pref file
     *      vibrateOn       "                          "
     *      stayOn          "                          "
     */
    private fun save() {
        val prefs = getApplication<Application>().getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)

        with (prefs.edit()) {
            putBoolean(SOUND_ON_PREF, clickOn)
            putBoolean(VIBRATE_ON_PREF, vibrateOn)
            putBoolean(DISABLE_SCREENSAVE_PREF, stayAwake)
            apply()
        }

        Log.d(TAG, "save() -> clickOn = $clickOn, stayOn = $stayAwake, vibrateOn = $vibrateOn")
    }

//-------------------------
//  constants
//-------------------------

    companion object {
        private const val TAG = "MainViewModel"

        /**
         * button ids.  this class will refer to the buttons throught
         * these ids instead of a Button class or some other View.
         */
        const val BUTTON_START_STOP = 0       // the start/stop button
        const val BUTTON_SPLIT_CLEAR = 1      // the button that shows split or clear

        /**
         * The states for the stopwatch
         */
        const val START_STATE = 0           // display is 0, nothing is running
        const val RUNNING_STATE = 1         // running, the display is counting
        const val STOPPED_STATE = 2         // display shows a time, but no counter is running. split is not shown.
        const val SPLIT_RUNNING_STATE = 3   // split, display shows split tim, but still counting
        const val SPLIT_STOPPED_STATE = 4   // display shows split time, but counter is stopped at another time
        const val ERROR_STATE = -1          // an error occurred

        private val STATE_NAMES = arrayOf(
            "START",
            "RUNNING",
            "STOPPED",
            "SPLIT_RUNNING",
            "SPLIT_STOPPED"
            )

        /** name of the shared preferences file */
        private const val PREF_FILE = "biggs.stopwatch_prefs"

        // keys for the prefs
        private const val SOUND_ON_PREF = "sound_on"
        private const val DISABLE_SCREENSAVE_PREF = "disable_screen_saver"
        private const val VIBRATE_ON_PREF = "vibrate_on_pref"
    }

}



