package com.ruslan.mynotes.data.model

import android.graphics.Color
import org.json.JSONObject

fun Note.toJson(): JSONObject {
    val result = JSONObject()
    result.put("uid", id)
    result.put("title", name)
    result.put("content", text)
    if (bgColor != Color.WHITE) {
        result.put("color", bgColor)
    }
    if (level != Importance.NORMAL) {
        result.put("importance", level.name)
    }
    return result
}