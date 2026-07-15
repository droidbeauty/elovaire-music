package elovaire.music.droidbeauty.app.data.update

import java.net.URL
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateDownloadUrlPolicyTest {
    @Test
    fun acceptsOnlyHttpsGithubDownloadHosts() {
        assertTrue(isTrustedUpdateDownloadUrl(URL("https://github.com/example/release.apk")))
        assertTrue(isTrustedUpdateDownloadUrl(URL("https://objects.githubusercontent.com/release.apk")))
        assertFalse(isTrustedUpdateDownloadUrl(URL("http://github.com/release.apk")))
        assertFalse(isTrustedUpdateDownloadUrl(URL("https://evilgithub.com/release.apk")))
        assertFalse(isTrustedUpdateDownloadUrl(URL("https://example.com/release.apk")))
    }
}
