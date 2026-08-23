package elovaire.music.droidbeauty.app.data.library.network

import android.content.Context
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

internal class NetworkLibrarySourceStore(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
    private val mutationLock = Any()
    private val _sources = MutableStateFlow(load())
    val sources: StateFlow<List<NetworkLibrarySource>> = _sources.asStateFlow()

    internal fun normalized(source: NetworkLibrarySource): NetworkLibrarySource = source.copy(
        name = source.name.trim().ifBlank { source.server.trim() },
        server = source.server.trim(),
        shareOrPath = NetworkPathPolicy.normalizeRelativePath(source.shareOrPath),
        username = source.username.trim(),
        credentialKey = source.credentialKey.trim().ifBlank { "network-credential-${source.id}" },
    )

    fun upsert(source: NetworkLibrarySource): NetworkLibrarySource {
        return synchronized(mutationLock) {
            val normalized = normalized(source)
            val next = (_sources.value.filterNot { it.id == normalized.id } + normalized)
                .sortedBy { it.name.lowercase() }
            save(next)
            _sources.value = next
            normalized
        }
    }

    fun create(
        name: String,
        protocol: NetworkLibraryProtocol,
        server: String,
        shareOrPath: String,
        username: String,
    ): NetworkLibrarySource {
        return NetworkLibrarySource(
            id = UUID.randomUUID().toString(),
            name = name,
            protocol = protocol,
            server = server,
            shareOrPath = shareOrPath,
            username = username,
            credentialKey = "network-credential-${UUID.randomUUID()}",
        )
    }

    fun remove(sourceId: String) {
        synchronized(mutationLock) {
            val next = _sources.value.filterNot { it.id == sourceId }
            save(next)
            _sources.value = next
        }
    }

    private fun load(): List<NetworkLibrarySource> {
        val root = runCatching {
            JSONObject(preferences.getString(KEY_SOURCES, "{}") ?: "{}")
        }.getOrNull() ?: return emptyList()
        if (root.optInt("version", 0) != STORAGE_VERSION) return emptyList()
        val array = root.optJSONArray("sources") ?: return emptyList()
        return buildList {
            repeat(array.length()) { index ->
                val item = array.optJSONObject(index) ?: return@repeat
                val protocol = runCatching {
                    NetworkLibraryProtocol.valueOf(item.optString("protocol"))
                }.getOrNull() ?: return@repeat
                val id = item.optString("id").takeIf(String::isNotBlank) ?: return@repeat
                val server = item.optString("server").trim().takeIf(String::isNotBlank) ?: return@repeat
                add(
                    NetworkLibrarySource(
                        id = id,
                        name = item.optString("name").ifBlank { server },
                        protocol = protocol,
                        server = server,
                        shareOrPath = NetworkPathPolicy.normalizeRelativePath(item.optString("shareOrPath")),
                        username = item.optString("username"),
                        credentialKey = item.optString("credentialKey").ifBlank { "network-credential-$id" },
                        enabled = item.optBoolean("enabled", true),
                    ),
                )
            }
        }.sortedBy { it.name.lowercase() }
    }

    private fun save(sources: List<NetworkLibrarySource>) {
        val array = JSONArray()
        sources.forEach { source ->
            array.put(
                JSONObject()
                    .put("id", source.id)
                    .put("name", source.name)
                    .put("protocol", source.protocol.name)
                    .put("server", source.server)
                    .put("shareOrPath", source.shareOrPath)
                    .put("username", source.username)
                    .put("credentialKey", source.credentialKey)
                    .put("enabled", source.enabled),
            )
        }
        check(preferences.edit()
            .putString(
                KEY_SOURCES,
                JSONObject()
                    .put("version", STORAGE_VERSION)
                    .put("sources", array)
                    .toString(),
            )
            .commit()) { "Unable to persist network library sources" }
    }

    private companion object {
        const val PREFERENCES = "network_library_sources_v1"
        const val KEY_SOURCES = "sources"
        const val STORAGE_VERSION = 1
    }
}
