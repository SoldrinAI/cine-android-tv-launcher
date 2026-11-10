package com.cine.launcher

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import com.cine.launcher.data.AppEntry
import com.cine.launcher.data.AppRepository
import com.cine.launcher.data.CatalogSource
import com.cine.launcher.data.Prefs
import com.cine.launcher.data.SampleCatalog
import com.cine.launcher.data.Title
import com.cine.launcher.ui.Accents
import com.cine.launcher.ui.AppTile
import com.cine.launcher.ui.AppsScreen
import com.cine.launcher.ui.CatalogScreen
import com.cine.launcher.ui.ContextMenu
import com.cine.launcher.ui.DetailsScreen
import com.cine.launcher.ui.HomeScreen
import com.cine.launcher.ui.Host
import com.cine.launcher.ui.Motion
import com.cine.launcher.ui.Screen
import com.cine.launcher.ui.SearchScreen
import com.cine.launcher.ui.SettingsScreen
import com.cine.launcher.ui.TopBar
import com.cine.launcher.ui.Ui
import com.cine.launcher.ui.Wallpapers

/**
 * Single-activity launcher. Owns the screen stack, top bar, global key handling and the app repository.
 * HOME (singleTask) always returns to the Home screen; BACK walks the stack and never exits the launcher.
 */
class MainActivity : Activity(), Host, AppRepository.Listener {
    override val context get() = this
    override lateinit var prefs: Prefs
    override lateinit var repo: AppRepository
    override val catalog: CatalogSource = SampleCatalog

    private lateinit var rootView: FrameLayout
    private lateinit var content: FrameLayout
    private lateinit var topBar: TopBar
    private val stack = ArrayList<Screen>()
    private var menu: ContextMenu? = null
    private var menuReturnFocus: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = Prefs(this)
        Accents.apply(prefs.accent)
        Motion.enabled = prefs.animations
        Ui.init(this, prefs.uiScale)
        repo = AppRepository(applicationContext)
        repo.addListener(this)

        rootView = FrameLayout(this)
        content = FrameLayout(this)
        rootView.addView(content, FrameLayout.LayoutParams(-1, -1))
        topBar = TopBar(this) { onNav(it) }
        rootView.addView(topBar, FrameLayout.LayoutParams(-1, -2))
        setContentView(rootView)
        applyWallpaper()
        hideSystemUi()

        val reopenSettings = savedInstanceState?.getBoolean(KEY_SETTINGS) == true || pendingSettings
        pendingSettings = false
        when {
            reopenSettings -> { setRoot(HomeScreen(this)); openSettings() }
            prefs.startScreen == 1 -> setRoot(AppsScreen(this))
            else -> setRoot(HomeScreen(this))
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_SETTINGS, stack.lastOrNull() is SettingsScreen)
    }

    override fun onStart() { super.onStart(); repo.start() }
    override fun onStop() { repo.stop(); super.onStop() }
    override fun onDestroy() {
        repo.removeListener(this)
        stack.forEach { it.release() }
        super.onDestroy()
    }

    override fun onResume() { super.onResume(); hideSystemUi() }

    /** HOME pressed while CINÉ is already running. */
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent?.hasCategory(Intent.CATEGORY_HOME) == true) {
            dismissMenu()
            if (stack.size == 1 && stack[0] is HomeScreen) (stack[0] as HomeScreen).onBack() else setRoot(HomeScreen(this))
        }
    }

    override fun onAppsChanged(apps: List<AppEntry>) { stack.lastOrNull()?.onAppsChanged() }

    // ---- Navigation -------------------------------------------------------------------------

    private fun onNav(i: Int) {
        when (i) {
            TopBar.SEARCH -> openSearch()
            TopBar.SETTINGS -> openSettings()
            0 -> setRoot(HomeScreen(this))
            1, 2, 4 -> setRoot(CatalogScreen(this, i))
            3 -> setRoot(AppsScreen(this))
        }
    }

    override fun openTab(index: Int) = onNav(index)
    override fun openSettings() = push(SettingsScreen(this))
    override fun openSearch() = push(SearchScreen(this))
    override fun openDetails(title: Title) = push(DetailsScreen(this, title))

    private fun setRoot(screen: Screen) {
        stack.lastOrNull()?.savedFocus = currentFocus
        stack.forEach { it.release() }
        stack.clear()
        stack += screen
        show(screen, restoreFocus = false)
    }

    private fun push(screen: Screen) {
        stack.lastOrNull()?.savedFocus = currentFocus
        stack += screen
        show(screen, restoreFocus = false)
    }

    override fun back() {
        if (menu != null) { dismissMenu(); return }
        val top = stack.lastOrNull() ?: return
        if (top.onBack()) return
        if (stack.size > 1) {
            stack.removeAt(stack.lastIndex).release()
            show(stack.last(), restoreFocus = true)
        } else if (top !is HomeScreen) {
            setRoot(HomeScreen(this))
        }
        // On Home itself BACK is consumed: a launcher must never exit to nothing.
    }

    private fun show(screen: Screen, restoreFocus: Boolean) {
        content.removeAllViews()
        val pad = if (screen.fullBleed) 0 else Ui.px(76)
        content.setPadding(0, pad, 0, 0)
        content.addView(screen.root, FrameLayout.LayoutParams(-1, -1))
        topBar.select(screen.tab)
        screen.onShow()
        if (Motion.enabled) { content.alpha = 0f; content.animate().alpha(1f).setDuration(180).start() } else content.alpha = 1f
        content.post { focusScreen(screen, restoreFocus) }
    }

    private fun focusScreen(screen: Screen, restoreFocus: Boolean) {
        val saved = screen.savedFocus
        val target = if (restoreFocus && saved != null && saved.isAttachedToWindow && saved.isFocusable) saved else screen.initialFocus()
        (target ?: firstFocusable(content) ?: topBar.searchBtn).requestFocus()
    }

    private fun firstFocusable(v: View): View? {
        if (v.isFocusable && v.visibility == View.VISIBLE) return v
        if (v is ViewGroup) for (i in 0 until v.childCount) firstFocusable(v.getChildAt(i))?.let { return it }
        return null
    }

    // ---- Host services ----------------------------------------------------------------------

    override fun launch(app: AppEntry) {
        if (repo.launch(this, app)) {
            if (app.installed) prefs.pushRecent(app.pkg)
        } else {
            Toast.makeText(this, "Couldn't open ${app.label}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun showAppMenu(app: AppEntry) {
        dismissMenu()
        menuReturnFocus = currentFocus
        val items = ArrayList<Pair<String, () -> Unit>>()
        items += (if (app.installed) "Open" else "Find in Play Store") to { launch(app) }
        if (app.installed) {
            items += (if (app.pkg in prefs.favorites) "Remove from favourites" else "Add to favourites") to {
                prefs.toggleFavorite(app.pkg); stack.lastOrNull()?.onAppsChanged()
            }
            items += (if (app.pkg in prefs.hidden) "Show on Home" else "Hide from Home") to {
                prefs.toggleHidden(app.pkg); stack.lastOrNull()?.onAppsChanged()
            }
            items += "App info" to {
                if (!repo.openAppInfo(this, app)) Toast.makeText(this, "App info unavailable", Toast.LENGTH_SHORT).show()
            }
        }
        val m = ContextMenu(this, app.label, items) { dismissMenu() }
        menu = m
        rootView.addView(m, FrameLayout.LayoutParams(-1, -1))
        m.post { (firstFocusable(m) ?: m).requestFocus() }
    }

    private fun dismissMenu() {
        menu?.let { rootView.removeView(it) }
        menu = null
        menuReturnFocus?.takeIf { it.isAttachedToWindow }?.requestFocus()
        menuReturnFocus = null
    }

    override fun restartUi() {
        pendingSettings = true
        recreate()
    }

    override fun applyWallpaper() {
        rootView.background = Wallpapers.drawable(prefs.wallpaper)
    }

    // ---- Keys -------------------------------------------------------------------------------

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> { back(); return true }
            KeyEvent.KEYCODE_MENU -> {
                (currentFocus as? AppTile)?.let { showAppMenu(it.entry) }
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    /** Safety net: focus must never be lost after a directional key. */
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val handled = super.dispatchKeyEvent(event)
        if (event.action == KeyEvent.ACTION_DOWN && event.keyCode in DPAD && currentFocus == null) {
            rootView.post {
                if (currentFocus == null) {
                    (menu?.let { firstFocusable(it) } ?: stack.lastOrNull()?.initialFocus() ?: firstFocusable(content) ?: topBar.searchBtn).requestFocus()
                }
            }
        }
        return handled
    }

    private fun hideSystemUi() {
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
    }

    companion object {
        private const val KEY_SETTINGS = "settings"
        private val DPAD = setOf(KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_DPAD_RIGHT)
        private var pendingSettings = false
    }
}
