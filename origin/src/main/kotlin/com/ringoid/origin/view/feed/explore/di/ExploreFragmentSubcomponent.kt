package com.ringoid.origin.view.feed.explore.di

import com.ringoid.origin.view.feed.explore.ExploreFragment
import dagger.Subcomponent
import dagger.android.AndroidInjector

@Subcomponent
interface ExploreFragmentSubcomponent : AndroidInjector<ExploreFragment> {

    @Subcomponent.Builder
    abstract class Builder : AndroidInjector.Builder<ExploreFragment>()
}
