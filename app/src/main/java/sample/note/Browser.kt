package sample.note

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
        webView!!.loadUrl(url!!)
    }

    inner class Delegate :WebViewClient(){
        override fun shouldOverrideUrlLoading(view: WebView, url:String):Boolean {

            view.loadUrl(url)
            return true
        }
    }
}