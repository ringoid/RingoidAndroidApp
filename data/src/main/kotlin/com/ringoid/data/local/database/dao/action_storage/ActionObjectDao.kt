package com.ringoid.data.local.database.dao.action_storage

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ringoid.data.local.database.model.action_storage.ActionObjectDbo
import io.reactivex.Single

@Dao
interface ActionObjectDao {

    @Query("SELECT COUNT(*) FROM ${ActionObjectDbo.TABLE_NAME}")
    fun countActionObjects(): Single<Int>

    @Query("SELECT * FROM ${ActionObjectDbo.TABLE_NAME}")
    fun actionObjects(): Single<List<ActionObjectDbo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addActionObjects(objects: List<ActionObjectDbo>)

    @Query("DELETE FROM ${ActionObjectDbo.TABLE_NAME}")
    fun deleteActionObjects()
}
