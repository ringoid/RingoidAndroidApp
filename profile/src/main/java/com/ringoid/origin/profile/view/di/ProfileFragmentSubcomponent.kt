package com.ringoid.origin.profile.view.di

import com.ringoid.origin.profile.view.ProfileFragment
import dagger.Subcomponent
import dagger.android.AndroidInjector

@Subcomponent
interface ProfileFragmentSubcomponent : AndroidInjector<ProfileFragment> {

    @Subcomponent.Builder
    abstract class Builder : AndroidInjector.Builder<ProfileFragment>()
}
