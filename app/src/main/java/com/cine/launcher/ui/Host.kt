package com.cine.launcher.ui

import android.content.Context
import android.view.View
import com.cine.launcher.data.AppEntry
import com.cine.launcher.data.AppRepository
import com.cine.launcher.data.CatalogSource
import com.cine.launcher.data.Prefs
import com.cine.launcher.data.Title

/** Services the screens need from the activity. Keeps screens free of Activity references. */
interface Host {
    val context: Context
    val prefs: Prefs
    val repo: AppRepository
    val catalog: CatalogSource
    fun openTab(index: Int)
    fun openDetails(title: Title)
    fun launch(app: AppEntry)
    fun showAppMenu(app: AppEntry)
    fun openSettings()
    fun openSearch()
    fun back()
    /** Rebuild the whole UI (used after scale/theme changes). */
    fun restartUi()
    fun applyWallpaper()
}

abstract class Screen(protected val host: Host) {
    abstract val root: View
    /** Which top-bar tab is highlighted, or -1 for none. */
    open val tab: Int = -1
    /** Whether the top bar is drawn above the screen's own backdrop (home hero). */
    open val fullBleed: Boolean = false
    var savedFocus: View? = null
    open fun initialFocus(): View? = null
    open fun onShow() {}
    /** Return true if the screen consumed BACK. */
    open fun onBack(): Boolean = false
    open fun onAppsChanged() {}
    open fun release() {}
}
