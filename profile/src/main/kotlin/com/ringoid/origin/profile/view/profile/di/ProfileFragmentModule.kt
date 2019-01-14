package com.ringoid.origin.profile.view.profile.di

import androidx.fragment.app.Fragment
import com.ringoid.origin.profile.view.profile.ProfileFragment
import dagger.Binds
import dagger.Module
import dagger.android.AndroidInjector
import dagger.android.support.FragmentKey
import dagger.multibindings.IntoMap

@Module(subcomponents = [ProfileFragmentSubcomponent::class])
abstract class ProfileFragmentModule {

    @Binds @IntoMap @FragmentKey(ProfileFragment::class)
    abstract fun bindProfileFragmentInjectorFactory(builder: ProfileFragmentSubcomponent.Builder): AndroidInjector.Factory<out Fragment>
}
