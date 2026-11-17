package com.dtouch.app.data

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration

/**
 * Persistencia de DTouch.
 *
 * Se usa SharedPreferences a propósito: el servicio de accesibilidad y la
 * Activity viven en el mismo proceso, y las lecturas son instantáneas.
 * Cada gesto consulta su acción en el momento de ejecutarse, así que no
 * hace falta reiniciar el servicio cuando el usuario cambia una asignación.
 */
class SettingsRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun actionFor(type: GestureType): GestureAction {
        val raw = prefs.getString(keyFor(type), GestureDefaults.actionFor(type).value)
        return GestureAction.fromValue(raw)
    }

    fun setAction(type: GestureType, action: GestureAction) {
        prefs.edit().putString(keyFor(type), action.value).apply()
    }

    fun orbSizeDp(): Int =
        prefs.getInt(KEY_ORB_SIZE_DP, DEFAULT_ORB_SIZE_DP)
            .coerceIn(MIN_ORB_SIZE_DP, MAX_ORB_SIZE_DP)

    fun setOrbSizeDp(sizeDp: Int) {
        prefs.edit()
            .putInt(KEY_ORB_SIZE_DP, sizeDp.coerceIn(MIN_ORB_SIZE_DP, MAX_ORB_SIZE_DP))
            .apply()
    }

    fun orbAlpha(): Int =
        prefs.getInt(KEY_ORB_ALPHA, DEFAULT_ORB_ALPHA)
            .coerceIn(MIN_ORB_ALPHA, MAX_ORB_ALPHA)

    fun setOrbAlpha(alpha: Int) {
        prefs.edit()
            .putInt(KEY_ORB_ALPHA, alpha.coerceIn(MIN_ORB_ALPHA, MAX_ORB_ALPHA))
            .apply()
    }

    fun orbStyle(): OrbStyle =
        OrbStyle.fromId(prefs.getString(KEY_ORB_STYLE, OrbStyle.DEFAULT.id))

    fun setOrbStyle(style: OrbStyle) {
        prefs.edit().putString(KEY_ORB_STYLE, style.id).apply()
    }

    fun gestureSensitivity(): Int =
        prefs.getInt(KEY_GESTURE_SENSITIVITY, DEFAULT_GESTURE_SENSITIVITY)
            .coerceIn(0, 100)

    fun setGestureSensitivity(value: Int) {
        prefs.edit()
            .putInt(KEY_GESTURE_SENSITIVITY, value.coerceIn(0, 100))
            .apply()
    }

    fun swipeThresholdDp(): Int = swipeThresholdDpFor(gestureSensitivity())

    fun orbX(orientation: Int): Int? = readInt(positionKeyX(orientation))

    fun orbY(orientation: Int): Int? = readInt(positionKeyY(orientation))

    fun saveOrbPosition(orientation: Int, x: Int, y: Int) {
        prefs.edit()
            .putInt(positionKeyX(orientation), x)
            .putInt(positionKeyY(orientation), y)
            .apply()
    }

    fun resetOrbPosition() {
        prefs.edit()
            .remove(KEY_X_PORT)
            .remove(KEY_Y_PORT)
            .remove(KEY_X_LAND)
            .remove(KEY_Y_LAND)
            .apply()
    }

    private fun readInt(key: String): Int? =
        if (prefs.contains(key)) prefs.getInt(key, 0) else null

    companion object {
        const val PREFS_NAME = "dtouch_settings"

        const val MIN_ORB_SIZE_DP = 32
        const val MAX_ORB_SIZE_DP = 80
        const val DEFAULT_ORB_SIZE_DP = 56

        const val MIN_ORB_ALPHA = 0
        const val MAX_ORB_ALPHA = 255
        const val DEFAULT_ORB_ALPHA = 255

        const val MIN_SWIPE_DP = 32
        const val MAX_SWIPE_DP = 80
        const val DEFAULT_GESTURE_SENSITIVITY = 50

        private const val KEY_ORB_SIZE_DP = "orb_size_dp"
        private const val KEY_ORB_ALPHA = "orb_alpha"
        private const val KEY_ORB_STYLE = "orb_style"
        private const val KEY_GESTURE_SENSITIVITY = "gesture_sensitivity"

        private const val KEY_X_PORT = "orb_x_port"
        private const val KEY_Y_PORT = "orb_y_port"
        private const val KEY_X_LAND = "orb_x_land"
        private const val KEY_Y_LAND = "orb_y_land"

        fun keyFor(type: GestureType): String = when (type) {
            GestureType.SINGLE_TAP -> "gesture_single_tap"
            GestureType.DOUBLE_TAP -> "gesture_double_tap"
            GestureType.SWIPE_UP -> "gesture_swipe_up"
            GestureType.SWIPE_DOWN -> "gesture_swipe_down"
            GestureType.SWIPE_LEFT -> "gesture_swipe_left"
            GestureType.SWIPE_RIGHT -> "gesture_swipe_right"
        }

        /**
         * 0 % = umbral máximo (80 dp, menos sensible); 100 % = umbral mínimo
         * (32 dp, más sensible). 50 % conserva el valor histórico de 56 dp.
         */
        fun swipeThresholdDpFor(sensitivity: Int): Int {
            val clamped = sensitivity.coerceIn(0, 100)
            return MAX_SWIPE_DP -
                ((MAX_SWIPE_DP - MIN_SWIPE_DP) * clamped / 100)
        }

        private fun isLandscape(orientation: Int): Boolean =
            orientation == Configuration.ORIENTATION_LANDSCAPE

        private fun positionKeyX(orientation: Int): String =
            if (isLandscape(orientation)) KEY_X_LAND else KEY_X_PORT

        private fun positionKeyY(orientation: Int): String =
            if (isLandscape(orientation)) KEY_Y_LAND else KEY_Y_PORT
    }
}
