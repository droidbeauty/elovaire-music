package elovaire.music.droidbeauty.app.data.library

import android.content.SharedPreferences
import java.lang.reflect.Proxy
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DeviceDeletePendingStateTest {
    @Test
    fun failedOrUnavailableLaunchClearsThePreparedOperation() = runBlocking {
        var aborts = 0
        launchPreparedDelete(createRequest = { null as String? }, launch = {}, onAborted = { aborts++ })
        org.junit.Assert.assertThrows(IllegalStateException::class.java) {
            runBlocking {
                launchPreparedDelete(createRequest = { "request" }, launch = { error("launcher failed") }, onAborted = { aborts++ })
            }
        }
        launchPreparedDelete(createRequest = { "request" }, launch = {}, onAborted = { aborts++ })
        assertEquals(2, aborts)
    }

    @Test
    fun secondDeleteCannotReplaceAnUnfinishedOperation() {
        val values = mutableMapOf<String, String>()
        val editor = Proxy.newProxyInstance(javaClass.classLoader, arrayOf(SharedPreferences.Editor::class.java)) { proxy, method, args ->
            when (method.name) {
                "putString" -> { values[args[0] as String] = args[1] as String; proxy }
                "commit" -> true
                else -> error("Unexpected editor method ${method.name}")
            }
        } as SharedPreferences.Editor
        val preferences = Proxy.newProxyInstance(javaClass.classLoader, arrayOf(SharedPreferences::class.java)) { _, method, args ->
            when (method.name) {
                "contains" -> values.containsKey(args[0])
                "edit" -> editor
                else -> error("Unexpected preferences method ${method.name}")
            }
        } as SharedPreferences

        assertTrue(persistPendingDeleteState(preferences, "first", "first-plan"))
        assertFalse(persistPendingDeleteState(preferences, "second", "second-plan"))
        assertEquals("first", values["pending_device_delete_operation_id"])
        assertEquals("first-plan", values["pending_device_delete_plan"])
    }
}
