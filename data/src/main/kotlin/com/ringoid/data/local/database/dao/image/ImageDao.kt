package com.ringoid.data.local.database.dao.image

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ringoid.data.local.database.model.image.UserImageDbo
import io.reactivex.Observable

@Dao
interface ImageDao {

    @Query("SELECT * FROM ${UserImageDbo.TABLE_NAME}")
    fun userImages(): Observable<List<UserImageDbo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addUserImages(images: Collection<UserImageDbo>)
}
