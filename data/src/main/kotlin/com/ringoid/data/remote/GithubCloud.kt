package com.ringoid.data.remote

import com.ringoid.data.remote.model.GithubUserEntity
import io.reactivex.Single

class GithubCloud(private val restAdapter: GithubRestAdapter) {

    fun users(): Single<List<GithubUserEntity>> = restAdapter.users()

    fun user(login: String): Single<GithubUserEntity> = restAdapter.user(login)
}
