package com.dtouch.app.service

import android.accessibilityservice.AccessibilityService
import android.app.admin.DevicePolicyManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.dtouch.app.R
import com.dtouch.app.data.GestureAction
import com.dtouch.app.data.OrbImageStore
import com.dtouch.app.data.SettingsRepository

/**
 * Servicio principal de DTouch.
 *
 * Responsabilidades:
 *  - Crear y quitar el orbe flotante.
 *  - Ejecutar acciones globales de accesibilidad (atrás, inicio, recientes,
 *    notificaciones y bloqueo en Android 9+).
 *  - Usar DeviceAdmin como fallback de bloqueo en Android 6-8.
 */
class DTouchAccessibilityService : AccessibilityService() {

    private lateinit var settings: SettingsRepository
    private lateinit var orbImageStore: OrbImageStore
    private var overlayController: OverlayController? = null

    private val commandReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                ACTION_RESET_POSITION -> overlayController?.resetPosition()
                ACTION_ORB_APPEARANCE_CHANGED -> overlayController?.applyAppearance()
                ACTION_ORB_IMAGE_CHANGED -> overlayController?.applyOrbImage()
                Intent.ACTION_SCREEN_OFF,
                Intent.ACTION_SCREEN_ON,
                Intent.ACTION_USER_PRESENT -> overlayController?.syncAnimationWithScreen()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        settings = SettingsRepository(this)
        orbImageStore = OrbImageStore(this)
        val filter = IntentFilter().apply {
            addAction(ACTION_RESET_POSITION)
            addAction(ACTION_ORB_APPEARANCE_CHANGED)
            addAction(ACTION_ORB_IMAGE_CHANGED)
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_USER_PRESENT)
        }
        ContextCompat.registerReceiver(
            this,
            commandReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        overlayController?.hide()
        overlayController = OverlayController(this, settings, orbImageStore).also { it.show() }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // DTouch no necesita leer eventos ni contenido de la pantalla.
    }

    override fun onInterrupt() {
        // No hay nada que interrumpir.
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        overlayController?.onConfigurationChanged()
    }

    override fun onDestroy() {
        overlayController?.hide()
        overlayController = null
        runCatching { unregisterReceiver(commandReceiver) }
        super.onDestroy()
    }

    fun dispatchAction(action: GestureAction) {
        when (action) {
            GestureAction.NONE -> Unit
            GestureAction.BACK -> performAction(GLOBAL_ACTION_BACK)
            GestureAction.HOME -> performAction(GLOBAL_ACTION_HOME)
            GestureAction.RECENTS -> performAction(GLOBAL_ACTION_RECENTS)
            GestureAction.NOTIFICATIONS -> performAction(GLOBAL_ACTION_NOTIFICATIONS)
            GestureAction.LOCK_SCREEN -> lockScreen()
        }
    }

    private fun performAction(action: Int) {
        if (!performGlobalAction(action)) {
            toast(R.string.action_unavailable)
        }
    }

    private fun lockScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            performAction(GLOBAL_ACTION_LOCK_SCREEN)
            return
        }

        val devicePolicyManager =
            getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val admin = ComponentName(this, LockScreenAdminReceiver::class.java)

        if (devicePolicyManager.isAdminActive(admin)) {
            devicePolicyManager.lockNow()
            return
        }

        toast(R.string.lock_admin_required)
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, admin)
            putExtra(
                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                getString(R.string.lock_admin_explanation)
            )
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { startActivity(intent) }
            .onFailure { toast(R.string.action_unavailable) }
    }

    fun toast(resId: Int) {
        Toast.makeText(this, resId, Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val ACTION_RESET_POSITION = "com.dtouch.app.action.RESET_POSITION"
        const val ACTION_ORB_APPEARANCE_CHANGED =
            "com.dtouch.app.action.ORB_APPEARANCE_CHANGED"
        const val ACTION_ORB_IMAGE_CHANGED =
            "com.dtouch.app.action.ORB_IMAGE_CHANGED"
    }
}
