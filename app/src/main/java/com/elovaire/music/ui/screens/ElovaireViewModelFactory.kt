package elovaire.music.droidbeauty.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import elovaire.music.droidbeauty.app.core.ElovaireViewModelDependencies
import elovaire.music.droidbeauty.app.ui.screens.tags.AlbumTagEditorViewModel

internal class ElovaireViewModelFactory(
    private val dependencies: ElovaireViewModelDependencies,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return createModel(modelClass, SavedStateHandle())
    }

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return createModel(modelClass, extras.createSavedStateHandle())
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : ViewModel> createModel(
        modelClass: Class<T>,
        savedStateHandle: SavedStateHandle,
    ): T {
        return when {
            modelClass.isAssignableFrom(RootViewModel::class.java) -> {
                RootViewModel(dependencies.root) as T
            }

            modelClass.isAssignableFrom(SearchViewModel::class.java) -> {
                SearchViewModel(
                    libraryRepository = dependencies.search.libraryReader,
                    preferenceStore = dependencies.search.searchSettings,
                    playbackReader = dependencies.search.playbackReader,
                    defaultDispatcher = dependencies.dispatchers.default,
                ) as T
            }

            modelClass.isAssignableFrom(NowPlayingViewModel::class.java) -> {
                NowPlayingViewModel(
                    playbackManager = dependencies.nowPlaying.playback,
                    preferenceStore = dependencies.nowPlaying.settings,
                    lyricsReader = dependencies.nowPlaying.lyricsReader,
                    lyricsWriter = dependencies.nowPlaying.lyricsWriter,
                ) as T
            }

            modelClass.isAssignableFrom(EqualizerViewModel::class.java) -> {
                EqualizerViewModel(
                    preferenceStore = dependencies.equalizer.settings,
                ) as T
            }

            modelClass.isAssignableFrom(AlbumTagEditorViewModel::class.java) -> {
                AlbumTagEditorViewModel(
                    libraryRepository = dependencies.albumTagEditor.libraryReader,
                    libraryTagUpdates = dependencies.albumTagEditor.libraryTagUpdates,
                    tagEditorService = dependencies.albumTagEditor.editor,
                    savedStateHandle = savedStateHandle,
                ) as T
            }

            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
