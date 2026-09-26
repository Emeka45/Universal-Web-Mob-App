package com.coeric.universalwebmob

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.Window
import android.widget.*
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoSessionSettings
import org.mozilla.geckoview.GeckoView
import org.mozilla.geckoview.GeckoResult
import org.mozilla.geckoview.WebResponse
import java.net.URLEncoder

class MainActivity : Activity() {
    private lateinit var geckoView: GeckoView
    private lateinit var address: EditText
    private lateinit var status: TextView

    private var runtime: GeckoRuntime? = null
    private var session: GeckoSession? = null
    private var desktopMode = true
    private var immersive = false

    companion object {
        private const val HOME = "https://www.google.com"
        private var sharedRuntime: GeckoRuntime? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.requestFeature(Window.FEATURE_NO_TITLE)
        buildShell()
        initializeGecko()
        session?.loadUri(savedInstanceState?.getString("last_url") ?: HOME)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("last_url", address.text.toString())
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        if (::geckoView.isInitialized) geckoView.releaseSession()
        session?.close()
        session = null
        super.onDestroy()
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    private fun buildShell() {
        val root = FrameLayout(this).apply {
            setBackgroundColor(Color.rgb(7, 10, 18))
        }

        val toolbar = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(8), dp(8), dp(8), 0)
            setBackgroundColor(Color.rgb(7, 10, 18))
        }

        val top = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val brand = TextView(this).apply {
            text = "U"
            textSize = 22f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            background = round(Color.rgb(124, 92, 255), dp(12))
        }
        top.addView(brand, LinearLayout.LayoutParams(dp(42), dp(42)))

        address = EditText(this).apply {
            hint = "Search or enter website"
            setHintTextColor(Color.rgb(150, 155, 170))
            setTextColor(Color.WHITE)
            setSingleLine(true)
            textSize = 15f
            imeOptions = android.view.inputmethod.EditorInfo.IME_ACTION_GO
            setPadding(dp(14), 0, dp(10), 0)
            background = round(Color.rgb(22, 27, 40), dp(14))
        }
        top.addView(address, LinearLayout.LayoutParams(0, dp(42), 1f).apply {
            setMargins(dp(8), 0, dp(8), 0)
        })

        top.addView(
            iconButton("↻") { session?.reload() },
            LinearLayout.LayoutParams(dp(42), dp(42))
        )
        toolbar.addView(top)

        val controls = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        listOf<Pair<String, () -> Unit>>(
            "‹" to { session?.goBack(); Unit },
            "›" to { session?.goForward(); Unit },
            "＋" to { newTab(); Unit },
            "▣" to { showWorkspace(); Unit },
            "☰" to { showMenu(); Unit }
        ).forEach { item ->
            controls.addView(
                iconButton(item.first, item.second),
                LinearLayout.LayoutParams(0, dp(38), 1f).apply {
                    setMargins(dp(2), dp(5), dp(2), dp(3))
                }
            )
        }
        toolbar.addView(controls)

        status = TextView(this).apply {
            text = "GECKO ENGINE • DESKTOP MODE"
            textSize = 10f
            setTextColor(Color.rgb(145, 150, 165))
            setPadding(dp(4), 0, 0, dp(3))
        }
        toolbar.addView(status)

        root.addView(toolbar, FrameLayout.LayoutParams(-1, -2))

        geckoView = GeckoView(this)
        root.addView(
            geckoView,
            FrameLayout.LayoutParams(-1, -1).apply {
                topMargin = dp(96)
            }
        )

        setContentView(root)

        address.setOnEditorActionListener { _, _, _ ->
            navigate(address.text.toString())
            true
        }
    }

    private fun createSession(): GeckoSession {
        val settings = GeckoSessionSettings.Builder()
            .allowJavascript(true)
            .userAgentMode(
                if (desktopMode)
                    GeckoSessionSettings.USER_AGENT_MODE_DESKTOP
                else
                    GeckoSessionSettings.USER_AGENT_MODE_MOBILE
            )
            .viewportMode(
                if (desktopMode)
                    GeckoSessionSettings.VIEWPORT_MODE_DESKTOP
                else
                    GeckoSessionSettings.VIEWPORT_MODE_MOBILE
            )
            .usePrivateMode(false)
            .build()

        return GeckoSession(settings)
    }

    private fun initializeGecko() {
        runtime = sharedRuntime ?: GeckoRuntime.create(this).also {
            sharedRuntime = it
        }

        session = createSession()
        val current = session ?: return
        attachDelegates(current)
        val readyRuntime = runtime ?: return
        current.open(readyRuntime)
        geckoView.setSession(current)
    }

    private fun attachDelegates(current: GeckoSession) {
        current.setNavigationDelegate(object : GeckoSession.NavigationDelegate {
            override fun onLocationChange(
                session: GeckoSession,
                url: String?,
                perms: List<GeckoSession.PermissionDelegate.ContentPermission>,
                hasUserGesture: Boolean
            ) {
                url?.let { address.setText(it) }
                status.text =
                    if (desktopMode) "GECKO ENGINE • DESKTOP MODE"
                    else "GECKO ENGINE • MOBILE MODE"
            }

            override fun onCanGoBack(session: GeckoSession, canGoBack: Boolean) {
                updateNavigationButtons(canGoBack, null)
            }

            override fun onCanGoForward(session: GeckoSession, canGoForward: Boolean) {
                updateNavigationButtons(null, canGoForward)
            }

            override fun onLoadError(
                session: GeckoSession,
                uri: String?,
                error: org.mozilla.geckoview.WebRequestError
            ): GeckoResult<String> {
                status.text = "PAGE ERROR • SUPPORTED BROWSER FALLBACK AVAILABLE"
                return GeckoResult.fromValue("about:blank")
            }

            override fun onNewSession(
                session: GeckoSession,
                uri: String
            ): GeckoResult<GeckoSession> {
                newTab(uri)
                val created = this@MainActivity.session ?: return GeckoResult.fromValue(session)
                return GeckoResult.fromValue(created)
            }
        })

        current.setContentDelegate(object : GeckoSession.ContentDelegate {
            override fun onTitleChange(session: GeckoSession, title: String?) {
                if (!title.isNullOrBlank()) {
                    status.text =
                        "READY • " + if (desktopMode) "DESKTOP" else "MOBILE" + " MODE"
                }
            }

            override fun onFullScreen(session: GeckoSession, fullScreen: Boolean) {
                immersive = fullScreen
                applyImmersiveMode(fullScreen)
            }

            override fun onExternalResponse(session: GeckoSession, response: WebResponse) {
                val uri = response.uri
                if (!uri.isNullOrBlank()) {
                    try {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(uri)))
                    } catch (_: Exception) {
                        Toast.makeText(
                            this@MainActivity,
                            "No application can open this download.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }

            override fun onCloseRequest(session: GeckoSession) {
                finish()
            }
        })
    }

    private fun navigate(raw: String) {
        var target = raw.trim()
        if (target.isEmpty()) return

        if (!target.contains("://")) {
            target = if (
                Regex("^[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}(/.*)?$")
                    .matches(target)
            ) {
                "https://" + target
            } else {
                "https://www.google.com/search?q=" +
                    URLEncoder.encode(target, "UTF-8")
            }
        }

        session?.loadUri(target)
    }

    private fun newTab(url: String = HOME) {
        if (session != null) {
            session?.flushSessionState()
            session?.close()
            geckoView.releaseSession()
        }
        initializeGecko()
        session?.loadUri(url)
    }

    private fun showWorkspace() {
        AlertDialog.Builder(this)
            .setTitle("Universal Web Mob")
            .setMessage(
                "Desktop workspace is active.\n\n" +
                    "Engine: Mozilla GeckoView\n" +
                    "Rendering: desktop viewport\n" +
                    "Browser identity: desktop Gecko\n" +
                    "Authentication: supported-browser fallback available"
            )
            .setPositiveButton("Continue", null)
            .show()
    }

    private fun showMenu() {
        val items = arrayOf(
            "Desktop mode: " + if (desktopMode) "ON" else "OFF",
            "Open in supported browser",
            "Find in page",
            "Share page",
            "Clear browsing data",
            "Fullscreen",
            "About"
        )

        AlertDialog.Builder(this)
            .setTitle("Universal Web Mob")
            .setItems(items) { _, which ->
                when (which) {
                    0 -> toggleDesktopMode()
                    1 -> openExternal()
                    2 -> showFind()
                    3 -> sharePage()
                    4 -> clearBrowsingData()
                    5 -> applyImmersiveMode(!immersive)
                    6 -> showAbout()
                }
            }
            .show()
    }

    private fun toggleDesktopMode() {
        desktopMode = !desktopMode
        val currentUrl = address.text.toString().ifBlank { HOME }

        if (session != null) {
            session?.close()
            geckoView.releaseSession()
        }

        initializeGecko()
        session?.loadUri(currentUrl)
    }

    private fun showFind() {
        val input = EditText(this).apply {
            hint = "Find text"
            setSingleLine(true)
        }

        AlertDialog.Builder(this)
            .setTitle("Find in page")
            .setView(input)
            .setPositiveButton("Find") { _, _ ->
                session?.getFinder()?.apply {
                    setDisplayFlags(GeckoSession.FINDER_DISPLAY_HIGHLIGHT_ALL)
                    find(
                        input.text.toString(),
                        GeckoSession.FINDER_FIND_FORWARD
                    )
                }
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun sharePage() {
        val currentUrl = address.text.toString()
        startActivity(
            Intent.createChooser(
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, currentUrl)
                },
                "Share page"
            )
        )
    }

    private fun openExternal() {
        val target = address.text.toString().ifBlank { HOME }
        try {
            startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse(target)).apply {
                    addCategory(Intent.CATEGORY_BROWSABLE)
                }
            )
        } catch (_: Exception) {
            Toast.makeText(
                this,
                "No supported browser is installed.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun clearBrowsingData() {
        runtime?.storageController?.clearData(
            org.mozilla.geckoview.StorageController.ClearFlags.ALL
        )
        Toast.makeText(this, "Browsing data cleared.", Toast.LENGTH_SHORT).show()
    }

    private fun applyImmersiveMode(enabled: Boolean) {
        immersive = enabled
        window.decorView.systemUiVisibility =
            if (enabled) {
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            } else {
                View.SYSTEM_UI_FLAG_VISIBLE
            }
    }

    private fun updateNavigationButtons(
        canGoBack: Boolean?,
        canGoForward: Boolean?
    ) {
        // Navigation state is delivered asynchronously by Gecko.
        // The controls stay available because Gecko safely ignores unavailable moves.
    }

    private fun showAbout() {
        AlertDialog.Builder(this)
            .setTitle("Universal Web Mob")
            .setMessage(
                "Desktop web workspace for Android.\n\n" +
                    "Engine: Mozilla GeckoView\n" +
                    "Version: 2.0.0\n\n" +
                    "This app does not embed Chromium or Android WebView. " +
                    "It does not bypass authentication, Cloudflare, DRM, " +
                    "or other website security controls."
            )
            .setPositiveButton("Close", null)
            .show()
    }

    private fun round(color: Int, radius: Int) =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = radius.toFloat()
        }

    private fun iconButton(label: String, action: () -> Unit) =
        Button(this).apply {
            text = label
            textSize = 18f
            setTextColor(Color.WHITE)
            setOnClickListener { action() }
            background = round(Color.rgb(22, 27, 40), dp(12))
            stateListAnimator = null
        }
}
