package com.ringoid.utility.image

import com.facebook.cache.common.CacheKey
import com.facebook.imagepipeline.cache.CountingMemoryCache
import com.facebook.imagepipeline.cache.ImageCacheStatsTracker
import timber.log.Timber

class ImageCacheTracker : ImageCacheStatsTracker {

    private var cacheAdds: Int = 0
    private var cacheHits: Int = 0
    private var cacheMiss: Int = 0

    override fun onBitmapCachePut() {
        ++cacheAdds
        Timber.v("Cache put (bitmap) [${hitAndMiss()}]")
    }

    override fun onBitmapCacheHit(cacheKey: CacheKey) {
        ++cacheHits
        Timber.i("Cache hit (bitmap) [${hitAndMiss()}]")
    }

    override fun onBitmapCacheMiss() {
        ++cacheMiss
        Timber.w("Cache miss (bitmap) [${hitAndMiss()}]")
    }

    // ------------------------------------------
    override fun onMemoryCachePut() {
        ++cacheAdds
        Timber.v("Cache put (encoded) [${hitAndMiss()}]")
    }

    override fun onMemoryCacheHit(cacheKey: CacheKey) {
        ++cacheHits
        Timber.i("Cache hit (encoded) [${hitAndMiss()}]")
    }

    override fun onMemoryCacheMiss() {
        ++cacheMiss
        Timber.w("Cache miss (encoded) [${hitAndMiss()}]")
    }

    // ------------------------------------------
    override fun onDiskCacheHit(cacheKey: CacheKey) {
        ++cacheHits
        Timber.i("Cache hit (disk) [${hitAndMiss()}]")
    }

    override fun onDiskCacheMiss() {
        ++cacheMiss
        Timber.e("Cache miss (disk) [${hitAndMiss()}]")
    }

    override fun onDiskCacheGetFail() {
    }

    // ------------------------------------------
    override fun onStagingAreaHit(cacheKey: CacheKey) {
    }

    override fun onStagingAreaMiss() {
    }

    override fun registerBitmapMemoryCache(bitmapMemoryCache: CountingMemoryCache<*, *>) {
    }

    override fun registerEncodedMemoryCache(encodedMemoryCache: CountingMemoryCache<*, *>) {
    }

    // ------------------------------------------
    private fun hitAndMiss(): String = "$cacheHits / $cacheMiss ($cacheAdds)"
}
