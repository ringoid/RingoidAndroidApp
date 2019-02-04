package com.ringoid.data.local.database.dao.messenger

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ringoid.data.local.database.model.messenger.MessageDbo
import com.ringoid.domain.DomainUtil
import io.reactivex.Single

@Dao
interface MessageDao {

    @Query("SELECT COUNT(*) FROM ${MessageDbo.TABLE_NAME}")
    fun countChatMessages(): Single<Int>

    @Query("SELECT COUNT(*) FROM ${MessageDbo.TABLE_NAME} WHERE ${MessageDbo.COLUMN_CHAT_ID} = :chatId")
    fun countChatMessages(chatId: String): Single<Int>

    // 'chatId' is normally equal to 'peerId', but 'peerId' could be equal to CURRENT_USER_ID
    @Query("SELECT COUNT(*) FROM ${MessageDbo.TABLE_NAME} WHERE ${MessageDbo.COLUMN_CHAT_ID} = :peerId AND ${MessageDbo.COLUMN_PEER_ID} = :peerId")
    fun countPeerMessages(peerId: String): Single<Int>

    @Query("SELECT COUNT(*) FROM ${MessageDbo.TABLE_NAME} WHERE ${MessageDbo.COLUMN_CHAT_ID} = :chatId AND ${MessageDbo.COLUMN_PEER_ID} = ${DomainUtil.CURRENT_USER_ID}")
    fun countUserMessages(chatId: String): Single<Int>

    @Query("SELECT * FROM ${MessageDbo.TABLE_NAME} WHERE ${MessageDbo.COLUMN_CHAT_ID} = :chatId")
    fun messages(chatId: String): Single<List<MessageDbo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addMessages(messages: Collection<MessageDbo>)
}
