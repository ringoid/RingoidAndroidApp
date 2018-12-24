package com.orcchg.githubuser.domain.repository

import com.orcchg.githubuser.domain.DomainUtil
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit

typealias ErrorCallback = ((e: Throwable) -> Unit)

/**
 * @see https://medium.com/@rikvanv/android-repository-pattern-using-room-retrofit2-and-rxjava2-b48aedd173c
 */
fun <T> network(errorCallback: ErrorCallback?): ObservableTransformer<T, T> =
    ObservableTransformer {
        it.materialize()
          .observeOn(AndroidSchedulers.mainThread())
          .map {
              it.error?.let { errorCallback?.invoke(it) }
              it
          }
          .filter { !it.isOnError }
          .dematerialize<T>()
    }

fun <T> repository(db: Observable<T>, net: Observable<T>): Observable<T> =
    Observable.concatArrayEager(db, net)
              .debounce(DomainUtil.DEBOUNCE_NET, TimeUnit.SECONDS)