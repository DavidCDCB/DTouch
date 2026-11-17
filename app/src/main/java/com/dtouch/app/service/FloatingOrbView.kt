package com.dtouch.app.service

import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.ViewConfiguration
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatImageView
import com.dtouch.app.R
import com.dtouch.app.data.GestureType
import com.dtouch.app.data.OrbAnimation
import com.dtouch.app.data.OrbStyle
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Vista del orbe flotante.
 *
 * Máquina de estados táctil:
 *  - down: prepara el long press.
 *  - long press: entra en modo "mover"; los movimientos siguientes se
 *    informan al OverlayController para desplazar la ventana.
 *  - move antes del long press: cancela el long press y se evalúa como swipe.
 *  - up: si hubo long press, termina el movimiento; si fue swipe, ejecuta
 *    la acción del swipe; si fue toque, resuelve single/double tap.
 *
 * El long press siempre mueve el orbe; no es configurable.
 */
class FloatingOrbView(context: Context) : AppCompatImageView(context) {

    interface Listener {
        fun onGesture(type: GestureType)
        fun isDoubleTapEnabled(): Boolean
        fun swipeThresholdPx(): Int
        fun onMoveStart()
        fun onMoveBy(dx: Float, dy: Float)
        fun onMoveEnd()
    }

    var listener: Listener? = null

    private val handler = Handler(Looper.getMainLooper())
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private val longPressTimeoutMs = 350L
    private val doubleTapTimeoutMs = 280L

    private var downRawX = 0f
    private var downRawY = 0f
    private var lastRawX = 0f
    private var lastRawY = 0f
    private var longPressTriggered = false
    private var tapCount = 0
    private var singleTapRunnable: Runnable? = null

    private val longPressRunnable = Runnable {
        if (!longPressTriggered) {
            cancelPendingSingleTap()
            longPressTriggered = true
            performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            listener?.onMoveStart()
        }
    }

    init {
        setOrbImage(null)
        isClickable = true
        isFocusable = true
        contentDescription = context.getString(R.string.orb_content_description)
        setPadding(dp(4f), dp(4f), dp(4f), dp(4f))
    }

    /**
     * null = orbe del estilo seleccionado.
     * Un Bitmap = imagen PNG personalizada elegida por el usuario.
     */
    fun setOrbImage(bitmap: Bitmap?, style: OrbStyle = OrbStyle.DEFAULT) {
        if (bitmap != null) {
            setImageBitmap(bitmap)
        } else if (style.animation != OrbAnimation.NONE) {
            val base = AppCompatResources.getDrawable(context, style.previewDrawableRes)
            if (base != null) {
                setImageDrawable(
                    OrbAnimatedDrawable(base, style.animation, style.animationDurationMs)
                )
                startDrawableAnimation()
            } else {
                setImageResource(style.previewDrawableRes)
            }
        } else {
            setImageResource(style.previewDrawableRes)
        }
        scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startDrawableAnimation()
    }

    fun pauseAnimation() {
        stopDrawableAnimation()
    }

    fun resumeAnimation() {
        startDrawableAnimation()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downRawX = event.rawX
                downRawY = event.rawY
                lastRawX = event.rawX
                lastRawY = event.rawY
                longPressTriggered = false
                animate().scaleX(0.92f).scaleY(0.92f).setDuration(80L).start()
                handler.postDelayed(longPressRunnable, longPressTimeoutMs)
            }

            MotionEvent.ACTION_MOVE -> {
                if (longPressTriggered) {
                    listener?.onMoveBy(event.rawX - lastRawX, event.rawY - lastRawY)
                } else if (distance(event.rawX - downRawX, event.rawY - downRawY) > touchSlop) {
                    handler.removeCallbacks(longPressRunnable)
                }
                lastRawX = event.rawX
                lastRawY = event.rawY
            }

            MotionEvent.ACTION_UP -> {
                handler.removeCallbacks(longPressRunnable)
                animate().scaleX(1f).scaleY(1f).setDuration(80L).start()

                if (longPressTriggered) {
                    listener?.onMoveEnd()
                } else {
                    val dx = event.rawX - downRawX
                    val dy = event.rawY - downRawY
                    if (distance(dx, dy) > currentSwipeThresholdPx()) {
                        cancelPendingSingleTap()
                        listener?.onGesture(swipeType(dx, dy))
                    } else {
                        handleTap()
                    }
                }
                longPressTriggered = false
            }

            MotionEvent.ACTION_CANCEL -> {
                handler.removeCallbacks(longPressRunnable)
                animate().scaleX(1f).scaleY(1f).setDuration(80L).start()
                if (longPressTriggered) {
                    listener?.onMoveEnd()
                }
                longPressTriggered = false
            }
        }
        return true
    }

    override fun onDetachedFromWindow() {
        stopDrawableAnimation()
        handler.removeCallbacksAndMessages(null)
        singleTapRunnable = null
        tapCount = 0
        super.onDetachedFromWindow()
    }

    private fun handleTap() {
        if (listener?.isDoubleTapEnabled() != true) {
            listener?.onGesture(GestureType.SINGLE_TAP)
            return
        }

        tapCount++
        if (tapCount == 1) {
            val runnable = Runnable {
                tapCount = 0
                singleTapRunnable = null
                listener?.onGesture(GestureType.SINGLE_TAP)
            }
            singleTapRunnable = runnable
            handler.postDelayed(runnable, doubleTapTimeoutMs)
        } else {
            cancelPendingSingleTap()
            listener?.onGesture(GestureType.DOUBLE_TAP)
        }
    }

    private fun cancelPendingSingleTap() {
        singleTapRunnable?.let { handler.removeCallbacks(it) }
        singleTapRunnable = null
        tapCount = 0
    }

    private fun swipeType(dx: Float, dy: Float): GestureType = when {
        abs(dx) > abs(dy) && dx > 0f -> GestureType.SWIPE_RIGHT
        abs(dx) > abs(dy) -> GestureType.SWIPE_LEFT
        dy > 0f -> GestureType.SWIPE_DOWN
        else -> GestureType.SWIPE_UP
    }

    private fun distance(dx: Float, dy: Float): Float = sqrt(dx * dx + dy * dy)

    private fun currentSwipeThresholdPx(): Float =
        listener?.swipeThresholdPx()?.toFloat() ?: dp(56f).toFloat()

    private fun startDrawableAnimation() {
        (drawable as? OrbAnimatedDrawable)?.start()
    }

    private fun stopDrawableAnimation() {
        (drawable as? OrbAnimatedDrawable)?.stop()
    }

    private fun dp(value: Float): Int =
        (value * resources.displayMetrics.density + 0.5f).toInt()
}
