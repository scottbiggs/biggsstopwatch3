package com.sleepfuriously.biggsstopwatch3

import android.os.CountDownTimer
import android.os.SystemClock
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel


/**
 * All logic goes through here.
 *
 * Uses LiveData to communicate w MainActivity (it's an observable
 * that respects life cycles).  The LiveData lives within the
 * ViewModel class.
 */
class MainViewModel : ViewModel() {

    //-------------------------
    //  vars
    //-------------------------

    /**
     * This will trigger anything watching it (observed) when it changes.
     *
     * The declaration sets the state to its default state or the last state
     * if app was closed unexpected
     */
//    val stopwatchState: StateFlow<Int> = savedStateHandle.getStateFlow(STATE_HANDLE_KEY, START_STATE)   // stateflow version

    // mutable state (compose state) which was designed specifically for jetpack compose
    val stopwatchState: MutableState<Int> = mutableIntStateOf(START_STATE)

    // another way of doing above
//    var stopwatchState2 by mutableStateOf(START_STATE)
//        private set


    /**
     * The time (in millis since jan 1, 1970) the start button was last pushed.
     */
//    val stopwatchStart: MutableLiveData<Long> by lazy {
//        MutableLiveData<Long>()
//    }
    val stopwatchStart: MutableState<Long> = mutableLongStateOf(0L)

    /**
     * Whenever the STOP button is pushed, the current time minus [stopwatchStart]
     * is added to elapsedTime.  It essentially keeps count of all the milliseconds
     * that have been timed and completed.  It is only reset when the CLEAR button
     * is pressed.
     */
    private val elapsedTime: MutableState<Long> = mutableLongStateOf(0L)

    /**
     * The time the split button was pushed.
     */
//    val stopwatchSplit: MutableLiveData<Long> by lazy {
//        MutableLiveData<Long>()
//    }
    val stopwatchSplit: MutableState<Long> = mutableLongStateOf(0L)

    /**
     * When TRUE, clicks are played on button taps.
     */
    val clickOn: MutableState<Boolean> = mutableStateOf(true)

    /**
     * Signals to the Activity that a tick has occurred.  It will actually
     * contain the number of milliseconds since [stopwatchStart].
     */
    var tick: MutableState<Long> = mutableLongStateOf(0L)

    /**
     * This is what makes the stopwatch tick.
     */
    private val timer = object : CountDownTimer(Long.MAX_VALUE, 30) {
        override fun onTick(millisUntilFinished: Long) {
            if ((stopwatchState.value == RUNNING_STATE) ||
                (stopwatchState.value == SPLIT_RUNNING_STATE)) {
                // this should activate the observer in the main view
                tick.value = (SystemClock.elapsedRealtime()
                        - stopwatchStart.value
                        + elapsedTime.value)
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
        // todo Load up prefs
        //
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
            stopwatchState.value = ERROR_STATE
            return stopwatchState.value
        }


        // At the end of this function, the current state may change. Save the
        // old value for our debug statement.
        val prevState = stopwatchState.value

        // Depending on the current state and which button was hit, we move
        // to the next state.
        when (stopwatchState.value) {

            START_STATE -> {
                if (buttonId == BUTTON_START_STOP) {    // START -> RUNNING_STATE
                    stopwatchState.value = RUNNING_STATE
                    stopwatchStart.value = SystemClock.elapsedRealtime()
                    timer.start()
                    Log.d(TAG, "START_STATE -> START, timer starting, stopwatchState = ${stopwatchState.value}, stopwatchStart = ${stopwatchStart.value}")
                }
                else {  // BUTTON_SPLIT_CLEAR           N/A
                    Log.e(TAG, "Error: split/clear button active in START_STATE!!!")
                    stopwatchState.value = ERROR_STATE
                    timer.cancel()
                }
            }

            RUNNING_STATE -> {
                if (buttonId == BUTTON_START_STOP) {    // STOP -> STOPPED_STATE
                    stopwatchState.value = STOPPED_STATE
                    elapsedTime.value += SystemClock.elapsedRealtime() - stopwatchStart.value   // add latest run batch
                    timer.cancel()
                    Log.d(TAG, "RUNNING_STATE -> STOP, timer canceling, stopwatchState = ${stopwatchState.value}, stopwatchStart = ${stopwatchStart.value}")
                }
                else {  // BUTTON_SPLIT_CLEAR           SPLIT -> SPLIT_RUNNING_STATE
                    stopwatchState.value = SPLIT_RUNNING_STATE
                    stopwatchSplit.value = elapsedTime.value + (SystemClock.elapsedRealtime() - stopwatchStart.value)
                    Log.d(TAG, "RUNNING_STATE -> SPLIT, stopwatchState = ${stopwatchState.value}, stopwatchStart = ${stopwatchStart.value}")
                }
            }

            STOPPED_STATE -> {
                if (buttonId == BUTTON_START_STOP) {    // START -> RUNNING_STATE
                    stopwatchState.value = RUNNING_STATE
                    stopwatchStart.value = SystemClock.elapsedRealtime()
                    timer.start()
                    Log.d(TAG, "STOPPED_STATE -> START, timer starting, stopwatchState = ${stopwatchState.value}, stopwatchStart = ${stopwatchStart.value}")
                }
                else {  // BUTTON_SPLIT_CLEAR           CLEAR -> START_STATE
                    stopwatchState.value = START_STATE
                    stopwatchSplit.value = 0L
                    elapsedTime.value = 0L
                    tick.value = 0L
                    Log.d(TAG, "STOPPED_STATE -> CLEAR, stopwatchState = ${stopwatchState.value}, stopwatchStart = ${stopwatchStart.value}")
                }
            }

            SPLIT_RUNNING_STATE -> {
                if (buttonId == BUTTON_START_STOP) {    // STOP -> SPLIT_STOPPED_STATE
                    stopwatchState.value = SPLIT_STOPPED_STATE
                    elapsedTime.value += SystemClock.elapsedRealtime() - stopwatchStart.value
                    timer.cancel()
                    Log.d(TAG, "SPLIT_RUNNING_STATE -> START, timer canceling, stopwatchState = ${stopwatchState.value}, stopwatchStart = ${stopwatchStart.value}")
                }
                else {  // BUTTON_SPLIT_CLEAR           // SPLIT -> SPLIT_RUNNING_STATE (no change)
                    Log.d(TAG, "hitting split button again")
                    stopwatchSplit.value = elapsedTime.value + (SystemClock.elapsedRealtime() - stopwatchStart.value)
                    Log.d(TAG, "SPLIT_RUNNING_STATE -> SPLIT, stopwatchState = ${stopwatchState.value}, stopwatchStart = ${stopwatchStart.value}")
                }
            }

            SPLIT_STOPPED_STATE -> {
                if (buttonId == BUTTON_START_STOP) {    // START -> SPLIT_RUNNING_STATE
                    stopwatchState.value = SPLIT_RUNNING_STATE
                    stopwatchStart.value = SystemClock.elapsedRealtime()
                    timer.start()
                    Log.d(TAG, "SPLIT_STOPPED_STATE -> START, timer starting, stopwatchState = ${stopwatchState.value}, stopwatchStart = ${stopwatchStart.value}")
                }
                else {  // BUTTON_SPLIT_CLEAR           CLEAR -> START_STATE
                    stopwatchState.value = START_STATE
                    elapsedTime.value = 0L
                    tick.value = 0L
                    stopwatchSplit.value = 0L
                    Log.d(TAG, "SPLIT_STOPPED_STATE -> CLEAR, stopwatchState = ${stopwatchState.value}, stopwatchStart = ${stopwatchStart.value}")
                }
            }

            else -> {
                Log.e(TAG, "Unknown state of ${stopwatchState.value} in nextState()!")
                stopwatchState.value = ERROR_STATE
                timer.cancel()
            }
        }

        Log.d(TAG, "nextState() moved from ${STATE_NAMES[prevState]} to ${STATE_NAMES[stopwatchState.value]}")
        Log.d(TAG, "   start time = ${stopwatchStart.value}, elapsed time = ${elapsedTime.value}, split time = ${stopwatchSplit.value}")
        return stopwatchState.value
    }

//-------------------------
//  constants
//-------------------------

    companion object {

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

        val STATE_NAMES = arrayOf(
            "START",
            "RUNNING",
            "STOPPED",
            "SPLIT_RUNNING",
            "SPLIT_STOPPED"
            )

    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //  inner classes
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

}


private const val TAG = "MainViewModel"

