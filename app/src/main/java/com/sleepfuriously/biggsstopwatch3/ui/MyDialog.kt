package com.sleepfuriously.biggsstopwatch3.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.sleepfuriously.biggsstopwatch3.R


/**
 * Simple dialog window for displaying some text.  The title will
 * appear at the top in a slightly bigger font than the msg.  And
 * of course the launcher icon will be displayed too.  Scrollable.
 */
@Composable
fun MyDialog(
    title : String,
    msg: String,
    onDismiss: () -> Unit = {}
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
            ) {
                Text(
                    title,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                MyScrollText(msg)

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.End)
                ) {
                    Text(
                        stringResource(id = R.string.ok),
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
    }
}

/**
 * This displays text in a scrollable view.  The height is kind of
 * stuck, but I don't see any way around this.  Until google actually
 * implements scrollable Text functions, this'll have to do.  Sigh.
 */
@Composable
fun MyScrollText(str: String) {
    val scroll = rememberScrollState(0)

    LazyColumn {
        item {
            Text(
                text = str,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .padding(8.dp)
                    .heightIn(0.dp, 300.dp)
                    .verticalScroll(scroll)
            )
        }
    }
}


@Preview
@Composable
private fun Preview() {
    MyDialog(title = "Test", msg = "This is a test")
}