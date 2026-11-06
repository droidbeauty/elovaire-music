package elovaire.music.droidbeauty.app.ui.i18n

import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import java.util.Locale

private const val UI_CHANGES = "UI tweaks in NAS configuration, tag editing system and settings layout"
private const val SETUP_CHANGES = "UI tweaks on the initial setup splash screen"
private const val NEW_LANGUAGES = "Added support for 11 new languages - Albanian, Bosnian, Bulgarian, Catalan, Finnish, Indonesian, Romanian, Serbian, Slovenian, Punjabi and Urdu"
private const val BUG_FIXES = "General bug fixes"

internal data class ChangelogCopy(val ui: String, val languages: String, val fixes: String)

internal fun localizedChangelogItem(item: String, language: AppLanguage): String {
    if (item == SETUP_CHANGES) return localizedSetupChangelogItem(language)
    if (language == AppLanguage.English || (item != UI_CHANGES && item != NEW_LANGUAGES && item != BUG_FIXES)) return item
    val copy = when (language) {
        AppLanguage.Albanian -> AlbanianTranslations.changelog_localizedChangelogItem_1
        AppLanguage.Bengali -> BengaliTranslations.changelog_localizedChangelogItem_1
        AppLanguage.Bosnian -> BosnianTranslations.changelog_localizedChangelogItem_1
        AppLanguage.Bulgarian -> BulgarianTranslations.changelog_localizedChangelogItem_1
        AppLanguage.Catalan -> CatalanTranslations.changelog_localizedChangelogItem_1
        AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.changelog_localizedChangelogItem_1
        AppLanguage.Croatian -> CroatianTranslations.changelog_localizedChangelogItem_1
        AppLanguage.Czech -> CzechTranslations.changelog_localizedChangelogItem_1
        AppLanguage.Danish -> DanishTranslations.changelog_localizedChangelogItem_1
        AppLanguage.Dutch -> DutchTranslations.changelog_localizedChangelogItem_1
        AppLanguage.English -> error("English changelog entries are returned unchanged")
        AppLanguage.Estonian -> EstonianTranslations.changelog_localizedChangelogItem_1
        AppLanguage.Finnish -> FinnishTranslations.changelog_localizedChangelogItem_1
        AppLanguage.French -> FrenchTranslations.changelog_localizedChangelogItem_1
        AppLanguage.German -> GermanTranslations.changelog_localizedChangelogItem_1
        AppLanguage.Greek -> GreekTranslations.changelog_localizedChangelogItem_1
        AppLanguage.Hindi -> HindiTranslations.changelog_localizedChangelogItem_1
        AppLanguage.Hungarian -> HungarianTranslations.changelog_localizedChangelogItem_1
        AppLanguage.Indonesian -> IndonesianTranslations.changelog_localizedChangelogItem_1
        AppLanguage.Italian -> ItalianTranslations.changelog_localizedChangelogItem_1
        AppLanguage.Japanese -> JapaneseTranslations.changelog_localizedChangelogItem_1
        AppLanguage.Korean -> KoreanTranslations.changelog_localizedChangelogItem_1
        AppLanguage.Latvian -> LatvianTranslations.changelog_localizedChangelogItem_1
        AppLanguage.Lithuanian -> LithuanianTranslations.changelog_localizedChangelogItem_1
        AppLanguage.Macedonian -> MacedonianTranslations.changelog_localizedChangelogItem_1
        AppLanguage.Malay -> MalayTranslations.changelog_localizedChangelogItem_1
        AppLanguage.Norwegian -> NorwegianTranslations.changelog_localizedChangelogItem_1
        AppLanguage.Polish -> PolishTranslations.changelog_localizedChangelogItem_1
        AppLanguage.Portuguese -> PortugueseTranslations.changelog_localizedChangelogItem_1
        AppLanguage.Punjabi -> PunjabiTranslations.changelog_localizedChangelogItem_1
        AppLanguage.Romanian -> RomanianTranslations.changelog_localizedChangelogItem_1
        AppLanguage.Russian -> RussianTranslations.changelog_localizedChangelogItem_1
        AppLanguage.Serbian -> SerbianTranslations.changelog_localizedChangelogItem_1
        AppLanguage.Slovak -> SlovakTranslations.changelog_localizedChangelogItem_1
        AppLanguage.Slovenian -> SlovenianTranslations.changelog_localizedChangelogItem_1
        AppLanguage.Spanish -> SpanishTranslations.changelog_localizedChangelogItem_1
        AppLanguage.Swedish -> SwedishTranslations.changelog_localizedChangelogItem_1
        AppLanguage.Thai -> ThaiTranslations.changelog_localizedChangelogItem_1
        AppLanguage.Ukrainian -> UkrainianTranslations.changelog_localizedChangelogItem_1
        AppLanguage.Urdu -> UrduTranslations.changelog_localizedChangelogItem_1
    }
    return when (item) {
        UI_CHANGES -> copy.ui
        BUG_FIXES -> copy.fixes
        else -> {
            val locale = language.appLocale()
            val separator = if (language == AppLanguage.Japanese || language == AppLanguage.ChineseSimplified) "、" else ", "
            val names = listOf("sq", "bs", "bg", "ca", "fi", "id", "ro", "sr", "sl", "pa", "ur")
                .joinToString(separator) { Locale.forLanguageTag(it).getDisplayLanguage(locale) }
            "${copy.languages}: $names"
        }
    }
}

private fun localizedSetupChangelogItem(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.changelog_localizedSetupChangelogItem_1
    AppLanguage.Bengali -> BengaliTranslations.changelog_localizedSetupChangelogItem_1
    AppLanguage.Bosnian -> BosnianTranslations.changelog_localizedSetupChangelogItem_1
    AppLanguage.Bulgarian -> BulgarianTranslations.changelog_localizedSetupChangelogItem_1
    AppLanguage.Catalan -> CatalanTranslations.changelog_localizedSetupChangelogItem_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.changelog_localizedSetupChangelogItem_1
    AppLanguage.Croatian -> CroatianTranslations.changelog_localizedSetupChangelogItem_1
    AppLanguage.Czech -> CzechTranslations.changelog_localizedSetupChangelogItem_1
    AppLanguage.Danish -> DanishTranslations.changelog_localizedSetupChangelogItem_1
    AppLanguage.Dutch -> DutchTranslations.changelog_localizedSetupChangelogItem_1
    AppLanguage.English -> SETUP_CHANGES
    AppLanguage.Estonian -> EstonianTranslations.changelog_localizedSetupChangelogItem_1
    AppLanguage.Finnish -> FinnishTranslations.changelog_localizedSetupChangelogItem_1
    AppLanguage.French -> FrenchTranslations.changelog_localizedSetupChangelogItem_1
    AppLanguage.German -> GermanTranslations.changelog_localizedSetupChangelogItem_1
    AppLanguage.Greek -> GreekTranslations.changelog_localizedSetupChangelogItem_1
    AppLanguage.Hindi -> HindiTranslations.changelog_localizedSetupChangelogItem_1
    AppLanguage.Hungarian -> HungarianTranslations.changelog_localizedSetupChangelogItem_1
    AppLanguage.Indonesian -> IndonesianTranslations.changelog_localizedSetupChangelogItem_1
    AppLanguage.Italian -> ItalianTranslations.changelog_localizedSetupChangelogItem_1
    AppLanguage.Japanese -> JapaneseTranslations.changelog_localizedSetupChangelogItem_1
    AppLanguage.Korean -> KoreanTranslations.changelog_localizedSetupChangelogItem_1
    AppLanguage.Latvian -> LatvianTranslations.changelog_localizedSetupChangelogItem_1
    AppLanguage.Lithuanian -> LithuanianTranslations.changelog_localizedSetupChangelogItem_1
    AppLanguage.Macedonian -> MacedonianTranslations.changelog_localizedSetupChangelogItem_1
    AppLanguage.Malay -> MalayTranslations.changelog_localizedSetupChangelogItem_1
    AppLanguage.Norwegian -> NorwegianTranslations.changelog_localizedSetupChangelogItem_1
    AppLanguage.Polish -> PolishTranslations.changelog_localizedSetupChangelogItem_1
    AppLanguage.Portuguese -> PortugueseTranslations.changelog_localizedSetupChangelogItem_1
    AppLanguage.Punjabi -> PunjabiTranslations.changelog_localizedSetupChangelogItem_1
    AppLanguage.Romanian -> RomanianTranslations.changelog_localizedSetupChangelogItem_1
    AppLanguage.Russian -> RussianTranslations.changelog_localizedSetupChangelogItem_1
    AppLanguage.Serbian -> SerbianTranslations.changelog_localizedSetupChangelogItem_1
    AppLanguage.Slovak -> SlovakTranslations.changelog_localizedSetupChangelogItem_1
    AppLanguage.Slovenian -> SlovenianTranslations.changelog_localizedSetupChangelogItem_1
    AppLanguage.Spanish -> SpanishTranslations.changelog_localizedSetupChangelogItem_1
    AppLanguage.Swedish -> SwedishTranslations.changelog_localizedSetupChangelogItem_1
    AppLanguage.Thai -> ThaiTranslations.changelog_localizedSetupChangelogItem_1
    AppLanguage.Ukrainian -> UkrainianTranslations.changelog_localizedSetupChangelogItem_1
    AppLanguage.Urdu -> UrduTranslations.changelog_localizedSetupChangelogItem_1
}

internal fun AppLanguage.appLocale(): Locale = Locale.forLanguageTag(when (this) {
    AppLanguage.Albanian -> "sq"; AppLanguage.Bengali -> "bn"; AppLanguage.Bosnian -> "bs"
    AppLanguage.Bulgarian -> "bg"; AppLanguage.Catalan -> "ca"; AppLanguage.ChineseSimplified -> "zh-CN"
    AppLanguage.Croatian -> "hr"; AppLanguage.Czech -> "cs"; AppLanguage.Danish -> "da"; AppLanguage.Dutch -> "nl"
    AppLanguage.English -> "en"; AppLanguage.Estonian -> "et"; AppLanguage.Finnish -> "fi"; AppLanguage.French -> "fr"
    AppLanguage.German -> "de"; AppLanguage.Greek -> "el"; AppLanguage.Hindi -> "hi"; AppLanguage.Hungarian -> "hu"
    AppLanguage.Indonesian -> "id"; AppLanguage.Italian -> "it"; AppLanguage.Japanese -> "ja"; AppLanguage.Korean -> "ko"
    AppLanguage.Latvian -> "lv"; AppLanguage.Lithuanian -> "lt"; AppLanguage.Macedonian -> "mk"
    AppLanguage.Malay -> "ms"; AppLanguage.Norwegian -> "nb"; AppLanguage.Polish -> "pl"; AppLanguage.Portuguese -> "pt"
    AppLanguage.Punjabi -> "pa"; AppLanguage.Romanian -> "ro"; AppLanguage.Russian -> "ru"; AppLanguage.Serbian -> "sr"
    AppLanguage.Slovak -> "sk"; AppLanguage.Slovenian -> "sl"; AppLanguage.Spanish -> "es"; AppLanguage.Swedish -> "sv"
    AppLanguage.Thai -> "th"; AppLanguage.Ukrainian -> "uk"; AppLanguage.Urdu -> "ur"
})
