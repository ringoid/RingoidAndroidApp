package com.ringoid.base.view

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.ringoid.base.viewmodel.ActivityDelegateVmFactory

abstract class BaseActivity : AppCompatActivity() {

    protected val vmFactory: ViewModelProvider.Factory by  ActivityDelegateVmFactory()

    @LayoutRes protected open fun getLayoutId(): Int? = null  // null means no layout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getLayoutId()?.let { setContentView(it) }
    }
}
