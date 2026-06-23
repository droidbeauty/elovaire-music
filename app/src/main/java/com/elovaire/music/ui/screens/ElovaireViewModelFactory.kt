package elovaire.music.droidbeauty.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import elovaire.music.droidbeauty.app.core.AppContainer
import elovaire.music.droidbeauty.app.ui.screens.tags.AlbumTagEditorViewModel

internal class ElovaireViewModelFactory(
    private val appContainer: AppContainer,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(SearchViewModel::class.java) -> {
                SearchViewModel(
                    libraryRepository = appContainer.libraryRepository,
                    preferenceStore = appContainer.preferenceStore,
                    playbackReader = appContainer.playbackManager,
                ) as T
            }

            modelClass.isAssignableFrom(NowPlayingViewModel::class.java) -> {
                NowPlayingViewModel(
                    playbackManager = appContainer.playbackManager,
                    preferenceStore = appContainer.preferenceStore,
                    lyricsService = appContainer.lyricsService,
                ) as T
            }

            modelClass.isAssignableFrom(EqualizerViewModel::class.java) -> {
                EqualizerViewModel(
                    preferenceStore = appContainer.preferenceStore,
                ) as T
            }

            modelClass.isAssignableFrom(AlbumTagEditorViewModel::class.java) -> {
                AlbumTagEditorViewModel(
                    libraryRepository = appContainer.libraryRepository,
                    tagEditorService = appContainer.albumTagEditorService,
                ) as T
            }

            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
