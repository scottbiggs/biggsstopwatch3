package com.sleepfuriously.biggsstopwatch3.ui

import android.util.Log
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.text.TextStyle


private const val TAG = "AutoSizeText"


/**
 * Creates a Text() that expands to fill the available space.
 *
 * Based on:
 *       https://stackoverflow.com/a/68258975/624814
 */
@Composable
fun AutoSizeText(
    text: String,
    textStyle: TextStyle,
    modifier: Modifier = Modifier
) {
    // NOTE:  mutableStates cause recomposition whenever they change!
    //  (but only the composables that read these values recompose)
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
                Log.d(TAG, "AutosizeText($text) font size too big at ${scaledTextStyle.fontSize}")
                scaledTextStyle =
                    scaledTextStyle.copy(fontSize = scaledTextStyle.fontSize * 0.9)
            } else {
                Log.d(TAG, "AutosizeText($text) ready to draw. fontSize = ${scaledTextStyle.fontSize}")
                readyToDraw = true
            }
        }
    )
}
