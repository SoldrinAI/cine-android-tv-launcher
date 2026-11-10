package com.cine.launcher.ui

import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import com.cine.launcher.data.AppEntry

/** Application library: recently opened, category filterNames and a grid of every launchable app. */
class AppsScreen(host: Host) : Screen(host) {
    private val ctx = host.context
    override val tab = 3

    private val chips = ArrayList<View>()
    private var filter = 0
    private val filterNames = listOf("All applications", "Video & Streaming", "Audio & Music", "Games", "Utilities", "Favourites")
    private val recentSection = Box(ctx).apply { orientation = LinearLayout.VERTICAL }
    private val grid = Box(ctx).apply { orientation = LinearLayout.VERTICAL }
    private val countLabel = ctx.label("", 15, C.onSurface, 700, spacing = 0.02f)
    private var firstChip: View? = null

    private val col = Box(ctx).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(0, 0, 0, Ui.px(60))
    }
    override val root: View = ScrollView(ctx).apply {
        isVerticalScrollBarEnabled = false
        clipChildren = false; clipToPadding = false
        addView(col, ViewGroup.LayoutParams(-1, -2))
    }

    init {
        val head = Box(ctx).apply { orientation = LinearLayout.VERTICAL; setPadding(Ui.px(56), Ui.px(4), Ui.px(56), 0) }
        head.addView(ctx.label("APPLICATIONS  /  INSTALLED", 12, C.primary, 700, spacing = 0.18f))
        val titleRow = Box(ctx).apply { gravity = Gravity.CENTER_VERTICAL }
        titleRow.addView(ctx.label("Your Applications", 40, C.onSurface, 600, display = true), LinearLayout.LayoutParams(0, -2, 1f))
        titleRow.addView(countLabel.apply { pad(18, 10); background = round(C.withAlpha(C.container, 0.9f), 10) })
        head.addView(titleRow, lp(-1, -2).margins(t = 6))
        col.addView(head)
        col.addView(recentSection, lp(-1, -2).margins(t = 18))

        val chipRow = Row(ctx)
        filterNames.forEachIndexed { i, n ->
            val chip = ctx.label(n.uppercase(), 13, C.onVariant, 700, spacing = 0.12f).apply {
                pad(20, 12)
                tvFocus(radius = 10, normal = C.container, focused = C.high, scale = 1.04f)
                setOnClickListener { filter = i; styleChips(); fillGrid() }
                contentDescription = n
            }
            chip.layoutParams = lp(-2, -2).margins(r = 12)
            chips += chip; chipRow.strip.addView(chip)
        }
        firstChip = chips.first()
        col.addView(chipRow, lp(-1, -2).margins(t = 6))
        col.addView(grid, lp(-1, -2))
        styleChips()
        fillAll()
    }

    private fun styleChips() {
        chips.forEachIndexed { i, v ->
            (v as android.widget.TextView).apply {
                setTextColor(if (i == filter) C.primary else C.onVariant)
                text = filterNames[i].uppercase()
            }
        }
    }

    private fun visibleApps(): List<AppEntry> {
        val fav = host.prefs.favorites
        val base = host.repo.apps
        return when (filter) {
            1 -> base.filter { it.category == AppEntry.Category.VIDEO }
            2 -> base.filter { it.category == AppEntry.Category.AUDIO }
            3 -> base.filter { it.category == AppEntry.Category.GAME }
            4 -> base.filter { it.category == AppEntry.Category.UTILITY }
            5 -> base.filter { it.pkg in fav }
            else -> base
        }.sortedWith(compareBy({ it.pkg !in fav }, { com.cine.launcher.data.Brands.rank(it.pkg) }, { it.label.lowercase() }))
    }

    private fun tile(e: AppEntry): View = AppTile(ctx, e, e.pkg in host.prefs.favorites).apply {
        setOnClickListener { host.launch(e) }
        setOnLongClickListener { host.showAppMenu(e); true }
    }

    private fun fillAll() { fillRecent(); fillGrid() }

    private fun fillRecent() {
        recentSection.removeAllViews()
        val byPkg = host.repo.apps.associateBy { it.pkg }
        val recent = host.prefs.recent.mapNotNull { byPkg[it] }
        if (recent.isEmpty()) return
        val head = Box(ctx).apply { gravity = Gravity.CENTER_VERTICAL; setPadding(Ui.px(56), 0, Ui.px(56), 0) }
        head.addView(ctx.label("Recently Opened", 22, C.onSurface, 600))
        recentSection.addView(head)
        val row = Row(ctx)
        recent.forEach { row.strip.addView(tile(it)) }
        recentSection.addView(row, LinearLayout.LayoutParams(-1, -2))
    }

    private fun fillGrid() {
        val hadFocus = grid.hasFocus()
        grid.removeAllViews()
        val apps = visibleApps()
        countLabel.text = "${host.repo.apps.size} installed"
        val perRow = 7
        apps.chunked(perRow).forEach { chunk ->
            val r = Box(ctx).apply { clipChildren = false; setPadding(Ui.px(56), Ui.px(8), Ui.px(56), Ui.px(8)) }
            chunk.forEach { r.addView(tile(it)) }
            grid.addView(r)
        }
        if (apps.isEmpty()) grid.addView(ctx.label("No applications in this category.", 16, C.onVariant, 500).apply { pad(56, 30) })
        if (hadFocus) firstChip?.requestFocus()
    }

    override fun initialFocus(): View? = (grid.getChildAt(0) as? ViewGroup)?.getChildAt(0) ?: firstChip
    override fun onShow() = fillAll()
    override fun onAppsChanged() = fillAll()
}
