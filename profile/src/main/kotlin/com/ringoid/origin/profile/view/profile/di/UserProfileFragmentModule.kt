package com.ringoid.origin.profile.view.profile.di

import androidx.fragment.app.Fragment
import com.ringoid.origin.profile.view.profile.UserProfileFragment
import dagger.Binds
import dagger.Module
import dagger.android.AndroidInjector
import dagger.android.support.FragmentKey
import dagger.multibindings.IntoMap

@Module(subcomponents = [UserProfileFragmentSubcomponent::class])
abstract class UserProfileFragmentModule {

    @Binds @IntoMap @FragmentKey(UserProfileFragment::class)
    abstract fun bindUserProfileFragmentInjectorFactory(builder: UserProfileFragmentSubcomponent.Builder): AndroidInjector.Factory<out Fragment>
}
