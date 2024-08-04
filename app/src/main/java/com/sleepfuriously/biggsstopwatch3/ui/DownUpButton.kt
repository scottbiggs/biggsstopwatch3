package com.sleepfuriously.biggsstopwatch3.ui

import android.util.Log
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import java.util.concurrent.CancellationException

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

private const val TAG = "DownUpButton"