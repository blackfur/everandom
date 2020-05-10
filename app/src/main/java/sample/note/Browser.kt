package sample.note

import android.os.Build
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

class Browser : AppCompatActivity(){

    var webView:WebView? = null
    var url:String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_browser)
        webView = findViewById(R.id.browser)
        webView!!.webViewClient = Delegate()
        webView!!.settings.loadsImagesAutomatically = true
        webView!!.settings.javaScriptEnabled=true
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) WebView.setWebContentsDebuggingEnabled(true)

        url = intent.getStringExtra("index") ?: errorHtml
        webView!!.clearCache(true)
        webView!!.loadUrl(url!!)
    }

    inner class Delegate :WebViewClient(){
        override fun shouldOverrideUrlLoading(view: WebView, url:String):Boolean {

            view.loadUrl(url)
            return true
        }
    }
}