package elovaire.music.droidbeauty.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import elovaire.music.droidbeauty.app.core.ElovaireViewModelDependencies
import elovaire.music.droidbeauty.app.ui.screens.tags.AlbumTagEditorViewModel

internal class ElovaireViewModelFactory(
    private val dependencies: ElovaireViewModelDependencies,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(RootViewModel::class.java) -> {
                RootViewModel(dependencies) as T
            }

            modelClass.isAssignableFrom(SearchViewModel::class.java) -> {
                SearchViewModel(
                    libraryRepository = dependencies.libraryRepository,
                    preferenceStore = dependencies.preferenceStore,
                    playbackReader = dependencies.playbackManager,
                ) as T
            }

            modelClass.isAssignableFrom(NowPlayingViewModel::class.java) -> {
                NowPlayingViewModel(
                    playbackManager = dependencies.playbackManager,
                    preferenceStore = dependencies.preferenceStore,
                    lyricsService = dependencies.lyricsService,
                ) as T
            }

            modelClass.isAssignableFrom(EqualizerViewModel::class.java) -> {
                EqualizerViewModel(
                    preferenceStore = dependencies.preferenceStore,
                ) as T
            }

            modelClass.isAssignableFrom(AlbumTagEditorViewModel::class.java) -> {
                AlbumTagEditorViewModel(
                    libraryRepository = dependencies.libraryRepository,
                    tagEditorService = dependencies.albumTagEditorService,
                ) as T
            }

            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
