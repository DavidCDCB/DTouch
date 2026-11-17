package com.dtouch.app.data

/**
 * Valores por defecto de los gestos.
 *
 * Se mantienen los del J Touch original:
 *  - toque simple: Atrás
 *  - doble toque: Ninguna
 *  - swipe arriba: Apagar pantalla
 *  - swipe abajo: Panel de notificaciones
 *  - swipe izquierda: Atrás
 *  - swipe derecha: Aplicaciones recientes
 */
object GestureDefaults {
    fun actionFor(type: GestureType): GestureAction = when (type) {
        GestureType.SINGLE_TAP -> GestureAction.BACK
        GestureType.DOUBLE_TAP -> GestureAction.NONE
        GestureType.SWIPE_UP -> GestureAction.LOCK_SCREEN
        GestureType.SWIPE_DOWN -> GestureAction.NOTIFICATIONS
        GestureType.SWIPE_LEFT -> GestureAction.BACK
        GestureType.SWIPE_RIGHT -> GestureAction.RECENTS
    }
}
