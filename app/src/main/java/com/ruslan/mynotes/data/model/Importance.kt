package com.ruslan.mynotes.data.model

enum class Importance {
    LOW, NORMAL, HIGH;

    fun getEmojiName(): String = when (this) {
        LOW -> "🟢 Неважная"
        NORMAL -> "🔵 Обычная"
        HIGH -> "🔴 Важная"
    }
}