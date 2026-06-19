package elovaire.music.droidbeauty.app.data.lyrics

import elovaire.music.droidbeauty.app.domain.model.Song

internal interface LyricsProvider {
    val providerName: String

    suspend fun findBestLyrics(
        song: Song,
        identity: LyricsIdentity,
        lookupMode: LyricsLookupMode,
    ): ProviderLyricsMatch? = null

    suspend fun search(query: LyricsSearchQuery): List<LyricsCandidate> = emptyList()

    suspend fun getLyrics(
        candidate: LyricsCandidate,
        identity: LyricsIdentity,
    ): ProviderLyricsMatch? = null
}
