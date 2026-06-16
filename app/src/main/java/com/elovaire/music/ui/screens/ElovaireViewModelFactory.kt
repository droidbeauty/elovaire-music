package elovaire.music.droidbeauty.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import elovaire.music.droidbeauty.app.core.AppContainer

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
                    playbackManager = appContainer.playbackManager,
                ) as T
            }

            modelClass.isAssignableFrom(NowPlayingViewModel::class.java) -> {
                NowPlayingViewModel(
                    playbackManager = appContainer.playbackManager,
                    lyricsService = appContainer.lyricsService,
                ) as T
            }

            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
