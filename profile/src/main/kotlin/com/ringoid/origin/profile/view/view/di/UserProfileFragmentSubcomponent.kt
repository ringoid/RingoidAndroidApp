package com.ringoid.origin.profile.view.view.di

import com.ringoid.origin.profile.view.view.UserProfileFragment
import dagger.Subcomponent
import dagger.android.AndroidInjector

@Subcomponent
interface UserProfileFragmentSubcomponent : AndroidInjector<UserProfileFragment> {

    @Subcomponent.Builder
    abstract class Builder : AndroidInjector.Builder<UserProfileFragment>()
}
