package net.spacealtctrl.discordrp.settings

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Exercises the storage rules against a dedicated preferences file. The live
 * session in "stash" is never written, so a killed test process cannot sign the
 * developer out.
 */
@RunWith(AndroidJUnit4::class)
class TokenStorageTest {
    private val context: Context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    private val prefs
        get() = context.getSharedPreferences(TEST_PREFS, Context.MODE_PRIVATE)

    private fun stash() = Stash.isolated(context, TEST_PREFS)

    @Before
    fun resetStore() {
        context.deleteSharedPreferences(TEST_PREFS)
    }

    @After
    fun removeStore() {
        // Flush any apply() still queued, then drop the file so test tokens do
        // not linger between runs.
        prefs.edit().clear().commit()
        context.deleteSharedPreferences(TEST_PREFS)
    }

    @Test
    fun legacyTokenIsMigrated() {
        prefs.edit().putString("discord.token", "legacy-token").commit()

        val stash = stash()
        assertEquals("legacy-token", stash.authToken)
        assertNull(prefs.getString("discord.token", null))
        assertNotNull(prefs.getString("discord.token.enc", null))
    }

    @Test
    fun tokenIsSealedAndNeverLeftInPlaintext() {
        val stash = stash()
        stash.authToken = "smoke-token"

        assertEquals("smoke-token", stash.authToken)
        assertNull(prefs.getString("discord.token", null))
        assertNotNull(prefs.getString("discord.token.enc", null))
    }

    @Test
    fun clearingTokenRemovesBothStoredKeys() {
        val stash = stash()
        stash.authToken = "clear-me"
        stash.authToken = ""

        assertFalse(stash.signedIn)
        assertNull(prefs.getString("discord.token", null))
        assertNull(prefs.getString("discord.token.enc", null))
    }

    @Test
    fun isolatedStoreLeavesTheLiveSessionAlone() {
        val live = context.getSharedPreferences("stash", Context.MODE_PRIVATE)
        val sealed = live.getString("discord.token.enc", null)

        stash().authToken = "isolated-token"

        assertEquals(sealed, live.getString("discord.token.enc", null))
    }

    private companion object {
        const val TEST_PREFS = "stash-test"
    }
}
