package elovaire.music.droidbeauty.app.ui.i18n

import elovaire.music.droidbeauty.app.domain.model.AppLanguage

internal fun localizedResourceTitle(title: String, language: AppLanguage): String {
    if (title != "Lucide icons" && title != "Geist font") return title
    val (icons, font) = when (language) {
        AppLanguage.Albanian -> AlbanianTranslations.aboutUpdate_localizedResourceTitle_variant_1 to AlbanianTranslations.aboutUpdate_localizedResourceTitle_variant_2
        AppLanguage.Bengali -> BengaliTranslations.aboutUpdate_localizedResourceTitle_variant_1 to BengaliTranslations.aboutUpdate_localizedResourceTitle_variant_2
        AppLanguage.Bosnian -> BosnianTranslations.aboutUpdate_localizedResourceTitle_variant_1 to BosnianTranslations.aboutUpdate_localizedResourceTitle_variant_2
        AppLanguage.Bulgarian -> BulgarianTranslations.aboutUpdate_localizedResourceTitle_variant_1 to BulgarianTranslations.aboutUpdate_localizedResourceTitle_variant_2
        AppLanguage.Catalan -> CatalanTranslations.aboutUpdate_localizedResourceTitle_variant_1 to CatalanTranslations.aboutUpdate_localizedResourceTitle_variant_2
        AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.aboutUpdate_localizedResourceTitle_variant_1 to ChineseSimplifiedTranslations.aboutUpdate_localizedResourceTitle_variant_2
        AppLanguage.Croatian -> CroatianTranslations.aboutUpdate_localizedResourceTitle_variant_1 to CroatianTranslations.aboutUpdate_localizedResourceTitle_variant_2
        AppLanguage.Czech -> CzechTranslations.aboutUpdate_localizedResourceTitle_variant_1 to CzechTranslations.aboutUpdate_localizedResourceTitle_variant_2
        AppLanguage.Danish -> DanishTranslations.aboutUpdate_localizedResourceTitle_variant_1 to DanishTranslations.aboutUpdate_localizedResourceTitle_variant_2
        AppLanguage.Dutch -> DutchTranslations.aboutUpdate_localizedResourceTitle_variant_1 to DutchTranslations.aboutUpdate_localizedResourceTitle_variant_2
        AppLanguage.English -> EnglishTranslations.aboutUpdate_localizedResourceTitle_variant_1 to EnglishTranslations.aboutUpdate_localizedResourceTitle_variant_2
        AppLanguage.Estonian -> EstonianTranslations.aboutUpdate_localizedResourceTitle_variant_1 to EstonianTranslations.aboutUpdate_localizedResourceTitle_variant_2
        AppLanguage.Finnish -> FinnishTranslations.aboutUpdate_localizedResourceTitle_variant_1 to FinnishTranslations.aboutUpdate_localizedResourceTitle_variant_2
        AppLanguage.French -> FrenchTranslations.aboutUpdate_localizedResourceTitle_variant_1 to FrenchTranslations.aboutUpdate_localizedResourceTitle_variant_2
        AppLanguage.German -> GermanTranslations.aboutUpdate_localizedResourceTitle_variant_1 to GermanTranslations.aboutUpdate_localizedResourceTitle_variant_2
        AppLanguage.Greek -> GreekTranslations.aboutUpdate_localizedResourceTitle_variant_1 to GreekTranslations.aboutUpdate_localizedResourceTitle_variant_2
        AppLanguage.Hindi -> HindiTranslations.aboutUpdate_localizedResourceTitle_variant_1 to HindiTranslations.aboutUpdate_localizedResourceTitle_variant_2
        AppLanguage.Hungarian -> HungarianTranslations.aboutUpdate_localizedResourceTitle_variant_1 to HungarianTranslations.aboutUpdate_localizedResourceTitle_variant_2
        AppLanguage.Indonesian -> IndonesianTranslations.aboutUpdate_localizedResourceTitle_variant_1 to IndonesianTranslations.aboutUpdate_localizedResourceTitle_variant_2
        AppLanguage.Italian -> ItalianTranslations.aboutUpdate_localizedResourceTitle_variant_1 to ItalianTranslations.aboutUpdate_localizedResourceTitle_variant_2
        AppLanguage.Japanese -> JapaneseTranslations.aboutUpdate_localizedResourceTitle_variant_1 to JapaneseTranslations.aboutUpdate_localizedResourceTitle_variant_2
        AppLanguage.Korean -> KoreanTranslations.aboutUpdate_localizedResourceTitle_variant_1 to KoreanTranslations.aboutUpdate_localizedResourceTitle_variant_2
        AppLanguage.Latvian -> LatvianTranslations.aboutUpdate_localizedResourceTitle_variant_1 to LatvianTranslations.aboutUpdate_localizedResourceTitle_variant_2
        AppLanguage.Lithuanian -> LithuanianTranslations.aboutUpdate_localizedResourceTitle_variant_1 to LithuanianTranslations.aboutUpdate_localizedResourceTitle_variant_2
        AppLanguage.Macedonian -> MacedonianTranslations.aboutUpdate_localizedResourceTitle_variant_1 to MacedonianTranslations.aboutUpdate_localizedResourceTitle_variant_2
        AppLanguage.Malay -> MalayTranslations.aboutUpdate_localizedResourceTitle_variant_1 to MalayTranslations.aboutUpdate_localizedResourceTitle_variant_2
        AppLanguage.Norwegian -> NorwegianTranslations.aboutUpdate_localizedResourceTitle_variant_1 to NorwegianTranslations.aboutUpdate_localizedResourceTitle_variant_2
        AppLanguage.Polish -> PolishTranslations.aboutUpdate_localizedResourceTitle_variant_1 to PolishTranslations.aboutUpdate_localizedResourceTitle_variant_2
        AppLanguage.Portuguese -> PortugueseTranslations.aboutUpdate_localizedResourceTitle_variant_1 to PortugueseTranslations.aboutUpdate_localizedResourceTitle_variant_2
        AppLanguage.Punjabi -> PunjabiTranslations.aboutUpdate_localizedResourceTitle_variant_1 to PunjabiTranslations.aboutUpdate_localizedResourceTitle_variant_2
        AppLanguage.Romanian -> RomanianTranslations.aboutUpdate_localizedResourceTitle_variant_1 to RomanianTranslations.aboutUpdate_localizedResourceTitle_variant_2
        AppLanguage.Russian -> RussianTranslations.aboutUpdate_localizedResourceTitle_variant_1 to RussianTranslations.aboutUpdate_localizedResourceTitle_variant_2
        AppLanguage.Serbian -> SerbianTranslations.aboutUpdate_localizedResourceTitle_variant_1 to SerbianTranslations.aboutUpdate_localizedResourceTitle_variant_2
        AppLanguage.Slovak -> SlovakTranslations.aboutUpdate_localizedResourceTitle_variant_1 to SlovakTranslations.aboutUpdate_localizedResourceTitle_variant_2
        AppLanguage.Slovenian -> SlovenianTranslations.aboutUpdate_localizedResourceTitle_variant_1 to SlovenianTranslations.aboutUpdate_localizedResourceTitle_variant_2
        AppLanguage.Spanish -> SpanishTranslations.aboutUpdate_localizedResourceTitle_variant_1 to SpanishTranslations.aboutUpdate_localizedResourceTitle_variant_2
        AppLanguage.Swedish -> SwedishTranslations.aboutUpdate_localizedResourceTitle_variant_1 to SwedishTranslations.aboutUpdate_localizedResourceTitle_variant_2
        AppLanguage.Thai -> ThaiTranslations.aboutUpdate_localizedResourceTitle_variant_1 to ThaiTranslations.aboutUpdate_localizedResourceTitle_variant_2
        AppLanguage.Ukrainian -> UkrainianTranslations.aboutUpdate_localizedResourceTitle_variant_1 to UkrainianTranslations.aboutUpdate_localizedResourceTitle_variant_2
        AppLanguage.Urdu -> UrduTranslations.aboutUpdate_localizedResourceTitle_variant_1 to UrduTranslations.aboutUpdate_localizedResourceTitle_variant_2
    }
    return if (title == "Lucide icons") icons else font
}

internal fun droidBeautyDescription(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.aboutUpdate_droidBeautyDescription_1
    AppLanguage.Bengali -> BengaliTranslations.aboutUpdate_droidBeautyDescription_1
    AppLanguage.Bosnian -> BosnianTranslations.aboutUpdate_droidBeautyDescription_1
    AppLanguage.Bulgarian -> BulgarianTranslations.aboutUpdate_droidBeautyDescription_1
    AppLanguage.Catalan -> CatalanTranslations.aboutUpdate_droidBeautyDescription_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.aboutUpdate_droidBeautyDescription_1
    AppLanguage.Croatian -> CroatianTranslations.aboutUpdate_droidBeautyDescription_1
    AppLanguage.Czech -> CzechTranslations.aboutUpdate_droidBeautyDescription_1
    AppLanguage.Danish -> DanishTranslations.aboutUpdate_droidBeautyDescription_1
    AppLanguage.Dutch -> DutchTranslations.aboutUpdate_droidBeautyDescription_1
    AppLanguage.English -> EnglishTranslations.aboutUpdate_droidBeautyDescription_1
    AppLanguage.Estonian -> EstonianTranslations.aboutUpdate_droidBeautyDescription_1
    AppLanguage.Finnish -> FinnishTranslations.aboutUpdate_droidBeautyDescription_1
    AppLanguage.French -> FrenchTranslations.aboutUpdate_droidBeautyDescription_1
    AppLanguage.German -> GermanTranslations.aboutUpdate_droidBeautyDescription_1
    AppLanguage.Greek -> GreekTranslations.aboutUpdate_droidBeautyDescription_1
    AppLanguage.Hindi -> HindiTranslations.aboutUpdate_droidBeautyDescription_1
    AppLanguage.Hungarian -> HungarianTranslations.aboutUpdate_droidBeautyDescription_1
    AppLanguage.Indonesian -> IndonesianTranslations.aboutUpdate_droidBeautyDescription_1
    AppLanguage.Italian -> ItalianTranslations.aboutUpdate_droidBeautyDescription_1
    AppLanguage.Japanese -> JapaneseTranslations.aboutUpdate_droidBeautyDescription_1
    AppLanguage.Korean -> KoreanTranslations.aboutUpdate_droidBeautyDescription_1
    AppLanguage.Latvian -> LatvianTranslations.aboutUpdate_droidBeautyDescription_1
    AppLanguage.Lithuanian -> LithuanianTranslations.aboutUpdate_droidBeautyDescription_1
    AppLanguage.Macedonian -> MacedonianTranslations.aboutUpdate_droidBeautyDescription_1
    AppLanguage.Malay -> MalayTranslations.aboutUpdate_droidBeautyDescription_1
    AppLanguage.Norwegian -> NorwegianTranslations.aboutUpdate_droidBeautyDescription_1
    AppLanguage.Polish -> PolishTranslations.aboutUpdate_droidBeautyDescription_1
    AppLanguage.Portuguese -> PortugueseTranslations.aboutUpdate_droidBeautyDescription_1
    AppLanguage.Punjabi -> PunjabiTranslations.aboutUpdate_droidBeautyDescription_1
    AppLanguage.Romanian -> RomanianTranslations.aboutUpdate_droidBeautyDescription_1
    AppLanguage.Russian -> RussianTranslations.aboutUpdate_droidBeautyDescription_1
    AppLanguage.Serbian -> SerbianTranslations.aboutUpdate_droidBeautyDescription_1
    AppLanguage.Slovak -> SlovakTranslations.aboutUpdate_droidBeautyDescription_1
    AppLanguage.Slovenian -> SlovenianTranslations.aboutUpdate_droidBeautyDescription_1
    AppLanguage.Spanish -> SpanishTranslations.aboutUpdate_droidBeautyDescription_1
    AppLanguage.Swedish -> SwedishTranslations.aboutUpdate_droidBeautyDescription_1
    AppLanguage.Thai -> ThaiTranslations.aboutUpdate_droidBeautyDescription_1
    AppLanguage.Ukrainian -> UkrainianTranslations.aboutUpdate_droidBeautyDescription_1
    AppLanguage.Urdu -> UrduTranslations.aboutUpdate_droidBeautyDescription_1
}

internal fun bundledLicensesLabel(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.aboutUpdate_bundledLicensesLabel_1
    AppLanguage.Bengali -> BengaliTranslations.aboutUpdate_bundledLicensesLabel_1
    AppLanguage.Bosnian -> BosnianTranslations.aboutUpdate_bundledLicensesLabel_1
    AppLanguage.Bulgarian -> BulgarianTranslations.aboutUpdate_bundledLicensesLabel_1
    AppLanguage.Catalan -> CatalanTranslations.aboutUpdate_bundledLicensesLabel_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.aboutUpdate_bundledLicensesLabel_1
    AppLanguage.Croatian -> CroatianTranslations.aboutUpdate_bundledLicensesLabel_1
    AppLanguage.Czech -> CzechTranslations.aboutUpdate_bundledLicensesLabel_1
    AppLanguage.Danish -> DanishTranslations.aboutUpdate_bundledLicensesLabel_1
    AppLanguage.Dutch -> DutchTranslations.aboutUpdate_bundledLicensesLabel_1
    AppLanguage.English -> EnglishTranslations.aboutUpdate_bundledLicensesLabel_1
    AppLanguage.Estonian -> EstonianTranslations.aboutUpdate_bundledLicensesLabel_1
    AppLanguage.Finnish -> FinnishTranslations.aboutUpdate_bundledLicensesLabel_1
    AppLanguage.French -> FrenchTranslations.aboutUpdate_bundledLicensesLabel_1
    AppLanguage.German -> GermanTranslations.aboutUpdate_bundledLicensesLabel_1
    AppLanguage.Greek -> GreekTranslations.aboutUpdate_bundledLicensesLabel_1
    AppLanguage.Hindi -> HindiTranslations.aboutUpdate_bundledLicensesLabel_1
    AppLanguage.Hungarian -> HungarianTranslations.aboutUpdate_bundledLicensesLabel_1
    AppLanguage.Indonesian -> IndonesianTranslations.aboutUpdate_bundledLicensesLabel_1
    AppLanguage.Italian -> ItalianTranslations.aboutUpdate_bundledLicensesLabel_1
    AppLanguage.Japanese -> JapaneseTranslations.aboutUpdate_bundledLicensesLabel_1
    AppLanguage.Korean -> KoreanTranslations.aboutUpdate_bundledLicensesLabel_1
    AppLanguage.Latvian -> LatvianTranslations.aboutUpdate_bundledLicensesLabel_1
    AppLanguage.Lithuanian -> LithuanianTranslations.aboutUpdate_bundledLicensesLabel_1
    AppLanguage.Macedonian -> MacedonianTranslations.aboutUpdate_bundledLicensesLabel_1
    AppLanguage.Malay -> MalayTranslations.aboutUpdate_bundledLicensesLabel_1
    AppLanguage.Norwegian -> NorwegianTranslations.aboutUpdate_bundledLicensesLabel_1
    AppLanguage.Polish -> PolishTranslations.aboutUpdate_bundledLicensesLabel_1
    AppLanguage.Portuguese -> PortugueseTranslations.aboutUpdate_bundledLicensesLabel_1
    AppLanguage.Punjabi -> PunjabiTranslations.aboutUpdate_bundledLicensesLabel_1
    AppLanguage.Romanian -> RomanianTranslations.aboutUpdate_bundledLicensesLabel_1
    AppLanguage.Russian -> RussianTranslations.aboutUpdate_bundledLicensesLabel_1
    AppLanguage.Serbian -> SerbianTranslations.aboutUpdate_bundledLicensesLabel_1
    AppLanguage.Slovak -> SlovakTranslations.aboutUpdate_bundledLicensesLabel_1
    AppLanguage.Slovenian -> SlovenianTranslations.aboutUpdate_bundledLicensesLabel_1
    AppLanguage.Spanish -> SpanishTranslations.aboutUpdate_bundledLicensesLabel_1
    AppLanguage.Swedish -> SwedishTranslations.aboutUpdate_bundledLicensesLabel_1
    AppLanguage.Thai -> ThaiTranslations.aboutUpdate_bundledLicensesLabel_1
    AppLanguage.Ukrainian -> UkrainianTranslations.aboutUpdate_bundledLicensesLabel_1
    AppLanguage.Urdu -> UrduTranslations.aboutUpdate_bundledLicensesLabel_1
}

internal fun localizedUpdateError(message: String, language: AppLanguage): String {
    if (message == "Update download is too large" || message == "Update download exceeded its byte limit") return updateDownloadTooLarge(language)
    if (message == "The staged update is no longer available") return stagedUpdateUnavailable(language)
    if (message == "Allow installing updates from this source first") return updateInstallPermission(language)
    if (message == "Update download failed") return updateDownloadFailed(language)
    if (message == "Downloaded update is incomplete") return updateDownloadIncomplete(language)
    if (message == "Update verification failed") return updateVerificationFailed(language)
    if (message == "Unable to verify update") return updateVerificationUnavailable(language)
    if (message == "Downloaded update targets another app") return updateTargetsAnotherApp(language)
    if (message == "Downloaded update version is invalid") return updateVersionInvalid(language)
    if (message == "Downloaded update is not newer") return updateNotNewer(language)
    if (message == "Downloaded update is not a valid app") return updateNotValidApp(language)
    if (message == "Downloaded update is invalid") return updateDownloadInvalid(language)
    if (message == "Unable to check for updates") return updateCheckUnavailable(language)
    if (message == "GitHub update check failed") return githubUpdateCheckFailed(language)
    if (message == "Update source is invalid") return updateSourceInvalid(language)
    if (message == "Update asset name is invalid") return updateFileNameInvalid(language)
    if (message == "Too many update redirects") return tooManyUpdateRedirects(language)
    if (message == "Update source redirect is invalid") return updateRedirectInvalid(language)
    if (message == "Unable to open install permission settings") return updatePermissionSettingsUnavailable(language)
    if (message == "Unable to persist update installer handoff") return updateHandoffSaveFailed(language)
    if (message == "Downloaded update is signed by an unknown certificate") return updateUnknownCertificate(language)
    if (message != "The update was not installed") return message
    return when (language) {
        AppLanguage.Albanian -> AlbanianTranslations.aboutUpdate_localizedUpdateError_1
        AppLanguage.Bengali -> BengaliTranslations.aboutUpdate_localizedUpdateError_1
        AppLanguage.Bosnian -> BosnianTranslations.aboutUpdate_localizedUpdateError_1
        AppLanguage.Bulgarian -> BulgarianTranslations.aboutUpdate_localizedUpdateError_1
        AppLanguage.Catalan -> CatalanTranslations.aboutUpdate_localizedUpdateError_1
        AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.aboutUpdate_localizedUpdateError_1
        AppLanguage.Croatian -> CroatianTranslations.aboutUpdate_localizedUpdateError_1
        AppLanguage.Czech -> CzechTranslations.aboutUpdate_localizedUpdateError_1
        AppLanguage.Danish -> DanishTranslations.aboutUpdate_localizedUpdateError_1
        AppLanguage.Dutch -> DutchTranslations.aboutUpdate_localizedUpdateError_1
        AppLanguage.English -> EnglishTranslations.aboutUpdate_localizedUpdateError_1
        AppLanguage.Estonian -> EstonianTranslations.aboutUpdate_localizedUpdateError_1
        AppLanguage.Finnish -> FinnishTranslations.aboutUpdate_localizedUpdateError_1
        AppLanguage.French -> FrenchTranslations.aboutUpdate_localizedUpdateError_1
        AppLanguage.German -> GermanTranslations.aboutUpdate_localizedUpdateError_1
        AppLanguage.Greek -> GreekTranslations.aboutUpdate_localizedUpdateError_1
        AppLanguage.Hindi -> HindiTranslations.aboutUpdate_localizedUpdateError_1
        AppLanguage.Hungarian -> HungarianTranslations.aboutUpdate_localizedUpdateError_1
        AppLanguage.Indonesian -> IndonesianTranslations.aboutUpdate_localizedUpdateError_1
        AppLanguage.Italian -> ItalianTranslations.aboutUpdate_localizedUpdateError_1
        AppLanguage.Japanese -> JapaneseTranslations.aboutUpdate_localizedUpdateError_1
        AppLanguage.Korean -> KoreanTranslations.aboutUpdate_localizedUpdateError_1
        AppLanguage.Latvian -> LatvianTranslations.aboutUpdate_localizedUpdateError_1
        AppLanguage.Lithuanian -> LithuanianTranslations.aboutUpdate_localizedUpdateError_1
        AppLanguage.Macedonian -> MacedonianTranslations.aboutUpdate_localizedUpdateError_1
        AppLanguage.Malay -> MalayTranslations.aboutUpdate_localizedUpdateError_1
        AppLanguage.Norwegian -> NorwegianTranslations.aboutUpdate_localizedUpdateError_1
        AppLanguage.Polish -> PolishTranslations.aboutUpdate_localizedUpdateError_1
        AppLanguage.Portuguese -> PortugueseTranslations.aboutUpdate_localizedUpdateError_1
        AppLanguage.Punjabi -> PunjabiTranslations.aboutUpdate_localizedUpdateError_1
        AppLanguage.Romanian -> RomanianTranslations.aboutUpdate_localizedUpdateError_1
        AppLanguage.Russian -> RussianTranslations.aboutUpdate_localizedUpdateError_1
        AppLanguage.Serbian -> SerbianTranslations.aboutUpdate_localizedUpdateError_1
        AppLanguage.Slovak -> SlovakTranslations.aboutUpdate_localizedUpdateError_1
        AppLanguage.Slovenian -> SlovenianTranslations.aboutUpdate_localizedUpdateError_1
        AppLanguage.Spanish -> SpanishTranslations.aboutUpdate_localizedUpdateError_1
        AppLanguage.Swedish -> SwedishTranslations.aboutUpdate_localizedUpdateError_1
        AppLanguage.Thai -> ThaiTranslations.aboutUpdate_localizedUpdateError_1
        AppLanguage.Ukrainian -> UkrainianTranslations.aboutUpdate_localizedUpdateError_1
        AppLanguage.Urdu -> UrduTranslations.aboutUpdate_localizedUpdateError_1
    }
}

private fun updateDownloadTooLarge(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.aboutUpdate_updateDownloadTooLarge_1
    AppLanguage.Bengali -> BengaliTranslations.aboutUpdate_updateDownloadTooLarge_1
    AppLanguage.Bosnian -> BosnianTranslations.aboutUpdate_updateDownloadTooLarge_1
    AppLanguage.Bulgarian -> BulgarianTranslations.aboutUpdate_updateDownloadTooLarge_1
    AppLanguage.Catalan -> CatalanTranslations.aboutUpdate_updateDownloadTooLarge_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.aboutUpdate_updateDownloadTooLarge_1
    AppLanguage.Croatian -> CroatianTranslations.aboutUpdate_updateDownloadTooLarge_1
    AppLanguage.Czech -> CzechTranslations.aboutUpdate_updateDownloadTooLarge_1
    AppLanguage.Danish -> DanishTranslations.aboutUpdate_updateDownloadTooLarge_1
    AppLanguage.Dutch -> DutchTranslations.aboutUpdate_updateDownloadTooLarge_1
    AppLanguage.English -> EnglishTranslations.aboutUpdate_updateDownloadTooLarge_1
    AppLanguage.Estonian -> EstonianTranslations.aboutUpdate_updateDownloadTooLarge_1
    AppLanguage.Finnish -> FinnishTranslations.aboutUpdate_updateDownloadTooLarge_1
    AppLanguage.French -> FrenchTranslations.aboutUpdate_updateDownloadTooLarge_1
    AppLanguage.German -> GermanTranslations.aboutUpdate_updateDownloadTooLarge_1
    AppLanguage.Greek -> GreekTranslations.aboutUpdate_updateDownloadTooLarge_1
    AppLanguage.Hindi -> HindiTranslations.aboutUpdate_updateDownloadTooLarge_1
    AppLanguage.Hungarian -> HungarianTranslations.aboutUpdate_updateDownloadTooLarge_1
    AppLanguage.Indonesian -> IndonesianTranslations.aboutUpdate_updateDownloadTooLarge_1
    AppLanguage.Italian -> ItalianTranslations.aboutUpdate_updateDownloadTooLarge_1
    AppLanguage.Japanese -> JapaneseTranslations.aboutUpdate_updateDownloadTooLarge_1
    AppLanguage.Korean -> KoreanTranslations.aboutUpdate_updateDownloadTooLarge_1
    AppLanguage.Latvian -> LatvianTranslations.aboutUpdate_updateDownloadTooLarge_1
    AppLanguage.Lithuanian -> LithuanianTranslations.aboutUpdate_updateDownloadTooLarge_1
    AppLanguage.Macedonian -> MacedonianTranslations.aboutUpdate_updateDownloadTooLarge_1
    AppLanguage.Malay -> MalayTranslations.aboutUpdate_updateDownloadTooLarge_1
    AppLanguage.Norwegian -> NorwegianTranslations.aboutUpdate_updateDownloadTooLarge_1
    AppLanguage.Polish -> PolishTranslations.aboutUpdate_updateDownloadTooLarge_1
    AppLanguage.Portuguese -> PortugueseTranslations.aboutUpdate_updateDownloadTooLarge_1
    AppLanguage.Punjabi -> PunjabiTranslations.aboutUpdate_updateDownloadTooLarge_1
    AppLanguage.Romanian -> RomanianTranslations.aboutUpdate_updateDownloadTooLarge_1
    AppLanguage.Russian -> RussianTranslations.aboutUpdate_updateDownloadTooLarge_1
    AppLanguage.Serbian -> SerbianTranslations.aboutUpdate_updateDownloadTooLarge_1
    AppLanguage.Slovak -> SlovakTranslations.aboutUpdate_updateDownloadTooLarge_1
    AppLanguage.Slovenian -> SlovenianTranslations.aboutUpdate_updateDownloadTooLarge_1
    AppLanguage.Spanish -> SpanishTranslations.aboutUpdate_updateDownloadTooLarge_1
    AppLanguage.Swedish -> SwedishTranslations.aboutUpdate_updateDownloadTooLarge_1
    AppLanguage.Thai -> ThaiTranslations.aboutUpdate_updateDownloadTooLarge_1
    AppLanguage.Ukrainian -> UkrainianTranslations.aboutUpdate_updateDownloadTooLarge_1
    AppLanguage.Urdu -> UrduTranslations.aboutUpdate_updateDownloadTooLarge_1
}

private fun updateHandoffSaveFailed(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.aboutUpdate_updateHandoffSaveFailed_1
    AppLanguage.Bengali -> BengaliTranslations.aboutUpdate_updateHandoffSaveFailed_1
    AppLanguage.Bosnian -> BosnianTranslations.aboutUpdate_updateHandoffSaveFailed_1
    AppLanguage.Bulgarian -> BulgarianTranslations.aboutUpdate_updateHandoffSaveFailed_1
    AppLanguage.Catalan -> CatalanTranslations.aboutUpdate_updateHandoffSaveFailed_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.aboutUpdate_updateHandoffSaveFailed_1
    AppLanguage.Croatian -> CroatianTranslations.aboutUpdate_updateHandoffSaveFailed_1
    AppLanguage.Czech -> CzechTranslations.aboutUpdate_updateHandoffSaveFailed_1
    AppLanguage.Danish -> DanishTranslations.aboutUpdate_updateHandoffSaveFailed_1
    AppLanguage.Dutch -> DutchTranslations.aboutUpdate_updateHandoffSaveFailed_1
    AppLanguage.English -> EnglishTranslations.aboutUpdate_updateHandoffSaveFailed_1
    AppLanguage.Estonian -> EstonianTranslations.aboutUpdate_updateHandoffSaveFailed_1
    AppLanguage.Finnish -> FinnishTranslations.aboutUpdate_updateHandoffSaveFailed_1
    AppLanguage.French -> FrenchTranslations.aboutUpdate_updateHandoffSaveFailed_1
    AppLanguage.German -> GermanTranslations.aboutUpdate_updateHandoffSaveFailed_1
    AppLanguage.Greek -> GreekTranslations.aboutUpdate_updateHandoffSaveFailed_1
    AppLanguage.Hindi -> HindiTranslations.aboutUpdate_updateHandoffSaveFailed_1
    AppLanguage.Hungarian -> HungarianTranslations.aboutUpdate_updateHandoffSaveFailed_1
    AppLanguage.Indonesian -> IndonesianTranslations.aboutUpdate_updateHandoffSaveFailed_1
    AppLanguage.Italian -> ItalianTranslations.aboutUpdate_updateHandoffSaveFailed_1
    AppLanguage.Japanese -> JapaneseTranslations.aboutUpdate_updateHandoffSaveFailed_1
    AppLanguage.Korean -> KoreanTranslations.aboutUpdate_updateHandoffSaveFailed_1
    AppLanguage.Latvian -> LatvianTranslations.aboutUpdate_updateHandoffSaveFailed_1
    AppLanguage.Lithuanian -> LithuanianTranslations.aboutUpdate_updateHandoffSaveFailed_1
    AppLanguage.Macedonian -> MacedonianTranslations.aboutUpdate_updateHandoffSaveFailed_1
    AppLanguage.Malay -> MalayTranslations.aboutUpdate_updateHandoffSaveFailed_1
    AppLanguage.Norwegian -> NorwegianTranslations.aboutUpdate_updateHandoffSaveFailed_1
    AppLanguage.Polish -> PolishTranslations.aboutUpdate_updateHandoffSaveFailed_1
    AppLanguage.Portuguese -> PortugueseTranslations.aboutUpdate_updateHandoffSaveFailed_1
    AppLanguage.Punjabi -> PunjabiTranslations.aboutUpdate_updateHandoffSaveFailed_1
    AppLanguage.Romanian -> RomanianTranslations.aboutUpdate_updateHandoffSaveFailed_1
    AppLanguage.Russian -> RussianTranslations.aboutUpdate_updateHandoffSaveFailed_1
    AppLanguage.Serbian -> SerbianTranslations.aboutUpdate_updateHandoffSaveFailed_1
    AppLanguage.Slovak -> SlovakTranslations.aboutUpdate_updateHandoffSaveFailed_1
    AppLanguage.Slovenian -> SlovenianTranslations.aboutUpdate_updateHandoffSaveFailed_1
    AppLanguage.Spanish -> SpanishTranslations.aboutUpdate_updateHandoffSaveFailed_1
    AppLanguage.Swedish -> SwedishTranslations.aboutUpdate_updateHandoffSaveFailed_1
    AppLanguage.Thai -> ThaiTranslations.aboutUpdate_updateHandoffSaveFailed_1
    AppLanguage.Ukrainian -> UkrainianTranslations.aboutUpdate_updateHandoffSaveFailed_1
    AppLanguage.Urdu -> UrduTranslations.aboutUpdate_updateHandoffSaveFailed_1
}

private fun updatePermissionSettingsUnavailable(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.aboutUpdate_updatePermissionSettingsUnavailable_1
    AppLanguage.Bengali -> BengaliTranslations.aboutUpdate_updatePermissionSettingsUnavailable_1
    AppLanguage.Bosnian -> BosnianTranslations.aboutUpdate_updatePermissionSettingsUnavailable_1
    AppLanguage.Bulgarian -> BulgarianTranslations.aboutUpdate_updatePermissionSettingsUnavailable_1
    AppLanguage.Catalan -> CatalanTranslations.aboutUpdate_updatePermissionSettingsUnavailable_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.aboutUpdate_updatePermissionSettingsUnavailable_1
    AppLanguage.Croatian -> CroatianTranslations.aboutUpdate_updatePermissionSettingsUnavailable_1
    AppLanguage.Czech -> CzechTranslations.aboutUpdate_updatePermissionSettingsUnavailable_1
    AppLanguage.Danish -> DanishTranslations.aboutUpdate_updatePermissionSettingsUnavailable_1
    AppLanguage.Dutch -> DutchTranslations.aboutUpdate_updatePermissionSettingsUnavailable_1
    AppLanguage.English -> EnglishTranslations.aboutUpdate_updatePermissionSettingsUnavailable_1
    AppLanguage.Estonian -> EstonianTranslations.aboutUpdate_updatePermissionSettingsUnavailable_1
    AppLanguage.Finnish -> FinnishTranslations.aboutUpdate_updatePermissionSettingsUnavailable_1
    AppLanguage.French -> FrenchTranslations.aboutUpdate_updatePermissionSettingsUnavailable_1
    AppLanguage.German -> GermanTranslations.aboutUpdate_updatePermissionSettingsUnavailable_1
    AppLanguage.Greek -> GreekTranslations.aboutUpdate_updatePermissionSettingsUnavailable_1
    AppLanguage.Hindi -> HindiTranslations.aboutUpdate_updatePermissionSettingsUnavailable_1
    AppLanguage.Hungarian -> HungarianTranslations.aboutUpdate_updatePermissionSettingsUnavailable_1
    AppLanguage.Indonesian -> IndonesianTranslations.aboutUpdate_updatePermissionSettingsUnavailable_1
    AppLanguage.Italian -> ItalianTranslations.aboutUpdate_updatePermissionSettingsUnavailable_1
    AppLanguage.Japanese -> JapaneseTranslations.aboutUpdate_updatePermissionSettingsUnavailable_1
    AppLanguage.Korean -> KoreanTranslations.aboutUpdate_updatePermissionSettingsUnavailable_1
    AppLanguage.Latvian -> LatvianTranslations.aboutUpdate_updatePermissionSettingsUnavailable_1
    AppLanguage.Lithuanian -> LithuanianTranslations.aboutUpdate_updatePermissionSettingsUnavailable_1
    AppLanguage.Macedonian -> MacedonianTranslations.aboutUpdate_updatePermissionSettingsUnavailable_1
    AppLanguage.Malay -> MalayTranslations.aboutUpdate_updatePermissionSettingsUnavailable_1
    AppLanguage.Norwegian -> NorwegianTranslations.aboutUpdate_updatePermissionSettingsUnavailable_1
    AppLanguage.Polish -> PolishTranslations.aboutUpdate_updatePermissionSettingsUnavailable_1
    AppLanguage.Portuguese -> PortugueseTranslations.aboutUpdate_updatePermissionSettingsUnavailable_1
    AppLanguage.Punjabi -> PunjabiTranslations.aboutUpdate_updatePermissionSettingsUnavailable_1
    AppLanguage.Romanian -> RomanianTranslations.aboutUpdate_updatePermissionSettingsUnavailable_1
    AppLanguage.Russian -> RussianTranslations.aboutUpdate_updatePermissionSettingsUnavailable_1
    AppLanguage.Serbian -> SerbianTranslations.aboutUpdate_updatePermissionSettingsUnavailable_1
    AppLanguage.Slovak -> SlovakTranslations.aboutUpdate_updatePermissionSettingsUnavailable_1
    AppLanguage.Slovenian -> SlovenianTranslations.aboutUpdate_updatePermissionSettingsUnavailable_1
    AppLanguage.Spanish -> SpanishTranslations.aboutUpdate_updatePermissionSettingsUnavailable_1
    AppLanguage.Swedish -> SwedishTranslations.aboutUpdate_updatePermissionSettingsUnavailable_1
    AppLanguage.Thai -> ThaiTranslations.aboutUpdate_updatePermissionSettingsUnavailable_1
    AppLanguage.Ukrainian -> UkrainianTranslations.aboutUpdate_updatePermissionSettingsUnavailable_1
    AppLanguage.Urdu -> UrduTranslations.aboutUpdate_updatePermissionSettingsUnavailable_1
}

private fun updateRedirectInvalid(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.aboutUpdate_updateRedirectInvalid_1
    AppLanguage.Bengali -> BengaliTranslations.aboutUpdate_updateRedirectInvalid_1
    AppLanguage.Bosnian -> BosnianTranslations.aboutUpdate_updateRedirectInvalid_1
    AppLanguage.Bulgarian -> BulgarianTranslations.aboutUpdate_updateRedirectInvalid_1
    AppLanguage.Catalan -> CatalanTranslations.aboutUpdate_updateRedirectInvalid_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.aboutUpdate_updateRedirectInvalid_1
    AppLanguage.Croatian -> CroatianTranslations.aboutUpdate_updateRedirectInvalid_1
    AppLanguage.Czech -> CzechTranslations.aboutUpdate_updateRedirectInvalid_1
    AppLanguage.Danish -> DanishTranslations.aboutUpdate_updateRedirectInvalid_1
    AppLanguage.Dutch -> DutchTranslations.aboutUpdate_updateRedirectInvalid_1
    AppLanguage.English -> EnglishTranslations.aboutUpdate_updateRedirectInvalid_1
    AppLanguage.Estonian -> EstonianTranslations.aboutUpdate_updateRedirectInvalid_1
    AppLanguage.Finnish -> FinnishTranslations.aboutUpdate_updateRedirectInvalid_1
    AppLanguage.French -> FrenchTranslations.aboutUpdate_updateRedirectInvalid_1
    AppLanguage.German -> GermanTranslations.aboutUpdate_updateRedirectInvalid_1
    AppLanguage.Greek -> GreekTranslations.aboutUpdate_updateRedirectInvalid_1
    AppLanguage.Hindi -> HindiTranslations.aboutUpdate_updateRedirectInvalid_1
    AppLanguage.Hungarian -> HungarianTranslations.aboutUpdate_updateRedirectInvalid_1
    AppLanguage.Indonesian -> IndonesianTranslations.aboutUpdate_updateRedirectInvalid_1
    AppLanguage.Italian -> ItalianTranslations.aboutUpdate_updateRedirectInvalid_1
    AppLanguage.Japanese -> JapaneseTranslations.aboutUpdate_updateRedirectInvalid_1
    AppLanguage.Korean -> KoreanTranslations.aboutUpdate_updateRedirectInvalid_1
    AppLanguage.Latvian -> LatvianTranslations.aboutUpdate_updateRedirectInvalid_1
    AppLanguage.Lithuanian -> LithuanianTranslations.aboutUpdate_updateRedirectInvalid_1
    AppLanguage.Macedonian -> MacedonianTranslations.aboutUpdate_updateRedirectInvalid_1
    AppLanguage.Malay -> MalayTranslations.aboutUpdate_updateRedirectInvalid_1
    AppLanguage.Norwegian -> NorwegianTranslations.aboutUpdate_updateRedirectInvalid_1
    AppLanguage.Polish -> PolishTranslations.aboutUpdate_updateRedirectInvalid_1
    AppLanguage.Portuguese -> PortugueseTranslations.aboutUpdate_updateRedirectInvalid_1
    AppLanguage.Punjabi -> PunjabiTranslations.aboutUpdate_updateRedirectInvalid_1
    AppLanguage.Romanian -> RomanianTranslations.aboutUpdate_updateRedirectInvalid_1
    AppLanguage.Russian -> RussianTranslations.aboutUpdate_updateRedirectInvalid_1
    AppLanguage.Serbian -> SerbianTranslations.aboutUpdate_updateRedirectInvalid_1
    AppLanguage.Slovak -> SlovakTranslations.aboutUpdate_updateRedirectInvalid_1
    AppLanguage.Slovenian -> SlovenianTranslations.aboutUpdate_updateRedirectInvalid_1
    AppLanguage.Spanish -> SpanishTranslations.aboutUpdate_updateRedirectInvalid_1
    AppLanguage.Swedish -> SwedishTranslations.aboutUpdate_updateRedirectInvalid_1
    AppLanguage.Thai -> ThaiTranslations.aboutUpdate_updateRedirectInvalid_1
    AppLanguage.Ukrainian -> UkrainianTranslations.aboutUpdate_updateRedirectInvalid_1
    AppLanguage.Urdu -> UrduTranslations.aboutUpdate_updateRedirectInvalid_1
}

private fun tooManyUpdateRedirects(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.aboutUpdate_tooManyUpdateRedirects_1
    AppLanguage.Bengali -> BengaliTranslations.aboutUpdate_tooManyUpdateRedirects_1
    AppLanguage.Bosnian -> BosnianTranslations.aboutUpdate_tooManyUpdateRedirects_1
    AppLanguage.Bulgarian -> BulgarianTranslations.aboutUpdate_tooManyUpdateRedirects_1
    AppLanguage.Catalan -> CatalanTranslations.aboutUpdate_tooManyUpdateRedirects_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.aboutUpdate_tooManyUpdateRedirects_1
    AppLanguage.Croatian -> CroatianTranslations.aboutUpdate_tooManyUpdateRedirects_1
    AppLanguage.Czech -> CzechTranslations.aboutUpdate_tooManyUpdateRedirects_1
    AppLanguage.Danish -> DanishTranslations.aboutUpdate_tooManyUpdateRedirects_1
    AppLanguage.Dutch -> DutchTranslations.aboutUpdate_tooManyUpdateRedirects_1
    AppLanguage.English -> EnglishTranslations.aboutUpdate_tooManyUpdateRedirects_1
    AppLanguage.Estonian -> EstonianTranslations.aboutUpdate_tooManyUpdateRedirects_1
    AppLanguage.Finnish -> FinnishTranslations.aboutUpdate_tooManyUpdateRedirects_1
    AppLanguage.French -> FrenchTranslations.aboutUpdate_tooManyUpdateRedirects_1
    AppLanguage.German -> GermanTranslations.aboutUpdate_tooManyUpdateRedirects_1
    AppLanguage.Greek -> GreekTranslations.aboutUpdate_tooManyUpdateRedirects_1
    AppLanguage.Hindi -> HindiTranslations.aboutUpdate_tooManyUpdateRedirects_1
    AppLanguage.Hungarian -> HungarianTranslations.aboutUpdate_tooManyUpdateRedirects_1
    AppLanguage.Indonesian -> IndonesianTranslations.aboutUpdate_tooManyUpdateRedirects_1
    AppLanguage.Italian -> ItalianTranslations.aboutUpdate_tooManyUpdateRedirects_1
    AppLanguage.Japanese -> JapaneseTranslations.aboutUpdate_tooManyUpdateRedirects_1
    AppLanguage.Korean -> KoreanTranslations.aboutUpdate_tooManyUpdateRedirects_1
    AppLanguage.Latvian -> LatvianTranslations.aboutUpdate_tooManyUpdateRedirects_1
    AppLanguage.Lithuanian -> LithuanianTranslations.aboutUpdate_tooManyUpdateRedirects_1
    AppLanguage.Macedonian -> MacedonianTranslations.aboutUpdate_tooManyUpdateRedirects_1
    AppLanguage.Malay -> MalayTranslations.aboutUpdate_tooManyUpdateRedirects_1
    AppLanguage.Norwegian -> NorwegianTranslations.aboutUpdate_tooManyUpdateRedirects_1
    AppLanguage.Polish -> PolishTranslations.aboutUpdate_tooManyUpdateRedirects_1
    AppLanguage.Portuguese -> PortugueseTranslations.aboutUpdate_tooManyUpdateRedirects_1
    AppLanguage.Punjabi -> PunjabiTranslations.aboutUpdate_tooManyUpdateRedirects_1
    AppLanguage.Romanian -> RomanianTranslations.aboutUpdate_tooManyUpdateRedirects_1
    AppLanguage.Russian -> RussianTranslations.aboutUpdate_tooManyUpdateRedirects_1
    AppLanguage.Serbian -> SerbianTranslations.aboutUpdate_tooManyUpdateRedirects_1
    AppLanguage.Slovak -> SlovakTranslations.aboutUpdate_tooManyUpdateRedirects_1
    AppLanguage.Slovenian -> SlovenianTranslations.aboutUpdate_tooManyUpdateRedirects_1
    AppLanguage.Spanish -> SpanishTranslations.aboutUpdate_tooManyUpdateRedirects_1
    AppLanguage.Swedish -> SwedishTranslations.aboutUpdate_tooManyUpdateRedirects_1
    AppLanguage.Thai -> ThaiTranslations.aboutUpdate_tooManyUpdateRedirects_1
    AppLanguage.Ukrainian -> UkrainianTranslations.aboutUpdate_tooManyUpdateRedirects_1
    AppLanguage.Urdu -> UrduTranslations.aboutUpdate_tooManyUpdateRedirects_1
}

private fun updateFileNameInvalid(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.aboutUpdate_updateFileNameInvalid_1
    AppLanguage.Bengali -> BengaliTranslations.aboutUpdate_updateFileNameInvalid_1
    AppLanguage.Bosnian -> BosnianTranslations.aboutUpdate_updateFileNameInvalid_1
    AppLanguage.Bulgarian -> BulgarianTranslations.aboutUpdate_updateFileNameInvalid_1
    AppLanguage.Catalan -> CatalanTranslations.aboutUpdate_updateFileNameInvalid_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.aboutUpdate_updateFileNameInvalid_1
    AppLanguage.Croatian -> CroatianTranslations.aboutUpdate_updateFileNameInvalid_1
    AppLanguage.Czech -> CzechTranslations.aboutUpdate_updateFileNameInvalid_1
    AppLanguage.Danish -> DanishTranslations.aboutUpdate_updateFileNameInvalid_1
    AppLanguage.Dutch -> DutchTranslations.aboutUpdate_updateFileNameInvalid_1
    AppLanguage.English -> EnglishTranslations.aboutUpdate_updateFileNameInvalid_1
    AppLanguage.Estonian -> EstonianTranslations.aboutUpdate_updateFileNameInvalid_1
    AppLanguage.Finnish -> FinnishTranslations.aboutUpdate_updateFileNameInvalid_1
    AppLanguage.French -> FrenchTranslations.aboutUpdate_updateFileNameInvalid_1
    AppLanguage.German -> GermanTranslations.aboutUpdate_updateFileNameInvalid_1
    AppLanguage.Greek -> GreekTranslations.aboutUpdate_updateFileNameInvalid_1
    AppLanguage.Hindi -> HindiTranslations.aboutUpdate_updateFileNameInvalid_1
    AppLanguage.Hungarian -> HungarianTranslations.aboutUpdate_updateFileNameInvalid_1
    AppLanguage.Indonesian -> IndonesianTranslations.aboutUpdate_updateFileNameInvalid_1
    AppLanguage.Italian -> ItalianTranslations.aboutUpdate_updateFileNameInvalid_1
    AppLanguage.Japanese -> JapaneseTranslations.aboutUpdate_updateFileNameInvalid_1
    AppLanguage.Korean -> KoreanTranslations.aboutUpdate_updateFileNameInvalid_1
    AppLanguage.Latvian -> LatvianTranslations.aboutUpdate_updateFileNameInvalid_1
    AppLanguage.Lithuanian -> LithuanianTranslations.aboutUpdate_updateFileNameInvalid_1
    AppLanguage.Macedonian -> MacedonianTranslations.aboutUpdate_updateFileNameInvalid_1
    AppLanguage.Malay -> MalayTranslations.aboutUpdate_updateFileNameInvalid_1
    AppLanguage.Norwegian -> NorwegianTranslations.aboutUpdate_updateFileNameInvalid_1
    AppLanguage.Polish -> PolishTranslations.aboutUpdate_updateFileNameInvalid_1
    AppLanguage.Portuguese -> PortugueseTranslations.aboutUpdate_updateFileNameInvalid_1
    AppLanguage.Punjabi -> PunjabiTranslations.aboutUpdate_updateFileNameInvalid_1
    AppLanguage.Romanian -> RomanianTranslations.aboutUpdate_updateFileNameInvalid_1
    AppLanguage.Russian -> RussianTranslations.aboutUpdate_updateFileNameInvalid_1
    AppLanguage.Serbian -> SerbianTranslations.aboutUpdate_updateFileNameInvalid_1
    AppLanguage.Slovak -> SlovakTranslations.aboutUpdate_updateFileNameInvalid_1
    AppLanguage.Slovenian -> SlovenianTranslations.aboutUpdate_updateFileNameInvalid_1
    AppLanguage.Spanish -> SpanishTranslations.aboutUpdate_updateFileNameInvalid_1
    AppLanguage.Swedish -> SwedishTranslations.aboutUpdate_updateFileNameInvalid_1
    AppLanguage.Thai -> ThaiTranslations.aboutUpdate_updateFileNameInvalid_1
    AppLanguage.Ukrainian -> UkrainianTranslations.aboutUpdate_updateFileNameInvalid_1
    AppLanguage.Urdu -> UrduTranslations.aboutUpdate_updateFileNameInvalid_1
}

private fun updateSourceInvalid(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.aboutUpdate_updateSourceInvalid_1
    AppLanguage.Bengali -> BengaliTranslations.aboutUpdate_updateSourceInvalid_1
    AppLanguage.Bosnian -> BosnianTranslations.aboutUpdate_updateSourceInvalid_1
    AppLanguage.Bulgarian -> BulgarianTranslations.aboutUpdate_updateSourceInvalid_1
    AppLanguage.Catalan -> CatalanTranslations.aboutUpdate_updateSourceInvalid_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.aboutUpdate_updateSourceInvalid_1
    AppLanguage.Croatian -> CroatianTranslations.aboutUpdate_updateSourceInvalid_1
    AppLanguage.Czech -> CzechTranslations.aboutUpdate_updateSourceInvalid_1
    AppLanguage.Danish -> DanishTranslations.aboutUpdate_updateSourceInvalid_1
    AppLanguage.Dutch -> DutchTranslations.aboutUpdate_updateSourceInvalid_1
    AppLanguage.English -> EnglishTranslations.aboutUpdate_updateSourceInvalid_1
    AppLanguage.Estonian -> EstonianTranslations.aboutUpdate_updateSourceInvalid_1
    AppLanguage.Finnish -> FinnishTranslations.aboutUpdate_updateSourceInvalid_1
    AppLanguage.French -> FrenchTranslations.aboutUpdate_updateSourceInvalid_1
    AppLanguage.German -> GermanTranslations.aboutUpdate_updateSourceInvalid_1
    AppLanguage.Greek -> GreekTranslations.aboutUpdate_updateSourceInvalid_1
    AppLanguage.Hindi -> HindiTranslations.aboutUpdate_updateSourceInvalid_1
    AppLanguage.Hungarian -> HungarianTranslations.aboutUpdate_updateSourceInvalid_1
    AppLanguage.Indonesian -> IndonesianTranslations.aboutUpdate_updateSourceInvalid_1
    AppLanguage.Italian -> ItalianTranslations.aboutUpdate_updateSourceInvalid_1
    AppLanguage.Japanese -> JapaneseTranslations.aboutUpdate_updateSourceInvalid_1
    AppLanguage.Korean -> KoreanTranslations.aboutUpdate_updateSourceInvalid_1
    AppLanguage.Latvian -> LatvianTranslations.aboutUpdate_updateSourceInvalid_1
    AppLanguage.Lithuanian -> LithuanianTranslations.aboutUpdate_updateSourceInvalid_1
    AppLanguage.Macedonian -> MacedonianTranslations.aboutUpdate_updateSourceInvalid_1
    AppLanguage.Malay -> MalayTranslations.aboutUpdate_updateSourceInvalid_1
    AppLanguage.Norwegian -> NorwegianTranslations.aboutUpdate_updateSourceInvalid_1
    AppLanguage.Polish -> PolishTranslations.aboutUpdate_updateSourceInvalid_1
    AppLanguage.Portuguese -> PortugueseTranslations.aboutUpdate_updateSourceInvalid_1
    AppLanguage.Punjabi -> PunjabiTranslations.aboutUpdate_updateSourceInvalid_1
    AppLanguage.Romanian -> RomanianTranslations.aboutUpdate_updateSourceInvalid_1
    AppLanguage.Russian -> RussianTranslations.aboutUpdate_updateSourceInvalid_1
    AppLanguage.Serbian -> SerbianTranslations.aboutUpdate_updateSourceInvalid_1
    AppLanguage.Slovak -> SlovakTranslations.aboutUpdate_updateSourceInvalid_1
    AppLanguage.Slovenian -> SlovenianTranslations.aboutUpdate_updateSourceInvalid_1
    AppLanguage.Spanish -> SpanishTranslations.aboutUpdate_updateSourceInvalid_1
    AppLanguage.Swedish -> SwedishTranslations.aboutUpdate_updateSourceInvalid_1
    AppLanguage.Thai -> ThaiTranslations.aboutUpdate_updateSourceInvalid_1
    AppLanguage.Ukrainian -> UkrainianTranslations.aboutUpdate_updateSourceInvalid_1
    AppLanguage.Urdu -> UrduTranslations.aboutUpdate_updateSourceInvalid_1
}

private fun githubUpdateCheckFailed(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.aboutUpdate_githubUpdateCheckFailed_1
    AppLanguage.Bengali -> BengaliTranslations.aboutUpdate_githubUpdateCheckFailed_1
    AppLanguage.Bosnian -> BosnianTranslations.aboutUpdate_githubUpdateCheckFailed_1
    AppLanguage.Bulgarian -> BulgarianTranslations.aboutUpdate_githubUpdateCheckFailed_1
    AppLanguage.Catalan -> CatalanTranslations.aboutUpdate_githubUpdateCheckFailed_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.aboutUpdate_githubUpdateCheckFailed_1
    AppLanguage.Croatian -> CroatianTranslations.aboutUpdate_githubUpdateCheckFailed_1
    AppLanguage.Czech -> CzechTranslations.aboutUpdate_githubUpdateCheckFailed_1
    AppLanguage.Danish -> DanishTranslations.aboutUpdate_githubUpdateCheckFailed_1
    AppLanguage.Dutch -> DutchTranslations.aboutUpdate_githubUpdateCheckFailed_1
    AppLanguage.English -> EnglishTranslations.aboutUpdate_githubUpdateCheckFailed_1
    AppLanguage.Estonian -> EstonianTranslations.aboutUpdate_githubUpdateCheckFailed_1
    AppLanguage.Finnish -> FinnishTranslations.aboutUpdate_githubUpdateCheckFailed_1
    AppLanguage.French -> FrenchTranslations.aboutUpdate_githubUpdateCheckFailed_1
    AppLanguage.German -> GermanTranslations.aboutUpdate_githubUpdateCheckFailed_1
    AppLanguage.Greek -> GreekTranslations.aboutUpdate_githubUpdateCheckFailed_1
    AppLanguage.Hindi -> HindiTranslations.aboutUpdate_githubUpdateCheckFailed_1
    AppLanguage.Hungarian -> HungarianTranslations.aboutUpdate_githubUpdateCheckFailed_1
    AppLanguage.Indonesian -> IndonesianTranslations.aboutUpdate_githubUpdateCheckFailed_1
    AppLanguage.Italian -> ItalianTranslations.aboutUpdate_githubUpdateCheckFailed_1
    AppLanguage.Japanese -> JapaneseTranslations.aboutUpdate_githubUpdateCheckFailed_1
    AppLanguage.Korean -> KoreanTranslations.aboutUpdate_githubUpdateCheckFailed_1
    AppLanguage.Latvian -> LatvianTranslations.aboutUpdate_githubUpdateCheckFailed_1
    AppLanguage.Lithuanian -> LithuanianTranslations.aboutUpdate_githubUpdateCheckFailed_1
    AppLanguage.Macedonian -> MacedonianTranslations.aboutUpdate_githubUpdateCheckFailed_1
    AppLanguage.Malay -> MalayTranslations.aboutUpdate_githubUpdateCheckFailed_1
    AppLanguage.Norwegian -> NorwegianTranslations.aboutUpdate_githubUpdateCheckFailed_1
    AppLanguage.Polish -> PolishTranslations.aboutUpdate_githubUpdateCheckFailed_1
    AppLanguage.Portuguese -> PortugueseTranslations.aboutUpdate_githubUpdateCheckFailed_1
    AppLanguage.Punjabi -> PunjabiTranslations.aboutUpdate_githubUpdateCheckFailed_1
    AppLanguage.Romanian -> RomanianTranslations.aboutUpdate_githubUpdateCheckFailed_1
    AppLanguage.Russian -> RussianTranslations.aboutUpdate_githubUpdateCheckFailed_1
    AppLanguage.Serbian -> SerbianTranslations.aboutUpdate_githubUpdateCheckFailed_1
    AppLanguage.Slovak -> SlovakTranslations.aboutUpdate_githubUpdateCheckFailed_1
    AppLanguage.Slovenian -> SlovenianTranslations.aboutUpdate_githubUpdateCheckFailed_1
    AppLanguage.Spanish -> SpanishTranslations.aboutUpdate_githubUpdateCheckFailed_1
    AppLanguage.Swedish -> SwedishTranslations.aboutUpdate_githubUpdateCheckFailed_1
    AppLanguage.Thai -> ThaiTranslations.aboutUpdate_githubUpdateCheckFailed_1
    AppLanguage.Ukrainian -> UkrainianTranslations.aboutUpdate_githubUpdateCheckFailed_1
    AppLanguage.Urdu -> UrduTranslations.aboutUpdate_githubUpdateCheckFailed_1
}

private fun updateCheckUnavailable(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.aboutUpdate_updateCheckUnavailable_1
    AppLanguage.Bengali -> BengaliTranslations.aboutUpdate_updateCheckUnavailable_1
    AppLanguage.Bosnian -> BosnianTranslations.aboutUpdate_updateCheckUnavailable_1
    AppLanguage.Bulgarian -> BulgarianTranslations.aboutUpdate_updateCheckUnavailable_1
    AppLanguage.Catalan -> CatalanTranslations.aboutUpdate_updateCheckUnavailable_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.aboutUpdate_updateCheckUnavailable_1
    AppLanguage.Croatian -> CroatianTranslations.aboutUpdate_updateCheckUnavailable_1
    AppLanguage.Czech -> CzechTranslations.aboutUpdate_updateCheckUnavailable_1
    AppLanguage.Danish -> DanishTranslations.aboutUpdate_updateCheckUnavailable_1
    AppLanguage.Dutch -> DutchTranslations.aboutUpdate_updateCheckUnavailable_1
    AppLanguage.English -> EnglishTranslations.aboutUpdate_updateCheckUnavailable_1
    AppLanguage.Estonian -> EstonianTranslations.aboutUpdate_updateCheckUnavailable_1
    AppLanguage.Finnish -> FinnishTranslations.aboutUpdate_updateCheckUnavailable_1
    AppLanguage.French -> FrenchTranslations.aboutUpdate_updateCheckUnavailable_1
    AppLanguage.German -> GermanTranslations.aboutUpdate_updateCheckUnavailable_1
    AppLanguage.Greek -> GreekTranslations.aboutUpdate_updateCheckUnavailable_1
    AppLanguage.Hindi -> HindiTranslations.aboutUpdate_updateCheckUnavailable_1
    AppLanguage.Hungarian -> HungarianTranslations.aboutUpdate_updateCheckUnavailable_1
    AppLanguage.Indonesian -> IndonesianTranslations.aboutUpdate_updateCheckUnavailable_1
    AppLanguage.Italian -> ItalianTranslations.aboutUpdate_updateCheckUnavailable_1
    AppLanguage.Japanese -> JapaneseTranslations.aboutUpdate_updateCheckUnavailable_1
    AppLanguage.Korean -> KoreanTranslations.aboutUpdate_updateCheckUnavailable_1
    AppLanguage.Latvian -> LatvianTranslations.aboutUpdate_updateCheckUnavailable_1
    AppLanguage.Lithuanian -> LithuanianTranslations.aboutUpdate_updateCheckUnavailable_1
    AppLanguage.Macedonian -> MacedonianTranslations.aboutUpdate_updateCheckUnavailable_1
    AppLanguage.Malay -> MalayTranslations.aboutUpdate_updateCheckUnavailable_1
    AppLanguage.Norwegian -> NorwegianTranslations.aboutUpdate_updateCheckUnavailable_1
    AppLanguage.Polish -> PolishTranslations.aboutUpdate_updateCheckUnavailable_1
    AppLanguage.Portuguese -> PortugueseTranslations.aboutUpdate_updateCheckUnavailable_1
    AppLanguage.Punjabi -> PunjabiTranslations.aboutUpdate_updateCheckUnavailable_1
    AppLanguage.Romanian -> RomanianTranslations.aboutUpdate_updateCheckUnavailable_1
    AppLanguage.Russian -> RussianTranslations.aboutUpdate_updateCheckUnavailable_1
    AppLanguage.Serbian -> SerbianTranslations.aboutUpdate_updateCheckUnavailable_1
    AppLanguage.Slovak -> SlovakTranslations.aboutUpdate_updateCheckUnavailable_1
    AppLanguage.Slovenian -> SlovenianTranslations.aboutUpdate_updateCheckUnavailable_1
    AppLanguage.Spanish -> SpanishTranslations.aboutUpdate_updateCheckUnavailable_1
    AppLanguage.Swedish -> SwedishTranslations.aboutUpdate_updateCheckUnavailable_1
    AppLanguage.Thai -> ThaiTranslations.aboutUpdate_updateCheckUnavailable_1
    AppLanguage.Ukrainian -> UkrainianTranslations.aboutUpdate_updateCheckUnavailable_1
    AppLanguage.Urdu -> UrduTranslations.aboutUpdate_updateCheckUnavailable_1
}

private fun updateDownloadInvalid(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.aboutUpdate_updateDownloadInvalid_1
    AppLanguage.Bengali -> BengaliTranslations.aboutUpdate_updateDownloadInvalid_1
    AppLanguage.Bosnian -> BosnianTranslations.aboutUpdate_updateDownloadInvalid_1
    AppLanguage.Bulgarian -> BulgarianTranslations.aboutUpdate_updateDownloadInvalid_1
    AppLanguage.Catalan -> CatalanTranslations.aboutUpdate_updateDownloadInvalid_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.aboutUpdate_updateDownloadInvalid_1
    AppLanguage.Croatian -> CroatianTranslations.aboutUpdate_updateDownloadInvalid_1
    AppLanguage.Czech -> CzechTranslations.aboutUpdate_updateDownloadInvalid_1
    AppLanguage.Danish -> DanishTranslations.aboutUpdate_updateDownloadInvalid_1
    AppLanguage.Dutch -> DutchTranslations.aboutUpdate_updateDownloadInvalid_1
    AppLanguage.English -> EnglishTranslations.aboutUpdate_updateDownloadInvalid_1
    AppLanguage.Estonian -> EstonianTranslations.aboutUpdate_updateDownloadInvalid_1
    AppLanguage.Finnish -> FinnishTranslations.aboutUpdate_updateDownloadInvalid_1
    AppLanguage.French -> FrenchTranslations.aboutUpdate_updateDownloadInvalid_1
    AppLanguage.German -> GermanTranslations.aboutUpdate_updateDownloadInvalid_1
    AppLanguage.Greek -> GreekTranslations.aboutUpdate_updateDownloadInvalid_1
    AppLanguage.Hindi -> HindiTranslations.aboutUpdate_updateDownloadInvalid_1
    AppLanguage.Hungarian -> HungarianTranslations.aboutUpdate_updateDownloadInvalid_1
    AppLanguage.Indonesian -> IndonesianTranslations.aboutUpdate_updateDownloadInvalid_1
    AppLanguage.Italian -> ItalianTranslations.aboutUpdate_updateDownloadInvalid_1
    AppLanguage.Japanese -> JapaneseTranslations.aboutUpdate_updateDownloadInvalid_1
    AppLanguage.Korean -> KoreanTranslations.aboutUpdate_updateDownloadInvalid_1
    AppLanguage.Latvian -> LatvianTranslations.aboutUpdate_updateDownloadInvalid_1
    AppLanguage.Lithuanian -> LithuanianTranslations.aboutUpdate_updateDownloadInvalid_1
    AppLanguage.Macedonian -> MacedonianTranslations.aboutUpdate_updateDownloadInvalid_1
    AppLanguage.Malay -> MalayTranslations.aboutUpdate_updateDownloadInvalid_1
    AppLanguage.Norwegian -> NorwegianTranslations.aboutUpdate_updateDownloadInvalid_1
    AppLanguage.Polish -> PolishTranslations.aboutUpdate_updateDownloadInvalid_1
    AppLanguage.Portuguese -> PortugueseTranslations.aboutUpdate_updateDownloadInvalid_1
    AppLanguage.Punjabi -> PunjabiTranslations.aboutUpdate_updateDownloadInvalid_1
    AppLanguage.Romanian -> RomanianTranslations.aboutUpdate_updateDownloadInvalid_1
    AppLanguage.Russian -> RussianTranslations.aboutUpdate_updateDownloadInvalid_1
    AppLanguage.Serbian -> SerbianTranslations.aboutUpdate_updateDownloadInvalid_1
    AppLanguage.Slovak -> SlovakTranslations.aboutUpdate_updateDownloadInvalid_1
    AppLanguage.Slovenian -> SlovenianTranslations.aboutUpdate_updateDownloadInvalid_1
    AppLanguage.Spanish -> SpanishTranslations.aboutUpdate_updateDownloadInvalid_1
    AppLanguage.Swedish -> SwedishTranslations.aboutUpdate_updateDownloadInvalid_1
    AppLanguage.Thai -> ThaiTranslations.aboutUpdate_updateDownloadInvalid_1
    AppLanguage.Ukrainian -> UkrainianTranslations.aboutUpdate_updateDownloadInvalid_1
    AppLanguage.Urdu -> UrduTranslations.aboutUpdate_updateDownloadInvalid_1
}

private fun updateNotValidApp(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.aboutUpdate_updateNotValidApp_1
    AppLanguage.Bengali -> BengaliTranslations.aboutUpdate_updateNotValidApp_1
    AppLanguage.Bosnian -> BosnianTranslations.aboutUpdate_updateNotValidApp_1
    AppLanguage.Bulgarian -> BulgarianTranslations.aboutUpdate_updateNotValidApp_1
    AppLanguage.Catalan -> CatalanTranslations.aboutUpdate_updateNotValidApp_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.aboutUpdate_updateNotValidApp_1
    AppLanguage.Croatian -> CroatianTranslations.aboutUpdate_updateNotValidApp_1
    AppLanguage.Czech -> CzechTranslations.aboutUpdate_updateNotValidApp_1
    AppLanguage.Danish -> DanishTranslations.aboutUpdate_updateNotValidApp_1
    AppLanguage.Dutch -> DutchTranslations.aboutUpdate_updateNotValidApp_1
    AppLanguage.English -> EnglishTranslations.aboutUpdate_updateNotValidApp_1
    AppLanguage.Estonian -> EstonianTranslations.aboutUpdate_updateNotValidApp_1
    AppLanguage.Finnish -> FinnishTranslations.aboutUpdate_updateNotValidApp_1
    AppLanguage.French -> FrenchTranslations.aboutUpdate_updateNotValidApp_1
    AppLanguage.German -> GermanTranslations.aboutUpdate_updateNotValidApp_1
    AppLanguage.Greek -> GreekTranslations.aboutUpdate_updateNotValidApp_1
    AppLanguage.Hindi -> HindiTranslations.aboutUpdate_updateNotValidApp_1
    AppLanguage.Hungarian -> HungarianTranslations.aboutUpdate_updateNotValidApp_1
    AppLanguage.Indonesian -> IndonesianTranslations.aboutUpdate_updateNotValidApp_1
    AppLanguage.Italian -> ItalianTranslations.aboutUpdate_updateNotValidApp_1
    AppLanguage.Japanese -> JapaneseTranslations.aboutUpdate_updateNotValidApp_1
    AppLanguage.Korean -> KoreanTranslations.aboutUpdate_updateNotValidApp_1
    AppLanguage.Latvian -> LatvianTranslations.aboutUpdate_updateNotValidApp_1
    AppLanguage.Lithuanian -> LithuanianTranslations.aboutUpdate_updateNotValidApp_1
    AppLanguage.Macedonian -> MacedonianTranslations.aboutUpdate_updateNotValidApp_1
    AppLanguage.Malay -> MalayTranslations.aboutUpdate_updateNotValidApp_1
    AppLanguage.Norwegian -> NorwegianTranslations.aboutUpdate_updateNotValidApp_1
    AppLanguage.Polish -> PolishTranslations.aboutUpdate_updateNotValidApp_1
    AppLanguage.Portuguese -> PortugueseTranslations.aboutUpdate_updateNotValidApp_1
    AppLanguage.Punjabi -> PunjabiTranslations.aboutUpdate_updateNotValidApp_1
    AppLanguage.Romanian -> RomanianTranslations.aboutUpdate_updateNotValidApp_1
    AppLanguage.Russian -> RussianTranslations.aboutUpdate_updateNotValidApp_1
    AppLanguage.Serbian -> SerbianTranslations.aboutUpdate_updateNotValidApp_1
    AppLanguage.Slovak -> SlovakTranslations.aboutUpdate_updateNotValidApp_1
    AppLanguage.Slovenian -> SlovenianTranslations.aboutUpdate_updateNotValidApp_1
    AppLanguage.Spanish -> SpanishTranslations.aboutUpdate_updateNotValidApp_1
    AppLanguage.Swedish -> SwedishTranslations.aboutUpdate_updateNotValidApp_1
    AppLanguage.Thai -> ThaiTranslations.aboutUpdate_updateNotValidApp_1
    AppLanguage.Ukrainian -> UkrainianTranslations.aboutUpdate_updateNotValidApp_1
    AppLanguage.Urdu -> UrduTranslations.aboutUpdate_updateNotValidApp_1
}

private fun updateNotNewer(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.aboutUpdate_updateNotNewer_1
    AppLanguage.Bengali -> BengaliTranslations.aboutUpdate_updateNotNewer_1
    AppLanguage.Bosnian -> BosnianTranslations.aboutUpdate_updateNotNewer_1
    AppLanguage.Bulgarian -> BulgarianTranslations.aboutUpdate_updateNotNewer_1
    AppLanguage.Catalan -> CatalanTranslations.aboutUpdate_updateNotNewer_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.aboutUpdate_updateNotNewer_1
    AppLanguage.Croatian -> CroatianTranslations.aboutUpdate_updateNotNewer_1
    AppLanguage.Czech -> CzechTranslations.aboutUpdate_updateNotNewer_1
    AppLanguage.Danish -> DanishTranslations.aboutUpdate_updateNotNewer_1
    AppLanguage.Dutch -> DutchTranslations.aboutUpdate_updateNotNewer_1
    AppLanguage.English -> EnglishTranslations.aboutUpdate_updateNotNewer_1
    AppLanguage.Estonian -> EstonianTranslations.aboutUpdate_updateNotNewer_1
    AppLanguage.Finnish -> FinnishTranslations.aboutUpdate_updateNotNewer_1
    AppLanguage.French -> FrenchTranslations.aboutUpdate_updateNotNewer_1
    AppLanguage.German -> GermanTranslations.aboutUpdate_updateNotNewer_1
    AppLanguage.Greek -> GreekTranslations.aboutUpdate_updateNotNewer_1
    AppLanguage.Hindi -> HindiTranslations.aboutUpdate_updateNotNewer_1
    AppLanguage.Hungarian -> HungarianTranslations.aboutUpdate_updateNotNewer_1
    AppLanguage.Indonesian -> IndonesianTranslations.aboutUpdate_updateNotNewer_1
    AppLanguage.Italian -> ItalianTranslations.aboutUpdate_updateNotNewer_1
    AppLanguage.Japanese -> JapaneseTranslations.aboutUpdate_updateNotNewer_1
    AppLanguage.Korean -> KoreanTranslations.aboutUpdate_updateNotNewer_1
    AppLanguage.Latvian -> LatvianTranslations.aboutUpdate_updateNotNewer_1
    AppLanguage.Lithuanian -> LithuanianTranslations.aboutUpdate_updateNotNewer_1
    AppLanguage.Macedonian -> MacedonianTranslations.aboutUpdate_updateNotNewer_1
    AppLanguage.Malay -> MalayTranslations.aboutUpdate_updateNotNewer_1
    AppLanguage.Norwegian -> NorwegianTranslations.aboutUpdate_updateNotNewer_1
    AppLanguage.Polish -> PolishTranslations.aboutUpdate_updateNotNewer_1
    AppLanguage.Portuguese -> PortugueseTranslations.aboutUpdate_updateNotNewer_1
    AppLanguage.Punjabi -> PunjabiTranslations.aboutUpdate_updateNotNewer_1
    AppLanguage.Romanian -> RomanianTranslations.aboutUpdate_updateNotNewer_1
    AppLanguage.Russian -> RussianTranslations.aboutUpdate_updateNotNewer_1
    AppLanguage.Serbian -> SerbianTranslations.aboutUpdate_updateNotNewer_1
    AppLanguage.Slovak -> SlovakTranslations.aboutUpdate_updateNotNewer_1
    AppLanguage.Slovenian -> SlovenianTranslations.aboutUpdate_updateNotNewer_1
    AppLanguage.Spanish -> SpanishTranslations.aboutUpdate_updateNotNewer_1
    AppLanguage.Swedish -> SwedishTranslations.aboutUpdate_updateNotNewer_1
    AppLanguage.Thai -> ThaiTranslations.aboutUpdate_updateNotNewer_1
    AppLanguage.Ukrainian -> UkrainianTranslations.aboutUpdate_updateNotNewer_1
    AppLanguage.Urdu -> UrduTranslations.aboutUpdate_updateNotNewer_1
}

private fun updateVersionInvalid(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.aboutUpdate_updateVersionInvalid_1
    AppLanguage.Bengali -> BengaliTranslations.aboutUpdate_updateVersionInvalid_1
    AppLanguage.Bosnian -> BosnianTranslations.aboutUpdate_updateVersionInvalid_1
    AppLanguage.Bulgarian -> BulgarianTranslations.aboutUpdate_updateVersionInvalid_1
    AppLanguage.Catalan -> CatalanTranslations.aboutUpdate_updateVersionInvalid_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.aboutUpdate_updateVersionInvalid_1
    AppLanguage.Croatian -> CroatianTranslations.aboutUpdate_updateVersionInvalid_1
    AppLanguage.Czech -> CzechTranslations.aboutUpdate_updateVersionInvalid_1
    AppLanguage.Danish -> DanishTranslations.aboutUpdate_updateVersionInvalid_1
    AppLanguage.Dutch -> DutchTranslations.aboutUpdate_updateVersionInvalid_1
    AppLanguage.English -> EnglishTranslations.aboutUpdate_updateVersionInvalid_1
    AppLanguage.Estonian -> EstonianTranslations.aboutUpdate_updateVersionInvalid_1
    AppLanguage.Finnish -> FinnishTranslations.aboutUpdate_updateVersionInvalid_1
    AppLanguage.French -> FrenchTranslations.aboutUpdate_updateVersionInvalid_1
    AppLanguage.German -> GermanTranslations.aboutUpdate_updateVersionInvalid_1
    AppLanguage.Greek -> GreekTranslations.aboutUpdate_updateVersionInvalid_1
    AppLanguage.Hindi -> HindiTranslations.aboutUpdate_updateVersionInvalid_1
    AppLanguage.Hungarian -> HungarianTranslations.aboutUpdate_updateVersionInvalid_1
    AppLanguage.Indonesian -> IndonesianTranslations.aboutUpdate_updateVersionInvalid_1
    AppLanguage.Italian -> ItalianTranslations.aboutUpdate_updateVersionInvalid_1
    AppLanguage.Japanese -> JapaneseTranslations.aboutUpdate_updateVersionInvalid_1
    AppLanguage.Korean -> KoreanTranslations.aboutUpdate_updateVersionInvalid_1
    AppLanguage.Latvian -> LatvianTranslations.aboutUpdate_updateVersionInvalid_1
    AppLanguage.Lithuanian -> LithuanianTranslations.aboutUpdate_updateVersionInvalid_1
    AppLanguage.Macedonian -> MacedonianTranslations.aboutUpdate_updateVersionInvalid_1
    AppLanguage.Malay -> MalayTranslations.aboutUpdate_updateVersionInvalid_1
    AppLanguage.Norwegian -> NorwegianTranslations.aboutUpdate_updateVersionInvalid_1
    AppLanguage.Polish -> PolishTranslations.aboutUpdate_updateVersionInvalid_1
    AppLanguage.Portuguese -> PortugueseTranslations.aboutUpdate_updateVersionInvalid_1
    AppLanguage.Punjabi -> PunjabiTranslations.aboutUpdate_updateVersionInvalid_1
    AppLanguage.Romanian -> RomanianTranslations.aboutUpdate_updateVersionInvalid_1
    AppLanguage.Russian -> RussianTranslations.aboutUpdate_updateVersionInvalid_1
    AppLanguage.Serbian -> SerbianTranslations.aboutUpdate_updateVersionInvalid_1
    AppLanguage.Slovak -> SlovakTranslations.aboutUpdate_updateVersionInvalid_1
    AppLanguage.Slovenian -> SlovenianTranslations.aboutUpdate_updateVersionInvalid_1
    AppLanguage.Spanish -> SpanishTranslations.aboutUpdate_updateVersionInvalid_1
    AppLanguage.Swedish -> SwedishTranslations.aboutUpdate_updateVersionInvalid_1
    AppLanguage.Thai -> ThaiTranslations.aboutUpdate_updateVersionInvalid_1
    AppLanguage.Ukrainian -> UkrainianTranslations.aboutUpdate_updateVersionInvalid_1
    AppLanguage.Urdu -> UrduTranslations.aboutUpdate_updateVersionInvalid_1
}

private fun updateTargetsAnotherApp(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.aboutUpdate_updateTargetsAnotherApp_1
    AppLanguage.Bengali -> BengaliTranslations.aboutUpdate_updateTargetsAnotherApp_1
    AppLanguage.Bosnian -> BosnianTranslations.aboutUpdate_updateTargetsAnotherApp_1
    AppLanguage.Bulgarian -> BulgarianTranslations.aboutUpdate_updateTargetsAnotherApp_1
    AppLanguage.Catalan -> CatalanTranslations.aboutUpdate_updateTargetsAnotherApp_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.aboutUpdate_updateTargetsAnotherApp_1
    AppLanguage.Croatian -> CroatianTranslations.aboutUpdate_updateTargetsAnotherApp_1
    AppLanguage.Czech -> CzechTranslations.aboutUpdate_updateTargetsAnotherApp_1
    AppLanguage.Danish -> DanishTranslations.aboutUpdate_updateTargetsAnotherApp_1
    AppLanguage.Dutch -> DutchTranslations.aboutUpdate_updateTargetsAnotherApp_1
    AppLanguage.English -> EnglishTranslations.aboutUpdate_updateTargetsAnotherApp_1
    AppLanguage.Estonian -> EstonianTranslations.aboutUpdate_updateTargetsAnotherApp_1
    AppLanguage.Finnish -> FinnishTranslations.aboutUpdate_updateTargetsAnotherApp_1
    AppLanguage.French -> FrenchTranslations.aboutUpdate_updateTargetsAnotherApp_1
    AppLanguage.German -> GermanTranslations.aboutUpdate_updateTargetsAnotherApp_1
    AppLanguage.Greek -> GreekTranslations.aboutUpdate_updateTargetsAnotherApp_1
    AppLanguage.Hindi -> HindiTranslations.aboutUpdate_updateTargetsAnotherApp_1
    AppLanguage.Hungarian -> HungarianTranslations.aboutUpdate_updateTargetsAnotherApp_1
    AppLanguage.Indonesian -> IndonesianTranslations.aboutUpdate_updateTargetsAnotherApp_1
    AppLanguage.Italian -> ItalianTranslations.aboutUpdate_updateTargetsAnotherApp_1
    AppLanguage.Japanese -> JapaneseTranslations.aboutUpdate_updateTargetsAnotherApp_1
    AppLanguage.Korean -> KoreanTranslations.aboutUpdate_updateTargetsAnotherApp_1
    AppLanguage.Latvian -> LatvianTranslations.aboutUpdate_updateTargetsAnotherApp_1
    AppLanguage.Lithuanian -> LithuanianTranslations.aboutUpdate_updateTargetsAnotherApp_1
    AppLanguage.Macedonian -> MacedonianTranslations.aboutUpdate_updateTargetsAnotherApp_1
    AppLanguage.Malay -> MalayTranslations.aboutUpdate_updateTargetsAnotherApp_1
    AppLanguage.Norwegian -> NorwegianTranslations.aboutUpdate_updateTargetsAnotherApp_1
    AppLanguage.Polish -> PolishTranslations.aboutUpdate_updateTargetsAnotherApp_1
    AppLanguage.Portuguese -> PortugueseTranslations.aboutUpdate_updateTargetsAnotherApp_1
    AppLanguage.Punjabi -> PunjabiTranslations.aboutUpdate_updateTargetsAnotherApp_1
    AppLanguage.Romanian -> RomanianTranslations.aboutUpdate_updateTargetsAnotherApp_1
    AppLanguage.Russian -> RussianTranslations.aboutUpdate_updateTargetsAnotherApp_1
    AppLanguage.Serbian -> SerbianTranslations.aboutUpdate_updateTargetsAnotherApp_1
    AppLanguage.Slovak -> SlovakTranslations.aboutUpdate_updateTargetsAnotherApp_1
    AppLanguage.Slovenian -> SlovenianTranslations.aboutUpdate_updateTargetsAnotherApp_1
    AppLanguage.Spanish -> SpanishTranslations.aboutUpdate_updateTargetsAnotherApp_1
    AppLanguage.Swedish -> SwedishTranslations.aboutUpdate_updateTargetsAnotherApp_1
    AppLanguage.Thai -> ThaiTranslations.aboutUpdate_updateTargetsAnotherApp_1
    AppLanguage.Ukrainian -> UkrainianTranslations.aboutUpdate_updateTargetsAnotherApp_1
    AppLanguage.Urdu -> UrduTranslations.aboutUpdate_updateTargetsAnotherApp_1
}

private fun updateVerificationUnavailable(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.aboutUpdate_updateVerificationUnavailable_1
    AppLanguage.Bengali -> BengaliTranslations.aboutUpdate_updateVerificationUnavailable_1
    AppLanguage.Bosnian -> BosnianTranslations.aboutUpdate_updateVerificationUnavailable_1
    AppLanguage.Bulgarian -> BulgarianTranslations.aboutUpdate_updateVerificationUnavailable_1
    AppLanguage.Catalan -> CatalanTranslations.aboutUpdate_updateVerificationUnavailable_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.aboutUpdate_updateVerificationUnavailable_1
    AppLanguage.Croatian -> CroatianTranslations.aboutUpdate_updateVerificationUnavailable_1
    AppLanguage.Czech -> CzechTranslations.aboutUpdate_updateVerificationUnavailable_1
    AppLanguage.Danish -> DanishTranslations.aboutUpdate_updateVerificationUnavailable_1
    AppLanguage.Dutch -> DutchTranslations.aboutUpdate_updateVerificationUnavailable_1
    AppLanguage.English -> EnglishTranslations.aboutUpdate_updateVerificationUnavailable_1
    AppLanguage.Estonian -> EstonianTranslations.aboutUpdate_updateVerificationUnavailable_1
    AppLanguage.Finnish -> FinnishTranslations.aboutUpdate_updateVerificationUnavailable_1
    AppLanguage.French -> FrenchTranslations.aboutUpdate_updateVerificationUnavailable_1
    AppLanguage.German -> GermanTranslations.aboutUpdate_updateVerificationUnavailable_1
    AppLanguage.Greek -> GreekTranslations.aboutUpdate_updateVerificationUnavailable_1
    AppLanguage.Hindi -> HindiTranslations.aboutUpdate_updateVerificationUnavailable_1
    AppLanguage.Hungarian -> HungarianTranslations.aboutUpdate_updateVerificationUnavailable_1
    AppLanguage.Indonesian -> IndonesianTranslations.aboutUpdate_updateVerificationUnavailable_1
    AppLanguage.Italian -> ItalianTranslations.aboutUpdate_updateVerificationUnavailable_1
    AppLanguage.Japanese -> JapaneseTranslations.aboutUpdate_updateVerificationUnavailable_1
    AppLanguage.Korean -> KoreanTranslations.aboutUpdate_updateVerificationUnavailable_1
    AppLanguage.Latvian -> LatvianTranslations.aboutUpdate_updateVerificationUnavailable_1
    AppLanguage.Lithuanian -> LithuanianTranslations.aboutUpdate_updateVerificationUnavailable_1
    AppLanguage.Macedonian -> MacedonianTranslations.aboutUpdate_updateVerificationUnavailable_1
    AppLanguage.Malay -> MalayTranslations.aboutUpdate_updateVerificationUnavailable_1
    AppLanguage.Norwegian -> NorwegianTranslations.aboutUpdate_updateVerificationUnavailable_1
    AppLanguage.Polish -> PolishTranslations.aboutUpdate_updateVerificationUnavailable_1
    AppLanguage.Portuguese -> PortugueseTranslations.aboutUpdate_updateVerificationUnavailable_1
    AppLanguage.Punjabi -> PunjabiTranslations.aboutUpdate_updateVerificationUnavailable_1
    AppLanguage.Romanian -> RomanianTranslations.aboutUpdate_updateVerificationUnavailable_1
    AppLanguage.Russian -> RussianTranslations.aboutUpdate_updateVerificationUnavailable_1
    AppLanguage.Serbian -> SerbianTranslations.aboutUpdate_updateVerificationUnavailable_1
    AppLanguage.Slovak -> SlovakTranslations.aboutUpdate_updateVerificationUnavailable_1
    AppLanguage.Slovenian -> SlovenianTranslations.aboutUpdate_updateVerificationUnavailable_1
    AppLanguage.Spanish -> SpanishTranslations.aboutUpdate_updateVerificationUnavailable_1
    AppLanguage.Swedish -> SwedishTranslations.aboutUpdate_updateVerificationUnavailable_1
    AppLanguage.Thai -> ThaiTranslations.aboutUpdate_updateVerificationUnavailable_1
    AppLanguage.Ukrainian -> UkrainianTranslations.aboutUpdate_updateVerificationUnavailable_1
    AppLanguage.Urdu -> UrduTranslations.aboutUpdate_updateVerificationUnavailable_1
}

private fun updateUnknownCertificate(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.aboutUpdate_updateUnknownCertificate_1
    AppLanguage.Bengali -> BengaliTranslations.aboutUpdate_updateUnknownCertificate_1
    AppLanguage.Bosnian -> BosnianTranslations.aboutUpdate_updateUnknownCertificate_1
    AppLanguage.Bulgarian -> BulgarianTranslations.aboutUpdate_updateUnknownCertificate_1
    AppLanguage.Catalan -> CatalanTranslations.aboutUpdate_updateUnknownCertificate_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.aboutUpdate_updateUnknownCertificate_1
    AppLanguage.Croatian -> CroatianTranslations.aboutUpdate_updateUnknownCertificate_1
    AppLanguage.Czech -> CzechTranslations.aboutUpdate_updateUnknownCertificate_1
    AppLanguage.Danish -> DanishTranslations.aboutUpdate_updateUnknownCertificate_1
    AppLanguage.Dutch -> DutchTranslations.aboutUpdate_updateUnknownCertificate_1
    AppLanguage.English -> EnglishTranslations.aboutUpdate_updateUnknownCertificate_1
    AppLanguage.Estonian -> EstonianTranslations.aboutUpdate_updateUnknownCertificate_1
    AppLanguage.Finnish -> FinnishTranslations.aboutUpdate_updateUnknownCertificate_1
    AppLanguage.French -> FrenchTranslations.aboutUpdate_updateUnknownCertificate_1
    AppLanguage.German -> GermanTranslations.aboutUpdate_updateUnknownCertificate_1
    AppLanguage.Greek -> GreekTranslations.aboutUpdate_updateUnknownCertificate_1
    AppLanguage.Hindi -> HindiTranslations.aboutUpdate_updateUnknownCertificate_1
    AppLanguage.Hungarian -> HungarianTranslations.aboutUpdate_updateUnknownCertificate_1
    AppLanguage.Indonesian -> IndonesianTranslations.aboutUpdate_updateUnknownCertificate_1
    AppLanguage.Italian -> ItalianTranslations.aboutUpdate_updateUnknownCertificate_1
    AppLanguage.Japanese -> JapaneseTranslations.aboutUpdate_updateUnknownCertificate_1
    AppLanguage.Korean -> KoreanTranslations.aboutUpdate_updateUnknownCertificate_1
    AppLanguage.Latvian -> LatvianTranslations.aboutUpdate_updateUnknownCertificate_1
    AppLanguage.Lithuanian -> LithuanianTranslations.aboutUpdate_updateUnknownCertificate_1
    AppLanguage.Macedonian -> MacedonianTranslations.aboutUpdate_updateUnknownCertificate_1
    AppLanguage.Malay -> MalayTranslations.aboutUpdate_updateUnknownCertificate_1
    AppLanguage.Norwegian -> NorwegianTranslations.aboutUpdate_updateUnknownCertificate_1
    AppLanguage.Polish -> PolishTranslations.aboutUpdate_updateUnknownCertificate_1
    AppLanguage.Portuguese -> PortugueseTranslations.aboutUpdate_updateUnknownCertificate_1
    AppLanguage.Punjabi -> PunjabiTranslations.aboutUpdate_updateUnknownCertificate_1
    AppLanguage.Romanian -> RomanianTranslations.aboutUpdate_updateUnknownCertificate_1
    AppLanguage.Russian -> RussianTranslations.aboutUpdate_updateUnknownCertificate_1
    AppLanguage.Serbian -> SerbianTranslations.aboutUpdate_updateUnknownCertificate_1
    AppLanguage.Slovak -> SlovakTranslations.aboutUpdate_updateUnknownCertificate_1
    AppLanguage.Slovenian -> SlovenianTranslations.aboutUpdate_updateUnknownCertificate_1
    AppLanguage.Spanish -> SpanishTranslations.aboutUpdate_updateUnknownCertificate_1
    AppLanguage.Swedish -> SwedishTranslations.aboutUpdate_updateUnknownCertificate_1
    AppLanguage.Thai -> ThaiTranslations.aboutUpdate_updateUnknownCertificate_1
    AppLanguage.Ukrainian -> UkrainianTranslations.aboutUpdate_updateUnknownCertificate_1
    AppLanguage.Urdu -> UrduTranslations.aboutUpdate_updateUnknownCertificate_1
}

private fun updateVerificationFailed(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.aboutUpdate_updateVerificationFailed_1
    AppLanguage.Bengali -> BengaliTranslations.aboutUpdate_updateVerificationFailed_1
    AppLanguage.Bosnian -> BosnianTranslations.aboutUpdate_updateVerificationFailed_1
    AppLanguage.Bulgarian -> BulgarianTranslations.aboutUpdate_updateVerificationFailed_1
    AppLanguage.Catalan -> CatalanTranslations.aboutUpdate_updateVerificationFailed_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.aboutUpdate_updateVerificationFailed_1
    AppLanguage.Croatian -> CroatianTranslations.aboutUpdate_updateVerificationFailed_1
    AppLanguage.Czech -> CzechTranslations.aboutUpdate_updateVerificationFailed_1
    AppLanguage.Danish -> DanishTranslations.aboutUpdate_updateVerificationFailed_1
    AppLanguage.Dutch -> DutchTranslations.aboutUpdate_updateVerificationFailed_1
    AppLanguage.English -> EnglishTranslations.aboutUpdate_updateVerificationFailed_1
    AppLanguage.Estonian -> EstonianTranslations.aboutUpdate_updateVerificationFailed_1
    AppLanguage.Finnish -> FinnishTranslations.aboutUpdate_updateVerificationFailed_1
    AppLanguage.French -> FrenchTranslations.aboutUpdate_updateVerificationFailed_1
    AppLanguage.German -> GermanTranslations.aboutUpdate_updateVerificationFailed_1
    AppLanguage.Greek -> GreekTranslations.aboutUpdate_updateVerificationFailed_1
    AppLanguage.Hindi -> HindiTranslations.aboutUpdate_updateVerificationFailed_1
    AppLanguage.Hungarian -> HungarianTranslations.aboutUpdate_updateVerificationFailed_1
    AppLanguage.Indonesian -> IndonesianTranslations.aboutUpdate_updateVerificationFailed_1
    AppLanguage.Italian -> ItalianTranslations.aboutUpdate_updateVerificationFailed_1
    AppLanguage.Japanese -> JapaneseTranslations.aboutUpdate_updateVerificationFailed_1
    AppLanguage.Korean -> KoreanTranslations.aboutUpdate_updateVerificationFailed_1
    AppLanguage.Latvian -> LatvianTranslations.aboutUpdate_updateVerificationFailed_1
    AppLanguage.Lithuanian -> LithuanianTranslations.aboutUpdate_updateVerificationFailed_1
    AppLanguage.Macedonian -> MacedonianTranslations.aboutUpdate_updateVerificationFailed_1
    AppLanguage.Malay -> MalayTranslations.aboutUpdate_updateVerificationFailed_1
    AppLanguage.Norwegian -> NorwegianTranslations.aboutUpdate_updateVerificationFailed_1
    AppLanguage.Polish -> PolishTranslations.aboutUpdate_updateVerificationFailed_1
    AppLanguage.Portuguese -> PortugueseTranslations.aboutUpdate_updateVerificationFailed_1
    AppLanguage.Punjabi -> PunjabiTranslations.aboutUpdate_updateVerificationFailed_1
    AppLanguage.Romanian -> RomanianTranslations.aboutUpdate_updateVerificationFailed_1
    AppLanguage.Russian -> RussianTranslations.aboutUpdate_updateVerificationFailed_1
    AppLanguage.Serbian -> SerbianTranslations.aboutUpdate_updateVerificationFailed_1
    AppLanguage.Slovak -> SlovakTranslations.aboutUpdate_updateVerificationFailed_1
    AppLanguage.Slovenian -> SlovenianTranslations.aboutUpdate_updateVerificationFailed_1
    AppLanguage.Spanish -> SpanishTranslations.aboutUpdate_updateVerificationFailed_1
    AppLanguage.Swedish -> SwedishTranslations.aboutUpdate_updateVerificationFailed_1
    AppLanguage.Thai -> ThaiTranslations.aboutUpdate_updateVerificationFailed_1
    AppLanguage.Ukrainian -> UkrainianTranslations.aboutUpdate_updateVerificationFailed_1
    AppLanguage.Urdu -> UrduTranslations.aboutUpdate_updateVerificationFailed_1
}

private fun updateDownloadIncomplete(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.aboutUpdate_updateDownloadIncomplete_1
    AppLanguage.Bengali -> BengaliTranslations.aboutUpdate_updateDownloadIncomplete_1
    AppLanguage.Bosnian -> BosnianTranslations.aboutUpdate_updateDownloadIncomplete_1
    AppLanguage.Bulgarian -> BulgarianTranslations.aboutUpdate_updateDownloadIncomplete_1
    AppLanguage.Catalan -> CatalanTranslations.aboutUpdate_updateDownloadIncomplete_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.aboutUpdate_updateDownloadIncomplete_1
    AppLanguage.Croatian -> CroatianTranslations.aboutUpdate_updateDownloadIncomplete_1
    AppLanguage.Czech -> CzechTranslations.aboutUpdate_updateDownloadIncomplete_1
    AppLanguage.Danish -> DanishTranslations.aboutUpdate_updateDownloadIncomplete_1
    AppLanguage.Dutch -> DutchTranslations.aboutUpdate_updateDownloadIncomplete_1
    AppLanguage.English -> EnglishTranslations.aboutUpdate_updateDownloadIncomplete_1
    AppLanguage.Estonian -> EstonianTranslations.aboutUpdate_updateDownloadIncomplete_1
    AppLanguage.Finnish -> FinnishTranslations.aboutUpdate_updateDownloadIncomplete_1
    AppLanguage.French -> FrenchTranslations.aboutUpdate_updateDownloadIncomplete_1
    AppLanguage.German -> GermanTranslations.aboutUpdate_updateDownloadIncomplete_1
    AppLanguage.Greek -> GreekTranslations.aboutUpdate_updateDownloadIncomplete_1
    AppLanguage.Hindi -> HindiTranslations.aboutUpdate_updateDownloadIncomplete_1
    AppLanguage.Hungarian -> HungarianTranslations.aboutUpdate_updateDownloadIncomplete_1
    AppLanguage.Indonesian -> IndonesianTranslations.aboutUpdate_updateDownloadIncomplete_1
    AppLanguage.Italian -> ItalianTranslations.aboutUpdate_updateDownloadIncomplete_1
    AppLanguage.Japanese -> JapaneseTranslations.aboutUpdate_updateDownloadIncomplete_1
    AppLanguage.Korean -> KoreanTranslations.aboutUpdate_updateDownloadIncomplete_1
    AppLanguage.Latvian -> LatvianTranslations.aboutUpdate_updateDownloadIncomplete_1
    AppLanguage.Lithuanian -> LithuanianTranslations.aboutUpdate_updateDownloadIncomplete_1
    AppLanguage.Macedonian -> MacedonianTranslations.aboutUpdate_updateDownloadIncomplete_1
    AppLanguage.Malay -> MalayTranslations.aboutUpdate_updateDownloadIncomplete_1
    AppLanguage.Norwegian -> NorwegianTranslations.aboutUpdate_updateDownloadIncomplete_1
    AppLanguage.Polish -> PolishTranslations.aboutUpdate_updateDownloadIncomplete_1
    AppLanguage.Portuguese -> PortugueseTranslations.aboutUpdate_updateDownloadIncomplete_1
    AppLanguage.Punjabi -> PunjabiTranslations.aboutUpdate_updateDownloadIncomplete_1
    AppLanguage.Romanian -> RomanianTranslations.aboutUpdate_updateDownloadIncomplete_1
    AppLanguage.Russian -> RussianTranslations.aboutUpdate_updateDownloadIncomplete_1
    AppLanguage.Serbian -> SerbianTranslations.aboutUpdate_updateDownloadIncomplete_1
    AppLanguage.Slovak -> SlovakTranslations.aboutUpdate_updateDownloadIncomplete_1
    AppLanguage.Slovenian -> SlovenianTranslations.aboutUpdate_updateDownloadIncomplete_1
    AppLanguage.Spanish -> SpanishTranslations.aboutUpdate_updateDownloadIncomplete_1
    AppLanguage.Swedish -> SwedishTranslations.aboutUpdate_updateDownloadIncomplete_1
    AppLanguage.Thai -> ThaiTranslations.aboutUpdate_updateDownloadIncomplete_1
    AppLanguage.Ukrainian -> UkrainianTranslations.aboutUpdate_updateDownloadIncomplete_1
    AppLanguage.Urdu -> UrduTranslations.aboutUpdate_updateDownloadIncomplete_1
}

private fun updateDownloadFailed(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.aboutUpdate_updateDownloadFailed_1
    AppLanguage.Bengali -> BengaliTranslations.aboutUpdate_updateDownloadFailed_1
    AppLanguage.Bosnian -> BosnianTranslations.aboutUpdate_updateDownloadFailed_1
    AppLanguage.Bulgarian -> BulgarianTranslations.aboutUpdate_updateDownloadFailed_1
    AppLanguage.Catalan -> CatalanTranslations.aboutUpdate_updateDownloadFailed_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.aboutUpdate_updateDownloadFailed_1
    AppLanguage.Croatian -> CroatianTranslations.aboutUpdate_updateDownloadFailed_1
    AppLanguage.Czech -> CzechTranslations.aboutUpdate_updateDownloadFailed_1
    AppLanguage.Danish -> DanishTranslations.aboutUpdate_updateDownloadFailed_1
    AppLanguage.Dutch -> DutchTranslations.aboutUpdate_updateDownloadFailed_1
    AppLanguage.English -> EnglishTranslations.aboutUpdate_updateDownloadFailed_1
    AppLanguage.Estonian -> EstonianTranslations.aboutUpdate_updateDownloadFailed_1
    AppLanguage.Finnish -> FinnishTranslations.aboutUpdate_updateDownloadFailed_1
    AppLanguage.French -> FrenchTranslations.aboutUpdate_updateDownloadFailed_1
    AppLanguage.German -> GermanTranslations.aboutUpdate_updateDownloadFailed_1
    AppLanguage.Greek -> GreekTranslations.aboutUpdate_updateDownloadFailed_1
    AppLanguage.Hindi -> HindiTranslations.aboutUpdate_updateDownloadFailed_1
    AppLanguage.Hungarian -> HungarianTranslations.aboutUpdate_updateDownloadFailed_1
    AppLanguage.Indonesian -> IndonesianTranslations.aboutUpdate_updateDownloadFailed_1
    AppLanguage.Italian -> ItalianTranslations.aboutUpdate_updateDownloadFailed_1
    AppLanguage.Japanese -> JapaneseTranslations.aboutUpdate_updateDownloadFailed_1
    AppLanguage.Korean -> KoreanTranslations.aboutUpdate_updateDownloadFailed_1
    AppLanguage.Latvian -> LatvianTranslations.aboutUpdate_updateDownloadFailed_1
    AppLanguage.Lithuanian -> LithuanianTranslations.aboutUpdate_updateDownloadFailed_1
    AppLanguage.Macedonian -> MacedonianTranslations.aboutUpdate_updateDownloadFailed_1
    AppLanguage.Malay -> MalayTranslations.aboutUpdate_updateDownloadFailed_1
    AppLanguage.Norwegian -> NorwegianTranslations.aboutUpdate_updateDownloadFailed_1
    AppLanguage.Polish -> PolishTranslations.aboutUpdate_updateDownloadFailed_1
    AppLanguage.Portuguese -> PortugueseTranslations.aboutUpdate_updateDownloadFailed_1
    AppLanguage.Punjabi -> PunjabiTranslations.aboutUpdate_updateDownloadFailed_1
    AppLanguage.Romanian -> RomanianTranslations.aboutUpdate_updateDownloadFailed_1
    AppLanguage.Russian -> RussianTranslations.aboutUpdate_updateDownloadFailed_1
    AppLanguage.Serbian -> SerbianTranslations.aboutUpdate_updateDownloadFailed_1
    AppLanguage.Slovak -> SlovakTranslations.aboutUpdate_updateDownloadFailed_1
    AppLanguage.Slovenian -> SlovenianTranslations.aboutUpdate_updateDownloadFailed_1
    AppLanguage.Spanish -> SpanishTranslations.aboutUpdate_updateDownloadFailed_1
    AppLanguage.Swedish -> SwedishTranslations.aboutUpdate_updateDownloadFailed_1
    AppLanguage.Thai -> ThaiTranslations.aboutUpdate_updateDownloadFailed_1
    AppLanguage.Ukrainian -> UkrainianTranslations.aboutUpdate_updateDownloadFailed_1
    AppLanguage.Urdu -> UrduTranslations.aboutUpdate_updateDownloadFailed_1
}

private fun updateInstallPermission(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.aboutUpdate_updateInstallPermission_1
    AppLanguage.Bengali -> BengaliTranslations.aboutUpdate_updateInstallPermission_1
    AppLanguage.Bosnian -> BosnianTranslations.aboutUpdate_updateInstallPermission_1
    AppLanguage.Bulgarian -> BulgarianTranslations.aboutUpdate_updateInstallPermission_1
    AppLanguage.Catalan -> CatalanTranslations.aboutUpdate_updateInstallPermission_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.aboutUpdate_updateInstallPermission_1
    AppLanguage.Croatian -> CroatianTranslations.aboutUpdate_updateInstallPermission_1
    AppLanguage.Czech -> CzechTranslations.aboutUpdate_updateInstallPermission_1
    AppLanguage.Danish -> DanishTranslations.aboutUpdate_updateInstallPermission_1
    AppLanguage.Dutch -> DutchTranslations.aboutUpdate_updateInstallPermission_1
    AppLanguage.English -> EnglishTranslations.aboutUpdate_updateInstallPermission_1
    AppLanguage.Estonian -> EstonianTranslations.aboutUpdate_updateInstallPermission_1
    AppLanguage.Finnish -> FinnishTranslations.aboutUpdate_updateInstallPermission_1
    AppLanguage.French -> FrenchTranslations.aboutUpdate_updateInstallPermission_1
    AppLanguage.German -> GermanTranslations.aboutUpdate_updateInstallPermission_1
    AppLanguage.Greek -> GreekTranslations.aboutUpdate_updateInstallPermission_1
    AppLanguage.Hindi -> HindiTranslations.aboutUpdate_updateInstallPermission_1
    AppLanguage.Hungarian -> HungarianTranslations.aboutUpdate_updateInstallPermission_1
    AppLanguage.Indonesian -> IndonesianTranslations.aboutUpdate_updateInstallPermission_1
    AppLanguage.Italian -> ItalianTranslations.aboutUpdate_updateInstallPermission_1
    AppLanguage.Japanese -> JapaneseTranslations.aboutUpdate_updateInstallPermission_1
    AppLanguage.Korean -> KoreanTranslations.aboutUpdate_updateInstallPermission_1
    AppLanguage.Latvian -> LatvianTranslations.aboutUpdate_updateInstallPermission_1
    AppLanguage.Lithuanian -> LithuanianTranslations.aboutUpdate_updateInstallPermission_1
    AppLanguage.Macedonian -> MacedonianTranslations.aboutUpdate_updateInstallPermission_1
    AppLanguage.Malay -> MalayTranslations.aboutUpdate_updateInstallPermission_1
    AppLanguage.Norwegian -> NorwegianTranslations.aboutUpdate_updateInstallPermission_1
    AppLanguage.Polish -> PolishTranslations.aboutUpdate_updateInstallPermission_1
    AppLanguage.Portuguese -> PortugueseTranslations.aboutUpdate_updateInstallPermission_1
    AppLanguage.Punjabi -> PunjabiTranslations.aboutUpdate_updateInstallPermission_1
    AppLanguage.Romanian -> RomanianTranslations.aboutUpdate_updateInstallPermission_1
    AppLanguage.Russian -> RussianTranslations.aboutUpdate_updateInstallPermission_1
    AppLanguage.Serbian -> SerbianTranslations.aboutUpdate_updateInstallPermission_1
    AppLanguage.Slovak -> SlovakTranslations.aboutUpdate_updateInstallPermission_1
    AppLanguage.Slovenian -> SlovenianTranslations.aboutUpdate_updateInstallPermission_1
    AppLanguage.Spanish -> SpanishTranslations.aboutUpdate_updateInstallPermission_1
    AppLanguage.Swedish -> SwedishTranslations.aboutUpdate_updateInstallPermission_1
    AppLanguage.Thai -> ThaiTranslations.aboutUpdate_updateInstallPermission_1
    AppLanguage.Ukrainian -> UkrainianTranslations.aboutUpdate_updateInstallPermission_1
    AppLanguage.Urdu -> UrduTranslations.aboutUpdate_updateInstallPermission_1
}

private fun stagedUpdateUnavailable(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.aboutUpdate_stagedUpdateUnavailable_1
    AppLanguage.Bengali -> BengaliTranslations.aboutUpdate_stagedUpdateUnavailable_1
    AppLanguage.Bosnian -> BosnianTranslations.aboutUpdate_stagedUpdateUnavailable_1
    AppLanguage.Bulgarian -> BulgarianTranslations.aboutUpdate_stagedUpdateUnavailable_1
    AppLanguage.Catalan -> CatalanTranslations.aboutUpdate_stagedUpdateUnavailable_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.aboutUpdate_stagedUpdateUnavailable_1
    AppLanguage.Croatian -> CroatianTranslations.aboutUpdate_stagedUpdateUnavailable_1
    AppLanguage.Czech -> CzechTranslations.aboutUpdate_stagedUpdateUnavailable_1
    AppLanguage.Danish -> DanishTranslations.aboutUpdate_stagedUpdateUnavailable_1
    AppLanguage.Dutch -> DutchTranslations.aboutUpdate_stagedUpdateUnavailable_1
    AppLanguage.English -> EnglishTranslations.aboutUpdate_stagedUpdateUnavailable_1
    AppLanguage.Estonian -> EstonianTranslations.aboutUpdate_stagedUpdateUnavailable_1
    AppLanguage.Finnish -> FinnishTranslations.aboutUpdate_stagedUpdateUnavailable_1
    AppLanguage.French -> FrenchTranslations.aboutUpdate_stagedUpdateUnavailable_1
    AppLanguage.German -> GermanTranslations.aboutUpdate_stagedUpdateUnavailable_1
    AppLanguage.Greek -> GreekTranslations.aboutUpdate_stagedUpdateUnavailable_1
    AppLanguage.Hindi -> HindiTranslations.aboutUpdate_stagedUpdateUnavailable_1
    AppLanguage.Hungarian -> HungarianTranslations.aboutUpdate_stagedUpdateUnavailable_1
    AppLanguage.Indonesian -> IndonesianTranslations.aboutUpdate_stagedUpdateUnavailable_1
    AppLanguage.Italian -> ItalianTranslations.aboutUpdate_stagedUpdateUnavailable_1
    AppLanguage.Japanese -> JapaneseTranslations.aboutUpdate_stagedUpdateUnavailable_1
    AppLanguage.Korean -> KoreanTranslations.aboutUpdate_stagedUpdateUnavailable_1
    AppLanguage.Latvian -> LatvianTranslations.aboutUpdate_stagedUpdateUnavailable_1
    AppLanguage.Lithuanian -> LithuanianTranslations.aboutUpdate_stagedUpdateUnavailable_1
    AppLanguage.Macedonian -> MacedonianTranslations.aboutUpdate_stagedUpdateUnavailable_1
    AppLanguage.Malay -> MalayTranslations.aboutUpdate_stagedUpdateUnavailable_1
    AppLanguage.Norwegian -> NorwegianTranslations.aboutUpdate_stagedUpdateUnavailable_1
    AppLanguage.Polish -> PolishTranslations.aboutUpdate_stagedUpdateUnavailable_1
    AppLanguage.Portuguese -> PortugueseTranslations.aboutUpdate_stagedUpdateUnavailable_1
    AppLanguage.Punjabi -> PunjabiTranslations.aboutUpdate_stagedUpdateUnavailable_1
    AppLanguage.Romanian -> RomanianTranslations.aboutUpdate_stagedUpdateUnavailable_1
    AppLanguage.Russian -> RussianTranslations.aboutUpdate_stagedUpdateUnavailable_1
    AppLanguage.Serbian -> SerbianTranslations.aboutUpdate_stagedUpdateUnavailable_1
    AppLanguage.Slovak -> SlovakTranslations.aboutUpdate_stagedUpdateUnavailable_1
    AppLanguage.Slovenian -> SlovenianTranslations.aboutUpdate_stagedUpdateUnavailable_1
    AppLanguage.Spanish -> SpanishTranslations.aboutUpdate_stagedUpdateUnavailable_1
    AppLanguage.Swedish -> SwedishTranslations.aboutUpdate_stagedUpdateUnavailable_1
    AppLanguage.Thai -> ThaiTranslations.aboutUpdate_stagedUpdateUnavailable_1
    AppLanguage.Ukrainian -> UkrainianTranslations.aboutUpdate_stagedUpdateUnavailable_1
    AppLanguage.Urdu -> UrduTranslations.aboutUpdate_stagedUpdateUnavailable_1
}

internal data class AboutUpdateCopy(
    val updateAvailable: String,
    val version: String,
    val later: String,
    val download: String,
    val closeChangelog: String,
    val noChangelog: String,
)

internal fun aboutUpdateCopy(language: AppLanguage): AboutUpdateCopy = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.aboutUpdate_aboutUpdateCopy_1
    AppLanguage.Bengali -> BengaliTranslations.aboutUpdate_aboutUpdateCopy_1
    AppLanguage.Bosnian -> BosnianTranslations.aboutUpdate_aboutUpdateCopy_1
    AppLanguage.Bulgarian -> BulgarianTranslations.aboutUpdate_aboutUpdateCopy_1
    AppLanguage.Catalan -> CatalanTranslations.aboutUpdate_aboutUpdateCopy_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.aboutUpdate_aboutUpdateCopy_1
    AppLanguage.Croatian -> CroatianTranslations.aboutUpdate_aboutUpdateCopy_1
    AppLanguage.Czech -> CzechTranslations.aboutUpdate_aboutUpdateCopy_1
    AppLanguage.Danish -> DanishTranslations.aboutUpdate_aboutUpdateCopy_1
    AppLanguage.Dutch -> DutchTranslations.aboutUpdate_aboutUpdateCopy_1
    AppLanguage.English -> EnglishTranslations.aboutUpdate_aboutUpdateCopy_1
    AppLanguage.Estonian -> EstonianTranslations.aboutUpdate_aboutUpdateCopy_1
    AppLanguage.Finnish -> FinnishTranslations.aboutUpdate_aboutUpdateCopy_1
    AppLanguage.French -> FrenchTranslations.aboutUpdate_aboutUpdateCopy_1
    AppLanguage.German -> GermanTranslations.aboutUpdate_aboutUpdateCopy_1
    AppLanguage.Greek -> GreekTranslations.aboutUpdate_aboutUpdateCopy_1
    AppLanguage.Hindi -> HindiTranslations.aboutUpdate_aboutUpdateCopy_1
    AppLanguage.Hungarian -> HungarianTranslations.aboutUpdate_aboutUpdateCopy_1
    AppLanguage.Indonesian -> IndonesianTranslations.aboutUpdate_aboutUpdateCopy_1
    AppLanguage.Italian -> ItalianTranslations.aboutUpdate_aboutUpdateCopy_1
    AppLanguage.Japanese -> JapaneseTranslations.aboutUpdate_aboutUpdateCopy_1
    AppLanguage.Korean -> KoreanTranslations.aboutUpdate_aboutUpdateCopy_1
    AppLanguage.Latvian -> LatvianTranslations.aboutUpdate_aboutUpdateCopy_1
    AppLanguage.Lithuanian -> LithuanianTranslations.aboutUpdate_aboutUpdateCopy_1
    AppLanguage.Macedonian -> MacedonianTranslations.aboutUpdate_aboutUpdateCopy_1
    AppLanguage.Malay -> MalayTranslations.aboutUpdate_aboutUpdateCopy_1
    AppLanguage.Norwegian -> NorwegianTranslations.aboutUpdate_aboutUpdateCopy_1
    AppLanguage.Polish -> PolishTranslations.aboutUpdate_aboutUpdateCopy_1
    AppLanguage.Portuguese -> PortugueseTranslations.aboutUpdate_aboutUpdateCopy_1
    AppLanguage.Punjabi -> PunjabiTranslations.aboutUpdate_aboutUpdateCopy_1
    AppLanguage.Romanian -> RomanianTranslations.aboutUpdate_aboutUpdateCopy_1
    AppLanguage.Russian -> RussianTranslations.aboutUpdate_aboutUpdateCopy_1
    AppLanguage.Serbian -> SerbianTranslations.aboutUpdate_aboutUpdateCopy_1
    AppLanguage.Slovak -> SlovakTranslations.aboutUpdate_aboutUpdateCopy_1
    AppLanguage.Slovenian -> SlovenianTranslations.aboutUpdate_aboutUpdateCopy_1
    AppLanguage.Spanish -> SpanishTranslations.aboutUpdate_aboutUpdateCopy_1
    AppLanguage.Swedish -> SwedishTranslations.aboutUpdate_aboutUpdateCopy_1
    AppLanguage.Thai -> ThaiTranslations.aboutUpdate_aboutUpdateCopy_1
    AppLanguage.Ukrainian -> UkrainianTranslations.aboutUpdate_aboutUpdateCopy_1
    AppLanguage.Urdu -> UrduTranslations.aboutUpdate_aboutUpdateCopy_1
}

internal data class UpdateActionCopy(
    val checking: String,
    val downloading: String,
    val installing: String,
    val allowInstall: String,
    val update: String,
    val retry: String,
)

internal fun updateActionCopy(language: AppLanguage): UpdateActionCopy = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.aboutUpdate_updateActionCopy_1
    AppLanguage.Bengali -> BengaliTranslations.aboutUpdate_updateActionCopy_1
    AppLanguage.Bosnian -> BosnianTranslations.aboutUpdate_updateActionCopy_1
    AppLanguage.Bulgarian -> BulgarianTranslations.aboutUpdate_updateActionCopy_1
    AppLanguage.Catalan -> CatalanTranslations.aboutUpdate_updateActionCopy_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.aboutUpdate_updateActionCopy_1
    AppLanguage.Croatian -> CroatianTranslations.aboutUpdate_updateActionCopy_1
    AppLanguage.Czech -> CzechTranslations.aboutUpdate_updateActionCopy_1
    AppLanguage.Danish -> DanishTranslations.aboutUpdate_updateActionCopy_1
    AppLanguage.Dutch -> DutchTranslations.aboutUpdate_updateActionCopy_1
    AppLanguage.English -> EnglishTranslations.aboutUpdate_updateActionCopy_1
    AppLanguage.Estonian -> EstonianTranslations.aboutUpdate_updateActionCopy_1
    AppLanguage.Finnish -> FinnishTranslations.aboutUpdate_updateActionCopy_1
    AppLanguage.French -> FrenchTranslations.aboutUpdate_updateActionCopy_1
    AppLanguage.German -> GermanTranslations.aboutUpdate_updateActionCopy_1
    AppLanguage.Greek -> GreekTranslations.aboutUpdate_updateActionCopy_1
    AppLanguage.Hindi -> HindiTranslations.aboutUpdate_updateActionCopy_1
    AppLanguage.Hungarian -> HungarianTranslations.aboutUpdate_updateActionCopy_1
    AppLanguage.Indonesian -> IndonesianTranslations.aboutUpdate_updateActionCopy_1
    AppLanguage.Italian -> ItalianTranslations.aboutUpdate_updateActionCopy_1
    AppLanguage.Japanese -> JapaneseTranslations.aboutUpdate_updateActionCopy_1
    AppLanguage.Korean -> KoreanTranslations.aboutUpdate_updateActionCopy_1
    AppLanguage.Latvian -> LatvianTranslations.aboutUpdate_updateActionCopy_1
    AppLanguage.Lithuanian -> LithuanianTranslations.aboutUpdate_updateActionCopy_1
    AppLanguage.Macedonian -> MacedonianTranslations.aboutUpdate_updateActionCopy_1
    AppLanguage.Malay -> MalayTranslations.aboutUpdate_updateActionCopy_1
    AppLanguage.Norwegian -> NorwegianTranslations.aboutUpdate_updateActionCopy_1
    AppLanguage.Polish -> PolishTranslations.aboutUpdate_updateActionCopy_1
    AppLanguage.Portuguese -> PortugueseTranslations.aboutUpdate_updateActionCopy_1
    AppLanguage.Punjabi -> PunjabiTranslations.aboutUpdate_updateActionCopy_1
    AppLanguage.Romanian -> RomanianTranslations.aboutUpdate_updateActionCopy_1
    AppLanguage.Russian -> RussianTranslations.aboutUpdate_updateActionCopy_1
    AppLanguage.Serbian -> SerbianTranslations.aboutUpdate_updateActionCopy_1
    AppLanguage.Slovak -> SlovakTranslations.aboutUpdate_updateActionCopy_1
    AppLanguage.Slovenian -> SlovenianTranslations.aboutUpdate_updateActionCopy_1
    AppLanguage.Spanish -> SpanishTranslations.aboutUpdate_updateActionCopy_1
    AppLanguage.Swedish -> SwedishTranslations.aboutUpdate_updateActionCopy_1
    AppLanguage.Thai -> ThaiTranslations.aboutUpdate_updateActionCopy_1
    AppLanguage.Ukrainian -> UkrainianTranslations.aboutUpdate_updateActionCopy_1
    AppLanguage.Urdu -> UrduTranslations.aboutUpdate_updateActionCopy_1
}
