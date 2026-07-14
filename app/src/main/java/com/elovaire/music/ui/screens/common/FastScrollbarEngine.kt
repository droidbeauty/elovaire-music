package elovaire.music.droidbeauty.app.ui.screens

import androidx.compose.runtime.Immutable
import kotlin.math.max

@Immutable
internal data class ScrollbarLayout(
    val trackLengthPx: Float,
    val thumbLengthPx: Float,
    val thumbOffsetPx: Float,
    val scrollFraction: Float,
    val viewportFraction: Float,
) {
    val thumbTravelPx: Float
        get() = (trackLengthPx - thumbLengthPx).coerceAtLeast(0f)
}

internal fun scrollbarLayout(
    trackLengthPx: Float,
    minThumbPx: Float,
    scrollFraction: Float,
    viewportFraction: Float,
): ScrollbarLayout {
    val track = trackLengthPx.coerceAtLeast(0f)
    val thumb = max(minThumbPx.coerceAtLeast(0f), track * viewportFraction.coerceIn(0f, 1f))
        .coerceAtMost(track)
    val fraction = scrollFraction.coerceIn(0f, 1f)
    return ScrollbarLayout(
        trackLengthPx = track,
        thumbLengthPx = thumb,
        thumbOffsetPx = fraction * (track - thumb).coerceAtLeast(0f),
        scrollFraction = fraction,
        viewportFraction = viewportFraction.coerceIn(0f, 1f),
    )
}

internal fun scrollbarFractionForPointer(
    pointerYPx: Float,
    layout: ScrollbarLayout,
    grabOffsetPx: Float = layout.thumbLengthPx / 2f,
): Float {
    if (layout.thumbTravelPx <= 0f) return 0f
    return ((pointerYPx - grabOffsetPx) / layout.thumbTravelPx).coerceIn(0f, 1f)
}

internal data class EstimatedItemPosition(
    val index: Int,
    val offsetPx: Int,
)

internal class LazyItemSizeEstimator {
    private var itemCount = 0
    private var spacingPx = 0
    private var sizes = IntArray(0)
    private var keyHashes = IntArray(0)
    private var recorded = BooleanArray(0)
    private var sizeTree = LongArray(1)
    private var countTree = IntArray(1)
    private var measuredSizeTotal = 0L
    private var measuredCount = 0

    fun prepare(totalItems: Int, itemSpacingPx: Int) {
        val count = totalItems.coerceAtLeast(0)
        if (count != itemCount) reset(count)
        spacingPx = itemSpacingPx.coerceAtLeast(0)
    }

    fun record(index: Int, sizePx: Int, key: Any? = null) {
        if (index !in 0 until itemCount || sizePx <= 0) return
        val keyHash = key?.hashCode() ?: 0
        if (recorded[index] && key != null && keyHashes[index] != keyHash) {
            reset(itemCount)
        }
        val oldSize = sizes[index]
        if (!recorded[index]) {
            recorded[index] = true
            keyHashes[index] = keyHash
            measuredCount++
            add(countTree, index, 1)
        } else if (oldSize == sizePx) {
            return
        }
        sizes[index] = sizePx
        measuredSizeTotal += (sizePx - oldSize).toLong()
        add(sizeTree, index, (sizePx - oldSize).toLong())
    }

    fun estimatedPrefixPx(index: Int): Float {
        val boundedIndex = index.coerceIn(0, itemCount)
        val knownCount = sum(countTree, boundedIndex)
        val knownSize = sum(sizeTree, boundedIndex)
        val unknownCount = boundedIndex - knownCount
        return knownSize + unknownCount * averageSizePx() + boundedIndex * spacingPx
    }

    fun estimatedTotalPx(): Float {
        if (itemCount == 0) return 0f
        return estimatedPrefixPx(itemCount) - spacingPx
    }

    fun positionForOffset(offsetPx: Float): EstimatedItemPosition {
        if (itemCount == 0) return EstimatedItemPosition(0, 0)
        val target = offsetPx.coerceAtLeast(0f)
        var low = 0
        var high = itemCount
        while (low < high) {
            val middle = (low + high + 1) ushr 1
            if (estimatedPrefixPx(middle) <= target) low = middle else high = middle - 1
        }
        val index = low.coerceAtMost(itemCount - 1)
        return EstimatedItemPosition(
            index = index,
            offsetPx = (target - estimatedPrefixPx(index)).toInt().coerceAtLeast(0),
        )
    }

    private fun averageSizePx(): Float {
        return if (measuredCount == 0) 1f else measuredSizeTotal.toFloat() / measuredCount
    }

    private fun reset(count: Int) {
        itemCount = count
        sizes = IntArray(count)
        keyHashes = IntArray(count)
        recorded = BooleanArray(count)
        sizeTree = LongArray(count + 1)
        countTree = IntArray(count + 1)
        measuredSizeTotal = 0L
        measuredCount = 0
    }

    private fun add(tree: LongArray, index: Int, delta: Long) {
        var position = index + 1
        while (position < tree.size) {
            tree[position] += delta
            position += position and -position
        }
    }

    private fun add(tree: IntArray, index: Int, delta: Int) {
        var position = index + 1
        while (position < tree.size) {
            tree[position] += delta
            position += position and -position
        }
    }

    private fun sum(tree: LongArray, endExclusive: Int): Long {
        var position = endExclusive
        var result = 0L
        while (position > 0) {
            result += tree[position]
            position -= position and -position
        }
        return result
    }

    private fun sum(tree: IntArray, endExclusive: Int): Int {
        var position = endExclusive
        var result = 0
        while (position > 0) {
            result += tree[position]
            position -= position and -position
        }
        return result
    }
}
