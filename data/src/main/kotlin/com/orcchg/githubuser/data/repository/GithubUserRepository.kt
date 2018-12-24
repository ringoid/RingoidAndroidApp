package com.orcchg.githubuser.data.repository

import com.orcchg.githubuser.data.local.GithubUserDao
import com.orcchg.githubuser.data.remote.GithubCloud
import com.orcchg.githubuser.domain.model.GithubUser
import com.orcchg.githubuser.domain.model.mapList
import com.orcchg.githubuser.domain.repository.ErrorCallback
import com.orcchg.githubuser.domain.repository.IGithubUserRepository
import com.orcchg.githubuser.domain.repository.network
import com.orcchg.githubuser.domain.repository.repository
import io.reactivex.Observable

class GithubUserRepository(private val cloud: GithubCloud, private val local: GithubUserDao) :
    IGithubUserRepository {

    override fun users(errorCallback: ErrorCallback?): Observable<List<GithubUser>> {
        val db = local.users().map { it.mapList() }.toObservable()
        val net = cloud.users().map { it.mapList() }.toObservable().compose(network(errorCallback))
        return repository(db, net)
    }

    override fun user(login: String, errorCallback: ErrorCallback?): Observable<GithubUser> {
        val db = local.user(login).map { it.map() }.toObservable()
        val net = cloud.user(login).map { it.map() }.toObservable().compose(network(errorCallback))
        return repository(db, net)
    }
}
