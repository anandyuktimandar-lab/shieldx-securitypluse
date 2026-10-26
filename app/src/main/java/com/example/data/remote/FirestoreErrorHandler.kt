package com.example.data.remote

import android.util.Log
import org.json.JSONObject

enum class OperationType(val value: String) {
    CREATE("create"),
    UPDATE("update"),
    DELETE("delete"),
    LIST("list"),
    GET("get"),
    WRITE("write")
}

fun handleFirestoreError(error: Exception, operationType: OperationType, path: String?) {
    val errInfo = JSONObject().apply {
        put("error", error.message ?: error.toString())
        put("operationType", operationType.value)
        put("path", path)
        put("mode", "direct_access_no_auth")
    }

    val jsonString = errInfo.toString()
    Log.e("FirestoreError", jsonString, error)
    throw RuntimeException(jsonString, error)
}
