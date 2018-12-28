package com.ringoid.base.view

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import javax.inject.Inject

abstract class BaseFragment : Fragment() {

    @Inject protected lateinit var vmFactory: ViewModelProvider.Factory
}
