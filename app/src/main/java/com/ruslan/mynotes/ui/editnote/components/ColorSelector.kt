package com.ruslan.mynotes.ui.editnote.components

import android.graphics.Color
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.unit.dp

private val PresetColors = listOf(
    Color.parseColor("#6B8E23") to "Оливковый",
    Color.parseColor("#8FBC6E") to "Салатовый",
    Color.parseColor("#E07A5F") to "Терракотовый",
    Color.parseColor("#BC6C25") to "Глиняный",
    Color.parseColor("#6C4E3A") to "Коричневый",
    Color.parseColor("#D4A373") to "Песочный"
)

@Composable
fun ColorSelector(
    selectedColor: Int,
    onColorPicked: (Int) -> Unit
) {
    var showPicker by remember { mutableStateOf(false) }
    val isCustom = selectedColor !in PresetColors.map { it.first }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Palette,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Цвет фона",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(PresetColors) { (color, name) ->
                    ColorChip(
                        color = color,
                        name = name,
                        isSelected = selectedColor == color,
                        onClick = { onColorPicked(color) }
                    )
                }
                item {
                    CustomColorChip(
                        isSelected = isCustom,
                        onClick = { showPicker = true }
                    )
                }
            }
        }
    }

    if (showPicker) {
        ColorPickerDialog(
            currentColor = selectedColor,
            onColorPicked = {
                onColorPicked(it)
                showPicker = false
            },
            onClose = { showPicker = false }
        )
    }
}

@Composable
private fun ColorChip(
    color: Int,
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(60.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(
                    width = if (isSelected) 3.dp else 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(16.dp)
                )
                .background(
                    color = ComposeColor(color),
                    shape = RoundedCornerShape(16.dp)
                )
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = if (color == Color.WHITE) ComposeColor.Black else ComposeColor.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun CustomColorChip(
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(60.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(
                    width = if (isSelected) 3.dp else 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(16.dp)
                )
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            ComposeColor.Red,
                            ComposeColor.Yellow,
                            ComposeColor.Green,
                            ComposeColor.Cyan,
                            ComposeColor.Blue,
                            ComposeColor.Magenta
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = ComposeColor.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Text(
            text = "Свой",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}