package com.dancingpumpkin.payflow

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription

@Composable
internal fun CalendarSearchHeader(active: Boolean, query: String, change: (String) -> Unit, close: () -> Unit, modifier: Modifier = Modifier) {
    val focus = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    LaunchedEffect(active) {
        if (active) { withFrameNanos { }; focus.requestFocus(); keyboard?.show() }
    }
    Row(modifier.fillMaxWidth().heightIn(min = 56.dp).padding(start = 12.dp, end = 24.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
        IconButton(onClick = close) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Close search") }
        Row(Modifier.weight(1f).padding(start = 8.dp).height(48.dp).background(Color(0xFF1C1C1C), RoundedCornerShape(28.dp)).padding(horizontal = 12.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
        SearchGlyph()
        BasicTextField(query, change, modifier = Modifier.weight(1f).padding(start = 8.dp).focusRequester(focus).semantics { contentDescription = "Search for events" },
            singleLine = true, textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface), cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search), keyboardActions = KeyboardActions(onSearch = { keyboard?.hide() }),
            decorationBox = { inner -> Box { if (query.isEmpty()) Text("Search for events", color = MaterialTheme.colorScheme.onSurfaceVariant); inner() } },
        )
        }
    }
}
