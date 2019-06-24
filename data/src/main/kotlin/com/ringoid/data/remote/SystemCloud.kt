package com.ringoid.data.remote

import io.reactivex.Completable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SystemCloud @Inject constructor(private val restAdapter: SlackRestAdapter) {

    fun postToSlack(channelId: String, text: String): Completable =
        restAdapter.postToSlack(channelId = channelId, text = text)
}
