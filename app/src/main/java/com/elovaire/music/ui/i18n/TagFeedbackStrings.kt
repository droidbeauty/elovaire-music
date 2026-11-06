package elovaire.music.droidbeauty.app.ui.i18n

import elovaire.music.droidbeauty.app.domain.model.AppLanguage
import elovaire.music.droidbeauty.app.data.tags.TagEditFailureCause
import elovaire.music.droidbeauty.app.data.tags.TagEditValidationFailure
import elovaire.music.droidbeauty.app.data.lyrics.EmbeddedLyricsWriteFailure
import elovaire.music.droidbeauty.app.data.lyrics.userMessage
import elovaire.music.droidbeauty.app.ui.screens.tags.TagEditFailureUi
import elovaire.music.droidbeauty.app.ui.screens.tags.TagValidationIssue
import elovaire.music.droidbeauty.app.ui.screens.tags.TagValidationKind
import elovaire.music.droidbeauty.app.ui.screens.LYRICS_PERMISSION_DENIED_MESSAGE
import elovaire.music.droidbeauty.app.ui.screens.LYRICS_POST_GRANT_FAILURE_MESSAGE

internal fun localizedLyricsSaveError(message: String, language: AppLanguage): String = when (message) {
    LYRICS_PERMISSION_DENIED_MESSAGE -> tagWriteAccessNotGranted(language)
    LYRICS_POST_GRANT_FAILURE_MESSAGE -> tagPermissionDeniedAfterGrant(language)
    EmbeddedLyricsWriteFailure.WriteAccessStillUnavailableAfterGrant.userMessage -> tagPermissionDeniedAfterGrant(language)
    EmbeddedLyricsWriteFailure.RollbackFailed.userMessage -> lyricsRollbackFailure(language)
    EmbeddedLyricsWriteFailure.LyricsTooLarge.userMessage -> lyricsTooLargeError(language)
    EmbeddedLyricsWriteFailure.WriteAccessUnavailable.userMessage -> lyricsWriteAccessUnavailable(language)
    EmbeddedLyricsWriteFailure.SourceReadFailed.userMessage -> lyricsSourceReadFailed(language)
    EmbeddedLyricsWriteFailure.StoragePermissionMissing.userMessage -> lyricsFolderAccessLost(language)
    EmbeddedLyricsWriteFailure.UnsupportedFormat.userMessage -> lyricsUnsupportedFormat(language)
    EmbeddedLyricsWriteFailure.UnsupportedSyncedLyrics.userMessage -> lyricsUnsupportedSynced(language)
    EmbeddedLyricsWriteFailure.ProviderRejectedWriteMode.userMessage -> lyricsProviderRejectsReplacement(language)
    EmbeddedLyricsWriteFailure.OriginalOverwriteFailed.userMessage -> lyricsWriteFailed(language)
    EmbeddedLyricsWriteFailure.TempVerificationFailed.userMessage -> lyricsVerificationBeforeSave(language)
    EmbeddedLyricsWriteFailure.PersistedVerificationFailed.userMessage -> lyricsVerificationAfterSave(language)
    EmbeddedLyricsWriteFailure.TempWriteFailed.userMessage -> lyricsPreparationFailed(language)
    EmbeddedLyricsWriteFailure.TagCommitFailed.userMessage -> lyricsMetadataUpdateFailed(language)
    EmbeddedLyricsWriteFailure.InvalidMediaStoreItemUri.userMessage -> lyricsInvalidMediaAddress(language)
    else -> if (EmbeddedLyricsWriteFailure.entries.any { it.userMessage == message }) {
        message.removeSuffix(".")
    } else {
        message
    }
}

private fun lyricsInvalidMediaAddress(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.tagFeedback_lyricsInvalidMediaAddress_1
    AppLanguage.Bengali -> BengaliTranslations.tagFeedback_lyricsInvalidMediaAddress_1
    AppLanguage.Bosnian -> BosnianTranslations.tagFeedback_lyricsInvalidMediaAddress_1
    AppLanguage.Bulgarian -> BulgarianTranslations.tagFeedback_lyricsInvalidMediaAddress_1
    AppLanguage.Catalan -> CatalanTranslations.tagFeedback_lyricsInvalidMediaAddress_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.tagFeedback_lyricsInvalidMediaAddress_1
    AppLanguage.Croatian -> CroatianTranslations.tagFeedback_lyricsInvalidMediaAddress_1
    AppLanguage.Czech -> CzechTranslations.tagFeedback_lyricsInvalidMediaAddress_1
    AppLanguage.Danish -> DanishTranslations.tagFeedback_lyricsInvalidMediaAddress_1
    AppLanguage.Dutch -> DutchTranslations.tagFeedback_lyricsInvalidMediaAddress_1
    AppLanguage.English -> EnglishTranslations.tagFeedback_lyricsInvalidMediaAddress_1
    AppLanguage.Estonian -> EstonianTranslations.tagFeedback_lyricsInvalidMediaAddress_1
    AppLanguage.Finnish -> FinnishTranslations.tagFeedback_lyricsInvalidMediaAddress_1
    AppLanguage.French -> FrenchTranslations.tagFeedback_lyricsInvalidMediaAddress_1
    AppLanguage.German -> GermanTranslations.tagFeedback_lyricsInvalidMediaAddress_1
    AppLanguage.Greek -> GreekTranslations.tagFeedback_lyricsInvalidMediaAddress_1
    AppLanguage.Hindi -> HindiTranslations.tagFeedback_lyricsInvalidMediaAddress_1
    AppLanguage.Hungarian -> HungarianTranslations.tagFeedback_lyricsInvalidMediaAddress_1
    AppLanguage.Indonesian -> IndonesianTranslations.tagFeedback_lyricsInvalidMediaAddress_1
    AppLanguage.Italian -> ItalianTranslations.tagFeedback_lyricsInvalidMediaAddress_1
    AppLanguage.Japanese -> JapaneseTranslations.tagFeedback_lyricsInvalidMediaAddress_1
    AppLanguage.Korean -> KoreanTranslations.tagFeedback_lyricsInvalidMediaAddress_1
    AppLanguage.Latvian -> LatvianTranslations.tagFeedback_lyricsInvalidMediaAddress_1
    AppLanguage.Lithuanian -> LithuanianTranslations.tagFeedback_lyricsInvalidMediaAddress_1
    AppLanguage.Macedonian -> MacedonianTranslations.tagFeedback_lyricsInvalidMediaAddress_1
    AppLanguage.Malay -> MalayTranslations.tagFeedback_lyricsInvalidMediaAddress_1
    AppLanguage.Norwegian -> NorwegianTranslations.tagFeedback_lyricsInvalidMediaAddress_1
    AppLanguage.Polish -> PolishTranslations.tagFeedback_lyricsInvalidMediaAddress_1
    AppLanguage.Portuguese -> PortugueseTranslations.tagFeedback_lyricsInvalidMediaAddress_1
    AppLanguage.Punjabi -> PunjabiTranslations.tagFeedback_lyricsInvalidMediaAddress_1
    AppLanguage.Romanian -> RomanianTranslations.tagFeedback_lyricsInvalidMediaAddress_1
    AppLanguage.Russian -> RussianTranslations.tagFeedback_lyricsInvalidMediaAddress_1
    AppLanguage.Serbian -> SerbianTranslations.tagFeedback_lyricsInvalidMediaAddress_1
    AppLanguage.Slovak -> SlovakTranslations.tagFeedback_lyricsInvalidMediaAddress_1
    AppLanguage.Slovenian -> SlovenianTranslations.tagFeedback_lyricsInvalidMediaAddress_1
    AppLanguage.Spanish -> SpanishTranslations.tagFeedback_lyricsInvalidMediaAddress_1
    AppLanguage.Swedish -> SwedishTranslations.tagFeedback_lyricsInvalidMediaAddress_1
    AppLanguage.Thai -> ThaiTranslations.tagFeedback_lyricsInvalidMediaAddress_1
    AppLanguage.Ukrainian -> UkrainianTranslations.tagFeedback_lyricsInvalidMediaAddress_1
    AppLanguage.Urdu -> UrduTranslations.tagFeedback_lyricsInvalidMediaAddress_1
}

private fun lyricsMetadataUpdateFailed(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.tagFeedback_lyricsMetadataUpdateFailed_1
    AppLanguage.Bengali -> BengaliTranslations.tagFeedback_lyricsMetadataUpdateFailed_1
    AppLanguage.Bosnian -> BosnianTranslations.tagFeedback_lyricsMetadataUpdateFailed_1
    AppLanguage.Bulgarian -> BulgarianTranslations.tagFeedback_lyricsMetadataUpdateFailed_1
    AppLanguage.Catalan -> CatalanTranslations.tagFeedback_lyricsMetadataUpdateFailed_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.tagFeedback_lyricsMetadataUpdateFailed_1
    AppLanguage.Croatian -> CroatianTranslations.tagFeedback_lyricsMetadataUpdateFailed_1
    AppLanguage.Czech -> CzechTranslations.tagFeedback_lyricsMetadataUpdateFailed_1
    AppLanguage.Danish -> DanishTranslations.tagFeedback_lyricsMetadataUpdateFailed_1
    AppLanguage.Dutch -> DutchTranslations.tagFeedback_lyricsMetadataUpdateFailed_1
    AppLanguage.English -> EnglishTranslations.tagFeedback_lyricsMetadataUpdateFailed_1
    AppLanguage.Estonian -> EstonianTranslations.tagFeedback_lyricsMetadataUpdateFailed_1
    AppLanguage.Finnish -> FinnishTranslations.tagFeedback_lyricsMetadataUpdateFailed_1
    AppLanguage.French -> FrenchTranslations.tagFeedback_lyricsMetadataUpdateFailed_1
    AppLanguage.German -> GermanTranslations.tagFeedback_lyricsMetadataUpdateFailed_1
    AppLanguage.Greek -> GreekTranslations.tagFeedback_lyricsMetadataUpdateFailed_1
    AppLanguage.Hindi -> HindiTranslations.tagFeedback_lyricsMetadataUpdateFailed_1
    AppLanguage.Hungarian -> HungarianTranslations.tagFeedback_lyricsMetadataUpdateFailed_1
    AppLanguage.Indonesian -> IndonesianTranslations.tagFeedback_lyricsMetadataUpdateFailed_1
    AppLanguage.Italian -> ItalianTranslations.tagFeedback_lyricsMetadataUpdateFailed_1
    AppLanguage.Japanese -> JapaneseTranslations.tagFeedback_lyricsMetadataUpdateFailed_1
    AppLanguage.Korean -> KoreanTranslations.tagFeedback_lyricsMetadataUpdateFailed_1
    AppLanguage.Latvian -> LatvianTranslations.tagFeedback_lyricsMetadataUpdateFailed_1
    AppLanguage.Lithuanian -> LithuanianTranslations.tagFeedback_lyricsMetadataUpdateFailed_1
    AppLanguage.Macedonian -> MacedonianTranslations.tagFeedback_lyricsMetadataUpdateFailed_1
    AppLanguage.Malay -> MalayTranslations.tagFeedback_lyricsMetadataUpdateFailed_1
    AppLanguage.Norwegian -> NorwegianTranslations.tagFeedback_lyricsMetadataUpdateFailed_1
    AppLanguage.Polish -> PolishTranslations.tagFeedback_lyricsMetadataUpdateFailed_1
    AppLanguage.Portuguese -> PortugueseTranslations.tagFeedback_lyricsMetadataUpdateFailed_1
    AppLanguage.Punjabi -> PunjabiTranslations.tagFeedback_lyricsMetadataUpdateFailed_1
    AppLanguage.Romanian -> RomanianTranslations.tagFeedback_lyricsMetadataUpdateFailed_1
    AppLanguage.Russian -> RussianTranslations.tagFeedback_lyricsMetadataUpdateFailed_1
    AppLanguage.Serbian -> SerbianTranslations.tagFeedback_lyricsMetadataUpdateFailed_1
    AppLanguage.Slovak -> SlovakTranslations.tagFeedback_lyricsMetadataUpdateFailed_1
    AppLanguage.Slovenian -> SlovenianTranslations.tagFeedback_lyricsMetadataUpdateFailed_1
    AppLanguage.Spanish -> SpanishTranslations.tagFeedback_lyricsMetadataUpdateFailed_1
    AppLanguage.Swedish -> SwedishTranslations.tagFeedback_lyricsMetadataUpdateFailed_1
    AppLanguage.Thai -> ThaiTranslations.tagFeedback_lyricsMetadataUpdateFailed_1
    AppLanguage.Ukrainian -> UkrainianTranslations.tagFeedback_lyricsMetadataUpdateFailed_1
    AppLanguage.Urdu -> UrduTranslations.tagFeedback_lyricsMetadataUpdateFailed_1
}

private fun lyricsPreparationFailed(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.tagFeedback_lyricsPreparationFailed_1
    AppLanguage.Bengali -> BengaliTranslations.tagFeedback_lyricsPreparationFailed_1
    AppLanguage.Bosnian -> BosnianTranslations.tagFeedback_lyricsPreparationFailed_1
    AppLanguage.Bulgarian -> BulgarianTranslations.tagFeedback_lyricsPreparationFailed_1
    AppLanguage.Catalan -> CatalanTranslations.tagFeedback_lyricsPreparationFailed_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.tagFeedback_lyricsPreparationFailed_1
    AppLanguage.Croatian -> CroatianTranslations.tagFeedback_lyricsPreparationFailed_1
    AppLanguage.Czech -> CzechTranslations.tagFeedback_lyricsPreparationFailed_1
    AppLanguage.Danish -> DanishTranslations.tagFeedback_lyricsPreparationFailed_1
    AppLanguage.Dutch -> DutchTranslations.tagFeedback_lyricsPreparationFailed_1
    AppLanguage.English -> EnglishTranslations.tagFeedback_lyricsPreparationFailed_1
    AppLanguage.Estonian -> EstonianTranslations.tagFeedback_lyricsPreparationFailed_1
    AppLanguage.Finnish -> FinnishTranslations.tagFeedback_lyricsPreparationFailed_1
    AppLanguage.French -> FrenchTranslations.tagFeedback_lyricsPreparationFailed_1
    AppLanguage.German -> GermanTranslations.tagFeedback_lyricsPreparationFailed_1
    AppLanguage.Greek -> GreekTranslations.tagFeedback_lyricsPreparationFailed_1
    AppLanguage.Hindi -> HindiTranslations.tagFeedback_lyricsPreparationFailed_1
    AppLanguage.Hungarian -> HungarianTranslations.tagFeedback_lyricsPreparationFailed_1
    AppLanguage.Indonesian -> IndonesianTranslations.tagFeedback_lyricsPreparationFailed_1
    AppLanguage.Italian -> ItalianTranslations.tagFeedback_lyricsPreparationFailed_1
    AppLanguage.Japanese -> JapaneseTranslations.tagFeedback_lyricsPreparationFailed_1
    AppLanguage.Korean -> KoreanTranslations.tagFeedback_lyricsPreparationFailed_1
    AppLanguage.Latvian -> LatvianTranslations.tagFeedback_lyricsPreparationFailed_1
    AppLanguage.Lithuanian -> LithuanianTranslations.tagFeedback_lyricsPreparationFailed_1
    AppLanguage.Macedonian -> MacedonianTranslations.tagFeedback_lyricsPreparationFailed_1
    AppLanguage.Malay -> MalayTranslations.tagFeedback_lyricsPreparationFailed_1
    AppLanguage.Norwegian -> NorwegianTranslations.tagFeedback_lyricsPreparationFailed_1
    AppLanguage.Polish -> PolishTranslations.tagFeedback_lyricsPreparationFailed_1
    AppLanguage.Portuguese -> PortugueseTranslations.tagFeedback_lyricsPreparationFailed_1
    AppLanguage.Punjabi -> PunjabiTranslations.tagFeedback_lyricsPreparationFailed_1
    AppLanguage.Romanian -> RomanianTranslations.tagFeedback_lyricsPreparationFailed_1
    AppLanguage.Russian -> RussianTranslations.tagFeedback_lyricsPreparationFailed_1
    AppLanguage.Serbian -> SerbianTranslations.tagFeedback_lyricsPreparationFailed_1
    AppLanguage.Slovak -> SlovakTranslations.tagFeedback_lyricsPreparationFailed_1
    AppLanguage.Slovenian -> SlovenianTranslations.tagFeedback_lyricsPreparationFailed_1
    AppLanguage.Spanish -> SpanishTranslations.tagFeedback_lyricsPreparationFailed_1
    AppLanguage.Swedish -> SwedishTranslations.tagFeedback_lyricsPreparationFailed_1
    AppLanguage.Thai -> ThaiTranslations.tagFeedback_lyricsPreparationFailed_1
    AppLanguage.Ukrainian -> UkrainianTranslations.tagFeedback_lyricsPreparationFailed_1
    AppLanguage.Urdu -> UrduTranslations.tagFeedback_lyricsPreparationFailed_1
}

private fun lyricsVerificationAfterSave(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.tagFeedback_lyricsVerificationAfterSave_1
    AppLanguage.Bengali -> BengaliTranslations.tagFeedback_lyricsVerificationAfterSave_1
    AppLanguage.Bosnian -> BosnianTranslations.tagFeedback_lyricsVerificationAfterSave_1
    AppLanguage.Bulgarian -> BulgarianTranslations.tagFeedback_lyricsVerificationAfterSave_1
    AppLanguage.Catalan -> CatalanTranslations.tagFeedback_lyricsVerificationAfterSave_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.tagFeedback_lyricsVerificationAfterSave_1
    AppLanguage.Croatian -> CroatianTranslations.tagFeedback_lyricsVerificationAfterSave_1
    AppLanguage.Czech -> CzechTranslations.tagFeedback_lyricsVerificationAfterSave_1
    AppLanguage.Danish -> DanishTranslations.tagFeedback_lyricsVerificationAfterSave_1
    AppLanguage.Dutch -> DutchTranslations.tagFeedback_lyricsVerificationAfterSave_1
    AppLanguage.English -> EnglishTranslations.tagFeedback_lyricsVerificationAfterSave_1
    AppLanguage.Estonian -> EstonianTranslations.tagFeedback_lyricsVerificationAfterSave_1
    AppLanguage.Finnish -> FinnishTranslations.tagFeedback_lyricsVerificationAfterSave_1
    AppLanguage.French -> FrenchTranslations.tagFeedback_lyricsVerificationAfterSave_1
    AppLanguage.German -> GermanTranslations.tagFeedback_lyricsVerificationAfterSave_1
    AppLanguage.Greek -> GreekTranslations.tagFeedback_lyricsVerificationAfterSave_1
    AppLanguage.Hindi -> HindiTranslations.tagFeedback_lyricsVerificationAfterSave_1
    AppLanguage.Hungarian -> HungarianTranslations.tagFeedback_lyricsVerificationAfterSave_1
    AppLanguage.Indonesian -> IndonesianTranslations.tagFeedback_lyricsVerificationAfterSave_1
    AppLanguage.Italian -> ItalianTranslations.tagFeedback_lyricsVerificationAfterSave_1
    AppLanguage.Japanese -> JapaneseTranslations.tagFeedback_lyricsVerificationAfterSave_1
    AppLanguage.Korean -> KoreanTranslations.tagFeedback_lyricsVerificationAfterSave_1
    AppLanguage.Latvian -> LatvianTranslations.tagFeedback_lyricsVerificationAfterSave_1
    AppLanguage.Lithuanian -> LithuanianTranslations.tagFeedback_lyricsVerificationAfterSave_1
    AppLanguage.Macedonian -> MacedonianTranslations.tagFeedback_lyricsVerificationAfterSave_1
    AppLanguage.Malay -> MalayTranslations.tagFeedback_lyricsVerificationAfterSave_1
    AppLanguage.Norwegian -> NorwegianTranslations.tagFeedback_lyricsVerificationAfterSave_1
    AppLanguage.Polish -> PolishTranslations.tagFeedback_lyricsVerificationAfterSave_1
    AppLanguage.Portuguese -> PortugueseTranslations.tagFeedback_lyricsVerificationAfterSave_1
    AppLanguage.Punjabi -> PunjabiTranslations.tagFeedback_lyricsVerificationAfterSave_1
    AppLanguage.Romanian -> RomanianTranslations.tagFeedback_lyricsVerificationAfterSave_1
    AppLanguage.Russian -> RussianTranslations.tagFeedback_lyricsVerificationAfterSave_1
    AppLanguage.Serbian -> SerbianTranslations.tagFeedback_lyricsVerificationAfterSave_1
    AppLanguage.Slovak -> SlovakTranslations.tagFeedback_lyricsVerificationAfterSave_1
    AppLanguage.Slovenian -> SlovenianTranslations.tagFeedback_lyricsVerificationAfterSave_1
    AppLanguage.Spanish -> SpanishTranslations.tagFeedback_lyricsVerificationAfterSave_1
    AppLanguage.Swedish -> SwedishTranslations.tagFeedback_lyricsVerificationAfterSave_1
    AppLanguage.Thai -> ThaiTranslations.tagFeedback_lyricsVerificationAfterSave_1
    AppLanguage.Ukrainian -> UkrainianTranslations.tagFeedback_lyricsVerificationAfterSave_1
    AppLanguage.Urdu -> UrduTranslations.tagFeedback_lyricsVerificationAfterSave_1
}

private fun lyricsVerificationBeforeSave(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.tagFeedback_lyricsVerificationBeforeSave_1
    AppLanguage.Bengali -> BengaliTranslations.tagFeedback_lyricsVerificationBeforeSave_1
    AppLanguage.Bosnian -> BosnianTranslations.tagFeedback_lyricsVerificationBeforeSave_1
    AppLanguage.Bulgarian -> BulgarianTranslations.tagFeedback_lyricsVerificationBeforeSave_1
    AppLanguage.Catalan -> CatalanTranslations.tagFeedback_lyricsVerificationBeforeSave_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.tagFeedback_lyricsVerificationBeforeSave_1
    AppLanguage.Croatian -> CroatianTranslations.tagFeedback_lyricsVerificationBeforeSave_1
    AppLanguage.Czech -> CzechTranslations.tagFeedback_lyricsVerificationBeforeSave_1
    AppLanguage.Danish -> DanishTranslations.tagFeedback_lyricsVerificationBeforeSave_1
    AppLanguage.Dutch -> DutchTranslations.tagFeedback_lyricsVerificationBeforeSave_1
    AppLanguage.English -> EnglishTranslations.tagFeedback_lyricsVerificationBeforeSave_1
    AppLanguage.Estonian -> EstonianTranslations.tagFeedback_lyricsVerificationBeforeSave_1
    AppLanguage.Finnish -> FinnishTranslations.tagFeedback_lyricsVerificationBeforeSave_1
    AppLanguage.French -> FrenchTranslations.tagFeedback_lyricsVerificationBeforeSave_1
    AppLanguage.German -> GermanTranslations.tagFeedback_lyricsVerificationBeforeSave_1
    AppLanguage.Greek -> GreekTranslations.tagFeedback_lyricsVerificationBeforeSave_1
    AppLanguage.Hindi -> HindiTranslations.tagFeedback_lyricsVerificationBeforeSave_1
    AppLanguage.Hungarian -> HungarianTranslations.tagFeedback_lyricsVerificationBeforeSave_1
    AppLanguage.Indonesian -> IndonesianTranslations.tagFeedback_lyricsVerificationBeforeSave_1
    AppLanguage.Italian -> ItalianTranslations.tagFeedback_lyricsVerificationBeforeSave_1
    AppLanguage.Japanese -> JapaneseTranslations.tagFeedback_lyricsVerificationBeforeSave_1
    AppLanguage.Korean -> KoreanTranslations.tagFeedback_lyricsVerificationBeforeSave_1
    AppLanguage.Latvian -> LatvianTranslations.tagFeedback_lyricsVerificationBeforeSave_1
    AppLanguage.Lithuanian -> LithuanianTranslations.tagFeedback_lyricsVerificationBeforeSave_1
    AppLanguage.Macedonian -> MacedonianTranslations.tagFeedback_lyricsVerificationBeforeSave_1
    AppLanguage.Malay -> MalayTranslations.tagFeedback_lyricsVerificationBeforeSave_1
    AppLanguage.Norwegian -> NorwegianTranslations.tagFeedback_lyricsVerificationBeforeSave_1
    AppLanguage.Polish -> PolishTranslations.tagFeedback_lyricsVerificationBeforeSave_1
    AppLanguage.Portuguese -> PortugueseTranslations.tagFeedback_lyricsVerificationBeforeSave_1
    AppLanguage.Punjabi -> PunjabiTranslations.tagFeedback_lyricsVerificationBeforeSave_1
    AppLanguage.Romanian -> RomanianTranslations.tagFeedback_lyricsVerificationBeforeSave_1
    AppLanguage.Russian -> RussianTranslations.tagFeedback_lyricsVerificationBeforeSave_1
    AppLanguage.Serbian -> SerbianTranslations.tagFeedback_lyricsVerificationBeforeSave_1
    AppLanguage.Slovak -> SlovakTranslations.tagFeedback_lyricsVerificationBeforeSave_1
    AppLanguage.Slovenian -> SlovenianTranslations.tagFeedback_lyricsVerificationBeforeSave_1
    AppLanguage.Spanish -> SpanishTranslations.tagFeedback_lyricsVerificationBeforeSave_1
    AppLanguage.Swedish -> SwedishTranslations.tagFeedback_lyricsVerificationBeforeSave_1
    AppLanguage.Thai -> ThaiTranslations.tagFeedback_lyricsVerificationBeforeSave_1
    AppLanguage.Ukrainian -> UkrainianTranslations.tagFeedback_lyricsVerificationBeforeSave_1
    AppLanguage.Urdu -> UrduTranslations.tagFeedback_lyricsVerificationBeforeSave_1
}

private fun lyricsWriteFailed(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.tagFeedback_lyricsWriteFailed_1
    AppLanguage.Bengali -> BengaliTranslations.tagFeedback_lyricsWriteFailed_1
    AppLanguage.Bosnian -> BosnianTranslations.tagFeedback_lyricsWriteFailed_1
    AppLanguage.Bulgarian -> BulgarianTranslations.tagFeedback_lyricsWriteFailed_1
    AppLanguage.Catalan -> CatalanTranslations.tagFeedback_lyricsWriteFailed_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.tagFeedback_lyricsWriteFailed_1
    AppLanguage.Croatian -> CroatianTranslations.tagFeedback_lyricsWriteFailed_1
    AppLanguage.Czech -> CzechTranslations.tagFeedback_lyricsWriteFailed_1
    AppLanguage.Danish -> DanishTranslations.tagFeedback_lyricsWriteFailed_1
    AppLanguage.Dutch -> DutchTranslations.tagFeedback_lyricsWriteFailed_1
    AppLanguage.English -> EnglishTranslations.tagFeedback_lyricsWriteFailed_1
    AppLanguage.Estonian -> EstonianTranslations.tagFeedback_lyricsWriteFailed_1
    AppLanguage.Finnish -> FinnishTranslations.tagFeedback_lyricsWriteFailed_1
    AppLanguage.French -> FrenchTranslations.tagFeedback_lyricsWriteFailed_1
    AppLanguage.German -> GermanTranslations.tagFeedback_lyricsWriteFailed_1
    AppLanguage.Greek -> GreekTranslations.tagFeedback_lyricsWriteFailed_1
    AppLanguage.Hindi -> HindiTranslations.tagFeedback_lyricsWriteFailed_1
    AppLanguage.Hungarian -> HungarianTranslations.tagFeedback_lyricsWriteFailed_1
    AppLanguage.Indonesian -> IndonesianTranslations.tagFeedback_lyricsWriteFailed_1
    AppLanguage.Italian -> ItalianTranslations.tagFeedback_lyricsWriteFailed_1
    AppLanguage.Japanese -> JapaneseTranslations.tagFeedback_lyricsWriteFailed_1
    AppLanguage.Korean -> KoreanTranslations.tagFeedback_lyricsWriteFailed_1
    AppLanguage.Latvian -> LatvianTranslations.tagFeedback_lyricsWriteFailed_1
    AppLanguage.Lithuanian -> LithuanianTranslations.tagFeedback_lyricsWriteFailed_1
    AppLanguage.Macedonian -> MacedonianTranslations.tagFeedback_lyricsWriteFailed_1
    AppLanguage.Malay -> MalayTranslations.tagFeedback_lyricsWriteFailed_1
    AppLanguage.Norwegian -> NorwegianTranslations.tagFeedback_lyricsWriteFailed_1
    AppLanguage.Polish -> PolishTranslations.tagFeedback_lyricsWriteFailed_1
    AppLanguage.Portuguese -> PortugueseTranslations.tagFeedback_lyricsWriteFailed_1
    AppLanguage.Punjabi -> PunjabiTranslations.tagFeedback_lyricsWriteFailed_1
    AppLanguage.Romanian -> RomanianTranslations.tagFeedback_lyricsWriteFailed_1
    AppLanguage.Russian -> RussianTranslations.tagFeedback_lyricsWriteFailed_1
    AppLanguage.Serbian -> SerbianTranslations.tagFeedback_lyricsWriteFailed_1
    AppLanguage.Slovak -> SlovakTranslations.tagFeedback_lyricsWriteFailed_1
    AppLanguage.Slovenian -> SlovenianTranslations.tagFeedback_lyricsWriteFailed_1
    AppLanguage.Spanish -> SpanishTranslations.tagFeedback_lyricsWriteFailed_1
    AppLanguage.Swedish -> SwedishTranslations.tagFeedback_lyricsWriteFailed_1
    AppLanguage.Thai -> ThaiTranslations.tagFeedback_lyricsWriteFailed_1
    AppLanguage.Ukrainian -> UkrainianTranslations.tagFeedback_lyricsWriteFailed_1
    AppLanguage.Urdu -> UrduTranslations.tagFeedback_lyricsWriteFailed_1
}

private fun lyricsProviderRejectsReplacement(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.tagFeedback_lyricsProviderRejectsReplacement_1
    AppLanguage.Bengali -> BengaliTranslations.tagFeedback_lyricsProviderRejectsReplacement_1
    AppLanguage.Bosnian -> BosnianTranslations.tagFeedback_lyricsProviderRejectsReplacement_1
    AppLanguage.Bulgarian -> BulgarianTranslations.tagFeedback_lyricsProviderRejectsReplacement_1
    AppLanguage.Catalan -> CatalanTranslations.tagFeedback_lyricsProviderRejectsReplacement_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.tagFeedback_lyricsProviderRejectsReplacement_1
    AppLanguage.Croatian -> CroatianTranslations.tagFeedback_lyricsProviderRejectsReplacement_1
    AppLanguage.Czech -> CzechTranslations.tagFeedback_lyricsProviderRejectsReplacement_1
    AppLanguage.Danish -> DanishTranslations.tagFeedback_lyricsProviderRejectsReplacement_1
    AppLanguage.Dutch -> DutchTranslations.tagFeedback_lyricsProviderRejectsReplacement_1
    AppLanguage.English -> EnglishTranslations.tagFeedback_lyricsProviderRejectsReplacement_1
    AppLanguage.Estonian -> EstonianTranslations.tagFeedback_lyricsProviderRejectsReplacement_1
    AppLanguage.Finnish -> FinnishTranslations.tagFeedback_lyricsProviderRejectsReplacement_1
    AppLanguage.French -> FrenchTranslations.tagFeedback_lyricsProviderRejectsReplacement_1
    AppLanguage.German -> GermanTranslations.tagFeedback_lyricsProviderRejectsReplacement_1
    AppLanguage.Greek -> GreekTranslations.tagFeedback_lyricsProviderRejectsReplacement_1
    AppLanguage.Hindi -> HindiTranslations.tagFeedback_lyricsProviderRejectsReplacement_1
    AppLanguage.Hungarian -> HungarianTranslations.tagFeedback_lyricsProviderRejectsReplacement_1
    AppLanguage.Indonesian -> IndonesianTranslations.tagFeedback_lyricsProviderRejectsReplacement_1
    AppLanguage.Italian -> ItalianTranslations.tagFeedback_lyricsProviderRejectsReplacement_1
    AppLanguage.Japanese -> JapaneseTranslations.tagFeedback_lyricsProviderRejectsReplacement_1
    AppLanguage.Korean -> KoreanTranslations.tagFeedback_lyricsProviderRejectsReplacement_1
    AppLanguage.Latvian -> LatvianTranslations.tagFeedback_lyricsProviderRejectsReplacement_1
    AppLanguage.Lithuanian -> LithuanianTranslations.tagFeedback_lyricsProviderRejectsReplacement_1
    AppLanguage.Macedonian -> MacedonianTranslations.tagFeedback_lyricsProviderRejectsReplacement_1
    AppLanguage.Malay -> MalayTranslations.tagFeedback_lyricsProviderRejectsReplacement_1
    AppLanguage.Norwegian -> NorwegianTranslations.tagFeedback_lyricsProviderRejectsReplacement_1
    AppLanguage.Polish -> PolishTranslations.tagFeedback_lyricsProviderRejectsReplacement_1
    AppLanguage.Portuguese -> PortugueseTranslations.tagFeedback_lyricsProviderRejectsReplacement_1
    AppLanguage.Punjabi -> PunjabiTranslations.tagFeedback_lyricsProviderRejectsReplacement_1
    AppLanguage.Romanian -> RomanianTranslations.tagFeedback_lyricsProviderRejectsReplacement_1
    AppLanguage.Russian -> RussianTranslations.tagFeedback_lyricsProviderRejectsReplacement_1
    AppLanguage.Serbian -> SerbianTranslations.tagFeedback_lyricsProviderRejectsReplacement_1
    AppLanguage.Slovak -> SlovakTranslations.tagFeedback_lyricsProviderRejectsReplacement_1
    AppLanguage.Slovenian -> SlovenianTranslations.tagFeedback_lyricsProviderRejectsReplacement_1
    AppLanguage.Spanish -> SpanishTranslations.tagFeedback_lyricsProviderRejectsReplacement_1
    AppLanguage.Swedish -> SwedishTranslations.tagFeedback_lyricsProviderRejectsReplacement_1
    AppLanguage.Thai -> ThaiTranslations.tagFeedback_lyricsProviderRejectsReplacement_1
    AppLanguage.Ukrainian -> UkrainianTranslations.tagFeedback_lyricsProviderRejectsReplacement_1
    AppLanguage.Urdu -> UrduTranslations.tagFeedback_lyricsProviderRejectsReplacement_1
}

private fun lyricsUnsupportedSynced(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.tagFeedback_lyricsUnsupportedSynced_1
    AppLanguage.Bengali -> BengaliTranslations.tagFeedback_lyricsUnsupportedSynced_1
    AppLanguage.Bosnian -> BosnianTranslations.tagFeedback_lyricsUnsupportedSynced_1
    AppLanguage.Bulgarian -> BulgarianTranslations.tagFeedback_lyricsUnsupportedSynced_1
    AppLanguage.Catalan -> CatalanTranslations.tagFeedback_lyricsUnsupportedSynced_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.tagFeedback_lyricsUnsupportedSynced_1
    AppLanguage.Croatian -> CroatianTranslations.tagFeedback_lyricsUnsupportedSynced_1
    AppLanguage.Czech -> CzechTranslations.tagFeedback_lyricsUnsupportedSynced_1
    AppLanguage.Danish -> DanishTranslations.tagFeedback_lyricsUnsupportedSynced_1
    AppLanguage.Dutch -> DutchTranslations.tagFeedback_lyricsUnsupportedSynced_1
    AppLanguage.English -> EnglishTranslations.tagFeedback_lyricsUnsupportedSynced_1
    AppLanguage.Estonian -> EstonianTranslations.tagFeedback_lyricsUnsupportedSynced_1
    AppLanguage.Finnish -> FinnishTranslations.tagFeedback_lyricsUnsupportedSynced_1
    AppLanguage.French -> FrenchTranslations.tagFeedback_lyricsUnsupportedSynced_1
    AppLanguage.German -> GermanTranslations.tagFeedback_lyricsUnsupportedSynced_1
    AppLanguage.Greek -> GreekTranslations.tagFeedback_lyricsUnsupportedSynced_1
    AppLanguage.Hindi -> HindiTranslations.tagFeedback_lyricsUnsupportedSynced_1
    AppLanguage.Hungarian -> HungarianTranslations.tagFeedback_lyricsUnsupportedSynced_1
    AppLanguage.Indonesian -> IndonesianTranslations.tagFeedback_lyricsUnsupportedSynced_1
    AppLanguage.Italian -> ItalianTranslations.tagFeedback_lyricsUnsupportedSynced_1
    AppLanguage.Japanese -> JapaneseTranslations.tagFeedback_lyricsUnsupportedSynced_1
    AppLanguage.Korean -> KoreanTranslations.tagFeedback_lyricsUnsupportedSynced_1
    AppLanguage.Latvian -> LatvianTranslations.tagFeedback_lyricsUnsupportedSynced_1
    AppLanguage.Lithuanian -> LithuanianTranslations.tagFeedback_lyricsUnsupportedSynced_1
    AppLanguage.Macedonian -> MacedonianTranslations.tagFeedback_lyricsUnsupportedSynced_1
    AppLanguage.Malay -> MalayTranslations.tagFeedback_lyricsUnsupportedSynced_1
    AppLanguage.Norwegian -> NorwegianTranslations.tagFeedback_lyricsUnsupportedSynced_1
    AppLanguage.Polish -> PolishTranslations.tagFeedback_lyricsUnsupportedSynced_1
    AppLanguage.Portuguese -> PortugueseTranslations.tagFeedback_lyricsUnsupportedSynced_1
    AppLanguage.Punjabi -> PunjabiTranslations.tagFeedback_lyricsUnsupportedSynced_1
    AppLanguage.Romanian -> RomanianTranslations.tagFeedback_lyricsUnsupportedSynced_1
    AppLanguage.Russian -> RussianTranslations.tagFeedback_lyricsUnsupportedSynced_1
    AppLanguage.Serbian -> SerbianTranslations.tagFeedback_lyricsUnsupportedSynced_1
    AppLanguage.Slovak -> SlovakTranslations.tagFeedback_lyricsUnsupportedSynced_1
    AppLanguage.Slovenian -> SlovenianTranslations.tagFeedback_lyricsUnsupportedSynced_1
    AppLanguage.Spanish -> SpanishTranslations.tagFeedback_lyricsUnsupportedSynced_1
    AppLanguage.Swedish -> SwedishTranslations.tagFeedback_lyricsUnsupportedSynced_1
    AppLanguage.Thai -> ThaiTranslations.tagFeedback_lyricsUnsupportedSynced_1
    AppLanguage.Ukrainian -> UkrainianTranslations.tagFeedback_lyricsUnsupportedSynced_1
    AppLanguage.Urdu -> UrduTranslations.tagFeedback_lyricsUnsupportedSynced_1
}

private fun lyricsUnsupportedFormat(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.tagFeedback_lyricsUnsupportedFormat_1
    AppLanguage.Bengali -> BengaliTranslations.tagFeedback_lyricsUnsupportedFormat_1
    AppLanguage.Bosnian -> BosnianTranslations.tagFeedback_lyricsUnsupportedFormat_1
    AppLanguage.Bulgarian -> BulgarianTranslations.tagFeedback_lyricsUnsupportedFormat_1
    AppLanguage.Catalan -> CatalanTranslations.tagFeedback_lyricsUnsupportedFormat_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.tagFeedback_lyricsUnsupportedFormat_1
    AppLanguage.Croatian -> CroatianTranslations.tagFeedback_lyricsUnsupportedFormat_1
    AppLanguage.Czech -> CzechTranslations.tagFeedback_lyricsUnsupportedFormat_1
    AppLanguage.Danish -> DanishTranslations.tagFeedback_lyricsUnsupportedFormat_1
    AppLanguage.Dutch -> DutchTranslations.tagFeedback_lyricsUnsupportedFormat_1
    AppLanguage.English -> EnglishTranslations.tagFeedback_lyricsUnsupportedFormat_1
    AppLanguage.Estonian -> EstonianTranslations.tagFeedback_lyricsUnsupportedFormat_1
    AppLanguage.Finnish -> FinnishTranslations.tagFeedback_lyricsUnsupportedFormat_1
    AppLanguage.French -> FrenchTranslations.tagFeedback_lyricsUnsupportedFormat_1
    AppLanguage.German -> GermanTranslations.tagFeedback_lyricsUnsupportedFormat_1
    AppLanguage.Greek -> GreekTranslations.tagFeedback_lyricsUnsupportedFormat_1
    AppLanguage.Hindi -> HindiTranslations.tagFeedback_lyricsUnsupportedFormat_1
    AppLanguage.Hungarian -> HungarianTranslations.tagFeedback_lyricsUnsupportedFormat_1
    AppLanguage.Indonesian -> IndonesianTranslations.tagFeedback_lyricsUnsupportedFormat_1
    AppLanguage.Italian -> ItalianTranslations.tagFeedback_lyricsUnsupportedFormat_1
    AppLanguage.Japanese -> JapaneseTranslations.tagFeedback_lyricsUnsupportedFormat_1
    AppLanguage.Korean -> KoreanTranslations.tagFeedback_lyricsUnsupportedFormat_1
    AppLanguage.Latvian -> LatvianTranslations.tagFeedback_lyricsUnsupportedFormat_1
    AppLanguage.Lithuanian -> LithuanianTranslations.tagFeedback_lyricsUnsupportedFormat_1
    AppLanguage.Macedonian -> MacedonianTranslations.tagFeedback_lyricsUnsupportedFormat_1
    AppLanguage.Malay -> MalayTranslations.tagFeedback_lyricsUnsupportedFormat_1
    AppLanguage.Norwegian -> NorwegianTranslations.tagFeedback_lyricsUnsupportedFormat_1
    AppLanguage.Polish -> PolishTranslations.tagFeedback_lyricsUnsupportedFormat_1
    AppLanguage.Portuguese -> PortugueseTranslations.tagFeedback_lyricsUnsupportedFormat_1
    AppLanguage.Punjabi -> PunjabiTranslations.tagFeedback_lyricsUnsupportedFormat_1
    AppLanguage.Romanian -> RomanianTranslations.tagFeedback_lyricsUnsupportedFormat_1
    AppLanguage.Russian -> RussianTranslations.tagFeedback_lyricsUnsupportedFormat_1
    AppLanguage.Serbian -> SerbianTranslations.tagFeedback_lyricsUnsupportedFormat_1
    AppLanguage.Slovak -> SlovakTranslations.tagFeedback_lyricsUnsupportedFormat_1
    AppLanguage.Slovenian -> SlovenianTranslations.tagFeedback_lyricsUnsupportedFormat_1
    AppLanguage.Spanish -> SpanishTranslations.tagFeedback_lyricsUnsupportedFormat_1
    AppLanguage.Swedish -> SwedishTranslations.tagFeedback_lyricsUnsupportedFormat_1
    AppLanguage.Thai -> ThaiTranslations.tagFeedback_lyricsUnsupportedFormat_1
    AppLanguage.Ukrainian -> UkrainianTranslations.tagFeedback_lyricsUnsupportedFormat_1
    AppLanguage.Urdu -> UrduTranslations.tagFeedback_lyricsUnsupportedFormat_1
}

private fun lyricsFolderAccessLost(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.tagFeedback_lyricsFolderAccessLost_1
    AppLanguage.Bengali -> BengaliTranslations.tagFeedback_lyricsFolderAccessLost_1
    AppLanguage.Bosnian -> BosnianTranslations.tagFeedback_lyricsFolderAccessLost_1
    AppLanguage.Bulgarian -> BulgarianTranslations.tagFeedback_lyricsFolderAccessLost_1
    AppLanguage.Catalan -> CatalanTranslations.tagFeedback_lyricsFolderAccessLost_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.tagFeedback_lyricsFolderAccessLost_1
    AppLanguage.Croatian -> CroatianTranslations.tagFeedback_lyricsFolderAccessLost_1
    AppLanguage.Czech -> CzechTranslations.tagFeedback_lyricsFolderAccessLost_1
    AppLanguage.Danish -> DanishTranslations.tagFeedback_lyricsFolderAccessLost_1
    AppLanguage.Dutch -> DutchTranslations.tagFeedback_lyricsFolderAccessLost_1
    AppLanguage.English -> EnglishTranslations.tagFeedback_lyricsFolderAccessLost_1
    AppLanguage.Estonian -> EstonianTranslations.tagFeedback_lyricsFolderAccessLost_1
    AppLanguage.Finnish -> FinnishTranslations.tagFeedback_lyricsFolderAccessLost_1
    AppLanguage.French -> FrenchTranslations.tagFeedback_lyricsFolderAccessLost_1
    AppLanguage.German -> GermanTranslations.tagFeedback_lyricsFolderAccessLost_1
    AppLanguage.Greek -> GreekTranslations.tagFeedback_lyricsFolderAccessLost_1
    AppLanguage.Hindi -> HindiTranslations.tagFeedback_lyricsFolderAccessLost_1
    AppLanguage.Hungarian -> HungarianTranslations.tagFeedback_lyricsFolderAccessLost_1
    AppLanguage.Indonesian -> IndonesianTranslations.tagFeedback_lyricsFolderAccessLost_1
    AppLanguage.Italian -> ItalianTranslations.tagFeedback_lyricsFolderAccessLost_1
    AppLanguage.Japanese -> JapaneseTranslations.tagFeedback_lyricsFolderAccessLost_1
    AppLanguage.Korean -> KoreanTranslations.tagFeedback_lyricsFolderAccessLost_1
    AppLanguage.Latvian -> LatvianTranslations.tagFeedback_lyricsFolderAccessLost_1
    AppLanguage.Lithuanian -> LithuanianTranslations.tagFeedback_lyricsFolderAccessLost_1
    AppLanguage.Macedonian -> MacedonianTranslations.tagFeedback_lyricsFolderAccessLost_1
    AppLanguage.Malay -> MalayTranslations.tagFeedback_lyricsFolderAccessLost_1
    AppLanguage.Norwegian -> NorwegianTranslations.tagFeedback_lyricsFolderAccessLost_1
    AppLanguage.Polish -> PolishTranslations.tagFeedback_lyricsFolderAccessLost_1
    AppLanguage.Portuguese -> PortugueseTranslations.tagFeedback_lyricsFolderAccessLost_1
    AppLanguage.Punjabi -> PunjabiTranslations.tagFeedback_lyricsFolderAccessLost_1
    AppLanguage.Romanian -> RomanianTranslations.tagFeedback_lyricsFolderAccessLost_1
    AppLanguage.Russian -> RussianTranslations.tagFeedback_lyricsFolderAccessLost_1
    AppLanguage.Serbian -> SerbianTranslations.tagFeedback_lyricsFolderAccessLost_1
    AppLanguage.Slovak -> SlovakTranslations.tagFeedback_lyricsFolderAccessLost_1
    AppLanguage.Slovenian -> SlovenianTranslations.tagFeedback_lyricsFolderAccessLost_1
    AppLanguage.Spanish -> SpanishTranslations.tagFeedback_lyricsFolderAccessLost_1
    AppLanguage.Swedish -> SwedishTranslations.tagFeedback_lyricsFolderAccessLost_1
    AppLanguage.Thai -> ThaiTranslations.tagFeedback_lyricsFolderAccessLost_1
    AppLanguage.Ukrainian -> UkrainianTranslations.tagFeedback_lyricsFolderAccessLost_1
    AppLanguage.Urdu -> UrduTranslations.tagFeedback_lyricsFolderAccessLost_1
}

private fun lyricsSourceReadFailed(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.tagFeedback_lyricsSourceReadFailed_1
    AppLanguage.Bengali -> BengaliTranslations.tagFeedback_lyricsSourceReadFailed_1
    AppLanguage.Bosnian -> BosnianTranslations.tagFeedback_lyricsSourceReadFailed_1
    AppLanguage.Bulgarian -> BulgarianTranslations.tagFeedback_lyricsSourceReadFailed_1
    AppLanguage.Catalan -> CatalanTranslations.tagFeedback_lyricsSourceReadFailed_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.tagFeedback_lyricsSourceReadFailed_1
    AppLanguage.Croatian -> CroatianTranslations.tagFeedback_lyricsSourceReadFailed_1
    AppLanguage.Czech -> CzechTranslations.tagFeedback_lyricsSourceReadFailed_1
    AppLanguage.Danish -> DanishTranslations.tagFeedback_lyricsSourceReadFailed_1
    AppLanguage.Dutch -> DutchTranslations.tagFeedback_lyricsSourceReadFailed_1
    AppLanguage.English -> EnglishTranslations.tagFeedback_lyricsSourceReadFailed_1
    AppLanguage.Estonian -> EstonianTranslations.tagFeedback_lyricsSourceReadFailed_1
    AppLanguage.Finnish -> FinnishTranslations.tagFeedback_lyricsSourceReadFailed_1
    AppLanguage.French -> FrenchTranslations.tagFeedback_lyricsSourceReadFailed_1
    AppLanguage.German -> GermanTranslations.tagFeedback_lyricsSourceReadFailed_1
    AppLanguage.Greek -> GreekTranslations.tagFeedback_lyricsSourceReadFailed_1
    AppLanguage.Hindi -> HindiTranslations.tagFeedback_lyricsSourceReadFailed_1
    AppLanguage.Hungarian -> HungarianTranslations.tagFeedback_lyricsSourceReadFailed_1
    AppLanguage.Indonesian -> IndonesianTranslations.tagFeedback_lyricsSourceReadFailed_1
    AppLanguage.Italian -> ItalianTranslations.tagFeedback_lyricsSourceReadFailed_1
    AppLanguage.Japanese -> JapaneseTranslations.tagFeedback_lyricsSourceReadFailed_1
    AppLanguage.Korean -> KoreanTranslations.tagFeedback_lyricsSourceReadFailed_1
    AppLanguage.Latvian -> LatvianTranslations.tagFeedback_lyricsSourceReadFailed_1
    AppLanguage.Lithuanian -> LithuanianTranslations.tagFeedback_lyricsSourceReadFailed_1
    AppLanguage.Macedonian -> MacedonianTranslations.tagFeedback_lyricsSourceReadFailed_1
    AppLanguage.Malay -> MalayTranslations.tagFeedback_lyricsSourceReadFailed_1
    AppLanguage.Norwegian -> NorwegianTranslations.tagFeedback_lyricsSourceReadFailed_1
    AppLanguage.Polish -> PolishTranslations.tagFeedback_lyricsSourceReadFailed_1
    AppLanguage.Portuguese -> PortugueseTranslations.tagFeedback_lyricsSourceReadFailed_1
    AppLanguage.Punjabi -> PunjabiTranslations.tagFeedback_lyricsSourceReadFailed_1
    AppLanguage.Romanian -> RomanianTranslations.tagFeedback_lyricsSourceReadFailed_1
    AppLanguage.Russian -> RussianTranslations.tagFeedback_lyricsSourceReadFailed_1
    AppLanguage.Serbian -> SerbianTranslations.tagFeedback_lyricsSourceReadFailed_1
    AppLanguage.Slovak -> SlovakTranslations.tagFeedback_lyricsSourceReadFailed_1
    AppLanguage.Slovenian -> SlovenianTranslations.tagFeedback_lyricsSourceReadFailed_1
    AppLanguage.Spanish -> SpanishTranslations.tagFeedback_lyricsSourceReadFailed_1
    AppLanguage.Swedish -> SwedishTranslations.tagFeedback_lyricsSourceReadFailed_1
    AppLanguage.Thai -> ThaiTranslations.tagFeedback_lyricsSourceReadFailed_1
    AppLanguage.Ukrainian -> UkrainianTranslations.tagFeedback_lyricsSourceReadFailed_1
    AppLanguage.Urdu -> UrduTranslations.tagFeedback_lyricsSourceReadFailed_1
}

private fun lyricsWriteAccessUnavailable(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.tagFeedback_lyricsWriteAccessUnavailable_1
    AppLanguage.Bengali -> BengaliTranslations.tagFeedback_lyricsWriteAccessUnavailable_1
    AppLanguage.Bosnian -> BosnianTranslations.tagFeedback_lyricsWriteAccessUnavailable_1
    AppLanguage.Bulgarian -> BulgarianTranslations.tagFeedback_lyricsWriteAccessUnavailable_1
    AppLanguage.Catalan -> CatalanTranslations.tagFeedback_lyricsWriteAccessUnavailable_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.tagFeedback_lyricsWriteAccessUnavailable_1
    AppLanguage.Croatian -> CroatianTranslations.tagFeedback_lyricsWriteAccessUnavailable_1
    AppLanguage.Czech -> CzechTranslations.tagFeedback_lyricsWriteAccessUnavailable_1
    AppLanguage.Danish -> DanishTranslations.tagFeedback_lyricsWriteAccessUnavailable_1
    AppLanguage.Dutch -> DutchTranslations.tagFeedback_lyricsWriteAccessUnavailable_1
    AppLanguage.English -> EnglishTranslations.tagFeedback_lyricsWriteAccessUnavailable_1
    AppLanguage.Estonian -> EstonianTranslations.tagFeedback_lyricsWriteAccessUnavailable_1
    AppLanguage.Finnish -> FinnishTranslations.tagFeedback_lyricsWriteAccessUnavailable_1
    AppLanguage.French -> FrenchTranslations.tagFeedback_lyricsWriteAccessUnavailable_1
    AppLanguage.German -> GermanTranslations.tagFeedback_lyricsWriteAccessUnavailable_1
    AppLanguage.Greek -> GreekTranslations.tagFeedback_lyricsWriteAccessUnavailable_1
    AppLanguage.Hindi -> HindiTranslations.tagFeedback_lyricsWriteAccessUnavailable_1
    AppLanguage.Hungarian -> HungarianTranslations.tagFeedback_lyricsWriteAccessUnavailable_1
    AppLanguage.Indonesian -> IndonesianTranslations.tagFeedback_lyricsWriteAccessUnavailable_1
    AppLanguage.Italian -> ItalianTranslations.tagFeedback_lyricsWriteAccessUnavailable_1
    AppLanguage.Japanese -> JapaneseTranslations.tagFeedback_lyricsWriteAccessUnavailable_1
    AppLanguage.Korean -> KoreanTranslations.tagFeedback_lyricsWriteAccessUnavailable_1
    AppLanguage.Latvian -> LatvianTranslations.tagFeedback_lyricsWriteAccessUnavailable_1
    AppLanguage.Lithuanian -> LithuanianTranslations.tagFeedback_lyricsWriteAccessUnavailable_1
    AppLanguage.Macedonian -> MacedonianTranslations.tagFeedback_lyricsWriteAccessUnavailable_1
    AppLanguage.Malay -> MalayTranslations.tagFeedback_lyricsWriteAccessUnavailable_1
    AppLanguage.Norwegian -> NorwegianTranslations.tagFeedback_lyricsWriteAccessUnavailable_1
    AppLanguage.Polish -> PolishTranslations.tagFeedback_lyricsWriteAccessUnavailable_1
    AppLanguage.Portuguese -> PortugueseTranslations.tagFeedback_lyricsWriteAccessUnavailable_1
    AppLanguage.Punjabi -> PunjabiTranslations.tagFeedback_lyricsWriteAccessUnavailable_1
    AppLanguage.Romanian -> RomanianTranslations.tagFeedback_lyricsWriteAccessUnavailable_1
    AppLanguage.Russian -> RussianTranslations.tagFeedback_lyricsWriteAccessUnavailable_1
    AppLanguage.Serbian -> SerbianTranslations.tagFeedback_lyricsWriteAccessUnavailable_1
    AppLanguage.Slovak -> SlovakTranslations.tagFeedback_lyricsWriteAccessUnavailable_1
    AppLanguage.Slovenian -> SlovenianTranslations.tagFeedback_lyricsWriteAccessUnavailable_1
    AppLanguage.Spanish -> SpanishTranslations.tagFeedback_lyricsWriteAccessUnavailable_1
    AppLanguage.Swedish -> SwedishTranslations.tagFeedback_lyricsWriteAccessUnavailable_1
    AppLanguage.Thai -> ThaiTranslations.tagFeedback_lyricsWriteAccessUnavailable_1
    AppLanguage.Ukrainian -> UkrainianTranslations.tagFeedback_lyricsWriteAccessUnavailable_1
    AppLanguage.Urdu -> UrduTranslations.tagFeedback_lyricsWriteAccessUnavailable_1
}

private fun lyricsTooLargeError(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.tagFeedback_lyricsTooLargeError_1
    AppLanguage.Bengali -> BengaliTranslations.tagFeedback_lyricsTooLargeError_1
    AppLanguage.Bosnian -> BosnianTranslations.tagFeedback_lyricsTooLargeError_1
    AppLanguage.Bulgarian -> BulgarianTranslations.tagFeedback_lyricsTooLargeError_1
    AppLanguage.Catalan -> CatalanTranslations.tagFeedback_lyricsTooLargeError_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.tagFeedback_lyricsTooLargeError_1
    AppLanguage.Croatian -> CroatianTranslations.tagFeedback_lyricsTooLargeError_1
    AppLanguage.Czech -> CzechTranslations.tagFeedback_lyricsTooLargeError_1
    AppLanguage.Danish -> DanishTranslations.tagFeedback_lyricsTooLargeError_1
    AppLanguage.Dutch -> DutchTranslations.tagFeedback_lyricsTooLargeError_1
    AppLanguage.English -> EnglishTranslations.tagFeedback_lyricsTooLargeError_1
    AppLanguage.Estonian -> EstonianTranslations.tagFeedback_lyricsTooLargeError_1
    AppLanguage.Finnish -> FinnishTranslations.tagFeedback_lyricsTooLargeError_1
    AppLanguage.French -> FrenchTranslations.tagFeedback_lyricsTooLargeError_1
    AppLanguage.German -> GermanTranslations.tagFeedback_lyricsTooLargeError_1
    AppLanguage.Greek -> GreekTranslations.tagFeedback_lyricsTooLargeError_1
    AppLanguage.Hindi -> HindiTranslations.tagFeedback_lyricsTooLargeError_1
    AppLanguage.Hungarian -> HungarianTranslations.tagFeedback_lyricsTooLargeError_1
    AppLanguage.Indonesian -> IndonesianTranslations.tagFeedback_lyricsTooLargeError_1
    AppLanguage.Italian -> ItalianTranslations.tagFeedback_lyricsTooLargeError_1
    AppLanguage.Japanese -> JapaneseTranslations.tagFeedback_lyricsTooLargeError_1
    AppLanguage.Korean -> KoreanTranslations.tagFeedback_lyricsTooLargeError_1
    AppLanguage.Latvian -> LatvianTranslations.tagFeedback_lyricsTooLargeError_1
    AppLanguage.Lithuanian -> LithuanianTranslations.tagFeedback_lyricsTooLargeError_1
    AppLanguage.Macedonian -> MacedonianTranslations.tagFeedback_lyricsTooLargeError_1
    AppLanguage.Malay -> MalayTranslations.tagFeedback_lyricsTooLargeError_1
    AppLanguage.Norwegian -> NorwegianTranslations.tagFeedback_lyricsTooLargeError_1
    AppLanguage.Polish -> PolishTranslations.tagFeedback_lyricsTooLargeError_1
    AppLanguage.Portuguese -> PortugueseTranslations.tagFeedback_lyricsTooLargeError_1
    AppLanguage.Punjabi -> PunjabiTranslations.tagFeedback_lyricsTooLargeError_1
    AppLanguage.Romanian -> RomanianTranslations.tagFeedback_lyricsTooLargeError_1
    AppLanguage.Russian -> RussianTranslations.tagFeedback_lyricsTooLargeError_1
    AppLanguage.Serbian -> SerbianTranslations.tagFeedback_lyricsTooLargeError_1
    AppLanguage.Slovak -> SlovakTranslations.tagFeedback_lyricsTooLargeError_1
    AppLanguage.Slovenian -> SlovenianTranslations.tagFeedback_lyricsTooLargeError_1
    AppLanguage.Spanish -> SpanishTranslations.tagFeedback_lyricsTooLargeError_1
    AppLanguage.Swedish -> SwedishTranslations.tagFeedback_lyricsTooLargeError_1
    AppLanguage.Thai -> ThaiTranslations.tagFeedback_lyricsTooLargeError_1
    AppLanguage.Ukrainian -> UkrainianTranslations.tagFeedback_lyricsTooLargeError_1
    AppLanguage.Urdu -> UrduTranslations.tagFeedback_lyricsTooLargeError_1
}

private fun lyricsRollbackFailure(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.tagFeedback_lyricsRollbackFailure_1
    AppLanguage.Bengali -> BengaliTranslations.tagFeedback_lyricsRollbackFailure_1
    AppLanguage.Bosnian -> BosnianTranslations.tagFeedback_lyricsRollbackFailure_1
    AppLanguage.Bulgarian -> BulgarianTranslations.tagFeedback_lyricsRollbackFailure_1
    AppLanguage.Catalan -> CatalanTranslations.tagFeedback_lyricsRollbackFailure_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.tagFeedback_lyricsRollbackFailure_1
    AppLanguage.Croatian -> CroatianTranslations.tagFeedback_lyricsRollbackFailure_1
    AppLanguage.Czech -> CzechTranslations.tagFeedback_lyricsRollbackFailure_1
    AppLanguage.Danish -> DanishTranslations.tagFeedback_lyricsRollbackFailure_1
    AppLanguage.Dutch -> DutchTranslations.tagFeedback_lyricsRollbackFailure_1
    AppLanguage.English -> EnglishTranslations.tagFeedback_lyricsRollbackFailure_1
    AppLanguage.Estonian -> EstonianTranslations.tagFeedback_lyricsRollbackFailure_1
    AppLanguage.Finnish -> FinnishTranslations.tagFeedback_lyricsRollbackFailure_1
    AppLanguage.French -> FrenchTranslations.tagFeedback_lyricsRollbackFailure_1
    AppLanguage.German -> GermanTranslations.tagFeedback_lyricsRollbackFailure_1
    AppLanguage.Greek -> GreekTranslations.tagFeedback_lyricsRollbackFailure_1
    AppLanguage.Hindi -> HindiTranslations.tagFeedback_lyricsRollbackFailure_1
    AppLanguage.Hungarian -> HungarianTranslations.tagFeedback_lyricsRollbackFailure_1
    AppLanguage.Indonesian -> IndonesianTranslations.tagFeedback_lyricsRollbackFailure_1
    AppLanguage.Italian -> ItalianTranslations.tagFeedback_lyricsRollbackFailure_1
    AppLanguage.Japanese -> JapaneseTranslations.tagFeedback_lyricsRollbackFailure_1
    AppLanguage.Korean -> KoreanTranslations.tagFeedback_lyricsRollbackFailure_1
    AppLanguage.Latvian -> LatvianTranslations.tagFeedback_lyricsRollbackFailure_1
    AppLanguage.Lithuanian -> LithuanianTranslations.tagFeedback_lyricsRollbackFailure_1
    AppLanguage.Macedonian -> MacedonianTranslations.tagFeedback_lyricsRollbackFailure_1
    AppLanguage.Malay -> MalayTranslations.tagFeedback_lyricsRollbackFailure_1
    AppLanguage.Norwegian -> NorwegianTranslations.tagFeedback_lyricsRollbackFailure_1
    AppLanguage.Polish -> PolishTranslations.tagFeedback_lyricsRollbackFailure_1
    AppLanguage.Portuguese -> PortugueseTranslations.tagFeedback_lyricsRollbackFailure_1
    AppLanguage.Punjabi -> PunjabiTranslations.tagFeedback_lyricsRollbackFailure_1
    AppLanguage.Romanian -> RomanianTranslations.tagFeedback_lyricsRollbackFailure_1
    AppLanguage.Russian -> RussianTranslations.tagFeedback_lyricsRollbackFailure_1
    AppLanguage.Serbian -> SerbianTranslations.tagFeedback_lyricsRollbackFailure_1
    AppLanguage.Slovak -> SlovakTranslations.tagFeedback_lyricsRollbackFailure_1
    AppLanguage.Slovenian -> SlovenianTranslations.tagFeedback_lyricsRollbackFailure_1
    AppLanguage.Spanish -> SpanishTranslations.tagFeedback_lyricsRollbackFailure_1
    AppLanguage.Swedish -> SwedishTranslations.tagFeedback_lyricsRollbackFailure_1
    AppLanguage.Thai -> ThaiTranslations.tagFeedback_lyricsRollbackFailure_1
    AppLanguage.Ukrainian -> UkrainianTranslations.tagFeedback_lyricsRollbackFailure_1
    AppLanguage.Urdu -> UrduTranslations.tagFeedback_lyricsRollbackFailure_1
}

internal fun localizedTagSaveStatus(status: String, failureCount: Int, language: AppLanguage): String {
    if (status == "The song file is empty.") return emptyTagSource(language)
    if (status == "The song file is not writable.") return tagSourceNotWritable(language)
    if (status == "The song is already being edited.") return tagSourceAlreadyBeingEdited(language)
    if (status == "The file path is unavailable." || status == "The song directory is unavailable.") {
        return tagSourceLocationUnavailable(language)
    }
    if (status == "There is not enough temporary storage to edit this song safely." ||
        status == "There is not enough storage to replace this song safely."
    ) return insufficientTagStorage(language)
    if (status == "Unsupported URI.") return unsupportedTagUriError(language)
    if (status == "Unable to open the source file.") return sourceFileOpenError(language)
    if (status == "Unable to open the file for writing.") return fileWriteOpenError(language)
    if (status == "Unable to query document write capability.") return tagDocumentWriteCapabilityError(language)
    if (status == "The selected document provider does not support writing this file.") return tagProviderDoesNotSupportWriting(language)
    if (status == "The selected document has no persisted write permission.") return tagDocumentWriteAccessMissing(language)
    if (status == "The selected document provider does not allow tag editing") return tagProviderDoesNotAllowEditing(language)
    if (status == "The selected folder did not grant persistent write access") return tagFolderWriteAccessMissing(language)
    if (status == "Unable to save audiobook tags") return unableToSaveAudiobookTags(language)
    if (status == "Unable to save tags") return unableToSaveTags(language)
    if (status == "No tags were saved") return noTagsSaved(language)
    if (status == "A write-access request is already pending") return tagWriteRequestPending(language)
    if (status == "Android could not open the write-access request") return tagWriteRequestUnavailable(language)
    if (status == "Write access was not granted") return tagWriteAccessNotGranted(language)
    if (status == "Additional write access is needed to finish saving") return tagSaveAwaitingAccess(language)
    if (status != "Saved with $failureCount issue(s)") return status
    if (language == AppLanguage.English) return "Saved with $failureCount ${if (failureCount == 1) "issue" else "issues"}"
    val label = when (language) {
        AppLanguage.Albanian -> AlbanianTranslations.tagFeedback_localizedTagSaveStatus_1
        AppLanguage.Bengali -> BengaliTranslations.tagFeedback_localizedTagSaveStatus_1
        AppLanguage.Bosnian -> BosnianTranslations.tagFeedback_localizedTagSaveStatus_1
        AppLanguage.Bulgarian -> BulgarianTranslations.tagFeedback_localizedTagSaveStatus_1
        AppLanguage.Catalan -> CatalanTranslations.tagFeedback_localizedTagSaveStatus_1
        AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.tagFeedback_localizedTagSaveStatus_1
        AppLanguage.Croatian -> CroatianTranslations.tagFeedback_localizedTagSaveStatus_1
        AppLanguage.Czech -> CzechTranslations.tagFeedback_localizedTagSaveStatus_1
        AppLanguage.Danish -> DanishTranslations.tagFeedback_localizedTagSaveStatus_1
        AppLanguage.Dutch -> DutchTranslations.tagFeedback_localizedTagSaveStatus_1
        AppLanguage.English -> error("English is handled above")
        AppLanguage.Estonian -> EstonianTranslations.tagFeedback_localizedTagSaveStatus_1
        AppLanguage.Finnish -> FinnishTranslations.tagFeedback_localizedTagSaveStatus_1
        AppLanguage.French -> FrenchTranslations.tagFeedback_localizedTagSaveStatus_1
        AppLanguage.German -> GermanTranslations.tagFeedback_localizedTagSaveStatus_1
        AppLanguage.Greek -> GreekTranslations.tagFeedback_localizedTagSaveStatus_1
        AppLanguage.Hindi -> HindiTranslations.tagFeedback_localizedTagSaveStatus_1
        AppLanguage.Hungarian -> HungarianTranslations.tagFeedback_localizedTagSaveStatus_1
        AppLanguage.Indonesian -> IndonesianTranslations.tagFeedback_localizedTagSaveStatus_1
        AppLanguage.Italian -> ItalianTranslations.tagFeedback_localizedTagSaveStatus_1
        AppLanguage.Japanese -> JapaneseTranslations.tagFeedback_localizedTagSaveStatus_1
        AppLanguage.Korean -> KoreanTranslations.tagFeedback_localizedTagSaveStatus_1
        AppLanguage.Latvian -> LatvianTranslations.tagFeedback_localizedTagSaveStatus_1
        AppLanguage.Lithuanian -> LithuanianTranslations.tagFeedback_localizedTagSaveStatus_1
        AppLanguage.Macedonian -> MacedonianTranslations.tagFeedback_localizedTagSaveStatus_1
        AppLanguage.Malay -> MalayTranslations.tagFeedback_localizedTagSaveStatus_1
        AppLanguage.Norwegian -> NorwegianTranslations.tagFeedback_localizedTagSaveStatus_1
        AppLanguage.Polish -> PolishTranslations.tagFeedback_localizedTagSaveStatus_1
        AppLanguage.Portuguese -> PortugueseTranslations.tagFeedback_localizedTagSaveStatus_1
        AppLanguage.Punjabi -> PunjabiTranslations.tagFeedback_localizedTagSaveStatus_1
        AppLanguage.Romanian -> RomanianTranslations.tagFeedback_localizedTagSaveStatus_1
        AppLanguage.Russian -> RussianTranslations.tagFeedback_localizedTagSaveStatus_1
        AppLanguage.Serbian -> SerbianTranslations.tagFeedback_localizedTagSaveStatus_1
        AppLanguage.Slovak -> SlovakTranslations.tagFeedback_localizedTagSaveStatus_1
        AppLanguage.Slovenian -> SlovenianTranslations.tagFeedback_localizedTagSaveStatus_1
        AppLanguage.Spanish -> SpanishTranslations.tagFeedback_localizedTagSaveStatus_1
        AppLanguage.Swedish -> SwedishTranslations.tagFeedback_localizedTagSaveStatus_1
        AppLanguage.Thai -> ThaiTranslations.tagFeedback_localizedTagSaveStatus_1
        AppLanguage.Ukrainian -> UkrainianTranslations.tagFeedback_localizedTagSaveStatus_1
        AppLanguage.Urdu -> UrduTranslations.tagFeedback_localizedTagSaveStatus_1
    }
    return "$label: $failureCount"
}

private fun tagSourceAlreadyBeingEdited(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.tagFeedback_tagSourceAlreadyBeingEdited_1
    AppLanguage.Bengali -> BengaliTranslations.tagFeedback_tagSourceAlreadyBeingEdited_1
    AppLanguage.Bosnian -> BosnianTranslations.tagFeedback_tagSourceAlreadyBeingEdited_1
    AppLanguage.Bulgarian -> BulgarianTranslations.tagFeedback_tagSourceAlreadyBeingEdited_1
    AppLanguage.Catalan -> CatalanTranslations.tagFeedback_tagSourceAlreadyBeingEdited_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.tagFeedback_tagSourceAlreadyBeingEdited_1
    AppLanguage.Croatian -> CroatianTranslations.tagFeedback_tagSourceAlreadyBeingEdited_1
    AppLanguage.Czech -> CzechTranslations.tagFeedback_tagSourceAlreadyBeingEdited_1
    AppLanguage.Danish -> DanishTranslations.tagFeedback_tagSourceAlreadyBeingEdited_1
    AppLanguage.Dutch -> DutchTranslations.tagFeedback_tagSourceAlreadyBeingEdited_1
    AppLanguage.English -> EnglishTranslations.tagFeedback_tagSourceAlreadyBeingEdited_1
    AppLanguage.Estonian -> EstonianTranslations.tagFeedback_tagSourceAlreadyBeingEdited_1
    AppLanguage.Finnish -> FinnishTranslations.tagFeedback_tagSourceAlreadyBeingEdited_1
    AppLanguage.French -> FrenchTranslations.tagFeedback_tagSourceAlreadyBeingEdited_1
    AppLanguage.German -> GermanTranslations.tagFeedback_tagSourceAlreadyBeingEdited_1
    AppLanguage.Greek -> GreekTranslations.tagFeedback_tagSourceAlreadyBeingEdited_1
    AppLanguage.Hindi -> HindiTranslations.tagFeedback_tagSourceAlreadyBeingEdited_1
    AppLanguage.Hungarian -> HungarianTranslations.tagFeedback_tagSourceAlreadyBeingEdited_1
    AppLanguage.Indonesian -> IndonesianTranslations.tagFeedback_tagSourceAlreadyBeingEdited_1
    AppLanguage.Italian -> ItalianTranslations.tagFeedback_tagSourceAlreadyBeingEdited_1
    AppLanguage.Japanese -> JapaneseTranslations.tagFeedback_tagSourceAlreadyBeingEdited_1
    AppLanguage.Korean -> KoreanTranslations.tagFeedback_tagSourceAlreadyBeingEdited_1
    AppLanguage.Latvian -> LatvianTranslations.tagFeedback_tagSourceAlreadyBeingEdited_1
    AppLanguage.Lithuanian -> LithuanianTranslations.tagFeedback_tagSourceAlreadyBeingEdited_1
    AppLanguage.Macedonian -> MacedonianTranslations.tagFeedback_tagSourceAlreadyBeingEdited_1
    AppLanguage.Malay -> MalayTranslations.tagFeedback_tagSourceAlreadyBeingEdited_1
    AppLanguage.Norwegian -> NorwegianTranslations.tagFeedback_tagSourceAlreadyBeingEdited_1
    AppLanguage.Polish -> PolishTranslations.tagFeedback_tagSourceAlreadyBeingEdited_1
    AppLanguage.Portuguese -> PortugueseTranslations.tagFeedback_tagSourceAlreadyBeingEdited_1
    AppLanguage.Punjabi -> PunjabiTranslations.tagFeedback_tagSourceAlreadyBeingEdited_1
    AppLanguage.Romanian -> RomanianTranslations.tagFeedback_tagSourceAlreadyBeingEdited_1
    AppLanguage.Russian -> RussianTranslations.tagFeedback_tagSourceAlreadyBeingEdited_1
    AppLanguage.Serbian -> SerbianTranslations.tagFeedback_tagSourceAlreadyBeingEdited_1
    AppLanguage.Slovak -> SlovakTranslations.tagFeedback_tagSourceAlreadyBeingEdited_1
    AppLanguage.Slovenian -> SlovenianTranslations.tagFeedback_tagSourceAlreadyBeingEdited_1
    AppLanguage.Spanish -> SpanishTranslations.tagFeedback_tagSourceAlreadyBeingEdited_1
    AppLanguage.Swedish -> SwedishTranslations.tagFeedback_tagSourceAlreadyBeingEdited_1
    AppLanguage.Thai -> ThaiTranslations.tagFeedback_tagSourceAlreadyBeingEdited_1
    AppLanguage.Ukrainian -> UkrainianTranslations.tagFeedback_tagSourceAlreadyBeingEdited_1
    AppLanguage.Urdu -> UrduTranslations.tagFeedback_tagSourceAlreadyBeingEdited_1
}

private fun tagSourceLocationUnavailable(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.tagFeedback_tagSourceLocationUnavailable_1
    AppLanguage.Bengali -> BengaliTranslations.tagFeedback_tagSourceLocationUnavailable_1
    AppLanguage.Bosnian -> BosnianTranslations.tagFeedback_tagSourceLocationUnavailable_1
    AppLanguage.Bulgarian -> BulgarianTranslations.tagFeedback_tagSourceLocationUnavailable_1
    AppLanguage.Catalan -> CatalanTranslations.tagFeedback_tagSourceLocationUnavailable_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.tagFeedback_tagSourceLocationUnavailable_1
    AppLanguage.Croatian -> CroatianTranslations.tagFeedback_tagSourceLocationUnavailable_1
    AppLanguage.Czech -> CzechTranslations.tagFeedback_tagSourceLocationUnavailable_1
    AppLanguage.Danish -> DanishTranslations.tagFeedback_tagSourceLocationUnavailable_1
    AppLanguage.Dutch -> DutchTranslations.tagFeedback_tagSourceLocationUnavailable_1
    AppLanguage.English -> EnglishTranslations.tagFeedback_tagSourceLocationUnavailable_1
    AppLanguage.Estonian -> EstonianTranslations.tagFeedback_tagSourceLocationUnavailable_1
    AppLanguage.Finnish -> FinnishTranslations.tagFeedback_tagSourceLocationUnavailable_1
    AppLanguage.French -> FrenchTranslations.tagFeedback_tagSourceLocationUnavailable_1
    AppLanguage.German -> GermanTranslations.tagFeedback_tagSourceLocationUnavailable_1
    AppLanguage.Greek -> GreekTranslations.tagFeedback_tagSourceLocationUnavailable_1
    AppLanguage.Hindi -> HindiTranslations.tagFeedback_tagSourceLocationUnavailable_1
    AppLanguage.Hungarian -> HungarianTranslations.tagFeedback_tagSourceLocationUnavailable_1
    AppLanguage.Indonesian -> IndonesianTranslations.tagFeedback_tagSourceLocationUnavailable_1
    AppLanguage.Italian -> ItalianTranslations.tagFeedback_tagSourceLocationUnavailable_1
    AppLanguage.Japanese -> JapaneseTranslations.tagFeedback_tagSourceLocationUnavailable_1
    AppLanguage.Korean -> KoreanTranslations.tagFeedback_tagSourceLocationUnavailable_1
    AppLanguage.Latvian -> LatvianTranslations.tagFeedback_tagSourceLocationUnavailable_1
    AppLanguage.Lithuanian -> LithuanianTranslations.tagFeedback_tagSourceLocationUnavailable_1
    AppLanguage.Macedonian -> MacedonianTranslations.tagFeedback_tagSourceLocationUnavailable_1
    AppLanguage.Malay -> MalayTranslations.tagFeedback_tagSourceLocationUnavailable_1
    AppLanguage.Norwegian -> NorwegianTranslations.tagFeedback_tagSourceLocationUnavailable_1
    AppLanguage.Polish -> PolishTranslations.tagFeedback_tagSourceLocationUnavailable_1
    AppLanguage.Portuguese -> PortugueseTranslations.tagFeedback_tagSourceLocationUnavailable_1
    AppLanguage.Punjabi -> PunjabiTranslations.tagFeedback_tagSourceLocationUnavailable_1
    AppLanguage.Romanian -> RomanianTranslations.tagFeedback_tagSourceLocationUnavailable_1
    AppLanguage.Russian -> RussianTranslations.tagFeedback_tagSourceLocationUnavailable_1
    AppLanguage.Serbian -> SerbianTranslations.tagFeedback_tagSourceLocationUnavailable_1
    AppLanguage.Slovak -> SlovakTranslations.tagFeedback_tagSourceLocationUnavailable_1
    AppLanguage.Slovenian -> SlovenianTranslations.tagFeedback_tagSourceLocationUnavailable_1
    AppLanguage.Spanish -> SpanishTranslations.tagFeedback_tagSourceLocationUnavailable_1
    AppLanguage.Swedish -> SwedishTranslations.tagFeedback_tagSourceLocationUnavailable_1
    AppLanguage.Thai -> ThaiTranslations.tagFeedback_tagSourceLocationUnavailable_1
    AppLanguage.Ukrainian -> UkrainianTranslations.tagFeedback_tagSourceLocationUnavailable_1
    AppLanguage.Urdu -> UrduTranslations.tagFeedback_tagSourceLocationUnavailable_1
}

private fun tagSourceNotWritable(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.tagFeedback_tagSourceNotWritable_1
    AppLanguage.Bengali -> BengaliTranslations.tagFeedback_tagSourceNotWritable_1
    AppLanguage.Bosnian -> BosnianTranslations.tagFeedback_tagSourceNotWritable_1
    AppLanguage.Bulgarian -> BulgarianTranslations.tagFeedback_tagSourceNotWritable_1
    AppLanguage.Catalan -> CatalanTranslations.tagFeedback_tagSourceNotWritable_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.tagFeedback_tagSourceNotWritable_1
    AppLanguage.Croatian -> CroatianTranslations.tagFeedback_tagSourceNotWritable_1
    AppLanguage.Czech -> CzechTranslations.tagFeedback_tagSourceNotWritable_1
    AppLanguage.Danish -> DanishTranslations.tagFeedback_tagSourceNotWritable_1
    AppLanguage.Dutch -> DutchTranslations.tagFeedback_tagSourceNotWritable_1
    AppLanguage.English -> EnglishTranslations.tagFeedback_tagSourceNotWritable_1
    AppLanguage.Estonian -> EstonianTranslations.tagFeedback_tagSourceNotWritable_1
    AppLanguage.Finnish -> FinnishTranslations.tagFeedback_tagSourceNotWritable_1
    AppLanguage.French -> FrenchTranslations.tagFeedback_tagSourceNotWritable_1
    AppLanguage.German -> GermanTranslations.tagFeedback_tagSourceNotWritable_1
    AppLanguage.Greek -> GreekTranslations.tagFeedback_tagSourceNotWritable_1
    AppLanguage.Hindi -> HindiTranslations.tagFeedback_tagSourceNotWritable_1
    AppLanguage.Hungarian -> HungarianTranslations.tagFeedback_tagSourceNotWritable_1
    AppLanguage.Indonesian -> IndonesianTranslations.tagFeedback_tagSourceNotWritable_1
    AppLanguage.Italian -> ItalianTranslations.tagFeedback_tagSourceNotWritable_1
    AppLanguage.Japanese -> JapaneseTranslations.tagFeedback_tagSourceNotWritable_1
    AppLanguage.Korean -> KoreanTranslations.tagFeedback_tagSourceNotWritable_1
    AppLanguage.Latvian -> LatvianTranslations.tagFeedback_tagSourceNotWritable_1
    AppLanguage.Lithuanian -> LithuanianTranslations.tagFeedback_tagSourceNotWritable_1
    AppLanguage.Macedonian -> MacedonianTranslations.tagFeedback_tagSourceNotWritable_1
    AppLanguage.Malay -> MalayTranslations.tagFeedback_tagSourceNotWritable_1
    AppLanguage.Norwegian -> NorwegianTranslations.tagFeedback_tagSourceNotWritable_1
    AppLanguage.Polish -> PolishTranslations.tagFeedback_tagSourceNotWritable_1
    AppLanguage.Portuguese -> PortugueseTranslations.tagFeedback_tagSourceNotWritable_1
    AppLanguage.Punjabi -> PunjabiTranslations.tagFeedback_tagSourceNotWritable_1
    AppLanguage.Romanian -> RomanianTranslations.tagFeedback_tagSourceNotWritable_1
    AppLanguage.Russian -> RussianTranslations.tagFeedback_tagSourceNotWritable_1
    AppLanguage.Serbian -> SerbianTranslations.tagFeedback_tagSourceNotWritable_1
    AppLanguage.Slovak -> SlovakTranslations.tagFeedback_tagSourceNotWritable_1
    AppLanguage.Slovenian -> SlovenianTranslations.tagFeedback_tagSourceNotWritable_1
    AppLanguage.Spanish -> SpanishTranslations.tagFeedback_tagSourceNotWritable_1
    AppLanguage.Swedish -> SwedishTranslations.tagFeedback_tagSourceNotWritable_1
    AppLanguage.Thai -> ThaiTranslations.tagFeedback_tagSourceNotWritable_1
    AppLanguage.Ukrainian -> UkrainianTranslations.tagFeedback_tagSourceNotWritable_1
    AppLanguage.Urdu -> UrduTranslations.tagFeedback_tagSourceNotWritable_1
}

private fun emptyTagSource(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.tagFeedback_emptyTagSource_1
    AppLanguage.Bengali -> BengaliTranslations.tagFeedback_emptyTagSource_1
    AppLanguage.Bosnian -> BosnianTranslations.tagFeedback_emptyTagSource_1
    AppLanguage.Bulgarian -> BulgarianTranslations.tagFeedback_emptyTagSource_1
    AppLanguage.Catalan -> CatalanTranslations.tagFeedback_emptyTagSource_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.tagFeedback_emptyTagSource_1
    AppLanguage.Croatian -> CroatianTranslations.tagFeedback_emptyTagSource_1
    AppLanguage.Czech -> CzechTranslations.tagFeedback_emptyTagSource_1
    AppLanguage.Danish -> DanishTranslations.tagFeedback_emptyTagSource_1
    AppLanguage.Dutch -> DutchTranslations.tagFeedback_emptyTagSource_1
    AppLanguage.English -> EnglishTranslations.tagFeedback_emptyTagSource_1
    AppLanguage.Estonian -> EstonianTranslations.tagFeedback_emptyTagSource_1
    AppLanguage.Finnish -> FinnishTranslations.tagFeedback_emptyTagSource_1
    AppLanguage.French -> FrenchTranslations.tagFeedback_emptyTagSource_1
    AppLanguage.German -> GermanTranslations.tagFeedback_emptyTagSource_1
    AppLanguage.Greek -> GreekTranslations.tagFeedback_emptyTagSource_1
    AppLanguage.Hindi -> HindiTranslations.tagFeedback_emptyTagSource_1
    AppLanguage.Hungarian -> HungarianTranslations.tagFeedback_emptyTagSource_1
    AppLanguage.Indonesian -> IndonesianTranslations.tagFeedback_emptyTagSource_1
    AppLanguage.Italian -> ItalianTranslations.tagFeedback_emptyTagSource_1
    AppLanguage.Japanese -> JapaneseTranslations.tagFeedback_emptyTagSource_1
    AppLanguage.Korean -> KoreanTranslations.tagFeedback_emptyTagSource_1
    AppLanguage.Latvian -> LatvianTranslations.tagFeedback_emptyTagSource_1
    AppLanguage.Lithuanian -> LithuanianTranslations.tagFeedback_emptyTagSource_1
    AppLanguage.Macedonian -> MacedonianTranslations.tagFeedback_emptyTagSource_1
    AppLanguage.Malay -> MalayTranslations.tagFeedback_emptyTagSource_1
    AppLanguage.Norwegian -> NorwegianTranslations.tagFeedback_emptyTagSource_1
    AppLanguage.Polish -> PolishTranslations.tagFeedback_emptyTagSource_1
    AppLanguage.Portuguese -> PortugueseTranslations.tagFeedback_emptyTagSource_1
    AppLanguage.Punjabi -> PunjabiTranslations.tagFeedback_emptyTagSource_1
    AppLanguage.Romanian -> RomanianTranslations.tagFeedback_emptyTagSource_1
    AppLanguage.Russian -> RussianTranslations.tagFeedback_emptyTagSource_1
    AppLanguage.Serbian -> SerbianTranslations.tagFeedback_emptyTagSource_1
    AppLanguage.Slovak -> SlovakTranslations.tagFeedback_emptyTagSource_1
    AppLanguage.Slovenian -> SlovenianTranslations.tagFeedback_emptyTagSource_1
    AppLanguage.Spanish -> SpanishTranslations.tagFeedback_emptyTagSource_1
    AppLanguage.Swedish -> SwedishTranslations.tagFeedback_emptyTagSource_1
    AppLanguage.Thai -> ThaiTranslations.tagFeedback_emptyTagSource_1
    AppLanguage.Ukrainian -> UkrainianTranslations.tagFeedback_emptyTagSource_1
    AppLanguage.Urdu -> UrduTranslations.tagFeedback_emptyTagSource_1
}

private fun insufficientTagStorage(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.tagFeedback_insufficientTagStorage_1
    AppLanguage.Bengali -> BengaliTranslations.tagFeedback_insufficientTagStorage_1
    AppLanguage.Bosnian -> BosnianTranslations.tagFeedback_insufficientTagStorage_1
    AppLanguage.Bulgarian -> BulgarianTranslations.tagFeedback_insufficientTagStorage_1
    AppLanguage.Catalan -> CatalanTranslations.tagFeedback_insufficientTagStorage_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.tagFeedback_insufficientTagStorage_1
    AppLanguage.Croatian -> CroatianTranslations.tagFeedback_insufficientTagStorage_1
    AppLanguage.Czech -> CzechTranslations.tagFeedback_insufficientTagStorage_1
    AppLanguage.Danish -> DanishTranslations.tagFeedback_insufficientTagStorage_1
    AppLanguage.Dutch -> DutchTranslations.tagFeedback_insufficientTagStorage_1
    AppLanguage.English -> EnglishTranslations.tagFeedback_insufficientTagStorage_1
    AppLanguage.Estonian -> EstonianTranslations.tagFeedback_insufficientTagStorage_1
    AppLanguage.Finnish -> FinnishTranslations.tagFeedback_insufficientTagStorage_1
    AppLanguage.French -> FrenchTranslations.tagFeedback_insufficientTagStorage_1
    AppLanguage.German -> GermanTranslations.tagFeedback_insufficientTagStorage_1
    AppLanguage.Greek -> GreekTranslations.tagFeedback_insufficientTagStorage_1
    AppLanguage.Hindi -> HindiTranslations.tagFeedback_insufficientTagStorage_1
    AppLanguage.Hungarian -> HungarianTranslations.tagFeedback_insufficientTagStorage_1
    AppLanguage.Indonesian -> IndonesianTranslations.tagFeedback_insufficientTagStorage_1
    AppLanguage.Italian -> ItalianTranslations.tagFeedback_insufficientTagStorage_1
    AppLanguage.Japanese -> JapaneseTranslations.tagFeedback_insufficientTagStorage_1
    AppLanguage.Korean -> KoreanTranslations.tagFeedback_insufficientTagStorage_1
    AppLanguage.Latvian -> LatvianTranslations.tagFeedback_insufficientTagStorage_1
    AppLanguage.Lithuanian -> LithuanianTranslations.tagFeedback_insufficientTagStorage_1
    AppLanguage.Macedonian -> MacedonianTranslations.tagFeedback_insufficientTagStorage_1
    AppLanguage.Malay -> MalayTranslations.tagFeedback_insufficientTagStorage_1
    AppLanguage.Norwegian -> NorwegianTranslations.tagFeedback_insufficientTagStorage_1
    AppLanguage.Polish -> PolishTranslations.tagFeedback_insufficientTagStorage_1
    AppLanguage.Portuguese -> PortugueseTranslations.tagFeedback_insufficientTagStorage_1
    AppLanguage.Punjabi -> PunjabiTranslations.tagFeedback_insufficientTagStorage_1
    AppLanguage.Romanian -> RomanianTranslations.tagFeedback_insufficientTagStorage_1
    AppLanguage.Russian -> RussianTranslations.tagFeedback_insufficientTagStorage_1
    AppLanguage.Serbian -> SerbianTranslations.tagFeedback_insufficientTagStorage_1
    AppLanguage.Slovak -> SlovakTranslations.tagFeedback_insufficientTagStorage_1
    AppLanguage.Slovenian -> SlovenianTranslations.tagFeedback_insufficientTagStorage_1
    AppLanguage.Spanish -> SpanishTranslations.tagFeedback_insufficientTagStorage_1
    AppLanguage.Swedish -> SwedishTranslations.tagFeedback_insufficientTagStorage_1
    AppLanguage.Thai -> ThaiTranslations.tagFeedback_insufficientTagStorage_1
    AppLanguage.Ukrainian -> UkrainianTranslations.tagFeedback_insufficientTagStorage_1
    AppLanguage.Urdu -> UrduTranslations.tagFeedback_insufficientTagStorage_1
}

private fun unsupportedTagUriError(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.tagFeedback_unsupportedTagUriError_1
    AppLanguage.Bengali -> BengaliTranslations.tagFeedback_unsupportedTagUriError_1
    AppLanguage.Bosnian -> BosnianTranslations.tagFeedback_unsupportedTagUriError_1
    AppLanguage.Bulgarian -> BulgarianTranslations.tagFeedback_unsupportedTagUriError_1
    AppLanguage.Catalan -> CatalanTranslations.tagFeedback_unsupportedTagUriError_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.tagFeedback_unsupportedTagUriError_1
    AppLanguage.Croatian -> CroatianTranslations.tagFeedback_unsupportedTagUriError_1
    AppLanguage.Czech -> CzechTranslations.tagFeedback_unsupportedTagUriError_1
    AppLanguage.Danish -> DanishTranslations.tagFeedback_unsupportedTagUriError_1
    AppLanguage.Dutch -> DutchTranslations.tagFeedback_unsupportedTagUriError_1
    AppLanguage.English -> EnglishTranslations.tagFeedback_unsupportedTagUriError_1
    AppLanguage.Estonian -> EstonianTranslations.tagFeedback_unsupportedTagUriError_1
    AppLanguage.Finnish -> FinnishTranslations.tagFeedback_unsupportedTagUriError_1
    AppLanguage.French -> FrenchTranslations.tagFeedback_unsupportedTagUriError_1
    AppLanguage.German -> GermanTranslations.tagFeedback_unsupportedTagUriError_1
    AppLanguage.Greek -> GreekTranslations.tagFeedback_unsupportedTagUriError_1
    AppLanguage.Hindi -> HindiTranslations.tagFeedback_unsupportedTagUriError_1
    AppLanguage.Hungarian -> HungarianTranslations.tagFeedback_unsupportedTagUriError_1
    AppLanguage.Indonesian -> IndonesianTranslations.tagFeedback_unsupportedTagUriError_1
    AppLanguage.Italian -> ItalianTranslations.tagFeedback_unsupportedTagUriError_1
    AppLanguage.Japanese -> JapaneseTranslations.tagFeedback_unsupportedTagUriError_1
    AppLanguage.Korean -> KoreanTranslations.tagFeedback_unsupportedTagUriError_1
    AppLanguage.Latvian -> LatvianTranslations.tagFeedback_unsupportedTagUriError_1
    AppLanguage.Lithuanian -> LithuanianTranslations.tagFeedback_unsupportedTagUriError_1
    AppLanguage.Macedonian -> MacedonianTranslations.tagFeedback_unsupportedTagUriError_1
    AppLanguage.Malay -> MalayTranslations.tagFeedback_unsupportedTagUriError_1
    AppLanguage.Norwegian -> NorwegianTranslations.tagFeedback_unsupportedTagUriError_1
    AppLanguage.Polish -> PolishTranslations.tagFeedback_unsupportedTagUriError_1
    AppLanguage.Portuguese -> PortugueseTranslations.tagFeedback_unsupportedTagUriError_1
    AppLanguage.Punjabi -> PunjabiTranslations.tagFeedback_unsupportedTagUriError_1
    AppLanguage.Romanian -> RomanianTranslations.tagFeedback_unsupportedTagUriError_1
    AppLanguage.Russian -> RussianTranslations.tagFeedback_unsupportedTagUriError_1
    AppLanguage.Serbian -> SerbianTranslations.tagFeedback_unsupportedTagUriError_1
    AppLanguage.Slovak -> SlovakTranslations.tagFeedback_unsupportedTagUriError_1
    AppLanguage.Slovenian -> SlovenianTranslations.tagFeedback_unsupportedTagUriError_1
    AppLanguage.Spanish -> SpanishTranslations.tagFeedback_unsupportedTagUriError_1
    AppLanguage.Swedish -> SwedishTranslations.tagFeedback_unsupportedTagUriError_1
    AppLanguage.Thai -> ThaiTranslations.tagFeedback_unsupportedTagUriError_1
    AppLanguage.Ukrainian -> UkrainianTranslations.tagFeedback_unsupportedTagUriError_1
    AppLanguage.Urdu -> UrduTranslations.tagFeedback_unsupportedTagUriError_1
}

private fun fileWriteOpenError(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.tagFeedback_fileWriteOpenError_1
    AppLanguage.Bengali -> BengaliTranslations.tagFeedback_fileWriteOpenError_1
    AppLanguage.Bosnian -> BosnianTranslations.tagFeedback_fileWriteOpenError_1
    AppLanguage.Bulgarian -> BulgarianTranslations.tagFeedback_fileWriteOpenError_1
    AppLanguage.Catalan -> CatalanTranslations.tagFeedback_fileWriteOpenError_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.tagFeedback_fileWriteOpenError_1
    AppLanguage.Croatian -> CroatianTranslations.tagFeedback_fileWriteOpenError_1
    AppLanguage.Czech -> CzechTranslations.tagFeedback_fileWriteOpenError_1
    AppLanguage.Danish -> DanishTranslations.tagFeedback_fileWriteOpenError_1
    AppLanguage.Dutch -> DutchTranslations.tagFeedback_fileWriteOpenError_1
    AppLanguage.English -> EnglishTranslations.tagFeedback_fileWriteOpenError_1
    AppLanguage.Estonian -> EstonianTranslations.tagFeedback_fileWriteOpenError_1
    AppLanguage.Finnish -> FinnishTranslations.tagFeedback_fileWriteOpenError_1
    AppLanguage.French -> FrenchTranslations.tagFeedback_fileWriteOpenError_1
    AppLanguage.German -> GermanTranslations.tagFeedback_fileWriteOpenError_1
    AppLanguage.Greek -> GreekTranslations.tagFeedback_fileWriteOpenError_1
    AppLanguage.Hindi -> HindiTranslations.tagFeedback_fileWriteOpenError_1
    AppLanguage.Hungarian -> HungarianTranslations.tagFeedback_fileWriteOpenError_1
    AppLanguage.Indonesian -> IndonesianTranslations.tagFeedback_fileWriteOpenError_1
    AppLanguage.Italian -> ItalianTranslations.tagFeedback_fileWriteOpenError_1
    AppLanguage.Japanese -> JapaneseTranslations.tagFeedback_fileWriteOpenError_1
    AppLanguage.Korean -> KoreanTranslations.tagFeedback_fileWriteOpenError_1
    AppLanguage.Latvian -> LatvianTranslations.tagFeedback_fileWriteOpenError_1
    AppLanguage.Lithuanian -> LithuanianTranslations.tagFeedback_fileWriteOpenError_1
    AppLanguage.Macedonian -> MacedonianTranslations.tagFeedback_fileWriteOpenError_1
    AppLanguage.Malay -> MalayTranslations.tagFeedback_fileWriteOpenError_1
    AppLanguage.Norwegian -> NorwegianTranslations.tagFeedback_fileWriteOpenError_1
    AppLanguage.Polish -> PolishTranslations.tagFeedback_fileWriteOpenError_1
    AppLanguage.Portuguese -> PortugueseTranslations.tagFeedback_fileWriteOpenError_1
    AppLanguage.Punjabi -> PunjabiTranslations.tagFeedback_fileWriteOpenError_1
    AppLanguage.Romanian -> RomanianTranslations.tagFeedback_fileWriteOpenError_1
    AppLanguage.Russian -> RussianTranslations.tagFeedback_fileWriteOpenError_1
    AppLanguage.Serbian -> SerbianTranslations.tagFeedback_fileWriteOpenError_1
    AppLanguage.Slovak -> SlovakTranslations.tagFeedback_fileWriteOpenError_1
    AppLanguage.Slovenian -> SlovenianTranslations.tagFeedback_fileWriteOpenError_1
    AppLanguage.Spanish -> SpanishTranslations.tagFeedback_fileWriteOpenError_1
    AppLanguage.Swedish -> SwedishTranslations.tagFeedback_fileWriteOpenError_1
    AppLanguage.Thai -> ThaiTranslations.tagFeedback_fileWriteOpenError_1
    AppLanguage.Ukrainian -> UkrainianTranslations.tagFeedback_fileWriteOpenError_1
    AppLanguage.Urdu -> UrduTranslations.tagFeedback_fileWriteOpenError_1
}

private fun sourceFileOpenError(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.tagFeedback_sourceFileOpenError_1
    AppLanguage.Bengali -> BengaliTranslations.tagFeedback_sourceFileOpenError_1
    AppLanguage.Bosnian -> BosnianTranslations.tagFeedback_sourceFileOpenError_1
    AppLanguage.Bulgarian -> BulgarianTranslations.tagFeedback_sourceFileOpenError_1
    AppLanguage.Catalan -> CatalanTranslations.tagFeedback_sourceFileOpenError_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.tagFeedback_sourceFileOpenError_1
    AppLanguage.Croatian -> CroatianTranslations.tagFeedback_sourceFileOpenError_1
    AppLanguage.Czech -> CzechTranslations.tagFeedback_sourceFileOpenError_1
    AppLanguage.Danish -> DanishTranslations.tagFeedback_sourceFileOpenError_1
    AppLanguage.Dutch -> DutchTranslations.tagFeedback_sourceFileOpenError_1
    AppLanguage.English -> EnglishTranslations.tagFeedback_sourceFileOpenError_1
    AppLanguage.Estonian -> EstonianTranslations.tagFeedback_sourceFileOpenError_1
    AppLanguage.Finnish -> FinnishTranslations.tagFeedback_sourceFileOpenError_1
    AppLanguage.French -> FrenchTranslations.tagFeedback_sourceFileOpenError_1
    AppLanguage.German -> GermanTranslations.tagFeedback_sourceFileOpenError_1
    AppLanguage.Greek -> GreekTranslations.tagFeedback_sourceFileOpenError_1
    AppLanguage.Hindi -> HindiTranslations.tagFeedback_sourceFileOpenError_1
    AppLanguage.Hungarian -> HungarianTranslations.tagFeedback_sourceFileOpenError_1
    AppLanguage.Indonesian -> IndonesianTranslations.tagFeedback_sourceFileOpenError_1
    AppLanguage.Italian -> ItalianTranslations.tagFeedback_sourceFileOpenError_1
    AppLanguage.Japanese -> JapaneseTranslations.tagFeedback_sourceFileOpenError_1
    AppLanguage.Korean -> KoreanTranslations.tagFeedback_sourceFileOpenError_1
    AppLanguage.Latvian -> LatvianTranslations.tagFeedback_sourceFileOpenError_1
    AppLanguage.Lithuanian -> LithuanianTranslations.tagFeedback_sourceFileOpenError_1
    AppLanguage.Macedonian -> MacedonianTranslations.tagFeedback_sourceFileOpenError_1
    AppLanguage.Malay -> MalayTranslations.tagFeedback_sourceFileOpenError_1
    AppLanguage.Norwegian -> NorwegianTranslations.tagFeedback_sourceFileOpenError_1
    AppLanguage.Polish -> PolishTranslations.tagFeedback_sourceFileOpenError_1
    AppLanguage.Portuguese -> PortugueseTranslations.tagFeedback_sourceFileOpenError_1
    AppLanguage.Punjabi -> PunjabiTranslations.tagFeedback_sourceFileOpenError_1
    AppLanguage.Romanian -> RomanianTranslations.tagFeedback_sourceFileOpenError_1
    AppLanguage.Russian -> RussianTranslations.tagFeedback_sourceFileOpenError_1
    AppLanguage.Serbian -> SerbianTranslations.tagFeedback_sourceFileOpenError_1
    AppLanguage.Slovak -> SlovakTranslations.tagFeedback_sourceFileOpenError_1
    AppLanguage.Slovenian -> SlovenianTranslations.tagFeedback_sourceFileOpenError_1
    AppLanguage.Spanish -> SpanishTranslations.tagFeedback_sourceFileOpenError_1
    AppLanguage.Swedish -> SwedishTranslations.tagFeedback_sourceFileOpenError_1
    AppLanguage.Thai -> ThaiTranslations.tagFeedback_sourceFileOpenError_1
    AppLanguage.Ukrainian -> UkrainianTranslations.tagFeedback_sourceFileOpenError_1
    AppLanguage.Urdu -> UrduTranslations.tagFeedback_sourceFileOpenError_1
}

private fun tagDocumentWriteCapabilityError(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.tagFeedback_tagDocumentWriteCapabilityError_1
    AppLanguage.Bengali -> BengaliTranslations.tagFeedback_tagDocumentWriteCapabilityError_1
    AppLanguage.Bosnian -> BosnianTranslations.tagFeedback_tagDocumentWriteCapabilityError_1
    AppLanguage.Bulgarian -> BulgarianTranslations.tagFeedback_tagDocumentWriteCapabilityError_1
    AppLanguage.Catalan -> CatalanTranslations.tagFeedback_tagDocumentWriteCapabilityError_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.tagFeedback_tagDocumentWriteCapabilityError_1
    AppLanguage.Croatian -> CroatianTranslations.tagFeedback_tagDocumentWriteCapabilityError_1
    AppLanguage.Czech -> CzechTranslations.tagFeedback_tagDocumentWriteCapabilityError_1
    AppLanguage.Danish -> DanishTranslations.tagFeedback_tagDocumentWriteCapabilityError_1
    AppLanguage.Dutch -> DutchTranslations.tagFeedback_tagDocumentWriteCapabilityError_1
    AppLanguage.English -> EnglishTranslations.tagFeedback_tagDocumentWriteCapabilityError_1
    AppLanguage.Estonian -> EstonianTranslations.tagFeedback_tagDocumentWriteCapabilityError_1
    AppLanguage.Finnish -> FinnishTranslations.tagFeedback_tagDocumentWriteCapabilityError_1
    AppLanguage.French -> FrenchTranslations.tagFeedback_tagDocumentWriteCapabilityError_1
    AppLanguage.German -> GermanTranslations.tagFeedback_tagDocumentWriteCapabilityError_1
    AppLanguage.Greek -> GreekTranslations.tagFeedback_tagDocumentWriteCapabilityError_1
    AppLanguage.Hindi -> HindiTranslations.tagFeedback_tagDocumentWriteCapabilityError_1
    AppLanguage.Hungarian -> HungarianTranslations.tagFeedback_tagDocumentWriteCapabilityError_1
    AppLanguage.Indonesian -> IndonesianTranslations.tagFeedback_tagDocumentWriteCapabilityError_1
    AppLanguage.Italian -> ItalianTranslations.tagFeedback_tagDocumentWriteCapabilityError_1
    AppLanguage.Japanese -> JapaneseTranslations.tagFeedback_tagDocumentWriteCapabilityError_1
    AppLanguage.Korean -> KoreanTranslations.tagFeedback_tagDocumentWriteCapabilityError_1
    AppLanguage.Latvian -> LatvianTranslations.tagFeedback_tagDocumentWriteCapabilityError_1
    AppLanguage.Lithuanian -> LithuanianTranslations.tagFeedback_tagDocumentWriteCapabilityError_1
    AppLanguage.Macedonian -> MacedonianTranslations.tagFeedback_tagDocumentWriteCapabilityError_1
    AppLanguage.Malay -> MalayTranslations.tagFeedback_tagDocumentWriteCapabilityError_1
    AppLanguage.Norwegian -> NorwegianTranslations.tagFeedback_tagDocumentWriteCapabilityError_1
    AppLanguage.Polish -> PolishTranslations.tagFeedback_tagDocumentWriteCapabilityError_1
    AppLanguage.Portuguese -> PortugueseTranslations.tagFeedback_tagDocumentWriteCapabilityError_1
    AppLanguage.Punjabi -> PunjabiTranslations.tagFeedback_tagDocumentWriteCapabilityError_1
    AppLanguage.Romanian -> RomanianTranslations.tagFeedback_tagDocumentWriteCapabilityError_1
    AppLanguage.Russian -> RussianTranslations.tagFeedback_tagDocumentWriteCapabilityError_1
    AppLanguage.Serbian -> SerbianTranslations.tagFeedback_tagDocumentWriteCapabilityError_1
    AppLanguage.Slovak -> SlovakTranslations.tagFeedback_tagDocumentWriteCapabilityError_1
    AppLanguage.Slovenian -> SlovenianTranslations.tagFeedback_tagDocumentWriteCapabilityError_1
    AppLanguage.Spanish -> SpanishTranslations.tagFeedback_tagDocumentWriteCapabilityError_1
    AppLanguage.Swedish -> SwedishTranslations.tagFeedback_tagDocumentWriteCapabilityError_1
    AppLanguage.Thai -> ThaiTranslations.tagFeedback_tagDocumentWriteCapabilityError_1
    AppLanguage.Ukrainian -> UkrainianTranslations.tagFeedback_tagDocumentWriteCapabilityError_1
    AppLanguage.Urdu -> UrduTranslations.tagFeedback_tagDocumentWriteCapabilityError_1
}

private fun tagProviderDoesNotSupportWriting(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.tagFeedback_tagProviderDoesNotSupportWriting_1
    AppLanguage.Bengali -> BengaliTranslations.tagFeedback_tagProviderDoesNotSupportWriting_1
    AppLanguage.Bosnian -> BosnianTranslations.tagFeedback_tagProviderDoesNotSupportWriting_1
    AppLanguage.Bulgarian -> BulgarianTranslations.tagFeedback_tagProviderDoesNotSupportWriting_1
    AppLanguage.Catalan -> CatalanTranslations.tagFeedback_tagProviderDoesNotSupportWriting_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.tagFeedback_tagProviderDoesNotSupportWriting_1
    AppLanguage.Croatian -> CroatianTranslations.tagFeedback_tagProviderDoesNotSupportWriting_1
    AppLanguage.Czech -> CzechTranslations.tagFeedback_tagProviderDoesNotSupportWriting_1
    AppLanguage.Danish -> DanishTranslations.tagFeedback_tagProviderDoesNotSupportWriting_1
    AppLanguage.Dutch -> DutchTranslations.tagFeedback_tagProviderDoesNotSupportWriting_1
    AppLanguage.English -> EnglishTranslations.tagFeedback_tagProviderDoesNotSupportWriting_1
    AppLanguage.Estonian -> EstonianTranslations.tagFeedback_tagProviderDoesNotSupportWriting_1
    AppLanguage.Finnish -> FinnishTranslations.tagFeedback_tagProviderDoesNotSupportWriting_1
    AppLanguage.French -> FrenchTranslations.tagFeedback_tagProviderDoesNotSupportWriting_1
    AppLanguage.German -> GermanTranslations.tagFeedback_tagProviderDoesNotSupportWriting_1
    AppLanguage.Greek -> GreekTranslations.tagFeedback_tagProviderDoesNotSupportWriting_1
    AppLanguage.Hindi -> HindiTranslations.tagFeedback_tagProviderDoesNotSupportWriting_1
    AppLanguage.Hungarian -> HungarianTranslations.tagFeedback_tagProviderDoesNotSupportWriting_1
    AppLanguage.Indonesian -> IndonesianTranslations.tagFeedback_tagProviderDoesNotSupportWriting_1
    AppLanguage.Italian -> ItalianTranslations.tagFeedback_tagProviderDoesNotSupportWriting_1
    AppLanguage.Japanese -> JapaneseTranslations.tagFeedback_tagProviderDoesNotSupportWriting_1
    AppLanguage.Korean -> KoreanTranslations.tagFeedback_tagProviderDoesNotSupportWriting_1
    AppLanguage.Latvian -> LatvianTranslations.tagFeedback_tagProviderDoesNotSupportWriting_1
    AppLanguage.Lithuanian -> LithuanianTranslations.tagFeedback_tagProviderDoesNotSupportWriting_1
    AppLanguage.Macedonian -> MacedonianTranslations.tagFeedback_tagProviderDoesNotSupportWriting_1
    AppLanguage.Malay -> MalayTranslations.tagFeedback_tagProviderDoesNotSupportWriting_1
    AppLanguage.Norwegian -> NorwegianTranslations.tagFeedback_tagProviderDoesNotSupportWriting_1
    AppLanguage.Polish -> PolishTranslations.tagFeedback_tagProviderDoesNotSupportWriting_1
    AppLanguage.Portuguese -> PortugueseTranslations.tagFeedback_tagProviderDoesNotSupportWriting_1
    AppLanguage.Punjabi -> PunjabiTranslations.tagFeedback_tagProviderDoesNotSupportWriting_1
    AppLanguage.Romanian -> RomanianTranslations.tagFeedback_tagProviderDoesNotSupportWriting_1
    AppLanguage.Russian -> RussianTranslations.tagFeedback_tagProviderDoesNotSupportWriting_1
    AppLanguage.Serbian -> SerbianTranslations.tagFeedback_tagProviderDoesNotSupportWriting_1
    AppLanguage.Slovak -> SlovakTranslations.tagFeedback_tagProviderDoesNotSupportWriting_1
    AppLanguage.Slovenian -> SlovenianTranslations.tagFeedback_tagProviderDoesNotSupportWriting_1
    AppLanguage.Spanish -> SpanishTranslations.tagFeedback_tagProviderDoesNotSupportWriting_1
    AppLanguage.Swedish -> SwedishTranslations.tagFeedback_tagProviderDoesNotSupportWriting_1
    AppLanguage.Thai -> ThaiTranslations.tagFeedback_tagProviderDoesNotSupportWriting_1
    AppLanguage.Ukrainian -> UkrainianTranslations.tagFeedback_tagProviderDoesNotSupportWriting_1
    AppLanguage.Urdu -> UrduTranslations.tagFeedback_tagProviderDoesNotSupportWriting_1
}

private fun tagDocumentWriteAccessMissing(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.tagFeedback_tagDocumentWriteAccessMissing_1
    AppLanguage.Bengali -> BengaliTranslations.tagFeedback_tagDocumentWriteAccessMissing_1
    AppLanguage.Bosnian -> BosnianTranslations.tagFeedback_tagDocumentWriteAccessMissing_1
    AppLanguage.Bulgarian -> BulgarianTranslations.tagFeedback_tagDocumentWriteAccessMissing_1
    AppLanguage.Catalan -> CatalanTranslations.tagFeedback_tagDocumentWriteAccessMissing_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.tagFeedback_tagDocumentWriteAccessMissing_1
    AppLanguage.Croatian -> CroatianTranslations.tagFeedback_tagDocumentWriteAccessMissing_1
    AppLanguage.Czech -> CzechTranslations.tagFeedback_tagDocumentWriteAccessMissing_1
    AppLanguage.Danish -> DanishTranslations.tagFeedback_tagDocumentWriteAccessMissing_1
    AppLanguage.Dutch -> DutchTranslations.tagFeedback_tagDocumentWriteAccessMissing_1
    AppLanguage.English -> EnglishTranslations.tagFeedback_tagDocumentWriteAccessMissing_1
    AppLanguage.Estonian -> EstonianTranslations.tagFeedback_tagDocumentWriteAccessMissing_1
    AppLanguage.Finnish -> FinnishTranslations.tagFeedback_tagDocumentWriteAccessMissing_1
    AppLanguage.French -> FrenchTranslations.tagFeedback_tagDocumentWriteAccessMissing_1
    AppLanguage.German -> GermanTranslations.tagFeedback_tagDocumentWriteAccessMissing_1
    AppLanguage.Greek -> GreekTranslations.tagFeedback_tagDocumentWriteAccessMissing_1
    AppLanguage.Hindi -> HindiTranslations.tagFeedback_tagDocumentWriteAccessMissing_1
    AppLanguage.Hungarian -> HungarianTranslations.tagFeedback_tagDocumentWriteAccessMissing_1
    AppLanguage.Indonesian -> IndonesianTranslations.tagFeedback_tagDocumentWriteAccessMissing_1
    AppLanguage.Italian -> ItalianTranslations.tagFeedback_tagDocumentWriteAccessMissing_1
    AppLanguage.Japanese -> JapaneseTranslations.tagFeedback_tagDocumentWriteAccessMissing_1
    AppLanguage.Korean -> KoreanTranslations.tagFeedback_tagDocumentWriteAccessMissing_1
    AppLanguage.Latvian -> LatvianTranslations.tagFeedback_tagDocumentWriteAccessMissing_1
    AppLanguage.Lithuanian -> LithuanianTranslations.tagFeedback_tagDocumentWriteAccessMissing_1
    AppLanguage.Macedonian -> MacedonianTranslations.tagFeedback_tagDocumentWriteAccessMissing_1
    AppLanguage.Malay -> MalayTranslations.tagFeedback_tagDocumentWriteAccessMissing_1
    AppLanguage.Norwegian -> NorwegianTranslations.tagFeedback_tagDocumentWriteAccessMissing_1
    AppLanguage.Polish -> PolishTranslations.tagFeedback_tagDocumentWriteAccessMissing_1
    AppLanguage.Portuguese -> PortugueseTranslations.tagFeedback_tagDocumentWriteAccessMissing_1
    AppLanguage.Punjabi -> PunjabiTranslations.tagFeedback_tagDocumentWriteAccessMissing_1
    AppLanguage.Romanian -> RomanianTranslations.tagFeedback_tagDocumentWriteAccessMissing_1
    AppLanguage.Russian -> RussianTranslations.tagFeedback_tagDocumentWriteAccessMissing_1
    AppLanguage.Serbian -> SerbianTranslations.tagFeedback_tagDocumentWriteAccessMissing_1
    AppLanguage.Slovak -> SlovakTranslations.tagFeedback_tagDocumentWriteAccessMissing_1
    AppLanguage.Slovenian -> SlovenianTranslations.tagFeedback_tagDocumentWriteAccessMissing_1
    AppLanguage.Spanish -> SpanishTranslations.tagFeedback_tagDocumentWriteAccessMissing_1
    AppLanguage.Swedish -> SwedishTranslations.tagFeedback_tagDocumentWriteAccessMissing_1
    AppLanguage.Thai -> ThaiTranslations.tagFeedback_tagDocumentWriteAccessMissing_1
    AppLanguage.Ukrainian -> UkrainianTranslations.tagFeedback_tagDocumentWriteAccessMissing_1
    AppLanguage.Urdu -> UrduTranslations.tagFeedback_tagDocumentWriteAccessMissing_1
}

private fun tagProviderDoesNotAllowEditing(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.tagFeedback_tagProviderDoesNotAllowEditing_1
    AppLanguage.Bengali -> BengaliTranslations.tagFeedback_tagProviderDoesNotAllowEditing_1
    AppLanguage.Bosnian -> BosnianTranslations.tagFeedback_tagProviderDoesNotAllowEditing_1
    AppLanguage.Bulgarian -> BulgarianTranslations.tagFeedback_tagProviderDoesNotAllowEditing_1
    AppLanguage.Catalan -> CatalanTranslations.tagFeedback_tagProviderDoesNotAllowEditing_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.tagFeedback_tagProviderDoesNotAllowEditing_1
    AppLanguage.Croatian -> CroatianTranslations.tagFeedback_tagProviderDoesNotAllowEditing_1
    AppLanguage.Czech -> CzechTranslations.tagFeedback_tagProviderDoesNotAllowEditing_1
    AppLanguage.Danish -> DanishTranslations.tagFeedback_tagProviderDoesNotAllowEditing_1
    AppLanguage.Dutch -> DutchTranslations.tagFeedback_tagProviderDoesNotAllowEditing_1
    AppLanguage.English -> EnglishTranslations.tagFeedback_tagProviderDoesNotAllowEditing_1
    AppLanguage.Estonian -> EstonianTranslations.tagFeedback_tagProviderDoesNotAllowEditing_1
    AppLanguage.Finnish -> FinnishTranslations.tagFeedback_tagProviderDoesNotAllowEditing_1
    AppLanguage.French -> FrenchTranslations.tagFeedback_tagProviderDoesNotAllowEditing_1
    AppLanguage.German -> GermanTranslations.tagFeedback_tagProviderDoesNotAllowEditing_1
    AppLanguage.Greek -> GreekTranslations.tagFeedback_tagProviderDoesNotAllowEditing_1
    AppLanguage.Hindi -> HindiTranslations.tagFeedback_tagProviderDoesNotAllowEditing_1
    AppLanguage.Hungarian -> HungarianTranslations.tagFeedback_tagProviderDoesNotAllowEditing_1
    AppLanguage.Indonesian -> IndonesianTranslations.tagFeedback_tagProviderDoesNotAllowEditing_1
    AppLanguage.Italian -> ItalianTranslations.tagFeedback_tagProviderDoesNotAllowEditing_1
    AppLanguage.Japanese -> JapaneseTranslations.tagFeedback_tagProviderDoesNotAllowEditing_1
    AppLanguage.Korean -> KoreanTranslations.tagFeedback_tagProviderDoesNotAllowEditing_1
    AppLanguage.Latvian -> LatvianTranslations.tagFeedback_tagProviderDoesNotAllowEditing_1
    AppLanguage.Lithuanian -> LithuanianTranslations.tagFeedback_tagProviderDoesNotAllowEditing_1
    AppLanguage.Macedonian -> MacedonianTranslations.tagFeedback_tagProviderDoesNotAllowEditing_1
    AppLanguage.Malay -> MalayTranslations.tagFeedback_tagProviderDoesNotAllowEditing_1
    AppLanguage.Norwegian -> NorwegianTranslations.tagFeedback_tagProviderDoesNotAllowEditing_1
    AppLanguage.Polish -> PolishTranslations.tagFeedback_tagProviderDoesNotAllowEditing_1
    AppLanguage.Portuguese -> PortugueseTranslations.tagFeedback_tagProviderDoesNotAllowEditing_1
    AppLanguage.Punjabi -> PunjabiTranslations.tagFeedback_tagProviderDoesNotAllowEditing_1
    AppLanguage.Romanian -> RomanianTranslations.tagFeedback_tagProviderDoesNotAllowEditing_1
    AppLanguage.Russian -> RussianTranslations.tagFeedback_tagProviderDoesNotAllowEditing_1
    AppLanguage.Serbian -> SerbianTranslations.tagFeedback_tagProviderDoesNotAllowEditing_1
    AppLanguage.Slovak -> SlovakTranslations.tagFeedback_tagProviderDoesNotAllowEditing_1
    AppLanguage.Slovenian -> SlovenianTranslations.tagFeedback_tagProviderDoesNotAllowEditing_1
    AppLanguage.Spanish -> SpanishTranslations.tagFeedback_tagProviderDoesNotAllowEditing_1
    AppLanguage.Swedish -> SwedishTranslations.tagFeedback_tagProviderDoesNotAllowEditing_1
    AppLanguage.Thai -> ThaiTranslations.tagFeedback_tagProviderDoesNotAllowEditing_1
    AppLanguage.Ukrainian -> UkrainianTranslations.tagFeedback_tagProviderDoesNotAllowEditing_1
    AppLanguage.Urdu -> UrduTranslations.tagFeedback_tagProviderDoesNotAllowEditing_1
}

private fun tagFolderWriteAccessMissing(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.tagFeedback_tagFolderWriteAccessMissing_1
    AppLanguage.Bengali -> BengaliTranslations.tagFeedback_tagFolderWriteAccessMissing_1
    AppLanguage.Bosnian -> BosnianTranslations.tagFeedback_tagFolderWriteAccessMissing_1
    AppLanguage.Bulgarian -> BulgarianTranslations.tagFeedback_tagFolderWriteAccessMissing_1
    AppLanguage.Catalan -> CatalanTranslations.tagFeedback_tagFolderWriteAccessMissing_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.tagFeedback_tagFolderWriteAccessMissing_1
    AppLanguage.Croatian -> CroatianTranslations.tagFeedback_tagFolderWriteAccessMissing_1
    AppLanguage.Czech -> CzechTranslations.tagFeedback_tagFolderWriteAccessMissing_1
    AppLanguage.Danish -> DanishTranslations.tagFeedback_tagFolderWriteAccessMissing_1
    AppLanguage.Dutch -> DutchTranslations.tagFeedback_tagFolderWriteAccessMissing_1
    AppLanguage.English -> EnglishTranslations.tagFeedback_tagFolderWriteAccessMissing_1
    AppLanguage.Estonian -> EstonianTranslations.tagFeedback_tagFolderWriteAccessMissing_1
    AppLanguage.Finnish -> FinnishTranslations.tagFeedback_tagFolderWriteAccessMissing_1
    AppLanguage.French -> FrenchTranslations.tagFeedback_tagFolderWriteAccessMissing_1
    AppLanguage.German -> GermanTranslations.tagFeedback_tagFolderWriteAccessMissing_1
    AppLanguage.Greek -> GreekTranslations.tagFeedback_tagFolderWriteAccessMissing_1
    AppLanguage.Hindi -> HindiTranslations.tagFeedback_tagFolderWriteAccessMissing_1
    AppLanguage.Hungarian -> HungarianTranslations.tagFeedback_tagFolderWriteAccessMissing_1
    AppLanguage.Indonesian -> IndonesianTranslations.tagFeedback_tagFolderWriteAccessMissing_1
    AppLanguage.Italian -> ItalianTranslations.tagFeedback_tagFolderWriteAccessMissing_1
    AppLanguage.Japanese -> JapaneseTranslations.tagFeedback_tagFolderWriteAccessMissing_1
    AppLanguage.Korean -> KoreanTranslations.tagFeedback_tagFolderWriteAccessMissing_1
    AppLanguage.Latvian -> LatvianTranslations.tagFeedback_tagFolderWriteAccessMissing_1
    AppLanguage.Lithuanian -> LithuanianTranslations.tagFeedback_tagFolderWriteAccessMissing_1
    AppLanguage.Macedonian -> MacedonianTranslations.tagFeedback_tagFolderWriteAccessMissing_1
    AppLanguage.Malay -> MalayTranslations.tagFeedback_tagFolderWriteAccessMissing_1
    AppLanguage.Norwegian -> NorwegianTranslations.tagFeedback_tagFolderWriteAccessMissing_1
    AppLanguage.Polish -> PolishTranslations.tagFeedback_tagFolderWriteAccessMissing_1
    AppLanguage.Portuguese -> PortugueseTranslations.tagFeedback_tagFolderWriteAccessMissing_1
    AppLanguage.Punjabi -> PunjabiTranslations.tagFeedback_tagFolderWriteAccessMissing_1
    AppLanguage.Romanian -> RomanianTranslations.tagFeedback_tagFolderWriteAccessMissing_1
    AppLanguage.Russian -> RussianTranslations.tagFeedback_tagFolderWriteAccessMissing_1
    AppLanguage.Serbian -> SerbianTranslations.tagFeedback_tagFolderWriteAccessMissing_1
    AppLanguage.Slovak -> SlovakTranslations.tagFeedback_tagFolderWriteAccessMissing_1
    AppLanguage.Slovenian -> SlovenianTranslations.tagFeedback_tagFolderWriteAccessMissing_1
    AppLanguage.Spanish -> SpanishTranslations.tagFeedback_tagFolderWriteAccessMissing_1
    AppLanguage.Swedish -> SwedishTranslations.tagFeedback_tagFolderWriteAccessMissing_1
    AppLanguage.Thai -> ThaiTranslations.tagFeedback_tagFolderWriteAccessMissing_1
    AppLanguage.Ukrainian -> UkrainianTranslations.tagFeedback_tagFolderWriteAccessMissing_1
    AppLanguage.Urdu -> UrduTranslations.tagFeedback_tagFolderWriteAccessMissing_1
}

private fun unableToSaveAudiobookTags(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.tagFeedback_unableToSaveAudiobookTags_1
    AppLanguage.Bengali -> BengaliTranslations.tagFeedback_unableToSaveAudiobookTags_1
    AppLanguage.Bosnian -> BosnianTranslations.tagFeedback_unableToSaveAudiobookTags_1
    AppLanguage.Bulgarian -> BulgarianTranslations.tagFeedback_unableToSaveAudiobookTags_1
    AppLanguage.Catalan -> CatalanTranslations.tagFeedback_unableToSaveAudiobookTags_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.tagFeedback_unableToSaveAudiobookTags_1
    AppLanguage.Croatian -> CroatianTranslations.tagFeedback_unableToSaveAudiobookTags_1
    AppLanguage.Czech -> CzechTranslations.tagFeedback_unableToSaveAudiobookTags_1
    AppLanguage.Danish -> DanishTranslations.tagFeedback_unableToSaveAudiobookTags_1
    AppLanguage.Dutch -> DutchTranslations.tagFeedback_unableToSaveAudiobookTags_1
    AppLanguage.English -> EnglishTranslations.tagFeedback_unableToSaveAudiobookTags_1
    AppLanguage.Estonian -> EstonianTranslations.tagFeedback_unableToSaveAudiobookTags_1
    AppLanguage.Finnish -> FinnishTranslations.tagFeedback_unableToSaveAudiobookTags_1
    AppLanguage.French -> FrenchTranslations.tagFeedback_unableToSaveAudiobookTags_1
    AppLanguage.German -> GermanTranslations.tagFeedback_unableToSaveAudiobookTags_1
    AppLanguage.Greek -> GreekTranslations.tagFeedback_unableToSaveAudiobookTags_1
    AppLanguage.Hindi -> HindiTranslations.tagFeedback_unableToSaveAudiobookTags_1
    AppLanguage.Hungarian -> HungarianTranslations.tagFeedback_unableToSaveAudiobookTags_1
    AppLanguage.Indonesian -> IndonesianTranslations.tagFeedback_unableToSaveAudiobookTags_1
    AppLanguage.Italian -> ItalianTranslations.tagFeedback_unableToSaveAudiobookTags_1
    AppLanguage.Japanese -> JapaneseTranslations.tagFeedback_unableToSaveAudiobookTags_1
    AppLanguage.Korean -> KoreanTranslations.tagFeedback_unableToSaveAudiobookTags_1
    AppLanguage.Latvian -> LatvianTranslations.tagFeedback_unableToSaveAudiobookTags_1
    AppLanguage.Lithuanian -> LithuanianTranslations.tagFeedback_unableToSaveAudiobookTags_1
    AppLanguage.Macedonian -> MacedonianTranslations.tagFeedback_unableToSaveAudiobookTags_1
    AppLanguage.Malay -> MalayTranslations.tagFeedback_unableToSaveAudiobookTags_1
    AppLanguage.Norwegian -> NorwegianTranslations.tagFeedback_unableToSaveAudiobookTags_1
    AppLanguage.Polish -> PolishTranslations.tagFeedback_unableToSaveAudiobookTags_1
    AppLanguage.Portuguese -> PortugueseTranslations.tagFeedback_unableToSaveAudiobookTags_1
    AppLanguage.Punjabi -> PunjabiTranslations.tagFeedback_unableToSaveAudiobookTags_1
    AppLanguage.Romanian -> RomanianTranslations.tagFeedback_unableToSaveAudiobookTags_1
    AppLanguage.Russian -> RussianTranslations.tagFeedback_unableToSaveAudiobookTags_1
    AppLanguage.Serbian -> SerbianTranslations.tagFeedback_unableToSaveAudiobookTags_1
    AppLanguage.Slovak -> SlovakTranslations.tagFeedback_unableToSaveAudiobookTags_1
    AppLanguage.Slovenian -> SlovenianTranslations.tagFeedback_unableToSaveAudiobookTags_1
    AppLanguage.Spanish -> SpanishTranslations.tagFeedback_unableToSaveAudiobookTags_1
    AppLanguage.Swedish -> SwedishTranslations.tagFeedback_unableToSaveAudiobookTags_1
    AppLanguage.Thai -> ThaiTranslations.tagFeedback_unableToSaveAudiobookTags_1
    AppLanguage.Ukrainian -> UkrainianTranslations.tagFeedback_unableToSaveAudiobookTags_1
    AppLanguage.Urdu -> UrduTranslations.tagFeedback_unableToSaveAudiobookTags_1
}

private fun unableToSaveTags(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.tagFeedback_unableToSaveTags_1
    AppLanguage.Bengali -> BengaliTranslations.tagFeedback_unableToSaveTags_1
    AppLanguage.Bosnian -> BosnianTranslations.tagFeedback_unableToSaveTags_1
    AppLanguage.Bulgarian -> BulgarianTranslations.tagFeedback_unableToSaveTags_1
    AppLanguage.Catalan -> CatalanTranslations.tagFeedback_unableToSaveTags_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.tagFeedback_unableToSaveTags_1
    AppLanguage.Croatian -> CroatianTranslations.tagFeedback_unableToSaveTags_1
    AppLanguage.Czech -> CzechTranslations.tagFeedback_unableToSaveTags_1
    AppLanguage.Danish -> DanishTranslations.tagFeedback_unableToSaveTags_1
    AppLanguage.Dutch -> DutchTranslations.tagFeedback_unableToSaveTags_1
    AppLanguage.English -> EnglishTranslations.tagFeedback_unableToSaveTags_1
    AppLanguage.Estonian -> EstonianTranslations.tagFeedback_unableToSaveTags_1
    AppLanguage.Finnish -> FinnishTranslations.tagFeedback_unableToSaveTags_1
    AppLanguage.French -> FrenchTranslations.tagFeedback_unableToSaveTags_1
    AppLanguage.German -> GermanTranslations.tagFeedback_unableToSaveTags_1
    AppLanguage.Greek -> GreekTranslations.tagFeedback_unableToSaveTags_1
    AppLanguage.Hindi -> HindiTranslations.tagFeedback_unableToSaveTags_1
    AppLanguage.Hungarian -> HungarianTranslations.tagFeedback_unableToSaveTags_1
    AppLanguage.Indonesian -> IndonesianTranslations.tagFeedback_unableToSaveTags_1
    AppLanguage.Italian -> ItalianTranslations.tagFeedback_unableToSaveTags_1
    AppLanguage.Japanese -> JapaneseTranslations.tagFeedback_unableToSaveTags_1
    AppLanguage.Korean -> KoreanTranslations.tagFeedback_unableToSaveTags_1
    AppLanguage.Latvian -> LatvianTranslations.tagFeedback_unableToSaveTags_1
    AppLanguage.Lithuanian -> LithuanianTranslations.tagFeedback_unableToSaveTags_1
    AppLanguage.Macedonian -> MacedonianTranslations.tagFeedback_unableToSaveTags_1
    AppLanguage.Malay -> MalayTranslations.tagFeedback_unableToSaveTags_1
    AppLanguage.Norwegian -> NorwegianTranslations.tagFeedback_unableToSaveTags_1
    AppLanguage.Polish -> PolishTranslations.tagFeedback_unableToSaveTags_1
    AppLanguage.Portuguese -> PortugueseTranslations.tagFeedback_unableToSaveTags_1
    AppLanguage.Punjabi -> PunjabiTranslations.tagFeedback_unableToSaveTags_1
    AppLanguage.Romanian -> RomanianTranslations.tagFeedback_unableToSaveTags_1
    AppLanguage.Russian -> RussianTranslations.tagFeedback_unableToSaveTags_1
    AppLanguage.Serbian -> SerbianTranslations.tagFeedback_unableToSaveTags_1
    AppLanguage.Slovak -> SlovakTranslations.tagFeedback_unableToSaveTags_1
    AppLanguage.Slovenian -> SlovenianTranslations.tagFeedback_unableToSaveTags_1
    AppLanguage.Spanish -> SpanishTranslations.tagFeedback_unableToSaveTags_1
    AppLanguage.Swedish -> SwedishTranslations.tagFeedback_unableToSaveTags_1
    AppLanguage.Thai -> ThaiTranslations.tagFeedback_unableToSaveTags_1
    AppLanguage.Ukrainian -> UkrainianTranslations.tagFeedback_unableToSaveTags_1
    AppLanguage.Urdu -> UrduTranslations.tagFeedback_unableToSaveTags_1
}

private fun noTagsSaved(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.tagFeedback_noTagsSaved_1
    AppLanguage.Bengali -> BengaliTranslations.tagFeedback_noTagsSaved_1
    AppLanguage.Bosnian -> BosnianTranslations.tagFeedback_noTagsSaved_1
    AppLanguage.Bulgarian -> BulgarianTranslations.tagFeedback_noTagsSaved_1
    AppLanguage.Catalan -> CatalanTranslations.tagFeedback_noTagsSaved_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.tagFeedback_noTagsSaved_1
    AppLanguage.Croatian -> CroatianTranslations.tagFeedback_noTagsSaved_1
    AppLanguage.Czech -> CzechTranslations.tagFeedback_noTagsSaved_1
    AppLanguage.Danish -> DanishTranslations.tagFeedback_noTagsSaved_1
    AppLanguage.Dutch -> DutchTranslations.tagFeedback_noTagsSaved_1
    AppLanguage.English -> EnglishTranslations.tagFeedback_noTagsSaved_1
    AppLanguage.Estonian -> EstonianTranslations.tagFeedback_noTagsSaved_1
    AppLanguage.Finnish -> FinnishTranslations.tagFeedback_noTagsSaved_1
    AppLanguage.French -> FrenchTranslations.tagFeedback_noTagsSaved_1
    AppLanguage.German -> GermanTranslations.tagFeedback_noTagsSaved_1
    AppLanguage.Greek -> GreekTranslations.tagFeedback_noTagsSaved_1
    AppLanguage.Hindi -> HindiTranslations.tagFeedback_noTagsSaved_1
    AppLanguage.Hungarian -> HungarianTranslations.tagFeedback_noTagsSaved_1
    AppLanguage.Indonesian -> IndonesianTranslations.tagFeedback_noTagsSaved_1
    AppLanguage.Italian -> ItalianTranslations.tagFeedback_noTagsSaved_1
    AppLanguage.Japanese -> JapaneseTranslations.tagFeedback_noTagsSaved_1
    AppLanguage.Korean -> KoreanTranslations.tagFeedback_noTagsSaved_1
    AppLanguage.Latvian -> LatvianTranslations.tagFeedback_noTagsSaved_1
    AppLanguage.Lithuanian -> LithuanianTranslations.tagFeedback_noTagsSaved_1
    AppLanguage.Macedonian -> MacedonianTranslations.tagFeedback_noTagsSaved_1
    AppLanguage.Malay -> MalayTranslations.tagFeedback_noTagsSaved_1
    AppLanguage.Norwegian -> NorwegianTranslations.tagFeedback_noTagsSaved_1
    AppLanguage.Polish -> PolishTranslations.tagFeedback_noTagsSaved_1
    AppLanguage.Portuguese -> PortugueseTranslations.tagFeedback_noTagsSaved_1
    AppLanguage.Punjabi -> PunjabiTranslations.tagFeedback_noTagsSaved_1
    AppLanguage.Romanian -> RomanianTranslations.tagFeedback_noTagsSaved_1
    AppLanguage.Russian -> RussianTranslations.tagFeedback_noTagsSaved_1
    AppLanguage.Serbian -> SerbianTranslations.tagFeedback_noTagsSaved_1
    AppLanguage.Slovak -> SlovakTranslations.tagFeedback_noTagsSaved_1
    AppLanguage.Slovenian -> SlovenianTranslations.tagFeedback_noTagsSaved_1
    AppLanguage.Spanish -> SpanishTranslations.tagFeedback_noTagsSaved_1
    AppLanguage.Swedish -> SwedishTranslations.tagFeedback_noTagsSaved_1
    AppLanguage.Thai -> ThaiTranslations.tagFeedback_noTagsSaved_1
    AppLanguage.Ukrainian -> UkrainianTranslations.tagFeedback_noTagsSaved_1
    AppLanguage.Urdu -> UrduTranslations.tagFeedback_noTagsSaved_1
}

private fun tagWriteRequestPending(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.tagFeedback_tagWriteRequestPending_1
    AppLanguage.Bengali -> BengaliTranslations.tagFeedback_tagWriteRequestPending_1
    AppLanguage.Bosnian -> BosnianTranslations.tagFeedback_tagWriteRequestPending_1
    AppLanguage.Bulgarian -> BulgarianTranslations.tagFeedback_tagWriteRequestPending_1
    AppLanguage.Catalan -> CatalanTranslations.tagFeedback_tagWriteRequestPending_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.tagFeedback_tagWriteRequestPending_1
    AppLanguage.Croatian -> CroatianTranslations.tagFeedback_tagWriteRequestPending_1
    AppLanguage.Czech -> CzechTranslations.tagFeedback_tagWriteRequestPending_1
    AppLanguage.Danish -> DanishTranslations.tagFeedback_tagWriteRequestPending_1
    AppLanguage.Dutch -> DutchTranslations.tagFeedback_tagWriteRequestPending_1
    AppLanguage.English -> EnglishTranslations.tagFeedback_tagWriteRequestPending_1
    AppLanguage.Estonian -> EstonianTranslations.tagFeedback_tagWriteRequestPending_1
    AppLanguage.Finnish -> FinnishTranslations.tagFeedback_tagWriteRequestPending_1
    AppLanguage.French -> FrenchTranslations.tagFeedback_tagWriteRequestPending_1
    AppLanguage.German -> GermanTranslations.tagFeedback_tagWriteRequestPending_1
    AppLanguage.Greek -> GreekTranslations.tagFeedback_tagWriteRequestPending_1
    AppLanguage.Hindi -> HindiTranslations.tagFeedback_tagWriteRequestPending_1
    AppLanguage.Hungarian -> HungarianTranslations.tagFeedback_tagWriteRequestPending_1
    AppLanguage.Indonesian -> IndonesianTranslations.tagFeedback_tagWriteRequestPending_1
    AppLanguage.Italian -> ItalianTranslations.tagFeedback_tagWriteRequestPending_1
    AppLanguage.Japanese -> JapaneseTranslations.tagFeedback_tagWriteRequestPending_1
    AppLanguage.Korean -> KoreanTranslations.tagFeedback_tagWriteRequestPending_1
    AppLanguage.Latvian -> LatvianTranslations.tagFeedback_tagWriteRequestPending_1
    AppLanguage.Lithuanian -> LithuanianTranslations.tagFeedback_tagWriteRequestPending_1
    AppLanguage.Macedonian -> MacedonianTranslations.tagFeedback_tagWriteRequestPending_1
    AppLanguage.Malay -> MalayTranslations.tagFeedback_tagWriteRequestPending_1
    AppLanguage.Norwegian -> NorwegianTranslations.tagFeedback_tagWriteRequestPending_1
    AppLanguage.Polish -> PolishTranslations.tagFeedback_tagWriteRequestPending_1
    AppLanguage.Portuguese -> PortugueseTranslations.tagFeedback_tagWriteRequestPending_1
    AppLanguage.Punjabi -> PunjabiTranslations.tagFeedback_tagWriteRequestPending_1
    AppLanguage.Romanian -> RomanianTranslations.tagFeedback_tagWriteRequestPending_1
    AppLanguage.Russian -> RussianTranslations.tagFeedback_tagWriteRequestPending_1
    AppLanguage.Serbian -> SerbianTranslations.tagFeedback_tagWriteRequestPending_1
    AppLanguage.Slovak -> SlovakTranslations.tagFeedback_tagWriteRequestPending_1
    AppLanguage.Slovenian -> SlovenianTranslations.tagFeedback_tagWriteRequestPending_1
    AppLanguage.Spanish -> SpanishTranslations.tagFeedback_tagWriteRequestPending_1
    AppLanguage.Swedish -> SwedishTranslations.tagFeedback_tagWriteRequestPending_1
    AppLanguage.Thai -> ThaiTranslations.tagFeedback_tagWriteRequestPending_1
    AppLanguage.Ukrainian -> UkrainianTranslations.tagFeedback_tagWriteRequestPending_1
    AppLanguage.Urdu -> UrduTranslations.tagFeedback_tagWriteRequestPending_1
}

private fun tagWriteRequestUnavailable(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.tagFeedback_tagWriteRequestUnavailable_1
    AppLanguage.Bengali -> BengaliTranslations.tagFeedback_tagWriteRequestUnavailable_1
    AppLanguage.Bosnian -> BosnianTranslations.tagFeedback_tagWriteRequestUnavailable_1
    AppLanguage.Bulgarian -> BulgarianTranslations.tagFeedback_tagWriteRequestUnavailable_1
    AppLanguage.Catalan -> CatalanTranslations.tagFeedback_tagWriteRequestUnavailable_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.tagFeedback_tagWriteRequestUnavailable_1
    AppLanguage.Croatian -> CroatianTranslations.tagFeedback_tagWriteRequestUnavailable_1
    AppLanguage.Czech -> CzechTranslations.tagFeedback_tagWriteRequestUnavailable_1
    AppLanguage.Danish -> DanishTranslations.tagFeedback_tagWriteRequestUnavailable_1
    AppLanguage.Dutch -> DutchTranslations.tagFeedback_tagWriteRequestUnavailable_1
    AppLanguage.English -> EnglishTranslations.tagFeedback_tagWriteRequestUnavailable_1
    AppLanguage.Estonian -> EstonianTranslations.tagFeedback_tagWriteRequestUnavailable_1
    AppLanguage.Finnish -> FinnishTranslations.tagFeedback_tagWriteRequestUnavailable_1
    AppLanguage.French -> FrenchTranslations.tagFeedback_tagWriteRequestUnavailable_1
    AppLanguage.German -> GermanTranslations.tagFeedback_tagWriteRequestUnavailable_1
    AppLanguage.Greek -> GreekTranslations.tagFeedback_tagWriteRequestUnavailable_1
    AppLanguage.Hindi -> HindiTranslations.tagFeedback_tagWriteRequestUnavailable_1
    AppLanguage.Hungarian -> HungarianTranslations.tagFeedback_tagWriteRequestUnavailable_1
    AppLanguage.Indonesian -> IndonesianTranslations.tagFeedback_tagWriteRequestUnavailable_1
    AppLanguage.Italian -> ItalianTranslations.tagFeedback_tagWriteRequestUnavailable_1
    AppLanguage.Japanese -> JapaneseTranslations.tagFeedback_tagWriteRequestUnavailable_1
    AppLanguage.Korean -> KoreanTranslations.tagFeedback_tagWriteRequestUnavailable_1
    AppLanguage.Latvian -> LatvianTranslations.tagFeedback_tagWriteRequestUnavailable_1
    AppLanguage.Lithuanian -> LithuanianTranslations.tagFeedback_tagWriteRequestUnavailable_1
    AppLanguage.Macedonian -> MacedonianTranslations.tagFeedback_tagWriteRequestUnavailable_1
    AppLanguage.Malay -> MalayTranslations.tagFeedback_tagWriteRequestUnavailable_1
    AppLanguage.Norwegian -> NorwegianTranslations.tagFeedback_tagWriteRequestUnavailable_1
    AppLanguage.Polish -> PolishTranslations.tagFeedback_tagWriteRequestUnavailable_1
    AppLanguage.Portuguese -> PortugueseTranslations.tagFeedback_tagWriteRequestUnavailable_1
    AppLanguage.Punjabi -> PunjabiTranslations.tagFeedback_tagWriteRequestUnavailable_1
    AppLanguage.Romanian -> RomanianTranslations.tagFeedback_tagWriteRequestUnavailable_1
    AppLanguage.Russian -> RussianTranslations.tagFeedback_tagWriteRequestUnavailable_1
    AppLanguage.Serbian -> SerbianTranslations.tagFeedback_tagWriteRequestUnavailable_1
    AppLanguage.Slovak -> SlovakTranslations.tagFeedback_tagWriteRequestUnavailable_1
    AppLanguage.Slovenian -> SlovenianTranslations.tagFeedback_tagWriteRequestUnavailable_1
    AppLanguage.Spanish -> SpanishTranslations.tagFeedback_tagWriteRequestUnavailable_1
    AppLanguage.Swedish -> SwedishTranslations.tagFeedback_tagWriteRequestUnavailable_1
    AppLanguage.Thai -> ThaiTranslations.tagFeedback_tagWriteRequestUnavailable_1
    AppLanguage.Ukrainian -> UkrainianTranslations.tagFeedback_tagWriteRequestUnavailable_1
    AppLanguage.Urdu -> UrduTranslations.tagFeedback_tagWriteRequestUnavailable_1
}

private fun tagWriteAccessNotGranted(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.tagFeedback_tagWriteAccessNotGranted_1
    AppLanguage.Bengali -> BengaliTranslations.tagFeedback_tagWriteAccessNotGranted_1
    AppLanguage.Bosnian -> BosnianTranslations.tagFeedback_tagWriteAccessNotGranted_1
    AppLanguage.Bulgarian -> BulgarianTranslations.tagFeedback_tagWriteAccessNotGranted_1
    AppLanguage.Catalan -> CatalanTranslations.tagFeedback_tagWriteAccessNotGranted_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.tagFeedback_tagWriteAccessNotGranted_1
    AppLanguage.Croatian -> CroatianTranslations.tagFeedback_tagWriteAccessNotGranted_1
    AppLanguage.Czech -> CzechTranslations.tagFeedback_tagWriteAccessNotGranted_1
    AppLanguage.Danish -> DanishTranslations.tagFeedback_tagWriteAccessNotGranted_1
    AppLanguage.Dutch -> DutchTranslations.tagFeedback_tagWriteAccessNotGranted_1
    AppLanguage.English -> EnglishTranslations.tagFeedback_tagWriteAccessNotGranted_1
    AppLanguage.Estonian -> EstonianTranslations.tagFeedback_tagWriteAccessNotGranted_1
    AppLanguage.Finnish -> FinnishTranslations.tagFeedback_tagWriteAccessNotGranted_1
    AppLanguage.French -> FrenchTranslations.tagFeedback_tagWriteAccessNotGranted_1
    AppLanguage.German -> GermanTranslations.tagFeedback_tagWriteAccessNotGranted_1
    AppLanguage.Greek -> GreekTranslations.tagFeedback_tagWriteAccessNotGranted_1
    AppLanguage.Hindi -> HindiTranslations.tagFeedback_tagWriteAccessNotGranted_1
    AppLanguage.Hungarian -> HungarianTranslations.tagFeedback_tagWriteAccessNotGranted_1
    AppLanguage.Indonesian -> IndonesianTranslations.tagFeedback_tagWriteAccessNotGranted_1
    AppLanguage.Italian -> ItalianTranslations.tagFeedback_tagWriteAccessNotGranted_1
    AppLanguage.Japanese -> JapaneseTranslations.tagFeedback_tagWriteAccessNotGranted_1
    AppLanguage.Korean -> KoreanTranslations.tagFeedback_tagWriteAccessNotGranted_1
    AppLanguage.Latvian -> LatvianTranslations.tagFeedback_tagWriteAccessNotGranted_1
    AppLanguage.Lithuanian -> LithuanianTranslations.tagFeedback_tagWriteAccessNotGranted_1
    AppLanguage.Macedonian -> MacedonianTranslations.tagFeedback_tagWriteAccessNotGranted_1
    AppLanguage.Malay -> MalayTranslations.tagFeedback_tagWriteAccessNotGranted_1
    AppLanguage.Norwegian -> NorwegianTranslations.tagFeedback_tagWriteAccessNotGranted_1
    AppLanguage.Polish -> PolishTranslations.tagFeedback_tagWriteAccessNotGranted_1
    AppLanguage.Portuguese -> PortugueseTranslations.tagFeedback_tagWriteAccessNotGranted_1
    AppLanguage.Punjabi -> PunjabiTranslations.tagFeedback_tagWriteAccessNotGranted_1
    AppLanguage.Romanian -> RomanianTranslations.tagFeedback_tagWriteAccessNotGranted_1
    AppLanguage.Russian -> RussianTranslations.tagFeedback_tagWriteAccessNotGranted_1
    AppLanguage.Serbian -> SerbianTranslations.tagFeedback_tagWriteAccessNotGranted_1
    AppLanguage.Slovak -> SlovakTranslations.tagFeedback_tagWriteAccessNotGranted_1
    AppLanguage.Slovenian -> SlovenianTranslations.tagFeedback_tagWriteAccessNotGranted_1
    AppLanguage.Spanish -> SpanishTranslations.tagFeedback_tagWriteAccessNotGranted_1
    AppLanguage.Swedish -> SwedishTranslations.tagFeedback_tagWriteAccessNotGranted_1
    AppLanguage.Thai -> ThaiTranslations.tagFeedback_tagWriteAccessNotGranted_1
    AppLanguage.Ukrainian -> UkrainianTranslations.tagFeedback_tagWriteAccessNotGranted_1
    AppLanguage.Urdu -> UrduTranslations.tagFeedback_tagWriteAccessNotGranted_1
}

private fun tagSaveAwaitingAccess(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.tagFeedback_tagSaveAwaitingAccess_1
    AppLanguage.Bengali -> BengaliTranslations.tagFeedback_tagSaveAwaitingAccess_1
    AppLanguage.Bosnian -> BosnianTranslations.tagFeedback_tagSaveAwaitingAccess_1
    AppLanguage.Bulgarian -> BulgarianTranslations.tagFeedback_tagSaveAwaitingAccess_1
    AppLanguage.Catalan -> CatalanTranslations.tagFeedback_tagSaveAwaitingAccess_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.tagFeedback_tagSaveAwaitingAccess_1
    AppLanguage.Croatian -> CroatianTranslations.tagFeedback_tagSaveAwaitingAccess_1
    AppLanguage.Czech -> CzechTranslations.tagFeedback_tagSaveAwaitingAccess_1
    AppLanguage.Danish -> DanishTranslations.tagFeedback_tagSaveAwaitingAccess_1
    AppLanguage.Dutch -> DutchTranslations.tagFeedback_tagSaveAwaitingAccess_1
    AppLanguage.English -> EnglishTranslations.tagFeedback_tagSaveAwaitingAccess_1
    AppLanguage.Estonian -> EstonianTranslations.tagFeedback_tagSaveAwaitingAccess_1
    AppLanguage.Finnish -> FinnishTranslations.tagFeedback_tagSaveAwaitingAccess_1
    AppLanguage.French -> FrenchTranslations.tagFeedback_tagSaveAwaitingAccess_1
    AppLanguage.German -> GermanTranslations.tagFeedback_tagSaveAwaitingAccess_1
    AppLanguage.Greek -> GreekTranslations.tagFeedback_tagSaveAwaitingAccess_1
    AppLanguage.Hindi -> HindiTranslations.tagFeedback_tagSaveAwaitingAccess_1
    AppLanguage.Hungarian -> HungarianTranslations.tagFeedback_tagSaveAwaitingAccess_1
    AppLanguage.Indonesian -> IndonesianTranslations.tagFeedback_tagSaveAwaitingAccess_1
    AppLanguage.Italian -> ItalianTranslations.tagFeedback_tagSaveAwaitingAccess_1
    AppLanguage.Japanese -> JapaneseTranslations.tagFeedback_tagSaveAwaitingAccess_1
    AppLanguage.Korean -> KoreanTranslations.tagFeedback_tagSaveAwaitingAccess_1
    AppLanguage.Latvian -> LatvianTranslations.tagFeedback_tagSaveAwaitingAccess_1
    AppLanguage.Lithuanian -> LithuanianTranslations.tagFeedback_tagSaveAwaitingAccess_1
    AppLanguage.Macedonian -> MacedonianTranslations.tagFeedback_tagSaveAwaitingAccess_1
    AppLanguage.Malay -> MalayTranslations.tagFeedback_tagSaveAwaitingAccess_1
    AppLanguage.Norwegian -> NorwegianTranslations.tagFeedback_tagSaveAwaitingAccess_1
    AppLanguage.Polish -> PolishTranslations.tagFeedback_tagSaveAwaitingAccess_1
    AppLanguage.Portuguese -> PortugueseTranslations.tagFeedback_tagSaveAwaitingAccess_1
    AppLanguage.Punjabi -> PunjabiTranslations.tagFeedback_tagSaveAwaitingAccess_1
    AppLanguage.Romanian -> RomanianTranslations.tagFeedback_tagSaveAwaitingAccess_1
    AppLanguage.Russian -> RussianTranslations.tagFeedback_tagSaveAwaitingAccess_1
    AppLanguage.Serbian -> SerbianTranslations.tagFeedback_tagSaveAwaitingAccess_1
    AppLanguage.Slovak -> SlovakTranslations.tagFeedback_tagSaveAwaitingAccess_1
    AppLanguage.Slovenian -> SlovenianTranslations.tagFeedback_tagSaveAwaitingAccess_1
    AppLanguage.Spanish -> SpanishTranslations.tagFeedback_tagSaveAwaitingAccess_1
    AppLanguage.Swedish -> SwedishTranslations.tagFeedback_tagSaveAwaitingAccess_1
    AppLanguage.Thai -> ThaiTranslations.tagFeedback_tagSaveAwaitingAccess_1
    AppLanguage.Ukrainian -> UkrainianTranslations.tagFeedback_tagSaveAwaitingAccess_1
    AppLanguage.Urdu -> UrduTranslations.tagFeedback_tagSaveAwaitingAccess_1
}

internal fun localizedTagSaveFailure(failure: TagEditFailureUi, language: AppLanguage): String {
    if (failure.cause == TagEditFailureCause.InvalidInput(TagEditValidationFailure.InvalidTrackSelection)) return invalidTrackSelectionError(language)
    if (failure.cause == TagEditFailureCause.InvalidInput(TagEditValidationFailure.TextTooLong)) return tagTextTooLongError(language)
    if (failure.cause == TagEditFailureCause.InvalidInput(TagEditValidationFailure.ArtworkTooLarge)) return artworkTooLargeError(language)
    if (failure.cause == TagEditFailureCause.InvalidInput(TagEditValidationFailure.InvalidTrackNumber)) {
        return if (language == AppLanguage.English) "Track and disc numbers must be between 1 and 9999" else trackNumbersError(language)
    }
    if (failure.reason == "This song is not ready for safe tag editing.") return tagNotReadyError(language)
    if (failure.cause == TagEditFailureCause.CannotOpenOutput && failure.reason == "Unable to save tags to the song file.") return tagOutputWriteError(language)
    if (failure.cause == TagEditFailureCause.CannotOpenInput && failure.reason == "Unable to read this song for tag editing.") return tagSourceReadError(language)
    if (failure.cause == TagEditFailureCause.PersistedVerificationFailed) return tagVerificationAfterSaveError(language)
    if (failure.cause == TagEditFailureCause.TempVerificationFailed) return tagVerificationBeforeSaveError(language)
    if (failure.cause == TagEditFailureCause.TempWriteFailed) return tagTemporaryWriteFailure(language)
    if (failure.cause == TagEditFailureCause.PermissionDeniedAfterGrant) return tagPermissionDeniedAfterGrant(language)
    if (failure.cause == TagEditFailureCause.PermissionRequired) return tagWritePermissionRequired(language)
    if (failure.cause == TagEditFailureCause.RollbackFailed) return tagRollbackFailure(language)
    if (failure.cause == TagEditFailureCause.CannotReadArtwork) return cannotReadArtworkError(language)
    if (failure.cause == TagEditFailureCause.UnsupportedArtworkFormat) return unsupportedArtworkError(language)
    if (failure.cause != TagEditFailureCause.UnsupportedFormat) return localizedTagSaveStatus(failure.reason, 0, language)
    return when (language) {
        AppLanguage.Albanian -> AlbanianTranslations.tagFeedback_localizedTagSaveFailure_1
        AppLanguage.Bengali -> BengaliTranslations.tagFeedback_localizedTagSaveFailure_1
        AppLanguage.Bosnian -> BosnianTranslations.tagFeedback_localizedTagSaveFailure_1
        AppLanguage.Bulgarian -> BulgarianTranslations.tagFeedback_localizedTagSaveFailure_1
        AppLanguage.Catalan -> CatalanTranslations.tagFeedback_localizedTagSaveFailure_1
        AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.tagFeedback_localizedTagSaveFailure_1
        AppLanguage.Croatian -> CroatianTranslations.tagFeedback_localizedTagSaveFailure_1
        AppLanguage.Czech -> CzechTranslations.tagFeedback_localizedTagSaveFailure_1
        AppLanguage.Danish -> DanishTranslations.tagFeedback_localizedTagSaveFailure_1
        AppLanguage.Dutch -> DutchTranslations.tagFeedback_localizedTagSaveFailure_1
        AppLanguage.English -> EnglishTranslations.tagFeedback_localizedTagSaveFailure_1
        AppLanguage.Estonian -> EstonianTranslations.tagFeedback_localizedTagSaveFailure_1
        AppLanguage.Finnish -> FinnishTranslations.tagFeedback_localizedTagSaveFailure_1
        AppLanguage.French -> FrenchTranslations.tagFeedback_localizedTagSaveFailure_1
        AppLanguage.German -> GermanTranslations.tagFeedback_localizedTagSaveFailure_1
        AppLanguage.Greek -> GreekTranslations.tagFeedback_localizedTagSaveFailure_1
        AppLanguage.Hindi -> HindiTranslations.tagFeedback_localizedTagSaveFailure_1
        AppLanguage.Hungarian -> HungarianTranslations.tagFeedback_localizedTagSaveFailure_1
        AppLanguage.Indonesian -> IndonesianTranslations.tagFeedback_localizedTagSaveFailure_1
        AppLanguage.Italian -> ItalianTranslations.tagFeedback_localizedTagSaveFailure_1
        AppLanguage.Japanese -> JapaneseTranslations.tagFeedback_localizedTagSaveFailure_1
        AppLanguage.Korean -> KoreanTranslations.tagFeedback_localizedTagSaveFailure_1
        AppLanguage.Latvian -> LatvianTranslations.tagFeedback_localizedTagSaveFailure_1
        AppLanguage.Lithuanian -> LithuanianTranslations.tagFeedback_localizedTagSaveFailure_1
        AppLanguage.Macedonian -> MacedonianTranslations.tagFeedback_localizedTagSaveFailure_1
        AppLanguage.Malay -> MalayTranslations.tagFeedback_localizedTagSaveFailure_1
        AppLanguage.Norwegian -> NorwegianTranslations.tagFeedback_localizedTagSaveFailure_1
        AppLanguage.Polish -> PolishTranslations.tagFeedback_localizedTagSaveFailure_1
        AppLanguage.Portuguese -> PortugueseTranslations.tagFeedback_localizedTagSaveFailure_1
        AppLanguage.Punjabi -> PunjabiTranslations.tagFeedback_localizedTagSaveFailure_1
        AppLanguage.Romanian -> RomanianTranslations.tagFeedback_localizedTagSaveFailure_1
        AppLanguage.Russian -> RussianTranslations.tagFeedback_localizedTagSaveFailure_1
        AppLanguage.Serbian -> SerbianTranslations.tagFeedback_localizedTagSaveFailure_1
        AppLanguage.Slovak -> SlovakTranslations.tagFeedback_localizedTagSaveFailure_1
        AppLanguage.Slovenian -> SlovenianTranslations.tagFeedback_localizedTagSaveFailure_1
        AppLanguage.Spanish -> SpanishTranslations.tagFeedback_localizedTagSaveFailure_1
        AppLanguage.Swedish -> SwedishTranslations.tagFeedback_localizedTagSaveFailure_1
        AppLanguage.Thai -> ThaiTranslations.tagFeedback_localizedTagSaveFailure_1
        AppLanguage.Ukrainian -> UkrainianTranslations.tagFeedback_localizedTagSaveFailure_1
        AppLanguage.Urdu -> UrduTranslations.tagFeedback_localizedTagSaveFailure_1
    }
}

private fun invalidTrackSelectionError(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.tagFeedback_invalidTrackSelectionError_1
    AppLanguage.Bengali -> BengaliTranslations.tagFeedback_invalidTrackSelectionError_1
    AppLanguage.Bosnian -> BosnianTranslations.tagFeedback_invalidTrackSelectionError_1
    AppLanguage.Bulgarian -> BulgarianTranslations.tagFeedback_invalidTrackSelectionError_1
    AppLanguage.Catalan -> CatalanTranslations.tagFeedback_invalidTrackSelectionError_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.tagFeedback_invalidTrackSelectionError_1
    AppLanguage.Croatian -> CroatianTranslations.tagFeedback_invalidTrackSelectionError_1
    AppLanguage.Czech -> CzechTranslations.tagFeedback_invalidTrackSelectionError_1
    AppLanguage.Danish -> DanishTranslations.tagFeedback_invalidTrackSelectionError_1
    AppLanguage.Dutch -> DutchTranslations.tagFeedback_invalidTrackSelectionError_1
    AppLanguage.English -> EnglishTranslations.tagFeedback_invalidTrackSelectionError_1
    AppLanguage.Estonian -> EstonianTranslations.tagFeedback_invalidTrackSelectionError_1
    AppLanguage.Finnish -> FinnishTranslations.tagFeedback_invalidTrackSelectionError_1
    AppLanguage.French -> FrenchTranslations.tagFeedback_invalidTrackSelectionError_1
    AppLanguage.German -> GermanTranslations.tagFeedback_invalidTrackSelectionError_1
    AppLanguage.Greek -> GreekTranslations.tagFeedback_invalidTrackSelectionError_1
    AppLanguage.Hindi -> HindiTranslations.tagFeedback_invalidTrackSelectionError_1
    AppLanguage.Hungarian -> HungarianTranslations.tagFeedback_invalidTrackSelectionError_1
    AppLanguage.Indonesian -> IndonesianTranslations.tagFeedback_invalidTrackSelectionError_1
    AppLanguage.Italian -> ItalianTranslations.tagFeedback_invalidTrackSelectionError_1
    AppLanguage.Japanese -> JapaneseTranslations.tagFeedback_invalidTrackSelectionError_1
    AppLanguage.Korean -> KoreanTranslations.tagFeedback_invalidTrackSelectionError_1
    AppLanguage.Latvian -> LatvianTranslations.tagFeedback_invalidTrackSelectionError_1
    AppLanguage.Lithuanian -> LithuanianTranslations.tagFeedback_invalidTrackSelectionError_1
    AppLanguage.Macedonian -> MacedonianTranslations.tagFeedback_invalidTrackSelectionError_1
    AppLanguage.Malay -> MalayTranslations.tagFeedback_invalidTrackSelectionError_1
    AppLanguage.Norwegian -> NorwegianTranslations.tagFeedback_invalidTrackSelectionError_1
    AppLanguage.Polish -> PolishTranslations.tagFeedback_invalidTrackSelectionError_1
    AppLanguage.Portuguese -> PortugueseTranslations.tagFeedback_invalidTrackSelectionError_1
    AppLanguage.Punjabi -> PunjabiTranslations.tagFeedback_invalidTrackSelectionError_1
    AppLanguage.Romanian -> RomanianTranslations.tagFeedback_invalidTrackSelectionError_1
    AppLanguage.Russian -> RussianTranslations.tagFeedback_invalidTrackSelectionError_1
    AppLanguage.Serbian -> SerbianTranslations.tagFeedback_invalidTrackSelectionError_1
    AppLanguage.Slovak -> SlovakTranslations.tagFeedback_invalidTrackSelectionError_1
    AppLanguage.Slovenian -> SlovenianTranslations.tagFeedback_invalidTrackSelectionError_1
    AppLanguage.Spanish -> SpanishTranslations.tagFeedback_invalidTrackSelectionError_1
    AppLanguage.Swedish -> SwedishTranslations.tagFeedback_invalidTrackSelectionError_1
    AppLanguage.Thai -> ThaiTranslations.tagFeedback_invalidTrackSelectionError_1
    AppLanguage.Ukrainian -> UkrainianTranslations.tagFeedback_invalidTrackSelectionError_1
    AppLanguage.Urdu -> UrduTranslations.tagFeedback_invalidTrackSelectionError_1
}

private fun tagTextTooLongError(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.tagFeedback_tagTextTooLongError_1
    AppLanguage.Bengali -> BengaliTranslations.tagFeedback_tagTextTooLongError_1
    AppLanguage.Bosnian -> BosnianTranslations.tagFeedback_tagTextTooLongError_1
    AppLanguage.Bulgarian -> BulgarianTranslations.tagFeedback_tagTextTooLongError_1
    AppLanguage.Catalan -> CatalanTranslations.tagFeedback_tagTextTooLongError_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.tagFeedback_tagTextTooLongError_1
    AppLanguage.Croatian -> CroatianTranslations.tagFeedback_tagTextTooLongError_1
    AppLanguage.Czech -> CzechTranslations.tagFeedback_tagTextTooLongError_1
    AppLanguage.Danish -> DanishTranslations.tagFeedback_tagTextTooLongError_1
    AppLanguage.Dutch -> DutchTranslations.tagFeedback_tagTextTooLongError_1
    AppLanguage.English -> EnglishTranslations.tagFeedback_tagTextTooLongError_1
    AppLanguage.Estonian -> EstonianTranslations.tagFeedback_tagTextTooLongError_1
    AppLanguage.Finnish -> FinnishTranslations.tagFeedback_tagTextTooLongError_1
    AppLanguage.French -> FrenchTranslations.tagFeedback_tagTextTooLongError_1
    AppLanguage.German -> GermanTranslations.tagFeedback_tagTextTooLongError_1
    AppLanguage.Greek -> GreekTranslations.tagFeedback_tagTextTooLongError_1
    AppLanguage.Hindi -> HindiTranslations.tagFeedback_tagTextTooLongError_1
    AppLanguage.Hungarian -> HungarianTranslations.tagFeedback_tagTextTooLongError_1
    AppLanguage.Indonesian -> IndonesianTranslations.tagFeedback_tagTextTooLongError_1
    AppLanguage.Italian -> ItalianTranslations.tagFeedback_tagTextTooLongError_1
    AppLanguage.Japanese -> JapaneseTranslations.tagFeedback_tagTextTooLongError_1
    AppLanguage.Korean -> KoreanTranslations.tagFeedback_tagTextTooLongError_1
    AppLanguage.Latvian -> LatvianTranslations.tagFeedback_tagTextTooLongError_1
    AppLanguage.Lithuanian -> LithuanianTranslations.tagFeedback_tagTextTooLongError_1
    AppLanguage.Macedonian -> MacedonianTranslations.tagFeedback_tagTextTooLongError_1
    AppLanguage.Malay -> MalayTranslations.tagFeedback_tagTextTooLongError_1
    AppLanguage.Norwegian -> NorwegianTranslations.tagFeedback_tagTextTooLongError_1
    AppLanguage.Polish -> PolishTranslations.tagFeedback_tagTextTooLongError_1
    AppLanguage.Portuguese -> PortugueseTranslations.tagFeedback_tagTextTooLongError_1
    AppLanguage.Punjabi -> PunjabiTranslations.tagFeedback_tagTextTooLongError_1
    AppLanguage.Romanian -> RomanianTranslations.tagFeedback_tagTextTooLongError_1
    AppLanguage.Russian -> RussianTranslations.tagFeedback_tagTextTooLongError_1
    AppLanguage.Serbian -> SerbianTranslations.tagFeedback_tagTextTooLongError_1
    AppLanguage.Slovak -> SlovakTranslations.tagFeedback_tagTextTooLongError_1
    AppLanguage.Slovenian -> SlovenianTranslations.tagFeedback_tagTextTooLongError_1
    AppLanguage.Spanish -> SpanishTranslations.tagFeedback_tagTextTooLongError_1
    AppLanguage.Swedish -> SwedishTranslations.tagFeedback_tagTextTooLongError_1
    AppLanguage.Thai -> ThaiTranslations.tagFeedback_tagTextTooLongError_1
    AppLanguage.Ukrainian -> UkrainianTranslations.tagFeedback_tagTextTooLongError_1
    AppLanguage.Urdu -> UrduTranslations.tagFeedback_tagTextTooLongError_1
}

private fun artworkTooLargeError(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.tagFeedback_artworkTooLargeError_1
    AppLanguage.Bengali -> BengaliTranslations.tagFeedback_artworkTooLargeError_1
    AppLanguage.Bosnian -> BosnianTranslations.tagFeedback_artworkTooLargeError_1
    AppLanguage.Bulgarian -> BulgarianTranslations.tagFeedback_artworkTooLargeError_1
    AppLanguage.Catalan -> CatalanTranslations.tagFeedback_artworkTooLargeError_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.tagFeedback_artworkTooLargeError_1
    AppLanguage.Croatian -> CroatianTranslations.tagFeedback_artworkTooLargeError_1
    AppLanguage.Czech -> CzechTranslations.tagFeedback_artworkTooLargeError_1
    AppLanguage.Danish -> DanishTranslations.tagFeedback_artworkTooLargeError_1
    AppLanguage.Dutch -> DutchTranslations.tagFeedback_artworkTooLargeError_1
    AppLanguage.English -> EnglishTranslations.tagFeedback_artworkTooLargeError_1
    AppLanguage.Estonian -> EstonianTranslations.tagFeedback_artworkTooLargeError_1
    AppLanguage.Finnish -> FinnishTranslations.tagFeedback_artworkTooLargeError_1
    AppLanguage.French -> FrenchTranslations.tagFeedback_artworkTooLargeError_1
    AppLanguage.German -> GermanTranslations.tagFeedback_artworkTooLargeError_1
    AppLanguage.Greek -> GreekTranslations.tagFeedback_artworkTooLargeError_1
    AppLanguage.Hindi -> HindiTranslations.tagFeedback_artworkTooLargeError_1
    AppLanguage.Hungarian -> HungarianTranslations.tagFeedback_artworkTooLargeError_1
    AppLanguage.Indonesian -> IndonesianTranslations.tagFeedback_artworkTooLargeError_1
    AppLanguage.Italian -> ItalianTranslations.tagFeedback_artworkTooLargeError_1
    AppLanguage.Japanese -> JapaneseTranslations.tagFeedback_artworkTooLargeError_1
    AppLanguage.Korean -> KoreanTranslations.tagFeedback_artworkTooLargeError_1
    AppLanguage.Latvian -> LatvianTranslations.tagFeedback_artworkTooLargeError_1
    AppLanguage.Lithuanian -> LithuanianTranslations.tagFeedback_artworkTooLargeError_1
    AppLanguage.Macedonian -> MacedonianTranslations.tagFeedback_artworkTooLargeError_1
    AppLanguage.Malay -> MalayTranslations.tagFeedback_artworkTooLargeError_1
    AppLanguage.Norwegian -> NorwegianTranslations.tagFeedback_artworkTooLargeError_1
    AppLanguage.Polish -> PolishTranslations.tagFeedback_artworkTooLargeError_1
    AppLanguage.Portuguese -> PortugueseTranslations.tagFeedback_artworkTooLargeError_1
    AppLanguage.Punjabi -> PunjabiTranslations.tagFeedback_artworkTooLargeError_1
    AppLanguage.Romanian -> RomanianTranslations.tagFeedback_artworkTooLargeError_1
    AppLanguage.Russian -> RussianTranslations.tagFeedback_artworkTooLargeError_1
    AppLanguage.Serbian -> SerbianTranslations.tagFeedback_artworkTooLargeError_1
    AppLanguage.Slovak -> SlovakTranslations.tagFeedback_artworkTooLargeError_1
    AppLanguage.Slovenian -> SlovenianTranslations.tagFeedback_artworkTooLargeError_1
    AppLanguage.Spanish -> SpanishTranslations.tagFeedback_artworkTooLargeError_1
    AppLanguage.Swedish -> SwedishTranslations.tagFeedback_artworkTooLargeError_1
    AppLanguage.Thai -> ThaiTranslations.tagFeedback_artworkTooLargeError_1
    AppLanguage.Ukrainian -> UkrainianTranslations.tagFeedback_artworkTooLargeError_1
    AppLanguage.Urdu -> UrduTranslations.tagFeedback_artworkTooLargeError_1
}

private fun tagNotReadyError(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.tagFeedback_tagNotReadyError_1
    AppLanguage.Bengali -> BengaliTranslations.tagFeedback_tagNotReadyError_1
    AppLanguage.Bosnian -> BosnianTranslations.tagFeedback_tagNotReadyError_1
    AppLanguage.Bulgarian -> BulgarianTranslations.tagFeedback_tagNotReadyError_1
    AppLanguage.Catalan -> CatalanTranslations.tagFeedback_tagNotReadyError_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.tagFeedback_tagNotReadyError_1
    AppLanguage.Croatian -> CroatianTranslations.tagFeedback_tagNotReadyError_1
    AppLanguage.Czech -> CzechTranslations.tagFeedback_tagNotReadyError_1
    AppLanguage.Danish -> DanishTranslations.tagFeedback_tagNotReadyError_1
    AppLanguage.Dutch -> DutchTranslations.tagFeedback_tagNotReadyError_1
    AppLanguage.English -> EnglishTranslations.tagFeedback_tagNotReadyError_1
    AppLanguage.Estonian -> EstonianTranslations.tagFeedback_tagNotReadyError_1
    AppLanguage.Finnish -> FinnishTranslations.tagFeedback_tagNotReadyError_1
    AppLanguage.French -> FrenchTranslations.tagFeedback_tagNotReadyError_1
    AppLanguage.German -> GermanTranslations.tagFeedback_tagNotReadyError_1
    AppLanguage.Greek -> GreekTranslations.tagFeedback_tagNotReadyError_1
    AppLanguage.Hindi -> HindiTranslations.tagFeedback_tagNotReadyError_1
    AppLanguage.Hungarian -> HungarianTranslations.tagFeedback_tagNotReadyError_1
    AppLanguage.Indonesian -> IndonesianTranslations.tagFeedback_tagNotReadyError_1
    AppLanguage.Italian -> ItalianTranslations.tagFeedback_tagNotReadyError_1
    AppLanguage.Japanese -> JapaneseTranslations.tagFeedback_tagNotReadyError_1
    AppLanguage.Korean -> KoreanTranslations.tagFeedback_tagNotReadyError_1
    AppLanguage.Latvian -> LatvianTranslations.tagFeedback_tagNotReadyError_1
    AppLanguage.Lithuanian -> LithuanianTranslations.tagFeedback_tagNotReadyError_1
    AppLanguage.Macedonian -> MacedonianTranslations.tagFeedback_tagNotReadyError_1
    AppLanguage.Malay -> MalayTranslations.tagFeedback_tagNotReadyError_1
    AppLanguage.Norwegian -> NorwegianTranslations.tagFeedback_tagNotReadyError_1
    AppLanguage.Polish -> PolishTranslations.tagFeedback_tagNotReadyError_1
    AppLanguage.Portuguese -> PortugueseTranslations.tagFeedback_tagNotReadyError_1
    AppLanguage.Punjabi -> PunjabiTranslations.tagFeedback_tagNotReadyError_1
    AppLanguage.Romanian -> RomanianTranslations.tagFeedback_tagNotReadyError_1
    AppLanguage.Russian -> RussianTranslations.tagFeedback_tagNotReadyError_1
    AppLanguage.Serbian -> SerbianTranslations.tagFeedback_tagNotReadyError_1
    AppLanguage.Slovak -> SlovakTranslations.tagFeedback_tagNotReadyError_1
    AppLanguage.Slovenian -> SlovenianTranslations.tagFeedback_tagNotReadyError_1
    AppLanguage.Spanish -> SpanishTranslations.tagFeedback_tagNotReadyError_1
    AppLanguage.Swedish -> SwedishTranslations.tagFeedback_tagNotReadyError_1
    AppLanguage.Thai -> ThaiTranslations.tagFeedback_tagNotReadyError_1
    AppLanguage.Ukrainian -> UkrainianTranslations.tagFeedback_tagNotReadyError_1
    AppLanguage.Urdu -> UrduTranslations.tagFeedback_tagNotReadyError_1
}

private fun tagOutputWriteError(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.tagFeedback_tagOutputWriteError_1
    AppLanguage.Bengali -> BengaliTranslations.tagFeedback_tagOutputWriteError_1
    AppLanguage.Bosnian -> BosnianTranslations.tagFeedback_tagOutputWriteError_1
    AppLanguage.Bulgarian -> BulgarianTranslations.tagFeedback_tagOutputWriteError_1
    AppLanguage.Catalan -> CatalanTranslations.tagFeedback_tagOutputWriteError_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.tagFeedback_tagOutputWriteError_1
    AppLanguage.Croatian -> CroatianTranslations.tagFeedback_tagOutputWriteError_1
    AppLanguage.Czech -> CzechTranslations.tagFeedback_tagOutputWriteError_1
    AppLanguage.Danish -> DanishTranslations.tagFeedback_tagOutputWriteError_1
    AppLanguage.Dutch -> DutchTranslations.tagFeedback_tagOutputWriteError_1
    AppLanguage.English -> EnglishTranslations.tagFeedback_tagOutputWriteError_1
    AppLanguage.Estonian -> EstonianTranslations.tagFeedback_tagOutputWriteError_1
    AppLanguage.Finnish -> FinnishTranslations.tagFeedback_tagOutputWriteError_1
    AppLanguage.French -> FrenchTranslations.tagFeedback_tagOutputWriteError_1
    AppLanguage.German -> GermanTranslations.tagFeedback_tagOutputWriteError_1
    AppLanguage.Greek -> GreekTranslations.tagFeedback_tagOutputWriteError_1
    AppLanguage.Hindi -> HindiTranslations.tagFeedback_tagOutputWriteError_1
    AppLanguage.Hungarian -> HungarianTranslations.tagFeedback_tagOutputWriteError_1
    AppLanguage.Indonesian -> IndonesianTranslations.tagFeedback_tagOutputWriteError_1
    AppLanguage.Italian -> ItalianTranslations.tagFeedback_tagOutputWriteError_1
    AppLanguage.Japanese -> JapaneseTranslations.tagFeedback_tagOutputWriteError_1
    AppLanguage.Korean -> KoreanTranslations.tagFeedback_tagOutputWriteError_1
    AppLanguage.Latvian -> LatvianTranslations.tagFeedback_tagOutputWriteError_1
    AppLanguage.Lithuanian -> LithuanianTranslations.tagFeedback_tagOutputWriteError_1
    AppLanguage.Macedonian -> MacedonianTranslations.tagFeedback_tagOutputWriteError_1
    AppLanguage.Malay -> MalayTranslations.tagFeedback_tagOutputWriteError_1
    AppLanguage.Norwegian -> NorwegianTranslations.tagFeedback_tagOutputWriteError_1
    AppLanguage.Polish -> PolishTranslations.tagFeedback_tagOutputWriteError_1
    AppLanguage.Portuguese -> PortugueseTranslations.tagFeedback_tagOutputWriteError_1
    AppLanguage.Punjabi -> PunjabiTranslations.tagFeedback_tagOutputWriteError_1
    AppLanguage.Romanian -> RomanianTranslations.tagFeedback_tagOutputWriteError_1
    AppLanguage.Russian -> RussianTranslations.tagFeedback_tagOutputWriteError_1
    AppLanguage.Serbian -> SerbianTranslations.tagFeedback_tagOutputWriteError_1
    AppLanguage.Slovak -> SlovakTranslations.tagFeedback_tagOutputWriteError_1
    AppLanguage.Slovenian -> SlovenianTranslations.tagFeedback_tagOutputWriteError_1
    AppLanguage.Spanish -> SpanishTranslations.tagFeedback_tagOutputWriteError_1
    AppLanguage.Swedish -> SwedishTranslations.tagFeedback_tagOutputWriteError_1
    AppLanguage.Thai -> ThaiTranslations.tagFeedback_tagOutputWriteError_1
    AppLanguage.Ukrainian -> UkrainianTranslations.tagFeedback_tagOutputWriteError_1
    AppLanguage.Urdu -> UrduTranslations.tagFeedback_tagOutputWriteError_1
}

private fun tagSourceReadError(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.tagFeedback_tagSourceReadError_1
    AppLanguage.Bengali -> BengaliTranslations.tagFeedback_tagSourceReadError_1
    AppLanguage.Bosnian -> BosnianTranslations.tagFeedback_tagSourceReadError_1
    AppLanguage.Bulgarian -> BulgarianTranslations.tagFeedback_tagSourceReadError_1
    AppLanguage.Catalan -> CatalanTranslations.tagFeedback_tagSourceReadError_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.tagFeedback_tagSourceReadError_1
    AppLanguage.Croatian -> CroatianTranslations.tagFeedback_tagSourceReadError_1
    AppLanguage.Czech -> CzechTranslations.tagFeedback_tagSourceReadError_1
    AppLanguage.Danish -> DanishTranslations.tagFeedback_tagSourceReadError_1
    AppLanguage.Dutch -> DutchTranslations.tagFeedback_tagSourceReadError_1
    AppLanguage.English -> EnglishTranslations.tagFeedback_tagSourceReadError_1
    AppLanguage.Estonian -> EstonianTranslations.tagFeedback_tagSourceReadError_1
    AppLanguage.Finnish -> FinnishTranslations.tagFeedback_tagSourceReadError_1
    AppLanguage.French -> FrenchTranslations.tagFeedback_tagSourceReadError_1
    AppLanguage.German -> GermanTranslations.tagFeedback_tagSourceReadError_1
    AppLanguage.Greek -> GreekTranslations.tagFeedback_tagSourceReadError_1
    AppLanguage.Hindi -> HindiTranslations.tagFeedback_tagSourceReadError_1
    AppLanguage.Hungarian -> HungarianTranslations.tagFeedback_tagSourceReadError_1
    AppLanguage.Indonesian -> IndonesianTranslations.tagFeedback_tagSourceReadError_1
    AppLanguage.Italian -> ItalianTranslations.tagFeedback_tagSourceReadError_1
    AppLanguage.Japanese -> JapaneseTranslations.tagFeedback_tagSourceReadError_1
    AppLanguage.Korean -> KoreanTranslations.tagFeedback_tagSourceReadError_1
    AppLanguage.Latvian -> LatvianTranslations.tagFeedback_tagSourceReadError_1
    AppLanguage.Lithuanian -> LithuanianTranslations.tagFeedback_tagSourceReadError_1
    AppLanguage.Macedonian -> MacedonianTranslations.tagFeedback_tagSourceReadError_1
    AppLanguage.Malay -> MalayTranslations.tagFeedback_tagSourceReadError_1
    AppLanguage.Norwegian -> NorwegianTranslations.tagFeedback_tagSourceReadError_1
    AppLanguage.Polish -> PolishTranslations.tagFeedback_tagSourceReadError_1
    AppLanguage.Portuguese -> PortugueseTranslations.tagFeedback_tagSourceReadError_1
    AppLanguage.Punjabi -> PunjabiTranslations.tagFeedback_tagSourceReadError_1
    AppLanguage.Romanian -> RomanianTranslations.tagFeedback_tagSourceReadError_1
    AppLanguage.Russian -> RussianTranslations.tagFeedback_tagSourceReadError_1
    AppLanguage.Serbian -> SerbianTranslations.tagFeedback_tagSourceReadError_1
    AppLanguage.Slovak -> SlovakTranslations.tagFeedback_tagSourceReadError_1
    AppLanguage.Slovenian -> SlovenianTranslations.tagFeedback_tagSourceReadError_1
    AppLanguage.Spanish -> SpanishTranslations.tagFeedback_tagSourceReadError_1
    AppLanguage.Swedish -> SwedishTranslations.tagFeedback_tagSourceReadError_1
    AppLanguage.Thai -> ThaiTranslations.tagFeedback_tagSourceReadError_1
    AppLanguage.Ukrainian -> UkrainianTranslations.tagFeedback_tagSourceReadError_1
    AppLanguage.Urdu -> UrduTranslations.tagFeedback_tagSourceReadError_1
}

private fun tagVerificationAfterSaveError(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.tagFeedback_tagVerificationAfterSaveError_1
    AppLanguage.Bengali -> BengaliTranslations.tagFeedback_tagVerificationAfterSaveError_1
    AppLanguage.Bosnian -> BosnianTranslations.tagFeedback_tagVerificationAfterSaveError_1
    AppLanguage.Bulgarian -> BulgarianTranslations.tagFeedback_tagVerificationAfterSaveError_1
    AppLanguage.Catalan -> CatalanTranslations.tagFeedback_tagVerificationAfterSaveError_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.tagFeedback_tagVerificationAfterSaveError_1
    AppLanguage.Croatian -> CroatianTranslations.tagFeedback_tagVerificationAfterSaveError_1
    AppLanguage.Czech -> CzechTranslations.tagFeedback_tagVerificationAfterSaveError_1
    AppLanguage.Danish -> DanishTranslations.tagFeedback_tagVerificationAfterSaveError_1
    AppLanguage.Dutch -> DutchTranslations.tagFeedback_tagVerificationAfterSaveError_1
    AppLanguage.English -> EnglishTranslations.tagFeedback_tagVerificationAfterSaveError_1
    AppLanguage.Estonian -> EstonianTranslations.tagFeedback_tagVerificationAfterSaveError_1
    AppLanguage.Finnish -> FinnishTranslations.tagFeedback_tagVerificationAfterSaveError_1
    AppLanguage.French -> FrenchTranslations.tagFeedback_tagVerificationAfterSaveError_1
    AppLanguage.German -> GermanTranslations.tagFeedback_tagVerificationAfterSaveError_1
    AppLanguage.Greek -> GreekTranslations.tagFeedback_tagVerificationAfterSaveError_1
    AppLanguage.Hindi -> HindiTranslations.tagFeedback_tagVerificationAfterSaveError_1
    AppLanguage.Hungarian -> HungarianTranslations.tagFeedback_tagVerificationAfterSaveError_1
    AppLanguage.Indonesian -> IndonesianTranslations.tagFeedback_tagVerificationAfterSaveError_1
    AppLanguage.Italian -> ItalianTranslations.tagFeedback_tagVerificationAfterSaveError_1
    AppLanguage.Japanese -> JapaneseTranslations.tagFeedback_tagVerificationAfterSaveError_1
    AppLanguage.Korean -> KoreanTranslations.tagFeedback_tagVerificationAfterSaveError_1
    AppLanguage.Latvian -> LatvianTranslations.tagFeedback_tagVerificationAfterSaveError_1
    AppLanguage.Lithuanian -> LithuanianTranslations.tagFeedback_tagVerificationAfterSaveError_1
    AppLanguage.Macedonian -> MacedonianTranslations.tagFeedback_tagVerificationAfterSaveError_1
    AppLanguage.Malay -> MalayTranslations.tagFeedback_tagVerificationAfterSaveError_1
    AppLanguage.Norwegian -> NorwegianTranslations.tagFeedback_tagVerificationAfterSaveError_1
    AppLanguage.Polish -> PolishTranslations.tagFeedback_tagVerificationAfterSaveError_1
    AppLanguage.Portuguese -> PortugueseTranslations.tagFeedback_tagVerificationAfterSaveError_1
    AppLanguage.Punjabi -> PunjabiTranslations.tagFeedback_tagVerificationAfterSaveError_1
    AppLanguage.Romanian -> RomanianTranslations.tagFeedback_tagVerificationAfterSaveError_1
    AppLanguage.Russian -> RussianTranslations.tagFeedback_tagVerificationAfterSaveError_1
    AppLanguage.Serbian -> SerbianTranslations.tagFeedback_tagVerificationAfterSaveError_1
    AppLanguage.Slovak -> SlovakTranslations.tagFeedback_tagVerificationAfterSaveError_1
    AppLanguage.Slovenian -> SlovenianTranslations.tagFeedback_tagVerificationAfterSaveError_1
    AppLanguage.Spanish -> SpanishTranslations.tagFeedback_tagVerificationAfterSaveError_1
    AppLanguage.Swedish -> SwedishTranslations.tagFeedback_tagVerificationAfterSaveError_1
    AppLanguage.Thai -> ThaiTranslations.tagFeedback_tagVerificationAfterSaveError_1
    AppLanguage.Ukrainian -> UkrainianTranslations.tagFeedback_tagVerificationAfterSaveError_1
    AppLanguage.Urdu -> UrduTranslations.tagFeedback_tagVerificationAfterSaveError_1
}

private fun tagVerificationBeforeSaveError(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.tagFeedback_tagVerificationBeforeSaveError_1
    AppLanguage.Bengali -> BengaliTranslations.tagFeedback_tagVerificationBeforeSaveError_1
    AppLanguage.Bosnian -> BosnianTranslations.tagFeedback_tagVerificationBeforeSaveError_1
    AppLanguage.Bulgarian -> BulgarianTranslations.tagFeedback_tagVerificationBeforeSaveError_1
    AppLanguage.Catalan -> CatalanTranslations.tagFeedback_tagVerificationBeforeSaveError_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.tagFeedback_tagVerificationBeforeSaveError_1
    AppLanguage.Croatian -> CroatianTranslations.tagFeedback_tagVerificationBeforeSaveError_1
    AppLanguage.Czech -> CzechTranslations.tagFeedback_tagVerificationBeforeSaveError_1
    AppLanguage.Danish -> DanishTranslations.tagFeedback_tagVerificationBeforeSaveError_1
    AppLanguage.Dutch -> DutchTranslations.tagFeedback_tagVerificationBeforeSaveError_1
    AppLanguage.English -> EnglishTranslations.tagFeedback_tagVerificationBeforeSaveError_1
    AppLanguage.Estonian -> EstonianTranslations.tagFeedback_tagVerificationBeforeSaveError_1
    AppLanguage.Finnish -> FinnishTranslations.tagFeedback_tagVerificationBeforeSaveError_1
    AppLanguage.French -> FrenchTranslations.tagFeedback_tagVerificationBeforeSaveError_1
    AppLanguage.German -> GermanTranslations.tagFeedback_tagVerificationBeforeSaveError_1
    AppLanguage.Greek -> GreekTranslations.tagFeedback_tagVerificationBeforeSaveError_1
    AppLanguage.Hindi -> HindiTranslations.tagFeedback_tagVerificationBeforeSaveError_1
    AppLanguage.Hungarian -> HungarianTranslations.tagFeedback_tagVerificationBeforeSaveError_1
    AppLanguage.Indonesian -> IndonesianTranslations.tagFeedback_tagVerificationBeforeSaveError_1
    AppLanguage.Italian -> ItalianTranslations.tagFeedback_tagVerificationBeforeSaveError_1
    AppLanguage.Japanese -> JapaneseTranslations.tagFeedback_tagVerificationBeforeSaveError_1
    AppLanguage.Korean -> KoreanTranslations.tagFeedback_tagVerificationBeforeSaveError_1
    AppLanguage.Latvian -> LatvianTranslations.tagFeedback_tagVerificationBeforeSaveError_1
    AppLanguage.Lithuanian -> LithuanianTranslations.tagFeedback_tagVerificationBeforeSaveError_1
    AppLanguage.Macedonian -> MacedonianTranslations.tagFeedback_tagVerificationBeforeSaveError_1
    AppLanguage.Malay -> MalayTranslations.tagFeedback_tagVerificationBeforeSaveError_1
    AppLanguage.Norwegian -> NorwegianTranslations.tagFeedback_tagVerificationBeforeSaveError_1
    AppLanguage.Polish -> PolishTranslations.tagFeedback_tagVerificationBeforeSaveError_1
    AppLanguage.Portuguese -> PortugueseTranslations.tagFeedback_tagVerificationBeforeSaveError_1
    AppLanguage.Punjabi -> PunjabiTranslations.tagFeedback_tagVerificationBeforeSaveError_1
    AppLanguage.Romanian -> RomanianTranslations.tagFeedback_tagVerificationBeforeSaveError_1
    AppLanguage.Russian -> RussianTranslations.tagFeedback_tagVerificationBeforeSaveError_1
    AppLanguage.Serbian -> SerbianTranslations.tagFeedback_tagVerificationBeforeSaveError_1
    AppLanguage.Slovak -> SlovakTranslations.tagFeedback_tagVerificationBeforeSaveError_1
    AppLanguage.Slovenian -> SlovenianTranslations.tagFeedback_tagVerificationBeforeSaveError_1
    AppLanguage.Spanish -> SpanishTranslations.tagFeedback_tagVerificationBeforeSaveError_1
    AppLanguage.Swedish -> SwedishTranslations.tagFeedback_tagVerificationBeforeSaveError_1
    AppLanguage.Thai -> ThaiTranslations.tagFeedback_tagVerificationBeforeSaveError_1
    AppLanguage.Ukrainian -> UkrainianTranslations.tagFeedback_tagVerificationBeforeSaveError_1
    AppLanguage.Urdu -> UrduTranslations.tagFeedback_tagVerificationBeforeSaveError_1
}

private fun tagTemporaryWriteFailure(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.tagFeedback_tagTemporaryWriteFailure_1
    AppLanguage.Bengali -> BengaliTranslations.tagFeedback_tagTemporaryWriteFailure_1
    AppLanguage.Bosnian -> BosnianTranslations.tagFeedback_tagTemporaryWriteFailure_1
    AppLanguage.Bulgarian -> BulgarianTranslations.tagFeedback_tagTemporaryWriteFailure_1
    AppLanguage.Catalan -> CatalanTranslations.tagFeedback_tagTemporaryWriteFailure_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.tagFeedback_tagTemporaryWriteFailure_1
    AppLanguage.Croatian -> CroatianTranslations.tagFeedback_tagTemporaryWriteFailure_1
    AppLanguage.Czech -> CzechTranslations.tagFeedback_tagTemporaryWriteFailure_1
    AppLanguage.Danish -> DanishTranslations.tagFeedback_tagTemporaryWriteFailure_1
    AppLanguage.Dutch -> DutchTranslations.tagFeedback_tagTemporaryWriteFailure_1
    AppLanguage.English -> EnglishTranslations.tagFeedback_tagTemporaryWriteFailure_1
    AppLanguage.Estonian -> EstonianTranslations.tagFeedback_tagTemporaryWriteFailure_1
    AppLanguage.Finnish -> FinnishTranslations.tagFeedback_tagTemporaryWriteFailure_1
    AppLanguage.French -> FrenchTranslations.tagFeedback_tagTemporaryWriteFailure_1
    AppLanguage.German -> GermanTranslations.tagFeedback_tagTemporaryWriteFailure_1
    AppLanguage.Greek -> GreekTranslations.tagFeedback_tagTemporaryWriteFailure_1
    AppLanguage.Hindi -> HindiTranslations.tagFeedback_tagTemporaryWriteFailure_1
    AppLanguage.Hungarian -> HungarianTranslations.tagFeedback_tagTemporaryWriteFailure_1
    AppLanguage.Indonesian -> IndonesianTranslations.tagFeedback_tagTemporaryWriteFailure_1
    AppLanguage.Italian -> ItalianTranslations.tagFeedback_tagTemporaryWriteFailure_1
    AppLanguage.Japanese -> JapaneseTranslations.tagFeedback_tagTemporaryWriteFailure_1
    AppLanguage.Korean -> KoreanTranslations.tagFeedback_tagTemporaryWriteFailure_1
    AppLanguage.Latvian -> LatvianTranslations.tagFeedback_tagTemporaryWriteFailure_1
    AppLanguage.Lithuanian -> LithuanianTranslations.tagFeedback_tagTemporaryWriteFailure_1
    AppLanguage.Macedonian -> MacedonianTranslations.tagFeedback_tagTemporaryWriteFailure_1
    AppLanguage.Malay -> MalayTranslations.tagFeedback_tagTemporaryWriteFailure_1
    AppLanguage.Norwegian -> NorwegianTranslations.tagFeedback_tagTemporaryWriteFailure_1
    AppLanguage.Polish -> PolishTranslations.tagFeedback_tagTemporaryWriteFailure_1
    AppLanguage.Portuguese -> PortugueseTranslations.tagFeedback_tagTemporaryWriteFailure_1
    AppLanguage.Punjabi -> PunjabiTranslations.tagFeedback_tagTemporaryWriteFailure_1
    AppLanguage.Romanian -> RomanianTranslations.tagFeedback_tagTemporaryWriteFailure_1
    AppLanguage.Russian -> RussianTranslations.tagFeedback_tagTemporaryWriteFailure_1
    AppLanguage.Serbian -> SerbianTranslations.tagFeedback_tagTemporaryWriteFailure_1
    AppLanguage.Slovak -> SlovakTranslations.tagFeedback_tagTemporaryWriteFailure_1
    AppLanguage.Slovenian -> SlovenianTranslations.tagFeedback_tagTemporaryWriteFailure_1
    AppLanguage.Spanish -> SpanishTranslations.tagFeedback_tagTemporaryWriteFailure_1
    AppLanguage.Swedish -> SwedishTranslations.tagFeedback_tagTemporaryWriteFailure_1
    AppLanguage.Thai -> ThaiTranslations.tagFeedback_tagTemporaryWriteFailure_1
    AppLanguage.Ukrainian -> UkrainianTranslations.tagFeedback_tagTemporaryWriteFailure_1
    AppLanguage.Urdu -> UrduTranslations.tagFeedback_tagTemporaryWriteFailure_1
}

private fun tagPermissionDeniedAfterGrant(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.tagFeedback_tagPermissionDeniedAfterGrant_1
    AppLanguage.Bengali -> BengaliTranslations.tagFeedback_tagPermissionDeniedAfterGrant_1
    AppLanguage.Bosnian -> BosnianTranslations.tagFeedback_tagPermissionDeniedAfterGrant_1
    AppLanguage.Bulgarian -> BulgarianTranslations.tagFeedback_tagPermissionDeniedAfterGrant_1
    AppLanguage.Catalan -> CatalanTranslations.tagFeedback_tagPermissionDeniedAfterGrant_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.tagFeedback_tagPermissionDeniedAfterGrant_1
    AppLanguage.Croatian -> CroatianTranslations.tagFeedback_tagPermissionDeniedAfterGrant_1
    AppLanguage.Czech -> CzechTranslations.tagFeedback_tagPermissionDeniedAfterGrant_1
    AppLanguage.Danish -> DanishTranslations.tagFeedback_tagPermissionDeniedAfterGrant_1
    AppLanguage.Dutch -> DutchTranslations.tagFeedback_tagPermissionDeniedAfterGrant_1
    AppLanguage.English -> EnglishTranslations.tagFeedback_tagPermissionDeniedAfterGrant_1
    AppLanguage.Estonian -> EstonianTranslations.tagFeedback_tagPermissionDeniedAfterGrant_1
    AppLanguage.Finnish -> FinnishTranslations.tagFeedback_tagPermissionDeniedAfterGrant_1
    AppLanguage.French -> FrenchTranslations.tagFeedback_tagPermissionDeniedAfterGrant_1
    AppLanguage.German -> GermanTranslations.tagFeedback_tagPermissionDeniedAfterGrant_1
    AppLanguage.Greek -> GreekTranslations.tagFeedback_tagPermissionDeniedAfterGrant_1
    AppLanguage.Hindi -> HindiTranslations.tagFeedback_tagPermissionDeniedAfterGrant_1
    AppLanguage.Hungarian -> HungarianTranslations.tagFeedback_tagPermissionDeniedAfterGrant_1
    AppLanguage.Indonesian -> IndonesianTranslations.tagFeedback_tagPermissionDeniedAfterGrant_1
    AppLanguage.Italian -> ItalianTranslations.tagFeedback_tagPermissionDeniedAfterGrant_1
    AppLanguage.Japanese -> JapaneseTranslations.tagFeedback_tagPermissionDeniedAfterGrant_1
    AppLanguage.Korean -> KoreanTranslations.tagFeedback_tagPermissionDeniedAfterGrant_1
    AppLanguage.Latvian -> LatvianTranslations.tagFeedback_tagPermissionDeniedAfterGrant_1
    AppLanguage.Lithuanian -> LithuanianTranslations.tagFeedback_tagPermissionDeniedAfterGrant_1
    AppLanguage.Macedonian -> MacedonianTranslations.tagFeedback_tagPermissionDeniedAfterGrant_1
    AppLanguage.Malay -> MalayTranslations.tagFeedback_tagPermissionDeniedAfterGrant_1
    AppLanguage.Norwegian -> NorwegianTranslations.tagFeedback_tagPermissionDeniedAfterGrant_1
    AppLanguage.Polish -> PolishTranslations.tagFeedback_tagPermissionDeniedAfterGrant_1
    AppLanguage.Portuguese -> PortugueseTranslations.tagFeedback_tagPermissionDeniedAfterGrant_1
    AppLanguage.Punjabi -> PunjabiTranslations.tagFeedback_tagPermissionDeniedAfterGrant_1
    AppLanguage.Romanian -> RomanianTranslations.tagFeedback_tagPermissionDeniedAfterGrant_1
    AppLanguage.Russian -> RussianTranslations.tagFeedback_tagPermissionDeniedAfterGrant_1
    AppLanguage.Serbian -> SerbianTranslations.tagFeedback_tagPermissionDeniedAfterGrant_1
    AppLanguage.Slovak -> SlovakTranslations.tagFeedback_tagPermissionDeniedAfterGrant_1
    AppLanguage.Slovenian -> SlovenianTranslations.tagFeedback_tagPermissionDeniedAfterGrant_1
    AppLanguage.Spanish -> SpanishTranslations.tagFeedback_tagPermissionDeniedAfterGrant_1
    AppLanguage.Swedish -> SwedishTranslations.tagFeedback_tagPermissionDeniedAfterGrant_1
    AppLanguage.Thai -> ThaiTranslations.tagFeedback_tagPermissionDeniedAfterGrant_1
    AppLanguage.Ukrainian -> UkrainianTranslations.tagFeedback_tagPermissionDeniedAfterGrant_1
    AppLanguage.Urdu -> UrduTranslations.tagFeedback_tagPermissionDeniedAfterGrant_1
}

private fun tagWritePermissionRequired(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.tagFeedback_tagWritePermissionRequired_1
    AppLanguage.Bengali -> BengaliTranslations.tagFeedback_tagWritePermissionRequired_1
    AppLanguage.Bosnian -> BosnianTranslations.tagFeedback_tagWritePermissionRequired_1
    AppLanguage.Bulgarian -> BulgarianTranslations.tagFeedback_tagWritePermissionRequired_1
    AppLanguage.Catalan -> CatalanTranslations.tagFeedback_tagWritePermissionRequired_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.tagFeedback_tagWritePermissionRequired_1
    AppLanguage.Croatian -> CroatianTranslations.tagFeedback_tagWritePermissionRequired_1
    AppLanguage.Czech -> CzechTranslations.tagFeedback_tagWritePermissionRequired_1
    AppLanguage.Danish -> DanishTranslations.tagFeedback_tagWritePermissionRequired_1
    AppLanguage.Dutch -> DutchTranslations.tagFeedback_tagWritePermissionRequired_1
    AppLanguage.English -> EnglishTranslations.tagFeedback_tagWritePermissionRequired_1
    AppLanguage.Estonian -> EstonianTranslations.tagFeedback_tagWritePermissionRequired_1
    AppLanguage.Finnish -> FinnishTranslations.tagFeedback_tagWritePermissionRequired_1
    AppLanguage.French -> FrenchTranslations.tagFeedback_tagWritePermissionRequired_1
    AppLanguage.German -> GermanTranslations.tagFeedback_tagWritePermissionRequired_1
    AppLanguage.Greek -> GreekTranslations.tagFeedback_tagWritePermissionRequired_1
    AppLanguage.Hindi -> HindiTranslations.tagFeedback_tagWritePermissionRequired_1
    AppLanguage.Hungarian -> HungarianTranslations.tagFeedback_tagWritePermissionRequired_1
    AppLanguage.Indonesian -> IndonesianTranslations.tagFeedback_tagWritePermissionRequired_1
    AppLanguage.Italian -> ItalianTranslations.tagFeedback_tagWritePermissionRequired_1
    AppLanguage.Japanese -> JapaneseTranslations.tagFeedback_tagWritePermissionRequired_1
    AppLanguage.Korean -> KoreanTranslations.tagFeedback_tagWritePermissionRequired_1
    AppLanguage.Latvian -> LatvianTranslations.tagFeedback_tagWritePermissionRequired_1
    AppLanguage.Lithuanian -> LithuanianTranslations.tagFeedback_tagWritePermissionRequired_1
    AppLanguage.Macedonian -> MacedonianTranslations.tagFeedback_tagWritePermissionRequired_1
    AppLanguage.Malay -> MalayTranslations.tagFeedback_tagWritePermissionRequired_1
    AppLanguage.Norwegian -> NorwegianTranslations.tagFeedback_tagWritePermissionRequired_1
    AppLanguage.Polish -> PolishTranslations.tagFeedback_tagWritePermissionRequired_1
    AppLanguage.Portuguese -> PortugueseTranslations.tagFeedback_tagWritePermissionRequired_1
    AppLanguage.Punjabi -> PunjabiTranslations.tagFeedback_tagWritePermissionRequired_1
    AppLanguage.Romanian -> RomanianTranslations.tagFeedback_tagWritePermissionRequired_1
    AppLanguage.Russian -> RussianTranslations.tagFeedback_tagWritePermissionRequired_1
    AppLanguage.Serbian -> SerbianTranslations.tagFeedback_tagWritePermissionRequired_1
    AppLanguage.Slovak -> SlovakTranslations.tagFeedback_tagWritePermissionRequired_1
    AppLanguage.Slovenian -> SlovenianTranslations.tagFeedback_tagWritePermissionRequired_1
    AppLanguage.Spanish -> SpanishTranslations.tagFeedback_tagWritePermissionRequired_1
    AppLanguage.Swedish -> SwedishTranslations.tagFeedback_tagWritePermissionRequired_1
    AppLanguage.Thai -> ThaiTranslations.tagFeedback_tagWritePermissionRequired_1
    AppLanguage.Ukrainian -> UkrainianTranslations.tagFeedback_tagWritePermissionRequired_1
    AppLanguage.Urdu -> UrduTranslations.tagFeedback_tagWritePermissionRequired_1
}

private fun tagRollbackFailure(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.tagFeedback_tagRollbackFailure_1
    AppLanguage.Bengali -> BengaliTranslations.tagFeedback_tagRollbackFailure_1
    AppLanguage.Bosnian -> BosnianTranslations.tagFeedback_tagRollbackFailure_1
    AppLanguage.Bulgarian -> BulgarianTranslations.tagFeedback_tagRollbackFailure_1
    AppLanguage.Catalan -> CatalanTranslations.tagFeedback_tagRollbackFailure_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.tagFeedback_tagRollbackFailure_1
    AppLanguage.Croatian -> CroatianTranslations.tagFeedback_tagRollbackFailure_1
    AppLanguage.Czech -> CzechTranslations.tagFeedback_tagRollbackFailure_1
    AppLanguage.Danish -> DanishTranslations.tagFeedback_tagRollbackFailure_1
    AppLanguage.Dutch -> DutchTranslations.tagFeedback_tagRollbackFailure_1
    AppLanguage.English -> EnglishTranslations.tagFeedback_tagRollbackFailure_1
    AppLanguage.Estonian -> EstonianTranslations.tagFeedback_tagRollbackFailure_1
    AppLanguage.Finnish -> FinnishTranslations.tagFeedback_tagRollbackFailure_1
    AppLanguage.French -> FrenchTranslations.tagFeedback_tagRollbackFailure_1
    AppLanguage.German -> GermanTranslations.tagFeedback_tagRollbackFailure_1
    AppLanguage.Greek -> GreekTranslations.tagFeedback_tagRollbackFailure_1
    AppLanguage.Hindi -> HindiTranslations.tagFeedback_tagRollbackFailure_1
    AppLanguage.Hungarian -> HungarianTranslations.tagFeedback_tagRollbackFailure_1
    AppLanguage.Indonesian -> IndonesianTranslations.tagFeedback_tagRollbackFailure_1
    AppLanguage.Italian -> ItalianTranslations.tagFeedback_tagRollbackFailure_1
    AppLanguage.Japanese -> JapaneseTranslations.tagFeedback_tagRollbackFailure_1
    AppLanguage.Korean -> KoreanTranslations.tagFeedback_tagRollbackFailure_1
    AppLanguage.Latvian -> LatvianTranslations.tagFeedback_tagRollbackFailure_1
    AppLanguage.Lithuanian -> LithuanianTranslations.tagFeedback_tagRollbackFailure_1
    AppLanguage.Macedonian -> MacedonianTranslations.tagFeedback_tagRollbackFailure_1
    AppLanguage.Malay -> MalayTranslations.tagFeedback_tagRollbackFailure_1
    AppLanguage.Norwegian -> NorwegianTranslations.tagFeedback_tagRollbackFailure_1
    AppLanguage.Polish -> PolishTranslations.tagFeedback_tagRollbackFailure_1
    AppLanguage.Portuguese -> PortugueseTranslations.tagFeedback_tagRollbackFailure_1
    AppLanguage.Punjabi -> PunjabiTranslations.tagFeedback_tagRollbackFailure_1
    AppLanguage.Romanian -> RomanianTranslations.tagFeedback_tagRollbackFailure_1
    AppLanguage.Russian -> RussianTranslations.tagFeedback_tagRollbackFailure_1
    AppLanguage.Serbian -> SerbianTranslations.tagFeedback_tagRollbackFailure_1
    AppLanguage.Slovak -> SlovakTranslations.tagFeedback_tagRollbackFailure_1
    AppLanguage.Slovenian -> SlovenianTranslations.tagFeedback_tagRollbackFailure_1
    AppLanguage.Spanish -> SpanishTranslations.tagFeedback_tagRollbackFailure_1
    AppLanguage.Swedish -> SwedishTranslations.tagFeedback_tagRollbackFailure_1
    AppLanguage.Thai -> ThaiTranslations.tagFeedback_tagRollbackFailure_1
    AppLanguage.Ukrainian -> UkrainianTranslations.tagFeedback_tagRollbackFailure_1
    AppLanguage.Urdu -> UrduTranslations.tagFeedback_tagRollbackFailure_1
}

private fun cannotReadArtworkError(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.tagFeedback_cannotReadArtworkError_1
    AppLanguage.Bengali -> BengaliTranslations.tagFeedback_cannotReadArtworkError_1
    AppLanguage.Bosnian -> BosnianTranslations.tagFeedback_cannotReadArtworkError_1
    AppLanguage.Bulgarian -> BulgarianTranslations.tagFeedback_cannotReadArtworkError_1
    AppLanguage.Catalan -> CatalanTranslations.tagFeedback_cannotReadArtworkError_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.tagFeedback_cannotReadArtworkError_1
    AppLanguage.Croatian -> CroatianTranslations.tagFeedback_cannotReadArtworkError_1
    AppLanguage.Czech -> CzechTranslations.tagFeedback_cannotReadArtworkError_1
    AppLanguage.Danish -> DanishTranslations.tagFeedback_cannotReadArtworkError_1
    AppLanguage.Dutch -> DutchTranslations.tagFeedback_cannotReadArtworkError_1
    AppLanguage.English -> EnglishTranslations.tagFeedback_cannotReadArtworkError_1
    AppLanguage.Estonian -> EstonianTranslations.tagFeedback_cannotReadArtworkError_1
    AppLanguage.Finnish -> FinnishTranslations.tagFeedback_cannotReadArtworkError_1
    AppLanguage.French -> FrenchTranslations.tagFeedback_cannotReadArtworkError_1
    AppLanguage.German -> GermanTranslations.tagFeedback_cannotReadArtworkError_1
    AppLanguage.Greek -> GreekTranslations.tagFeedback_cannotReadArtworkError_1
    AppLanguage.Hindi -> HindiTranslations.tagFeedback_cannotReadArtworkError_1
    AppLanguage.Hungarian -> HungarianTranslations.tagFeedback_cannotReadArtworkError_1
    AppLanguage.Indonesian -> IndonesianTranslations.tagFeedback_cannotReadArtworkError_1
    AppLanguage.Italian -> ItalianTranslations.tagFeedback_cannotReadArtworkError_1
    AppLanguage.Japanese -> JapaneseTranslations.tagFeedback_cannotReadArtworkError_1
    AppLanguage.Korean -> KoreanTranslations.tagFeedback_cannotReadArtworkError_1
    AppLanguage.Latvian -> LatvianTranslations.tagFeedback_cannotReadArtworkError_1
    AppLanguage.Lithuanian -> LithuanianTranslations.tagFeedback_cannotReadArtworkError_1
    AppLanguage.Macedonian -> MacedonianTranslations.tagFeedback_cannotReadArtworkError_1
    AppLanguage.Malay -> MalayTranslations.tagFeedback_cannotReadArtworkError_1
    AppLanguage.Norwegian -> NorwegianTranslations.tagFeedback_cannotReadArtworkError_1
    AppLanguage.Polish -> PolishTranslations.tagFeedback_cannotReadArtworkError_1
    AppLanguage.Portuguese -> PortugueseTranslations.tagFeedback_cannotReadArtworkError_1
    AppLanguage.Punjabi -> PunjabiTranslations.tagFeedback_cannotReadArtworkError_1
    AppLanguage.Romanian -> RomanianTranslations.tagFeedback_cannotReadArtworkError_1
    AppLanguage.Russian -> RussianTranslations.tagFeedback_cannotReadArtworkError_1
    AppLanguage.Serbian -> SerbianTranslations.tagFeedback_cannotReadArtworkError_1
    AppLanguage.Slovak -> SlovakTranslations.tagFeedback_cannotReadArtworkError_1
    AppLanguage.Slovenian -> SlovenianTranslations.tagFeedback_cannotReadArtworkError_1
    AppLanguage.Spanish -> SpanishTranslations.tagFeedback_cannotReadArtworkError_1
    AppLanguage.Swedish -> SwedishTranslations.tagFeedback_cannotReadArtworkError_1
    AppLanguage.Thai -> ThaiTranslations.tagFeedback_cannotReadArtworkError_1
    AppLanguage.Ukrainian -> UkrainianTranslations.tagFeedback_cannotReadArtworkError_1
    AppLanguage.Urdu -> UrduTranslations.tagFeedback_cannotReadArtworkError_1
}

private fun unsupportedArtworkError(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.tagFeedback_unsupportedArtworkError_1
    AppLanguage.Bengali -> BengaliTranslations.tagFeedback_unsupportedArtworkError_1
    AppLanguage.Bosnian -> BosnianTranslations.tagFeedback_unsupportedArtworkError_1
    AppLanguage.Bulgarian -> BulgarianTranslations.tagFeedback_unsupportedArtworkError_1
    AppLanguage.Catalan -> CatalanTranslations.tagFeedback_unsupportedArtworkError_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.tagFeedback_unsupportedArtworkError_1
    AppLanguage.Croatian -> CroatianTranslations.tagFeedback_unsupportedArtworkError_1
    AppLanguage.Czech -> CzechTranslations.tagFeedback_unsupportedArtworkError_1
    AppLanguage.Danish -> DanishTranslations.tagFeedback_unsupportedArtworkError_1
    AppLanguage.Dutch -> DutchTranslations.tagFeedback_unsupportedArtworkError_1
    AppLanguage.English -> EnglishTranslations.tagFeedback_unsupportedArtworkError_1
    AppLanguage.Estonian -> EstonianTranslations.tagFeedback_unsupportedArtworkError_1
    AppLanguage.Finnish -> FinnishTranslations.tagFeedback_unsupportedArtworkError_1
    AppLanguage.French -> FrenchTranslations.tagFeedback_unsupportedArtworkError_1
    AppLanguage.German -> GermanTranslations.tagFeedback_unsupportedArtworkError_1
    AppLanguage.Greek -> GreekTranslations.tagFeedback_unsupportedArtworkError_1
    AppLanguage.Hindi -> HindiTranslations.tagFeedback_unsupportedArtworkError_1
    AppLanguage.Hungarian -> HungarianTranslations.tagFeedback_unsupportedArtworkError_1
    AppLanguage.Indonesian -> IndonesianTranslations.tagFeedback_unsupportedArtworkError_1
    AppLanguage.Italian -> ItalianTranslations.tagFeedback_unsupportedArtworkError_1
    AppLanguage.Japanese -> JapaneseTranslations.tagFeedback_unsupportedArtworkError_1
    AppLanguage.Korean -> KoreanTranslations.tagFeedback_unsupportedArtworkError_1
    AppLanguage.Latvian -> LatvianTranslations.tagFeedback_unsupportedArtworkError_1
    AppLanguage.Lithuanian -> LithuanianTranslations.tagFeedback_unsupportedArtworkError_1
    AppLanguage.Macedonian -> MacedonianTranslations.tagFeedback_unsupportedArtworkError_1
    AppLanguage.Malay -> MalayTranslations.tagFeedback_unsupportedArtworkError_1
    AppLanguage.Norwegian -> NorwegianTranslations.tagFeedback_unsupportedArtworkError_1
    AppLanguage.Polish -> PolishTranslations.tagFeedback_unsupportedArtworkError_1
    AppLanguage.Portuguese -> PortugueseTranslations.tagFeedback_unsupportedArtworkError_1
    AppLanguage.Punjabi -> PunjabiTranslations.tagFeedback_unsupportedArtworkError_1
    AppLanguage.Romanian -> RomanianTranslations.tagFeedback_unsupportedArtworkError_1
    AppLanguage.Russian -> RussianTranslations.tagFeedback_unsupportedArtworkError_1
    AppLanguage.Serbian -> SerbianTranslations.tagFeedback_unsupportedArtworkError_1
    AppLanguage.Slovak -> SlovakTranslations.tagFeedback_unsupportedArtworkError_1
    AppLanguage.Slovenian -> SlovenianTranslations.tagFeedback_unsupportedArtworkError_1
    AppLanguage.Spanish -> SpanishTranslations.tagFeedback_unsupportedArtworkError_1
    AppLanguage.Swedish -> SwedishTranslations.tagFeedback_unsupportedArtworkError_1
    AppLanguage.Thai -> ThaiTranslations.tagFeedback_unsupportedArtworkError_1
    AppLanguage.Ukrainian -> UkrainianTranslations.tagFeedback_unsupportedArtworkError_1
    AppLanguage.Urdu -> UrduTranslations.tagFeedback_unsupportedArtworkError_1
}

private fun trackArtistAndChapterTitleErrors(language: AppLanguage): Pair<String, String> = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.tagFeedback_trackArtistAndChapterTitleErrors_variant_1 to AlbanianTranslations.tagFeedback_trackArtistAndChapterTitleErrors_variant_2
    AppLanguage.Bengali -> BengaliTranslations.tagFeedback_trackArtistAndChapterTitleErrors_variant_1 to BengaliTranslations.tagFeedback_trackArtistAndChapterTitleErrors_variant_2
    AppLanguage.Bosnian -> BosnianTranslations.tagFeedback_trackArtistAndChapterTitleErrors_variant_1 to BosnianTranslations.tagFeedback_trackArtistAndChapterTitleErrors_variant_2
    AppLanguage.Bulgarian -> BulgarianTranslations.tagFeedback_trackArtistAndChapterTitleErrors_variant_1 to BulgarianTranslations.tagFeedback_trackArtistAndChapterTitleErrors_variant_2
    AppLanguage.Catalan -> CatalanTranslations.tagFeedback_trackArtistAndChapterTitleErrors_variant_1 to CatalanTranslations.tagFeedback_trackArtistAndChapterTitleErrors_variant_2
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.tagFeedback_trackArtistAndChapterTitleErrors_variant_1 to ChineseSimplifiedTranslations.tagFeedback_trackArtistAndChapterTitleErrors_variant_2
    AppLanguage.Croatian -> CroatianTranslations.tagFeedback_trackArtistAndChapterTitleErrors_variant_1 to CroatianTranslations.tagFeedback_trackArtistAndChapterTitleErrors_variant_2
    AppLanguage.Czech -> CzechTranslations.tagFeedback_trackArtistAndChapterTitleErrors_variant_1 to CzechTranslations.tagFeedback_trackArtistAndChapterTitleErrors_variant_2
    AppLanguage.Danish -> DanishTranslations.tagFeedback_trackArtistAndChapterTitleErrors_variant_1 to DanishTranslations.tagFeedback_trackArtistAndChapterTitleErrors_variant_2
    AppLanguage.Dutch -> DutchTranslations.tagFeedback_trackArtistAndChapterTitleErrors_variant_1 to DutchTranslations.tagFeedback_trackArtistAndChapterTitleErrors_variant_2
    AppLanguage.English -> TagValidationKind.TrackArtist.english to TagValidationKind.ChapterTitle.english
    AppLanguage.Estonian -> EstonianTranslations.tagFeedback_trackArtistAndChapterTitleErrors_variant_1 to EstonianTranslations.tagFeedback_trackArtistAndChapterTitleErrors_variant_2
    AppLanguage.Finnish -> FinnishTranslations.tagFeedback_trackArtistAndChapterTitleErrors_variant_1 to FinnishTranslations.tagFeedback_trackArtistAndChapterTitleErrors_variant_2
    AppLanguage.French -> FrenchTranslations.tagFeedback_trackArtistAndChapterTitleErrors_variant_1 to FrenchTranslations.tagFeedback_trackArtistAndChapterTitleErrors_variant_2
    AppLanguage.German -> GermanTranslations.tagFeedback_trackArtistAndChapterTitleErrors_variant_1 to GermanTranslations.tagFeedback_trackArtistAndChapterTitleErrors_variant_2
    AppLanguage.Greek -> GreekTranslations.tagFeedback_trackArtistAndChapterTitleErrors_variant_1 to GreekTranslations.tagFeedback_trackArtistAndChapterTitleErrors_variant_2
    AppLanguage.Hindi -> HindiTranslations.tagFeedback_trackArtistAndChapterTitleErrors_variant_1 to HindiTranslations.tagFeedback_trackArtistAndChapterTitleErrors_variant_2
    AppLanguage.Hungarian -> HungarianTranslations.tagFeedback_trackArtistAndChapterTitleErrors_variant_1 to HungarianTranslations.tagFeedback_trackArtistAndChapterTitleErrors_variant_2
    AppLanguage.Indonesian -> IndonesianTranslations.tagFeedback_trackArtistAndChapterTitleErrors_variant_1 to IndonesianTranslations.tagFeedback_trackArtistAndChapterTitleErrors_variant_2
    AppLanguage.Italian -> ItalianTranslations.tagFeedback_trackArtistAndChapterTitleErrors_variant_1 to ItalianTranslations.tagFeedback_trackArtistAndChapterTitleErrors_variant_2
    AppLanguage.Japanese -> JapaneseTranslations.tagFeedback_trackArtistAndChapterTitleErrors_variant_1 to JapaneseTranslations.tagFeedback_trackArtistAndChapterTitleErrors_variant_2
    AppLanguage.Korean -> KoreanTranslations.tagFeedback_trackArtistAndChapterTitleErrors_variant_1 to KoreanTranslations.tagFeedback_trackArtistAndChapterTitleErrors_variant_2
    AppLanguage.Latvian -> LatvianTranslations.tagFeedback_trackArtistAndChapterTitleErrors_variant_1 to LatvianTranslations.tagFeedback_trackArtistAndChapterTitleErrors_variant_2
    AppLanguage.Lithuanian -> LithuanianTranslations.tagFeedback_trackArtistAndChapterTitleErrors_variant_1 to LithuanianTranslations.tagFeedback_trackArtistAndChapterTitleErrors_variant_2
    AppLanguage.Macedonian -> MacedonianTranslations.tagFeedback_trackArtistAndChapterTitleErrors_variant_1 to MacedonianTranslations.tagFeedback_trackArtistAndChapterTitleErrors_variant_2
    AppLanguage.Malay -> MalayTranslations.tagFeedback_trackArtistAndChapterTitleErrors_variant_1 to MalayTranslations.tagFeedback_trackArtistAndChapterTitleErrors_variant_2
    AppLanguage.Norwegian -> NorwegianTranslations.tagFeedback_trackArtistAndChapterTitleErrors_variant_1 to NorwegianTranslations.tagFeedback_trackArtistAndChapterTitleErrors_variant_2
    AppLanguage.Polish -> PolishTranslations.tagFeedback_trackArtistAndChapterTitleErrors_variant_1 to PolishTranslations.tagFeedback_trackArtistAndChapterTitleErrors_variant_2
    AppLanguage.Portuguese -> PortugueseTranslations.tagFeedback_trackArtistAndChapterTitleErrors_variant_1 to PortugueseTranslations.tagFeedback_trackArtistAndChapterTitleErrors_variant_2
    AppLanguage.Punjabi -> PunjabiTranslations.tagFeedback_trackArtistAndChapterTitleErrors_variant_1 to PunjabiTranslations.tagFeedback_trackArtistAndChapterTitleErrors_variant_2
    AppLanguage.Romanian -> RomanianTranslations.tagFeedback_trackArtistAndChapterTitleErrors_variant_1 to RomanianTranslations.tagFeedback_trackArtistAndChapterTitleErrors_variant_2
    AppLanguage.Russian -> RussianTranslations.tagFeedback_trackArtistAndChapterTitleErrors_variant_1 to RussianTranslations.tagFeedback_trackArtistAndChapterTitleErrors_variant_2
    AppLanguage.Serbian -> SerbianTranslations.tagFeedback_trackArtistAndChapterTitleErrors_variant_1 to SerbianTranslations.tagFeedback_trackArtistAndChapterTitleErrors_variant_2
    AppLanguage.Slovak -> SlovakTranslations.tagFeedback_trackArtistAndChapterTitleErrors_variant_1 to SlovakTranslations.tagFeedback_trackArtistAndChapterTitleErrors_variant_2
    AppLanguage.Slovenian -> SlovenianTranslations.tagFeedback_trackArtistAndChapterTitleErrors_variant_1 to SlovenianTranslations.tagFeedback_trackArtistAndChapterTitleErrors_variant_2
    AppLanguage.Spanish -> SpanishTranslations.tagFeedback_trackArtistAndChapterTitleErrors_variant_1 to SpanishTranslations.tagFeedback_trackArtistAndChapterTitleErrors_variant_2
    AppLanguage.Swedish -> SwedishTranslations.tagFeedback_trackArtistAndChapterTitleErrors_variant_1 to SwedishTranslations.tagFeedback_trackArtistAndChapterTitleErrors_variant_2
    AppLanguage.Thai -> ThaiTranslations.tagFeedback_trackArtistAndChapterTitleErrors_variant_1 to ThaiTranslations.tagFeedback_trackArtistAndChapterTitleErrors_variant_2
    AppLanguage.Ukrainian -> UkrainianTranslations.tagFeedback_trackArtistAndChapterTitleErrors_variant_1 to UkrainianTranslations.tagFeedback_trackArtistAndChapterTitleErrors_variant_2
    AppLanguage.Urdu -> UrduTranslations.tagFeedback_trackArtistAndChapterTitleErrors_variant_1 to UrduTranslations.tagFeedback_trackArtistAndChapterTitleErrors_variant_2
}

private fun trackTitleError(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.tagFeedback_trackTitleError_1
    AppLanguage.Bengali -> BengaliTranslations.tagFeedback_trackTitleError_1
    AppLanguage.Bosnian -> BosnianTranslations.tagFeedback_trackTitleError_1
    AppLanguage.Bulgarian -> BulgarianTranslations.tagFeedback_trackTitleError_1
    AppLanguage.Catalan -> CatalanTranslations.tagFeedback_trackTitleError_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.tagFeedback_trackTitleError_1
    AppLanguage.Croatian -> CroatianTranslations.tagFeedback_trackTitleError_1
    AppLanguage.Czech -> CzechTranslations.tagFeedback_trackTitleError_1
    AppLanguage.Danish -> DanishTranslations.tagFeedback_trackTitleError_1
    AppLanguage.Dutch -> DutchTranslations.tagFeedback_trackTitleError_1
    AppLanguage.English -> TagValidationKind.TrackTitle.english
    AppLanguage.Estonian -> EstonianTranslations.tagFeedback_trackTitleError_1
    AppLanguage.Finnish -> FinnishTranslations.tagFeedback_trackTitleError_1
    AppLanguage.French -> FrenchTranslations.tagFeedback_trackTitleError_1
    AppLanguage.German -> GermanTranslations.tagFeedback_trackTitleError_1
    AppLanguage.Greek -> GreekTranslations.tagFeedback_trackTitleError_1
    AppLanguage.Hindi -> HindiTranslations.tagFeedback_trackTitleError_1
    AppLanguage.Hungarian -> HungarianTranslations.tagFeedback_trackTitleError_1
    AppLanguage.Indonesian -> IndonesianTranslations.tagFeedback_trackTitleError_1
    AppLanguage.Italian -> ItalianTranslations.tagFeedback_trackTitleError_1
    AppLanguage.Japanese -> JapaneseTranslations.tagFeedback_trackTitleError_1
    AppLanguage.Korean -> KoreanTranslations.tagFeedback_trackTitleError_1
    AppLanguage.Latvian -> LatvianTranslations.tagFeedback_trackTitleError_1
    AppLanguage.Lithuanian -> LithuanianTranslations.tagFeedback_trackTitleError_1
    AppLanguage.Macedonian -> MacedonianTranslations.tagFeedback_trackTitleError_1
    AppLanguage.Malay -> MalayTranslations.tagFeedback_trackTitleError_1
    AppLanguage.Norwegian -> NorwegianTranslations.tagFeedback_trackTitleError_1
    AppLanguage.Polish -> PolishTranslations.tagFeedback_trackTitleError_1
    AppLanguage.Portuguese -> PortugueseTranslations.tagFeedback_trackTitleError_1
    AppLanguage.Punjabi -> PunjabiTranslations.tagFeedback_trackTitleError_1
    AppLanguage.Romanian -> RomanianTranslations.tagFeedback_trackTitleError_1
    AppLanguage.Russian -> RussianTranslations.tagFeedback_trackTitleError_1
    AppLanguage.Serbian -> SerbianTranslations.tagFeedback_trackTitleError_1
    AppLanguage.Slovak -> SlovakTranslations.tagFeedback_trackTitleError_1
    AppLanguage.Slovenian -> SlovenianTranslations.tagFeedback_trackTitleError_1
    AppLanguage.Spanish -> SpanishTranslations.tagFeedback_trackTitleError_1
    AppLanguage.Swedish -> SwedishTranslations.tagFeedback_trackTitleError_1
    AppLanguage.Thai -> ThaiTranslations.tagFeedback_trackTitleError_1
    AppLanguage.Ukrainian -> UkrainianTranslations.tagFeedback_trackTitleError_1
    AppLanguage.Urdu -> UrduTranslations.tagFeedback_trackTitleError_1
}

private fun trackNumbersError(language: AppLanguage): String = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.tagFeedback_trackNumbersError_1
    AppLanguage.Bengali -> BengaliTranslations.tagFeedback_trackNumbersError_1
    AppLanguage.Bosnian -> BosnianTranslations.tagFeedback_trackNumbersError_1
    AppLanguage.Bulgarian -> BulgarianTranslations.tagFeedback_trackNumbersError_1
    AppLanguage.Catalan -> CatalanTranslations.tagFeedback_trackNumbersError_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.tagFeedback_trackNumbersError_1
    AppLanguage.Croatian -> CroatianTranslations.tagFeedback_trackNumbersError_1
    AppLanguage.Czech -> CzechTranslations.tagFeedback_trackNumbersError_1
    AppLanguage.Danish -> DanishTranslations.tagFeedback_trackNumbersError_1
    AppLanguage.Dutch -> DutchTranslations.tagFeedback_trackNumbersError_1
    AppLanguage.English -> TagValidationKind.TrackNumbers.english
    AppLanguage.Estonian -> EstonianTranslations.tagFeedback_trackNumbersError_1
    AppLanguage.Finnish -> FinnishTranslations.tagFeedback_trackNumbersError_1
    AppLanguage.French -> FrenchTranslations.tagFeedback_trackNumbersError_1
    AppLanguage.German -> GermanTranslations.tagFeedback_trackNumbersError_1
    AppLanguage.Greek -> GreekTranslations.tagFeedback_trackNumbersError_1
    AppLanguage.Hindi -> HindiTranslations.tagFeedback_trackNumbersError_1
    AppLanguage.Hungarian -> HungarianTranslations.tagFeedback_trackNumbersError_1
    AppLanguage.Indonesian -> IndonesianTranslations.tagFeedback_trackNumbersError_1
    AppLanguage.Italian -> ItalianTranslations.tagFeedback_trackNumbersError_1
    AppLanguage.Japanese -> JapaneseTranslations.tagFeedback_trackNumbersError_1
    AppLanguage.Korean -> KoreanTranslations.tagFeedback_trackNumbersError_1
    AppLanguage.Latvian -> LatvianTranslations.tagFeedback_trackNumbersError_1
    AppLanguage.Lithuanian -> LithuanianTranslations.tagFeedback_trackNumbersError_1
    AppLanguage.Macedonian -> MacedonianTranslations.tagFeedback_trackNumbersError_1
    AppLanguage.Malay -> MalayTranslations.tagFeedback_trackNumbersError_1
    AppLanguage.Norwegian -> NorwegianTranslations.tagFeedback_trackNumbersError_1
    AppLanguage.Polish -> PolishTranslations.tagFeedback_trackNumbersError_1
    AppLanguage.Portuguese -> PortugueseTranslations.tagFeedback_trackNumbersError_1
    AppLanguage.Punjabi -> PunjabiTranslations.tagFeedback_trackNumbersError_1
    AppLanguage.Romanian -> RomanianTranslations.tagFeedback_trackNumbersError_1
    AppLanguage.Russian -> RussianTranslations.tagFeedback_trackNumbersError_1
    AppLanguage.Serbian -> SerbianTranslations.tagFeedback_trackNumbersError_1
    AppLanguage.Slovak -> SlovakTranslations.tagFeedback_trackNumbersError_1
    AppLanguage.Slovenian -> SlovenianTranslations.tagFeedback_trackNumbersError_1
    AppLanguage.Spanish -> SpanishTranslations.tagFeedback_trackNumbersError_1
    AppLanguage.Swedish -> SwedishTranslations.tagFeedback_trackNumbersError_1
    AppLanguage.Thai -> ThaiTranslations.tagFeedback_trackNumbersError_1
    AppLanguage.Ukrainian -> UkrainianTranslations.tagFeedback_trackNumbersError_1
    AppLanguage.Urdu -> UrduTranslations.tagFeedback_trackNumbersError_1
}

private fun albumRequiredFields(language: AppLanguage): Pair<String, String> = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.tagFeedback_albumRequiredFields_variant_1 to AlbanianTranslations.tagFeedback_albumRequiredFields_variant_2
    AppLanguage.Bengali -> BengaliTranslations.tagFeedback_albumRequiredFields_variant_1 to BengaliTranslations.tagFeedback_albumRequiredFields_variant_2
    AppLanguage.Bosnian -> BosnianTranslations.tagFeedback_albumRequiredFields_variant_1 to BosnianTranslations.tagFeedback_albumRequiredFields_variant_2
    AppLanguage.Bulgarian -> BulgarianTranslations.tagFeedback_albumRequiredFields_variant_1 to BulgarianTranslations.tagFeedback_albumRequiredFields_variant_2
    AppLanguage.Catalan -> CatalanTranslations.tagFeedback_albumRequiredFields_variant_1 to CatalanTranslations.tagFeedback_albumRequiredFields_variant_2
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.tagFeedback_albumRequiredFields_variant_1 to ChineseSimplifiedTranslations.tagFeedback_albumRequiredFields_variant_2
    AppLanguage.Croatian -> CroatianTranslations.tagFeedback_albumRequiredFields_variant_1 to CroatianTranslations.tagFeedback_albumRequiredFields_variant_2
    AppLanguage.Czech -> CzechTranslations.tagFeedback_albumRequiredFields_variant_1 to CzechTranslations.tagFeedback_albumRequiredFields_variant_2
    AppLanguage.Danish -> DanishTranslations.tagFeedback_albumRequiredFields_variant_1 to DanishTranslations.tagFeedback_albumRequiredFields_variant_2
    AppLanguage.Dutch -> DutchTranslations.tagFeedback_albumRequiredFields_variant_1 to DutchTranslations.tagFeedback_albumRequiredFields_variant_2
    AppLanguage.English -> EnglishTranslations.tagFeedback_albumRequiredFields_variant_1 to EnglishTranslations.tagFeedback_albumRequiredFields_variant_2
    AppLanguage.Estonian -> EstonianTranslations.tagFeedback_albumRequiredFields_variant_1 to EstonianTranslations.tagFeedback_albumRequiredFields_variant_2
    AppLanguage.Finnish -> FinnishTranslations.tagFeedback_albumRequiredFields_variant_1 to FinnishTranslations.tagFeedback_albumRequiredFields_variant_2
    AppLanguage.French -> FrenchTranslations.tagFeedback_albumRequiredFields_variant_1 to FrenchTranslations.tagFeedback_albumRequiredFields_variant_2
    AppLanguage.German -> GermanTranslations.tagFeedback_albumRequiredFields_variant_1 to GermanTranslations.tagFeedback_albumRequiredFields_variant_2
    AppLanguage.Greek -> GreekTranslations.tagFeedback_albumRequiredFields_variant_1 to GreekTranslations.tagFeedback_albumRequiredFields_variant_2
    AppLanguage.Hindi -> HindiTranslations.tagFeedback_albumRequiredFields_variant_1 to HindiTranslations.tagFeedback_albumRequiredFields_variant_2
    AppLanguage.Hungarian -> HungarianTranslations.tagFeedback_albumRequiredFields_variant_1 to HungarianTranslations.tagFeedback_albumRequiredFields_variant_2
    AppLanguage.Indonesian -> IndonesianTranslations.tagFeedback_albumRequiredFields_variant_1 to IndonesianTranslations.tagFeedback_albumRequiredFields_variant_2
    AppLanguage.Italian -> ItalianTranslations.tagFeedback_albumRequiredFields_variant_1 to ItalianTranslations.tagFeedback_albumRequiredFields_variant_2
    AppLanguage.Japanese -> JapaneseTranslations.tagFeedback_albumRequiredFields_variant_1 to JapaneseTranslations.tagFeedback_albumRequiredFields_variant_2
    AppLanguage.Korean -> KoreanTranslations.tagFeedback_albumRequiredFields_variant_1 to KoreanTranslations.tagFeedback_albumRequiredFields_variant_2
    AppLanguage.Latvian -> LatvianTranslations.tagFeedback_albumRequiredFields_variant_1 to LatvianTranslations.tagFeedback_albumRequiredFields_variant_2
    AppLanguage.Lithuanian -> LithuanianTranslations.tagFeedback_albumRequiredFields_variant_1 to LithuanianTranslations.tagFeedback_albumRequiredFields_variant_2
    AppLanguage.Macedonian -> MacedonianTranslations.tagFeedback_albumRequiredFields_variant_1 to MacedonianTranslations.tagFeedback_albumRequiredFields_variant_2
    AppLanguage.Malay -> MalayTranslations.tagFeedback_albumRequiredFields_variant_1 to MalayTranslations.tagFeedback_albumRequiredFields_variant_2
    AppLanguage.Norwegian -> NorwegianTranslations.tagFeedback_albumRequiredFields_variant_1 to NorwegianTranslations.tagFeedback_albumRequiredFields_variant_2
    AppLanguage.Polish -> PolishTranslations.tagFeedback_albumRequiredFields_variant_1 to PolishTranslations.tagFeedback_albumRequiredFields_variant_2
    AppLanguage.Portuguese -> PortugueseTranslations.tagFeedback_albumRequiredFields_variant_1 to PortugueseTranslations.tagFeedback_albumRequiredFields_variant_2
    AppLanguage.Punjabi -> PunjabiTranslations.tagFeedback_albumRequiredFields_variant_1 to PunjabiTranslations.tagFeedback_albumRequiredFields_variant_2
    AppLanguage.Romanian -> RomanianTranslations.tagFeedback_albumRequiredFields_variant_1 to RomanianTranslations.tagFeedback_albumRequiredFields_variant_2
    AppLanguage.Russian -> RussianTranslations.tagFeedback_albumRequiredFields_variant_1 to RussianTranslations.tagFeedback_albumRequiredFields_variant_2
    AppLanguage.Serbian -> SerbianTranslations.tagFeedback_albumRequiredFields_variant_1 to SerbianTranslations.tagFeedback_albumRequiredFields_variant_2
    AppLanguage.Slovak -> SlovakTranslations.tagFeedback_albumRequiredFields_variant_1 to SlovakTranslations.tagFeedback_albumRequiredFields_variant_2
    AppLanguage.Slovenian -> SlovenianTranslations.tagFeedback_albumRequiredFields_variant_1 to SlovenianTranslations.tagFeedback_albumRequiredFields_variant_2
    AppLanguage.Spanish -> SpanishTranslations.tagFeedback_albumRequiredFields_variant_1 to SpanishTranslations.tagFeedback_albumRequiredFields_variant_2
    AppLanguage.Swedish -> SwedishTranslations.tagFeedback_albumRequiredFields_variant_1 to SwedishTranslations.tagFeedback_albumRequiredFields_variant_2
    AppLanguage.Thai -> ThaiTranslations.tagFeedback_albumRequiredFields_variant_1 to ThaiTranslations.tagFeedback_albumRequiredFields_variant_2
    AppLanguage.Ukrainian -> UkrainianTranslations.tagFeedback_albumRequiredFields_variant_1 to UkrainianTranslations.tagFeedback_albumRequiredFields_variant_2
    AppLanguage.Urdu -> UrduTranslations.tagFeedback_albumRequiredFields_variant_1 to UrduTranslations.tagFeedback_albumRequiredFields_variant_2
}

private fun audiobookRequiredFields(language: AppLanguage): Pair<String, String> = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.tagFeedback_audiobookRequiredFields_variant_1 to AlbanianTranslations.tagFeedback_audiobookRequiredFields_variant_2
    AppLanguage.Bengali -> BengaliTranslations.tagFeedback_audiobookRequiredFields_variant_1 to BengaliTranslations.tagFeedback_audiobookRequiredFields_variant_2
    AppLanguage.Bosnian -> BosnianTranslations.tagFeedback_audiobookRequiredFields_variant_1 to BosnianTranslations.tagFeedback_audiobookRequiredFields_variant_2
    AppLanguage.Bulgarian -> BulgarianTranslations.tagFeedback_audiobookRequiredFields_variant_1 to BulgarianTranslations.tagFeedback_audiobookRequiredFields_variant_2
    AppLanguage.Catalan -> CatalanTranslations.tagFeedback_audiobookRequiredFields_variant_1 to CatalanTranslations.tagFeedback_audiobookRequiredFields_variant_2
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.tagFeedback_audiobookRequiredFields_variant_1 to ChineseSimplifiedTranslations.tagFeedback_audiobookRequiredFields_variant_2
    AppLanguage.Croatian -> CroatianTranslations.tagFeedback_audiobookRequiredFields_variant_1 to CroatianTranslations.tagFeedback_audiobookRequiredFields_variant_2
    AppLanguage.Czech -> CzechTranslations.tagFeedback_audiobookRequiredFields_variant_1 to CzechTranslations.tagFeedback_audiobookRequiredFields_variant_2
    AppLanguage.Danish -> DanishTranslations.tagFeedback_audiobookRequiredFields_variant_1 to DanishTranslations.tagFeedback_audiobookRequiredFields_variant_2
    AppLanguage.Dutch -> DutchTranslations.tagFeedback_audiobookRequiredFields_variant_1 to DutchTranslations.tagFeedback_audiobookRequiredFields_variant_2
    AppLanguage.English -> EnglishTranslations.tagFeedback_audiobookRequiredFields_variant_1 to EnglishTranslations.tagFeedback_audiobookRequiredFields_variant_2
    AppLanguage.Estonian -> EstonianTranslations.tagFeedback_audiobookRequiredFields_variant_1 to EstonianTranslations.tagFeedback_audiobookRequiredFields_variant_2
    AppLanguage.Finnish -> FinnishTranslations.tagFeedback_audiobookRequiredFields_variant_1 to FinnishTranslations.tagFeedback_audiobookRequiredFields_variant_2
    AppLanguage.French -> FrenchTranslations.tagFeedback_audiobookRequiredFields_variant_1 to FrenchTranslations.tagFeedback_audiobookRequiredFields_variant_2
    AppLanguage.German -> GermanTranslations.tagFeedback_audiobookRequiredFields_variant_1 to GermanTranslations.tagFeedback_audiobookRequiredFields_variant_2
    AppLanguage.Greek -> GreekTranslations.tagFeedback_audiobookRequiredFields_variant_1 to GreekTranslations.tagFeedback_audiobookRequiredFields_variant_2
    AppLanguage.Hindi -> HindiTranslations.tagFeedback_audiobookRequiredFields_variant_1 to HindiTranslations.tagFeedback_audiobookRequiredFields_variant_2
    AppLanguage.Hungarian -> HungarianTranslations.tagFeedback_audiobookRequiredFields_variant_1 to HungarianTranslations.tagFeedback_audiobookRequiredFields_variant_2
    AppLanguage.Indonesian -> IndonesianTranslations.tagFeedback_audiobookRequiredFields_variant_1 to IndonesianTranslations.tagFeedback_audiobookRequiredFields_variant_2
    AppLanguage.Italian -> ItalianTranslations.tagFeedback_audiobookRequiredFields_variant_1 to ItalianTranslations.tagFeedback_audiobookRequiredFields_variant_2
    AppLanguage.Japanese -> JapaneseTranslations.tagFeedback_audiobookRequiredFields_variant_1 to JapaneseTranslations.tagFeedback_audiobookRequiredFields_variant_2
    AppLanguage.Korean -> KoreanTranslations.tagFeedback_audiobookRequiredFields_variant_1 to KoreanTranslations.tagFeedback_audiobookRequiredFields_variant_2
    AppLanguage.Latvian -> LatvianTranslations.tagFeedback_audiobookRequiredFields_variant_1 to LatvianTranslations.tagFeedback_audiobookRequiredFields_variant_2
    AppLanguage.Lithuanian -> LithuanianTranslations.tagFeedback_audiobookRequiredFields_variant_1 to LithuanianTranslations.tagFeedback_audiobookRequiredFields_variant_2
    AppLanguage.Macedonian -> MacedonianTranslations.tagFeedback_audiobookRequiredFields_variant_1 to MacedonianTranslations.tagFeedback_audiobookRequiredFields_variant_2
    AppLanguage.Malay -> MalayTranslations.tagFeedback_audiobookRequiredFields_variant_1 to MalayTranslations.tagFeedback_audiobookRequiredFields_variant_2
    AppLanguage.Norwegian -> NorwegianTranslations.tagFeedback_audiobookRequiredFields_variant_1 to NorwegianTranslations.tagFeedback_audiobookRequiredFields_variant_2
    AppLanguage.Polish -> PolishTranslations.tagFeedback_audiobookRequiredFields_variant_1 to PolishTranslations.tagFeedback_audiobookRequiredFields_variant_2
    AppLanguage.Portuguese -> PortugueseTranslations.tagFeedback_audiobookRequiredFields_variant_1 to PortugueseTranslations.tagFeedback_audiobookRequiredFields_variant_2
    AppLanguage.Punjabi -> PunjabiTranslations.tagFeedback_audiobookRequiredFields_variant_1 to PunjabiTranslations.tagFeedback_audiobookRequiredFields_variant_2
    AppLanguage.Romanian -> RomanianTranslations.tagFeedback_audiobookRequiredFields_variant_1 to RomanianTranslations.tagFeedback_audiobookRequiredFields_variant_2
    AppLanguage.Russian -> RussianTranslations.tagFeedback_audiobookRequiredFields_variant_1 to RussianTranslations.tagFeedback_audiobookRequiredFields_variant_2
    AppLanguage.Serbian -> SerbianTranslations.tagFeedback_audiobookRequiredFields_variant_1 to SerbianTranslations.tagFeedback_audiobookRequiredFields_variant_2
    AppLanguage.Slovak -> SlovakTranslations.tagFeedback_audiobookRequiredFields_variant_1 to SlovakTranslations.tagFeedback_audiobookRequiredFields_variant_2
    AppLanguage.Slovenian -> SlovenianTranslations.tagFeedback_audiobookRequiredFields_variant_1 to SlovenianTranslations.tagFeedback_audiobookRequiredFields_variant_2
    AppLanguage.Spanish -> SpanishTranslations.tagFeedback_audiobookRequiredFields_variant_1 to SpanishTranslations.tagFeedback_audiobookRequiredFields_variant_2
    AppLanguage.Swedish -> SwedishTranslations.tagFeedback_audiobookRequiredFields_variant_1 to SwedishTranslations.tagFeedback_audiobookRequiredFields_variant_2
    AppLanguage.Thai -> ThaiTranslations.tagFeedback_audiobookRequiredFields_variant_1 to ThaiTranslations.tagFeedback_audiobookRequiredFields_variant_2
    AppLanguage.Ukrainian -> UkrainianTranslations.tagFeedback_audiobookRequiredFields_variant_1 to UkrainianTranslations.tagFeedback_audiobookRequiredFields_variant_2
    AppLanguage.Urdu -> UrduTranslations.tagFeedback_audiobookRequiredFields_variant_1 to UrduTranslations.tagFeedback_audiobookRequiredFields_variant_2
}

internal fun localizedTagValidationError(issue: TagValidationIssue, language: AppLanguage): String {
    val message = when (issue.kind) {
        TagValidationKind.TrackArtist -> trackArtistAndChapterTitleErrors(language).first
        TagValidationKind.ChapterTitle -> trackArtistAndChapterTitleErrors(language).second
        TagValidationKind.TrackTitle -> trackTitleError(language)
        TagValidationKind.TrackNumbers -> trackNumbersError(language)
        TagValidationKind.AlbumTitle -> albumRequiredFields(language).first
        TagValidationKind.AlbumArtist -> albumRequiredFields(language).second
        TagValidationKind.BookTitle -> audiobookRequiredFields(language).first
        TagValidationKind.Author -> audiobookRequiredFields(language).second
        TagValidationKind.ChapterAuthor -> if (language == AppLanguage.English) issue.kind.english else audiobookRequiredFields(language).second
        TagValidationKind.ReleaseYear -> when (language) {
        AppLanguage.Albanian -> AlbanianTranslations.tagFeedback_localizedTagValidationError_1
        AppLanguage.Bengali -> BengaliTranslations.tagFeedback_localizedTagValidationError_1
        AppLanguage.Bosnian -> BosnianTranslations.tagFeedback_localizedTagValidationError_1
        AppLanguage.Bulgarian -> BulgarianTranslations.tagFeedback_localizedTagValidationError_1
        AppLanguage.Catalan -> CatalanTranslations.tagFeedback_localizedTagValidationError_1
        AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.tagFeedback_localizedTagValidationError_1
        AppLanguage.Croatian -> CroatianTranslations.tagFeedback_localizedTagValidationError_1
        AppLanguage.Czech -> CzechTranslations.tagFeedback_localizedTagValidationError_1
        AppLanguage.Danish -> DanishTranslations.tagFeedback_localizedTagValidationError_1
        AppLanguage.Dutch -> DutchTranslations.tagFeedback_localizedTagValidationError_1
        AppLanguage.English -> issue.kind.english
        AppLanguage.Estonian -> EstonianTranslations.tagFeedback_localizedTagValidationError_1
        AppLanguage.Finnish -> FinnishTranslations.tagFeedback_localizedTagValidationError_1
        AppLanguage.French -> FrenchTranslations.tagFeedback_localizedTagValidationError_1
        AppLanguage.German -> GermanTranslations.tagFeedback_localizedTagValidationError_1
        AppLanguage.Greek -> GreekTranslations.tagFeedback_localizedTagValidationError_1
        AppLanguage.Hindi -> HindiTranslations.tagFeedback_localizedTagValidationError_1
        AppLanguage.Hungarian -> HungarianTranslations.tagFeedback_localizedTagValidationError_1
        AppLanguage.Indonesian -> IndonesianTranslations.tagFeedback_localizedTagValidationError_1
        AppLanguage.Italian -> ItalianTranslations.tagFeedback_localizedTagValidationError_1
        AppLanguage.Japanese -> JapaneseTranslations.tagFeedback_localizedTagValidationError_1
        AppLanguage.Korean -> KoreanTranslations.tagFeedback_localizedTagValidationError_1
        AppLanguage.Latvian -> LatvianTranslations.tagFeedback_localizedTagValidationError_1
        AppLanguage.Lithuanian -> LithuanianTranslations.tagFeedback_localizedTagValidationError_1
        AppLanguage.Macedonian -> MacedonianTranslations.tagFeedback_localizedTagValidationError_1
        AppLanguage.Malay -> MalayTranslations.tagFeedback_localizedTagValidationError_1
        AppLanguage.Norwegian -> NorwegianTranslations.tagFeedback_localizedTagValidationError_1
        AppLanguage.Polish -> PolishTranslations.tagFeedback_localizedTagValidationError_1
        AppLanguage.Portuguese -> PortugueseTranslations.tagFeedback_localizedTagValidationError_1
        AppLanguage.Punjabi -> PunjabiTranslations.tagFeedback_localizedTagValidationError_1
        AppLanguage.Romanian -> RomanianTranslations.tagFeedback_localizedTagValidationError_1
        AppLanguage.Russian -> RussianTranslations.tagFeedback_localizedTagValidationError_1
        AppLanguage.Serbian -> SerbianTranslations.tagFeedback_localizedTagValidationError_1
        AppLanguage.Slovak -> SlovakTranslations.tagFeedback_localizedTagValidationError_1
        AppLanguage.Slovenian -> SlovenianTranslations.tagFeedback_localizedTagValidationError_1
        AppLanguage.Spanish -> SpanishTranslations.tagFeedback_localizedTagValidationError_1
        AppLanguage.Swedish -> SwedishTranslations.tagFeedback_localizedTagValidationError_1
        AppLanguage.Thai -> ThaiTranslations.tagFeedback_localizedTagValidationError_1
        AppLanguage.Ukrainian -> UkrainianTranslations.tagFeedback_localizedTagValidationError_1
        AppLanguage.Urdu -> UrduTranslations.tagFeedback_localizedTagValidationError_1
        }
    }
    return issue.fileName?.let { "$it: $message" } ?: message
}

internal data class TagAdditionalIssuesCopy(val validation: String, val save: String)

internal fun tagAdditionalIssuesCopy(language: AppLanguage): TagAdditionalIssuesCopy = when (language) {
    AppLanguage.Albanian -> AlbanianTranslations.tagFeedback_tagAdditionalIssuesCopy_1
    AppLanguage.Bengali -> BengaliTranslations.tagFeedback_tagAdditionalIssuesCopy_1
    AppLanguage.Bosnian -> BosnianTranslations.tagFeedback_tagAdditionalIssuesCopy_1
    AppLanguage.Bulgarian -> BulgarianTranslations.tagFeedback_tagAdditionalIssuesCopy_1
    AppLanguage.Catalan -> CatalanTranslations.tagFeedback_tagAdditionalIssuesCopy_1
    AppLanguage.ChineseSimplified -> ChineseSimplifiedTranslations.tagFeedback_tagAdditionalIssuesCopy_1
    AppLanguage.Croatian -> CroatianTranslations.tagFeedback_tagAdditionalIssuesCopy_1
    AppLanguage.Czech -> CzechTranslations.tagFeedback_tagAdditionalIssuesCopy_1
    AppLanguage.Danish -> DanishTranslations.tagFeedback_tagAdditionalIssuesCopy_1
    AppLanguage.Dutch -> DutchTranslations.tagFeedback_tagAdditionalIssuesCopy_1
    AppLanguage.English -> EnglishTranslations.tagFeedback_tagAdditionalIssuesCopy_1
    AppLanguage.Estonian -> EstonianTranslations.tagFeedback_tagAdditionalIssuesCopy_1
    AppLanguage.Finnish -> FinnishTranslations.tagFeedback_tagAdditionalIssuesCopy_1
    AppLanguage.French -> FrenchTranslations.tagFeedback_tagAdditionalIssuesCopy_1
    AppLanguage.German -> GermanTranslations.tagFeedback_tagAdditionalIssuesCopy_1
    AppLanguage.Greek -> GreekTranslations.tagFeedback_tagAdditionalIssuesCopy_1
    AppLanguage.Hindi -> HindiTranslations.tagFeedback_tagAdditionalIssuesCopy_1
    AppLanguage.Hungarian -> HungarianTranslations.tagFeedback_tagAdditionalIssuesCopy_1
    AppLanguage.Indonesian -> IndonesianTranslations.tagFeedback_tagAdditionalIssuesCopy_1
    AppLanguage.Italian -> ItalianTranslations.tagFeedback_tagAdditionalIssuesCopy_1
    AppLanguage.Japanese -> JapaneseTranslations.tagFeedback_tagAdditionalIssuesCopy_1
    AppLanguage.Korean -> KoreanTranslations.tagFeedback_tagAdditionalIssuesCopy_1
    AppLanguage.Latvian -> LatvianTranslations.tagFeedback_tagAdditionalIssuesCopy_1
    AppLanguage.Lithuanian -> LithuanianTranslations.tagFeedback_tagAdditionalIssuesCopy_1
    AppLanguage.Macedonian -> MacedonianTranslations.tagFeedback_tagAdditionalIssuesCopy_1
    AppLanguage.Malay -> MalayTranslations.tagFeedback_tagAdditionalIssuesCopy_1
    AppLanguage.Norwegian -> NorwegianTranslations.tagFeedback_tagAdditionalIssuesCopy_1
    AppLanguage.Polish -> PolishTranslations.tagFeedback_tagAdditionalIssuesCopy_1
    AppLanguage.Portuguese -> PortugueseTranslations.tagFeedback_tagAdditionalIssuesCopy_1
    AppLanguage.Punjabi -> PunjabiTranslations.tagFeedback_tagAdditionalIssuesCopy_1
    AppLanguage.Romanian -> RomanianTranslations.tagFeedback_tagAdditionalIssuesCopy_1
    AppLanguage.Russian -> RussianTranslations.tagFeedback_tagAdditionalIssuesCopy_1
    AppLanguage.Serbian -> SerbianTranslations.tagFeedback_tagAdditionalIssuesCopy_1
    AppLanguage.Slovak -> SlovakTranslations.tagFeedback_tagAdditionalIssuesCopy_1
    AppLanguage.Slovenian -> SlovenianTranslations.tagFeedback_tagAdditionalIssuesCopy_1
    AppLanguage.Spanish -> SpanishTranslations.tagFeedback_tagAdditionalIssuesCopy_1
    AppLanguage.Swedish -> SwedishTranslations.tagFeedback_tagAdditionalIssuesCopy_1
    AppLanguage.Thai -> ThaiTranslations.tagFeedback_tagAdditionalIssuesCopy_1
    AppLanguage.Ukrainian -> UkrainianTranslations.tagFeedback_tagAdditionalIssuesCopy_1
    AppLanguage.Urdu -> UrduTranslations.tagFeedback_tagAdditionalIssuesCopy_1
}
