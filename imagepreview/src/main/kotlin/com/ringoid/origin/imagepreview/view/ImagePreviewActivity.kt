package com.ringoid.origin.imagepreview.view

import android.os.Bundle
import com.ringoid.origin.Extras
import com.ringoid.origin.imagepreview.R
import com.ringoid.origin.view.base.BaseHostActivity

class ImagePreviewActivity : BaseHostActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState ?: run {
            val fragment = ImagePreviewFragment.newInstance(
                uri = intent.dataString, needOpenMainScreenIfNoUri = intent.getBooleanExtra(Extras.EXTRA_OPEN_MAIN_IF_NO_URI, false))
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.fl_container, fragment, ImagePreviewFragment.TAG)
                .commitNow()
        }
    }
}
