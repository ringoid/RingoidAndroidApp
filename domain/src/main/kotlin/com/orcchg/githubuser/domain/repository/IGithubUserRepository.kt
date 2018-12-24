package com.orcchg.githubuser.domain.repository

import com.orcchg.githubuser.domain.model.GithubUser
import io.reactivex.Observable

interface IGithubUserRepository {

    fun users(errorCallback: ErrorCallback? = null): Observable<List<GithubUser>>

    fun user(login: String, errorCallback: ErrorCallback? = null): Observable<GithubUser>
}
