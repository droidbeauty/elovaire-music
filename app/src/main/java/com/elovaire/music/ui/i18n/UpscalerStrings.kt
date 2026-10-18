package elovaire.music.droidbeauty.app.ui.i18n

import elovaire.music.droidbeauty.app.domain.model.AppLanguage

internal data class UpscalerCopy(
    val title: String = "Audio upscaler",
    val enabled: String = "Enable upscaling",
    val subtitle: String = "Filtered sample-rate conversion during playback",
    val off: String = "Off",
    val multiplier: String = "Upsampling factor",
    val precision: String = "High precision filter",
    val precisionSubtitle: String = "A sharper filter with higher battery use. Balanced filtering is used when off.",
    val headroom: String = "Headroom",
    val headroomSubtitle: String = "Lower the level before output to leave room for peaks created by interpolation.",
    val explanation: String = "Upsampling preserves pitch, duration and audio channels. It cannot restore detail lost during compression. The highest supported factor is used, up to 384 kHz; otherwise the original rate is kept. Android may resample again for your output. Enabling this uses software processing instead of direct or offloaded playback.",
)

internal fun upscalerCopy(language: AppLanguage): UpscalerCopy = when (language) {
    AppLanguage.Polish -> UpscalerCopy(
        title = "Upscaler dźwięku",
        enabled = "Włącz upsampling",
        subtitle = "Filtrowane zwiększanie częstotliwości próbkowania",
        off = "Wyłączony",
        multiplier = "Mnożnik próbkowania",
        precision = "Filtr o wysokiej precyzji",
        precisionSubtitle = "Dokładniejszy filtr zużywa więcej energii. Po wyłączeniu używany jest filtr zrównoważony.",
        headroom = "Zapas poziomu",
        headroomSubtitle = "Obniża poziom wyjściowy, pozostawiając miejsce na szczyty powstałe podczas interpolacji.",
        explanation = "Upsampling zachowuje wysokość dźwięku, czas trwania i kanały. Nie odtwarza szczegółów utraconych przez kompresję. Używany jest najwyższy obsługiwany mnożnik, do 384 kHz; w przeciwnym razie zachowana jest oryginalna częstotliwość. Android może ponownie zmienić próbkowanie. Włączenie używa przetwarzania programowego zamiast odtwarzania bezpośredniego lub sprzętowego.",
    )
    else -> UpscalerCopy()
}
