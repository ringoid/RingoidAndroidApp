package com.ringoid.origin.profile.view.image.di

import com.ringoid.origin.profile.view.image.ProfileImagePageFragment
import dagger.Subcomponent
import dagger.android.AndroidInjector

@Subcomponent
interface ProfileImagePageFragmentSubcomponent : AndroidInjector<ProfileImagePageFragment> {

    @Subcomponent.Builder
    abstract class Builder : AndroidInjector.Builder<ProfileImagePageFragment>()
}
