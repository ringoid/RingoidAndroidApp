package com.ringoid.data.local.database.dao.debug

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ringoid.data.local.database.model.debug.DebugLogItemDbo
import com.ringoid.domain.debug.DebugOnly
import io.reactivex.Single

@Dao @DebugOnly
interface DebugLogDao {

    @Query("SELECT * FROM ${DebugLogItemDbo.TABLE_NAME}")
    fun debugLog(): Single<List<DebugLogItemDbo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addDebugLog(log: DebugLogItemDbo)

    @Query("DELETE FROM ${DebugLogItemDbo.TABLE_NAME}")
    fun deleteDebugLog()
}
