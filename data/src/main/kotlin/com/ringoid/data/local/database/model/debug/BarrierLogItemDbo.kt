package com.ringoid.data.local.database.model.debug

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ringoid.domain.debug.BarrierLogItem
import com.ringoid.domain.debug.DebugOnly
import com.ringoid.domain.model.Mappable

@DebugOnly
@Entity(tableName = BarrierLogItemDbo.TABLE_NAME)
data class BarrierLogItemDbo(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = COLUMN_ID) val id: Int = 0,
    @ColumnInfo(name = COLUMN_LOG) val log: String,
    @ColumnInfo(name = COLUMN_THREAD_ID) val threadId: Long,
    @ColumnInfo(name = COLUMN_THREAD_NAME) val threadName: String,
    @ColumnInfo(name = COLUMN_TS) val ts: Long) : Mappable<BarrierLogItem> {

    companion object {
        const val COLUMN_ID = "id"
        const val COLUMN_LOG = "log"
        const val COLUMN_THREAD_ID = "threadId"
        const val COLUMN_THREAD_NAME = "threadName"
        const val COLUMN_TS = "ts"

        const val TABLE_NAME = "BarrierLog"

        fun from(log: BarrierLogItem): BarrierLogItemDbo =
            BarrierLogItemDbo(log = log.log, threadId = log.threadId,
                              threadName = log.threadName, ts = log.ts)
    }

    override fun map(): BarrierLogItem =
        BarrierLogItem(log = log, threadId = threadId, threadName = threadName, ts = ts)
}
