package com.ringoid.data.local.database.dao.user

import androidx.room.*
import com.ringoid.data.local.database.model.feed.UserProfileDbo

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addUserProfile(userProfile: UserProfileDbo)

    @Query("DELETE FROM ${UserProfileDbo.TABLE_NAME} WHERE ${UserProfileDbo.COLUMN_ID} = :userId")
    fun deleteUserProfile(userId: String)

    @Delete
    fun deleteUserProfile(userProfile: UserProfileDbo)
}
