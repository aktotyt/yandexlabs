package com.ruslan.mynotes.model

enum class Importance {
    LOW, NORMAL, HIGH;

    fun getEmojiName(): String = when (this) {
        LOW -> "🟢 Неважная"
        NORMAL -> "🔵 Обычная"
        HIGH -> "🔴 Важная"
    }
}