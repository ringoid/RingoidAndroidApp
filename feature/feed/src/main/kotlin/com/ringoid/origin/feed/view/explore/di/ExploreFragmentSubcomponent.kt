package com.ringoid.origin.feed.view.explore.di

import com.ringoid.origin.feed.view.explore.ExploreFeedFragment
import dagger.Subcomponent
import dagger.android.AndroidInjector

@Subcomponent
interface ExploreFragmentSubcomponent : AndroidInjector<ExploreFeedFragment> {

    @Subcomponent.Builder
    abstract class Builder : AndroidInjector.Builder<ExploreFeedFragment>()
}
