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
    fun addActionObject(aobj: ActionObjectDbo)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addActionObjects(objects: List<ActionObjectDbo>)

    @Query("UPDATE ${ActionObjectDbo.TABLE_NAME} SET ${ActionObjectDbo.COLUMN_USED} = :used WHERE ${ActionObjectDbo.COLUMN_ID} IN (:ids)")
    fun markActionObjectsAsUsed(ids: List<Int>, used: Int = 1): Int

    @Query("DELETE FROM ${ActionObjectDbo.TABLE_NAME}")
    fun deleteActionObjects()

    @Query("DELETE FROM ${ActionObjectDbo.TABLE_NAME} WHERE ${ActionObjectDbo.COLUMN_USED} = 1")
    fun deleteUsedActionObjects()
}
