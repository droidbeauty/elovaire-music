package elovaire.music.droidbeauty.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import elovaire.music.droidbeauty.app.core.ElovaireViewModelDependencies
import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import elovaire.music.droidbeauty.app.domain.model.EqSettings
import elovaire.music.droidbeauty.app.domain.model.Playlist
import elovaire.music.droidbeauty.app.domain.model.TextSizePreset
import elovaire.music.droidbeauty.app.domain.model.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

internal data class RootPreferenceState(
    val eqSettings: EqSettings,
    val themeMode: ThemeMode,
    val textSizePreset: TextSizePreset,
    val appLanguage: AppLanguage,
    val playlists: List<Playlist>,
    val favoriteSongIds: Set<Long>,
    val albumPlayCounts: Map<Long, Int>,
    val songPlayCounts: Map<Long, Int>,
    val albumCollectionLayoutModeName: String,
    val songCollectionGridEnabled: Boolean,
    val albumCollectionSortModeName: String,
    val songCollectionSortModeName: String,
)

internal class RootViewModel(
    dependencies: ElovaireViewModelDependencies,
) : ViewModel() {
    private val libraryState = combine(
        dependencies.libraryRepository.contentState,
        dependencies.libraryRepository.scanState,
        ::libraryUiStateOf,
    )

    private val playbackState = combine(
        dependencies.playbackManager.nowPlayingState,
        dependencies.playbackManager.transportState,
        dependencies.playbackManager.queueState,
        dependencies.playbackManager.volumeState,
        dependencies.playbackManager.recentPlaybackState,
        ::playbackUiStateOf,
    )

    private val preferenceState = combine(
        combine(
            dependencies.preferenceStore.eqSettings,
            dependencies.preferenceStore.themeMode,
            dependencies.preferenceStore.textSizePreset,
            dependencies.preferenceStore.appLanguage,
            dependencies.preferenceStore.playlists,
        ) { eq, theme, textSize, language, playlists ->
            PartialRootPreferenceStateA(eq, theme, textSize, language, playlists)
        },
        combine(
            dependencies.preferenceStore.favoriteSongIds,
            dependencies.preferenceStore.albumPlayCounts,
            dependencies.preferenceStore.songPlayCounts,
            dependencies.preferenceStore.albumCollectionLayoutMode,
            dependencies.preferenceStore.songCollectionGridEnabled,
        ) { favorites, albumCounts, songCounts, albumLayout, songGrid ->
            PartialRootPreferenceStateB(favorites.toHashSet(), albumCounts, songCounts, albumLayout, songGrid)
        },
        combine(
            dependencies.preferenceStore.albumCollectionSortMode,
            dependencies.preferenceStore.songCollectionSortMode,
        ) { albumSort, songSort ->
            albumSort to songSort
        },
    ) { a, b, sorts ->
        RootPreferenceState(
            eqSettings = a.eqSettings,
            themeMode = a.themeMode,
            textSizePreset = a.textSizePreset,
            appLanguage = a.appLanguage,
            playlists = a.playlists,
            favoriteSongIds = b.favoriteSongIds,
            albumPlayCounts = b.albumPlayCounts,
            songPlayCounts = b.songPlayCounts,
            albumCollectionLayoutModeName = b.albumCollectionLayoutModeName,
            songCollectionGridEnabled = b.songCollectionGridEnabled,
            albumCollectionSortModeName = sorts.first,
            songCollectionSortModeName = sorts.second,
        )
    }

    val appState: StateFlow<RootAppState> = combine(
        libraryState,
        playbackState,
        preferenceState,
        dependencies.appUpdateManager.uiState,
    ) { library, playback, prefs, update ->
        RootAppState(
            library = library,
            playback = playback,
            eqSettings = prefs.eqSettings,
            themeMode = prefs.themeMode,
            textSizePreset = prefs.textSizePreset,
            appLanguage = prefs.appLanguage,
            playlists = prefs.playlists,
            favoriteSongIds = prefs.favoriteSongIds,
            albumPlayCounts = prefs.albumPlayCounts,
            songPlayCounts = prefs.songPlayCounts,
            albumCollectionLayoutModeName = prefs.albumCollectionLayoutModeName,
            songCollectionGridEnabled = prefs.songCollectionGridEnabled,
            albumCollectionSortModeName = prefs.albumCollectionSortModeName,
            songCollectionSortModeName = prefs.songCollectionSortModeName,
            appUpdateState = update,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = RootAppState(
            library = libraryUiStateOf(
                dependencies.libraryRepository.contentState.value,
                dependencies.libraryRepository.scanState.value,
            ),
            playback = playbackUiStateOf(
                dependencies.playbackManager.nowPlayingState.value,
                dependencies.playbackManager.transportState.value,
                dependencies.playbackManager.queueState.value,
                dependencies.playbackManager.volumeState.value,
                dependencies.playbackManager.recentPlaybackState.value,
            ),
            eqSettings = dependencies.preferenceStore.eqSettings.value,
            themeMode = dependencies.preferenceStore.themeMode.value,
            textSizePreset = dependencies.preferenceStore.textSizePreset.value,
            appLanguage = dependencies.preferenceStore.appLanguage.value,
            playlists = dependencies.preferenceStore.playlists.value,
            favoriteSongIds = dependencies.preferenceStore.favoriteSongIds.value.toHashSet(),
            albumPlayCounts = dependencies.preferenceStore.albumPlayCounts.value,
            songPlayCounts = dependencies.preferenceStore.songPlayCounts.value,
            albumCollectionLayoutModeName = dependencies.preferenceStore.albumCollectionLayoutMode.value,
            songCollectionGridEnabled = dependencies.preferenceStore.songCollectionGridEnabled.value,
            albumCollectionSortModeName = dependencies.preferenceStore.albumCollectionSortMode.value,
            songCollectionSortModeName = dependencies.preferenceStore.songCollectionSortMode.value,
            appUpdateState = dependencies.appUpdateManager.uiState.value,
        ),
    )
}

private data class PartialRootPreferenceStateA(
    val eqSettings: EqSettings,
    val themeMode: ThemeMode,
    val textSizePreset: TextSizePreset,
    val appLanguage: AppLanguage,
    val playlists: List<Playlist>,
)

private data class PartialRootPreferenceStateB(
    val favoriteSongIds: Set<Long>,
    val albumPlayCounts: Map<Long, Int>,
    val songPlayCounts: Map<Long, Int>,
    val albumCollectionLayoutModeName: String,
    val songCollectionGridEnabled: Boolean,
)
