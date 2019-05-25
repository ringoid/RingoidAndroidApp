package com.ringoid.utility.collection

import android.os.Bundle
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

fun Bundle.toJsonObject(): JSONObject {
    try {
        return toJsonValue(this) as JSONObject
    } catch (e: JSONException) {
        throw IllegalArgumentException("Cannot convert bundle to JSON: " + e.message, e)
    }
}

@Throws(JSONException::class)
private fun toJsonValue(value: Any?): Any? =
    if (value == null) {
        null
    } else if (value is Bundle) {
        val result = JSONObject()
        for (key in value.keySet()) {
            result.put(key, toJsonValue(value.get(key)))
        }
        result
    } else if (value.javaClass.isArray) {
        val xvalue = value as Array<*>
        val result = JSONArray()
        for (i in 0 until xvalue.size) {
            result.put(i, toJsonValue(xvalue[i]))
        }
        result
    } else if (value is String || value is Boolean || value is Int || value is Long || value is Double) {
        value
    } else {
        value.toString()
    }
