package com.orcchg.githubuser.data.remote

import com.orcchg.githubuser.data.remote.model.GithubUserEntity
import io.reactivex.Single

class GithubCloud(private val restAdapter: GithubRestAdapter) {

    fun users(): Single<List<GithubUserEntity>> = restAdapter.users()

    fun user(login: String): Single<GithubUserEntity> = restAdapter.user(login)
}
