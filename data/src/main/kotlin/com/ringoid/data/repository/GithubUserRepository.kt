package com.ringoid.data.repository

import com.ringoid.data.local.GithubUserDao
import com.ringoid.data.remote.GithubCloud
import com.ringoid.domain.model.GithubUser
import com.ringoid.domain.model.mapList
import com.ringoid.domain.repository.ErrorCallback
import com.ringoid.domain.repository.IGithubUserRepository
import com.ringoid.domain.repository.network
import com.ringoid.domain.repository.repository
import io.reactivex.Observable

@Deprecated("Sample")
class GithubUserRepository(private val cloud: GithubCloud, private val local: GithubUserDao) :
    IGithubUserRepository {

    override fun users(errorCallback: ErrorCallback?): Observable<List<GithubUser>> {
        val db = local.users().map { it.mapList() }.toObservable()
        val net = cloud.users().map { it.mapList() }.toObservable().compose(
            network(
                errorCallback
            )
        )
        return repository(db, net)
    }

    override fun user(login: String, errorCallback: ErrorCallback?): Observable<GithubUser> {
        val db = local.user(login).map { it.map() }.toObservable()
        val net = cloud.user(login).map { it.map() }.toObservable().compose(
            network(
                errorCallback
            )
        )
        return repository(db, net)
    }
}
