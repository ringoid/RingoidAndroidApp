package com.ringoid.data.local.dao.image

import androidx.room.Dao
import androidx.room.Query
import com.ringoid.data.local.model.image.UserImageDbo
import io.reactivex.Observable

@Dao
interface ImageDao {

    @Query("SELECT * FROM ${UserImageDbo.TABLE_NAME}")
    fun userImages(): Observable<List<UserImageDbo>>
}
