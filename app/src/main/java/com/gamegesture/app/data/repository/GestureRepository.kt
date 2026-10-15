package com.gamegesture.app.data.repository

import android.content.Context
import com.gamegesture.app.data.model.ActionType
import com.gamegesture.app.data.model.GestureAction
import com.gamegesture.app.data.model.GestureSequence
import com.gamegesture.app.data.model.NormalizedPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class GestureRepository(
    private val context: Context? = null,
    storageDir: File? = null
) {

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        encodeDefaults = true
        classDiscriminator = "actionClass"
    }

    private val gesturesFile = File(storageDir ?: context?.filesDir ?: File("."), "saved_gestures.json")
    private val _gestures = MutableStateFlow<List<GestureSequence>>(emptyList())
    val gestures: StateFlow<List<GestureSequence>> = _gestures.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.IO)
    private val fileMutex = Mutex()

    init {
        loadGestures()
    }

    fun loadGestures() {
        scope.launch {
            reloadGestures()
        }
    }

    suspend fun reloadGestures(): List<GestureSequence> = withContext(Dispatchers.IO) {
        fileMutex.withLock {
            val list = if (gesturesFile.exists()) {
                try {
                    val text = gesturesFile.readText()
                    json.decodeFromString<List<GestureSequence>>(text)
                } catch (e: Exception) {
                    e.printStackTrace()
                    createDefaultGestures().also { persistList(it) }
                }
            } else {
                createDefaultGestures().also { defaults ->
                    persistList(defaults)
                }
            }
            _gestures.value = list
            list
        }
    }

    suspend fun saveGesture(gesture: GestureSequence) = withContext(Dispatchers.IO) {
        fileMutex.withLock {
            val current = _gestures.value.toMutableList()
            val index = current.indexOfFirst { it.id == gesture.id }
            if (index >= 0) {
                current[index] = gesture.copy(updatedAt = System.currentTimeMillis())
            } else {
                current.add(0, gesture)
            }
            persistList(current)
            _gestures.value = current
        }
    }

    suspend fun deleteGesture(id: String) = withContext(Dispatchers.IO) {
        fileMutex.withLock {
            val current = _gestures.value.filterNot { it.id == id }
            persistList(current)
            _gestures.value = current
        }
    }

    fun getGestureById(id: String): GestureSequence? {
        return _gestures.value.find { it.id == id }
    }

    private fun persistList(list: List<GestureSequence>) {
        try {
            val encoded = json.encodeToString(list)
            gesturesFile.writeText(encoded)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createDefaultGestures(): List<GestureSequence> {
        val now = System.currentTimeMillis()

        // 1. 360° Movement (Rotational circle)
        val circular360 = GestureSequence(
            id = "preset_360_movement",
            name = "360° Rotational Camera",
            description = "Rapid 360-degree sweep across aim area for swift situational awareness.",
            actions = listOf(
                GestureAction.CircularAction(
                    centerX = 0.65f,
                    centerY = 0.55f,
                    radius = 0.22f,
                    startAngleDeg = 0f,
                    sweepAngleDeg = 360f,
                    isClockwise = true,
                    durationMs = 260L,
                    pointCount = 36
                )
            ),
            repeatCount = 1,
            isCalibrated = true,
            createdAt = now,
            updatedAt = now
        )

        // 2. Quick Swipe (Quick flick/turn)
        val quickSwipe = GestureSequence(
            id = "preset_quick_swipe",
            name = "Quick 180° Flick Turn",
            description = "Fast horizontal drag from right edge to center for rapid direction turnaround.",
            actions = listOf(
                GestureAction.SwipeAction(
                    startX = 0.82f,
                    startY = 0.50f,
                    endX = 0.28f,
                    endY = 0.50f,
                    durationMs = 120L
                )
            ),
            repeatCount = 1,
            isCalibrated = true,
            createdAt = now,
            updatedAt = now
        )

        // 3. Multi-Step Tactical Action (Tap Utility -> Quick Aim Flick -> Placement Tap)
        val tacticalAction = GestureSequence(
            id = "preset_tactical_deploy",
            name = "Tactical Utility Fast-Drop",
            description = "Select utility at bottom-right, swipe camera to ground angle, and tap place.",
            actions = listOf(
                GestureAction.TapAction(x = 0.85f, y = 0.75f, delayMs = 0L),
                GestureAction.DelayAction(delayMs = 60L),
                GestureAction.SwipeAction(
                    startX = 0.65f,
                    startY = 0.50f,
                    endX = 0.65f,
                    endY = 0.72f,
                    durationMs = 140L,
                    delayMs = 0L
                ),
                GestureAction.DelayAction(delayMs = 40L),
                GestureAction.TapAction(x = 0.70f, y = 0.68f, delayMs = 0L)
            ),
            repeatCount = 1,
            isCalibrated = true,
            createdAt = now,
            updatedAt = now
        )

        // 4. Double Quick Tap (Burst Trigger)
        val doubleTap = GestureSequence(
            id = "preset_double_tap",
            name = "Rapid Double Tap",
            description = "Two instant consecutive taps with 40ms interval.",
            actions = listOf(
                GestureAction.TapAction(x = 0.80f, y = 0.60f, delayMs = 0L),
                GestureAction.DelayAction(delayMs = 40L),
                GestureAction.TapAction(x = 0.80f, y = 0.60f, delayMs = 0L)
            ),
            repeatCount = 1,
            isCalibrated = true,
            createdAt = now,
            updatedAt = now
        )

        return listOf(circular360, quickSwipe, tacticalAction, doubleTap)
    }
}
