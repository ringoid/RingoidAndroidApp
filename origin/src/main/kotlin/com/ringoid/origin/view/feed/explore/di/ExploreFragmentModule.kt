package com.ringoid.origin.view.feed.explore.di

import androidx.fragment.app.Fragment
import com.ringoid.origin.view.feed.explore.ExploreFragment
import dagger.Binds
import dagger.Module
import dagger.android.AndroidInjector
import dagger.android.support.FragmentKey
import dagger.multibindings.IntoMap

@Module(subcomponents = [ExploreFragmentSubcomponent::class])
abstract class ExploreFragmentModule {

    @Binds @IntoMap @FragmentKey(ExploreFragment::class)
    abstract fun bindExploreFragmentInjectorFactory(builder: ExploreFragmentSubcomponent.Builder): AndroidInjector.Factory<out Fragment>
}
