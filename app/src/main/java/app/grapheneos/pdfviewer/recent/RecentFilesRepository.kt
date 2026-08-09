package app.grapheneos.pdfviewer.recent

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class RecentFile(
    val uri: String,
    val name: String,
    val lastPage: Int,
    val lastOpenedAt: Long
)

class RecentFilesRepository(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    private val _files = MutableStateFlow(load())
    val files: StateFlow<List<RecentFile>> = _files.asStateFlow()

    fun addOrUpdate(uri: String, name: String, page: Int) {
        val existing = _files.value.firstOrNull { it.uri == uri }
        val entry = RecentFile(
            uri = uri,
            name = name.ifEmpty { existing?.name.orEmpty() },
            lastPage = page.coerceAtLeast(1),
            lastOpenedAt = existing?.lastOpenedAt ?: System.currentTimeMillis()
        )
        _files.value = buildList {
            add(entry)
            addAll(_files.value.filterNot { it.uri == uri })
        }.take(MAX_ENTRIES)
        save()
    }

    fun remove(uri: String) {
        _files.value = _files.value.filterNot { it.uri == uri }
        save()
    }

    private fun load(): List<RecentFile> = try {
        json.decodeFromString<List<RecentFile>>(prefs.getString(KEY_FILES, "[]").orEmpty())
    } catch (_: Exception) {
        emptyList()
    }

    private fun save() {
        prefs.edit { putString(KEY_FILES, json.encodeToString(_files.value)) }
    }

    private companion object {
        const val PREFS_NAME = "recent_files"
        const val KEY_FILES = "files"
        const val MAX_ENTRIES = 50
    }
}
