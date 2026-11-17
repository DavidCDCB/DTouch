package com.dtouch.app.data

/**
 * Gestos que el orbe puede reconocer.
 *
 * El long press no está en esta lista a propósito: siempre se usa para mover el orbe.
 */
enum class GestureType {
    SINGLE_TAP,
    DOUBLE_TAP,
    SWIPE_UP,
    SWIPE_DOWN,
    SWIPE_LEFT,
    SWIPE_RIGHT
}
