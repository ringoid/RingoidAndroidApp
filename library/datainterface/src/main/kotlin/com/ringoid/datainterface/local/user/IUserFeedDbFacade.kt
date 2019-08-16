package com.ringoid.datainterface.local.user

import com.ringoid.domain.model.feed.Profile
import io.reactivex.Single

interface IUserFeedDbFacade {

    fun addProfileId(profileId: String)

    fun addProfileIds(profileIds: Collection<String>)

    fun addProfileModelIds(profiles: Collection<Profile>)

    fun countProfileIds(): Single<Int>

    fun deleteProfileIds()

    fun profileIds(): Single<List<String>>
}
