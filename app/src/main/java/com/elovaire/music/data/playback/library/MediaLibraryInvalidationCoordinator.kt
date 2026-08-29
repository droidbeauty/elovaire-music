package elovaire.music.droidbeauty.app.data.playback.library

import android.os.Handler
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaLibraryService.MediaLibrarySession
import elovaire.music.droidbeauty.app.data.library.LibraryRepository
import elovaire.music.droidbeauty.app.data.settings.RootSettingsReader
import elovaire.music.droidbeauty.app.domain.model.Playlist
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

internal data class MediaLibraryCommittedState(
    val libraryRevision: String,
    val permissionGranted: Boolean,
    val favoriteSongIds: List<Long>,
    val playlists: List<Playlist>,
)

internal object MediaLibraryInvalidationParents {
    fun changedParents(
        previous: MediaLibraryCommittedState,
        current: MediaLibraryCommittedState,
    ): List<String> {
        val changed = linkedSetOf<String>()
        if (previous.permissionGranted != current.permissionGranted) {
            changed += ElovaireMediaId.Root.value
        }
        if (previous.libraryRevision != current.libraryRevision) {
            changed += listOf(
                ElovaireMediaId.Root.value,
                ElovaireMediaId.Songs.value,
                ElovaireMediaId.Albums.value,
                ElovaireMediaId.Artists.value,
                ElovaireMediaId.Genres.value,
                ElovaireMediaId.RecentlyAdded.value,
            )
        }
        if (previous.favoriteSongIds != current.favoriteSongIds) {
            changed += ElovaireMediaId.Favorites.value
        }
        val previousPlaylists = previous.playlists.associateBy(Playlist::id)
        val currentPlaylists = current.playlists.associateBy(Playlist::id)
        if (previous.playlists != current.playlists) {
            changed += ElovaireMediaId.Playlists.value
            previousPlaylists.keys.intersect(currentPlaylists.keys).forEach { playlistId ->
                if (previousPlaylists[playlistId] != currentPlaylists[playlistId]) {
                    changed += ElovaireMediaIds.playlist(playlistId)
                }
            }
        }
        return changed.toList()
    }
}

@OptIn(UnstableApi::class, FlowPreview::class)
internal class MediaLibraryInvalidationCoordinator(
    private val session: MediaLibrarySession,
    private val libraryRepository: LibraryRepository,
    private val settings: RootSettingsReader,
    private val scope: CoroutineScope,
    private val coalesceWindowMs: Long = 25L,
) : AutoCloseable {
    private val released = AtomicBoolean(false)
    private val playerHandler = Handler(session.player.applicationLooper)
    private var observationJob: Job? = null

    @kotlin.OptIn(FlowPreview::class)
    fun start() {
        if (released.get() || observationJob != null) return
        observationJob = scope.launch {
            combine(
                libraryRepository.contentState,
                libraryRepository.scanState,
                settings.userDataSnapshot,
            ) { content, scan, userData ->
                MediaLibraryCommittedState(
                    libraryRevision = content.contentRevision,
                    permissionGranted = scan.permissionGranted,
                    favoriteSongIds = userData.favoriteSongIds,
                    playlists = userData.playlists,
                )
            }
                .distinctUntilChanged()
                .debounce(coalesceWindowMs)
                .collectRevisions()
        }
    }

    override fun close() {
        if (!released.compareAndSet(false, true)) return
        observationJob?.cancel()
        observationJob = null
        playerHandler.removeCallbacksAndMessages(null)
    }

    private suspend fun kotlinx.coroutines.flow.Flow<MediaLibraryCommittedState>.collectRevisions() {
        var previous: MediaLibraryCommittedState? = null
        collect { current ->
            val before = previous
            previous = current
            if (before == null) return@collect
            val parents = MediaLibraryInvalidationParents.changedParents(before, current)
            if (parents.isEmpty() || released.get()) return@collect
            playerHandler.post {
                if (released.get()) return@post
                parents.forEach(::notifySubscribers)
            }
        }
    }

    private fun notifySubscribers(parentId: String) {
        session.getSubscribedControllers(parentId).forEach { controller ->
            if (!released.get()) {
                session.notifyChildrenChanged(controller, parentId, Int.MAX_VALUE, null)
            }
        }
    }
}
