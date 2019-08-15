package com.ringoid.data.local.database.dao.feed

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
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

    @Query("SELECT * FROM ${ProfileIdDbo.TABLE_NAME}")
    fun profileIds(): Single<List<ProfileIdDbo>>

    @Query("DELETE FROM ${ProfileIdDbo.TABLE_NAME}")
    fun deleteProfileIds()
}
