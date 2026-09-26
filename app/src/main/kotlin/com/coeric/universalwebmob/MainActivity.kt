package com.coeric.universalwebmob

import android.app.*
import android.content.*
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.webkit.*
import android.widget.*
import java.net.URLEncoder

class MainActivity : Activity() {
    private lateinit var web: WebView
    private lateinit var url: EditText
    private lateinit var status: TextView
    private var desktopMode = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildShell()
        configureWeb()
        web.loadUrl("https://www.google.com")
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    private fun buildShell() {
        val root = FrameLayout(this).apply { setBackgroundColor(Color.rgb(7,10,18)) }
        val column = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(8), dp(8), dp(8), 0)
        }
        val top = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val brand = TextView(this).apply {
            text = "U"; textSize = 22f; setTextColor(Color.WHITE); gravity = Gravity.CENTER
            background = round(Color.rgb(124,92,255), dp(12))
        }
        top.addView(brand, LinearLayout.LayoutParams(dp(42), dp(42)))
        url = EditText(this).apply {
            hint = "Search or enter website"
            setHintTextColor(Color.rgb(150,155,170))
            setTextColor(Color.WHITE)
            setSingleLine(true); textSize = 15f
            imeOptions = EditorInfo.IME_ACTION_GO
            setPadding(dp(14), 0, dp(10), 0)
            background = round(Color.rgb(22,27,40), dp(14))
        }
        top.addView(url, LinearLayout.LayoutParams(0, dp(42), 1f).apply {
            setMargins(dp(8),0,dp(8),0)
        })
        top.addView(iconButton("↻") { web.reload() }, LinearLayout.LayoutParams(dp(42),dp(42)))
        column.addView(top)

        val controls = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        listOf(
            "‹" to { web.goBack() },
            "›" to { web.goForward() },
            "＋" to { web.loadUrl("https://www.google.com") },
            "▣" to { showTabs() },
            "☰" to { showMenu() }
        ).forEach { pair ->
            controls.addView(iconButton(pair.first, pair.second),
                LinearLayout.LayoutParams(0,dp(38),1f).apply {
                    setMargins(dp(2),dp(5),dp(2),dp(3))
                })
        }
        column.addView(controls)
        status = TextView(this).apply {
            text = "DESKTOP MODE • SECURE WEBVIEW"
            textSize = 10f
            setTextColor(Color.rgb(145,150,165))
            setPadding(dp(4),0,0,dp(3))
        }
        column.addView(status)
        root.addView(column, FrameLayout.LayoutParams(-1,-2))
        web = WebView(this)
        root.addView(web, FrameLayout.LayoutParams(-1,-1).apply { topMargin = dp(96) })
        setContentView(root)
        url.setOnEditorActionListener { _,_,_ -> navigate(url.text.toString()); true }
    }

    private fun configureWeb() {
        val s = web.settings
        s.javaScriptEnabled = true
        s.domStorageEnabled = true
        s.databaseEnabled = true
        s.loadsImagesAutomatically = true
        s.javaScriptCanOpenWindowsAutomatically = true
        s.allowFileAccess = true
        s.allowContentAccess = true
        s.mediaPlaybackRequiresUserGesture = false
        s.builtInZoomControls = true
        s.displayZoomControls = false
        s.useWideViewPort = true
        s.loadWithOverviewMode = false
        setDesktopUserAgent()
        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(web,true)

        web.webViewClient = object : WebViewClient() {
            override fun onPageStarted(v: WebView?, u: String?, favicon: android.graphics.Bitmap?) {
                status.text = "LOADING • DESKTOP MODE"
                u?.let { url.setText(it) }
            }
            override fun onPageFinished(v: WebView?, u: String?) {
                status.text = "READY • " + if (desktopMode) "DESKTOP" else "MOBILE" + " MODE"
                u?.let { url.setText(it) }
            }
        }
        web.webChromeClient = object : WebChromeClient() {
            override fun onReceivedTitle(view: WebView?, title: String?) {
                super.onReceivedTitle(view,title)
            }
        }
        web.setDownloadListener { u,_,_,_,_ ->
            try { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(u))) } catch(_:Exception) {}
        }
    }

    private fun setDesktopUserAgent() {
        web.settings.userAgentString =
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36"
    }

    private fun navigate(raw: String) {
        var target = raw.trim()
        if (target.isEmpty()) return
        if (!target.contains("://")) {
            target = if (Regex("^[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}(/.*)?$").matches(target))
                "https://" + target
            else "https://www.google.com/search?q=" + URLEncoder.encode(target,"UTF-8")
        }
        web.loadUrl(target)
    }

    private fun showMenu() {
        val items = arrayOf(
            "Desktop mode: " + if(desktopMode) "ON" else "OFF",
            "Reload page",
            "Clear site data",
            "Find in page",
            "Share page",
            "About"
        )
        AlertDialog.Builder(this).setTitle("Universal Web Mob").setItems(items) { _, which ->
            when(which) {
                0 -> {
                    desktopMode = !desktopMode
                    if(desktopMode) setDesktopUserAgent()
                    else web.settings.userAgentString = null
                    web.reload()
                }
                1 -> web.reload()
                2 -> {
                    CookieManager.getInstance().removeAllCookies(null)
                    web.clearCache(true)
                    web.reload()
                }
                3 -> showFind()
                4 -> sharePage()
                5 -> showAbout()
            }
        }.show()
    }

    private fun showFind() {
        val input = EditText(this).apply { hint = "Find text" }
        AlertDialog.Builder(this).setTitle("Find in page").setView(input)
            .setPositiveButton("Find") { _,_ -> web.findAllAsync(input.text.toString()) }
            .setNegativeButton("Close",null).show()
    }

    private fun sharePage() {
        startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
            type="text/plain"; putExtra(Intent.EXTRA_TEXT, web.url)
        },"Share page"))
    }

    private fun showTabs() {
        AlertDialog.Builder(this).setTitle("Universal Web Mob")
            .setMessage("Multi-tab workspace foundation is active. The next release will add persistent tab cards, private tabs, tab groups and session restore.")
            .setPositiveButton("New tab") { _,_ -> web.loadUrl("https://www.google.com") }
            .setNegativeButton("Close",null).show()
    }

    private fun showAbout() {
        AlertDialog.Builder(this).setTitle("Universal Web Mob")
            .setMessage("A desktop-grade web workspace for Android. Built for sites and web applications that normally expect a computer.\n\nVersion 1.0.0")
            .setPositiveButton("Close",null).show()
    }

    private fun round(color:Int,r:Int) = GradientDrawable().apply {
        setColor(color); cornerRadius = r.toFloat()
    }

    private fun iconButton(label:String, action:()->Unit) = Button(this).apply {
        text=label; textSize=18f; setTextColor(Color.WHITE); setOnClickListener { action() }
        background=round(Color.rgb(22,27,40),dp(12)); stateListAnimator=null
    }
}
