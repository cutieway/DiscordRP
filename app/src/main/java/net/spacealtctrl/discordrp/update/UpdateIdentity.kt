package net.spacealtctrl.discordrp.update

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import java.io.File
import java.security.MessageDigest

/**
 * An update must be the same app, signed by the same key that installed the
 * running build. Android enforces that at install time; checking it before
 * handing the file to the installer keeps a foreign or re-signed package from
 * ever being presented as an update, and turns the failure into a clear one.
 */
internal object UpdateIdentity {
    fun verify(context: Context, apk: File) {
        val pm = context.packageManager
        val archive = pm.getPackageArchiveInfo(apk.absolutePath, signingFlags)
            ?: error("unreadable update package")
        if (archive.packageName != context.packageName) {
            error("update package id mismatch")
        }

        val candidate = signerDigests(archive)
        val installed = signerDigests(pm.getPackageInfo(context.packageName, signingFlags))
        if (candidate.isEmpty() || installed.isEmpty() || candidate != installed) {
            error("update signature mismatch")
        }
    }

    private val signingFlags: Int
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // Some devices leave signingInfo unset when parsing an archive, so
            // ask for the legacy field as well and fall back to it below.
            PackageManager.GET_SIGNING_CERTIFICATES or PackageManager.GET_SIGNATURES
        } else {
            @Suppress("DEPRECATION")
            PackageManager.GET_SIGNATURES
        }

    private fun signerDigests(info: PackageInfo): Set<String> {
        val signingInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) info.signingInfo else null
        val signers = (signingInfo?.apkContentsSigners ?: legacySignatures(info)).orEmpty()
        if (signers.isEmpty()) return emptySet()
        return signers.mapTo(mutableSetOf()) { signature ->
            MessageDigest.getInstance("SHA-256")
                .digest(signature.toByteArray())
                .joinToString("") { "%02x".format(it.toInt() and 0xff) }
        }
    }

    @Suppress("DEPRECATION")
    private fun legacySignatures(info: PackageInfo) = info.signatures
}
