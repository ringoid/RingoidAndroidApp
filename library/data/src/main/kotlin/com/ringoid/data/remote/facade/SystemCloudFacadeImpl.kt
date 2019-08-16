package com.ringoid.data.remote.facade

import com.ringoid.data.remote.api.SystemCloud
import com.ringoid.datainterface.remote.ISystemCloudFacade
import io.reactivex.Completable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SystemCloudFacadeImpl @Inject constructor(private val cloud: SystemCloud) : ISystemCloudFacade {

    override fun postToSlack(channelId: String, text: String): Completable =
        cloud.postToSlack(channelId = channelId, text = text)
}
