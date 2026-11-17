package com.dtouch.app.service

import android.animation.ValueAnimator
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import com.dtouch.app.data.OrbAnimation
import kotlin.math.abs

/**
 * Drawable que anima una base vectorial estática con un Handler a 12,5 fps.
 *
 * Los AnimatedVectorDrawable nativos corren a 60 fps e invalidaban la ventana
 * del overlay continuamente; en un POCO/Android 16 eso llegó a consumir ~43 %
 * de CPU y el sistema mató el proceso con ScreenOffCPUCheckKill. Una rotación
 * lenta no necesita 60 fps: 80 ms por frame es suficiente y baja el consumo
 * sin cambiar el aspecto.
 */
class OrbAnimatedDrawable(
    private val base: Drawable,
    private val animation: OrbAnimation,
    private val durationMs: Long
) : Drawable() {

    private val handler = Handler(Looper.getMainLooper())
    private var running = false
    private var startTime = 0L

    private val frame = object : Runnable {
        override fun run() {
            if (!running) return
            invalidateSelf()
            handler.postDelayed(this, FRAME_DELAY_MS)
        }
    }

    fun start() {
        if (running || animation == OrbAnimation.NONE) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            !ValueAnimator.areAnimatorsEnabled()
        ) {
            return
        }
        running = true
        startTime = SystemClock.uptimeMillis()
        handler.removeCallbacks(frame)
        handler.post(frame)
    }

    fun stop() {
        running = false
        handler.removeCallbacks(frame)
    }

    override fun draw(canvas: Canvas) {
        val bounds = bounds
        if (bounds.isEmpty || durationMs <= 0L) {
            base.setBounds(bounds)
            base.draw(canvas)
            return
        }

        val elapsed = ((SystemClock.uptimeMillis() - startTime) % durationMs)
            .toFloat() / durationMs
        val centerX = bounds.exactCenterX()
        val centerY = bounds.exactCenterY()
        val save = canvas.save()

        when (animation) {
            OrbAnimation.ROTATE -> canvas.rotate(elapsed * 360f, centerX, centerY)
            OrbAnimation.PULSE -> {
                val scale = 0.78f + 0.22f * (1f - abs(2f * elapsed - 1f))
                canvas.scale(scale, scale, centerX, centerY)
            }

            OrbAnimation.NONE -> Unit
        }

        base.setBounds(bounds)
        base.draw(canvas)
        canvas.restoreToCount(save)
    }

    override fun setVisible(visible: Boolean, restart: Boolean): Boolean {
        val changed = super.setVisible(visible, restart)
        if (visible) start() else stop()
        return changed
    }

    override fun onBoundsChange(bounds: Rect) {
        base.setBounds(bounds)
    }

    override fun setAlpha(alpha: Int) {
        base.alpha = alpha
        invalidateSelf()
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        base.colorFilter = colorFilter
        invalidateSelf()
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    override fun getIntrinsicWidth(): Int = base.intrinsicWidth

    override fun getIntrinsicHeight(): Int = base.intrinsicHeight

    companion object {
        private const val FRAME_DELAY_MS = 80L
    }
}
