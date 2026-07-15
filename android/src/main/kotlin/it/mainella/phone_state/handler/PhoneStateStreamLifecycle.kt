package it.mainella.phone_state.handler

import io.flutter.plugin.common.EventChannel

internal class PhoneStateStreamLifecycle<T> {
    private var active = false
    private var token = 0L
    private var eventSink: EventChannel.EventSink? = null
    private var receiver: T? = null

    @Synchronized
    fun start(events: EventChannel.EventSink?): Long {
        token += 1
        active = true
        eventSink = events
        return token
    }

    @Synchronized
    fun stop() {
        token += 1
        active = false
        eventSink = null
    }

    @Synchronized
    fun isActive(expectedToken: Long): Boolean {
        return active && expectedToken == token
    }

    fun emit(expectedToken: Long, event: Map<String, Any?>) {
        val sink = synchronized(this) {
            if (active && expectedToken == token) eventSink else null
        }
        sink?.success(event)
    }

    @Synchronized
    fun registerReceiver(receiver: T) {
        this.receiver = receiver
    }

    fun unregisterReceiver(unregister: (T) -> Unit) {
        val registeredReceiver = synchronized(this) {
            val currentReceiver = receiver ?: return
            receiver = null
            currentReceiver
        }

        try {
            unregister(registeredReceiver)
        } catch (_: IllegalArgumentException) {
        }
    }
}
