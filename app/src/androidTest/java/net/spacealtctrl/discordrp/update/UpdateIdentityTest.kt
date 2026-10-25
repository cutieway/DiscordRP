package net.spacealtctrl.discordrp.update

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class UpdateIdentityTest {
    @Test
    fun acceptsTheInstalledApk() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        UpdateIdentity.verify(context, File(context.applicationInfo.sourceDir))
    }

    @Test
    fun rejectsAnApkForAnotherPackage() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val testApk = InstrumentationRegistry.getInstrumentation().context.applicationInfo.sourceDir
        try {
            UpdateIdentity.verify(context, File(testApk))
            fail("an APK for another package must be rejected")
        } catch (_: IllegalStateException) {
            // Expected: the archive carries the instrumentation package id.
        }
    }
}
