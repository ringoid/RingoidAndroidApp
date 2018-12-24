package com.ringoid.data.remote

import com.ringoid.data.remote.model.auth.AuthCreateProfileResponse
import io.reactivex.Single
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.POST

interface RingoidRestAdapter {

    /* Auth */
    // --------------------------------------------------------------------------------------------
    @POST("create_profile")
    fun createProfile(@Body body: RequestBody): Single<AuthCreateProfileResponse>

    /* Actions */
    // --------------------------------------------------------------------------------------------

    /* Image */
    // --------------------------------------------------------------------------------------------

    /* Feed */
    // --------------------------------------------------------------------------------------------

    /* Test */
    // --------------------------------------------------------------------------------------------
}
