package elovaire.music.droidbeauty.app.ui.screens

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import elovaire.music.droidbeauty.app.data.playback.EqValuePolicy
import elovaire.music.droidbeauty.app.data.playback.EqualizerDspModel
import elovaire.music.droidbeauty.app.data.playback.normalizeReverbDurationMs
import elovaire.music.droidbeauty.app.data.settings.PreferenceStore
import elovaire.music.droidbeauty.app.domain.model.EqSettings
import elovaire.music.droidbeauty.app.domain.model.ReverbProfile
import elovaire.music.droidbeauty.app.domain.model.SpaciousnessMode
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.math.roundToInt

@Immutable
internal data class EqBandUiState(
    val id: String,
    val frequencyHz: Int,
    val label: String,
    val gainDb: Float,
    val minGainDb: Float = EqValuePolicy.MIN_GAIN_DB,
    val maxGainDb: Float = EqValuePolicy.MAX_GAIN_DB,
)

@Immutable
internal data class EqualizerUiState(
    val enabled: Boolean = false,
    val bands: List<EqBandUiState> = defaultEqBandUiStates(),
    val bassBoost: Float = 0f,
    val midrange: Float = 0f,
    val treble: Float = 0f,
    val reverbDurationMs: Int = 0,
    val reverbProfile: ReverbProfile = ReverbProfile.Dry,
    val spaciousness: Float = 0f,
    val spaciousnessMode: SpaciousnessMode = SpaciousnessMode.Off,
    val mono: Boolean = false,
    val presetName: String? = null,
    val isDirty: Boolean = false,
    val isBitPerfectBlocked: Boolean = false,
) {
    fun toEqSettings(): EqSettings = EqValuePolicy.sanitize(
        EqSettings(
            bands = bands.map { EqValuePolicy.dbToNormalized(it.gainDb) },
            bass = bassBoost,
            midrange = midrange,
            treble = treble,
            spaciousness = spaciousness,
            spaciousnessMode = spaciousnessMode,
            monoEnabled = mono,
            reverbDurationMs = reverbDurationMs,
            reverbProfile = reverbProfile,
        ),
    )
}

@OptIn(kotlinx.coroutines.FlowPreview::class)
internal class EqualizerViewModel(
    private val preferenceStore: PreferenceStore,
) : ViewModel() {
    private val _uiState = MutableStateFlow(preferenceStore.eqSettings.value.toEqualizerUiState())
    val uiState: StateFlow<EqualizerUiState> = _uiState.asStateFlow()
    private val pendingSettings = MutableSharedFlow<EqSettings>(
        extraBufferCapacity = 32,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    init {
        preferenceStore.eqSettings
            .onEach { settings ->
                val sanitized = EqValuePolicy.sanitize(settings)
                if (sanitized != _uiState.value.toEqSettings()) {
                    _uiState.value = sanitized.toEqualizerUiState()
                }
            }
            .launchIn(viewModelScope)

        pendingSettings
            .debounce(40L)
            .distinctUntilChanged()
            .onEach(preferenceStore::setEqSettings)
            .launchIn(viewModelScope)
    }

    fun updateBand(index: Int, normalizedValue: Float) {
        if (index !in _uiState.value.bands.indices) return
        val gainDb = EqValuePolicy.normalizedToDb(EqValuePolicy.clampBandNormalized(normalizedValue))
        updateState(presetName = "Custom") { state ->
            state.copy(
                bands = state.bands.mapIndexed { bandIndex, band ->
                    if (bandIndex == index) band.copy(gainDb = gainDb) else band
                },
            )
        }
    }

    fun updateBass(value: Float) = updateState { it.copy(bassBoost = EqValuePolicy.clampPositiveMacro(value)) }

    fun updateMidrange(value: Float) = updateState { it.copy(midrange = EqValuePolicy.clampMacro(value)) }

    fun updateTreble(value: Float) = updateState { it.copy(treble = EqValuePolicy.clampMacro(value)) }

    fun updateSpaciousness(value: Float) = updateState {
        it.copy(spaciousness = EqValuePolicy.clampPositiveMacro(value))
    }

    fun updateSpaciousnessMode(mode: SpaciousnessMode) = updateState {
        when {
            mode == SpaciousnessMode.Off -> it.copy(spaciousnessMode = SpaciousnessMode.Off, spaciousness = 0f)
            it.spaciousnessMode == mode && it.spaciousness > 0.001f ->
                it.copy(spaciousnessMode = SpaciousnessMode.Off, spaciousness = 0f)
            else -> it.copy(spaciousnessMode = mode, spaciousness = 0.5f)
        }
    }

    fun updateReverbDuration(valueMs: Int) = updateState {
        it.copy(reverbDurationMs = normalizeReverbDurationMs(valueMs))
    }

    fun updateReverbProfile(profile: ReverbProfile) = updateState { it.copy(reverbProfile = profile) }

    fun resetReverb() = updateState {
        it.copy(reverbDurationMs = 0, reverbProfile = ReverbProfile.Dry)
    }

    fun applyPreset(name: String, settings: EqSettings) {
        val next = EqValuePolicy.sanitize(settings).toEqualizerUiState(presetName = name, isDirty = true)
        publish(next)
    }

    fun resetEffects() {
        publish(EqSettings().toEqualizerUiState(presetName = null, isDirty = true))
    }

    private fun updateState(
        presetName: String? = _uiState.value.presetName,
        transform: (EqualizerUiState) -> EqualizerUiState,
    ) {
        publish(transform(_uiState.value).copy(presetName = presetName, isDirty = true))
    }

    private fun publish(state: EqualizerUiState) {
        val settings = state.toEqSettings()
        _uiState.value = state.copy(
            enabled = EqValuePolicy.hasSignalAlteringEffects(settings),
            isBitPerfectBlocked = EqValuePolicy.hasSignalAlteringEffects(settings),
        )
        pendingSettings.tryEmit(settings)
    }

    override fun onCleared() {
        if (_uiState.value.isDirty) preferenceStore.setEqSettings(_uiState.value.toEqSettings())
        super.onCleared()
    }
}

private fun defaultEqBandUiStates(): List<EqBandUiState> {
    return List(EqualizerDspModel.BAND_COUNT) { index ->
        val frequency = EqualizerDspModel.bandDefinition(index).frequencyHz.roundToInt()
        EqBandUiState(
            id = "eq_$frequency",
            frequencyHz = frequency,
            label = if (frequency >= 1_000) "${frequency / 1_000f}k" else frequency.toString(),
            gainDb = 0f,
        )
    }
}

private fun EqSettings.toEqualizerUiState(
    presetName: String? = null,
    isDirty: Boolean = false,
): EqualizerUiState {
    val sanitized = EqValuePolicy.sanitize(this)
    val effectsActive = EqValuePolicy.hasSignalAlteringEffects(sanitized)
    return EqualizerUiState(
        enabled = effectsActive,
        bands = defaultEqBandUiStates().mapIndexed { index, band ->
            band.copy(gainDb = EqValuePolicy.normalizedToDb(sanitized.bands[index]))
        },
        bassBoost = sanitized.bass,
        midrange = sanitized.midrange,
        treble = sanitized.treble,
        reverbDurationMs = sanitized.reverbDurationMs,
        reverbProfile = sanitized.reverbProfile,
        spaciousness = sanitized.spaciousness,
        spaciousnessMode = sanitized.spaciousnessMode,
        mono = sanitized.monoEnabled,
        presetName = presetName,
        isDirty = isDirty,
        isBitPerfectBlocked = effectsActive,
    )
}
