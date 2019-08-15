package com.ringoid.origin.view.web.di

import com.ringoid.origin.view.web.WebPageFragment
import dagger.Subcomponent
import dagger.android.AndroidInjector

@Subcomponent
interface WebPageFragmentSubcomponent : AndroidInjector<WebPageFragment> {

    @Subcomponent.Builder
    abstract class Builder : AndroidInjector.Builder<WebPageFragment>()
}
