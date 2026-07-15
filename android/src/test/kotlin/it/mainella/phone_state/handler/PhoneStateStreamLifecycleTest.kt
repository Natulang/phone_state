package it.mainella.phone_state.handler

import io.flutter.plugin.common.EventChannel
import org.junit.Assert.assertEquals
import org.junit.Test

class PhoneStateStreamLifecycleTest {
    @Test
    fun emitsOnlyForCurrentActiveSubscription() {
        val lifecycle = PhoneStateStreamLifecycle<String>()
        val firstSink = RecordingEventSink()
        val secondSink = RecordingEventSink()

        val firstToken = lifecycle.start(firstSink)
        lifecycle.emit(firstToken, mapOf("status" to "CALL_STARTED"))

        lifecycle.stop()
        lifecycle.emit(firstToken, mapOf("status" to "CALL_ENDED"))

        val secondToken = lifecycle.start(secondSink)
        lifecycle.emit(firstToken, mapOf("status" to "CALL_STARTED"))
        lifecycle.emit(secondToken, mapOf("status" to "CALL_ENDED"))

        lifecycle.stop()
        lifecycle.emit(secondToken, mapOf("status" to "CALL_STARTED"))

        assertEquals(listOf(mapOf("status" to "CALL_STARTED")), firstSink.events)
        assertEquals(listOf(mapOf("status" to "CALL_ENDED")), secondSink.events)
    }

    @Test
    fun unregistersRegisteredReceiverAtMostOnce() {
        val lifecycle = PhoneStateStreamLifecycle<String>()
        var unregisterCalls = 0

        lifecycle.registerReceiver("receiver")
        lifecycle.unregisterReceiver {
            unregisterCalls++
            throw IllegalArgumentException("receiver was already unregistered")
        }
        lifecycle.unregisterReceiver {
            unregisterCalls++
        }

        assertEquals(1, unregisterCalls)
    }
}

private class RecordingEventSink : EventChannel.EventSink {
    val events = mutableListOf<Any?>()

    override fun success(event: Any?) {
        events.add(event)
    }

    override fun error(errorCode: String, errorMessage: String?, errorDetails: Any?) {}

    override fun endOfStream() {}
}
