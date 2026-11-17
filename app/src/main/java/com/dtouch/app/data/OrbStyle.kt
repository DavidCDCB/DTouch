package com.dtouch.app.data

import com.dtouch.app.R

/**
 * Tipo de animación del orbe. La animación se dibuja a 12,5 fps para no
 * gastar CPU con la pantalla encendida; los AVD a 60 fps consumían demasiado.
 */
enum class OrbAnimation {
    NONE,
    ROTATE,
    PULSE
}

/**
 * Estilos de orbe predefinidos.
 *
 * [previewDrawableRes] es el drawable estático que se usa en la galería y en
 * la vista previa. Para los estilos animados, el orbe flotante envuelve ese
 * mismo drawable en [com.dtouch.app.service.OrbAnimatedDrawable].
 *
 * [id] es el valor que se guarda en SharedPreferences.
 */
enum class OrbStyle(
    val id: String,
    val previewDrawableRes: Int,
    val labelRes: Int,
    val animation: OrbAnimation = OrbAnimation.NONE,
    val animationDurationMs: Long = 0L
) {
    CLASSIC("classic", R.drawable.orb, R.string.orb_style_classic),
    SOLID("solid", R.drawable.orb_style_solid, R.string.orb_style_solid),
    RING("ring", R.drawable.orb_style_ring, R.string.orb_style_ring),
    DOT("dot", R.drawable.orb_style_dot, R.string.orb_style_dot),
    DUO("duo", R.drawable.orb_style_duo, R.string.orb_style_duo),
    MIDNIGHT("midnight", R.drawable.orb_style_midnight, R.string.orb_style_midnight),
    FROST("frost", R.drawable.orb_style_frost, R.string.orb_style_frost),
    AQUA("aqua", R.drawable.orb_style_aqua, R.string.orb_style_aqua),
    RAINBOW("rainbow", R.drawable.orb_style_rainbow_base, R.string.orb_style_rainbow, OrbAnimation.ROTATE, 6000L),
    AURORA("aurora", R.drawable.orb_style_aurora_base, R.string.orb_style_aurora, OrbAnimation.ROTATE, 8000L),
    PULSE("pulse", R.drawable.orb_style_pulse_base, R.string.orb_style_pulse, OrbAnimation.PULSE, 1800L),
    COMET("comet", R.drawable.orb_style_comet_base, R.string.orb_style_comet, OrbAnimation.ROTATE, 2500L);

    companion object {
        val DEFAULT = CLASSIC

        fun fromId(raw: String?): OrbStyle =
            entries.firstOrNull { it.id == raw } ?: DEFAULT
    }
}
