package com.gamegesture.app

import com.gamegesture.app.data.model.ActionType
import com.gamegesture.app.data.model.GestureAction
import com.gamegesture.app.data.model.GestureSequence
import com.gamegesture.app.data.model.NormalizedPoint
import com.gamegesture.app.data.repository.GestureRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import java.util.UUID

class GesturePersistenceTest {

    private lateinit var tempDir: File
    private lateinit var repository: GestureRepository

    @Before
    fun setUp() {
        tempDir = File.createTempFile("gesture_test", "").apply {
            delete()
            mkdir()
        }
        repository = GestureRepository(storageDir = tempDir)
        runBlocking {
            repository.reloadGestures()
        }
    }

    @After
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun testDefaultPresetsCreatedOnInitialRun() = runBlocking {
        val list = repository.reloadGestures()
        assertTrue("Presets should be populated", list.isNotEmpty())
        assertEquals(4, list.size)

        val names = list.map { it.name }
        assertTrue(names.any { it.contains("360°") })
        assertTrue(names.any { it.contains("Flick Turn") })
    }

    @Test
    fun testSaveAndRetrieveCustomGesture() = runBlocking {
        repository.reloadGestures()

        val gestureId = UUID.randomUUID().toString()
        val customGesture = GestureSequence(
            id = gestureId,
            name = "Test Tactical Combo",
            description = "Custom macro combo for testing",
            actions = listOf(
                GestureAction.TapAction(0.85f, 0.70f, delayMs = 10L),
                GestureAction.LongPressAction(0.50f, 0.50f, durationMs = 350L),
                GestureAction.SwipeAction(0.80f, 0.50f, 0.20f, 0.50f, durationMs = 160L),
                GestureAction.CircularAction(centerX = 0.6f, centerY = 0.6f, radius = 0.2f),
                GestureAction.FreeformAction(
                    points = listOf(
                        NormalizedPoint(0.1f, 0.1f, 0L),
                        NormalizedPoint(0.2f, 0.3f, 50L),
                        NormalizedPoint(0.4f, 0.5f, 100L)
                    ),
                    durationMs = 200L
                ),
                GestureAction.DelayAction(delayMs = 120L)
            ),
            repeatCount = 3,
            delayBetweenRepeatsMs = 60L
        )

        repository.saveGesture(customGesture)

        val retrieved = repository.getGestureById(gestureId)
        assertNotNull("Saved gesture should be retrievable", retrieved)
        assertEquals("Test Tactical Combo", retrieved!!.name)
        assertEquals(6, retrieved.actions.size)
        assertEquals(3, retrieved.repeatCount)
        assertEquals(ActionType.TAP, retrieved.actions[0].type)
        assertEquals(ActionType.LONG_PRESS, retrieved.actions[1].type)
        assertEquals(ActionType.SWIPE, retrieved.actions[2].type)
        assertEquals(ActionType.CIRCULAR, retrieved.actions[3].type)
        assertEquals(ActionType.FREEFORM, retrieved.actions[4].type)
        assertEquals(ActionType.DELAY, retrieved.actions[5].type)
    }

    @Test
    fun testEditAndRenameGesture() = runBlocking {
        repository.reloadGestures()

        val id = UUID.randomUUID().toString()
        val original = GestureSequence(
            id = id,
            name = "Initial Name",
            actions = listOf(GestureAction.TapAction(0.5f, 0.5f))
        )
        repository.saveGesture(original)

        // Rename and update actions
        val updated = original.copy(
            name = "Renamed Power Strike",
            actions = listOf(
                GestureAction.TapAction(0.5f, 0.5f),
                GestureAction.TapAction(0.6f, 0.6f)
            )
        )
        repository.saveGesture(updated)

        val reloaded = repository.getGestureById(id)
        assertNotNull(reloaded)
        assertEquals("Renamed Power Strike", reloaded!!.name)
        assertEquals(2, reloaded.actions.size)
    }

    @Test
    fun testDeleteGesture() = runBlocking {
        repository.reloadGestures()

        val id = UUID.randomUUID().toString()
        val gesture = GestureSequence(
            id = id,
            name = "Temporary Gesture",
            actions = listOf(GestureAction.TapAction(0.1f, 0.1f))
        )
        repository.saveGesture(gesture)
        assertNotNull(repository.getGestureById(id))

        repository.deleteGesture(id)
        assertNull("Deleted gesture should no longer exist", repository.getGestureById(id))
    }

    @Test
    fun testPersistenceAcrossRepositoryInstances() = runBlocking {
        val id = UUID.randomUUID().toString()
        val gesture = GestureSequence(
            id = id,
            name = "Persistent Across Restarts",
            actions = listOf(GestureAction.SwipeAction(0.2f, 0.2f, 0.8f, 0.8f, 150L))
        )
        repository.saveGesture(gesture)

        // Create a new repository instance pointing to the same folder
        val newRepo = GestureRepository(storageDir = tempDir)
        val loaded = newRepo.reloadGestures()

        val found = loaded.find { it.id == id }
        assertNotNull("New repository instance should load previously persisted gestures", found)
        assertEquals("Persistent Across Restarts", found!!.name)
    }
}
