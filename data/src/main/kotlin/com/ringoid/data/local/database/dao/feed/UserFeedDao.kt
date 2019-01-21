package com.ringoid.data.local.database.dao.feed

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ringoid.data.local.database.model.feed.ProfileIdDbo
import io.reactivex.Single

@Dao
interface UserFeedDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addBlockedProfileId(profileId: ProfileIdDbo)

    @Query("SELECT * FROM ${ProfileIdDbo.TABLE_NAME}")
    fun blockedProfileIds(): Single<List<ProfileIdDbo>>

    @Query("DELETE FROM ${ProfileIdDbo.TABLE_NAME}")
    fun deleteBlockedProfileIds()
}
