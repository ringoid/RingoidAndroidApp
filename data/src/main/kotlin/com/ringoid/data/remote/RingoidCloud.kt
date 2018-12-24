package com.ringoid.data.remote

import com.ringoid.data.remote.model.auth.AuthCreateProfileResponse
import com.ringoid.data.remote.model.auth.essence.AuthCreateProfileEssence
import io.reactivex.Single
import okhttp3.MediaType
import okhttp3.RequestBody

class RingoidCloud(private val restAdapter: RingoidRestAdapter) {

    fun createProfile(essence: AuthCreateProfileEssence): Single<AuthCreateProfileResponse> {
        val body = RequestBody.create(MediaType.parse("application/json"), essence.toJson())
        return restAdapter.createProfile(body)
    }
}
