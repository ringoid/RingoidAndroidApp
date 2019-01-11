package com.ringoid.origin.profile.view.image.di

import androidx.fragment.app.Fragment
import com.ringoid.origin.profile.view.image.ProfileImagePageFragment
import dagger.Binds
import dagger.Module
import dagger.android.AndroidInjector
import dagger.android.support.FragmentKey
import dagger.multibindings.IntoMap

@Module(subcomponents = [ProfileImagePageFragmentSubcomponent::class])
abstract class ProfileImagePageFragmentModule {

    @Binds @IntoMap @FragmentKey(ProfileImagePageFragment::class)
    abstract fun bindProfileImagePageFragmentInjectorFactory(builder: ProfileImagePageFragmentSubcomponent.Builder): AndroidInjector.Factory<out Fragment>
}
