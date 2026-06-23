package elovaire.music.droidbeauty.app.core

import elovaire.music.droidbeauty.app.data.library.LibraryRepository
import elovaire.music.droidbeauty.app.data.settings.PreferenceStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

internal class LibrarySettingsBridge(
    private val scope: CoroutineScope,
    private val preferenceStore: PreferenceStore,
    private val libraryRepository: LibraryRepository,
) {
    fun start() {
        scope.launch {
            preferenceStore.libraryFolderPath
                .collect(libraryRepository::setPreferredLibraryFolderPath)
        }
    }
}
