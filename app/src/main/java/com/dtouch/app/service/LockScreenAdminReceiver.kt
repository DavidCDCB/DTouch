package com.dtouch.app.service

import android.app.admin.DeviceAdminReceiver

/**
 * Administrador del dispositivo usado únicamente para apagar la pantalla
 * en Android 8 o anterior. En Android 9+ se usa GLOBAL_ACTION_LOCK_SCREEN.
 */
class LockScreenAdminReceiver : DeviceAdminReceiver()
