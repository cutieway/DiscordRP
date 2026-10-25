package net.spacealtctrl.discordrp.ui.screens.login

import java.net.URI

/**
 * Trust boundary for the embedded Discord login page. Only canonical HTTPS
 * Discord origins can load in the login WebView, and only /app may trigger
 * token extraction.
 */
internal object LoginWebPolicy {
    private const val DEFAULT_HTTPS_PORT = 443

    fun isDiscordUrl(raw: String?): Boolean {
        val uri = raw?.let { runCatching { URI(it) }.getOrNull() } ?: return false
        if (!"https".equals(uri.scheme, ignoreCase = true)) return false
        if (uri.userInfo != null) return false
        if (uri.port != -1 && uri.port != DEFAULT_HTTPS_PORT) return false
        val host = uri.host?.lowercase() ?: return false
        // Only the origins that serve the login application itself. Discord
        // controls every subdomain, but none of them are needed to sign in, and
        // extraction runs on this trust boundary.
        return host == "discord.com" || host == "discordapp.com"
    }

    fun isDiscordAppUrl(raw: String?): Boolean {
        val uri = raw?.let { runCatching { URI(it) }.getOrNull() } ?: return false
        return isDiscordUrl(raw) && uri.path?.trimEnd('/') == "/app"
    }
}
