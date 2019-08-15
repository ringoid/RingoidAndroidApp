package com.ringoid.data.local.database.dao.image

import androidx.room.*
import com.ringoid.data.local.database.model.image.ImageRequestDbo
import io.reactivex.Single

@Dao
interface ImageRequestDao {

    @Query("SELECT COUNT(*) FROM ${ImageRequestDbo.TABLE_NAME}")
    fun countRequests(): Single<Int>

    @Query("SELECT * FROM ${ImageRequestDbo.TABLE_NAME}")
    fun requests(): Single<List<ImageRequestDbo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addRequest(request: ImageRequestDbo)

    @Query("DELETE FROM ${ImageRequestDbo.TABLE_NAME}")
    fun deleteAllRequests(): Int

    @Delete
    fun deleteRequest(request: ImageRequestDbo)
}
