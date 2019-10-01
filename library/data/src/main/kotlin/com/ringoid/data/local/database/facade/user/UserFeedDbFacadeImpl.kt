package com.ringoid.data.local.database.facade.user

import com.ringoid.data.local.database.dao.feed.UserFeedDao
import com.ringoid.data.local.database.model.feed.ProfileIdDbo
import com.ringoid.datainterface.local.user.IUserFeedDbFacade
import com.ringoid.domain.model.feed.Profile
import com.ringoid.domain.model.mapList
import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserFeedDbFacadeImpl @Inject constructor(private val dao: UserFeedDao) : IUserFeedDbFacade {

    override fun addProfileId(profileId: String) {
        dao.addProfileId(ProfileIdDbo(profileId))
    }

    override fun addProfileIds(profileIds: Collection<String>) {
        profileIds.map { ProfileIdDbo(it) }.also { dao.addProfileIds(it) }
    }

    override fun addProfileModelIds(profiles: Collection<Profile>) {
        profiles.map { ProfileIdDbo(it.id) }.also { dao.addProfileIds(it) }
    }

    override fun countProfileIds(): Single<Int> = dao.countProfileIds()

    override fun deleteProfileId(profileId: String) = dao.deleteProfileId(ProfileIdDbo(profileId))

    override fun deleteProfileIds() = dao.deleteProfileIds()

    override fun insertProfileId(profileId: String): Boolean =
        dao.insertProfileId(ProfileIdDbo(profileId)) > 0

    override fun insertProfileIds(profileIds: Collection<String>) =
        profileIds.map { ProfileIdDbo(it) }.let { dao.insertProfileIds(it).size }

    override fun profileIds(): Single<List<String>> = dao.profileIds().map { it.mapList() }
}
