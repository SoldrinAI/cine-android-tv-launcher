package com.cine.launcher.ui

import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.cine.launcher.R
import com.cine.launcher.data.AppEntry
import com.cine.launcher.data.ArtDrawable
import com.cine.launcher.data.Title

/**
 * The cinematic hero. Shows either a featured sample title, or — when a streaming app tile is focused —
 * a preview for that app: its real name and icon, sample artwork/title/description and an "OK launches" cue.
 * It never claims the title is actually available on the app.
 */
class Hero(private val host: Host) : FrameLayout(host.context) {
    private val ctx = host.context
    private val art = View(ctx)
    private val content = Box(ctx).apply { orientation = LinearLayout.VERTICAL }
    private var current: Title? = null
    val primary: View
    val secondary: View
    private val watchBtn: LinearLayout

    init {
        clipChildren = false
        setBackgroundColor(C.lowest)
        addView(art, LayoutParams(-1, -1))
        addView(View(ctx).apply { background = gradientScrim(false, C.withAlpha(C.lowest, 0.95f), 0) }, LayoutParams(Ui.screenW * 3 / 5, -1))
        addView(View(ctx).apply { background = gradientScrim(true, 0, C.surface) }, LayoutParams(-1, -1))
        content.setPadding(Ui.px(56), Ui.px(70), Ui.px(56), Ui.px(22))
        content.gravity = Gravity.BOTTOM
        addView(content, LayoutParams(-1, -1))

        primary = ctx.pill("Open details", R.drawable.ic_info, primary = true, hint = "OK")
        watchBtn = ctx.pill("Watchlist", R.drawable.ic_add)
        secondary = watchBtn
    }

    fun showTitle(t: Title) = render(t, null)

    fun showApp(app: AppEntry) = render(host.catalog.previewFor(app.pkg), app)

    private fun render(t: Title, app: AppEntry?) {
        val same = current?.id == t.id && app == null
        current = t
        val apply = {
            art.background = ArtDrawable(t.seed)
            buildContent(t, app)
        }
        if (same || !Motion.enabled) {
            apply(); content.alpha = 1f
        } else {
            content.animate().cancel()
            content.animate().alpha(0f).setDuration(110).withEndAction {
                apply()
                content.animate().alpha(1f).setDuration(220).start()
            }.start()
            art.animate().cancel(); art.alpha = 0.4f; art.animate().alpha(1f).setDuration(380).start()
        }
    }

    private fun buildContent(t: Title, app: AppEntry?) {
        content.removeAllViews()
        val kicker = if (app != null) "SAMPLE PREVIEW  •  ${app.label.uppercase()}" else "FEATURED SAMPLE  •  ${t.badge}"
        val kickRow = Box(ctx).apply { gravity = Gravity.CENTER_VERTICAL }
        if (app != null && app.installed) {
            kickRow.addView(ImageView(ctx).apply {
                setImageDrawable(app.icon()); scaleType = ImageView.ScaleType.FIT_CENTER
                layoutParams = lpPx(34, 34).margins(r = 12)
            })
        }
        kickRow.addView(ctx.label(kicker, 12, C.secondary, 700, spacing = 0.2f))
        content.addView(kickRow, lp(-2, -2).margins(b = 10))

        content.addView(ctx.label(t.name.uppercase(), 56, C.onSurface, 600, display = true, spacing = -0.01f, lines = 2).apply {
            layoutParams = lp(Ui.px(760), -2).margins(b = 10)
        })
        val meta = Box(ctx).apply { gravity = Gravity.CENTER_VERTICAL }
        meta.addView(ctx.label("${t.year}", 13, C.onSurface, 700))
        meta.addView(ctx.label("  •  ${t.genre}  •  ${t.runtime}  ", 13, C.onVariant, 500))
        meta.addView(ctx.badge(t.badge, C.onSurface, C.withAlpha(C.high, 0.85f), 11))
        meta.addView(ctx.label("   ★ ${t.rating}", 13, C.primary, 700))
        content.addView(meta, lp(-2, -2).margins(b = 12))
        content.addView(ctx.label(t.synopsis, 16, C.onVariant, 400, lines = 2).apply {
            setLineSpacing(Ui.f(6), 1f)
            layoutParams = lp(Ui.px(700), -2).margins(b = 16)
        })

        val actions = Box(ctx).apply { gravity = Gravity.CENTER_VERTICAL }
        if (app != null) {
            val label = if (app.installed) "Press OK to launch ${app.label}" else "OK opens ${app.label} in the Play Store"
            val cue = ctx.pill(label, R.drawable.ic_play, primary = true)
            // Cue is purely informational here; the focused app tile itself handles OK.
            cue.isFocusable = false; cue.isClickable = false; cue.onFocusChangeListener = null
            cue.scaleX = 1.0f
            actions.addView(cue)
            actions.addView(ctx.label("  Sample content — availability on ${app.label} is not verified.", 12, C.outline, 500))
        } else {
            (primary.parent as? ViewGroup)?.removeView(primary)
            (watchBtn.parent as? ViewGroup)?.removeView(watchBtn)
            actions.addView(primary, lp(-2, -2).margins(r = 14))
            updateWatch(t)
            actions.addView(watchBtn, lp(-2, -2).margins(r = 14))
        }
        content.addView(actions, lp(-2, -2))
    }

    fun updateWatch(t: Title) {
        val on = t.id in host.prefs.watchlist
        (watchBtn.getChildAt(1) as TextView).text = if (on) "In watchlist" else "Watchlist"
    }

    val currentTitle: Title? get() = current
}

class HomeScreen(host: Host) : Screen(host) {
    private val ctx = host.context
    override val tab = 0
    override val fullBleed = true

    private val hero = Hero(host)
    private val list = Box(ctx).apply { orientation = LinearLayout.VERTICAL; setPadding(0, Ui.px(14), 0, Ui.px(60)) }
    private val scroll = ScrollView(ctx).apply { isVerticalScrollBarEnabled = false; clipChildren = false; clipToPadding = false }
    private var appsRow: Row? = null
    private var firstTile: View? = null
    private val handler = Handler(Looper.getMainLooper())
    private var pending: Runnable? = null
    private val featured = host.catalog.featured()

    private val appsSection = Box(ctx).apply { orientation = LinearLayout.VERTICAL }
    private val shelvesSection = Box(ctx).apply { orientation = LinearLayout.VERTICAL }

    override val root: View = Box(ctx).apply {
        orientation = LinearLayout.VERTICAL
        addView(hero, lp(-1, (Ui.screenH * 0.50f).toInt()))
        scroll.addView(list, ViewGroup.LayoutParams(-1, -2))
        addView(scroll, lp(-1, 0).also { it.weight = 1f })
    }

    init {
        hero.showTitle(featured[0])
        hero.primary.setOnClickListener { hero.currentTitle?.let { host.openDetails(it) } }
        hero.secondary.setOnClickListener {
            hero.currentTitle?.let { host.prefs.toggleWatchlist(it.id); hero.updateWatch(it); rebuildShelves() }
        }
        buildAll()
    }

    private fun buildAll() {
        list.removeAllViews()
        buildApps()
        rebuildShelves()
    }

    private fun hintRow(): View = Box(ctx).apply {
        gravity = Gravity.CENTER_VERTICAL
        setPadding(Ui.px(56), 0, Ui.px(56), 0)
        addView(ctx.label("YOUR ENTERTAINMENT", 12, C.secondary, 700, spacing = 0.2f))
        addView(ctx.spacer(14))
        addView(ctx.label("Installed on this TV  •  OK launches  •  hold OK for options", 12, C.onVariant, 500))
    }

    private fun buildApps() {
        list.addView(appsSection)
        list.addView(shelvesSection)
        fillApps()
    }

    private fun fillApps() {
        val hadFocus = appsRow?.hasFocus() == true
        val idx = appsRow?.strip?.indexOfChild(appsRow?.strip?.focusedChild) ?: -1
        appsSection.removeAllViews()
        appsSection.addView(hintRow())
        val row = Row(ctx)
        appsRow = row
        val fav = host.prefs.favorites
        val hidden = host.prefs.hidden
        val installed = host.repo.apps.filter { it.pkg !in hidden }
            .sortedWith(compareBy({ it.pkg !in fav }, { com.cine.launcher.data.Brands.rank(it.pkg) }, { it.label.lowercase() }))
            .take(12)
        val entries = installed + host.repo.missingBrands().filter { it.pkg !in hidden }
        firstTile = null
        entries.forEach { e ->
            val tile = AppTile(ctx, e, e.pkg in fav)
            tile.addFocusListener { has -> if (has) scheduleHero { hero.showApp(e) } }
            tile.setOnClickListener { host.launch(e) }
            tile.setOnLongClickListener { host.showAppMenu(e); true }
            if (firstTile == null) firstTile = tile
            row.strip.addView(tile)
        }
        if (entries.isEmpty()) row.strip.addView(ctx.label("No launchable apps found.", 15, C.onVariant, 500))
        appsSection.addView(row, LinearLayout.LayoutParams(-1, -2))
        if (hadFocus && row.strip.childCount > 0) row.strip.getChildAt(idx.coerceIn(0, row.strip.childCount - 1)).requestFocus()
    }

    private fun rebuildShelves() {
        val focusedBefore = shelvesSection.focusedChild != null
        shelvesSection.removeAllViews()
        val show = host.prefs.showSampleBadges
        host.catalog.shelves(host.prefs.watchlist).forEach { shelf ->
            shelvesSection.addView(
                ShelfView(ctx, shelf, show, onFocus = { t -> scheduleHero { hero.showTitle(t) } }, onOpen = { host.openDetails(it) }),
                LinearLayout.LayoutParams(-1, -2).margins(t = 14)
            )
        }
        if (focusedBefore) shelvesSection.getChildAt(0)?.requestFocus()
    }

    /** Debounce hero updates so fast D-pad scrolling doesn't thrash the hero. */
    private fun scheduleHero(block: () -> Unit) {
        pending?.let { handler.removeCallbacks(it) }
        val r = Runnable { block() }
        pending = r
        handler.postDelayed(r, if (Motion.enabled) 140 else 0)
    }

    override fun initialFocus(): View? = firstTile ?: hero.primary

    override fun onShow() {
        fillApps()
        rebuildShelves()
        hero.currentTitle?.let { hero.updateWatch(it) }
    }

    override fun onBack(): Boolean {
        // BACK from a lower shelf returns to the apps row; from the apps row returns to the hero.
        if (scroll.scrollY > 0 || shelvesSection.hasFocus()) {
            scroll.smoothScrollTo(0, 0)
            firstTile?.requestFocus()
            return true
        }
        return false
    }

    override fun onAppsChanged() = fillApps()

    override fun release() {
        pending?.let { handler.removeCallbacks(it) }
    }
}
