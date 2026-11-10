package com.cine.launcher.ui

import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.cine.launcher.R
import com.cine.launcher.data.ArtDrawable
import com.cine.launcher.data.Brands
import com.cine.launcher.data.Title

/** Movie details. All data is clearly-labelled sample content; no availability is claimed. */
class DetailsScreen(host: Host, private val title: Title) : Screen(host) {
    private val ctx = host.context
    private lateinit var watchLabel: TextView
    private lateinit var firstButton: View

    override val fullBleed = true

    override val root: View = FrameLayout(ctx).apply {
        setBackgroundColor(C.lowest)
        addView(View(ctx).apply { background = ArtDrawable(title.seed) }, FrameLayout.LayoutParams(-1, -1))
        addView(View(ctx).apply { background = gradientScrim(false, C.withAlpha(C.lowest, 0.96f), C.withAlpha(C.lowest, 0.1f)) }, FrameLayout.LayoutParams(-1, -1))
        addView(View(ctx).apply { background = gradientScrim(true, 0, C.surface) }, FrameLayout.LayoutParams(-1, -1))

        val col = Box(ctx).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.BOTTOM
            setPadding(Ui.px(56), Ui.px(90), Ui.px(56), Ui.px(48))
        }
        col.addView(ctx.label("SAMPLE TITLE  •  ${title.badge}", 12, C.secondary, 700, spacing = 0.2f), lp(-2, -2).margins(b = 10))
        col.addView(ctx.label(title.name.uppercase(), 64, C.onSurface, 600, display = true, lines = 2), lp(Ui.px(900), -2).margins(b = 12))
        val meta = Box(ctx).apply { gravity = Gravity.CENTER_VERTICAL }
        meta.addView(ctx.label("${title.year}  •  ${title.genre}  •  ${title.runtime}   ", 15, C.onVariant, 500))
        meta.addView(ctx.icon(R.drawable.ic_star, 16, C.primary))
        meta.addView(ctx.label("  ${title.rating}", 15, C.onSurface, 700))
        col.addView(meta, lp(-2, -2).margins(b = 16))
        col.addView(ctx.label(title.synopsis, 18, C.onVariant, 400).apply { setLineSpacing(Ui.f(7), 1f) }, lp(Ui.px(760), -2).margins(b = 14))
        col.addView(ctx.label("Directed by ${title.director}   •   Cast: ${title.cast.joinToString(", ")}", 14, C.onVariant, 500), lp(-2, -2).margins(b = 22))

        val actions = Box(ctx).apply { gravity = Gravity.CENTER_VERTICAL }
        val watch = ctx.pill("Watchlist", R.drawable.ic_bookmark, primary = true)
        watchLabel = watch.getChildAt(1) as TextView
        refreshWatch()
        watch.setOnClickListener { host.prefs.toggleWatchlist(title.id); refreshWatch() }
        firstButton = watch
        actions.addView(watch, lp(-2, -2).margins(r = 14))

        val streaming = host.repo.apps.filter { Brands.rank(it.pkg) < Brands.core.size }.take(3)
        streaming.forEach { app ->
            val b = ctx.pill("Open ${app.label}", R.drawable.ic_play)
            b.setOnClickListener { host.launch(app) }
            actions.addView(b, lp(-2, -2).margins(r = 14))
        }
        val back = ctx.pill("Back", null)
        back.setOnClickListener { host.back() }
        actions.addView(back)
        col.addView(actions, lp(-2, -2))
        col.addView(ctx.label("Sample content with generated artwork. Availability on any streaming service is not verified.", 12, C.outline, 500), lp(-2, -2).margins(t = 16))
        addView(col, FrameLayout.LayoutParams(-1, -1))
    }

    private fun refreshWatch() {
        watchLabel.text = if (title.id in host.prefs.watchlist) "Remove from watchlist" else "Add to watchlist"
    }

    override fun initialFocus(): View? = firstButton
}
