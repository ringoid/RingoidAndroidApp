package com.ringoid.domain.interactor.base

import com.ringoid.domain.exception.MissingRequiredParamsException
import io.reactivex.*

class Params {

    companion object {
        val EMPTY = Params()
    }

    private val map: MutableMap<String, Any> = mutableMapOf()

    @Suppress("Unchecked_Cast")
    fun <T> get(key: String): T? = map[key] as? T

    @Suppress("Unchecked_Cast")
    fun <T> get(klass: Class<T>): T? = map[klass.simpleName] as? T

    fun put(item: Any): Params {
        map[item.javaClass.simpleName] = item
        return this
    }

    fun <T> put(key: String, item: T): Params {
        map[key] = item as Any
        return this
    }
}

// ------------------------------------------------------------------------------------------------
inline fun <reified T> Params.processCompletable(key: String, body: (p: T) -> Completable): Completable =
    get<T>(key)?.let { body(it) } ?: Completable.error { MissingRequiredParamsException() }

inline fun <reified T> Params.processCompletable(klass: Class<T>, body: (p: T) -> Completable): Completable =
    get(klass)?.let { body(it) } ?: Completable.error { MissingRequiredParamsException() }

inline fun <reified T, reified R> Params.processMaybe(key: String, body: (p: T) -> Maybe<R>): Maybe<R> =
    get<T>(key)?.let { body(it) } ?: Maybe.error<R> { MissingRequiredParamsException() }

inline fun <reified T, reified R> Params.processMaybe(klass: Class<T>, body: (p: T) -> Maybe<R>): Maybe<R> =
    get(klass)?.let { body(it) } ?: Maybe.error<R> { MissingRequiredParamsException() }

inline fun <reified T, reified R> Params.processSingle(key: String, body: (p: T) -> Single<R>): Single<R> =
    get<T>(key)?.let { body(it) } ?: Single.error<R> { MissingRequiredParamsException() }

inline fun <reified T, reified R> Params.processSingle(klass: Class<T>, body: (p: T) -> Single<R>): Single<R> =
    get(klass)?.let { body(it) } ?: Single.error<R> { MissingRequiredParamsException() }

inline fun <reified T, reified R> Params.processFlowable(key: String, body: (p: T) -> Flowable<R>): Flowable<R> =
    get<T>(key)?.let { body(it) } ?: Flowable.error<R> { MissingRequiredParamsException() }

inline fun <reified T, reified R> Params.processFlowable(klass: Class<T>, body: (p: T) -> Flowable<R>): Flowable<R> =
    get(klass)?.let { body(it) } ?: Flowable.error<R> { MissingRequiredParamsException() }

inline fun <reified T, reified R> Params.processObservable(key: String, body: (p: T) -> Observable<R>): Observable<R> =
    get<T>(key)?.let { body(it) } ?: Observable.error<R> { MissingRequiredParamsException() }

inline fun <reified T, reified R> Params.processObservable(klass: Class<T>, body: (p: T) -> Observable<R>): Observable<R> =
    get(klass)?.let { body(it) } ?: Observable.error<R> { MissingRequiredParamsException() }
