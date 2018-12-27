package com.ringoid.base.view

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.ringoid.base.viewmodel.ActivityDelegateVmFactory

abstract class BaseActivity : AppCompatActivity() {

    protected val vmFactory: ViewModelProvider.Factory by  ActivityDelegateVmFactory()
}
