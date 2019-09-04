package com.ringoid.data.local.database.model.debug

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ringoid.debug.DebugLogItem
import com.ringoid.debug.DebugLogLevel
import com.ringoid.domain.model.Mappable
import com.ringoid.utility.DebugOnly

@DebugOnly
@Entity(tableName = DebugLogItemDbo.TABLE_NAME)
data class DebugLogItemDbo(
    @PrimaryKey @ColumnInfo(name = COLUMN_ID) val id: String,
    @ColumnInfo(name = COLUMN_LOG) val log: String,
    @ColumnInfo(name = COLUMN_LEVEL) val level: Int,
    @ColumnInfo(name = COLUMN_TS) val ts: Long) : Mappable<DebugLogItem> {

    companion object {
        const val COLUMN_ID = "id"
        const val COLUMN_LOG = "log"
        const val COLUMN_LEVEL = "level"
        const val COLUMN_TS = "ts"

        const val TABLE_NAME = "DebugLog"

        fun from(log: DebugLogItem): DebugLogItemDbo =
            DebugLogItemDbo(id = log.id, log = log.log, level = log.level.ordinal, ts = log.ts)
    }

    override fun map(): DebugLogItem = DebugLogItem(id = id, log = log, level = DebugLogLevel.values()[level], ts = ts)
}
