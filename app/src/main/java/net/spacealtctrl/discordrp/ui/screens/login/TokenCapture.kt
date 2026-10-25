package net.spacealtctrl.discordrp.ui.screens.login

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.JsResult
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebStorage
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView

private const val LOGIN_URL = "https://discord.com/login"
private const val TOKEN_ALERT_PREFIX = "discordrp-token:"

private const val TOKEN_HOOK =
    "javascript:(function()%7Bvar%20i%3Ddocument.createElement('iframe')%3B" +
        "document.body.appendChild(i)%3B" +
        "alert('discordrp-token:'%2Bi.contentWindow.localStorage.token.slice(1,-1))%7D)()"

private const val FALLBACK_UA =
    "Mozilla/5.0 (Linux; Android 14; SM-S921U; Build/UP1A.231005.007) " +
        "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Mobile Safari/537.363"

private fun isDiscordUrl(raw: String?): Boolean {
    val uri = runCatching { Uri.parse(raw) }.getOrNull() ?: return false
    if (uri.scheme != "https") return false
    val host = uri.host?.lowercase() ?: return false
    return host == "discord.com" ||
        host.endsWith(".discord.com") ||
        host == "discordapp.com" ||
        host.endsWith(".discordapp.com")
}

private fun isDiscordAppUrl(raw: String?): Boolean {
    val uri = runCatching { Uri.parse(raw) }.getOrNull() ?: return false
    return isDiscordUrl(raw) && uri.path?.trimEnd('/') == "/app"
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun TokenCapture(onToken: (String) -> Unit) {
    AndroidView(
        onRelease = { webView ->
            // Never leave Discord's web session (and a possible token in its
            // localStorage) behind after the login sheet closes.
            WebStorage.getInstance().deleteAllData()
            CookieManager.getInstance().removeAllCookies(null)
            webView.stopLoading()
            webView.destroy()
        },
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.allowFileAccess = false
                settings.allowContentAccess = false
                settings.javaScriptCanOpenWindowsAutomatically = false
                settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                if (Build.MANUFACTURER.equals("motorola", ignoreCase = true)) {
                    settings.userAgentString = FALLBACK_UA
                }

                webViewClient = object : WebViewClient() {
                    private var capturing = false

                    private fun capture(view: WebView, url: String?) {
                        if (capturing || !isDiscordAppUrl(url)) return
                        capturing = true
                        view.loadUrl(TOKEN_HOOK)
                        view.visibility = View.GONE
                    }

                    @Deprecated("Deprecated in Java")
                    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                        if (!isDiscordUrl(url)) return true
                        if (isDiscordAppUrl(url)) {
                            view.stopLoading()
                            capture(view, url)
                            return true
                        }
                        return false
                    }

                    // The first composited frame can stay white while the bottom
                    // sheet animates in; repaint once the page is committed.
                    override fun onPageCommitVisible(view: WebView, url: String) {
                        super.onPageCommitVisible(view, url)
                        view.postInvalidateOnAnimation()
                    }

                    override fun onPageFinished(view: WebView, url: String) {
                        super.onPageFinished(view, url)
                        view.post {
                            view.requestLayout()
                            view.invalidate()
                        }
                        capture(view, url)
                    }
                }
                webChromeClient = object : WebChromeClient() {
                    override fun onJsAlert(
                        view: WebView,
                        url: String,
                        message: String,
                        result: JsResult,
                    ): Boolean {
                        // Only our prefixed alert on Discord's own origin is a
                        // token; every other page dialog stays with WebView.
                        if (!message.startsWith(TOKEN_ALERT_PREFIX) || !isDiscordUrl(url)) {
                            return false
                        }
                        val token = message.removePrefix(TOKEN_ALERT_PREFIX).trim()
                        if (token.isEmpty()) return true

                        // The app keeps the token in Keystore-sealed prefs; drop
                        // Discord's web session copy so it is not stored twice.
                        WebStorage.getInstance().deleteAllData()
                        CookieManager.getInstance().removeAllCookies(null)
                        onToken(token)
                        view.visibility = View.GONE
                        return true
                    }
                }
                loadUrl(LOGIN_URL)
            }
        },
    )
}
