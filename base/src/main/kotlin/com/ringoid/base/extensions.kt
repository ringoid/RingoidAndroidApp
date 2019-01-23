package com.ringoid.base

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*

/**
 * Inspired with Clean Architecture...Reloaded.
 *
 * @see https://fernandocejas.com/2018/05/07/architecting-android-reloaded/
 * @see https://github.com/android10/Android-CleanArchitecture-Kotlin/blob/master/app/src/main/kotlin/com/fernandocejas/sample/core/extension/Fragment.kt#L33
 */
inline fun <reified T : ViewModel> FragmentActivity.viewModel(factory: ViewModelProvider.Factory, body: T.() -> Unit = {}): T {
    val vm = ViewModelProviders.of(this, factory)[T::class.java]
    vm.body()
    return vm
}

fun <T : ViewModel> FragmentActivity.viewModel(klass: Class<T>, factory: ViewModelProvider.Factory, body: T.() -> Unit = {}): T {
    val vm = ViewModelProviders.of(this, factory)[klass]
    vm.body()
    return vm
}

inline fun <reified T : ViewModel> Fragment.viewModel(factory: ViewModelProvider.Factory, body: T.() -> Unit = {}): T {
    val vm = ViewModelProviders.of(this, factory)[T::class.java]
    vm.body()
    return vm
}

fun <T : ViewModel> Fragment.viewModel(klass: Class<T>, factory: ViewModelProvider.Factory, body: T.() -> Unit = {}): T {
    val vm = ViewModelProviders.of(this, factory)[klass]
    vm.body()
    return vm
}

// ----------------------------------------------
fun <T : Any, L : LiveData<T>> LifecycleOwner.observe(liveData: L, body: (T) -> Unit = {}) =
    liveData.observe(this, Observer(body))

fun <T : Any, L : LiveData<T>> LifecycleOwner.observe(liveData: L, body: (T) -> Unit = {}, also: (T) -> Unit = {}) =
    liveData.observe(this, Observer { body(it) ; also(it) })
