package com.enric.glancemobile

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.http.SslError
import android.os.Bundle
import android.view.View
import android.webkit.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.enric.glancemobile.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    // --- CONFIGURACIÓN ---
    private val BASE_URL = "http://192.168.1.62:8080" // Cambia esto por tu URL
    private val ALLOW_SELF_SIGNED = true // Habilitado para pruebas locales
    // ---------------------

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupWebView()
        setupSwipeRefresh()
        setupRetryButton()
        setupBackNavigation()

        // Carga inicial
        binding.webView.loadUrl(BASE_URL)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        val webSettings = binding.webView.settings
        
        // Habilitar JavaScript y DOM Storage
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        
        // Configuración de visualización
        webSettings.useWideViewPort = true
        webSettings.loadWithOverviewMode = true
        webSettings.displayZoomControls = false
        webSettings.builtInZoomControls = true
        webSettings.setSupportZoom(true)
        
        // Manejo de Cookies
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        cookieManager.setAcceptThirdPartyCookies(binding.webView, true)

        binding.webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                binding.progressBar.visibility = View.VISIBLE
                binding.errorLayout.visibility = View.GONE
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                binding.progressBar.visibility = View.GONE
                binding.swipeRefresh.isRefreshing = false
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                // Mostrar vista de error si la carga falla
                if (request?.isForMainFrame == true) {
                    binding.errorLayout.visibility = View.VISIBLE
                    binding.webView.visibility = View.GONE
                }
            }

            @SuppressLint("WebViewClientOnReceivedSslError")
            override fun onReceivedSslError(
                view: WebView?,
                handler: SslErrorHandler?,
                error: SslError?
            ) {
                if (ALLOW_SELF_SIGNED) {
                    handler?.proceed()
                } else {
                    super.onReceivedSslError(view, handler, error)
                }
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                // Mantener la navegación dentro del WebView
                return false 
            }
        }

        binding.webView.webChromeClient = WebChromeClient()
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            binding.webView.reload()
        }
    }

    private fun setupRetryButton() {
        binding.btnRetry.setOnClickListener {
            binding.webView.visibility = View.VISIBLE
            binding.errorLayout.visibility = View.GONE
            binding.webView.loadUrl(BASE_URL)
        }
    }

    private fun setupBackNavigation() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.webView.canGoBack()) {
                    binding.webView.goBack()
                } else {
                    finish()
                }
            }
        })
    }
}
