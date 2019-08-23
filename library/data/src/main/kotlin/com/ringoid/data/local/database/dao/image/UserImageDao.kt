package com.ringoid.data.local.database.dao.image

import androidx.room.*
import com.ringoid.data.local.database.model.image.BaseImageDbo
import com.ringoid.data.local.database.model.image.UserImageDbo
import io.reactivex.Single

@Dao
interface UserImageDao {

    @Query("SELECT COUNT(*) FROM ${UserImageDbo.TABLE_NAME}")
    fun countUserImages(): Single<Int>

    @Query("SELECT * FROM ${UserImageDbo.TABLE_NAME} WHERE ${BaseImageDbo.COLUMN_ID} = :id")
    fun userImage(id: String): Single<UserImageDbo>

    @Query("SELECT * FROM ${UserImageDbo.TABLE_NAME}")
    fun userImages(): Single<List<UserImageDbo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addUserImage(image: UserImageDbo)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addUserImages(images: Collection<UserImageDbo>)

    @Query("DELETE FROM ${UserImageDbo.TABLE_NAME}")
    fun deleteAllUserImages(): Int

    @Query("DELETE FROM ${UserImageDbo.TABLE_NAME} WHERE ${BaseImageDbo.COLUMN_ID} = :id")
    fun deleteUserImage(id: String): Int

    @Delete
    fun deleteUserImage(image: UserImageDbo): Int

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateUserImage(image: UserImageDbo): Int

    @Query("UPDATE ${UserImageDbo.TABLE_NAME} SET ${BaseImageDbo.COLUMN_URI} = :uri, ${UserImageDbo.COLUMN_NUMBER_LIKES} = :numberOfLikes, ${UserImageDbo.COLUMN_FLAG_BLOCKED} = :isBlocked, ${UserImageDbo.COLUMN_SORT_POSITION} = :sortPosition WHERE ${UserImageDbo.COLUMN_ORIGIN_ID} = :originImageId")
    fun updateUserImageByOriginId(originImageId: String, uri: String, numberOfLikes: Int, isBlocked: Boolean, sortPosition: Int): Int

    @Query("SELECT ${UserImageDbo.COLUMN_FLAG_BLOCKED} FROM ${UserImageDbo.TABLE_NAME} WHERE ${UserImageDbo.COLUMN_ORIGIN_ID} = :originImageId")
    fun isUserImageBlockedByOriginId(originImageId: String): Boolean
}
