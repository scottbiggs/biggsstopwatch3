package com.sleepfuriously.biggsstopwatch3

import android.content.res.Configuration
import android.media.SoundPool
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
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

        // set the sound system
        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .build()
        soundPool!!.load(baseContext, R.raw.button_click, 1)
    }
}


/**
 * Plays a click (assuming that the preferences allow it)
 */
fun click() {
    if (mainViewModel.clickOn) {
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
                        when (mainViewModel.stopwatchState) {
                            START_STATE -> stringResource(id = R.string.start)
                            RUNNING_STATE -> stringResource(id = R.string.stop)
                            STOPPED_STATE -> stringResource(id = R.string.start)
                            SPLIT_RUNNING_STATE ->  stringResource(id = R.string.stop)
                            SPLIT_STOPPED_STATE -> stringResource(id = R.string.start)
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
                    enabled = mainViewModel.stopwatchState != START_STATE,
                    onClick = {
                        click()
                        mainViewModel.nextState(BUTTON_SPLIT_CLEAR)
                        Log.d(TAG, "split button click")
                }) {
                    val splitClearButtonTxt =
                        when (mainViewModel.stopwatchState) {
                            START_STATE -> ""
                            RUNNING_STATE -> stringResource(id = R.string.split)
                            STOPPED_STATE -> stringResource(id = R.string.clear)
                            SPLIT_RUNNING_STATE -> stringResource(id = R.string.split)
                            SPLIT_STOPPED_STATE -> stringResource(id = R.string.clear)
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

            // names for the constrained widgets
            val (
                mainTimer, splitTimer, dropdownMenu
            ) = createRefs()

            // line for the top of the main display
            val mainDisplayGuide = createGuidelineFromTop(
                if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    0.33f   // about a third down for portrait mode
                }
                else {
                    0f      // keep this at top for landscape mode
                }
            )

            // settings menu
            MyDropdownMenu(modifier = Modifier
                .constrainAs(dropdownMenu) {
                    top.linkTo(parent.top)
                    end.linkTo(parent.end)
                }
            )


            // main display
            AutoSizeText(
                text = getDisplayTime(mainViewModel.tick),
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
                                        dropdownMenu.bottom)
                        start.linkTo(parent.start, 16.dp)
                        end.linkTo(parent.end, 16.dp)
                    }
            )

            // split time
            val splitString = getDisplayTime(mainViewModel.stopwatchSplit)
            if ((mainViewModel.stopwatchState == SPLIT_RUNNING_STATE) ||
                (mainViewModel.stopwatchState == SPLIT_STOPPED_STATE)) {
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

/**
 * Displays a dropdown menu button (the 3 dots in vertical column) and
 * handles the menu selection results.
 *
 * @param   modifier        Compose modifier.  I expect this to be done
 *                          through a constraint layout, so
 *                          all the constraints will be here.
 *
 * side effects
 *      mainViewmodel.clickOn
 *      mainViewmodel.stayOn
 *      mainViewmodel.vibrateOn
 */
@Composable
fun MyDropdownMenu(modifier: Modifier) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier)
    {
        IconButton(onClick = { expanded = !expanded }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                tint = if (isSystemInDarkTheme()) Color.White else Color.Black,
                contentDescription = "settings"
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = {
                    Text(
                        if (mainViewModel.clickOn)
                            stringResource(id = R.string.settings_menu_sound_on)
                        else
                            stringResource(id = R.string.settings_menu_sound_on)
                    )
                },
                onClick = {
                    mainViewModel.toggleSound()
                    val str = if (mainViewModel.clickOn) "now playing" else "turned off"
                    Toast.makeText(context, "Sound is $str", Toast.LENGTH_LONG).show()
                    expanded = false        // closes the menu
                }
            )

            Divider()

            DropdownMenuItem(
                text = {
                    Text(
                        if (mainViewModel.stayOn)
                            stringResource(id = R.string.settings_menu_screen_saver_on)
                        else
                            stringResource(id = R.string.settings_menu_screen_saver_off)
                    )
                },
                onClick = {
                    mainViewModel.toggleStayOn()
                    val str = if (mainViewModel.stayOn) "never engage" else "work normally"
                    Toast.makeText(context, "Screen saver will $str", Toast.LENGTH_SHORT).show()
                    expanded = false
                }
            )

            Divider()

            DropdownMenuItem(
                text = {
                    Text(
                        if (mainViewModel.vibrateOn)
                            stringResource(id = R.string.settings_menu_vibrate_on)
                        else
                    stringResource(id = R.string.settings_menu_vibrate_off)
                    )
                },
                onClick = {
                    mainViewModel.toggleVibrateOn()
                    val str = if (mainViewModel.vibrateOn) "on" else "off"
                    Toast.makeText(context, "Vibration is turned $str", Toast.LENGTH_LONG).show()
                    expanded = false
                }
            )

        }

    }
}


//-------------------------
//  constants
//-------------------------

private const val TAG = "MainActivity"

private const val CLICK_SOUND_ID = 1
