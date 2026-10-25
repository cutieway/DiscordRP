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

/**
 * These tests run in the app process and use the production preferences, so
 * every case that writes tokens snapshots the stored session first and puts it
 * back afterwards. Otherwise a test run signs the user out.
 */
@RunWith(AndroidJUnit4::class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class TokenStorageTest {
    private val context: Context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    private val prefs
        get() = context.getSharedPreferences("stash", Context.MODE_PRIVATE)

    /** The stored token, read without populating the Stash singleton's cache. */
    private fun storedToken(): String {
        val sealed = prefs.getString("discord.token.enc", null)
        if (sealed != null) {
            TokenVault.decrypt(sealed)?.let { return it }
        }
        return prefs.getString("discord.token", null).orEmpty()
    }

    private fun restore(stash: Stash, token: String) {
        stash.authToken = token
        // Stash writes with apply(). Persist the resulting map synchronously so
        // tearing the test process down cannot drop the restored session.
        prefs.edit()
            .putString("discord.token.enc", prefs.getString("discord.token.enc", null))
            .commit()
    }

    @Test
    fun a_legacyTokenIsMigrated() {
        val previous = storedToken()
        val stash = Stash.of(context)
        try {
            prefs.edit()
                .putString("discord.token", "legacy-token")
                .remove("discord.token.enc")
                .commit()

            assertEquals("legacy-token", stash.authToken)
            assertNull(prefs.getString("discord.token", null))
            assertNotNull(prefs.getString("discord.token.enc", null))
        } finally {
            restore(stash, previous)
        }
    }

    @Test
    fun b_tokenIsSealedAndNeverLeftInPlaintext() {
        val stash = Stash.of(context)
        val previous = stash.authToken
        try {
            stash.authToken = "smoke-token"
            assertEquals("smoke-token", stash.authToken)

            assertNull(prefs.getString("discord.token", null))
            assertNotNull(prefs.getString("discord.token.enc", null))
        } finally {
            restore(stash, previous)
        }
        assertEquals(previous, stash.authToken)
        assertEquals(previous.isNotBlank(), stash.signedIn)
    }

    @Test
    fun c_clearingTokenRemovesBothStoredKeys() {
        val stash = Stash.of(context)
        val previous = stash.authToken
        try {
            stash.authToken = "clear-me"
            stash.authToken = ""

            assertFalse(stash.signedIn)
            assertNull(prefs.getString("discord.token", null))
            assertNull(prefs.getString("discord.token.enc", null))
        } finally {
            restore(stash, previous)
        }
    }
}
