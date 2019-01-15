package com.ringoid.data.local.database.dao.image

import androidx.room.*
import com.ringoid.data.local.database.model.image.BaseImageDbo
import com.ringoid.data.local.database.model.image.ImageDbo
import com.ringoid.data.local.database.model.image.UserImageDbo
import io.reactivex.Observable

@Dao
interface ImageDao {

    @Query("SELECT * FROM ${UserImageDbo.TABLE_NAME}")
    fun userImages(): Observable<List<UserImageDbo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addImage(image: ImageDbo)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addUserImages(images: Collection<UserImageDbo>)

    @Query("DELETE FROM ${UserImageDbo.TABLE_NAME}")
    fun deleteAllImages(): Int

    @Query("DELETE FROM ${UserImageDbo.TABLE_NAME} WHERE ${BaseImageDbo.COLUMN_ID} = :id")
    fun deleteImage(id: String): Int

    @Delete
    fun deleteImage(image: ImageDbo): Int
}
