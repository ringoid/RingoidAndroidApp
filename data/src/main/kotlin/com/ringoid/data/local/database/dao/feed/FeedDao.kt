package com.ringoid.data.local.database.dao.feed

import androidx.room.*
import com.ringoid.data.local.database.model.feed.ProfileDbo
import com.ringoid.data.local.database.model.feed.ProfileWithImagesDbo
import com.ringoid.data.local.database.model.image.ImageDbo
import io.reactivex.Observable

@Dao
interface FeedDao {

    @Transaction
    @Query("SELECT * FROM ${ProfileDbo.TABLE_NAME}")
    fun profiles(): Observable<List<ProfileWithImagesDbo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addProfiles(profiles: Collection<ProfileDbo>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addProfileImages(images: Collection<ImageDbo>)
}
