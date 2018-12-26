package com.ringoid.data.local.database.dao.feed

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ringoid.data.local.database.model.feed.ProfileDbo
import com.ringoid.data.local.database.model.feed.ProfileWithImagesDbo
import com.ringoid.data.local.database.model.image.ImageDbo
import io.reactivex.Observable

@Dao
interface FeedDao {

    @Query("SELECT * FROM ${ProfileDbo.TABLE_NAME}")
    fun profiles(): Observable<List<ProfileWithImagesDbo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addProfiles(profiles: Collection<ProfileDbo>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addProfileImages(images: Collection<ImageDbo>)
}
