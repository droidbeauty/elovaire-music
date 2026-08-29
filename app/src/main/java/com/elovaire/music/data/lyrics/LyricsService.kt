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

internal interface LyricsReader {
    suspend fun cachedLyrics(song: Song, includeNotFound: Boolean = true): LyricsResult?
    suspend fun localLyrics(song: Song): LyricsResult?
    fun lyricsForSong(song: Song): Flow<LyricsResult>
}

internal interface LyricsWriter {
    suspend fun saveEmbeddedLyrics(
        song: Song,
        lyrics: String,
        operationId: String? = null,
        approvedMediaUri: android.net.Uri? = null,
    ): EmbeddedLyricsWriteResult
}

internal class LyricsService internal constructor(
    context: Context,
    mediaMutationJournal: MediaMutationJournal? = null,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val onlineLyricsEnabled: () -> Boolean = { true },
) : LyricsReader, LyricsWriter {
    private val embeddedLyricsWriter = EmbeddedLyricsWriter(context.applicationContext, mediaMutationJournal)
    private val repository = LyricsRepository(
        appContext = context.applicationContext,
        ioDispatcher = ioDispatcher,
    )

    override suspend fun cachedLyrics(
        song: Song,
        includeNotFound: Boolean,
    ): LyricsResult? = withContext(ioDispatcher) {
        repository.cachedLyrics(song, includeNotFound, onlineLyricsEnabled())
    }

    fun clearCacheFor(song: Song) {
        repository.clearCacheFor(song)
    }

    override suspend fun localLyrics(song: Song): LyricsResult? = withContext(ioDispatcher) {
        repository.localLyrics(song)
    }

    override suspend fun saveEmbeddedLyrics(
        song: Song,
        lyrics: String,
        operationId: String?,
        approvedMediaUri: android.net.Uri?,
    ): EmbeddedLyricsWriteResult = withContext(ioDispatcher) {
        embeddedLyricsWriter.write(song, lyrics, operationId, approvedMediaUri).also { result ->
            if (result is EmbeddedLyricsWriteResult.Success) {
                repository.clearCacheFor(song)
            }
        }
    }

    internal fun onMemoryPressure(pressure: MemoryPressure) {
        repository.onMemoryPressure(pressure)
    }

    suspend fun fetchLyrics(
        song: Song,
        allowCachedNotFound: Boolean = true,
    ): LyricsResult = repository.fetchLyrics(song, allowCachedNotFound, onlineLyricsEnabled())

    override fun lyricsForSong(song: Song): Flow<LyricsResult> = flow {
        emit(
            repository.fetchLyrics(
                song = song,
                allowCachedNotFound = false,
                onlineEnabled = onlineLyricsEnabled(),
            ),
        )
    }.catch { throwable ->
        if (throwable is CancellationException) throw throwable
        emit(LyricsResult.Unavailable)
    }.flowOn(ioDispatcher)
}
