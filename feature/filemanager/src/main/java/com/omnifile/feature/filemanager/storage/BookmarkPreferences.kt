package com.omnifile.feature.filemanager.storage

import android.content.Context
import android.content.SharedPreferences
import com.omnifile.feature.filemanager.model.BookmarkItem
import com.omnifile.feature.filemanager.model.RecentAccessRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

/**
 * Local-first persistence manager for Bookmarks/Favorites and Recent Access History.
 * Operates offline using Android SharedPreferences with JSON serialization.
 */
class BookmarkPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("omnifile_bookmarks_prefs", Context.MODE_PRIVATE)

    private val _favoritesFlow = MutableStateFlow<List<BookmarkItem>>(loadFavorites())
    val favoritesFlow: StateFlow<List<BookmarkItem>> = _favoritesFlow.asStateFlow()

    private val _recentsFlow = MutableStateFlow<List<RecentAccessRecord>>(loadRecents())
    val recentsFlow: StateFlow<List<RecentAccessRecord>> = _recentsFlow.asStateFlow()

    fun isFavorite(uriString: String): Boolean {
        return _favoritesFlow.value.any { it.uriString == uriString }
    }

    fun toggleFavorite(uriString: String, name: String, isDirectory: Boolean) {
        val current = _favoritesFlow.value.toMutableList()
        val existingIndex = current.indexOfFirst { it.uriString == uriString }

        if (existingIndex >= 0) {
            current.removeAt(existingIndex)
        } else {
            current.add(0, BookmarkItem(uriString, name, isDirectory))
        }

        saveFavorites(current)
        _favoritesFlow.value = current
    }

    fun removeFavorite(uriString: String) {
        val current = _favoritesFlow.value.filter { it.uriString != uriString }
        saveFavorites(current)
        _favoritesFlow.value = current
    }

    fun recordRecentAccess(uriString: String, name: String, isDirectory: Boolean, sizeBytes: Long) {
        val current = _recentsFlow.value.toMutableList()
        // Remove existing duplicate to push it to the top
        current.removeAll { it.uriString == uriString }
        current.add(0, RecentAccessRecord(uriString, name, isDirectory, sizeBytes))

        // Keep maximum 50 most recent records
        val trimmed = if (current.size > 50) current.subList(0, 50) else current
        saveRecents(trimmed)
        _recentsFlow.value = trimmed
    }

    fun clearRecents() {
        saveRecents(emptyList())
        _recentsFlow.value = emptyList()
    }

    private fun loadFavorites(): List<BookmarkItem> {
        val jsonStr = prefs.getString("favorites_json", null) ?: return emptyList()
        val list = mutableListOf<BookmarkItem>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    BookmarkItem(
                        uriString = obj.getString("uriString"),
                        name = obj.getString("name"),
                        isDirectory = obj.optBoolean("isDirectory", false),
                        addedTimeMillis = obj.optLong("addedTimeMillis", System.currentTimeMillis())
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }

    private fun saveFavorites(items: List<BookmarkItem>) {
        val array = JSONArray()
        for (item in items) {
            val obj = JSONObject().apply {
                put("uriString", item.uriString)
                put("name", item.name)
                put("isDirectory", item.isDirectory)
                put("addedTimeMillis", item.addedTimeMillis)
            }
            array.put(obj)
        }
        prefs.edit().putString("favorites_json", array.toString()).apply()
    }

    private fun loadRecents(): List<RecentAccessRecord> {
        val jsonStr = prefs.getString("recents_json", null) ?: return emptyList()
        val list = mutableListOf<RecentAccessRecord>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    RecentAccessRecord(
                        uriString = obj.getString("uriString"),
                        name = obj.getString("name"),
                        isDirectory = obj.optBoolean("isDirectory", false),
                        sizeBytes = obj.optLong("sizeBytes", 0L),
                        accessTimeMillis = obj.optLong("accessTimeMillis", System.currentTimeMillis())
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }

    private fun saveRecents(items: List<RecentAccessRecord>) {
        val array = JSONArray()
        for (item in items) {
            val obj = JSONObject().apply {
                put("uriString", item.uriString)
                put("name", item.name)
                put("isDirectory", item.isDirectory)
                put("sizeBytes", item.sizeBytes)
                put("accessTimeMillis", item.accessTimeMillis)
            }
            array.put(obj)
        }
        prefs.edit().putString("recents_json", array.toString()).apply()
    }
}
