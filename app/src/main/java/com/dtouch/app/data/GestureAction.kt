package com.dtouch.app.data

import androidx.annotation.StringRes
import com.dtouch.app.R

/**
 * Acciones que puede ejecutar DTouch.
 *
 * No hay acciones root, capturas ni paneles rápidos: el alcance de esta
 * reconstrucción es solamente el orbe y las acciones de navegación/bloqueo.
 */
enum class GestureAction(
    val value: String,
    @StringRes val labelRes: Int
) {
    NONE("none", R.string.action_none),
    LOCK_SCREEN("lock_screen", R.string.action_lock_screen),
    BACK("back", R.string.action_back),
    HOME("home", R.string.action_home),
    RECENTS("recents", R.string.action_recents),
    NOTIFICATIONS("notifications", R.string.action_notifications);

    companion object {
        fun fromValue(value: String?): GestureAction =
            entries.firstOrNull { it.value == value } ?: NONE
    }
}
