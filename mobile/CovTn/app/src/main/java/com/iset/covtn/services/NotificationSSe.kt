package com.iset.covtn.services

import android.os.Looper
import android.util.Log
import androidx.core.os.postDelayed
import okhttp3.*
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import java.util.concurrent.TimeUnit
import java.util.logging.Handler

class NotificationSSe(
    private val url: String,
    private val onMessage: (String) -> Unit,
    private val onError: (String) -> Unit
) {
    private val client = OkHttpClient.Builder()
        .retryOnConnectionFailure(true)
        .connectTimeout(0,TimeUnit.SECONDS)
        .readTimeout(0,TimeUnit.SECONDS)
        .writeTimeout(0,TimeUnit.SECONDS)
        .build()



    fun start() {
        val request = Request.Builder()
            .url(url)
            .build()

        EventSources.createFactory(client)
            .newEventSource(request, object : EventSourceListener() {

                override fun onOpen(eventSource: EventSource, response: Response) {
                    Log.d("SSE", "Connected to SSE server")
                }

                override fun onEvent(
                    eventSource: EventSource,
                    id: String?,
                    type: String?,
                    data: String
                ) {
                    Log.d("SSE", "Received event: type=$type data=$data")

                    if (type == "notification") {
                        onMessage(data)
                    } else {
                        onError("Unknown event type: $type")
                    }
                }

                override fun onClosed(eventSource: EventSource) {

                    reconnect()
                    Log.d("SSE", "SSE closed")
                }

                override fun onFailure(
                    eventSource: EventSource,
                    t: Throwable?,
                    response: Response?
                ) {
                    Log.e("SSE", "Error: ${t?.message}")
                    reconnect()
                }
            })

    }

    private fun reconnect() {
        android.os.Handler(Looper.getMainLooper()).postDelayed({
            start()   // restart SSE connection
        }, 3000L)
    }


}