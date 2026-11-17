package com.dtouch.app.service

import android.app.KeyguardManager
import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Point
import android.os.Build
import android.os.PowerManager
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import com.dtouch.app.R
import com.dtouch.app.data.GestureAction
import com.dtouch.app.data.GestureType
import com.dtouch.app.data.OrbImageStore
import com.dtouch.app.data.SettingsRepository
import kotlin.math.roundToInt

/**
 * Dueño de la ventana del orbe.
 *
 * Usa TYPE_ACCESSIBILITY_OVERLAY: al ser una ventana de un servicio de
 * accesibilidad, no requiere SYSTEM_ALERT_WINDOW. La ventana solo existe
 * mientras el servicio está conectado.
 */
class OverlayController(
    private val service: DTouchAccessibilityService,
    private val settings: SettingsRepository,
    private val orbImageStore: OrbImageStore
) : FloatingOrbView.Listener {

    private val windowManager =
        service.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    private var orbView: FloatingOrbView? = null
    private var params: WindowManager.LayoutParams? = null
    private var orientation = service.resources.configuration.orientation
    private var screenWidth = 0
    private var screenHeight = 0
    private var moving = false

    fun show() {
        if (orbView != null) return

        refreshScreenSize()
        val size = orbSizePx()
        val defaultX = screenWidth - size - screenMargin()
        val defaultY = (screenHeight - size) / 2
        val x = settings.orbX(orientation) ?: defaultX
        val y = settings.orbY(orientation) ?: defaultY

        val view = FloatingOrbView(service).apply {
            listener = this@OverlayController
            imageAlpha = settings.orbAlpha()
            setOrbImage(orbImageStore.loadBitmap(), settings.orbStyle())
        }

        val layoutParams = WindowManager.LayoutParams(
            size,
            size,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            this.x = clampX(x, size)
            this.y = clampY(y, size)
        }

        try {
            windowManager.addView(view, layoutParams)
            orbView = view
            params = layoutParams
            syncAnimationWithScreen()
        } catch (e: Exception) {
            Log.e(TAG, "No se pudo agregar el orbe", e)
            service.toast(R.string.error_overlay)
        }
    }

    /**
     * Las animaciones del orbe solo deben correr con la pantalla encendida y
     * desbloqueada. Con la pantalla apagada el AVD sigue generando frames y
     * Android puede matar el proceso por CPU (ScreenOffCPUCheckKill).
     */
    fun syncAnimationWithScreen() {
        val powerManager = service.getSystemService(Context.POWER_SERVICE) as PowerManager
        val keyguardManager = service.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        if (powerManager.isInteractive && !keyguardManager.isKeyguardLocked) {
            orbView?.resumeAnimation()
        } else {
            orbView?.pauseAnimation()
        }
    }

    fun hide() {
        val view = orbView ?: return
        runCatching { windowManager.removeViewImmediate(view) }
            .onFailure { Log.w(TAG, "No se pudo quitar el orbe", it) }
        orbView = null
        params = null
        moving = false
    }

    fun onConfigurationChanged() {
        val newOrientation = service.resources.configuration.orientation
        if (newOrientation == orientation) return

        saveCurrentPosition()
        orientation = newOrientation
        refreshScreenSize()

        val size = orbSizePx()
        val defaultX = screenWidth - size - screenMargin()
        val defaultY = (screenHeight - size) / 2
        val x = settings.orbX(orientation) ?: defaultX
        val y = settings.orbY(orientation) ?: defaultY
        applyPosition(clampX(x, size), clampY(y, size))
    }

    fun resetPosition() {
        settings.resetOrbPosition()
        refreshScreenSize()
        val size = orbSizePx()
        applyPosition(
            clampX(screenWidth - size - screenMargin(), size),
            clampY((screenHeight - size) / 2, size)
        )
    }

    /**
     * Recarga la imagen personalizada (o el orbe por defecto) sin recrear
     * la ventana. Se llama cuando el usuario elige o quita una imagen.
     */
    fun applyOrbImage() {
        orbView?.setOrbImage(orbImageStore.loadBitmap(), settings.orbStyle())
        syncAnimationWithScreen()
    }

    /**
     * Aplica tamaño y opacidad sin recrear la ventana.
     *
     * Al cambiar el tamaño se conserva el centro del orbe y se vuelve a
     * limitar a los bordes, para que no salte ni se salga de la pantalla.
     */
    fun applyAppearance() {
        val view = orbView ?: return
        val layoutParams = params ?: return

        view.imageAlpha = settings.orbAlpha()

        val newSize = orbSizePx()
        if (layoutParams.width == newSize && layoutParams.height == newSize) {
            return
        }

        val centerX = layoutParams.x + layoutParams.width / 2
        val centerY = layoutParams.y + layoutParams.height / 2

        layoutParams.width = newSize
        layoutParams.height = newSize
        layoutParams.x = clampX(centerX - newSize / 2, newSize)
        layoutParams.y = clampY(centerY - newSize / 2, newSize)
        updateView(layoutParams)
        saveCurrentPosition()
    }

    override fun onGesture(type: GestureType) {
        service.dispatchAction(settings.actionFor(type))
    }

    override fun isDoubleTapEnabled(): Boolean =
        settings.actionFor(GestureType.DOUBLE_TAP) != GestureAction.NONE

    override fun swipeThresholdPx(): Int = dpToPx(settings.swipeThresholdDp())

    override fun onMoveStart() {
        moving = true
    }

    override fun onMoveBy(dx: Float, dy: Float) {
        if (!moving) return
        val layoutParams = params ?: return
        layoutParams.x = clampX(layoutParams.x + dx.roundToInt(), layoutParams.width)
        layoutParams.y = clampY(layoutParams.y + dy.roundToInt(), layoutParams.height)
        updateView(layoutParams)
    }

    override fun onMoveEnd() {
        if (!moving) return
        moving = false
        saveCurrentPosition()
    }

    private fun saveCurrentPosition() {
        val layoutParams = params ?: return
        settings.saveOrbPosition(orientation, layoutParams.x, layoutParams.y)
    }

    private fun applyPosition(x: Int, y: Int) {
        val layoutParams = params ?: return
        layoutParams.x = x
        layoutParams.y = y
        updateView(layoutParams)
    }

    private fun updateView(layoutParams: WindowManager.LayoutParams) {
        val view = orbView ?: return
        runCatching { windowManager.updateViewLayout(view, layoutParams) }
            .onFailure { Log.w(TAG, "No se pudo mover el orbe", it) }
    }

    @Suppress("DEPRECATION")
    private fun refreshScreenSize() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val bounds = windowManager.currentWindowMetrics.bounds
            screenWidth = bounds.width()
            screenHeight = bounds.height()
        } else {
            val point = Point()
            windowManager.defaultDisplay.getRealSize(point)
            screenWidth = point.x
            screenHeight = point.y
        }
    }

    private fun orbSizePx(): Int = dpToPx(settings.orbSizeDp())

    private fun screenMargin(): Int =
        service.resources.getDimensionPixelSize(R.dimen.screen_margin)

    private fun dpToPx(dp: Int): Int =
        (dp * service.resources.displayMetrics.density + 0.5f).toInt()

    private fun clampX(x: Int, size: Int): Int =
        x.coerceIn(0, (screenWidth - size).coerceAtLeast(0))

    private fun clampY(y: Int, size: Int): Int =
        y.coerceIn(0, (screenHeight - size).coerceAtLeast(0))

    companion object {
        private const val TAG = "DTouchOverlay"
    }
}
