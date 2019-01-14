package com.ringoid.origin.profile.view.profile.di

import com.ringoid.origin.profile.view.profile.ProfileFragment
import dagger.Subcomponent
import dagger.android.AndroidInjector

@Subcomponent
interface ProfileFragmentSubcomponent : AndroidInjector<ProfileFragment> {

    @Subcomponent.Builder
    abstract class Builder : AndroidInjector.Builder<ProfileFragment>()
}
