package elovaire.music.droidbeauty.app.data.library.network

internal data class NetworkRangeResolution(
    val availableBytes: Long,
    val exposedLength: Long,
)

internal fun resolveNetworkRange(
    totalLength: Long,
    position: Long,
    requestedLength: Long,
): NetworkRangeResolution {
    require(totalLength >= 0L)
    require(position >= 0L)
    require(requestedLength == -1L || requestedLength >= 0L)
    if (position > totalLength) throw NetworkRangeException("Network read position is outside the resource")
    val availableBytes = totalLength - position
    return NetworkRangeResolution(
        availableBytes = availableBytes,
        exposedLength = if (requestedLength >= 0L) requestedLength else availableBytes,
    )
}
