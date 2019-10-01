package com.ringoid.datainterface.local.user

import com.ringoid.domain.model.feed.Profile
import io.reactivex.Single

interface IUserFeedDbFacade {

    fun addProfileId(profileId: String)

    fun addProfileIds(profileIds: Collection<String>)

    fun addProfileModelIds(profiles: Collection<Profile>)

    fun countProfileIds(): Single<Int>

    fun deleteProfileId(profileId: String)

    fun deleteProfileIds()

    /**
     * Inserts item if not already exists. Returns true, if item has been actually inserted.
     */
    fun insertProfileId(profileId: String): Boolean

    /**
     * Inserts items if not already exists. Returns count of actually inserted items.
     */
    fun insertProfileIds(profileIds: Collection<String>): Int

    fun profileIds(): Single<List<String>>
}
