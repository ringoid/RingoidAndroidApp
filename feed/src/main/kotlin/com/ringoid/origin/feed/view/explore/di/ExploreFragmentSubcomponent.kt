package com.ringoid.origin.feed.view.explore.di

import com.ringoid.origin.feed.view.explore.ExploreFragment
import dagger.Subcomponent
import dagger.android.AndroidInjector

@Subcomponent
interface ExploreFragmentSubcomponent : AndroidInjector<ExploreFragment> {

    @Subcomponent.Builder
    abstract class Builder : AndroidInjector.Builder<ExploreFragment>()
}
