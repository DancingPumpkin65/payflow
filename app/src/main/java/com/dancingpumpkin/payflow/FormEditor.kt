package com.dancingpumpkin.payflow

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.activity.compose.BackHandler
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/** Use the phone window while keeping the save action above the keyboard. */
@Composable
internal fun FormEditor(
    title: String,
    dismiss: () -> Unit,
    action: @Composable (finish: (() -> Unit) -> Unit) -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val progress = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val keyboard = LocalSoftwareKeyboardController.current
    var closing by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { progress.animateTo(1f, tween(300, easing = LinearOutSlowInEasing)) }
    fun finish(after: () -> Unit) {
        if (closing) return
        closing = true
        keyboard?.hide()
        scope.launch {
            try { progress.animateTo(0f, tween(240, easing = FastOutLinearInEasing)) }
            finally { after() }
        }
    }
    Dialog(onDismissRequest = { finish(dismiss) }, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        BackHandler { finish(dismiss) }
        Surface(Modifier.fillMaxSize().graphicsLayer { alpha = progress.value; translationY = (1f - progress.value) * 32.dp.toPx() }, color = MaterialTheme.colorScheme.background) {
            Column(Modifier.fillMaxSize().safeDrawingPadding().imePadding()) {
                Row(Modifier.fillMaxWidth().heightIn(min = 56.dp).padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { finish(dismiss) }) { Icon(Icons.Default.Close, "Cancel edit") }
                    Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                    action(::finish)
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                Column(Modifier.weight(1f).fillMaxWidth().padding(24.dp)) {
                    FormFields(content)
                }
            }
        }
    }
}
