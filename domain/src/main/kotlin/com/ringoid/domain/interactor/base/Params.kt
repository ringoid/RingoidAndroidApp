package com.ringoid.domain.interactor.base

class Params {

    companion object {
        val EMPTY = Params()
    }

    private val map: MutableMap<String, Any> = mutableMapOf()

    fun get(key: String): Any? = map[key]

    @Suppress("Unchecked_Cast")
    fun <T> get(klass: Class<T>): T? = map[klass.simpleName] as? T

    fun put(item: Any) {
        map[item.javaClass.simpleName] = item
    }
}
