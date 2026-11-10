package com.cine.launcher.ui

import android.content.Context
import android.graphics.Outline
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat

/** Whether focus/transition animations are enabled (user preference). */
object Motion { var enabled = true }

fun round(color: Int, radius: Number, stroke: Int = 0, strokeW: Number = 0): GradientDrawable =
    GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = Ui.f(radius)
        setColor(color)
        if (strokeW.toFloat() > 0f) setStroke(Ui.px(strokeW).coerceAtLeast(1), stroke)
    }

fun Context.label(
    text: CharSequence,
    size: Number,
    color: Int = C.onSurface,
    weight: Int = 500,
    display: Boolean = false,
    spacing: Float = 0f,
    lines: Int = 0,
    italic: Boolean = false,
    allCaps: Boolean = false,
): TextView = TextView(this).apply {
    this.text = if (allCaps) text.toString().uppercase() else text
    setTextSize(TypedValue.COMPLEX_UNIT_PX, Ui.f(size))
    setTextColor(color)
    typeface = if (display) Fonts.bodoni(context, weight) else Fonts.manrope(context, weight)
    if (italic) setTypeface(typeface, Typeface.ITALIC)
    letterSpacing = spacing
    includeFontPadding = false
    if (lines > 0) {
        maxLines = lines
        ellipsize = TextUtils.TruncateAt.END
    }
}

fun Context.icon(res: Int, sizeCss: Number, tint: Int = C.onSurface): ImageView = ImageView(this).apply {
    setImageDrawable(ContextCompat.getDrawable(context, res)?.mutate())
    setColorFilter(tint)
    layoutParams = LinearLayout.LayoutParams(Ui.px(sizeCss), Ui.px(sizeCss))
    scaleType = ImageView.ScaleType.FIT_CENTER
}

fun lp(w: Int, h: Int) = LinearLayout.LayoutParams(w, h)
fun lpPx(wCss: Number, hCss: Number) = LinearLayout.LayoutParams(Ui.px(wCss), Ui.px(hCss))
fun LinearLayout.LayoutParams.margins(l: Number = 0, t: Number = 0, r: Number = 0, b: Number = 0) = apply {
    setMargins(Ui.px(l), Ui.px(t), Ui.px(r), Ui.px(b))
}

fun View.pad(l: Number, t: Number = l, r: Number = l, b: Number = t) = setPadding(Ui.px(l), Ui.px(t), Ui.px(r), Ui.px(b))

fun View.clipRounded(radiusCss: Number) {
    val r = Ui.f(radiusCss)
    outlineProvider = object : ViewOutlineProvider() {
        override fun getOutline(view: View, outline: Outline) {
            outline.setRoundRect(0, 0, view.width, view.height, r)
        }
    }
    clipToOutline = true
}

/**
 * Standard TV focus treatment: filled card that gains a jewelry-gold ring, lifts and scales when focused.
 * The ring is a foreground so it always draws above child artwork.
 */
fun View.tvFocus(
    radius: Number = 8,
    normal: Int = C.low,
    focused: Int = C.high,
    scale: Float = 1.05f,
    ring: Boolean = true,
) {
    isFocusable = true
    isClickable = true
    isFocusableInTouchMode = false
    background = StateListDrawable().apply {
        addState(intArrayOf(android.R.attr.state_focused), round(focused, radius))
        addState(intArrayOf(), round(normal, radius))
    }
    if (ring) {
        foreground = StateListDrawable().apply {
            addState(intArrayOf(android.R.attr.state_focused), round(0, radius, C.primary, 2.5))
            addState(intArrayOf(), null as Drawable? ?: GradientDrawable())
        }
    }
    val base = elevation
    onFocusChangeListener = View.OnFocusChangeListener { v, has ->
        val s = if (has) scale else 1f
        val dur = if (Motion.enabled) 170L else 0L
        v.animate().cancel()
        v.animate().scaleX(s).scaleY(s).setDuration(dur).start()
        v.elevation = if (has) Ui.f(12) else base
        v.translationZ = if (has) Ui.f(6) else 0f
    }
}

/** Plain text/icon button with the gold-primary or glass-secondary look from the hero CTAs. */
fun Context.pill(text: String, iconRes: Int? = null, primary: Boolean = false, hint: String? = null): LinearLayout {
    val ctx = this
    return LinearLayout(ctx).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        pad(20, 11)
        val fg = if (primary) C.onPrimary else C.onSurface
        if (iconRes != null) addView(icon(iconRes, 18, fg).also { it.layoutParams = lpPx(18, 18).margins(r = 8) })
        addView(ctx.label(text, 15, fg, 700))
        if (hint != null) {
            addView(ctx.label(hint, 10, fg, 700, spacing = 0.05f).apply {
                pad(6, 2)
                background = round(C.withAlpha(fg, 0.15f), 4)
                layoutParams = lpPx(-2, -2).margins(l = 8).also { it.width = ViewGroup.LayoutParams.WRAP_CONTENT; it.height = ViewGroup.LayoutParams.WRAP_CONTENT }
            })
        }
        tvFocus(
            radius = 8,
            normal = if (primary) C.primary else C.withAlpha(C.high, 0.8f),
            focused = if (primary) C.primary else C.highest,
            scale = 1.06f,
            ring = !primary || true,
        )
    }
}

fun Context.badge(text: String, color: Int = C.onSurface, bg: Int = C.withAlpha(C.lowest, 0.85f), size: Number = 10): TextView =
    label(text, size, color, 700, spacing = 0.06f).apply {
        pad(7, 3)
        background = round(bg, 5)
    }

fun Context.spacer(wCss: Number, hCss: Number = 0) = View(this).apply { layoutParams = lpPx(wCss, hCss) }

/** Adds a focus callback without replacing the visual focus listener installed by [tvFocus]. */
fun View.addFocusListener(cb: (Boolean) -> Unit) {
    val prev = onFocusChangeListener
    onFocusChangeListener = View.OnFocusChangeListener { v, has ->
        prev?.onFocusChange(v, has)
        cb(has)
    }
}

/** LinearLayout that never clips children, so scaled/focused TV cards and glows can overflow their row. */
open class Box(context: Context) : LinearLayout(context) {
    init {
        clipChildren = false
        clipToPadding = false
    }
}
