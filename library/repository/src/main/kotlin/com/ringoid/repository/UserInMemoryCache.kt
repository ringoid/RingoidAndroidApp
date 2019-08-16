package com.ringoid.repository

import com.ringoid.domain.memory.IUserInMemoryCache
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserInMemoryCache @Inject constructor() : IUserInMemoryCache {

    private val userImagesCountValue = AtomicInteger(0)

    override fun userImagesCount(): Int = userImagesCountValue.get()

    internal fun setUserImagesCount(count: Int) {
        userImagesCountValue.set(count)
    }
}
