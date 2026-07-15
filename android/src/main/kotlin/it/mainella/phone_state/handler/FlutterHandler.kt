package it.mainella.phone_state.handler

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.EventChannel
import it.mainella.phone_state.receiver.PhoneStateReceiver
import it.mainella.phone_state.utils.Constants
import it.mainella.phone_state.utils.PhoneStateStatus
import java.util.Timer
import java.util.TimerTask

class FlutterHandler(binding: FlutterPlugin.FlutterPluginBinding) {
    private val applicationContext = binding.applicationContext
    private var phoneStateEventChannel: EventChannel = EventChannel(binding.binaryMessenger, Constants.EVENT_CHANNEL)
    private val streamLifecycle = PhoneStateStreamLifecycle<PhoneStateReceiver>()
    private var durationTimer: Timer? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    private fun startDurationUpdates(token: Long, receiver: PhoneStateReceiver) {
        durationTimer?.cancel()
        durationTimer = Timer()
        durationTimer?.schedule(object : TimerTask() {
            override fun run() {
                if (streamLifecycle.isActive(token) && receiver.status == PhoneStateStatus.CALL_STARTED) {
                    receiver.updateDuration()
                    mainHandler.post {
                        emitPhoneState(token, receiver)
                    }
                }
            }
        }, 0, 1000)
    }

    private fun stopDurationUpdates() {
        durationTimer?.cancel()
        durationTimer = null
    }

    private fun unregisterReceiver() {
        streamLifecycle.unregisterReceiver { receiver ->
            applicationContext.unregisterReceiver(receiver)
        }
    }

    private fun stopListening() {
        streamLifecycle.stop()
        stopDurationUpdates()
        unregisterReceiver()
    }

    private fun emitPhoneState(token: Long, receiver: PhoneStateReceiver) {
        streamLifecycle.emit(
            token,
            mapOf(
                "status" to receiver.status.name,
                "phoneNumber" to receiver.phoneNumber,
                "callDuration" to receiver.callDuration.toInt()
            )
        )
    }

    private fun createReceiver(token: Long): PhoneStateReceiver {
        return object : PhoneStateReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (!streamLifecycle.isActive(token)) {
                    return
                }

                super.onReceive(context, intent)

                if (!streamLifecycle.isActive(token)) {
                    return
                }

                if (status == PhoneStateStatus.CALL_STARTED) {
                    startDurationUpdates(token, this)
                } else if (status == PhoneStateStatus.CALL_ENDED) {
                    stopDurationUpdates()
                }
                emitPhoneState(token, this)
            }
        }
    }

    init {
        phoneStateEventChannel.setStreamHandler(object : EventChannel.StreamHandler {
            override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
                stopListening()

                val token = streamLifecycle.start(events)
                val receiver = createReceiver(token)

                val hasPhoneStatePermission = ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.READ_PHONE_STATE
                ) == PackageManager.PERMISSION_GRANTED

                val hasCallLogPermission = ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.READ_CALL_LOG
                ) == PackageManager.PERMISSION_GRANTED

                if (hasPhoneStatePermission && hasCallLogPermission) {
                    receiver.instance(applicationContext)
                    emitPhoneState(token, receiver)
                }

                applicationContext.registerReceiver(
                    receiver,
                    IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED)
                )
                streamLifecycle.registerReceiver(receiver)
            }

            override fun onCancel(arguments: Any?) {
                stopListening()
            }
        })
    }

    fun dispose() {
        stopListening()
        phoneStateEventChannel.setStreamHandler(null)
    }
}
