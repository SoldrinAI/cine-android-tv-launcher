package com.cine.launcher.ui

import android.content.Context
import android.graphics.Typeface
import kotlin.math.roundToInt

/** Colour tokens taken from the Stitch "Cinematic Luxury TV OS" design system. */
object C {
    val surface = 0xFF121316.toInt()
    val lowest = 0xFF0D0E11.toInt()
    val low = 0xFF1B1B1F.toInt()
    val container = 0xFF1F1F23.toInt()
    val high = 0xFF292A2D.toInt()
    val highest = 0xFF343538.toInt()
    val onSurface = 0xFFE3E2E6.toInt()
    val onVariant = 0xFFD0C5AF.toInt()
    val outline = 0xFF99907C.toInt()
    val outlineVariant = 0xFF4D4635.toInt()
    val secondary = 0xFFE1C290.toInt()
    val error = 0xFFFFB4AB.toInt()

    /** Accent colour, changed by the theme preference. */
    var primary = 0xFFF2CA50.toInt()
    var onPrimary = 0xFF3C2F00.toInt()

    fun withAlpha(color: Int, alpha: Float): Int =
        (color and 0x00FFFFFF) or ((alpha.coerceIn(0f, 1f) * 255).roundToInt() shl 24)
}

data class Accent(val name: String, val primary: Int, val onPrimary: Int)

object Accents {
    val all = listOf(
        Accent("Jewelry Gold", 0xFFF2CA50.toInt(), 0xFF3C2F00.toInt()),
        Accent("Ivory", 0xFFE3E2E6.toInt(), 0xFF2F3034.toInt()),
        Accent("Rose", 0xFFFFB1C1.toInt(), 0xFF5E1130.toInt()),
        Accent("Azure", 0xFF9FCAFF.toInt(), 0xFF00325B.toInt()),
    )

    fun apply(index: Int) {
        val a = all[index.coerceIn(0, all.lastIndex)]
        C.primary = a.primary
        C.onPrimary = a.onPrimary
    }
}

/**
 * The Stitch layout is authored on a 1280 px wide canvas (=1920 px at 1.5x on a 1080p TV).
 * [Ui.px] converts a design px value to real pixels on whatever density the TV reports,
 * multiplied by the user's UI scale preference.
 */
object Ui {
    private var unit = 1.5f
    var screenW = 1920
    var screenH = 1080

    fun init(context: Context, scalePercent: Int) {
        val m = context.resources.displayMetrics
        screenW = m.widthPixels
        screenH = m.heightPixels
        unit = screenW / 1280f * (scalePercent / 100f)
    }

    fun px(css: Number): Int = (css.toFloat() * unit).roundToInt()
    fun f(css: Number): Float = css.toFloat() * unit
}

/** Bodoni Moda (display) and Manrope (UI), both SIL OFL, bundled as variable fonts in assets. */
object Fonts {
    private val cache = HashMap<String, Typeface>()

    private fun load(context: Context, file: String, variation: String): Typeface =
        cache.getOrPut("$file|$variation") {
            try {
                Typeface.Builder(context.assets, "fonts/$file")
                    .setFontVariationSettings(variation)
                    .build() ?: Typeface.DEFAULT
            } catch (e: Exception) {
                Typeface.DEFAULT
            }
        }

    fun manrope(context: Context, weight: Int = 400): Typeface =
        load(context, "manrope.ttf", "'wght' $weight")

    fun bodoni(context: Context, weight: Int = 600): Typeface =
        load(context, "bodoni_moda.ttf", "'wght' $weight, 'opsz' 72")
}
