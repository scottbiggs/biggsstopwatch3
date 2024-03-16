package com.sleepfuriously.biggsstopwatch3

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.media.SoundPool
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.content.ContextCompat.getString
import androidx.core.content.ContextCompat.getSystemService
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
import java.util.concurrent.CancellationException


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

/** when TRUE, this device is able to vibrate */
private var hasVibe = true


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

        // does this device have a vibrator?
        val vibrator = getSystemService(this, Vibrator::class.java)
        hasVibe = vibrator?.hasVibrator() ?: false

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

//-------------------
//  free functions
//-------------------

/**
 * Plays a click (assuming that the preferences allow it)
 */
fun playClick() {
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

/**
 * Guess what this does?  And can you guess what happens if vibration is
 * turned off?  Yep, I thought you could!
 *
 * preconditions
 *      mainViewModel.vibrateOn     Only vibrates if this value is true.
 *
 * @param   millis      Number of milliseconds to vibrate
 *
 * @param   ctx         Context for playing this sound
 *
 */
fun vibrate(millis: Long, ctx: Context) {

    // don't bother if vibration is turned off
    if (!mainViewModel.vibrateOn)
        return

    val vibrator = getSystemService(ctx, Vibrator::class.java)

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
                // START / STOP
                val startStopButtonTxt =
                    when (mainViewModel.stopwatchState) {
                        START_STATE -> stringResource(id = R.string.start)
                        RUNNING_STATE -> stringResource(id = R.string.stop)
                        STOPPED_STATE -> stringResource(id = R.string.start)
                        SPLIT_RUNNING_STATE ->  stringResource(id = R.string.stop)
                        SPLIT_STOPPED_STATE -> stringResource(id = R.string.start)
                        else -> "error"
                    }
                DownUpButton(
                    title = startStopButtonTxt,
                    modifier = Modifier
                        .weight(1f),
                    backgroundColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    textColor = MaterialTheme.colorScheme.onPrimary,
                    touchedBackgroundColor = MaterialTheme.colorScheme.secondary,
                    touchedTextColor = MaterialTheme.colorScheme.onSecondary,
                    downTouchFun = {
                        mainViewModel.nextState(BUTTON_START_STOP)
                        playClick()
                        vibrate(BUTTON_VIBRATION_DURATION, ctx)
                        Log.d(TAG, "start button click")
                    }
                )

                Spacer(Modifier.width(16.dp))

                // SPLIT / CLEAR
                val splitClearButtonTxt =
                    when (mainViewModel.stopwatchState) {
                        START_STATE -> ""
                        RUNNING_STATE -> stringResource(id = R.string.split)
                        STOPPED_STATE -> stringResource(id = R.string.clear)
                        SPLIT_RUNNING_STATE -> stringResource(id = R.string.split)
                        SPLIT_STOPPED_STATE -> stringResource(id = R.string.clear)
                        else -> "error"
                    }
                DownUpButton(
                    title = splitClearButtonTxt,
                    modifier = Modifier
                        .weight(1f),
                    backgroundColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    textColor = MaterialTheme.colorScheme.onPrimary,
                    touchedBackgroundColor = MaterialTheme.colorScheme.tertiary,
                    touchedTextColor = MaterialTheme.colorScheme.onTertiary,
                    enabled = mainViewModel.stopwatchState != START_STATE,
                    downTouchFun = {
                        playClick()
                        mainViewModel.nextState(BUTTON_SPLIT_CLEAR)
                        vibrate(BUTTON_VIBRATION_DURATION, ctx)
                        Log.d(TAG, "split button click")
                    }
                )

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
    val ctx = LocalContext.current
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier)
    {
        IconButton(onClick = {
            expanded = !expanded
            vibrate(BUTTON_VIBRATION_DURATION, ctx)
        }) {
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
                        if (mainViewModel.clickOn) {
                            stringResource(id = R.string.settings_menu_sound_on)
                        }
                        else {
                            stringResource(id = R.string.settings_menu_sound_off)
                        }
                    )
                },
                onClick = {
                    mainViewModel.toggleSound()
                    val str = if (mainViewModel.clickOn)
                                getString(ctx, R.string.sound_on_toast_msg)
                              else
                                getString(ctx, R.string.sound_off_toast_msg)
                    Toast.makeText(ctx, str, Toast.LENGTH_LONG).show()
                    expanded = false        // closes the menu
                }
            )

            Divider()

            DropdownMenuItem(
                text = {
                    Text(
                        // if we are currently disabling the screen saver, have the menu
                        // suggest the opposite
                        if (mainViewModel.stayAwake)
                            stringResource(id = R.string.settings_menu_enable_screen_saver)
                        else
                            stringResource(id = R.string.settings_menu_disable_screen_saver)
                    )
                },
                onClick = {
                    mainViewModel.toggleStayOn()
                    val str =
                        if (mainViewModel.stayAwake)
                            getString(ctx, R.string.screen_saver_disabled_toast_msg)
                        else
                            getString(ctx, R.string.screen_saver_enabled_toast_msg)
                    Toast.makeText(ctx, str, Toast.LENGTH_SHORT).show()
                    expanded = false

                    // enable/disable screen saver
                    val activity = ctx as Activity
                    if (mainViewModel.stayAwake) {
                        activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    }
                    else {
                        activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    }
                }
            )

            Divider()

            DropdownMenuItem(
                enabled = hasVibe,
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
                    val str =
                        if (mainViewModel.vibrateOn)
                            getString(ctx, R.string.vibration_on_toast_msg)
                        else
                            getString(ctx, R.string.vibration_off_toast_msg)
                    Toast.makeText(ctx, str, Toast.LENGTH_LONG).show()
                    expanded = false
                    vibrate(BUTTON_VIBRATION_DURATION, ctx)
                }
            )

        }

    }
}


/**
 * This is a button that activates specifically on the DOWN touch of the button
 * and/or the button release.
 *
 * Note that this does NOT have a ripple effect.  It changes color on touch,
 * which is handled by this function.
 *
 * @param   title           Title for the button
 *
 * @param   modifier        You've seen this before!
 *
 * @param   backgroundColor Color for the background of the button.  Defaults to
 *                          the primary color.
 *
 * @param   textColor       Color for the text of the button.  Defaults on onPrimary.
 *
 * @param   touchedBackgroundColor      The color for the background while this button
 *                                      is being touched.  Defaults to tertiary.
 *
 * @param   touchedTextColor            Color of the text when the button is touched.
 *                                      Defaults to onTertiary.
 *
 * @param   enabled         Set this to false to disable this button. Default = true.
 *
 * @param   downTouchFun    Run this function when the button goes down
 *
 * @param   upTouchFun      Function to run when the button has an UP action.
 *                          Note that if the user slides his finger away from
 *                          the button before lifting, then this will not trigger.
 */
@Composable
fun DownUpButton(
    title: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
    textColor: Color = MaterialTheme.colorScheme.onPrimary,
    touchedBackgroundColor: Color = MaterialTheme.colorScheme.tertiary,
    touchedTextColor: Color = MaterialTheme.colorScheme.onTertiary,
    enabled: Boolean = true,
    downTouchFun: () -> Unit = {},
    upTouchFun: () -> Unit = {}
) {
    var touchedDown : Boolean by rememberSaveable{ mutableStateOf(false) }

    var currentBackgroundColor = backgroundColor
    var currentTextColor = textColor

    // necessary for the colors to work correctly.
    if (touchedDown) {
        Log.d(TAG, "SampleButton2() if touchedDown...")
//        currentBackgroundColor = MaterialTheme.colorScheme.tertiary
//        currentTextColor = MaterialTheme.colorScheme.onTertiary
        currentBackgroundColor = touchedBackgroundColor
        currentTextColor = touchedTextColor
    }

    if (!enabled) {
        // disabled -- draw a fake button
        Card(
            modifier = modifier,
            shape = RoundedCornerShape(40f),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
            )
        ) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .padding(12.dp)
                    .align(Alignment.CenterHorizontally)
            )
        }
    }
    else {
        // draw the "real" button
        Card(
            modifier = modifier
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            Log.i(TAG, "On Touch Down.")
                            downTouchFun.invoke()
                            touchedDown = true
                            //start
                            val released = try {
                                tryAwaitRelease()
                            } catch (c: CancellationException) {
                                // this happens if the touch drags
                                false
                            }
                            if (released) {
                                //ACTION_UP
                                Log.i(TAG, "On Touch Release.")
                                touchedDown = false
                                Log.d(TAG, "button released. Running upTouchFun.invoke()")
                                upTouchFun.invoke()
                            } else {
                                //CANCELED
                                Log.i(TAG, "On Touch Cancelled.")
                                touchedDown = false
                            }
                        },
                    )
                },
            shape = RoundedCornerShape(40f),
            colors = CardDefaults.cardColors(
                containerColor = currentBackgroundColor,
            ),
        ) {
            Text(
                text = title,
                color = currentTextColor,
                modifier = Modifier
                    .padding(12.dp)
                    .align(Alignment.CenterHorizontally)
            )
        }
    }
}


//-------------------------
//  constants
//-------------------------

private const val TAG = "MainActivity"

private const val CLICK_SOUND_ID = 1

/** time in milliseconds the phone should vibrate when a button is pressed */
private const val BUTTON_VIBRATION_DURATION = 15L