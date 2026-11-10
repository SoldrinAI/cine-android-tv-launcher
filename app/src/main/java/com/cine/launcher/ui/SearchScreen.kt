package com.cine.launcher.ui

import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.cine.launcher.data.Brands

/** On-screen D-pad keyboard with live results from installed apps and the sample catalogue. */
class SearchScreen(host: Host) : Screen(host) {
    private val ctx = host.context
    private val query = StringBuilder()
    private val queryView: TextView = ctx.label("", 30, C.onSurface, 600, display = true, lines = 1)
    private val results = Box(ctx).apply { orientation = LinearLayout.VERTICAL }
    private var firstKey: View? = null

    override val root: View = Box(ctx).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(Ui.px(56), Ui.px(4), Ui.px(56), Ui.px(20))
    }

    init {
        val r = root as LinearLayout
        r.addView(ctx.label("SEARCH", 12, C.primary, 700, spacing = 0.2f))
        queryView.apply { pad(18, 12); background = round(C.withAlpha(C.container, 0.9f), 12); hint = "Search apps and titles" ; setHintTextColor(C.outline) }
        r.addView(queryView, lp(-1, -2).margins(t = 8, b = 14))

        val body = Box(ctx).apply { gravity = Gravity.TOP }
        val keyboard = Box(ctx).apply { orientation = LinearLayout.VERTICAL }
        val chars = ('A'..'Z').map { it.toString() } + ('0'..'9').map { it.toString() }
        chars.chunked(9).forEach { rowKeys ->
            val row = Box(ctx)
            rowKeys.forEach { row.addView(key(it) { type(it) }) }
            keyboard.addView(row)
        }
        val actions = Box(ctx)
        actions.addView(key("SPACE", 3) { type(" ") })
        actions.addView(key("DEL", 3) { if (query.isNotEmpty()) { query.setLength(query.length - 1); update() } })
        actions.addView(key("CLEAR", 3) { query.setLength(0); update() })
        keyboard.addView(actions)
        body.addView(keyboard, lp(-2, -2).margins(r = 40))
        body.addView(results, LinearLayout.LayoutParams(0, -2, 1f))
        r.addView(body)
        update()
    }

    private fun key(text: String, span: Int = 1, action: () -> Unit): View {
        val w = 52 * span + 8 * (span - 1)
        return ctx.label(text, if (span > 1) 12 else 18, C.onSurface, 700).apply {
            gravity = Gravity.CENTER
            layoutParams = lpPx(w, 52).margins(r = 8, b = 8)
            tvFocus(radius = 8, normal = C.container, focused = C.high, scale = 1.08f)
            setOnClickListener { action() }
            if (firstKey == null) firstKey = this
        }
    }

    private fun type(s: String) { if (query.length < 40) { query.append(s.lowercase()); update() } }

    private fun update() {
        queryView.text = if (query.isEmpty()) "" else query.toString()
        results.removeAllViews()
        val q = query.toString().trim()
        if (q.isEmpty()) {
            results.addView(ctx.label("Type to search installed apps and sample titles.", 16, C.onVariant, 500))
            return
        }
        val apps = host.repo.apps.filter { it.label.contains(q, true) }
            .sortedBy { Brands.rank(it.pkg) }.take(8)
        val titles = host.catalog.search(q)
        if (apps.isNotEmpty()) {
            results.addView(ctx.label("APPLICATIONS", 12, C.secondary, 700, spacing = 0.18f), lp(-2, -2).margins(b = 6))
            val row = Row(ctx).apply { setPadding(0, Ui.px(10), 0, Ui.px(16)) }
            apps.forEach { e ->
                row.strip.addView(AppTile(ctx, e, false).apply {
                    setOnClickListener { host.launch(e) }
                    setOnLongClickListener { host.showAppMenu(e); true }
                })
            }
            results.addView(row, LinearLayout.LayoutParams(-1, -2))
        }
        if (titles.isNotEmpty()) {
            results.addView(ctx.label("SAMPLE TITLES", 12, C.secondary, 700, spacing = 0.18f), lp(-2, -2).margins(b = 6))
            val row = Row(ctx).apply { setPadding(0, Ui.px(10), 0, Ui.px(10)) }
            titles.take(6).forEach { t ->
                val card = TitleCard(ctx, t, true, host.prefs.showSampleBadges)
                card.focusTarget.setOnClickListener { host.openDetails(t) }
                row.strip.addView(card)
            }
            results.addView(row, LinearLayout.LayoutParams(-1, -2))
        }
        if (apps.isEmpty() && titles.isEmpty()) results.addView(ctx.label("No results for \"$q\".", 16, C.onVariant, 500))
    }

    override fun initialFocus(): View? = firstKey
    override fun onAppsChanged() = update()
}
