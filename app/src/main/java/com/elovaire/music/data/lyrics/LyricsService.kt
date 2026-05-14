package elovaire.music.app.data.lyrics

import android.content.Context
import elovaire.music.app.domain.model.Song
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class LyricsService(
    context: Context,
    ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    private val repository = LyricsRepository(
        appContext = context.applicationContext,
        ioDispatcher = ioDispatcher,
    )

    fun cachedLyrics(
        song: Song,
        includeNotFound: Boolean = true,
    ): LyricsResult? = repository.cachedLyrics(song, includeNotFound)

    fun isLookupInFlight(song: Song): Boolean = repository.isLookupInFlight(song)

    fun prefetchLyrics(song: Song) {
        repository.prefetchLyrics(song)
    }

    fun cancelObsoleteRequests(keepSongs: List<Song?>) {
        repository.cancelObsoleteRequests(keepSongs)
    }

    suspend fun fetchLyrics(
        song: Song,
        allowCachedNotFound: Boolean = true,
        lookupMode: LyricsLookupMode = LyricsLookupMode.Full,
    ): LyricsResult = repository.fetchLyrics(song, allowCachedNotFound, lookupMode)
}
