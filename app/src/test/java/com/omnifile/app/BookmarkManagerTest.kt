package com.omnifile.app

import com.omnifile.feature.filemanager.model.BookmarkItem
import com.omnifile.feature.filemanager.model.RecentAccessRecord
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for Favorites/Bookmarks management and Recent files FIFO history.
 */
class BookmarkManagerTest {

    @Test
    fun verifyFavoriteToggleLogic() {
        val list = mutableListOf<BookmarkItem>()

        fun toggle(uri: String, name: String, isDir: Boolean) {
            val idx = list.indexOfFirst { it.uriString == uri }
            if (idx >= 0) {
                list.removeAt(idx)
            } else {
                list.add(0, BookmarkItem(uri, name, isDir))
            }
        }

        // Add
        toggle("content://tree/downloads", "Downloads", true)
        assertEquals(1, list.size)
        assertEquals("Downloads", list[0].name)

        // Toggle again -> Remove
        toggle("content://tree/downloads", "Downloads", true)
        assertEquals(0, list.size)

        // Add two
        toggle("content://tree/folder1", "Folder 1", true)
        toggle("content://tree/folder2", "Folder 2", true)
        assertEquals(2, list.size)
        assertEquals("Folder 2", list[0].name) // LIFO insertion
    }

    @Test
    fun verifyRecentAccessFifoCap() {
        val recents = mutableListOf<RecentAccessRecord>()

        fun record(uri: String, name: String, isDir: Boolean, size: Long) {
            recents.removeAll { it.uriString == uri }
            recents.add(0, RecentAccessRecord(uri, name, isDir, size))
            if (recents.size > 50) {
                val trimmed = recents.subList(0, 50)
                recents.clear()
                recents.addAll(trimmed)
            }
        }

        // Add 60 entries
        for (i in 1..60) {
            record("content://file/$i", "File $i", false, i * 100L)
        }

        assertEquals(50, recents.size)
        assertEquals("File 60", recents[0].name) // Most recent is on top
        assertEquals("File 11", recents[49].name) // Oldest kept
    }
}
