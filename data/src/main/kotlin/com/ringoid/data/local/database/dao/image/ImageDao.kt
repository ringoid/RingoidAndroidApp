package com.ringoid.data.local.database.dao.image

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ringoid.data.local.database.model.image.ImageDbo

@Dao
interface ImageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addImages(images: Collection<ImageDbo>)

    @Query("DELETE FROM ${ImageDbo.TABLE_NAME}")
    fun deleteImages(): Int
}
