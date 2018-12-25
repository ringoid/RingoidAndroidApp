package com.ringoid.data.local.dao.feed

import androidx.room.Dao
import androidx.room.Query
import com.ringoid.data.local.model.feed.ProfileDbo
import com.ringoid.data.local.model.feed.ProfileWithImagesDbo
import io.reactivex.Observable

@Dao
interface ProfileDao {

    @Query("SELECT * FROM ${ProfileDbo.TABLE_NAME}")
    fun profiles(): Observable<List<ProfileWithImagesDbo>>
}
