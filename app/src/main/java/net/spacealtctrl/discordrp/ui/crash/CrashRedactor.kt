package net.spacealtctrl.discordrp.ui.crash

/**
 * Crash reports are user-initiated shares, but they can contain exception
 * messages that quote credentials or signed URLs. Strip those before the text
 * ever leaves the process.
 */
internal object CrashRedactor {
    private const val REPLACEMENT = "[redacted]"

    private val HEADER = Regex(
        "(?i)\\b(authorization|proxy-authorization|cookie|set-cookie)\\s*:\\s*[^\\r\\n]+",
    )
    private val AUTH_SCHEME = Regex("(?i)\\b(bearer|bot)\\s+[A-Za-z0-9._~+/=-]{8,}")
    private val TOKEN = Regex(
        "\\b(?:mfa\\.[A-Za-z0-9_-]{20,}" +
            "|[A-Za-z0-9_-]{23,28}\\.[A-Za-z0-9_-]{6,7}\\.[A-Za-z0-9_-]{27,40})\\b",
    )
    private val QUERY = Regex(
        "(?i)([?&](?:token|access_token|refresh_token|client_secret|code|hm|sig|signature|key)=)" +
            "[^&#\\s'\"]+",
    )
    private val USERINFO = Regex("([A-Za-z][A-Za-z0-9+.-]*://)[^/?#\\s@]+@")

    fun redact(text: String): String = text
        .replace(HEADER) { match -> "${match.groupValues[1]}: $REPLACEMENT" }
        .replace(AUTH_SCHEME) { match -> "${match.groupValues[1]} $REPLACEMENT" }
        .replace(TOKEN, "[redacted-token]")
        .replace(QUERY) { match -> "${match.groupValues[1]}$REPLACEMENT" }
        .replace(USERINFO) { match -> "${match.groupValues[1]}$REPLACEMENT@" }
}
