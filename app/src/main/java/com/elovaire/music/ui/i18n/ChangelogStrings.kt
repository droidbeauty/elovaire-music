package elovaire.music.droidbeauty.app.ui.i18n

import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import java.util.Locale

internal fun localizedChangelogItem(item: String, @Suppress("UNUSED_PARAMETER") language: AppLanguage): String = item

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
