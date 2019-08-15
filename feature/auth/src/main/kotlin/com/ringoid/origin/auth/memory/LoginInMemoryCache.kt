package com.ringoid.origin.auth.memory

import com.ringoid.domain.memory.ILoginInMemoryCache
import javax.inject.Singleton

@Singleton
internal class LoginInMemoryCache : ILoginInMemoryCache {

    private var isNewUserFlag: Boolean = false

    /**
     * Internal in ':auth' module: can only be set there.
     */
    internal fun setNewUser(isNew: Boolean) {
        isNewUserFlag = isNew
    }

    override fun isNewUser(): Boolean {
        val value = isNewUserFlag
        setNewUser(isNew = false)  // drop flag, use just once
        return value
    }
}
