package com.ringoid.base.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import javax.inject.Inject
import javax.inject.Provider

/**
 * Factory that uses Dagger to inject [ViewModel].
 *
 * @see https://blog.kotlin-academy.com/understanding-dagger-2-multibindings-viewmodel-8418eb372848
 * @see https://gist.github.com/krage/058074b40d0819c4b73e43ab9d1afdde
 */
class DaggerViewModelFactory<T : ViewModel> @Inject constructor(private val provider: Provider<T>) : ViewModelProvider.Factory {
    @Suppress("Unchecked_Cast")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = provider.get() as T
}
