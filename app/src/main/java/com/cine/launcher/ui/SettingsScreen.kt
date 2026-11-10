package com.cine.launcher.ui

import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.cine.launcher.R
import com.cine.launcher.data.AppEntry

/** Wallpaper, animation, scaling, app management, favourites, theme, startup and About. */
class SettingsScreen(host: Host) : Screen(host) {
    private val ctx = host.context
    private val p = host.prefs
    private val cats = listOf("Appearance", "Wallpaper", "Display & Motion", "Applications", "Startup", "About CINÉ")
    private val catViews = ArrayList<View>()
    private var selected = 0
    private val panel = Box(ctx).apply { orientation = LinearLayout.VERTICAL }

    override val root: View = Box(ctx).apply {
        orientation = LinearLayout.HORIZONTAL
        setPadding(Ui.px(56), Ui.px(4), Ui.px(56), Ui.px(20))
    }

    init {
        val r = root as LinearLayout
        val left = Box(ctx).apply { orientation = LinearLayout.VERTICAL }
        left.addView(ctx.label("SETTINGS", 12, C.primary, 700, spacing = 0.2f), lp(-2, -2).margins(b = 14))
        cats.forEachIndexed { i, n ->
            val v = ctx.label(n, 18, C.onVariant, 600).apply {
                pad(18, 14)
                layoutParams = lpPx(260, -2).margins(b = 6).also { it.height = android.view.ViewGroup.LayoutParams.WRAP_CONTENT }
                tvFocus(radius = 10, normal = 0, focused = C.high, scale = 1.03f)
                setOnClickListener { select(i, focusPanel = true) }
                addFocusListener { has -> if (has && selected != i) select(i, focusPanel = false) }
                setOnKeyListener { _, code, ev ->
                    if (ev.action == KeyEvent.ACTION_DOWN && code == KeyEvent.KEYCODE_DPAD_RIGHT) { focusPanel(); true } else false
                }
            }
            catViews += v; left.addView(v)
        }
        r.addView(left, lp(-2, -2).margins(r = 36))
        r.addView(ScrollView(ctx).apply {
            isVerticalScrollBarEnabled = false; clipChildren = false; clipToPadding = false
            addView(panel, android.view.ViewGroup.LayoutParams(-1, -2))
        }, LinearLayout.LayoutParams(0, -1, 1f))
        select(0, false)
    }

    private fun select(i: Int, focusPanel: Boolean) {
        selected = i
        catViews.forEachIndexed { idx, v -> (v as TextView).setTextColor(if (idx == i) C.primary else C.onVariant) }
        panel.removeAllViews()
        when (i) {
            0 -> appearance()
            1 -> wallpaper()
            2 -> display()
            3 -> applications()
            4 -> startup()
            else -> about()
        }
        if (focusPanel) focusPanel()
    }

    private fun focusPanel() { panel.getChildAt(1)?.let { if (it.isFocusable) it.requestFocus() else firstFocusable(panel)?.requestFocus() } }

    private fun firstFocusable(v: View): View? {
        if (v.isFocusable) return v
        if (v is android.view.ViewGroup) for (c in 0 until v.childCount) firstFocusable(v.getChildAt(c))?.let { return it }
        return null
    }

    private fun heading(t: String, sub: String) {
        panel.addView(ctx.label(t, 36, C.onSurface, 600, display = true), lp(-2, -2).margins(b = 6))
        panel.addView(ctx.label(sub, 15, C.onVariant, 400), lp(-2, -2).margins(b = 18))
    }

    /** Row whose value cycles with OK and LEFT/RIGHT. */
    private fun option(title: String, desc: String, values: List<String>, get: () -> Int, set: (Int) -> Unit, after: () -> Unit = {}) {
        val value = ctx.label(values[get()], 15, C.primary, 700, spacing = 0.04f)
        val row = Box(ctx).apply {
            gravity = Gravity.CENTER_VERTICAL
            pad(22, 16)
            layoutParams = lp(-1, -2).margins(b = 10)
            tvFocus(radius = 12, normal = C.low, focused = C.container, scale = 1.01f)
        }
        val text = Box(ctx).apply { orientation = LinearLayout.VERTICAL }
        text.addView(ctx.label(title, 18, C.onSurface, 600))
        text.addView(ctx.label(desc, 13, C.onVariant, 400), lp(-2, -2).margins(t = 3))
        row.addView(text, LinearLayout.LayoutParams(0, -2, 1f))
        row.addView(ctx.label("‹ ", 18, C.outline, 700))
        row.addView(value)
        row.addView(ctx.label(" ›", 18, C.outline, 700))
        fun step(d: Int) {
            set(Math.floorMod(get() + d, values.size))
            value.text = values[get()]
            after()
        }
        row.setOnClickListener { step(1) }
        row.setOnKeyListener { _, code, ev ->
            if (ev.action != KeyEvent.ACTION_DOWN) false
            else when (code) {
                KeyEvent.KEYCODE_DPAD_RIGHT -> { step(1); true }
                KeyEvent.KEYCODE_DPAD_LEFT -> { step(-1); true }
                else -> false
            }
        }
        panel.addView(row)
    }

    private fun toggle(title: String, desc: String, get: () -> Boolean, set: (Boolean) -> Unit, after: () -> Unit = {}) =
        option(title, desc, listOf("Off", "On"), { if (get()) 1 else 0 }, { set(it == 1) }, after)

    private fun appearance() {
        heading("Appearance", "Theme accent and labels.")
        option("Theme accent", "Colour used for focus rings, buttons and highlights.",
            Accents.all.map { it.name }, { p.accent }, { p.accent = it }) { host.restartUi() }
        toggle("Sample content badges", "Mark placeholder titles as SAMPLE on their artwork.", { p.showSampleBadges }, { p.showSampleBadges = it })
    }

    private fun wallpaper() {
        heading("Wallpaper", "Backdrop behind every screen. Generated on-device, no downloads.")
        option("Wallpaper", "Choose a backdrop.", Wallpapers.names, { p.wallpaper }, { p.wallpaper = it }) { host.applyWallpaper() }
    }

    private fun display() {
        heading("Display & Motion", "Tune CINÉ for your TV and viewing distance.")
        val scales = listOf(90, 100, 110, 120)
        option("UI scale", "Enlarge or shrink the whole interface.", scales.map { "$it%" },
            { scales.indexOf(p.uiScale).coerceAtLeast(1) }, { p.uiScale = scales[it] }) { host.restartUi() }
        toggle("Animations", "Focus zoom and hero transitions. Turn off for the snappiest response on older TVs.",
            { p.animations }, { p.animations = it; Motion.enabled = it })
    }

    private fun applications() {
        heading("Applications", "OK toggles Favourite (★ first on Home). Press → then OK to hide an app from Home.")
        val apps = host.repo.apps
        if (apps.isEmpty()) panel.addView(ctx.label("No launchable apps found.", 16, C.onVariant, 500))
        apps.forEach { e -> panel.addView(appRow(e)) }
    }

    private fun appRow(e: AppEntry): View {
        val row = Box(ctx).apply { gravity = Gravity.CENTER_VERTICAL; layoutParams = lp(-1, -2).margins(b = 8) }
        val fav = ctx.label("", 14, C.primary, 700)
        val hid = ctx.label("", 14, C.onVariant, 700)
        val main = Box(ctx).apply {
            gravity = Gravity.CENTER_VERTICAL; pad(16, 10)
            tvFocus(radius = 10, normal = C.low, focused = C.container, scale = 1.01f)
        }
        main.addView(android.widget.ImageView(ctx).apply { setImageDrawable(e.icon()); layoutParams = lpPx(36, 36).margins(r = 14) })
        main.addView(ctx.label(e.label, 17, C.onSurface, 600, lines = 1), LinearLayout.LayoutParams(0, -2, 1f))
        main.addView(fav); main.addView(ctx.spacer(14)); main.addView(hid)
        fun refresh() {
            fav.text = if (e.pkg in p.favorites) "★ FAVOURITE" else "☆ FAVOURITE"
            hid.text = if (e.pkg in p.hidden) "HIDDEN" else "VISIBLE"
        }
        refresh()
        main.setOnClickListener { p.toggleFavorite(e.pkg); refresh() }
        main.setOnKeyListener { _, code, ev ->
            if (ev.action == KeyEvent.ACTION_DOWN && code == KeyEvent.KEYCODE_DPAD_RIGHT) { p.toggleHidden(e.pkg); refresh(); true } else false
        }
        main.setOnLongClickListener { host.showAppMenu(e); true }
        return main.apply { layoutParams = lp(-1, -2).margins(b = 8) }
    }

    private fun startup() {
        heading("Startup", "What CINÉ shows when it opens.")
        option("Start screen", "The first screen after launching CINÉ.", listOf("Home", "Applications"), { p.startScreen }, { p.startScreen = it })
        panel.addView(ctx.label("To use CINÉ as the Home app: press HOME on the remote and choose CINÉ if your TV offers a launcher picker. The stock launcher is never disabled; CINÉ also works as a normal app.", 14, C.onVariant, 400).apply {
            setLineSpacing(Ui.f(5), 1f); layoutParams = lp(Ui.px(700), -2).margins(t = 10)
        })
    }

    private fun about() {
        heading("About CINÉ", "A cinematic, customisable launcher for Android TV.")
        listOf(
            "Version" to "1.0.0",
            "Device" to "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}",
            "Android" to "${android.os.Build.VERSION.RELEASE} (API ${android.os.Build.VERSION.SDK_INT})",
            "Installed apps" to "${host.repo.apps.size}",
            "Content" to "Sample titles with generated artwork; no network access used",
            "Licence" to "MIT. Fonts Bodoni Moda & Manrope: SIL OFL 1.1",
        ).forEach { (k, v) ->
            val row = Box(ctx).apply { pad(22, 14); background = round(C.low, 12); layoutParams = lp(-1, -2).margins(b = 8) }
            row.addView(ctx.label(k, 16, C.onVariant, 600), LinearLayout.LayoutParams(Ui.px(200), -2))
            row.addView(ctx.label(v, 16, C.onSurface, 500), LinearLayout.LayoutParams(0, -2, 1f))
            panel.addView(row)
        }
    }

    override fun initialFocus(): View? = catViews.getOrNull(selected)
}
