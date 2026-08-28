package elovaire.music.droidbeauty.app.data.library

import android.content.ContentResolver
import android.content.ContentValues
import android.database.MatrixCursor
import android.net.Uri
import android.provider.MediaStore
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MediaStoreScannerInstrumentedTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val resolver: ContentResolver = context.contentResolver
    private val insertedUris = mutableListOf<Uri>()

    @After
    fun tearDown() {
        insertedUris.forEach { uri -> runCatching { resolver.delete(uri, null, null) } }
    }

    @Test
    fun appOwnedMusicRowSurvivesDirectQueryAndScanner() {
        val fixtureName = "elovaire-scan-${System.nanoTime()}.wav"
        val uri = insertWav(fixtureName)

        val directRows = MediaStoreAudioQuery.query(resolver).cursor.use { cursor ->
            val nameIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            buildList {
                while (cursor.moveToNext()) add(cursor.getString(nameIndex))
            }
        }
        assertTrue(fixtureName in directRows)

        val snapshot = runBlocking {
            MediaStoreScanner(context).scan(
                refreshMediaIndex = false,
                enrichMetadata = false,
            )
        }

        assertTrue("MediaStore fixture was not included: $uri", snapshot.songs.any { song ->
            song.fileName == fixtureName
        })
    }

    @Test
    fun indexRepairFailureDoesNotSuppressMediaStoreRows() {
        val fixtureName = "elovaire-scan-failure-${System.nanoTime()}.wav"
        insertWav(fixtureName)

        val snapshot = runBlocking {
            MediaStoreScanner(
                context = context,
                indexRefresher = object : MediaStoreIndexRefresher {
                    override fun refreshAll(shouldContinue: () -> Boolean): MediaStoreIndexRefreshResult =
                        MediaStoreIndexRefreshResult.Unavailable(IllegalStateException("test index failure"))

                    override fun refreshPaths(
                        paths: List<String>,
                        shouldContinue: () -> Boolean,
                    ): MediaStoreIndexRefreshResult =
                        MediaStoreIndexRefreshResult.Unavailable(IllegalStateException("test index failure"))
                },
            ).scan(
                refreshMediaIndex = true,
                enrichMetadata = false,
            )
        }

        assertTrue(snapshot.songs.any { song -> song.fileName == fixtureName })
        assertTrue(ScannerDebugLogger.latestDiagnosticSnapshot()?.indexRefresh?.startsWith("Unavailable:") == true)
    }

    @Test
    fun minimalProviderProjectionKeepsAudioRowDiscoverable() {
        val cursor = MatrixCursor(
            arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
            ),
        ).apply {
            addRow(arrayOf<Any?>(42L, "missing-metadata.mp3"))
        }

        val row = cursor.use {
            assertTrue(it.moveToFirst())
            MediaStoreAudioRowMapper(context, it).row(it)
        }

        assertEquals(0L, row.durationMs)
        assertNull(row.relativePath)
        assertNull(row.volumeName)
        assertTrue(
            LibraryAudioFileFilter(
                selectedRelativeRoots = setOf("music"),
                libraryRootPaths = emptySet(),
                allowUnscopedMediaStoreRows = true,
            ).evaluate(AudioScanCandidateMapper.toCandidate(row, detectedFormat = null))
                is AudioFileFilterDecision.Include,
        )
    }

    private fun insertWav(fileName: String): Uri {
        val uri = resolver.insert(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            ContentValues().apply {
                put(MediaStore.Audio.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Audio.Media.MIME_TYPE, "audio/wav")
                put(MediaStore.Audio.Media.RELATIVE_PATH, "Music/ElovaireScanTest")
                put(MediaStore.Audio.Media.IS_MUSIC, 1)
                put(MediaStore.Audio.Media.IS_PENDING, 1)
            },
        ) ?: error("Unable to insert MediaStore fixture.")
        insertedUris += uri
        resolver.openOutputStream(uri)?.use { output -> output.write(wavBytes()) }
            ?: error("Unable to write MediaStore fixture.")
        resolver.update(
            uri,
            ContentValues().apply { put(MediaStore.Audio.Media.IS_PENDING, 0) },
            null,
            null,
        )
        return uri
    }

    private fun wavBytes(): ByteArray {
        val sampleRate = 8_000
        val samples = sampleRate / 4
        val pcm = ByteArray(samples * 2)
        for (index in 0 until samples) {
            val sample = (kotlin.math.sin(index * 2.0 * Math.PI * 440.0 / sampleRate) * 10_000.0).toInt()
            pcm[index * 2] = sample.toByte()
            pcm[index * 2 + 1] = (sample shr 8).toByte()
        }
        return ByteArrayOutputStream(44 + pcm.size).apply {
            writeAscii("RIFF")
            writeLittleEndianInt(36 + pcm.size)
            writeAscii("WAVEfmt ")
            writeLittleEndianInt(16)
            writeLittleEndianShort(1)
            writeLittleEndianShort(1)
            writeLittleEndianInt(sampleRate)
            writeLittleEndianInt(sampleRate * 2)
            writeLittleEndianShort(2)
            writeLittleEndianShort(16)
            writeAscii("data")
            writeLittleEndianInt(pcm.size)
            write(pcm)
        }.toByteArray()
    }

    private fun ByteArrayOutputStream.writeAscii(value: String) {
        write(value.toByteArray(Charsets.US_ASCII))
    }

    private fun ByteArrayOutputStream.writeLittleEndianShort(value: Int) {
        write(value and 0xff)
        write((value ushr 8) and 0xff)
    }

    private fun ByteArrayOutputStream.writeLittleEndianInt(value: Int) {
        write(value and 0xff)
        write((value ushr 8) and 0xff)
        write((value ushr 16) and 0xff)
        write((value ushr 24) and 0xff)
    }

}
