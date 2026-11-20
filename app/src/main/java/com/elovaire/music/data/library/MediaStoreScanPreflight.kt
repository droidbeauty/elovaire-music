package elovaire.music.droidbeauty.app.data.library

import elovaire.music.droidbeauty.app.data.audio.AudioFormatPolicy

/** Rejects rows that need no container inspection before opening a MediaExtractor. */
internal object MediaStoreScanPreflight {
    data class Result(val candidate: AudioScanCandidate, val decision: AudioFileFilterDecision)

    fun evaluateBeforeContainerDetection(
        candidate: AudioScanCandidate,
        filter: LibraryAudioFileFilter,
    ): Result? {
        if (
            candidate.extension.isNullOrBlank() ||
            candidate.extension !in AudioFormatPolicy.scannerExtensions ||
            AudioFormatPolicy.requiresContainerValidation(candidate.extension)
        ) return null
        val fastCandidate = candidate.copy(
            detectedFormat = AudioScanCandidateMapper.fastDetectedFormat(
                extension = candidate.extension.orEmpty(),
                mimeType = candidate.mimeType,
            ),
        )
        return Result(fastCandidate, filter.evaluate(fastCandidate))
    }
}
