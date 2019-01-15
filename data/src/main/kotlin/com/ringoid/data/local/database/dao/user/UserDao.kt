package com.ringoid.data.local.database.dao.user

import androidx.room.*
import com.ringoid.data.local.database.model.feed.ProfileDbo

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addUserProfile(profile: ProfileDbo)

    @Query("DELETE FROM ${ProfileDbo.TABLE_NAME} WHERE ${ProfileDbo.COLUMN_ID} = :userId")
    fun deleteUserProfile(userId: String)

    @Delete
    fun deleteUserProfile(profile: ProfileDbo)
}
