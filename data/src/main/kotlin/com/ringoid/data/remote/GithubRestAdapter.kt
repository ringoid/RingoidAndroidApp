package com.ringoid.data.remote

import com.ringoid.data.remote.model.GithubUserEntity
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path

@Deprecated("Sample")
interface GithubRestAdapter {

    @GET("users")
    fun users(): Single<List<GithubUserEntity>>

    @GET("users/{login}")
    fun user(@Path("login") login: String): Single<GithubUserEntity>
}
