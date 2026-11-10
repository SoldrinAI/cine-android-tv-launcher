package com.cine.launcher.ui

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextClock
import android.widget.TextView
import com.cine.launcher.R
import com.cine.launcher.data.AppEntry
import com.cine.launcher.data.ArtDrawable
import com.cine.launcher.data.Title

/** Streaming/application tile: the REAL icon from PackageManager, drawn undistorted (FIT_CENTER). */
class AppTile(context: Context, val entry: AppEntry, private val favorite: Boolean) : FrameLayout(context) {
    init {
        val w = 150
        val h = 112
        layoutParams = LinearLayout.LayoutParams(Ui.px(w), Ui.px(h)).margins(r = 16)
        tvFocus(radius = 10, normal = C.low, focused = C.high, scale = 1.08f)
        val col = Box(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            pad(10, 12, 10, 8)
        }
        val iconBox = FrameLayout(context).apply { layoutParams = lpPx(60, 60) }
        val iv = ImageView(context).apply {
            scaleType = ImageView.ScaleType.FIT_CENTER
            layoutParams = FrameLayout.LayoutParams(-1, -1)
        }
        if (entry.installed) {
            iv.setImageDrawable(entry.icon())
        } else {
            // No logo is fabricated for apps that are not installed: show a neutral placeholder.
            iconBox.background = round(C.highest, 12)
            iconBox.addView(context.label(entry.label.take(1).uppercase(), 26, C.outline, 700, display = true).apply {
                gravity = Gravity.CENTER
                layoutParams = FrameLayout.LayoutParams(-1, -1)
            })
        }
        iconBox.addView(iv)
        col.addView(iconBox)
        col.addView(context.label(entry.label, 13, C.onSurface, 700, lines = 1).apply {
            layoutParams = lpPx(-1, -2).margins(t = 8).also { it.width = ViewGroup.LayoutParams.MATCH_PARENT; it.height = ViewGroup.LayoutParams.WRAP_CONTENT }
            gravity = Gravity.CENTER
        })
        addView(col, FrameLayout.LayoutParams(-1, -1))
        if (!entry.installed) {
            addView(context.badge("NOT INSTALLED", C.outline, C.withAlpha(C.lowest, 0.9f), 8).apply {
                layoutParams = FrameLayout.LayoutParams(-2, -2, Gravity.TOP or Gravity.END).also { it.setMargins(0, Ui.px(6), Ui.px(6), 0) }
            })
            alpha = 0.78f
        } else if (favorite) {
            addView(context.icon(R.drawable.ic_star, 14, C.primary).apply {
                layoutParams = FrameLayout.LayoutParams(Ui.px(14), Ui.px(14), Gravity.TOP or Gravity.END).also { it.setMargins(0, Ui.px(8), Ui.px(8), 0) }
            })
        }
        contentDescription = entry.label
    }
}

/** Landscape (continue watching) or portrait (poster) card with sample artwork. */
class TitleCard(context: Context, val title: Title, portrait: Boolean, showSample: Boolean) : Box(context) {
    init {
        orientation = VERTICAL
        val w = if (portrait) 168 else 296
        val artH = if (portrait) 252 else 167
        layoutParams = LinearLayout.LayoutParams(Ui.px(w), ViewGroup.LayoutParams.WRAP_CONTENT).margins(r = 20)
        isFocusable = true
        isClickable = true
        val art = FrameLayout(context).apply {
            layoutParams = lpPx(w, artH)
            clipRounded(8)
            tvFocus(radius = 8, normal = C.high, focused = C.high, scale = 1.05f)
        }
        art.addView(View(context).apply { background = ArtDrawable(title.seed); layoutParams = FrameLayout.LayoutParams(-1, -1) })
        art.addView(context.badge(title.badge, C.secondary, size = 9).apply {
            layoutParams = FrameLayout.LayoutParams(-2, -2, Gravity.TOP or Gravity.START).also { it.setMargins(Ui.px(8), Ui.px(8), 0, 0) }
        })
        if (showSample) {
            art.addView(context.badge("SAMPLE", C.onPrimary, C.primary, 8).apply {
                layoutParams = FrameLayout.LayoutParams(-2, -2, Gravity.TOP or Gravity.END).also { it.setMargins(0, Ui.px(8), Ui.px(8), 0) }
            })
        }
        if (title.progress != null) {
            val track = FrameLayout(context).apply {
                setBackgroundColor(C.withAlpha(C.lowest, 0.85f))
                layoutParams = FrameLayout.LayoutParams(-1, Ui.px(4), Gravity.BOTTOM)
            }
            track.addView(View(context).apply {
                setBackgroundColor(C.primary)
                layoutParams = FrameLayout.LayoutParams((Ui.px(w) * title.progress).toInt(), -1)
            })
            art.addView(track)
        } else {
            art.addView(context.label("★ ${title.rating}", 11, C.onSurface, 700).apply {
                pad(7, 3); background = round(C.withAlpha(C.lowest, 0.9f), 5)
                layoutParams = FrameLayout.LayoutParams(-2, -2, Gravity.BOTTOM or Gravity.END).also { it.setMargins(0, 0, Ui.px(8), Ui.px(8)) }
            })
        }
        addView(art)
        addView(context.label(title.name, 15, C.onSurface, 700, lines = 1).apply { layoutParams = lpPx(w, -2).margins(t = 8).also { it.height = ViewGroup.LayoutParams.WRAP_CONTENT } })
        val sub = title.remaining ?: "${title.year} • ${title.director}"
        addView(context.label(sub, 12, C.onVariant, 500, lines = 1).apply { layoutParams = lpPx(w, -2).margins(t = 2).also { it.height = ViewGroup.LayoutParams.WRAP_CONTENT } })
        // Focus is carried by the art box; forward click/focus from the card to it.
        descendantFocusability = FOCUS_AFTER_DESCENDANTS
        isFocusable = false
        art.setOnClickListener { performClick() }
    }

    /** The focusable region of the card. */
    val focusTarget: View get() = getChildAt(0)
}

/** Horizontal row that scrolls to keep the focused child visible, with no scrollbars. */
class Row(context: Context) : HorizontalScrollView(context) {
    val strip = Box(context).apply { orientation = LinearLayout.HORIZONTAL; clipChildren = false; clipToPadding = false }
    init {
        isHorizontalScrollBarEnabled = false
        isFillViewport = false
        clipChildren = false
        clipToPadding = false
        overScrollMode = OVER_SCROLL_NEVER
        isSmoothScrollingEnabled = true
        addView(strip, LayoutParams(-2, -2))
        setPadding(Ui.px(56), Ui.px(14), Ui.px(56), Ui.px(26))
    }
}

/** Top navigation: CINÉ wordmark, section tabs, search/settings, device badge, clock. */
class TopBar(context: Context, private val onNav: (Int) -> Unit) : Box(context) {
    private val tabs = ArrayList<TextView>()
    private val dots = ArrayList<View>()
    private val names = listOf("Home", "Movies", "Series", "Applications", "Watchlist")
    val searchBtn: View
    val settingsBtn: View
    val tabViews: List<View> get() = tabs.map { it.parent as View }

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(Ui.px(56), Ui.px(18), Ui.px(56), Ui.px(8))
        addView(context.label("CINÉ", 30, C.primary, 500, display = true, spacing = 0.18f))
        addView(context.spacer(36))
        names.forEachIndexed { i, n ->
            val box = Box(context).apply {
                orientation = VERTICAL
                gravity = Gravity.CENTER_HORIZONTAL
                pad(14, 8, 14, 6)
                tvFocus(radius = 10, normal = 0, focused = C.withAlpha(C.high, 0.9f), scale = 1.04f)
                setOnClickListener { onNav(i) }
                contentDescription = n
            }
            val tv = context.label(n, 16, C.onVariant, 600, spacing = 0.02f)
            val dot = View(context).apply { background = round(C.primary, 4); layoutParams = lpPx(5, 5).margins(t = 4); visibility = View.INVISIBLE }
            box.addView(tv, lp(-2, -2)); box.addView(dot)
            tabs += tv; dots += dot
            addView(box)
        }
        addView(View(context), LinearLayout.LayoutParams(0, 1, 1f))
        searchBtn = iconButton(context, R.drawable.ic_search, "Search") { onNav(SEARCH) }
        settingsBtn = iconButton(context, R.drawable.ic_settings, "Settings") { onNav(SETTINGS) }
        addView(searchBtn); addView(context.spacer(10)); addView(settingsBtn)
        addView(context.spacer(20))
        addView(context.label("${android.os.Build.MODEL}", 11, C.onVariant, 700, spacing = 0.06f, lines = 1).apply {
            pad(10, 5); background = round(C.withAlpha(C.container, 0.9f), 6)
            maxWidth = Ui.px(200)
        })
        addView(context.spacer(18))
        addView(TextClock(context).apply {
            format12Hour = "h:mm"; format24Hour = "HH:mm"
            setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, Ui.f(18))
            setTextColor(C.onSurface); typeface = Fonts.manrope(context, 600); letterSpacing = 0.05f
        })
    }

    private fun iconButton(ctx: Context, res: Int, desc: String, click: () -> Unit): View = FrameLayout(ctx).apply {
        layoutParams = lpPx(40, 40)
        tvFocus(radius = 20, normal = 0, focused = C.high, scale = 1.12f)
        addView(ctx.icon(res, 22, C.onSurface).apply { layoutParams = FrameLayout.LayoutParams(Ui.px(22), Ui.px(22), Gravity.CENTER) })
        setOnClickListener { click() }
        contentDescription = desc
    }

    fun select(index: Int) {
        tabs.forEachIndexed { i, tv ->
            tv.setTextColor(if (i == index) C.onSurface else C.onVariant)
            dots[i].visibility = if (i == index) View.VISIBLE else View.INVISIBLE
        }
    }

    companion object { const val SEARCH = 100; const val SETTINGS = 101 }
}

/** Simple full-screen-ish modal used for the app context menu. */
class ContextMenu(context: Context, title: String, items: List<Pair<String, () -> Unit>>, private val dismiss: () -> Unit) : FrameLayout(context) {
    init {
        setBackgroundColor(C.withAlpha(C.lowest, 0.78f))
        isClickable = true
        val panel = Box(context).apply {
            orientation = LinearLayout.VERTICAL
            pad(26, 22)
            background = round(C.container, 14, C.outlineVariant, 1)
        }
        panel.addView(context.label(title, 22, C.onSurface, 600, display = true, lines = 1).apply { layoutParams = lpPx(320, -2).margins(b = 14).also { it.height = ViewGroup.LayoutParams.WRAP_CONTENT } })
        items.forEach { (text, action) ->
            panel.addView(context.label(text, 16, C.onSurface, 600).apply {
                pad(16, 12)
                layoutParams = lpPx(320, -2).margins(b = 8).also { it.height = ViewGroup.LayoutParams.WRAP_CONTENT }
                tvFocus(radius = 8, normal = C.high, focused = C.highest)
                setOnClickListener { dismiss(); action() }
            })
        }
        addView(panel, FrameLayout.LayoutParams(-2, -2, Gravity.CENTER))
        descendantFocusability = FOCUS_AFTER_DESCENDANTS
    }
}

fun gradientScrim(vertical: Boolean, from: Int, to: Int): GradientDrawable = GradientDrawable(
    if (vertical) GradientDrawable.Orientation.TOP_BOTTOM else GradientDrawable.Orientation.LEFT_RIGHT, intArrayOf(from, to)
)
