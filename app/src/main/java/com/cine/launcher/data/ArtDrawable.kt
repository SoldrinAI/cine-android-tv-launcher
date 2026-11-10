package com.cine.launcher.data

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.drawable.Drawable
import java.util.Random

/**
 * Procedural "cinematic still" used as clearly-labelled SAMPLE artwork.
 * Cheap to draw, zero memory for bitmaps, no network and no third-party imagery.
 */
class ArtDrawable(private val seed: Int) : Drawable() {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()

    private val palettes = arrayOf(
        intArrayOf(0xFF2B1B0E.toInt(), 0xFFB5651D.toInt(), 0xFFF2CA50.toInt()),
        intArrayOf(0xFF0B1B2B.toInt(), 0xFF1F5E7A.toInt(), 0xFFE1C290.toInt()),
        intArrayOf(0xFF2A0F1A.toInt(), 0xFF8C2F4A.toInt(), 0xFFFFB1C1.toInt()),
        intArrayOf(0xFF0F2018.toInt(), 0xFF2F6B4F.toInt(), 0xFFCFE8B0.toInt()),
        intArrayOf(0xFF1A1530.toInt(), 0xFF5B3E96.toInt(), 0xFFE9C0FF.toInt()),
        intArrayOf(0xFF1F1A14.toInt(), 0xFF6E5B3A.toInt(), 0xFFE3D3A8.toInt()),
    )

    override fun draw(canvas: Canvas) {
        val b = bounds
        if (b.isEmpty) return
        val w = b.width().toFloat()
        val h = b.height().toFloat()
        val rnd = Random(seed * 7919L)
        val pal = palettes[Math.floorMod(seed, palettes.size)]

        paint.shader = LinearGradient(0f, b.top.toFloat(), 0f, b.bottom.toFloat(), pal[0], pal[1], Shader.TileMode.CLAMP)
        canvas.drawRect(b, paint)

        // Sun / moon glow
        val cx = b.left + w * (0.25f + rnd.nextFloat() * 0.5f)
        val cy = b.top + h * (0.30f + rnd.nextFloat() * 0.2f)
        val r = minOf(w, h) * (0.35f + rnd.nextFloat() * 0.2f)
        paint.shader = RadialGradient(cx, cy, r, intArrayOf(pal[2], (pal[2] and 0x00FFFFFF) or 0x55000000, 0x00000000), floatArrayOf(0f, 0.35f, 1f), Shader.TileMode.CLAMP)
        canvas.drawCircle(cx, cy, r, paint)

        // Layered ridge silhouettes
        paint.shader = null
        for (layer in 0 until 3) {
            val base = b.top + h * (0.62f + layer * 0.12f)
            path.reset()
            path.moveTo(b.left.toFloat(), b.bottom.toFloat())
            var x = 0f
            while (x <= w + 8f) {
                val y = base - rnd.nextFloat() * h * (0.10f - layer * 0.02f)
                path.lineTo(b.left + x, y)
                x += w / 9f
            }
            path.lineTo(b.right.toFloat(), b.bottom.toFloat())
            path.close()
            paint.color = blend(pal[0], 0xFF000000.toInt(), 0.25f + layer * 0.25f)
            canvas.drawPath(path, paint)
        }
    }

    private fun blend(a: Int, b: Int, t: Float): Int {
        fun ch(s: Int) = ((((a shr s) and 255) * (1 - t) + ((b shr s) and 255) * t).toInt() and 255) shl s
        return 0xFF000000.toInt() or ch(16) or ch(8) or ch(0)
    }

    override fun setAlpha(alpha: Int) { paint.alpha = alpha }
    override fun setColorFilter(cf: ColorFilter?) { paint.colorFilter = cf }
    @Deprecated("Deprecated in Java")
    override fun getOpacity() = PixelFormat.OPAQUE
}
