package com.ringoid.base.view

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.ringoid.base.viewmodel.FragmentDelegateVmFactory

abstract class BaseFragment : Fragment() {

    protected val vmFactory: ViewModelProvider.Factory by FragmentDelegateVmFactory()
}
