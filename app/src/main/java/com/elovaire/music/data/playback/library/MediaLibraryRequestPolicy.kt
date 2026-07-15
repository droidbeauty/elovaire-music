package elovaire.music.droidbeauty.app.data.playback.library

internal object MediaLibraryRequestPolicy {
    fun acceptsPage(page: Int, pageSize: Int): Boolean {
        return page >= 0 && pageSize in 1..MAX_PAGE_SIZE
    }

    fun acceptsSearchQuery(query: String): Boolean {
        return query.length <= MAX_SEARCH_QUERY_LENGTH && query.none { it.isISOControl() && !it.isWhitespace() }
    }

    const val MAX_PAGE_SIZE = 500
    const val MAX_SEARCH_QUERY_LENGTH = 256
}
