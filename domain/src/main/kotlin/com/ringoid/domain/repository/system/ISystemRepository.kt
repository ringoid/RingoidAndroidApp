package com.ringoid.domain.repository.system

import io.reactivex.Completable

interface ISystemRepository {

    fun postToSlack(channelId: String, text: String): Completable
}
