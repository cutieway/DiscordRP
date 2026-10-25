package net.spacealtctrl.discordrp.ui.screens.login

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LoginWebPolicyTest {
    @Test
    fun acceptsCanonicalDiscordHttpsOrigins() {
        assertTrue(LoginWebPolicy.isDiscordUrl("https://discord.com/login"))
        assertTrue(LoginWebPolicy.isDiscordUrl("https://canary.discord.com/app"))
        assertTrue(LoginWebPolicy.isDiscordUrl("https://discordapp.com/app"))
        assertTrue(LoginWebPolicy.isDiscordAppUrl("https://discord.com/app"))
        assertTrue(LoginWebPolicy.isDiscordAppUrl("https://discord.com/app/"))
        assertTrue(LoginWebPolicy.isDiscordAppUrl("https://discord.com/app?from=login"))
    }

    @Test
    fun onlyAppPathTriggersTokenExtraction() {
        assertFalse(LoginWebPolicy.isDiscordAppUrl("https://discord.com/login"))
        assertFalse(LoginWebPolicy.isDiscordAppUrl("https://discord.com/app/messages"))
    }

    @Test
    fun rejectsLookalikeAndInsecureOrigins() {
        assertFalse(LoginWebPolicy.isDiscordUrl("http://discord.com/app"))
        assertFalse(LoginWebPolicy.isDiscordUrl("https://discord.com.evil.test/app"))
        assertFalse(LoginWebPolicy.isDiscordUrl("https://notdiscord.com/app"))
        assertFalse(LoginWebPolicy.isDiscordUrl("https://user@discord.com/app"))
        assertFalse(LoginWebPolicy.isDiscordUrl("https://discord.com:8443/app"))
        assertFalse(LoginWebPolicy.isDiscordAppUrl("https://evil.test/app"))
    }
}
