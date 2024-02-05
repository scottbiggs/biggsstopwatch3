package com.sleepfuriously.biggsstopwatch3

import android.content.res.Configuration
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.sleepfuriously.biggsstopwatch3.MainViewModel.Companion.BUTTON_SPLIT_CLEAR
import com.sleepfuriously.biggsstopwatch3.MainViewModel.Companion.BUTTON_START_STOP
import com.sleepfuriously.biggsstopwatch3.MainViewModel.Companion.RUNNING_STATE
import com.sleepfuriously.biggsstopwatch3.MainViewModel.Companion.SPLIT_RUNNING_STATE
import com.sleepfuriously.biggsstopwatch3.MainViewModel.Companion.SPLIT_STOPPED_STATE
import com.sleepfuriously.biggsstopwatch3.MainViewModel.Companion.START_STATE
import com.sleepfuriously.biggsstopwatch3.MainViewModel.Companion.STATE_NAMES
import com.sleepfuriously.biggsstopwatch3.MainViewModel.Companion.STOPPED_STATE
import com.sleepfuriously.biggsstopwatch3.ui.theme.BiggsStopwatch3Theme


/**
 * This is my stopwatch app for the year 2024.  It uses jetpack compose,
 * a viewmodel, and stateflow, three things that I am reluctantly learning
 * to use.  Sigh.
 */


//-------------------------
//  globals
//-------------------------

// Should be a constant, but need to get it from strings.xml
lateinit var testString : String

var current_fontsize = 0.sp


//-------------------------
//  classes
//-------------------------

class MainActivity : ComponentActivity() {

    //-------------------
    //  properties
    //-------------------

    /** access to the view model */
    private lateinit var mainViewModel: MainViewModel


    //-------------------
    //  class functions
    //-------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // get viewmodel instance
        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)


        testString = getString(R.string.test_string)

        setContent {
            MainDisplay(mainViewModel = mainViewModel)
        }
    }
}


/**
 * Given the current time in millis, convert to a String formatted:
 *
 *      HH:MM:SS.hh
 *
 * where H = hours, M = minutes, S = seconds, h = hundredths
 *
 * @param       millis      A bunch of milliseconds.  Will be converted
 *                          to an easier to read time interval.
 */
fun getDisplayTime(millis : Long) : String {

    val hours = millis / 3600000L
    var remainder = millis % 3600000L

    val minutes = remainder / 60000
    remainder %= 60000

    val seconds = remainder / 1000L
    remainder %= 1000L

    val hundredths = remainder / 10L

    // figure out the pieces
    var hoursStr = ""
    var minStr = ""
    var secStr = ""
    var hunStr = ""

    if (hours >= 10)
        hoursStr = "$hours"
    else
        hoursStr = "0$hours"

    if (minutes >= 10)
        minStr = "$minutes"
    else
        minStr = "0$minutes"

    if (seconds >= 10)
        secStr = "$seconds"
    else
        secStr = "0$seconds"

    if (hundredths >= 10)
        hunStr = "$hundredths"
    else
        hunStr = "0$hundredths"

    // If there are more than 100 hours, then we won't bother
    // displaying hundredths of seconds.
    if (hours >= 100)
        return "$hoursStr:$minStr:$secStr"
    else
        return "$hoursStr:$minStr:$secStr.$hunStr"
}


//-------------------
//  composables
//-------------------

/**
 * The top-level display for the app.
 */
@Composable
fun MainDisplay(mainViewModel : MainViewModel) {

    BiggsStopwatch3Theme {

//        val stopwatchState by mainViewModel.stopwatchState.collectAsState()     // stateflow version
        val stopwatchState = mainViewModel.stopwatchState           // any composable that uses this value
                                                                    // will automatically recompose

        val stopwatchStart = mainViewModel.stopwatchStart           // also composable version

        val stopwatchSplit = mainViewModel.stopwatchSplit           // also composable

        val tick = mainViewModel.tick


        // box background for entire drawing.  Also the buttons will be at the
        // bottom of this box.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.LightGray)
                .padding(24.dp),
            Alignment.BottomStart,
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
//                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Start Button
                Column {

                }
                ElevatedButton(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        mainViewModel.nextState(BUTTON_START_STOP)
                        Log.d(TAG, "start button click")
                }) {
                    val startStopButtonTxt =
                        when (stopwatchState.value) {
                            START_STATE -> BUTTON_START_TEXT
                            RUNNING_STATE -> BUTTON_STOP_TEXT
                            STOPPED_STATE -> BUTTON_START_TEXT
                            SPLIT_RUNNING_STATE -> BUTTON_STOP_TEXT
                            SPLIT_STOPPED_STATE -> BUTTON_START_TEXT
                            else -> "error"
                        }
                    Text("$startStopButtonTxt")
                }

                Spacer(Modifier.width(16.dp))

                ElevatedButton(
                    modifier = Modifier.weight(1f),
                    enabled = stopwatchState.value != START_STATE,
                    onClick = {
                        mainViewModel.nextState(BUTTON_SPLIT_CLEAR)
                        Log.d(TAG, "split button click")
                }) {
                    val splitClearButtonTxt =
                        when (stopwatchState.value) {
                            START_STATE -> ""
                            RUNNING_STATE -> BUTTON_SPLIT_TEXT
                            STOPPED_STATE -> BUTTON_CLEAR_TEXT
                            SPLIT_RUNNING_STATE -> BUTTON_SPLIT_TEXT
                            SPLIT_STOPPED_STATE -> BUTTON_CLEAR_TEXT
                            else -> "error"
                        }
                    Text(splitClearButtonTxt)
                }
            }
        }

        // holds vertical components: main timer and split timers
        Column {

            // Main timer
            AutoSizeText(
                text = getDisplayTime(tick.value),
                textStyle = TextStyle(
                    fontSize = 240.sp,
                    color = MaterialTheme.colorScheme.primary,
                    shadow = Shadow(
                        color = Color.Black,
                        offset = Offset(5.0f, 10.0f),
                        blurRadius = 3f
                    )
                )
            )

            Spacer(Modifier.height(4.dp))

            Row(
                modifier = Modifier
                    .align(Alignment.End)
                    .fillMaxWidth(0.75f)
            ) {
                Text(
                    text = "split",
                    modifier = Modifier.align(
                        Alignment.Bottom
                    ))

                Spacer(Modifier.width(6.dp))

                val splitTime = (stopwatchSplit.value ?: 0) - (stopwatchStart.value ?: 0)
                val splitString = getDisplayTime(splitTime)
                if ((stopwatchState.value == SPLIT_RUNNING_STATE) ||
                    (stopwatchState.value == SPLIT_STOPPED_STATE)) {
                    AutoSizeText(
                        text = splitString,
                        textStyle = TextStyle(fontSize = 200.sp),   // max font size
                        Modifier.background(Color.Yellow)
                    )
                }
            }

            // NOTE: this updates AUTOMATICALLY because of how stopwatchState is
            // defined.  Any composable that uses it will automatically update.
            // Cool--or is it magic?
            Text(
                "state = ${STATE_NAMES[stopwatchState.value]}",
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier
                    .background(Color.Cyan)
                    .clickable {
                        Log.d(TAG, "click")
                        mainViewModel.nextState(2)  // fixme: change param
                    }
            )

            Text(text = "start time = ${stopwatchStart.value}")
            Text("split time = ${stopwatchSplit.value}")

        }

    }

}


/**
 * Based on:
 *       https://stackoverflow.com/a/68258975/624814
 *
 * @param
 *      zoomFactor      This is a multiplication factor to the text
 *                      size (I added).  It allows you to make the
 *                      text bigger or smaller than maximum.
 *                      For example: 0.5 will make it half the width.
 */
@Composable
fun AutoSizeText(
    text: String,
    textStyle: TextStyle,
    modifier: Modifier = Modifier
) {
    var scaledTextStyle by remember { mutableStateOf(textStyle) }
    var readyToDraw by remember { mutableStateOf(false) }


    Text(
        text,
        modifier.drawWithContent {
            if (readyToDraw) {
                drawContent()
            }
        },
        style = scaledTextStyle,
        softWrap = false,
        onTextLayout = { textLayoutResult ->
            if (textLayoutResult.didOverflowWidth) {
                scaledTextStyle =
                    scaledTextStyle.copy(fontSize = scaledTextStyle.fontSize * 0.9)
            } else {
                current_fontsize = scaledTextStyle.fontSize
                Log.d(TAG, "font size = ${current_fontsize}")
                readyToDraw = true
            }
        }
    )

}

//-------------------
//  previews
//-------------------

@Preview(
    name = "dark mode",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Preview(
    name = "light mode",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Composable
fun PreviewStopwatch() {
//    MainDisplay()
}


//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//  classes
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

//-------------
//  While I'm actually counting UP, this is useful for getting
//  lots of looping to update the UI.
//
//  input
//      millisInFuture      To use this forever, enter the MAX LONG for millisInFuture.
//                          While not exactly forever, it's good enough for this app!
//
//      countDownInterval   The frequency the onTick() function fires (approximately).
//
class StopwatchTimer(
    val millisInFuture : Long,
    val countDownInterval : Long
) : CountDownTimer(millisInFuture, countDownInterval) {

    override fun onTick(millisUntilFinished: Long) {
        // todo: increment timer
    }

    override fun onFinish() {
        // noop
    }
}

//-------------------------
//  constants
//-------------------------

private const val TAG = "MainActivity"

private const val BUTTON_START_TEXT = "START"
private const val BUTTON_STOP_TEXT = "STOP"

private const val BUTTON_SPLIT_TEXT = "SPLIT"
private const val BUTTON_CLEAR_TEXT = "CLEAR"