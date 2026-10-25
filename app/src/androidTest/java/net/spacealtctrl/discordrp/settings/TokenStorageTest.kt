package net.spacealtctrl.discordrp.settings

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters

@RunWith(AndroidJUnit4::class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class TokenStorageTest {
    @Test
    fun a_legacyTokenIsMigrated() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val prefs = context.getSharedPreferences("stash", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("discord.token", "legacy-token")
            .remove("discord.token.enc")
            .commit()

        val stash = Stash.of(context)
        assertEquals("legacy-token", stash.authToken)
        assertNull(prefs.getString("discord.token", null))
        assertNotNull(prefs.getString("discord.token.enc", null))
        stash.authToken = ""
    }

    @Test
    fun b_tokenIsSealedAndNeverLeftInPlaintext() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val stash = Stash.of(context)
        val previous = stash.authToken
        try {
            stash.authToken = "smoke-token"
            assertEquals("smoke-token", stash.authToken)

            val prefs = context.getSharedPreferences("stash", Context.MODE_PRIVATE)
            assertNull(prefs.getString("discord.token", null))
            assertNotNull(prefs.getString("discord.token.enc", null))
        } finally {
            stash.authToken = previous
        }
        assertEquals(previous, stash.authToken)
        assertEquals(previous.isNotBlank(), stash.signedIn)
    }

    @Test
    fun c_clearingTokenRemovesBothStoredKeys() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val prefs = context.getSharedPreferences("stash", Context.MODE_PRIVATE)
        val stash = Stash.of(context)

        stash.authToken = "clear-me"
        stash.authToken = ""

        assertFalse(stash.signedIn)
        assertNull(prefs.getString("discord.token", null))
        assertNull(prefs.getString("discord.token.enc", null))

        // Stash.write applies asynchronously. Flush the removal so tearing the
        // instrumentation process down right after the run cannot leave a stale
        // sealed token behind (which would look like a signed-in session).
        prefs.edit()
            .remove("discord.token")
            .remove("discord.token.enc")
            .commit()
    }
}
