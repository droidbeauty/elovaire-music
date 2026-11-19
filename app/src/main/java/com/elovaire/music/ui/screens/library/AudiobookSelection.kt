package elovaire.music.droidbeauty.app.ui.screens

import elovaire.music.droidbeauty.app.domain.model.Audiobook
import elovaire.music.droidbeauty.app.domain.model.Song

internal fun toggleAudiobookSelection(
    selectedKeys: Set<String>,
    stableKey: String,
): Set<String> {
    return if (stableKey in selectedKeys) {
        selectedKeys - stableKey
    } else {
        selectedKeys + stableKey
    }
}

internal fun pruneAudiobookSelection(
    selectedKeys: Set<String>,
    books: List<Audiobook>,
): Set<String> {
    if (selectedKeys.isEmpty()) return emptySet()
    val availableKeys = books.mapTo(hashSetOf(), Audiobook::stableKey)
    return selectedKeys.filterTo(linkedSetOf()) { it in availableKeys }
}

internal fun selectedAudiobookSongs(
    books: List<Audiobook>,
    selectedKeys: Set<String>,
): List<Song> {
    if (selectedKeys.isEmpty()) return emptyList()
    return books
        .asSequence()
        .filter { it.stableKey in selectedKeys }
        .flatMap { it.parts.asSequence().map { part -> part.song } }
        .distinctBy(Song::id)
        .toList()
}
