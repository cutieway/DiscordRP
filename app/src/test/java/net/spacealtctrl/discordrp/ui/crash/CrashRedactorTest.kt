package net.spacealtctrl.discordrp.ui.crash

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CrashRedactorTest {
    @Test
    fun removesTokenShapedValues() {
        val token = "${"M".repeat(24)}.${"Ga4BcD"}.${"a".repeat(38)}"
        val mfa = "mfa.${"A".repeat(84)}"

        val redacted = CrashRedactor.redact("token=$token\nmfa=$mfa")
        assertFalse(redacted.contains(token))
        assertFalse(redacted.contains(mfa))
        assertTrue(redacted.contains("[redacted-token]"))
    }

    @Test
    fun removesCredentialHeaders() {
        val report = """
            Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.payload.signature
            Cookie: session=abc123; discord_token=secret
            Accept: application/json
        """.trimIndent()

        val redacted = CrashRedactor.redact(report)
        assertFalse(redacted.contains("eyJhbGciOiJIUzI1NiJ9"))
        assertFalse(redacted.contains("session=abc123"))
        assertTrue(redacted.contains("Authorization: [redacted]"))
        assertTrue(redacted.contains("Cookie: [redacted]"))
        assertTrue(redacted.contains("Accept: application/json"))
    }

    @Test
    fun removesAuthenticatedUrlParts() {
        val redacted = CrashRedactor.redact(
            "GET https://cdn.discordapp.com/a/b.png?ex=1&is=2&hm=deadbeef " +
                "https://user:pass@discord.com/app?token=abc",
        )
        assertFalse(redacted.contains("deadbeef"))
        assertFalse(redacted.contains("token=abc"))
        assertFalse(redacted.contains("user:pass"))
        assertTrue(redacted.contains("ex=1&is=2"))
        assertTrue(redacted.contains("https://[redacted]@discord.com/app"))
    }

    @Test
    fun leavesOrdinaryTextAlone() {
        val report = """
            java.lang.IllegalStateException: update signature mismatch
                at net.spacealtctrl.discordrp.update.UpdateIdentity.verify(UpdateIdentity.kt:31)
            App version: 1.5.3
            See https://discord.com/app
        """.trimIndent()

        assertEquals(report, CrashRedactor.redact(report))
    }
}
