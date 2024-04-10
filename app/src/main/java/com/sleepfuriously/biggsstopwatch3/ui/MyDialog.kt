package com.sleepfuriously.biggsstopwatch3.ui

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sleepfuriously.biggsstopwatch3.R


@Suppress("unused")
const val TAG = "MyDialog"


/**
 * This shows a dialog with all the settings that I like.  It has only one
 * button which says 'OK'.
 *
 * @param   title               String for title
 *
 * @param   text                Message to display
 * @param   onDismiss           Function (lambda) to be called when the OK button
 *                              is hit.
 *
 * @param   onDismissRequest    What to do when the back button is hit.
 */
@Composable
fun MyDialog(
    title : String,
    text : String,
    onDismiss : () -> Unit,
    onDismissRequest : () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(title)
        },
        text = {
            MyScrollText(str = text)
        },

        confirmButton = { },        // don't show anything -- not used

        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(stringResource(id = R.string.ok))
            }
        }

    )

}

/**
 * This displays text in a scrollable view.  The height is kind of
 * stuck, but I don't see any way around this.  Until google actually
 * implements scrollable Text functions, this'll have to do.  Sigh.
 */
@Composable
fun MyScrollText(
    str: String,
    modifier: Modifier = Modifier
) {
    val scroll = rememberScrollState(0)

    LazyColumn {
        item {
            Text(
                text = str,
                style = MaterialTheme.typography.bodyMedium,
                modifier = modifier
                    .height(400.dp)
                    .verticalScroll(scroll)
            )
        }
    }
}

