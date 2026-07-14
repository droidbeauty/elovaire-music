package elovaire.music.droidbeauty.app.ui.screens

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FastScrollbarTest {
    @Test
    fun visualChromeIsThinnerThanTouchTarget() {
        assertEquals(40f, FastScrollbarTouchWidth.value)
        assertEquals(1f, FastScrollbarTrackWidth.value)
        assertEquals(3f, FastScrollbarThumbWidth.value)
        assertTrue(FastScrollbarThumbWidth < FastScrollbarTouchWidth)
        assertTrue(FastScrollbarTrackWidth < FastScrollbarTouchWidth)
    }

    @Test
    fun minimumThumbHeightIsPreserved() {
        assertEquals(40f, FastScrollbarMinThumbHeight.value)
    }

    @Test
    fun thumbUsesTravelInsteadOfFullTrack() {
        val layout = scrollbarLayout(
            trackLengthPx = 1000f,
            minThumbPx = 40f,
            scrollFraction = 1f,
            viewportFraction = 0.2f,
        )

        assertEquals(200f, layout.thumbLengthPx)
        assertEquals(800f, layout.thumbOffsetPx)
    }

    @Test
    fun pointerMappingPreservesGrabOffset() {
        val layout = scrollbarLayout(1000f, 40f, 0.5f, 0.2f)

        assertEquals(0.5f, scrollbarFractionForPointer(450f, layout, 50f), 0.0001f)
        assertEquals(0f, scrollbarFractionForPointer(-50f, layout), 0f)
        assertEquals(1f, scrollbarFractionForPointer(1200f, layout), 0f)
    }

    @Test
    fun estimatorConvergesForVariableRowsAndSpacing() {
        val estimator = LazyItemSizeEstimator()
        estimator.prepare(totalItems = 4, itemSpacingPx = 10)
        estimator.record(0, 100, "a")
        estimator.record(1, 200, "b")
        estimator.record(2, 100, "c")
        estimator.record(3, 200, "d")

        assertEquals(630f, estimator.estimatedTotalPx(), 0f)
        assertEquals(320f, estimator.estimatedPrefixPx(2), 0f)
        assertEquals(EstimatedItemPosition(1, 40), estimator.positionForOffset(150f))
    }

    @Test
    fun estimatorResetsWhenDatasetKeyChanges() {
        val estimator = LazyItemSizeEstimator()
        estimator.prepare(totalItems = 2, itemSpacingPx = 0)
        estimator.record(0, 100, "old")
        estimator.record(1, 300, "second")
        estimator.record(0, 50, "new")

        assertEquals(100f, estimator.estimatedTotalPx(), 0f)
    }
}
