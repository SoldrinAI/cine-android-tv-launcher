package com.cine.launcher

import com.cine.launcher.data.Brands
import com.cine.launcher.data.SampleCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CatalogTest {
    @Test fun shelvesIncludeAllRequiredRows() {
        val ids = SampleCatalog.shelves(emptySet()).map { it.id }
        assertEquals(listOf("continue", "trending", "acclaimed", "recent", "watchlist"), ids)
    }

    @Test fun watchlistShelfReflectsSavedTitles() {
        val shelf = SampleCatalog.shelves(setOf("chronos")).first { it.id == "watchlist" }
        assertEquals(listOf("chronos"), shelf.items.map { it.id })
    }

    @Test fun searchMatchesNameAndIgnoresCase() {
        assertTrue(SampleCatalog.search("SALT").any { it.id == "saltline" })
        assertTrue(SampleCatalog.search("   ").isEmpty())
    }

    @Test fun previewIsStablePerPackage() {
        assertEquals(SampleCatalog.previewFor("com.example.a"), SampleCatalog.previewFor("com.example.a"))
    }

    @Test fun brandRankPrefersCoreBrandsInOrder() {
        assertTrue(Brands.rank("com.netflix.ninja") < Brands.rank("com.google.android.youtube.tv"))
        assertEquals(Int.MAX_VALUE, Brands.rank("com.unknown.app"))
    }
}
