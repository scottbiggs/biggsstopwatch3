package com.sleepfuriously.biggsstopwatch3

import android.os.CountDownTimer
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import kotlin.random.Random




/**
 * All logic goes through here.
 *
 * Uses LiveData to communicate w MainActivity (it's an observable
 * that respects life cycles).  The LiveData lives within the
 * ViewModel class.
 */
class MainViewModel(
    private val savedStateHandle: SavedStateHandle      // retains previous state if force closed
) : ViewModel() {


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
    val stopwatchState: MutableState<Int> = mutableStateOf(START_STATE)

    // another way of doing above
//    var stopwatchState2 by mutableStateOf(START_STATE)
//        private set


    /**
     * The time (in millis since jan 1, 1970) the start button was first pushed.
     */
//    val stopwatchStart: MutableLiveData<Long> by lazy {
//        MutableLiveData<Long>()
//    }
    val stopwatchStart: MutableState<Long> = mutableStateOf(0L)


    /**
     * The time the split button was pushed.
     */
//    val stopwatchSplit: MutableLiveData<Long> by lazy {
//        MutableLiveData<Long>()
//    }
    val stopwatchSplit: MutableState<Long> = mutableStateOf(0L)

    /**
     * True only when the split button should be active.
     */
//    val splitButtonActive: MutableLiveData<Boolean> by lazy {
//        MutableLiveData<Boolean>()
//    }
    val splitButtonActive: MutableState<Boolean> = mutableStateOf(false)

    /**
     * Signals to the Activity that a tick has occurred.  It will actually
     * contain the number of milliseconds since [stopwatchStart].
     */
    var tick: MutableState<Long> = mutableStateOf(0L)

    /**
     * This is what makes the stopwatch tick.
     */
    val timer = object : CountDownTimer(Long.MAX_VALUE, 30) {
        override fun onTick(millisUntilFinished: Long) {
            if ((stopwatchState.value == RUNNING_STATE) ||
                (stopwatchState.value == SPLIT_RUNNING_STATE)) {
                // this should activate the observer in the main view
                tick.value = System.currentTimeMillis() - stopwatchStart.value
            }
        }

        override fun onFinish() {
            // not used
        }
    }


    //-------------------------
    //  functions
    //-------------------------

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
                if (buttonId == BUTTON_START_STOP) {
                    stopwatchState.value = RUNNING_STATE
                    stopwatchStart.value = System.currentTimeMillis()
                    timer.start()
                }
                else {  // BUTTON_SPLIT_CLEAR
                    Log.e(TAG, "Error: split/clear button active in START_STATE!!!")
                    stopwatchState.value = ERROR_STATE
                    timer.cancel()
                }
            }

            RUNNING_STATE -> {
                if (buttonId == BUTTON_START_STOP) {
                    stopwatchState.value = STOPPED_STATE
                    timer.cancel()
                }
                else {  // BUTTON_SPLIT_CLEAR
                    stopwatchState.value = SPLIT_RUNNING_STATE
                    stopwatchSplit.value = System.currentTimeMillis()
                }
            }

            STOPPED_STATE -> {
                if (buttonId == BUTTON_START_STOP) {
                    stopwatchState.value = RUNNING_STATE
                    stopwatchStart.value = System.currentTimeMillis()
                    timer.start()
                }
                else {  // BUTTON_SPLIT_CLEAR
                    stopwatchState.value = START_STATE
                }
            }

            SPLIT_RUNNING_STATE -> {
                if (buttonId == BUTTON_START_STOP) {
                    stopwatchState.value = SPLIT_STOPPED_STATE
                    timer.cancel()
                }
                else {  // BUTTON_SPLIT_CLEAR
                    Log.d(TAG, "hitting split button again")
                    stopwatchSplit.value = System.currentTimeMillis()
                }
            }

            SPLIT_STOPPED_STATE -> {
                if (buttonId == BUTTON_START_STOP) {
                    stopwatchState.value = SPLIT_RUNNING_STATE
                    timer.start()
                }
                else {  // BUTTON_SPLIT_CLEAR
                    stopwatchState.value = START_STATE
                }
            }

            else -> {
                Log.e(TAG, "Unknown state of ${stopwatchState.value} in nextState()!")
                stopwatchState.value = ERROR_STATE
                timer.cancel()
            }
        }

        Log.d(TAG, "nextState() moved from $prevState to ${stopwatchState.value}")
        Log.d(TAG, "   start time = ${stopwatchStart.value}, split time = ${stopwatchSplit.value}")
        return stopwatchState.value
    }

//-------------------------
//  constants
//-------------------------

    companion object {

        /** key for accessing the state from the savedStateHandle */
        const val STATE_HANDLE_KEY = "state"

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

    /**
     * Extending [CountDownTimer] allows me to neatly make my
     * own timer.
     *
     * @param   millisInFuture      How many milliseconds should this
     *                              timer run?  Use Long.MAX_VALUE to
     *                              run as long as possible.
     *
     * @param   countDownInterval   Number of milliseconds between
     *                              calls to onTick().
     *
     */
    class MyCountdown(
        val millisInFuture : Long,
        val countDownInterval : Long,
        var ticker : Long
    ) : CountDownTimer(millisInFuture, countDownInterval) {


        /**
         * update the display on each tick
         */
        override fun onTick(millisUntilFinished: Long) {
            ticker += countDownInterval
        }

        override fun onFinish() {
            // not used
        }
    }

}


private const val TAG = "MainViewModel"

