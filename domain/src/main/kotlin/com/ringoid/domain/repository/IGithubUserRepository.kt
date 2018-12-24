package com.ringoid.domain.repository

import com.ringoid.domain.model.GithubUser
import io.reactivex.Observable

interface IGithubUserRepository {

    fun users(errorCallback: ErrorCallback? = null): Observable<List<GithubUser>>

    fun user(login: String, errorCallback: ErrorCallback? = null): Observable<GithubUser>
}
