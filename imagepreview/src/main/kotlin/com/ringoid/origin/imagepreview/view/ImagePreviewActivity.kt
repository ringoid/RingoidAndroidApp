package com.ringoid.origin.imagepreview.view

import android.os.Bundle
import com.ringoid.origin.imagepreview.R
import com.ringoid.origin.view.base.BaseHostActivity

class ImagePreviewActivity : BaseHostActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState ?: run {
            val fragment = ImagePreviewFragment.newInstance(uri = "")  // TODO: image uri
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.fl_container, fragment, ImagePreviewFragment.TAG)
                .commitNow()
        }
    }
}
