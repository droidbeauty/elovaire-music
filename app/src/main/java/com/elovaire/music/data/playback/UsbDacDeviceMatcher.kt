package elovaire.music.droidbeauty.app.data.playback

internal data class UsbDacMatchCandidate(
    val deviceId: Int,
    val deviceName: String,
    val productName: String?,
    val manufacturerName: String?,
    val vendorId: Int,
    val productId: Int,
    val audioInterfaceCount: Int,
)

internal sealed interface UsbDacMatchResult {
    data class Matched(val deviceId: Int) : UsbDacMatchResult

    data class Ambiguous(val candidateDeviceIds: List<Int>) : UsbDacMatchResult

    data object NoCandidates : UsbDacMatchResult

    data object NoConfidentMatch : UsbDacMatchResult
}

internal object UsbDacDeviceMatcher {
    fun match(
        descriptor: UsbAudioDeviceDescriptor,
        candidates: List<UsbDacMatchCandidate>,
    ): UsbDacMatchResult {
        if (candidates.isEmpty()) return UsbDacMatchResult.NoCandidates
        if (candidates.size == 1) return UsbDacMatchResult.Matched(candidates.first().deviceId)

        val normalizedProductName = descriptor.productName.normalizedOrNull()
        val normalizedAddress = descriptor.address.normalizedOrNull()
        val scored = candidates.map { candidate ->
            candidate to scoreCandidate(
                candidate = candidate,
                normalizedProductName = normalizedProductName,
                normalizedAddress = normalizedAddress,
            )
        }
        val bestScore = scored.maxOf { it.second }
        if (bestScore <= 0) {
            return UsbDacMatchResult.NoConfidentMatch
        }
        val bestCandidates = scored
            .filter { it.second == bestScore }
            .map { it.first.deviceId }
            .distinct()
        return if (bestCandidates.size == 1) {
            UsbDacMatchResult.Matched(bestCandidates.first())
        } else {
            UsbDacMatchResult.Ambiguous(bestCandidates)
        }
    }

    private fun scoreCandidate(
        candidate: UsbDacMatchCandidate,
        normalizedProductName: String?,
        normalizedAddress: String?,
    ): Int {
        var score = 0
        val candidateProductName = candidate.productName.normalizedOrNull()
        val candidateManufacturerName = candidate.manufacturerName.normalizedOrNull()
        val candidateDeviceName = candidate.deviceName.normalizedOrNull()

        if (normalizedAddress != null && candidateDeviceName == normalizedAddress) {
            score += 500
        }
        if (normalizedProductName != null && candidateProductName == normalizedProductName) {
            score += 200
        }
        if (
            normalizedProductName != null &&
            candidateProductName != null &&
            candidateProductName != normalizedProductName &&
            (
                candidateProductName.contains(normalizedProductName) ||
                    normalizedProductName.contains(candidateProductName)
                )
        ) {
            score += 50
        }
        if (
            normalizedProductName != null &&
            candidateManufacturerName != null &&
            (
                normalizedProductName.contains(candidateManufacturerName) ||
                    candidateManufacturerName.contains(normalizedProductName)
                )
        ) {
            score += 15
        }
        if (candidate.vendorId > 0 && candidate.productId > 0) {
            score += 5
        }
        if (candidate.audioInterfaceCount > 1) {
            score += 1
        }
        return score
    }

    private fun String?.normalizedOrNull(): String? {
        return this
            ?.trim()
            ?.lowercase()
            ?.takeIf { it.isNotBlank() }
    }
}
