package com.cine.launcher.data

import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Handler
import android.os.Looper
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors

/** A launchable application discovered through PackageManager (real label, real icon). */
class AppEntry(
    val pkg: String,
    val label: String,
    private val iconDrawable: Drawable?,
    val launchIntent: Intent?,
    val category: Category,
    val installed: Boolean = true,
    /** For not-installed placeholders: package to look up in the Play Store. */
    val storePkg: String? = null,
) {
    enum class Category { VIDEO, AUDIO, GAME, UTILITY }

    /** A fresh drawable instance so the same icon can safely appear in several views. */
    fun icon(): Drawable? = iconDrawable?.constantState?.newDrawable()?.mutate() ?: iconDrawable
}

/** Well-known streaming brands. Package names are only hints: they are validated against PackageManager. */
class Brand(val name: String, val packages: List<String>)

object Brands {
    val core = listOf(
        Brand("Netflix", listOf("com.netflix.ninja", "com.netflix.mediaclient")),
        Brand("Prime Video", listOf("com.amazon.amazonvideo.livingroom", "com.amazon.avod.thirdpartyclient", "com.amazon.avod")),
        Brand("YouTube", listOf("com.google.android.youtube.tv", "com.google.android.youtube.tvkids")),
        Brand("Disney+", listOf("com.disney.disneyplus", "com.disney.disneyplus.tv")),
        Brand("Sony LIV", listOf("com.sonyliv")),
        Brand("Spotify", listOf("com.spotify.tv.android", "com.spotify.music")),
    )

    val extra = listOf(
        "com.apple.atve.androidtv.appletv", "in.startv.hotstar", "com.jio.media.stb.ondemand",
        "com.hbo.hbonow", "com.wbd.stream", "tv.twitch.android.app", "com.plexapp.android",
        "com.google.android.videos", "com.google.android.apps.youtube.music.tv",
    )

    fun rank(pkg: String): Int {
        core.forEachIndexed { i, b -> if (pkg in b.packages) return i }
        val e = extra.indexOf(pkg)
        return if (e >= 0) core.size + e else Int.MAX_VALUE
    }
}

class AppRepository(private val context: Context) {
    interface Listener { fun onAppsChanged(apps: List<AppEntry>) }

    @Volatile var apps: List<AppEntry> = emptyList()
        private set

    private val io = Executors.newSingleThreadExecutor()
    private val main = Handler(Looper.getMainLooper())
    private val listeners = CopyOnWriteArrayList<Listener>()
    private var receiver: BroadcastReceiver? = null
    private val refreshRunnable = Runnable { refresh() }

    fun addListener(l: Listener) { listeners += l }
    fun removeListener(l: Listener) { listeners -= l }

    /** Start watching for installs/uninstalls and load the initial list. */
    fun start() {
        if (receiver == null) {
            val r = object : BroadcastReceiver() {
                override fun onReceive(c: Context?, i: Intent?) {
                    // Debounce bursts (replace = remove + add).
                    main.removeCallbacks(refreshRunnable)
                    main.postDelayed(refreshRunnable, 500)
                }
            }
            val f = IntentFilter().apply {
                addAction(Intent.ACTION_PACKAGE_ADDED)
                addAction(Intent.ACTION_PACKAGE_REMOVED)
                addAction(Intent.ACTION_PACKAGE_REPLACED)
                addAction(Intent.ACTION_PACKAGE_CHANGED)
                addDataScheme("package")
            }
            context.registerReceiver(r, f)
            receiver = r
        }
        refresh()
    }

    fun stop() {
        main.removeCallbacks(refreshRunnable)
        receiver?.let { try { context.unregisterReceiver(it) } catch (_: IllegalArgumentException) {} }
        receiver = null
    }

    fun refresh() {
        io.execute {
            val list = try { query() } catch (e: Exception) { emptyList() }
            main.post {
                apps = list
                listeners.forEach { it.onAppsChanged(list) }
            }
        }
    }

    private fun query(): List<AppEntry> {
        val pm = context.packageManager
        val found = LinkedHashMap<String, AppEntry>()
        for (cat in listOf(Intent.CATEGORY_LEANBACK_LAUNCHER, Intent.CATEGORY_LAUNCHER)) {
            val q = Intent(Intent.ACTION_MAIN).addCategory(cat)
            for (ri in pm.queryIntentActivities(q, 0)) {
                val pkg = ri.activityInfo.packageName
                if (pkg == context.packageName || pkg in found) continue
                val intent = pm.getLeanbackLaunchIntentForPackage(pkg) ?: pm.getLaunchIntentForPackage(pkg) ?: continue
                found[pkg] = AppEntry(
                    pkg = pkg,
                    label = ri.loadLabel(pm).toString(),
                    iconDrawable = ri.loadIcon(pm),
                    launchIntent = intent,
                    category = categoryOf(pkg, ri.activityInfo.applicationInfo),
                )
            }
        }
        return found.values.sortedWith(compareBy({ Brands.rank(it.pkg) }, { it.label.lowercase() }))
    }

    /** Placeholder entries (no logo, no fabricated artwork) for core brands that are not installed. */
    fun missingBrands(): List<AppEntry> {
        val installed = apps.map { it.pkg }.toSet()
        return Brands.core.filter { b -> b.packages.none { it in installed } }.map {
            AppEntry(it.packages.first(), it.name, null, null, AppEntry.Category.VIDEO, installed = false, storePkg = it.packages.first())
        }
    }

    /** Launch [entry]. Returns false (and never throws) if it cannot be launched or opened in the store. */
    fun launch(context: Context, entry: AppEntry): Boolean {
        val intent = if (entry.installed) {
            entry.launchIntent
        } else {
            entry.storePkg?.let { Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$it")) }
        } ?: return false
        return try {
            context.startActivity(Intent(intent).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED))
            true
        } catch (e: ActivityNotFoundException) {
            false
        } catch (e: SecurityException) {
            false
        }
    }

    fun openAppInfo(context: Context, entry: AppEntry): Boolean = try {
        context.startActivity(
            Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${entry.pkg}"))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
        true
    } catch (e: Exception) { false }

    private fun categoryOf(pkg: String, info: ApplicationInfo): AppEntry.Category {
        if (Brands.rank(pkg) < Brands.core.size + Brands.extra.size) {
            return if (pkg.contains("spotify") || pkg.contains("music")) AppEntry.Category.AUDIO else AppEntry.Category.VIDEO
        }
        return when (info.category) {
            ApplicationInfo.CATEGORY_VIDEO -> AppEntry.Category.VIDEO
            ApplicationInfo.CATEGORY_AUDIO -> AppEntry.Category.AUDIO
            ApplicationInfo.CATEGORY_GAME -> AppEntry.Category.GAME
            else -> if ((info.flags and ApplicationInfo.FLAG_IS_GAME) != 0) AppEntry.Category.GAME else AppEntry.Category.UTILITY
        }
    }
}
