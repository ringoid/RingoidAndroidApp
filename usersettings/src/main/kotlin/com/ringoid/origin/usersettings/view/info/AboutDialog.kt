package com.ringoid.origin.usersettings.view.info

import android.os.Bundle
import android.text.Spannable
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.LayoutRes
import com.jakewharton.rxbinding3.view.clicks
import com.ringoid.base.view.SimpleBaseDialogFragment
import com.ringoid.origin.navigation.navigate
import com.ringoid.usersettings.R
import com.ringoid.utility.AutoLinkMovementMethod
import com.ringoid.utility.clickDebounce
import kotlinx.android.synthetic.main.dialog_about.*

class AboutDialog : SimpleBaseDialogFragment() {

    companion object {
        const val TAG = "TextDialog_tag"

        fun newInstance(): AboutDialog = AboutDialog()
    }

    @LayoutRes
    override fun getLayoutId(): Int = R.layout.dialog_about

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
            ?.apply { setOnClickListener { dismiss() } }
    }

    @Suppress("CheckResult", "AutoDispose")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btn_close.clicks().compose(clickDebounce()).subscribe { dismiss() }
        tv_description.movementMethod = object : AutoLinkMovementMethod() {
            override fun onTouchEvent(widget: TextView, buffer: Spannable, event: MotionEvent): Boolean {
                val result = super.onTouchEvent(widget, buffer, event)
                if (event.action == MotionEvent.ACTION_UP) {
                    widget.post { dismiss() }
                }
                return result
            }

            override fun processUrl(url: String) {
                navigate(this@AboutDialog, path = "/webpage?url=$url")
            }
        }
    }
}
