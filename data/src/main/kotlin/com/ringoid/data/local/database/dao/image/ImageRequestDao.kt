package com.ringoid.data.local.database.dao.image

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ringoid.data.local.database.model.image.ImageRequestDbo
import io.reactivex.Single

@Dao
interface ImageRequestDao {

    @Query("SELECT * FROM ${ImageRequestDbo.TABLE_NAME}")
    fun requests(): Single<List<ImageRequestDbo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addRequest(request: ImageRequestDbo)
}
