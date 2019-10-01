package com.ringoid.data.local.database.dao.feed

import androidx.room.*
import com.ringoid.data.local.database.model.feed.ProfileIdDbo
import io.reactivex.Single

@Dao
interface UserFeedDao {

    @Query("SELECT COUNT(*) FROM ${ProfileIdDbo.TABLE_NAME}")
    fun countProfileIds(): Single<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addProfileId(profileId: ProfileIdDbo)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addProfileIds(profileIds: Collection<ProfileIdDbo>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertProfileId(profileId: ProfileIdDbo): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertProfileIds(profileIds: Collection<ProfileIdDbo>): List<Long>

    @Query("SELECT * FROM ${ProfileIdDbo.TABLE_NAME}")
    fun profileIds(): Single<List<ProfileIdDbo>>

    @Delete
    fun deleteProfileId(profileId: ProfileIdDbo)

    @Query("DELETE FROM ${ProfileIdDbo.TABLE_NAME}")
    fun deleteProfileIds()
}
