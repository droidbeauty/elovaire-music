package elovaire.music.droidbeauty.app.data.lyrics

import android.content.Context
import elovaire.music.droidbeauty.app.core.MemoryPressure
import elovaire.music.droidbeauty.app.data.mutation.MediaMutationJournal
import elovaire.music.droidbeauty.app.domain.model.Song
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class LyricsService internal constructor(
    context: Context,
    mediaMutationJournal: MediaMutationJournal? = null,
    private val onEmbeddedLyricsChanged: (Song) -> Unit = {},
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    private val embeddedLyricsWriter = EmbeddedLyricsWriter(context.applicationContext, mediaMutationJournal)
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

    fun localLyrics(song: Song): LyricsResult? = repository.localLyrics(song)

    internal suspend fun saveEmbeddedLyrics(
        song: Song,
        lyrics: String,
        operationId: String? = null,
        approvedMediaUri: android.net.Uri? = null,
    ): EmbeddedLyricsWriteResult = withContext(ioDispatcher) {
        embeddedLyricsWriter.write(song, lyrics, operationId, approvedMediaUri).also { result ->
            if (result is EmbeddedLyricsWriteResult.Success) {
                repository.clearCacheFor(song)
                onEmbeddedLyricsChanged(song)
            }
        }
    }

    internal fun onMemoryPressure(pressure: MemoryPressure) {
        repository.onMemoryPressure(pressure)
    }

    suspend fun fetchLyrics(
        song: Song,
        allowCachedNotFound: Boolean = true,
    ): LyricsResult = repository.fetchLyrics(song, allowCachedNotFound)

    fun lyricsForSong(song: Song): Flow<LyricsResult> = flow {
        emit(
            repository.fetchLyrics(
                song = song,
                allowCachedNotFound = false,
            ),
        )
    }.catch { throwable ->
        if (throwable is CancellationException) throw throwable
        emit(LyricsResult.Timeout)
    }.flowOn(ioDispatcher)
}
