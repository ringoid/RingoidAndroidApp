package com.ringoid.origin.view.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ringoid.origin.R
import dagger.android.AndroidInjection

class MainActivity : AppCompatActivity() {//BaseActivity<MainViewModel>() {

//    override fun getVmClass() = MainViewModel::class.java

//    override fun getLayoutId(): Int = R.layout.activity_main

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}
