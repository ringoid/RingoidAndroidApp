package com.ringoid.datainterface.remote

import io.reactivex.Completable

interface ISystemCloudFacade {

    fun postToSlack(channelId: String, text: String): Completable
}
