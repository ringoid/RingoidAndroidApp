package com.ringoid.origin.profile.view.di

import androidx.fragment.app.Fragment
import com.ringoid.origin.profile.view.ProfileFragment
import dagger.Binds
import dagger.Module
import dagger.android.AndroidInjector
import dagger.android.FragmentKey
import dagger.multibindings.IntoMap

@Module(subcomponents = [ProfileFragmentSubcomponent::class])
abstract class ProfileFragmentModule {

    @Binds @IntoMap @FragmentKey(ProfileFragment::class)
    abstract fun bindProfileFragmentInjectorFactory(builder: ProfileFragmentSubcomponent.Builder): AndroidInjector.Factory<out Fragment>
}
