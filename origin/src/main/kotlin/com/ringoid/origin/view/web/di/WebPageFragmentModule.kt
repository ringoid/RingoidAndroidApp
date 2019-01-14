package com.ringoid.origin.view.web.di

import androidx.fragment.app.Fragment
import com.ringoid.origin.view.web.WebPageFragment
import dagger.Binds
import dagger.Module
import dagger.android.AndroidInjector
import dagger.android.support.FragmentKey
import dagger.multibindings.IntoMap

@Module(subcomponents = [])
abstract class WebPageFragmentModule {

    @Binds @IntoMap @FragmentKey(WebPageFragment::class)
    abstract fun bindWebPageFragmentInjectorFactory(builder: WebPageFragmentSubcomponent.Builder): AndroidInjector.Factory<out Fragment>
}
