package com.sleepfuriously.biggsstopwatch3.ui

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.media.SoundPool
import android.os.Build
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.getString
import androidx.core.content.res.ResourcesCompat
import com.sleepfuriously.biggsstopwatch3.MainViewModel
import com.sleepfuriously.biggsstopwatch3.MainViewModel.Companion.BUTTON_SPLIT_CLEAR
import com.sleepfuriously.biggsstopwatch3.R
import com.sleepfuriously.biggsstopwatch3.getDisplayTime
import com.sleepfuriously.biggsstopwatch3.ui.theme.BiggsStopwatch3Theme
import com.sleepfuriously.biggsstopwatch3.ui.theme.brown
import com.sleepfuriously.biggsstopwatch3.ui.theme.maroon_light


//-----------------------------
//  composable functions
//-----------------------------

/**
 * Displays the main screen of the app.
 *
 * @param   mainViewModel       Reference to view model
 *
 * @param   soundPool           Reference SoundPool
 */
@Composable
fun MainScreen(mainViewModel : MainViewModel, soundPool: SoundPool) {

    val portraitMode = LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT

    val ctx = LocalContext.current
    val stopwatchFontFamily = remember {
        FontFamily(
            typeface = ResourcesCompat.getFont(ctx,
                R.font.alarm_clock)!!
        )
    }

    BiggsStopwatch3Theme {

        // Surface for entire drawing.
        Surface {

            // the buttons (at the bottom)
            DrawButtons(ctx, mainViewModel, soundPool)

            // The timer displays (middle)
            DrawTimers(mainViewModel, stopwatchFontFamily)

            // The 3 dots menu at top right
            DrawDropDownMenu(
                mainViewModel = mainViewModel,
                ctx = ctx
            )
        }

    }

}


@Composable
fun DrawButtons(ctx: Context, mainViewModel : MainViewModel, soundPool: SoundPool) {
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
                    MainViewModel.START_STATE -> stringResource(id = R.string.start)
                    MainViewModel.RUNNING_STATE -> stringResource(id = R.string.stop)
                    MainViewModel.STOPPED_STATE -> stringResource(id = R.string.start)
                    MainViewModel.SPLIT_RUNNING_STATE ->  stringResource(id = R.string.stop)
                    MainViewModel.SPLIT_STOPPED_STATE -> stringResource(id = R.string.start)
                    else -> "error"
                }
            DownUpButton(
                title = startStopButtonTxt,
                modifier = Modifier
                    .weight(1f),
                backgroundColor = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) maroon_light
                else MaterialTheme.colorScheme.onPrimaryContainer,
                textColor = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) Color.White
                else MaterialTheme.colorScheme.onPrimary,
                touchedBackgroundColor = MaterialTheme.colorScheme.secondary,
                touchedTextColor = MaterialTheme.colorScheme.onSecondary,
                downTouchFun = {
                    mainViewModel.nextState(MainViewModel.BUTTON_START_STOP)
                    playClick(mainViewModel, soundPool)
                    vibrate(ctx, BUTTON_VIBRATION_DURATION, mainViewModel)
                    Log.d(TAG, "start button click")
                }
            )

            Spacer(Modifier.width(16.dp))

            // SPLIT / CLEAR
            val splitClearButtonTxt =
                when (mainViewModel.stopwatchState) {
                    MainViewModel.START_STATE -> ""
                    MainViewModel.RUNNING_STATE -> stringResource(id = R.string.split)
                    MainViewModel.STOPPED_STATE -> stringResource(id = R.string.clear)
                    MainViewModel.SPLIT_RUNNING_STATE -> stringResource(id = R.string.split)
                    MainViewModel.SPLIT_STOPPED_STATE -> stringResource(id = R.string.clear)
                    else -> "error"
                }
            DownUpButton(
                title = splitClearButtonTxt,
                modifier = Modifier
                    .weight(1f),
                backgroundColor = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) brown
                else MaterialTheme.colorScheme.onTertiaryContainer,
                textColor = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) Color.White
                else MaterialTheme.colorScheme.onPrimary,
                touchedBackgroundColor = MaterialTheme.colorScheme.tertiary,
                touchedTextColor = MaterialTheme.colorScheme.onTertiary,
                enabled = mainViewModel.stopwatchState != MainViewModel.START_STATE,
                downTouchFun = {
                    playClick(mainViewModel, soundPool)
                    mainViewModel.nextState(BUTTON_SPLIT_CLEAR)
                    MyVibrator.vibrate(ctx, BUTTON_VIBRATION_DURATION)
                    Log.d(TAG, "split button click")
                }
            )

        }
    }
}

@Composable
fun DrawTimers(mainViewModel: MainViewModel, stopwatchFontFamily: FontFamily) {

    // Figure out the padding for the top timer.  This depends on whether we are
    // in landscape or portrait.
    val config = LocalConfiguration.current
    val screenHeight = config.screenHeightDp.dp
    val screenWidth = config.screenWidthDp.dp

    val padding = if (screenHeight > screenWidth) {
        // portrait mode: make the padding a little more than 25% of the screen size.
        screenHeight / 4
    }
    else {
        // landscape mode: just use 40dp
        40.dp
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 8.dp, top = padding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        DrawMainTimer(mainViewModel, stopwatchFontFamily)
        DrawSplitTimer(mainViewModel, stopwatchFontFamily)
    }
}

@Composable
fun DrawMainTimer(mainViewModel: MainViewModel, stopwatchFontFamily: FontFamily) {
    AutoSizeText(
        text = getDisplayTime(mainViewModel.tick),
        textStyle = TextStyle(
            fontFamily = stopwatchFontFamily,
            fontSize = 240.sp,
            // main color depends on the theme. We use dark theme for old devices too.
            color = if (isSystemInDarkTheme() or (Build.VERSION.SDK_INT < Build.VERSION_CODES.S)) Color.White
            else Color.Black,
            shadow = Shadow(
                offset = Offset(5.0f, 7.0f),
                color = MaterialTheme.colorScheme.primary,
                blurRadius = 3f
            )
        ),
        modifier = Modifier
            .fillMaxWidth()
    )
}

@Composable
fun DrawSplitTimer(mainViewModel: MainViewModel, stopwatchFontFamily: FontFamily) {
    if (mainViewModel.stopwatchSplit == 0L) {
        return
    }

    Text(
        text = getDisplayTime(mainViewModel.stopwatchSplit),
        fontSize = 48.sp,
        fontFamily = stopwatchFontFamily,
        color = MaterialTheme.colorScheme.secondary
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
 * @param   mainViewModel   Reference to viewModel so its side effects
 *                          can be done.
 *
 * side effects
 *      mainViewmodel.clickOn
 *      mainViewmodel.stayOn
 *      mainViewmodel.vibrateOn
 */
@Composable
fun DrawDropDownMenu(modifier: Modifier = Modifier, mainViewModel: MainViewModel, ctx: Context) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentSize(Alignment.TopEnd)
    ) {
        IconButton(
            onClick = {
                expanded = !expanded
                MyVibrator.vibrate(ctx, BUTTON_VIBRATION_DURATION)
            }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                tint = if (isSystemInDarkTheme() or (Build.VERSION.SDK_INT < Build.VERSION_CODES.S)) Color.White
                else Color.Black,
                contentDescription = "settings"
            )
        }

        DropdownMenu(
            modifier = Modifier.align(Alignment.TopEnd),
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
                enabled = MyVibrator.hasVibrator(ctx),
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
                    MyVibrator.vibrate(ctx, BUTTON_VIBRATION_DURATION)
                }
            )

            Divider()

            // only show the dialog if showDialog is true
            var showDialog by remember { mutableStateOf(false) }
            if (showDialog) {
                MyDialog(
                    title = stringResource(id = R.string.about_title),
                    text = stringResource(id = R.string.about_msg),
                    onDismiss = {
                        Log.d(TAG, "dismiss button hit")
                        showDialog = false
                        expanded = false
                    },
                    onDismissRequest = {
                        Log.d(TAG, "dismissRequesthit")
                        showDialog = false
                        expanded = false
                    }
                )
            }

            DropdownMenuItem(
                text = {
                    Text(stringResource(id = R.string.about))
                },
                onClick = {
                    showDialog = true
                }
            )

        }

    }
}

//-----------------------------
//  misc functions
//-----------------------------

/**
 * Guess what this does?  And can you guess what happens if vibration is
 * turned off?  Yep, I thought you could!
 *
 * preconditions
 *      mainViewModel.vibrateOn     Only vibrates if this value is true.
 *
 * @param   ctx         Context for playing this sound
 *
 * @param   millis      Number of milliseconds to vibrate
 *
 * @param   mainViewModel   Reference to viewModel.  Needed to make sure
 *                      that we aren't double vibrating.
 *
 */
fun vibrate(ctx: Context, millis: Long, mainViewModel: MainViewModel) {

    // don't bother if vibration is turned off
    if (!mainViewModel.vibrateOn)
        return

    MyVibrator.vibrate(ctx, millis)
}


/**
 * Plays a click (assuming that the preferences allow it)
 */
fun playClick(mainViewModel: MainViewModel, soundPool: SoundPool) {
    if (mainViewModel.clickOn) {
        soundPool.play(CLICK_SOUND_ID, 1f, 1f, 0, 0, 1f)
    }
}


//-----------------------------
//  constants
//-----------------------------

private const val TAG = "MainScreen"

private const val CLICK_SOUND_ID = 1

/** time in milliseconds the phone should vibrate when a button is pressed */
private const val BUTTON_VIBRATION_DURATION = 15L

