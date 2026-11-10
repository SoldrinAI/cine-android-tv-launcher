package com.cine.launcher.ui

import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView

/** Movies / Series / Watchlist tabs: sample shelves from the pluggable catalogue source. */
class CatalogScreen(host: Host, override val tab: Int) : Screen(host) {
    private val ctx = host.context
    private val col = Box(ctx).apply { orientation = LinearLayout.VERTICAL; setPadding(0, 0, 0, Ui.px(60)) }
    override val root: View = ScrollView(ctx).apply {
        isVerticalScrollBarEnabled = false; clipChildren = false; clipToPadding = false
        addView(col, ViewGroup.LayoutParams(-1, -2))
    }

    private fun build() {
        col.removeAllViews()
        val name = when (tab) { 1 -> "Movies"; 2 -> "Series"; else -> "Watchlist" }
        col.addView(ctx.label(name, 40, C.onSurface, 600, display = true).apply { setPadding(Ui.px(56), Ui.px(4), 0, Ui.px(6)) })
        val wl = host.prefs.watchlist
        val ids = when (tab) { 1 -> listOf("trending", "acclaimed"); 2 -> listOf("continue", "recent"); else -> listOf("watchlist") }
        host.catalog.shelves(wl).filter { it.id in ids }.forEach { shelf ->
            col.addView(
                ShelfView(ctx, shelf, host.prefs.showSampleBadges, onFocus = {}, onOpen = { host.openDetails(it) }),
                LinearLayout.LayoutParams(-1, -2).margins(t = 10)
            )
        }
    }

    init { build() }
    override fun onShow() = build()
    override fun initialFocus(): View? = ((col.getChildAt(1) as? ShelfView)?.cards?.firstOrNull())
}
