package com.ringoid.data.local.database.dao.messenger

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ringoid.data.local.database.model.messenger.MessageDbo
import io.reactivex.Observable

@Dao
interface MessageDao {

    @Query("SELECT * FROM ${MessageDbo.TABLE_NAME} WHERE ${MessageDbo.COLUMN_PEER_ID} = :peerId")
    fun messages(peerId: String): Observable<List<MessageDbo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addMessages(messages: Collection<MessageDbo>)
}
