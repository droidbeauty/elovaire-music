package elovaire.music.droidbeauty.app.ui.i18n

import elovaire.music.droidbeauty.app.domain.model.AppLanguage

internal fun localizedPlaylistCreateError(message: String, language: AppLanguage): String {
    if (message == "Database operation failed.") return playlistDatabaseFailure(language)
    if (message == "User-data operation failed.") return playlistUserDataOperationFailure(language)
    if (message == "User-data store failed.") return playlistUserDataStoreFailure(language)
    if (message == "User-data actor failed.") return playlistUserDataStoreFailure(language)
    if (message == "Mutation did not produce a result.") return playlistMutationNoResult(language)
    if (message == "User-data store is released.") return playlistUserDataStoreClosed(language)
    if (message != "User-data queue is full.") return message
    return when (language) {
        AppLanguage.Albanian -> AlbanianTranslations.playlistFeedback_localizedPlaylistCreateError_1
        AppLanguage.Bengali -> BengaliTranslations.playlistFeedback_localizedPlaylistCreateError_1
        AppLanguage.Bosnian -> BosnianTranslations.playlistFeedback_localizedPlaylistCreateError_1
        AppLanguage.Bulgarian -> BulgarianTranslations.playlistFeedback_localizedPlaylistCreateError_1
        AppLanguage.Catalan -> CatalanTranslations.playlistFeedback_localizedPlaylistCreateError_1
        AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.playlistFeedback_localizedPlaylistCreateError_1
        AppLanguage.Croatian -> CroatianTranslations.playlistFeedback_localizedPlaylistCreateError_1
        AppLanguage.Czech -> CzechTranslations.playlistFeedback_localizedPlaylistCreateError_1
        AppLanguage.Danish -> DanishTranslations.playlistFeedback_localizedPlaylistCreateError_1
        AppLanguage.Dutch -> DutchTranslations.playlistFeedback_localizedPlaylistCreateError_1
        AppLanguage.English -> EnglishTranslations.playlistFeedback_localizedPlaylistCreateError_1
        AppLanguage.Estonian -> EstonianTranslations.playlistFeedback_localizedPlaylistCreateError_1
        AppLanguage.Finnish -> FinnishTranslations.playlistFeedback_localizedPlaylistCreateError_1
        AppLanguage.French -> FrenchTranslations.playlistFeedback_localizedPlaylistCreateError_1
        AppLanguage.German -> GermanTranslations.playlistFeedback_localizedPlaylistCreateError_1
        AppLanguage.Greek -> GreekTranslations.playlistFeedback_localizedPlaylistCreateError_1
        AppLanguage.Hindi -> HindiTranslations.playlistFeedback_localizedPlaylistCreateError_1
        AppLanguage.Hungarian -> HungarianTranslations.playlistFeedback_localizedPlaylistCreateError_1
        AppLanguage.Indonesian -> IndonesianTranslations.playlistFeedback_localizedPlaylistCreateError_1
        AppLanguage.Italian -> ItalianTranslations.playlistFeedback_localizedPlaylistCreateError_1
        AppLanguage.Japanese -> JapaneseTranslations.playlistFeedback_localizedPlaylistCreateError_1
        AppLanguage.Korean -> KoreanTranslations.playlistFeedback_localizedPlaylistCreateError_1
        AppLanguage.Latvian -> LatvianTranslations.playlistFeedback_localizedPlaylistCreateError_1
        AppLanguage.Lithuanian -> LithuanianTranslations.playlistFeedback_localizedPlaylistCreateError_1
        AppLanguage.Macedonian -> MacedonianTranslations.playlistFeedback_localizedPlaylistCreateError_1
        AppLanguage.Malay -> MalayTranslations.playlistFeedback_localizedPlaylistCreateError_1
        AppLanguage.Norwegian -> NorwegianTranslations.playlistFeedback_localizedPlaylistCreateError_1
        AppLanguage.Polish -> PolishTranslations.playlistFeedback_localizedPlaylistCreateError_1
        AppLanguage.Portuguese -> PortugueseTranslations.playlistFeedback_localizedPlaylistCreateError_1
        AppLanguage.Punjabi -> PunjabiTranslations.playlistFeedback_localizedPlaylistCreateError_1
        AppLanguage.Romanian -> RomanianTranslations.playlistFeedback_localizedPlaylistCreateError_1
        AppLanguage.Russian -> RussianTranslations.playlistFeedback_localizedPlaylistCreateError_1
        AppLanguage.Serbian -> SerbianTranslations.playlistFeedback_localizedPlaylistCreateError_1
        AppLanguage.Slovak -> SlovakTranslations.playlistFeedback_localizedPlaylistCreateError_1
        AppLanguage.Slovenian -> SlovenianTranslations.playlistFeedback_localizedPlaylistCreateError_1
        AppLanguage.Spanish -> SpanishTranslations.playlistFeedback_localizedPlaylistCreateError_1
        AppLanguage.Swedish -> SwedishTranslations.playlistFeedback_localizedPlaylistCreateError_1
        AppLanguage.Thai -> ThaiTranslations.playlistFeedback_localizedPlaylistCreateError_1
        AppLanguage.Ukrainian -> UkrainianTranslations.playlistFeedback_localizedPlaylistCreateError_1
        AppLanguage.Urdu -> UrduTranslations.playlistFeedback_localizedPlaylistCreateError_1
    }
}

private fun playlistMutationNoResult(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.playlistFeedback_playlistMutationNoResult_1
    AppLanguage.Bengali -> BengaliTranslations.playlistFeedback_playlistMutationNoResult_1
    AppLanguage.Bosnian -> BosnianTranslations.playlistFeedback_playlistMutationNoResult_1
    AppLanguage.Bulgarian -> BulgarianTranslations.playlistFeedback_playlistMutationNoResult_1
    AppLanguage.Catalan -> CatalanTranslations.playlistFeedback_playlistMutationNoResult_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.playlistFeedback_playlistMutationNoResult_1
    AppLanguage.Croatian -> CroatianTranslations.playlistFeedback_playlistMutationNoResult_1
    AppLanguage.Czech -> CzechTranslations.playlistFeedback_playlistMutationNoResult_1
    AppLanguage.Danish -> DanishTranslations.playlistFeedback_playlistMutationNoResult_1
    AppLanguage.Dutch -> DutchTranslations.playlistFeedback_playlistMutationNoResult_1
    AppLanguage.English -> EnglishTranslations.playlistFeedback_playlistMutationNoResult_1
    AppLanguage.Estonian -> EstonianTranslations.playlistFeedback_playlistMutationNoResult_1
    AppLanguage.Finnish -> FinnishTranslations.playlistFeedback_playlistMutationNoResult_1
    AppLanguage.French -> FrenchTranslations.playlistFeedback_playlistMutationNoResult_1
    AppLanguage.German -> GermanTranslations.playlistFeedback_playlistMutationNoResult_1
    AppLanguage.Greek -> GreekTranslations.playlistFeedback_playlistMutationNoResult_1
    AppLanguage.Hindi -> HindiTranslations.playlistFeedback_playlistMutationNoResult_1
    AppLanguage.Hungarian -> HungarianTranslations.playlistFeedback_playlistMutationNoResult_1
    AppLanguage.Indonesian -> IndonesianTranslations.playlistFeedback_playlistMutationNoResult_1
    AppLanguage.Italian -> ItalianTranslations.playlistFeedback_playlistMutationNoResult_1
    AppLanguage.Japanese -> JapaneseTranslations.playlistFeedback_playlistMutationNoResult_1
    AppLanguage.Korean -> KoreanTranslations.playlistFeedback_playlistMutationNoResult_1
    AppLanguage.Latvian -> LatvianTranslations.playlistFeedback_playlistMutationNoResult_1
    AppLanguage.Lithuanian -> LithuanianTranslations.playlistFeedback_playlistMutationNoResult_1
    AppLanguage.Macedonian -> MacedonianTranslations.playlistFeedback_playlistMutationNoResult_1
    AppLanguage.Malay -> MalayTranslations.playlistFeedback_playlistMutationNoResult_1
    AppLanguage.Norwegian -> NorwegianTranslations.playlistFeedback_playlistMutationNoResult_1
    AppLanguage.Polish -> PolishTranslations.playlistFeedback_playlistMutationNoResult_1
    AppLanguage.Portuguese -> PortugueseTranslations.playlistFeedback_playlistMutationNoResult_1
    AppLanguage.Punjabi -> PunjabiTranslations.playlistFeedback_playlistMutationNoResult_1
    AppLanguage.Romanian -> RomanianTranslations.playlistFeedback_playlistMutationNoResult_1
    AppLanguage.Russian -> RussianTranslations.playlistFeedback_playlistMutationNoResult_1
    AppLanguage.Serbian -> SerbianTranslations.playlistFeedback_playlistMutationNoResult_1
    AppLanguage.Slovak -> SlovakTranslations.playlistFeedback_playlistMutationNoResult_1
    AppLanguage.Slovenian -> SlovenianTranslations.playlistFeedback_playlistMutationNoResult_1
    AppLanguage.Spanish -> SpanishTranslations.playlistFeedback_playlistMutationNoResult_1
    AppLanguage.Swedish -> SwedishTranslations.playlistFeedback_playlistMutationNoResult_1
    AppLanguage.Thai -> ThaiTranslations.playlistFeedback_playlistMutationNoResult_1
    AppLanguage.Ukrainian -> UkrainianTranslations.playlistFeedback_playlistMutationNoResult_1
    AppLanguage.Urdu -> UrduTranslations.playlistFeedback_playlistMutationNoResult_1
}

private fun playlistUserDataStoreClosed(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.playlistFeedback_playlistUserDataStoreClosed_1
    AppLanguage.Bengali -> BengaliTranslations.playlistFeedback_playlistUserDataStoreClosed_1
    AppLanguage.Bosnian -> BosnianTranslations.playlistFeedback_playlistUserDataStoreClosed_1
    AppLanguage.Bulgarian -> BulgarianTranslations.playlistFeedback_playlistUserDataStoreClosed_1
    AppLanguage.Catalan -> CatalanTranslations.playlistFeedback_playlistUserDataStoreClosed_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.playlistFeedback_playlistUserDataStoreClosed_1
    AppLanguage.Croatian -> CroatianTranslations.playlistFeedback_playlistUserDataStoreClosed_1
    AppLanguage.Czech -> CzechTranslations.playlistFeedback_playlistUserDataStoreClosed_1
    AppLanguage.Danish -> DanishTranslations.playlistFeedback_playlistUserDataStoreClosed_1
    AppLanguage.Dutch -> DutchTranslations.playlistFeedback_playlistUserDataStoreClosed_1
    AppLanguage.English -> EnglishTranslations.playlistFeedback_playlistUserDataStoreClosed_1
    AppLanguage.Estonian -> EstonianTranslations.playlistFeedback_playlistUserDataStoreClosed_1
    AppLanguage.Finnish -> FinnishTranslations.playlistFeedback_playlistUserDataStoreClosed_1
    AppLanguage.French -> FrenchTranslations.playlistFeedback_playlistUserDataStoreClosed_1
    AppLanguage.German -> GermanTranslations.playlistFeedback_playlistUserDataStoreClosed_1
    AppLanguage.Greek -> GreekTranslations.playlistFeedback_playlistUserDataStoreClosed_1
    AppLanguage.Hindi -> HindiTranslations.playlistFeedback_playlistUserDataStoreClosed_1
    AppLanguage.Hungarian -> HungarianTranslations.playlistFeedback_playlistUserDataStoreClosed_1
    AppLanguage.Indonesian -> IndonesianTranslations.playlistFeedback_playlistUserDataStoreClosed_1
    AppLanguage.Italian -> ItalianTranslations.playlistFeedback_playlistUserDataStoreClosed_1
    AppLanguage.Japanese -> JapaneseTranslations.playlistFeedback_playlistUserDataStoreClosed_1
    AppLanguage.Korean -> KoreanTranslations.playlistFeedback_playlistUserDataStoreClosed_1
    AppLanguage.Latvian -> LatvianTranslations.playlistFeedback_playlistUserDataStoreClosed_1
    AppLanguage.Lithuanian -> LithuanianTranslations.playlistFeedback_playlistUserDataStoreClosed_1
    AppLanguage.Macedonian -> MacedonianTranslations.playlistFeedback_playlistUserDataStoreClosed_1
    AppLanguage.Malay -> MalayTranslations.playlistFeedback_playlistUserDataStoreClosed_1
    AppLanguage.Norwegian -> NorwegianTranslations.playlistFeedback_playlistUserDataStoreClosed_1
    AppLanguage.Polish -> PolishTranslations.playlistFeedback_playlistUserDataStoreClosed_1
    AppLanguage.Portuguese -> PortugueseTranslations.playlistFeedback_playlistUserDataStoreClosed_1
    AppLanguage.Punjabi -> PunjabiTranslations.playlistFeedback_playlistUserDataStoreClosed_1
    AppLanguage.Romanian -> RomanianTranslations.playlistFeedback_playlistUserDataStoreClosed_1
    AppLanguage.Russian -> RussianTranslations.playlistFeedback_playlistUserDataStoreClosed_1
    AppLanguage.Serbian -> SerbianTranslations.playlistFeedback_playlistUserDataStoreClosed_1
    AppLanguage.Slovak -> SlovakTranslations.playlistFeedback_playlistUserDataStoreClosed_1
    AppLanguage.Slovenian -> SlovenianTranslations.playlistFeedback_playlistUserDataStoreClosed_1
    AppLanguage.Spanish -> SpanishTranslations.playlistFeedback_playlistUserDataStoreClosed_1
    AppLanguage.Swedish -> SwedishTranslations.playlistFeedback_playlistUserDataStoreClosed_1
    AppLanguage.Thai -> ThaiTranslations.playlistFeedback_playlistUserDataStoreClosed_1
    AppLanguage.Ukrainian -> UkrainianTranslations.playlistFeedback_playlistUserDataStoreClosed_1
    AppLanguage.Urdu -> UrduTranslations.playlistFeedback_playlistUserDataStoreClosed_1
}

private fun playlistUserDataStoreFailure(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.playlistFeedback_playlistUserDataStoreFailure_1
    AppLanguage.Bengali -> BengaliTranslations.playlistFeedback_playlistUserDataStoreFailure_1
    AppLanguage.Bosnian -> BosnianTranslations.playlistFeedback_playlistUserDataStoreFailure_1
    AppLanguage.Bulgarian -> BulgarianTranslations.playlistFeedback_playlistUserDataStoreFailure_1
    AppLanguage.Catalan -> CatalanTranslations.playlistFeedback_playlistUserDataStoreFailure_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.playlistFeedback_playlistUserDataStoreFailure_1
    AppLanguage.Croatian -> CroatianTranslations.playlistFeedback_playlistUserDataStoreFailure_1
    AppLanguage.Czech -> CzechTranslations.playlistFeedback_playlistUserDataStoreFailure_1
    AppLanguage.Danish -> DanishTranslations.playlistFeedback_playlistUserDataStoreFailure_1
    AppLanguage.Dutch -> DutchTranslations.playlistFeedback_playlistUserDataStoreFailure_1
    AppLanguage.English -> EnglishTranslations.playlistFeedback_playlistUserDataStoreFailure_1
    AppLanguage.Estonian -> EstonianTranslations.playlistFeedback_playlistUserDataStoreFailure_1
    AppLanguage.Finnish -> FinnishTranslations.playlistFeedback_playlistUserDataStoreFailure_1
    AppLanguage.French -> FrenchTranslations.playlistFeedback_playlistUserDataStoreFailure_1
    AppLanguage.German -> GermanTranslations.playlistFeedback_playlistUserDataStoreFailure_1
    AppLanguage.Greek -> GreekTranslations.playlistFeedback_playlistUserDataStoreFailure_1
    AppLanguage.Hindi -> HindiTranslations.playlistFeedback_playlistUserDataStoreFailure_1
    AppLanguage.Hungarian -> HungarianTranslations.playlistFeedback_playlistUserDataStoreFailure_1
    AppLanguage.Indonesian -> IndonesianTranslations.playlistFeedback_playlistUserDataStoreFailure_1
    AppLanguage.Italian -> ItalianTranslations.playlistFeedback_playlistUserDataStoreFailure_1
    AppLanguage.Japanese -> JapaneseTranslations.playlistFeedback_playlistUserDataStoreFailure_1
    AppLanguage.Korean -> KoreanTranslations.playlistFeedback_playlistUserDataStoreFailure_1
    AppLanguage.Latvian -> LatvianTranslations.playlistFeedback_playlistUserDataStoreFailure_1
    AppLanguage.Lithuanian -> LithuanianTranslations.playlistFeedback_playlistUserDataStoreFailure_1
    AppLanguage.Macedonian -> MacedonianTranslations.playlistFeedback_playlistUserDataStoreFailure_1
    AppLanguage.Malay -> MalayTranslations.playlistFeedback_playlistUserDataStoreFailure_1
    AppLanguage.Norwegian -> NorwegianTranslations.playlistFeedback_playlistUserDataStoreFailure_1
    AppLanguage.Polish -> PolishTranslations.playlistFeedback_playlistUserDataStoreFailure_1
    AppLanguage.Portuguese -> PortugueseTranslations.playlistFeedback_playlistUserDataStoreFailure_1
    AppLanguage.Punjabi -> PunjabiTranslations.playlistFeedback_playlistUserDataStoreFailure_1
    AppLanguage.Romanian -> RomanianTranslations.playlistFeedback_playlistUserDataStoreFailure_1
    AppLanguage.Russian -> RussianTranslations.playlistFeedback_playlistUserDataStoreFailure_1
    AppLanguage.Serbian -> SerbianTranslations.playlistFeedback_playlistUserDataStoreFailure_1
    AppLanguage.Slovak -> SlovakTranslations.playlistFeedback_playlistUserDataStoreFailure_1
    AppLanguage.Slovenian -> SlovenianTranslations.playlistFeedback_playlistUserDataStoreFailure_1
    AppLanguage.Spanish -> SpanishTranslations.playlistFeedback_playlistUserDataStoreFailure_1
    AppLanguage.Swedish -> SwedishTranslations.playlistFeedback_playlistUserDataStoreFailure_1
    AppLanguage.Thai -> ThaiTranslations.playlistFeedback_playlistUserDataStoreFailure_1
    AppLanguage.Ukrainian -> UkrainianTranslations.playlistFeedback_playlistUserDataStoreFailure_1
    AppLanguage.Urdu -> UrduTranslations.playlistFeedback_playlistUserDataStoreFailure_1
}

private fun playlistUserDataOperationFailure(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.playlistFeedback_playlistUserDataOperationFailure_1
    AppLanguage.Bengali -> BengaliTranslations.playlistFeedback_playlistUserDataOperationFailure_1
    AppLanguage.Bosnian -> BosnianTranslations.playlistFeedback_playlistUserDataOperationFailure_1
    AppLanguage.Bulgarian -> BulgarianTranslations.playlistFeedback_playlistUserDataOperationFailure_1
    AppLanguage.Catalan -> CatalanTranslations.playlistFeedback_playlistUserDataOperationFailure_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.playlistFeedback_playlistUserDataOperationFailure_1
    AppLanguage.Croatian -> CroatianTranslations.playlistFeedback_playlistUserDataOperationFailure_1
    AppLanguage.Czech -> CzechTranslations.playlistFeedback_playlistUserDataOperationFailure_1
    AppLanguage.Danish -> DanishTranslations.playlistFeedback_playlistUserDataOperationFailure_1
    AppLanguage.Dutch -> DutchTranslations.playlistFeedback_playlistUserDataOperationFailure_1
    AppLanguage.English -> EnglishTranslations.playlistFeedback_playlistUserDataOperationFailure_1
    AppLanguage.Estonian -> EstonianTranslations.playlistFeedback_playlistUserDataOperationFailure_1
    AppLanguage.Finnish -> FinnishTranslations.playlistFeedback_playlistUserDataOperationFailure_1
    AppLanguage.French -> FrenchTranslations.playlistFeedback_playlistUserDataOperationFailure_1
    AppLanguage.German -> GermanTranslations.playlistFeedback_playlistUserDataOperationFailure_1
    AppLanguage.Greek -> GreekTranslations.playlistFeedback_playlistUserDataOperationFailure_1
    AppLanguage.Hindi -> HindiTranslations.playlistFeedback_playlistUserDataOperationFailure_1
    AppLanguage.Hungarian -> HungarianTranslations.playlistFeedback_playlistUserDataOperationFailure_1
    AppLanguage.Indonesian -> IndonesianTranslations.playlistFeedback_playlistUserDataOperationFailure_1
    AppLanguage.Italian -> ItalianTranslations.playlistFeedback_playlistUserDataOperationFailure_1
    AppLanguage.Japanese -> JapaneseTranslations.playlistFeedback_playlistUserDataOperationFailure_1
    AppLanguage.Korean -> KoreanTranslations.playlistFeedback_playlistUserDataOperationFailure_1
    AppLanguage.Latvian -> LatvianTranslations.playlistFeedback_playlistUserDataOperationFailure_1
    AppLanguage.Lithuanian -> LithuanianTranslations.playlistFeedback_playlistUserDataOperationFailure_1
    AppLanguage.Macedonian -> MacedonianTranslations.playlistFeedback_playlistUserDataOperationFailure_1
    AppLanguage.Malay -> MalayTranslations.playlistFeedback_playlistUserDataOperationFailure_1
    AppLanguage.Norwegian -> NorwegianTranslations.playlistFeedback_playlistUserDataOperationFailure_1
    AppLanguage.Polish -> PolishTranslations.playlistFeedback_playlistUserDataOperationFailure_1
    AppLanguage.Portuguese -> PortugueseTranslations.playlistFeedback_playlistUserDataOperationFailure_1
    AppLanguage.Punjabi -> PunjabiTranslations.playlistFeedback_playlistUserDataOperationFailure_1
    AppLanguage.Romanian -> RomanianTranslations.playlistFeedback_playlistUserDataOperationFailure_1
    AppLanguage.Russian -> RussianTranslations.playlistFeedback_playlistUserDataOperationFailure_1
    AppLanguage.Serbian -> SerbianTranslations.playlistFeedback_playlistUserDataOperationFailure_1
    AppLanguage.Slovak -> SlovakTranslations.playlistFeedback_playlistUserDataOperationFailure_1
    AppLanguage.Slovenian -> SlovenianTranslations.playlistFeedback_playlistUserDataOperationFailure_1
    AppLanguage.Spanish -> SpanishTranslations.playlistFeedback_playlistUserDataOperationFailure_1
    AppLanguage.Swedish -> SwedishTranslations.playlistFeedback_playlistUserDataOperationFailure_1
    AppLanguage.Thai -> ThaiTranslations.playlistFeedback_playlistUserDataOperationFailure_1
    AppLanguage.Ukrainian -> UkrainianTranslations.playlistFeedback_playlistUserDataOperationFailure_1
    AppLanguage.Urdu -> UrduTranslations.playlistFeedback_playlistUserDataOperationFailure_1
}

private fun playlistDatabaseFailure(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.playlistFeedback_playlistDatabaseFailure_1
    AppLanguage.Bengali -> BengaliTranslations.playlistFeedback_playlistDatabaseFailure_1
    AppLanguage.Bosnian -> BosnianTranslations.playlistFeedback_playlistDatabaseFailure_1
    AppLanguage.Bulgarian -> BulgarianTranslations.playlistFeedback_playlistDatabaseFailure_1
    AppLanguage.Catalan -> CatalanTranslations.playlistFeedback_playlistDatabaseFailure_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.playlistFeedback_playlistDatabaseFailure_1
    AppLanguage.Croatian -> CroatianTranslations.playlistFeedback_playlistDatabaseFailure_1
    AppLanguage.Czech -> CzechTranslations.playlistFeedback_playlistDatabaseFailure_1
    AppLanguage.Danish -> DanishTranslations.playlistFeedback_playlistDatabaseFailure_1
    AppLanguage.Dutch -> DutchTranslations.playlistFeedback_playlistDatabaseFailure_1
    AppLanguage.English -> EnglishTranslations.playlistFeedback_playlistDatabaseFailure_1
    AppLanguage.Estonian -> EstonianTranslations.playlistFeedback_playlistDatabaseFailure_1
    AppLanguage.Finnish -> FinnishTranslations.playlistFeedback_playlistDatabaseFailure_1
    AppLanguage.French -> FrenchTranslations.playlistFeedback_playlistDatabaseFailure_1
    AppLanguage.German -> GermanTranslations.playlistFeedback_playlistDatabaseFailure_1
    AppLanguage.Greek -> GreekTranslations.playlistFeedback_playlistDatabaseFailure_1
    AppLanguage.Hindi -> HindiTranslations.playlistFeedback_playlistDatabaseFailure_1
    AppLanguage.Hungarian -> HungarianTranslations.playlistFeedback_playlistDatabaseFailure_1
    AppLanguage.Indonesian -> IndonesianTranslations.playlistFeedback_playlistDatabaseFailure_1
    AppLanguage.Italian -> ItalianTranslations.playlistFeedback_playlistDatabaseFailure_1
    AppLanguage.Japanese -> JapaneseTranslations.playlistFeedback_playlistDatabaseFailure_1
    AppLanguage.Korean -> KoreanTranslations.playlistFeedback_playlistDatabaseFailure_1
    AppLanguage.Latvian -> LatvianTranslations.playlistFeedback_playlistDatabaseFailure_1
    AppLanguage.Lithuanian -> LithuanianTranslations.playlistFeedback_playlistDatabaseFailure_1
    AppLanguage.Macedonian -> MacedonianTranslations.playlistFeedback_playlistDatabaseFailure_1
    AppLanguage.Malay -> MalayTranslations.playlistFeedback_playlistDatabaseFailure_1
    AppLanguage.Norwegian -> NorwegianTranslations.playlistFeedback_playlistDatabaseFailure_1
    AppLanguage.Polish -> PolishTranslations.playlistFeedback_playlistDatabaseFailure_1
    AppLanguage.Portuguese -> PortugueseTranslations.playlistFeedback_playlistDatabaseFailure_1
    AppLanguage.Punjabi -> PunjabiTranslations.playlistFeedback_playlistDatabaseFailure_1
    AppLanguage.Romanian -> RomanianTranslations.playlistFeedback_playlistDatabaseFailure_1
    AppLanguage.Russian -> RussianTranslations.playlistFeedback_playlistDatabaseFailure_1
    AppLanguage.Serbian -> SerbianTranslations.playlistFeedback_playlistDatabaseFailure_1
    AppLanguage.Slovak -> SlovakTranslations.playlistFeedback_playlistDatabaseFailure_1
    AppLanguage.Slovenian -> SlovenianTranslations.playlistFeedback_playlistDatabaseFailure_1
    AppLanguage.Spanish -> SpanishTranslations.playlistFeedback_playlistDatabaseFailure_1
    AppLanguage.Swedish -> SwedishTranslations.playlistFeedback_playlistDatabaseFailure_1
    AppLanguage.Thai -> ThaiTranslations.playlistFeedback_playlistDatabaseFailure_1
    AppLanguage.Ukrainian -> UkrainianTranslations.playlistFeedback_playlistDatabaseFailure_1
    AppLanguage.Urdu -> UrduTranslations.playlistFeedback_playlistDatabaseFailure_1
}

