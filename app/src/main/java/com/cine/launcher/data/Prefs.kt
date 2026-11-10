package com.cine.launcher.data

import android.content.Context
import android.content.SharedPreferences

/** All user preferences, persisted locally in SharedPreferences. */
class Prefs(context: Context) {
    private val sp: SharedPreferences =
        context.applicationContext.getSharedPreferences("cine_prefs", Context.MODE_PRIVATE)

    var wallpaper: Int
        get() = sp.getInt("wallpaper", 0)
        set(v) = sp.edit().putInt("wallpaper", v).apply()

    var animations: Boolean
        get() = sp.getBoolean("animations", true)
        set(v) = sp.edit().putBoolean("animations", v).apply()

    /** UI scale in percent (90..120). */
    var uiScale: Int
        get() = sp.getInt("ui_scale", 100)
        set(v) = sp.edit().putInt("ui_scale", v).apply()

    var accent: Int
        get() = sp.getInt("accent", 0)
        set(v) = sp.edit().putInt("accent", v).apply()

    /** 0 = Home, 1 = Applications. */
    var startScreen: Int
        get() = sp.getInt("start_screen", 0)
        set(v) = sp.edit().putInt("start_screen", v).apply()

    var showSampleBadges: Boolean
        get() = sp.getBoolean("sample_badges", true)
        set(v) = sp.edit().putBoolean("sample_badges", v).apply()

    var favorites: Set<String>
        get() = sp.getStringSet("favorites", emptySet())!!.toSet()
        set(v) = sp.edit().putStringSet("favorites", v).apply()

    var hidden: Set<String>
        get() = sp.getStringSet("hidden", emptySet())!!.toSet()
        set(v) = sp.edit().putStringSet("hidden", v).apply()

    var watchlist: Set<String>
        get() = sp.getStringSet("watchlist", emptySet())!!.toSet()
        set(v) = sp.edit().putStringSet("watchlist", v).apply()

    /** Most recently launched package names, newest first. */
    var recent: List<String>
        get() = sp.getString("recent", "")!!.split(',').filter { it.isNotBlank() }
        set(v) = sp.edit().putString("recent", v.joinToString(",")).apply()

    fun toggleFavorite(pkg: String): Boolean = toggle(pkg, favorites) { favorites = it }
    fun toggleHidden(pkg: String): Boolean = toggle(pkg, hidden) { hidden = it }
    fun toggleWatchlist(id: String): Boolean = toggle(id, watchlist) { watchlist = it }

    fun pushRecent(pkg: String) {
        recent = (listOf(pkg) + recent.filter { it != pkg }).take(8)
    }

    private fun toggle(key: String, set: Set<String>, save: (Set<String>) -> Unit): Boolean {
        val on = key !in set
        save(if (on) set + key else set - key)
        return on
    }
}
