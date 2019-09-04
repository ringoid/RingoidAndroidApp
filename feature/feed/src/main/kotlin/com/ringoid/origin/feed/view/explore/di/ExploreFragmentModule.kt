package com.ringoid.origin.feed.view.explore.di

import androidx.fragment.app.Fragment
import com.ringoid.origin.feed.view.explore.ExploreFeedFragment
import com.ringoid.origin.feed.view.explore.ExploreFeedFiltersFragment
import dagger.Binds
import dagger.Module
import dagger.android.AndroidInjector
import dagger.android.ContributesAndroidInjector
import dagger.android.support.FragmentKey
import dagger.multibindings.IntoMap

@Module(subcomponents = [ExploreFragmentSubcomponent::class])
abstract class ExploreFragmentModule {

    @Binds @IntoMap @FragmentKey(ExploreFeedFragment::class)
    abstract fun bindExploreFeedFragmentInjectorFactory(builder: ExploreFragmentSubcomponent.Builder): AndroidInjector.Factory<out Fragment>

    @ContributesAndroidInjector
    abstract fun contributeExploreFeedFiltersFragmentInjector(): ExploreFeedFiltersFragment
}
