package com.lightningshop.jobtrack.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.offset
import com.lightningshop.jobtrack.settings.AppPreferences
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CornerCalculator(prefs: AppPreferences) {
  val scope = rememberCoroutineScope()
  val calcPrefs by prefs.calculatorPrefs.collectAsState(
    initial = com.lightningshop.jobtrack.settings.CalculatorPrefs(
      defaultRatePerSqft = 0.22,
      defaultMultiplier = 1.0,
      cornerX = 0.85f,
      cornerY = 0.65f,
    ),
  )
  val currency by prefs.currencyCode.collectAsState(initial = "USD")

  var showSheet by remember { mutableStateOf(false) }

  val bubbleSize = 46.dp
  val bubbleSizePx = with(LocalDensity.current) { bubbleSize.toPx() }

  var parentW by remember { mutableStateOf(1f) }
  var parentH by remember { mutableStateOf(1f) }

  fun clampOffset(o: Offset): Offset {
    val maxX = (parentW - bubbleSizePx).coerceAtLeast(0f)
    val maxY = (parentH - bubbleSizePx).coerceAtLeast(0f)
    return Offset(o.x.coerceIn(0f, maxX), o.y.coerceIn(0f, maxY))
  }

  var offset by remember(parentW, parentH, calcPrefs.cornerX, calcPrefs.cornerY) {
    val x = calcPrefs.cornerX * (parentW - bubbleSizePx).coerceAtLeast(0f)
    val y = calcPrefs.cornerY * (parentH - bubbleSizePx).coerceAtLeast(0f)
    mutableStateOf(clampOffset(Offset(x, y)))
  }

  Box(
    modifier = Modifier.onGloballyPositioned {
      parentW = it.size.width.toFloat()
      parentH = it.size.height.toFloat()
    },
  ) {
    Box(
      modifier = Modifier
        .offset { IntOffset(offset.x.roundToInt(), offset.y.roundToInt()) }
        .size(bubbleSize)
        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
        .pointerInput(Unit) {
          detectTapGestures(onTap = { showSheet = true })
        }
        .pointerInput(Unit) {
          detectDragGestures(
            onDrag = { change, dragAmount ->
              change.consume()
              offset = clampOffset(offset + dragAmount)
            },
            onDragEnd = {
              val maxX = (parentW - bubbleSizePx).coerceAtLeast(0f)
              val maxY = (parentH - bubbleSizePx).coerceAtLeast(0f)
              val nx = if (maxX == 0f) 0f else (offset.x / maxX).coerceIn(0f, 1f)
              val ny = if (maxY == 0f) 0f else (offset.y / maxY).coerceIn(0f, 1f)
              scope.launch { prefs.setCalculatorCorner(nx, ny) }
            },
          )
        },
      contentAlignment = Alignment.Center,
    ) {
      Text("∑", style = MaterialTheme.typography.titleLarge)
    }
  }

  if (showSheet) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
      onDismissRequest = { showSheet = false },
      sheetState = sheetState,
    ) {
      CalculatorSheet(
        prefs = prefs,
        initialRate = calcPrefs.defaultRatePerSqft,
        initialMultiplier = calcPrefs.defaultMultiplier,
        currencyCode = currency,
      )
    }
  }
}

private operator fun Offset.plus(other: Offset): Offset = Offset(x + other.x, y + other.y)
