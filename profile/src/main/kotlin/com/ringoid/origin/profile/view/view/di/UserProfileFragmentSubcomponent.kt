package com.ringoid.origin.profile.view.profile.di

import com.ringoid.origin.profile.view.profile.UserProfileFragment
import dagger.Subcomponent
import dagger.android.AndroidInjector

@Subcomponent
interface UserProfileFragmentSubcomponent : AndroidInjector<UserProfileFragment> {

    @Subcomponent.Builder
    abstract class Builder : AndroidInjector.Builder<UserProfileFragment>()
}
