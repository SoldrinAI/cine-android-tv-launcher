package com.cine.launcher.data

/**
 * A title shown in the UI. All built-in titles are fictional SAMPLE content with procedurally generated art;
 * nothing implies availability on any streaming service.
 */
data class Title(
    val id: String,
    val name: String,
    val year: Int,
    val genre: String,
    val runtime: String,
    val rating: Double,
    val synopsis: String,
    val director: String,
    val cast: List<String>,
    val badge: String,
    val seed: Int,
    val progress: Float? = null,
    val remaining: String? = null,
)

data class Shelf(val id: String, val title: String, val subtitle: String, val portrait: Boolean, val items: List<Title>)

/** Pluggable content source. Replace [SampleCatalog] with a real metadata provider later. */
interface CatalogSource {
    fun featured(): List<Title>
    fun shelves(watchlist: Set<String>): List<Shelf>
    /** Sample preview for a streaming app. Must not claim real availability. */
    fun previewFor(appPackage: String): Title
    fun search(query: String): List<Title>
    fun byId(id: String): Title?
}

object SampleCatalog : CatalogSource {
    private fun t(id: String, name: String, year: Int, genre: String, runtime: String, rating: Double, synopsis: String,
                  director: String, badge: String, seed: Int, progress: Float? = null, remaining: String? = null) =
        Title(id, name, year, genre, runtime, rating, synopsis, director, listOf("Sample Actor A", "Sample Actor B", "Sample Actor C"), badge, seed, progress, remaining)

    private val all = listOf(
        t("chronos", "Chronos : Horizon", 2024, "Sci-Fi / Epic", "2h 44m", 8.9,
            "When time itself fractures across deep space colonies, a lone chronologist must navigate non-linear gravitational anomalies to avert humanity's impending temporal collapse.",
            "A. Sample", "4K ULTRA HD", 1),
        t("gilded", "The Gilded Hour", 2023, "Period Drama", "2h 08m", 8.4,
            "A fading opera house becomes the stage for one last, scandalous season in an age of gaslight and ambition.",
            "B. Sample", "DOLBY VISION", 2),
        t("saltline", "Saltline", 2022, "Thriller", "1h 51m", 7.9,
            "A coastal surveyor discovers that the tide tables she has kept for twenty years were written by someone else.",
            "C. Sample", "HDR10+", 3),
        t("ember", "Ember Protocol", 2024, "Action", "2h 02m", 7.6,
            "A retired fire marshal is pulled back for a final job inside a city that is deliberately burning.",
            "D. Sample", "IMAX ENHANCED", 4),
        t("paper", "Paper Moons", 2021, "Drama", "1h 47m", 8.1,
            "Two siblings reunite to sell the family cinema and find the reels of a film no one remembers shooting.",
            "E. Sample", "A24 STYLE", 5),
        t("velvet", "Velvet Static", 2023, "Mystery", "1h 58m", 8.0,
            "A late-night radio host begins receiving calls from listeners who haven't been born yet.",
            "F. Sample", "DOLBY ATMOS", 6),
        t("harbor", "Harbor Light", 2020, "Romance", "1h 44m", 7.8,
            "Two lighthouse keepers on opposite coasts correspond by signal lamp for one unforgettable winter.",
            "G. Sample", "4K", 7),
        t("nocturne", "Nocturne for Strings", 2022, "Biography", "2h 20m", 8.6,
            "A reclusive composer finishes the symphony that cost her everything.",
            "H. Sample", "4K HDR", 8),
        t("lowtide", "Low Tide, High Noon", 2019, "Western", "2h 11m", 8.2,
            "A gunslinger turned ferryman guards the only river crossing for three hundred miles.",
            "I. Sample", "RESTORED", 9),
        t("orbit", "Orbit of Glass", 2025, "Sci-Fi", "2h 03m", 8.3,
            "On a station built entirely of windows, nothing stays secret for long.",
            "J. Sample", "4K DOLBY", 10),
        t("quiet", "The Quiet Archive", 2021, "Documentary", "1h 36m", 8.7,
            "Inside the vault where a nation's lost films are kept alive, frame by frame.",
            "K. Sample", "HDR10+", 11),
        t("marble", "Marble & Rain", 2024, "Drama", "2h 15m", 8.5,
            "A sculptor loses her sight and begins the largest work of her life.",
            "L. Sample", "DOLBY VISION", 12),
        t("lantern", "Lantern District", 2023, "Crime", "1h 55m", 7.7,
            "Every night the same man walks through the market. Every night a different shop is robbed.",
            "M. Sample", "4K", 13),
        t("echoes", "Echoes of Arden", 2022, "Fantasy", "2h 30m", 8.0,
            "The last cartographer of a drowned kingdom redraws the map from memory.",
            "N. Sample", "IMAX ENHANCED", 14),
        t("zero", "Zero Hour Garden", 2025, "Thriller", "1h 49m", 7.5,
            "A botanist is the only witness to a crime committed in a greenhouse sealed from the inside.",
            "O. Sample", "HDR10+", 15),
        t("sundial", "Sundial", 2018, "Drama", "1h 42m", 8.3,
            "One village, one clock, and the afternoon it stopped.",
            "P. Sample", "4K", 16),
    )

    private val resume = listOf(
        all[1].copy(progress = 0.62f, remaining = "1h 12m left"),
        all[7].copy(progress = 0.78f, remaining = "24m left"),
        all[5].copy(progress = 0.40f, remaining = "45m left"),
        all[2].copy(progress = 0.15f, remaining = "18m left"),
    )

    override fun featured() = listOf(all[0], all[9], all[11], all[7])

    override fun shelves(watchlist: Set<String>): List<Shelf> = listOf(
        Shelf("continue", "Continue Watching", "${resume.size} sample sessions", false, resume),
        Shelf("trending", "Trending This Week", "Sample catalogue", true, listOf(all[3], all[8], all[4], all[9], all[12], all[13])),
        Shelf("acclaimed", "Critically Acclaimed", "Sample catalogue", true, listOf(all[0], all[10], all[7], all[11], all[15], all[1])),
        Shelf("recent", "Recently Added", "Sample catalogue", true, listOf(all[14], all[9], all[11], all[13], all[6], all[3])),
        Shelf("watchlist", "Watchlist", if (watchlist.isEmpty()) "Add titles from their detail page" else "${watchlist.size} saved", true,
            all.filter { it.id in watchlist }),
    )

    override fun previewFor(appPackage: String): Title {
        val featured = featured()
        return featured[Math.floorMod(appPackage.hashCode(), featured.size)]
    }

    override fun search(query: String): List<Title> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return emptyList()
        return all.filter { it.name.lowercase().contains(q) || it.genre.lowercase().contains(q) || it.director.lowercase().contains(q) }
    }

    override fun byId(id: String) = all.firstOrNull { it.id == id }
}
