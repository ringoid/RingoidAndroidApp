package com.ringoid.data.local.database.dao.image

import androidx.room.*
import com.ringoid.data.local.database.model.image.BaseImageDbo
import com.ringoid.data.local.database.model.image.UserImageDbo
import io.reactivex.Observable
import io.reactivex.Single

@Dao
interface ImageDao {

    @Query("SELECT COUNT(*) FROM ${UserImageDbo.TABLE_NAME}")
    fun countUserImages(): Single<Int>

    @Query("SELECT * FROM ${UserImageDbo.TABLE_NAME} WHERE ${BaseImageDbo.COLUMN_ID} = :id")
    fun userImage(id: String): Single<UserImageDbo>

    @Query("SELECT * FROM ${UserImageDbo.TABLE_NAME}")
    fun userImages(): Observable<List<UserImageDbo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addImage(image: UserImageDbo)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addUserImages(images: Collection<UserImageDbo>)

    @Query("DELETE FROM ${UserImageDbo.TABLE_NAME}")
    fun deleteAllImages(): Int

    @Query("DELETE FROM ${UserImageDbo.TABLE_NAME} WHERE ${BaseImageDbo.COLUMN_ID} = :id")
    fun deleteImage(id: String): Int

    @Delete
    fun deleteImage(image: UserImageDbo): Int
}
