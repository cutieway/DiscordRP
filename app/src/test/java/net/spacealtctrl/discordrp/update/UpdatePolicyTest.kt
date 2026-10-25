package net.spacealtctrl.discordrp.update

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdatePolicyTest {
    private val digest = "a".repeat(64)

    @Test
    fun acceptsOnlyForkReleaseAssets() {
        assertTrue(
            UpdatePolicy.isTrustedApkUrl(
                "https://github.com/cutieway/DiscordRP/releases/download/v1.6.0/app.apk",
            ),
        )
        assertFalse(
            UpdatePolicy.isTrustedApkUrl(
                "https://github.com/spacealtctrl/DiscordRP/releases/download/v1.6.0/app.apk",
            ),
        )
        assertFalse(
            UpdatePolicy.isTrustedApkUrl(
                "http://github.com/cutieway/DiscordRP/releases/download/v1.6.0/app.apk",
            ),
        )
        assertFalse(
            UpdatePolicy.isTrustedApkUrl(
                "https://github.com.evil.test/cutieway/DiscordRP/releases/download/v1.6.0/app.apk",
            ),
        )
        assertFalse(
            UpdatePolicy.isTrustedApkUrl(
                "https://user@github.com/cutieway/DiscordRP/releases/download/v1.6.0/app.apk",
            ),
        )
        assertFalse(
            UpdatePolicy.isTrustedApkUrl(
                "https://github.com:8443/cutieway/DiscordRP/releases/download/v1.6.0/app.apk",
            ),
        )
    }

    @Test
    fun requiresACompleteSha256Digest() {
        assertEquals(digest, UpdatePolicy.sha256("sha256:$digest"))
        assertEquals(digest, UpdatePolicy.sha256("sha256:${digest.uppercase()}"))
        assertNull(UpdatePolicy.sha256(null))
        assertNull(UpdatePolicy.sha256(""))
        assertNull(UpdatePolicy.sha256("sha256:${"a".repeat(63)}"))
        assertNull(UpdatePolicy.sha256("sha256:${"a".repeat(65)}"))
        assertNull(UpdatePolicy.sha256("md5:$digest"))
    }
}
