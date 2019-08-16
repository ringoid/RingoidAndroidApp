package com.ringoid.data.remote.api

import io.reactivex.Completable
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Headers
import retrofit2.http.POST

interface SlackRestAdapter {

    companion object {
        const val SLACK_URL = "https://slack.com/api/"
    }

    @Headers("Authorization: Bearer xoxp-457467555377-521724314999-637621413061-869a987bacc19dc81fb258a633b34100")
    @FormUrlEncoded
    @POST("chat.postMessage")
    fun postToSlack(@Field("channel") channelId: String,
                    @Field("text") text: String): Completable
}
