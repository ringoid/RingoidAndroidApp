package com.ringoid.base.adapter

import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.ListUpdateCallback
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class SimpleAsyncListDiffer<T>(private val cb: ListUpdateCallback, diffCb: BaseDiffCallback<T>)
    : AsyncListDiffer<T>(cb, AsyncDifferConfig.Builder(diffCb).build()) {

    private val currentList: MutableList<T> = mutableListOf()

    override fun getCurrentList(): MutableList<T> = currentList

    @Suppress("CheckResult")
    override fun submitList(list: List<T>?) {
        val countInserted = list?.size ?: 0
        val countRemoved = currentList.size

        Completable.fromCallable { currentList.clear() }
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .toSingle { cb.onRemoved(0, countRemoved) }
            .flatMapCompletable {
                if (list.isNullOrEmpty()) {
                    Completable.complete()
                } else {
                    Completable
                        .fromCallable { currentList.addAll(list) }
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .andThen { cb.onInserted(0, countInserted) }
                }
            }
            .subscribe({}, Timber::e)
    }
}
