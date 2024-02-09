package com.sleepfuriously.biggsstopwatch3

import android.content.res.Configuration
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.Typeface
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProvider
import com.sleepfuriously.biggsstopwatch3.MainViewModel.Companion.BUTTON_SPLIT_CLEAR
import com.sleepfuriously.biggsstopwatch3.MainViewModel.Companion.BUTTON_START_STOP
import com.sleepfuriously.biggsstopwatch3.MainViewModel.Companion.RUNNING_STATE
import com.sleepfuriously.biggsstopwatch3.MainViewModel.Companion.SPLIT_RUNNING_STATE
import com.sleepfuriously.biggsstopwatch3.MainViewModel.Companion.SPLIT_STOPPED_STATE
import com.sleepfuriously.biggsstopwatch3.MainViewModel.Companion.START_STATE
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

/** access to the view model */
private lateinit var mainViewModel: MainViewModel

/** another way of playing sounds */
private var soundPool : SoundPool? = null


//-------------------------
//  classes
//-------------------------

class MainActivity : ComponentActivity() {

    //-------------------
    //  properties
    //-------------------

    //-------------------
    //  class functions
    //-------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // get viewmodel instance
        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]


        testString = getString(R.string.test_string)

        setContent {
            MainDisplay(mainViewModel = mainViewModel)
        }

        soundPool = SoundPool(5, AudioManager.STREAM_MUSIC, 0)
        soundPool!!.load(baseContext, R.raw.button_click, 1)
    }
}


/**
 * Plays a click (assuming that the preferences allow it)
 */
fun click() {
    if (mainViewModel.clickOn.value) {
        soundPool?.play(CLICK_SOUND_ID, 1f, 1f, 0, 0, 1f)
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

    val hoursStr: String =
        if (hours >= 10) "$hours"
        else "0$hours"

    val minStr: String =
        if (minutes >= 10) "$minutes"
        else "0$minutes"

    val secStr: String =
        if (seconds >= 10) "$seconds"
        else "0$seconds"

    val hunStr: String =
        if (hundredths >= 10) "$hundredths"
        else "0$hundredths"

    // If there are more than 100 hours, then we won't bother
    // displaying hundredths of seconds.
    return when {
        (hours >= 100) -> "$hoursStr:$minStr:$secStr"
        else -> "$hoursStr:$minStr:$secStr.$hunStr"
    }
}


//-------------------
//  composables
//-------------------

/**
 * The top-level display for the app.
 */
@Composable
fun MainDisplay(mainViewModel : MainViewModel) {

    val stopwatchState = mainViewModel.stopwatchState
    val stopwatchStart = mainViewModel.stopwatchStart
    val stopwatchSplit = mainViewModel.stopwatchSplit
    val tick = mainViewModel.tick

    val portraitMode = LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT

    val ctx = LocalContext.current
    val stopwatchFontFamily = remember {
        FontFamily(
            typeface = ResourcesCompat.getFont(ctx,
                R.font.alarm_clock)!!
        )
    }

        BiggsStopwatch3Theme {

        // box background for entire drawing.  Also the buttons will be at the
        // bottom of this box.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
            Alignment.BottomStart,
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                // Start Button
                Column {

                }

                // START / STOP
                ElevatedButton(
                    modifier = Modifier
                        .weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    onClick = {
                        click()
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
                    Text(startStopButtonTxt)
                }

                Spacer(Modifier.width(16.dp))

                // SPLIT / CLEAR
                ElevatedButton(
                    modifier = Modifier
                        .weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onTertiaryContainer
                    ),
                    enabled = stopwatchState.value != START_STATE,
                    onClick = {
                        click()
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


        // for timer and split timer, I'm using a constraintlayout.  Makes some things
        // a lot easier.

        ConstraintLayout(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp)
        ) {



            // line for the top of the main display
            val mainDisplayGuide = createGuidelineFromTop(
                if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    0.33f   // about a third down for portrait mode
                }
                else {
                    0f      // keep this at top for landscape mode
                }
            )

            // names for the constrained widgets
            val (
                mainTimer, splitTimer, settingsButton
            ) = createRefs()

            // settings menu
            IconButton(
                onClick = {
                    // todo: action on the button click
                    Log.d(TAG, "icon button clicked")
                },
                modifier = Modifier
                    .constrainAs(settingsButton) {
                        top.linkTo(parent.top)
                        end.linkTo(parent.end, 8.dp)
                    }
            ) {
                Icon(
                    painter = painterResource(R.drawable.menu_dark_mode),
                    tint = if (isSystemInDarkTheme()) Color.White else Color.Black,
                    contentDescription = null,
                )
            }


            // main display
            AutoSizeText(
                text = getDisplayTime(tick.value),
                textStyle = TextStyle(
                    fontFamily = stopwatchFontFamily,
                    fontSize = 240.sp,
                    color = if (isSystemInDarkTheme()) Color.White else Color.Black,
                    shadow = Shadow(
                        offset = Offset(5.0f, 7.0f),
                        color = MaterialTheme.colorScheme.primary,
                        blurRadius = 3f
                    )
                ),
                modifier = Modifier
                    .constrainAs(mainTimer) {
                        top.linkTo(if (portraitMode)
                                        mainDisplayGuide
                                   else
                                        settingsButton.bottom)
                        start.linkTo(parent.start, 16.dp)
                        end.linkTo(parent.end, 16.dp)
                    }
            )

            // split time
            val splitTime = stopwatchSplit.value
            val splitString = getDisplayTime(splitTime)
            if ((stopwatchState.value == SPLIT_RUNNING_STATE) ||
                (stopwatchState.value == SPLIT_STOPPED_STATE)) {
                Text(
                    splitString,
                    fontSize = 40.sp,
                    fontFamily = stopwatchFontFamily,
                    textAlign = TextAlign.End,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .constrainAs(splitTimer) {
                        end.linkTo(mainTimer.end)
                        top.linkTo(mainTimer.bottom)
                        }
                )
            }

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
                readyToDraw = true
            }
        }
    )

}


//-------------------------
//  constants
//-------------------------

private const val TAG = "MainActivity"

private const val BUTTON_START_TEXT = "START"
private const val BUTTON_STOP_TEXT = "STOP"

private const val BUTTON_SPLIT_TEXT = "SPLIT"
private const val BUTTON_CLEAR_TEXT = "CLEAR"

private const val CLICK_SOUND_ID = 1
