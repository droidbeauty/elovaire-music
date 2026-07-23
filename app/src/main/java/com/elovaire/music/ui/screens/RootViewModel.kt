package elovaire.music.droidbeauty.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import elovaire.music.droidbeauty.app.core.ElovaireViewModelDependencies
import elovaire.music.droidbeauty.app.data.smartplaylists.SmartPlaylist
import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import elovaire.music.droidbeauty.app.domain.model.EqSettings
import elovaire.music.droidbeauty.app.domain.model.Playlist
import elovaire.music.droidbeauty.app.domain.model.TextSizePreset
import elovaire.music.droidbeauty.app.domain.model.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

internal data class RootAppearanceState(
    val eqSettings: EqSettings,
    val themeMode: ThemeMode,
    val textSizePreset: TextSizePreset,
    val appLanguage: AppLanguage,
    val albumCollectionLayoutModeName: String,
    val songCollectionGridEnabled: Boolean,
    val albumCollectionSortModeName: String,
    val songCollectionSortModeName: String,
    val volumeNormalizationEnabled: Boolean,
)

internal data class RootCollectionState(
    val playlists: List<Playlist>,
    val smartPlaylists: List<SmartPlaylist>,
    val favoriteSongIds: Set<Long>,
    val albumPlayCounts: Map<Long, Int>,
    val songPlayCounts: Map<Long, Int>,
)

internal class RootViewModel(
    dependencies: ElovaireViewModelDependencies,
) : ViewModel() {
    val libraryState = combine(
        dependencies.libraryReader.contentState,
        dependencies.libraryReader.scanState,
        ::libraryUiStateOf,
    ).distinctUntilChanged().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = libraryUiStateOf(
            dependencies.libraryReader.contentState.value,
            dependencies.libraryReader.scanState.value,
        ),
    )

    val playbackState = combine(
        dependencies.playbackReader.nowPlayingState,
        dependencies.playbackReader.transportState,
        dependencies.playbackReader.queueState,
        dependencies.playbackReader.volumeState,
        dependencies.playbackReader.recentPlaybackState,
        ::playbackUiStateOf,
    ).distinctUntilChanged().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = playbackUiStateOf(
            dependencies.playbackReader.nowPlayingState.value,
            dependencies.playbackReader.transportState.value,
            dependencies.playbackReader.queueState.value,
            dependencies.playbackReader.volumeState.value,
            dependencies.playbackReader.recentPlaybackState.value,
        ),
    )

    val appearanceState = combine(
        combine(
            dependencies.rootSettingsReader.eqSettings,
            dependencies.rootSettingsReader.themeMode,
            dependencies.rootSettingsReader.textSizePreset,
            dependencies.rootSettingsReader.appLanguage,
        ) { eq, theme, textSize, language ->
            AppearanceCore(eq, theme, textSize, language)
        },
        combine(
            dependencies.rootSettingsReader.albumCollectionLayoutMode,
            dependencies.rootSettingsReader.songCollectionGridEnabled,
            dependencies.rootSettingsReader.albumCollectionSortMode,
            dependencies.rootSettingsReader.songCollectionSortMode,
        ) { albumLayout, songGrid, albumSort, songSort ->
            AppearanceLayout(albumLayout, songGrid, albumSort, songSort)
        },
        dependencies.rootSettingsReader.volumeNormalizationEnabled,
    ) { core, layout, volumeNormalization ->
        RootAppearanceState(
            eqSettings = core.eqSettings,
            themeMode = core.themeMode,
            textSizePreset = core.textSizePreset,
            appLanguage = core.appLanguage,
            albumCollectionLayoutModeName = layout.albumCollectionLayoutModeName,
            songCollectionGridEnabled = layout.songCollectionGridEnabled,
            albumCollectionSortModeName = layout.albumCollectionSortModeName,
            songCollectionSortModeName = layout.songCollectionSortModeName,
            volumeNormalizationEnabled = volumeNormalization,
        )
    }.distinctUntilChanged().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = rootAppearanceStateOf(dependencies.rootSettingsReader),
    )

    private val favoriteSongIds = dependencies.rootSettingsReader.favoriteSongIds
        .map { ids -> ids.toSet() }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = dependencies.rootSettingsReader.favoriteSongIds.value.toSet(),
        )

    val collectionState = combine(
        dependencies.rootSettingsReader.playlists,
        dependencies.rootSettingsReader.smartPlaylists,
        favoriteSongIds,
        dependencies.rootSettingsReader.albumPlayCounts,
        dependencies.rootSettingsReader.songPlayCounts,
    ) { playlists, smartPlaylists, favorites, albumCounts, songCounts ->
        RootCollectionState(
            playlists = playlists,
            smartPlaylists = smartPlaylists,
            favoriteSongIds = favorites,
            albumPlayCounts = albumCounts,
            songPlayCounts = songCounts,
        )
    }.distinctUntilChanged().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = RootCollectionState(
            playlists = dependencies.rootSettingsReader.playlists.value,
            smartPlaylists = dependencies.rootSettingsReader.smartPlaylists.value,
            favoriteSongIds = favoriteSongIds.value,
            albumPlayCounts = dependencies.rootSettingsReader.albumPlayCounts.value,
            songPlayCounts = dependencies.rootSettingsReader.songPlayCounts.value,
        ),
    )

    val appState: StateFlow<RootAppState> = combine(
        libraryState,
        playbackState,
        appearanceState,
        collectionState,
    ) { library, playback, appearance, collections ->
        RootAppState(
            library = library,
            playback = playback,
            eqSettings = appearance.eqSettings,
            themeMode = appearance.themeMode,
            textSizePreset = appearance.textSizePreset,
            appLanguage = appearance.appLanguage,
            playlists = collections.playlists,
            smartPlaylists = collections.smartPlaylists,
            favoriteSongIds = collections.favoriteSongIds,
            albumPlayCounts = collections.albumPlayCounts,
            songPlayCounts = collections.songPlayCounts,
            albumCollectionLayoutModeName = appearance.albumCollectionLayoutModeName,
            songCollectionGridEnabled = appearance.songCollectionGridEnabled,
            albumCollectionSortModeName = appearance.albumCollectionSortModeName,
            songCollectionSortModeName = appearance.songCollectionSortModeName,
            volumeNormalizationEnabled = appearance.volumeNormalizationEnabled,
        )
    }.distinctUntilChanged().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = RootAppState(
            library = libraryState.value,
            playback = playbackState.value,
            eqSettings = appearanceState.value.eqSettings,
            themeMode = appearanceState.value.themeMode,
            textSizePreset = appearanceState.value.textSizePreset,
            appLanguage = appearanceState.value.appLanguage,
            playlists = collectionState.value.playlists,
            smartPlaylists = collectionState.value.smartPlaylists,
            favoriteSongIds = collectionState.value.favoriteSongIds,
            albumPlayCounts = collectionState.value.albumPlayCounts,
            songPlayCounts = collectionState.value.songPlayCounts,
            albumCollectionLayoutModeName = appearanceState.value.albumCollectionLayoutModeName,
            songCollectionGridEnabled = appearanceState.value.songCollectionGridEnabled,
            albumCollectionSortModeName = appearanceState.value.albumCollectionSortModeName,
            songCollectionSortModeName = appearanceState.value.songCollectionSortModeName,
            volumeNormalizationEnabled = appearanceState.value.volumeNormalizationEnabled,
        ),
    )
}

private data class AppearanceCore(
    val eqSettings: EqSettings,
    val themeMode: ThemeMode,
    val textSizePreset: TextSizePreset,
    val appLanguage: AppLanguage,
)

private data class AppearanceLayout(
    val albumCollectionLayoutModeName: String,
    val songCollectionGridEnabled: Boolean,
    val albumCollectionSortModeName: String,
    val songCollectionSortModeName: String,
)

private fun rootAppearanceStateOf(settings: elovaire.music.droidbeauty.app.data.settings.RootSettingsReader) =
    RootAppearanceState(
        eqSettings = settings.eqSettings.value,
        themeMode = settings.themeMode.value,
        textSizePreset = settings.textSizePreset.value,
        appLanguage = settings.appLanguage.value,
        albumCollectionLayoutModeName = settings.albumCollectionLayoutMode.value,
        songCollectionGridEnabled = settings.songCollectionGridEnabled.value,
        albumCollectionSortModeName = settings.albumCollectionSortMode.value,
        songCollectionSortModeName = settings.songCollectionSortMode.value,
        volumeNormalizationEnabled = settings.volumeNormalizationEnabled.value,
    )
