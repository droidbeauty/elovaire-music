package elovaire.music.droidbeauty.app.ui.i18n

import elovaire.music.droidbeauty.app.domain.model.AppLanguage

internal data class UpscalerCopy(
    val title: String = "Audio upscaler",
    val enabled: String = "Enable upscaling",
    val subtitle: String = "Filtered sample-rate conversion during playback",
    val off: String = "Off",
    val multiplier: String = "Upsampling factor",
    val precision: String = "High precision filter",
    val precisionSubtitle: String = "A sharper filter with higher battery use. Balanced filtering is used when off",
    val headroom: String = "Headroom",
    val headroomSubtitle: String = "Lower the level before output to leave room for peaks created by interpolation",
    val explanation: String = "Upsampling preserves pitch, duration and audio channels. It cannot restore detail lost during compression; the highest supported factor is used, up to 384 kHz, otherwise the original rate is kept; Android may resample again for your output, and enabling this uses software processing instead of direct or offloaded playback",
)

internal data class UpscalerStateCopy(val enabled: String, val disabled: String)

internal fun upscalerStateCopy(language: AppLanguage): UpscalerStateCopy = when (language) {
    AppLanguage.Albanian -> UpscalerStateCopy("Aktivizuar", "Çaktivizuar")
    AppLanguage.Bengali -> UpscalerStateCopy("সক্রিয়", "নিষ্ক্রিয়")
    AppLanguage.Bosnian -> UpscalerStateCopy("Omogućeno", "Onemogućeno")
    AppLanguage.Bulgarian -> UpscalerStateCopy("Включено", "Изключено")
    AppLanguage.Catalan -> UpscalerStateCopy("Activat", "Desactivat")
    AppLanguage.ChineseSimplified -> UpscalerStateCopy("已启用", "已停用")
    AppLanguage.Croatian -> UpscalerStateCopy("Omogućeno", "Onemogućeno")
    AppLanguage.Czech -> UpscalerStateCopy("Zapnuto", "Vypnuto")
    AppLanguage.Danish -> UpscalerStateCopy("Aktiveret", "Deaktiveret")
    AppLanguage.Dutch -> UpscalerStateCopy("Ingeschakeld", "Uitgeschakeld")
    AppLanguage.English -> UpscalerStateCopy("Enabled", "Disabled")
    AppLanguage.Estonian -> UpscalerStateCopy("Lubatud", "Keelatud")
    AppLanguage.Finnish -> UpscalerStateCopy("Käytössä", "Ei käytössä")
    AppLanguage.French -> UpscalerStateCopy("Activé", "Désactivé")
    AppLanguage.German -> UpscalerStateCopy("Aktiviert", "Deaktiviert")
    AppLanguage.Greek -> UpscalerStateCopy("Ενεργό", "Ανενεργό")
    AppLanguage.Hindi -> UpscalerStateCopy("चालू", "बंद")
    AppLanguage.Hungarian -> UpscalerStateCopy("Bekapcsolva", "Kikapcsolva")
    AppLanguage.Indonesian -> UpscalerStateCopy("Aktif", "Nonaktif")
    AppLanguage.Italian -> UpscalerStateCopy("Attivo", "Disattivato")
    AppLanguage.Japanese -> UpscalerStateCopy("有効", "無効")
    AppLanguage.Korean -> UpscalerStateCopy("사용", "사용 안 함")
    AppLanguage.Latvian -> UpscalerStateCopy("Ieslēgts", "Izslēgts")
    AppLanguage.Lithuanian -> UpscalerStateCopy("Įjungta", "Išjungta")
    AppLanguage.Macedonian -> UpscalerStateCopy("Овозможено", "Оневозможено")
    AppLanguage.Malay -> UpscalerStateCopy("Diaktifkan", "Dilumpuhkan")
    AppLanguage.Norwegian -> UpscalerStateCopy("Aktivert", "Deaktivert")
    AppLanguage.Polish -> UpscalerStateCopy("Włączony", "Wyłączony")
    AppLanguage.Portuguese -> UpscalerStateCopy("Ativado", "Desativado")
    AppLanguage.Punjabi -> UpscalerStateCopy("ਚਾਲੂ", "ਬੰਦ")
    AppLanguage.Romanian -> UpscalerStateCopy("Activat", "Dezactivat")
    AppLanguage.Russian -> UpscalerStateCopy("Включено", "Выключено")
    AppLanguage.Serbian -> UpscalerStateCopy("Омогућено", "Онемогућено")
    AppLanguage.Slovak -> UpscalerStateCopy("Zapnuté", "Vypnuté")
    AppLanguage.Slovenian -> UpscalerStateCopy("Omogočeno", "Onemogočeno")
    AppLanguage.Spanish -> UpscalerStateCopy("Activado", "Desactivado")
    AppLanguage.Swedish -> UpscalerStateCopy("Aktiverad", "Inaktiverad")
    AppLanguage.Thai -> UpscalerStateCopy("เปิดใช้งาน", "ปิดใช้งาน")
    AppLanguage.Ukrainian -> UpscalerStateCopy("Увімкнено", "Вимкнено")
    AppLanguage.Urdu -> UpscalerStateCopy("فعال", "غیر فعال")
}

internal fun upscalerCopy(language: AppLanguage): UpscalerCopy = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.upscaler_upscalerCopy_1
    AppLanguage.Bengali -> BengaliTranslations.upscaler_upscalerCopy_1
    AppLanguage.Hindi -> HindiTranslations.upscaler_upscalerCopy_1
    AppLanguage.Malay -> MalayTranslations.upscaler_upscalerCopy_1
    AppLanguage.Thai -> ThaiTranslations.upscaler_upscalerCopy_1
    AppLanguage.Urdu -> UrduTranslations.upscaler_upscalerCopy_1
    AppLanguage.Estonian -> EstonianTranslations.upscaler_upscalerCopy_1
    AppLanguage.Greek -> GreekTranslations.upscaler_upscalerCopy_1
    AppLanguage.Hungarian -> HungarianTranslations.upscaler_upscalerCopy_1
    AppLanguage.Latvian -> LatvianTranslations.upscaler_upscalerCopy_1
    AppLanguage.Lithuanian -> LithuanianTranslations.upscaler_upscalerCopy_1
    AppLanguage.Macedonian -> MacedonianTranslations.upscaler_upscalerCopy_1
    AppLanguage.Russian -> RussianTranslations.upscaler_upscalerCopy_1
    AppLanguage.Ukrainian -> UkrainianTranslations.upscaler_upscalerCopy_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.upscaler_upscalerCopy_1
    AppLanguage.Japanese -> JapaneseTranslations.upscaler_upscalerCopy_1
    AppLanguage.Korean -> KoreanTranslations.upscaler_upscalerCopy_1
    AppLanguage.Danish -> DanishTranslations.upscaler_upscalerCopy_1
    AppLanguage.Swedish -> SwedishTranslations.upscaler_upscalerCopy_1
    AppLanguage.Norwegian -> NorwegianTranslations.upscaler_upscalerCopy_1
    AppLanguage.Czech -> CzechTranslations.upscaler_upscalerCopy_1
    AppLanguage.Slovak -> SlovakTranslations.upscaler_upscalerCopy_1
    AppLanguage.Croatian -> CroatianTranslations.upscaler_upscalerCopy_1
    AppLanguage.Serbian -> SerbianTranslations.upscaler_upscalerCopy_1
    AppLanguage.German -> GermanTranslations.upscaler_upscalerCopy_1
    AppLanguage.French -> FrenchTranslations.upscaler_upscalerCopy_1
    AppLanguage.Spanish -> SpanishTranslations.upscaler_upscalerCopy_1
    AppLanguage.Italian -> ItalianTranslations.upscaler_upscalerCopy_1
    AppLanguage.Portuguese -> PortugueseTranslations.upscaler_upscalerCopy_1
    AppLanguage.Dutch -> DutchTranslations.upscaler_upscalerCopy_1
    AppLanguage.Bulgarian -> BulgarianTranslations.upscaler_upscalerCopy_1
    AppLanguage.Catalan -> CatalanTranslations.upscaler_upscalerCopy_1
    AppLanguage.Finnish -> FinnishTranslations.upscaler_upscalerCopy_1
    AppLanguage.Romanian -> RomanianTranslations.upscaler_upscalerCopy_1
    AppLanguage.Slovenian -> SlovenianTranslations.upscaler_upscalerCopy_1
    AppLanguage.Bosnian -> BosnianTranslations.upscaler_upscalerCopy_1
    AppLanguage.Indonesian -> IndonesianTranslations.upscaler_upscalerCopy_1
    AppLanguage.Punjabi -> PunjabiTranslations.upscaler_upscalerCopy_1
    AppLanguage.Polish -> PolishTranslations.upscaler_upscalerCopy_multiline_1
    AppLanguage.English -> UpscalerCopy()
}

internal fun upscalerSetToDefault(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.upscaler_upscalerSetToDefault_1
    AppLanguage.Bengali -> BengaliTranslations.upscaler_upscalerSetToDefault_1
    AppLanguage.Bosnian -> BosnianTranslations.upscaler_upscalerSetToDefault_1
    AppLanguage.Bulgarian -> BulgarianTranslations.upscaler_upscalerSetToDefault_1
    AppLanguage.Catalan -> CatalanTranslations.upscaler_upscalerSetToDefault_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.upscaler_upscalerSetToDefault_1
    AppLanguage.Croatian -> CroatianTranslations.upscaler_upscalerSetToDefault_1
    AppLanguage.Czech -> CzechTranslations.upscaler_upscalerSetToDefault_1
    AppLanguage.Danish -> DanishTranslations.upscaler_upscalerSetToDefault_1
    AppLanguage.Dutch -> DutchTranslations.upscaler_upscalerSetToDefault_1
    AppLanguage.English -> EnglishTranslations.upscaler_upscalerSetToDefault_1
    AppLanguage.Estonian -> EstonianTranslations.upscaler_upscalerSetToDefault_1
    AppLanguage.Finnish -> FinnishTranslations.upscaler_upscalerSetToDefault_1
    AppLanguage.French -> FrenchTranslations.upscaler_upscalerSetToDefault_1
    AppLanguage.German -> GermanTranslations.upscaler_upscalerSetToDefault_1
    AppLanguage.Greek -> GreekTranslations.upscaler_upscalerSetToDefault_1
    AppLanguage.Hindi -> HindiTranslations.upscaler_upscalerSetToDefault_1
    AppLanguage.Hungarian -> HungarianTranslations.upscaler_upscalerSetToDefault_1
    AppLanguage.Indonesian -> IndonesianTranslations.upscaler_upscalerSetToDefault_1
    AppLanguage.Italian -> ItalianTranslations.upscaler_upscalerSetToDefault_1
    AppLanguage.Japanese -> JapaneseTranslations.upscaler_upscalerSetToDefault_1
    AppLanguage.Korean -> KoreanTranslations.upscaler_upscalerSetToDefault_1
    AppLanguage.Latvian -> LatvianTranslations.upscaler_upscalerSetToDefault_1
    AppLanguage.Lithuanian -> LithuanianTranslations.upscaler_upscalerSetToDefault_1
    AppLanguage.Macedonian -> MacedonianTranslations.upscaler_upscalerSetToDefault_1
    AppLanguage.Malay -> MalayTranslations.upscaler_upscalerSetToDefault_1
    AppLanguage.Norwegian -> NorwegianTranslations.upscaler_upscalerSetToDefault_1
    AppLanguage.Polish -> PolishTranslations.upscaler_upscalerSetToDefault_1
    AppLanguage.Portuguese -> PortugueseTranslations.upscaler_upscalerSetToDefault_1
    AppLanguage.Punjabi -> PunjabiTranslations.upscaler_upscalerSetToDefault_1
    AppLanguage.Romanian -> RomanianTranslations.upscaler_upscalerSetToDefault_1
    AppLanguage.Russian -> RussianTranslations.upscaler_upscalerSetToDefault_1
    AppLanguage.Serbian -> SerbianTranslations.upscaler_upscalerSetToDefault_1
    AppLanguage.Slovak -> SlovakTranslations.upscaler_upscalerSetToDefault_1
    AppLanguage.Slovenian -> SlovenianTranslations.upscaler_upscalerSetToDefault_1
    AppLanguage.Spanish -> SpanishTranslations.upscaler_upscalerSetToDefault_1
    AppLanguage.Swedish -> SwedishTranslations.upscaler_upscalerSetToDefault_1
    AppLanguage.Thai -> ThaiTranslations.upscaler_upscalerSetToDefault_1
    AppLanguage.Ukrainian -> UkrainianTranslations.upscaler_upscalerSetToDefault_1
    AppLanguage.Urdu -> UrduTranslations.upscaler_upscalerSetToDefault_1
}
