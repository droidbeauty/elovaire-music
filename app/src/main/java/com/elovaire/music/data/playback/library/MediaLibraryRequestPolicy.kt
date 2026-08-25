package elovaire.music.droidbeauty.app.data.playback.library

internal object MediaLibraryRequestPolicy {
    fun acceptsPage(page: Int, pageSize: Int): Boolean {
        return page >= 0 && pageSize in 1..MAX_PAGE_SIZE
    }

    fun acceptsSearchQuery(query: String): Boolean {
        return query.length <= MAX_SEARCH_QUERY_LENGTH && query.none { it.isISOControl() && !it.isWhitespace() }
    }

    fun acceptsStartPositionMs(positionMs: Long): Boolean {
        return positionMs == androidx.media3.common.C.TIME_UNSET ||
            positionMs in 0L..MAX_START_POSITION_MS
    }

    const val MAX_PAGE_SIZE = 500
    const val MAX_SEARCH_RESULT_ITEMS = 50_000
    const val MAX_SEARCH_QUERY_LENGTH = 256
    const val MAX_START_POSITION_MS = 7L * 24L * 60L * 60L * 1_000L
}
