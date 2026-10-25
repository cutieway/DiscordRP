package net.spacealtctrl.discordrp.gateway

import java.net.URI

/**
 * Resume endpoints arrive over the gateway itself, so they are validated
 * before a credential-bearing reconnect can use them.
 */
internal object GatewayEndpoint {
    const val DEFAULT_URL = "wss://gateway.discord.gg/?v=10&encoding=json"

    private const val QUERY = "?v=10&encoding=json"
    private const val DEFAULT_WSS_PORT = 443

    fun resumeUrl(raw: String?): String? {
        val uri = raw?.let { runCatching { URI(it) }.getOrNull() } ?: return null
        if (!"wss".equals(uri.scheme, ignoreCase = true)) return null
        if (uri.userInfo != null) return null
        if (uri.port != -1 && uri.port != DEFAULT_WSS_PORT) return null
        if (uri.rawQuery != null || uri.fragment != null) return null

        val path = uri.path.orEmpty()
        if (path.isNotEmpty() && path != "/") return null

        val host = uri.host?.lowercase() ?: return null
        if (!isGatewayHost(host)) return null
        return "wss://$host/$QUERY"
    }

    private fun isGatewayHost(host: String): Boolean {
        if (host == "gateway.discord.gg") return true
        return host.endsWith(".discord.gg") &&
            host.substringBefore('.').startsWith("gateway")
    }
}
