package com.dtouch.app.ui.main

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.accessibility.AccessibilityManager
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.dtouch.app.R
import com.dtouch.app.data.GestureAction
import com.dtouch.app.data.GestureType
import com.dtouch.app.data.OrbImageStore
import com.dtouch.app.data.OrbStyle
import com.dtouch.app.data.SettingsRepository
import com.dtouch.app.databinding.ActivityMainBinding
import com.dtouch.app.service.DTouchAccessibilityService
import com.dtouch.app.service.LockScreenAdminReceiver
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Pantalla de configuración de DTouch.
 *
 * No usa PreferenceActivity ni RecyclerView: son seis filas fijas y un
 * diálogo de selección. Las filas se generan desde GestureType para que
 * agregar un gesto nuevo sea una sola enumeración + su string.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var settings: SettingsRepository
    private lateinit var orbImageStore: OrbImageStore
    private var previewBitmap: Bitmap? = null
    private val rows = mutableMapOf<GestureType, TextView>()
    private val styleTiles = mutableMapOf<OrbStyle, StyleTile>()

    private val pickOrbImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let { importOrbImage(it) }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        settings = SettingsRepository(this)
        orbImageStore = OrbImageStore(this)
        applyWindowInsets()

        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_reset_position -> {
                    settings.resetOrbPosition()
                    sendBroadcast(
                        Intent(DTouchAccessibilityService.ACTION_RESET_POSITION)
                            .setPackage(packageName)
                    )
                    Toast.makeText(this, R.string.position_reset, Toast.LENGTH_SHORT).show()
                    true
                }

                R.id.action_about -> {
                    showAbout()
                    true
                }

                else -> false
            }
        }

        setupGestureRows()
        setupGestureSensitivity()
        setupAppearanceControls()
        binding.buttonAccessibility.setOnClickListener { openAccessibilitySettings() }
    }

    override fun onResume() {
        super.onResume()
        refreshStatus()
        refreshGestureSummaries()
    }

    /**
     * Deja que la toolbar pinte debajo de la barra de estado y suma el inset
     * superior a su altura. Así el orbe y los ajustes se ven bien tanto en
     * Android clásico como en el edge-to-edge obligatorio de Android 15.
     */
    private fun applyWindowInsets() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, 0, 0, bars.bottom)

            val params = binding.toolbar.layoutParams
            params.height = resources.getDimensionPixelSize(R.dimen.toolbar_height) + bars.top
            binding.toolbar.layoutParams = params
            binding.toolbar.setPadding(0, bars.top, 0, 0)

            insets
        }
        ViewCompat.requestApplyInsets(binding.root)
    }

    private fun setupGestureRows() {
        binding.gestureContainer.removeAllViews()
        rows.clear()

        GestureType.entries.forEach { type ->
            val row = layoutInflater.inflate(
                R.layout.item_setting_row,
                binding.gestureContainer,
                false
            )
            row.findViewById<TextView>(R.id.row_title).setText(titleFor(type))
            val summary = row.findViewById<TextView>(R.id.row_summary)
            row.setOnClickListener { showActionPicker(type) }
            binding.gestureContainer.addView(row)
            rows[type] = summary
        }
    }

    private fun setupGestureSensitivity() {
        binding.skbarGestureSensitivity.max = 100
        val initial = settings.gestureSensitivity()
        binding.skbarGestureSensitivity.progress = initial
        updateGestureSensitivityLabel(initial)

        binding.skbarGestureSensitivity.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if (!fromUser) return
                    settings.setGestureSensitivity(progress)
                    updateGestureSensitivityLabel(progress)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
                override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
            }
        )
    }

    private fun updateGestureSensitivityLabel(value: Int) {
        binding.tvGestureSensitivityValue.text =
            getString(R.string.gesture_sensitivity_value, value)
    }

    private fun setupAppearanceControls() {
        binding.skbarOrbSize.max =
            SettingsRepository.MAX_ORB_SIZE_DP - SettingsRepository.MIN_ORB_SIZE_DP
        binding.skbarOrbSize.progress =
            settings.orbSizeDp() - SettingsRepository.MIN_ORB_SIZE_DP

        binding.skbarOrbAlpha.max = SettingsRepository.MAX_ORB_ALPHA
        binding.skbarOrbAlpha.progress = settings.orbAlpha()

        previewBitmap = orbImageStore.loadBitmap()
        setupOrbStyleRows()
        updateOrbPreview()
        updateOrbStyleSelection()
        updateOrbImageButtons()

        binding.buttonChooseOrbImage.setOnClickListener {
            pickOrbImage.launch("image/*")
        }
        binding.buttonResetOrbImage.setOnClickListener {
            resetOrbImage()
        }

        binding.skbarOrbSize.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if (!fromUser) return
                    settings.setOrbSizeDp(SettingsRepository.MIN_ORB_SIZE_DP + progress)
                    updateOrbPreview()
                    notifyOrbAppearanceChanged()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
                override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
            }
        )

        binding.skbarOrbAlpha.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if (!fromUser) return
                    settings.setOrbAlpha(progress)
                    updateOrbPreview()
                    notifyOrbAppearanceChanged()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
                override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
            }
        )
    }

    private fun setupOrbStyleRows() {
        binding.orbStyleRow1.removeAllViews()
        binding.orbStyleRow2.removeAllViews()
        binding.orbStyleRow3.removeAllViews()
        binding.orbStyleRow4.removeAllViews()
        styleTiles.clear()

        OrbStyle.entries.forEachIndexed { index, style ->
            val row = when (index / 3) {
                0 -> binding.orbStyleRow1
                1 -> binding.orbStyleRow2
                2 -> binding.orbStyleRow3
                else -> binding.orbStyleRow4
            }
            val tile = layoutInflater.inflate(R.layout.item_orb_style, row, false)
            val card = tile.findViewById<MaterialCardView>(R.id.orb_style_card)
            val preview = tile.findViewById<ImageView>(R.id.orb_style_preview)
            val label = tile.findViewById<TextView>(R.id.orb_style_name)

            preview.setImageResource(style.previewDrawableRes)
            label.setText(style.labelRes)
            tile.contentDescription = getString(style.labelRes)
            tile.setOnClickListener { selectOrbStyle(style) }

            row.addView(tile)
            styleTiles[style] = StyleTile(card, label)
        }
    }

    private fun selectOrbStyle(style: OrbStyle) {
        settings.setOrbStyle(style)
        if (orbImageStore.hasCustomImage()) {
            orbImageStore.clear()
            previewBitmap = null
        }
        updateOrbPreview()
        updateOrbStyleSelection()
        updateOrbImageButtons()
        notifyOrbImageChanged()
    }

    private fun updateOrbStyleSelection() {
        val hasCustomImage = orbImageStore.hasCustomImage()
        val selected = settings.orbStyle()
        val selectedColor = ContextCompat.getColor(this, R.color.dt_primary)
        val normalColor = ContextCompat.getColor(this, R.color.dt_on_surface_variant)

        styleTiles.forEach { (style, tile) ->
            val isSelected = !hasCustomImage && style == selected
            tile.card.strokeColor = if (isSelected) selectedColor else Color.TRANSPARENT
            tile.label.setTextColor(if (isSelected) selectedColor else normalColor)
            tile.card.isSelected = isSelected
        }
    }

    private fun updateOrbPreview() {
        val bitmap = previewBitmap
        if (bitmap != null) {
            binding.ivOrbPreview.setImageBitmap(bitmap)
        } else {
            binding.ivOrbPreview.setImageResource(settings.orbStyle().previewDrawableRes)
        }

        val sizeDp = settings.orbSizeDp()
        val sizePx = dpToPx(sizeDp)
        binding.ivOrbPreview.layoutParams.apply {
            width = sizePx
            height = sizePx
        }
        binding.ivOrbPreview.requestLayout()
        binding.ivOrbPreview.imageAlpha = settings.orbAlpha()

        binding.tvOrbSizeValue.text = getString(R.string.orb_size_value, sizeDp)
        val alphaPercent = (settings.orbAlpha() * 100 + 127) / 255
        binding.tvOrbAlphaValue.text = getString(R.string.orb_alpha_value, alphaPercent)
    }

    private fun updateOrbImageButtons() {
        val hasCustomImage = orbImageStore.hasCustomImage()
        binding.buttonResetOrbImage.isVisible = hasCustomImage
        binding.tvOrbImageStatus.text = if (hasCustomImage) {
            getString(R.string.orb_image_custom_active)
        } else {
            getString(R.string.orb_style_active, getString(settings.orbStyle().labelRes))
        }
    }

    private fun importOrbImage(uri: Uri) {
        binding.buttonChooseOrbImage.isEnabled = false
        binding.buttonResetOrbImage.isEnabled = false

        lifecycleScope.launch {
            val saved = withContext(Dispatchers.IO) {
                orbImageStore.saveFromUri(uri)
            }
            if (isFinishing || isDestroyed) return@launch

            binding.buttonChooseOrbImage.isEnabled = true
            binding.buttonResetOrbImage.isEnabled = true

            if (saved) {
                previewBitmap = withContext(Dispatchers.IO) {
                    orbImageStore.loadBitmap()
                }
                updateOrbPreview()
                updateOrbStyleSelection()
                updateOrbImageButtons()
                notifyOrbImageChanged()
                Toast.makeText(this@MainActivity, R.string.orb_image_saved, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@MainActivity, R.string.orb_image_error, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun resetOrbImage() {
        orbImageStore.clear()
        previewBitmap = null
        updateOrbPreview()
        updateOrbStyleSelection()
        updateOrbImageButtons()
        notifyOrbImageChanged()
        Toast.makeText(this, R.string.orb_image_reset, Toast.LENGTH_SHORT).show()
    }

    private fun notifyOrbAppearanceChanged() {
        sendBroadcast(
            Intent(DTouchAccessibilityService.ACTION_ORB_APPEARANCE_CHANGED)
                .setPackage(packageName)
        )
    }

    private fun notifyOrbImageChanged() {
        sendBroadcast(
            Intent(DTouchAccessibilityService.ACTION_ORB_IMAGE_CHANGED)
                .setPackage(packageName)
        )
    }

    private fun dpToPx(dp: Int): Int =
        (dp * resources.displayMetrics.density + 0.5f).toInt()

    private fun showActionPicker(type: GestureType) {
        val actions = GestureAction.entries
        val labels = actions.map { getString(it.labelRes) }.toTypedArray()
        val checked = actions.indexOf(settings.actionFor(type)).coerceAtLeast(0)

        MaterialAlertDialogBuilder(this)
            .setTitle(titleFor(type))
            .setSingleChoiceItems(labels, checked) { dialog, which ->
                val action = actions[which]
                settings.setAction(type, action)
                rows[type]?.text = getString(action.labelRes)
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun refreshGestureSummaries() {
        rows.forEach { (type, summary) ->
            summary.text = getString(settings.actionFor(type).labelRes)
        }
    }

    private fun refreshStatus() {
        val enabled = isAccessibilityServiceEnabled()
        binding.statusText.setText(
            if (enabled) R.string.status_accessibility_on else R.string.status_accessibility_off
        )
        binding.buttonAccessibility.setText(
            if (enabled) R.string.open_accessibility_settings else R.string.enable_accessibility
        )

        val needsAdmin = Build.VERSION.SDK_INT < Build.VERSION_CODES.P
        binding.lockTitle.visibility = if (needsAdmin) View.VISIBLE else View.GONE
        binding.lockAdminGroup.visibility = if (needsAdmin) View.VISIBLE else View.GONE

        if (!needsAdmin) return

        val adminActive = isAdminActive()
        binding.lockAdminStatus.setText(
            if (adminActive) R.string.lock_admin_active else R.string.lock_admin_inactive
        )
        binding.buttonLockAdmin.setText(
            if (adminActive) R.string.disable_lock_admin else R.string.enable_lock_admin
        )
        binding.buttonLockAdmin.setOnClickListener {
            if (isAdminActive()) disableAdmin() else requestAdmin()
        }
    }

    private fun openAccessibilitySettings() {
        if (isAccessibilityServiceEnabled()) {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            return
        }

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.accessibility_disclosure_title)
            .setMessage(R.string.accessibility_disclosure)
            .setPositiveButton(R.string.continue_label) { _, _ ->
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val manager = getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val expected = ComponentName(this, DTouchAccessibilityService::class.java)
        return manager.getEnabledAccessibilityServiceList(
            AccessibilityServiceInfo.FEEDBACK_ALL_MASK
        ).any { info ->
            val component = info.id?.let { ComponentName.unflattenFromString(it) }
            component == expected
        }
    }

    private fun isAdminActive(): Boolean {
        val manager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        return manager.isAdminActive(ComponentName(this, LockScreenAdminReceiver::class.java))
    }

    private fun requestAdmin() {
        val admin = ComponentName(this, LockScreenAdminReceiver::class.java)
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, admin)
            putExtra(
                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                getString(R.string.lock_admin_explanation)
            )
        }
        startActivity(intent)
    }

    private fun disableAdmin() {
        val manager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        manager.removeActiveAdmin(ComponentName(this, LockScreenAdminReceiver::class.java))
        refreshStatus()
    }

    @Suppress("DEPRECATION")
    private fun showAbout() {
        val version = runCatching {
            packageManager.getPackageInfo(packageName, 0).versionName
        }.getOrNull() ?: "1.0.0"

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.about)
            .setMessage(getString(R.string.about_message, version))
            .setPositiveButton(R.string.ok, null)
            .show()
    }

    private fun titleFor(type: GestureType): Int = when (type) {
        GestureType.SINGLE_TAP -> R.string.gesture_single_tap
        GestureType.DOUBLE_TAP -> R.string.gesture_double_tap
        GestureType.SWIPE_UP -> R.string.gesture_swipe_up
        GestureType.SWIPE_DOWN -> R.string.gesture_swipe_down
        GestureType.SWIPE_LEFT -> R.string.gesture_swipe_left
        GestureType.SWIPE_RIGHT -> R.string.gesture_swipe_right
    }

    private data class StyleTile(
        val card: MaterialCardView,
        val label: TextView
    )
}
