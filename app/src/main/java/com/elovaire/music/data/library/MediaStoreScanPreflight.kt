package elovaire.music.droidbeauty.app.data.library

import elovaire.music.droidbeauty.app.data.audio.AudioFormatPolicy

/** Rejects rows that need no container inspection before opening a MediaExtractor. */
internal object MediaStoreScanPreflight {
    fun rejectionBeforeContainerDetection(
        candidate: AudioScanCandidate,
        filter: LibraryAudioFileFilter,
    ): AudioFileFilterDecision.Exclude? {
        if (AudioFormatPolicy.requiresContainerValidation(candidate.extension)) return null
        val fastCandidate = candidate.copy(
            detectedFormat = AudioScanCandidateMapper.fastDetectedFormat(
                extension = candidate.extension.orEmpty(),
                mimeType = candidate.mimeType,
            ),
        )
        return filter.evaluate(fastCandidate) as? AudioFileFilterDecision.Exclude
    }
}
