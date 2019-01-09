package com.ringoid.domain.interactor.base

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
}
