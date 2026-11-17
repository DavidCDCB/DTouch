package com.dtouch.app.data

import org.junit.Assert.assertEquals
import org.junit.Test

class GestureActionTest {

    @Test
    fun fromValueReturnsMatchingAction() {
        assertEquals(GestureAction.BACK, GestureAction.fromValue("back"))
        assertEquals(GestureAction.HOME, GestureAction.fromValue("home"))
        assertEquals(GestureAction.RECENTS, GestureAction.fromValue("recents"))
        assertEquals(GestureAction.NOTIFICATIONS, GestureAction.fromValue("notifications"))
        assertEquals(GestureAction.LOCK_SCREEN, GestureAction.fromValue("lock_screen"))
    }

    @Test
    fun fromValueFallsBackToNone() {
        assertEquals(GestureAction.NONE, GestureAction.fromValue(null))
        assertEquals(GestureAction.NONE, GestureAction.fromValue(""))
        assertEquals(GestureAction.NONE, GestureAction.fromValue("unknown"))
    }

    @Test
    fun actionValuesAreUnique() {
        val values = GestureAction.entries.map { it.value }
        assertEquals(values.size, values.toSet().size)
    }

    @Test
    fun gestureKeysAreUnique() {
        val keys = GestureType.entries.map { SettingsRepository.keyFor(it) }
        assertEquals(keys.size, keys.toSet().size)
    }

    @Test
    fun defaultsMatchTheOriginalBehaviour() {
        assertEquals(GestureAction.BACK, GestureDefaults.actionFor(GestureType.SINGLE_TAP))
        assertEquals(GestureAction.NONE, GestureDefaults.actionFor(GestureType.DOUBLE_TAP))
        assertEquals(GestureAction.LOCK_SCREEN, GestureDefaults.actionFor(GestureType.SWIPE_UP))
        assertEquals(GestureAction.NOTIFICATIONS, GestureDefaults.actionFor(GestureType.SWIPE_DOWN))
        assertEquals(GestureAction.BACK, GestureDefaults.actionFor(GestureType.SWIPE_LEFT))
        assertEquals(GestureAction.RECENTS, GestureDefaults.actionFor(GestureType.SWIPE_RIGHT))
    }

    @Test
    fun orbAppearanceDefaultsAreStable() {
        assertEquals(32, SettingsRepository.MIN_ORB_SIZE_DP)
        assertEquals(80, SettingsRepository.MAX_ORB_SIZE_DP)
        assertEquals(56, SettingsRepository.DEFAULT_ORB_SIZE_DP)

        assertEquals(0, SettingsRepository.MIN_ORB_ALPHA)
        assertEquals(255, SettingsRepository.MAX_ORB_ALPHA)
        assertEquals(255, SettingsRepository.DEFAULT_ORB_ALPHA)
    }

    @Test
    fun gestureSensitivityMapsToSwipeThreshold() {
        assertEquals(80, SettingsRepository.swipeThresholdDpFor(0))
        assertEquals(56, SettingsRepository.swipeThresholdDpFor(50))
        assertEquals(32, SettingsRepository.swipeThresholdDpFor(100))
        assertEquals(80, SettingsRepository.swipeThresholdDpFor(-10))
        assertEquals(32, SettingsRepository.swipeThresholdDpFor(150))
        assertEquals(50, SettingsRepository.DEFAULT_GESTURE_SENSITIVITY)
    }
}
