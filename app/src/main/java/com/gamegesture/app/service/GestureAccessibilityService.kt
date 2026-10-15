package com.gamegesture.app.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.gamegesture.app.data.model.GestureSequence
import com.gamegesture.app.gesture.DispatchStep
import com.gamegesture.app.gesture.GestureEngine
import com.gamegesture.app.utils.ScreenUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.UUID
import kotlin.coroutines.resume

data class GestureExecutionLog(
    val id: String = UUID.randomUUID().toString(),
    val gestureName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val stepCount: Int,
    val durationMs: Long,
    val success: Boolean,
    val message: String
)

class GestureAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "GameGestureService"
        var instance: GestureAccessibilityService? = null
            private set

        private val _isServiceConnected = MutableStateFlow(false)
        val isServiceConnected: StateFlow<Boolean> = _isServiceConnected.asStateFlow()

        private val _isExecuting = MutableStateFlow(false)
        val isExecuting: StateFlow<Boolean> = _isExecuting.asStateFlow()

        private val _debugLogs = MutableStateFlow<List<GestureExecutionLog>>(emptyList())
        val debugLogs: StateFlow<List<GestureExecutionLog>> = _debugLogs.asStateFlow()

        fun logDebugEvent(log: GestureExecutionLog) {
            val current = _debugLogs.value.toMutableList()
            current.add(0, log)
            if (current.size > 50) current.removeAt(current.size - 1)
            _debugLogs.value = current
        }

        fun clearDebugLogs() {
            _debugLogs.value = emptyList()
        }
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        _isServiceConnected.value = true
        Log.i(TAG, "GestureAccessibilityService connected successfully")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Not consuming window events to preserve absolute zero latency
    }

    override fun onInterrupt() {
        Log.w(TAG, "GestureAccessibilityService interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        _isServiceConnected.value = false
        Log.i(TAG, "GestureAccessibilityService destroyed")
    }

    fun executeSequence(
        sequence: GestureSequence,
        triggerHaptic: Boolean = true,
        onResult: ((Boolean, String) -> Unit)? = null
    ) {
        if (_isExecuting.value) {
            val msg = "Execution skipped: another gesture sequence is currently running"
            Log.w(TAG, msg)
            onResult?.invoke(false, msg)
            return
        }

        serviceScope.launch {
            _isExecuting.value = true
            val startTime = System.currentTimeMillis()

            if (triggerHaptic) {
                performHapticTick()
            }

            try {
                val metrics = ScreenUtils.getScreenMetrics(this@GestureAccessibilityService)
                val screenW = metrics.widthPixels
                val screenH = metrics.heightPixels

                val steps = GestureEngine.compileSequenceSteps(sequence, screenW, screenH)

                var allSuccess = true
                var executedSteps = 0

                for (step in steps) {
                    when (step) {
                        is DispatchStep.DelayOnlyStep -> {
                            delay(step.delayMs)
                        }

                        is DispatchStep.StrokeStep -> {
                            if (step.delayBeforeMs > 0) {
                                delay(step.delayBeforeMs)
                            }

                            val gestureDesc = GestureEngine.buildGestureDescription(step)
                            val stepSuccess = dispatchGestureAsync(gestureDesc)

                            if (stepSuccess) {
                                executedSteps++
                            } else {
                                allSuccess = false
                                Log.e(TAG, "Step failed: ${step.label}")
                                break
                            }
                        }
                    }
                }

                val totalTime = System.currentTimeMillis() - startTime
                val log = GestureExecutionLog(
                    gestureName = sequence.name,
                    stepCount = executedSteps,
                    durationMs = totalTime,
                    success = allSuccess,
                    message = if (allSuccess) "Executed $executedSteps steps successfully" else "Failed at step $executedSteps"
                )
                logDebugEvent(log)
                onResult?.invoke(allSuccess, log.message)

            } catch (e: Exception) {
                Log.e(TAG, "Error executing sequence", e)
                val totalTime = System.currentTimeMillis() - startTime
                val log = GestureExecutionLog(
                    gestureName = sequence.name,
                    stepCount = 0,
                    durationMs = totalTime,
                    success = false,
                    message = "Exception: ${e.localizedMessage}"
                )
                logDebugEvent(log)
                onResult?.invoke(false, e.localizedMessage ?: "Unknown error")
            } finally {
                _isExecuting.value = false
            }
        }
    }

    private suspend fun dispatchGestureAsync(description: GestureDescription): Boolean {
        return suspendCancellableCoroutine { continuation ->
            val callback = object : GestureResultCallback() {
                override fun onCompleted(gestureDescription: GestureDescription?) {
                    if (continuation.isActive) continuation.resume(true)
                }

                override fun onCancelled(gestureDescription: GestureDescription?) {
                    if (continuation.isActive) continuation.resume(false)
                }
            }

            val dispatched = dispatchGesture(description, callback, null)
            if (!dispatched) {
                if (continuation.isActive) continuation.resume(false)
            }
        }
    }

    private fun performHapticTick() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                vibrator?.vibrate(30L)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
