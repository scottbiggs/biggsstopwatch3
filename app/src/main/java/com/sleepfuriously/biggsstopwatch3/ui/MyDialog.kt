package com.sleepfuriously.biggsstopwatch3.ui

import android.util.Log
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.constraintlayout.compose.ConstraintLayout
import com.sleepfuriously.biggsstopwatch3.R


const val TAG = "MyDialog"


/**
 * Simple dialog window for displaying some text.  The title will
 * appear at the top in a slightly bigger font than the msg.  And
 * of course the launcher icon will be displayed too.  Scrollable.
 */
@Composable
fun MyDialog(
    titleStr : String,
    msgStr: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            ConstraintLayout(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                // names for the constrained widgets
                val (
                    title, msg, ok
                ) = createRefs()

                // title at the top
                Text(
                    titleStr,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .constrainAs(title) {
                            top.linkTo(parent.top)
                            start.linkTo(parent.start)
                        }
                )

                // ok button at bottom right
                TextButton(
                    onClick = { onDismiss.invoke() },
                    modifier = Modifier
                        .constrainAs(ok) {
                            bottom.linkTo(parent.bottom)
                            end.linkTo(parent.end)
                        }
                ) {
                    Text(stringResource(id = R.string.ok))
                }

                // text in the middle
                MyScrollText(
                    msgStr,
                    Modifier
                        .constrainAs(msg) {
                            top.linkTo(title.bottom)
                            bottom.linkTo(ok.top)
                            centerHorizontallyTo(parent)
                        }
                )
            } // constraint layout

/*
            Column(
                modifier = Modifier
                    .padding(24.dp)
            ) {
                Text(
                    titleStr,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                MyScrollText(msgStr)

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
*/
        }
    }
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
                    .padding(8.dp, 22.dp)
                    .height(400.dp)
                    .verticalScroll(scroll)
            )
        }
    }
}



@Preview
@Composable
private fun Preview() {
    MyDialog(titleStr = "Test", msgStr = "This is a test") {
    }
}