package net.spacealtctrl.discordrp.settings

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TokenStorageTest {
    @Test
    fun tokenIsSealedAndNeverLeftInPlaintext() {
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
}
