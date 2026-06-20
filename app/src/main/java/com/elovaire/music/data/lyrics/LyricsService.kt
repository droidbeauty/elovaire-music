package elovaire.music.droidbeauty.app.data.lyrics

import android.content.Context
import elovaire.music.droidbeauty.app.domain.model.Song
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class LyricsService(
    context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    private val repository = LyricsRepository(
        appContext = context.applicationContext,
        ioDispatcher = ioDispatcher,
    )

    fun cachedLyrics(
        song: Song,
        includeNotFound: Boolean = true,
    ): LyricsResult? = repository.cachedLyrics(song, includeNotFound)

    fun clearCacheFor(song: Song) {
        repository.clearCacheFor(song)
    }

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

    fun lyricsForSong(
        song: Song,
        lookupMode: LyricsLookupMode = LyricsLookupMode.Full,
    ): Flow<LyricsResult> = flow {
        emit(
            repository.fetchLyrics(
                song = song,
                allowCachedNotFound = false,
                lookupMode = lookupMode,
            ),
        )
    }.catch { throwable ->
        if (throwable is CancellationException) throw throwable
        emit(LyricsResult.Timeout)
    }.flowOn(ioDispatcher)
}
