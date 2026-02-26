package com.ruslan.mynotes.ui.editnote.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color as ComposeColor

@Composable
fun ColorPickerDialog(
    currentColor: Int,
    onColorPicked: (Int) -> Unit,
    onClose: () -> Unit,
) {
    val startColor = remember { ComposeColor(currentColor) }

    var colorState by remember {
        mutableStateOf(ColorState(startColor, 1f))
    }

    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("Выберите цвет") },
        confirmButton = {
            Button(onClick = {
                onColorPicked(colorState.toFinal().toArgb())
                onClose()
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            Button(onClick = onClose) {
                Text("Отмена")
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(colorState.toFinal(), RoundedCornerShape(8.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                    )

                    Slider(
                        value = colorState.brightness,
                        onValueChange = { colorState = colorState.copy(brightness = it) },
                        valueRange = 0.2f..1f,
                        modifier = Modifier.weight(1f)
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    val hue = (offset.x / size.width) * 360f
                                    colorState = colorState.copy(color = ComposeColor.hsv(hue, 1f, 1f))
                                },
                                onDrag = { change, _ ->
                                    val hue = (change.position.x / size.width) * 360f
                                    colorState = colorState.copy(color = ComposeColor.hsv(hue, 1f, 1f))
                                }
                            )
                        }
                ) {
                    val gradientColors = remember {
                        listOf(
                            ComposeColor.Red,
                            ComposeColor.Yellow,
                            ComposeColor.Green,
                            ComposeColor.Cyan,
                            ComposeColor.Blue,
                            ComposeColor.Magenta,
                            ComposeColor.Red
                        )
                    }

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawRect(
                            brush = Brush.horizontalGradient(
                                colors = gradientColors,
                                tileMode = TileMode.Clamp
                            )
                        )

                        val hsv = FloatArray(3)
                        android.graphics.Color.colorToHSV(colorState.color.toArgb(), hsv)
                        val selectorX = (hsv[0] / 360f) * size.width
                        val centerY = size.height / 2f

                        drawCircle(
                            color = ComposeColor.White,
                            center = Offset(selectorX, centerY),
                            radius = 8.dp.toPx(),
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
                }
            }
        }
    )
}

private data class ColorState(
    val color: ComposeColor,
    val brightness: Float,
) {
    fun toFinal(): ComposeColor {
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(color.toArgb(), hsv)
        hsv[2] = brightness.coerceIn(0f, 1f)
        return ComposeColor(android.graphics.Color.HSVToColor(hsv))
    }
}