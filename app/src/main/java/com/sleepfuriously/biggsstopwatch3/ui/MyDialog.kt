package com.sleepfuriously.biggsstopwatch3.ui

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


const val TAG = "MyDialog"


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

