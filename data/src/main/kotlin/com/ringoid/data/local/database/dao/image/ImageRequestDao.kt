package com.ringoid.data.local.database.dao.image

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.ringoid.data.local.database.model.image.ImageRequestDbo

@Dao
interface ImageRequestDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addRequest(request: ImageRequestDbo)
}
