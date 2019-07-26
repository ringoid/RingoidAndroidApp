package com.ringoid.data.local.database.dao.debug

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ringoid.data.local.database.model.debug.BarrierLogItemDbo
import com.ringoid.domain.debug.DebugOnly
import io.reactivex.Single

@Dao @DebugOnly
interface BarrierLogDao {

    @Query("SELECT * FROM ${BarrierLogItemDbo.TABLE_NAME}")
    fun log(): Single<List<BarrierLogItemDbo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addLog(log: BarrierLogItemDbo)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addLogs(logs: List<BarrierLogItemDbo>)

    @Query("DELETE FROM ${BarrierLogItemDbo.TABLE_NAME}")
    fun deleteLog()
}
