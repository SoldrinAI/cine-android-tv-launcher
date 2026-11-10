package com.cine.launcher.ui

import android.graphics.drawable.GradientDrawable

/** Built-in generated wallpapers (no bundled imagery, no memory-heavy bitmaps). */
object Wallpapers {
    private class W(val name: String, val top: Int, val bottom: Int)

    private val list = listOf(
        W("Midnight", 0xFF121316.toInt(), 0xFF0D0E11.toInt()),
        W("Gold Dusk", 0xFF2A2012.toInt(), 0xFF0D0E11.toInt()),
        W("Deep Ocean", 0xFF0B1B2B.toInt(), 0xFF07090C.toInt()),
        W("Crimson Noir", 0xFF2A0F1A.toInt(), 0xFF0C0709.toInt()),
        W("Graphite", 0xFF2B2C30.toInt(), 0xFF101114.toInt()),
    )

    val names: List<String> = list.map { it.name }

    fun drawable(index: Int): GradientDrawable {
        val w = list[index.coerceIn(0, list.lastIndex)]
        return GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(w.top, w.bottom))
    }
}
