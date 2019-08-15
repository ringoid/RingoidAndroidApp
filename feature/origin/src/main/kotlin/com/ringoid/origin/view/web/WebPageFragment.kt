package com.ringoid.origin.view.web

import android.os.Bundle
import android.view.View
import android.webkit.*
import androidx.appcompat.widget.Toolbar
import com.ringoid.base.view.BaseFragment
import com.ringoid.origin.AppRes
import com.ringoid.origin.R
import com.ringoid.origin.navigation.ExternalNavigator
import com.ringoid.utility.changeVisibility
import com.ringoid.utility.getAttributeColor
import com.ringoid.utility.snackbar
import kotlinx.android.synthetic.main.fragment_web.*

class WebPageFragment : BaseFragment<WebPageViewModel>() {

    companion object {
        internal const val TAG = "WebPageFragment_tag"

        private const val BUNDLE_KEY_WEB_URL = "bundle_key_web_url"

        fun newInstance(webUrl: String?): WebPageFragment =
            WebPageFragment().apply {
                arguments = Bundle().apply { putString(BUNDLE_KEY_WEB_URL, webUrl) }
            }
    }

    private var webUrl: String? = null

    override fun getVmClass(): Class<WebPageViewModel> = WebPageViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_web

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        webUrl = arguments?.getString(BUNDLE_KEY_WEB_URL)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (toolbar as Toolbar).apply {
            val titleResId = when (webUrl) {
                AppRes.WEB_URL_LICENSES -> R.string.web_page_licenses
                AppRes.WEB_URL_PRIVACY -> R.string.web_page_privacy
                AppRes.WEB_URL_TERMS -> R.string.web_page_terms
                else -> R.string.app_name
            }

            inflateMenu(R.menu.menu_internet)
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.menu_internet -> ExternalNavigator.openBrowser(activity, webUrl)
                }
                true
            }
            setNavigationOnClickListener { activity?.onBackPressed() }
            setTitle(titleResId)
        }

        wv_link.apply {
            setBackgroundColor(context.getAttributeColor(R.attr.refColorBg))
            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView, newProgress: Int) {
                    super.onProgressChanged(view, newProgress)
                    pb_web?.changeVisibility(isVisible = newProgress < 100)
                }
            }
            webViewClient = object : WebViewClient() {
                override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
                    super.onReceivedError(view, request, error)
                    pb_web?.changeVisibility(isVisible = false)
                    snackbar(view, R.string.error_common)
                }
            }
            webUrl?.let { loadUrl(it) } ?: run { snackbar(view, R.string.error_common) }
        }
    }
}
