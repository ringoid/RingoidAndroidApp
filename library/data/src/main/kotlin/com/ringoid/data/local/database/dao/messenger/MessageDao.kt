package com.ringoid.data.local.database.dao.messenger

import androidx.room.*
import com.ringoid.data.local.database.model.messenger.MessageDbo
import com.ringoid.domain.DomainUtil
import io.reactivex.Maybe
import io.reactivex.Single

@Dao
interface MessageDao {

    @Query("SELECT COUNT(*) FROM ${MessageDbo.TABLE_NAME}")
    fun countChatMessages(): Single<Int>

    @Query("SELECT COUNT(*) FROM ${MessageDbo.TABLE_NAME} WHERE ${MessageDbo.COLUMN_CHAT_ID} = :chatId")
    fun countChatMessages(chatId: String): Single<Int>

    @Query("SELECT COUNT(*) FROM ${MessageDbo.TABLE_NAME} WHERE ${MessageDbo.COLUMN_PEER_ID} != '${DomainUtil.CURRENT_USER_ID}'")
    fun countPeerMessages(): Single<Int>

    // 'chatId' is normally equal to 'peerId', but 'peerId' could be equal to CURRENT_USER_ID
    @Query("SELECT COUNT(*) FROM ${MessageDbo.TABLE_NAME} WHERE ${MessageDbo.COLUMN_CHAT_ID} = :peerId AND ${MessageDbo.COLUMN_PEER_ID} = :peerId")
    fun countPeerMessages(peerId: String): Single<Int>

    @Query("SELECT COUNT(*) FROM ${MessageDbo.TABLE_NAME} WHERE ${MessageDbo.COLUMN_CHAT_ID} = :chatId AND ${MessageDbo.COLUMN_PEER_ID} = '${DomainUtil.CURRENT_USER_ID}'")
    fun countUserMessages(chatId: String): Single<Int>

    @Query("SELECT COUNT(*) FROM ${MessageDbo.TABLE_NAME} WHERE ${MessageDbo.COLUMN_PEER_ID} != '${DomainUtil.CURRENT_USER_ID}' AND ${MessageDbo.COLUMN_UNREAD} != 0")
    fun countUnreadMessages(): Single<Int>

    @Query("SELECT * FROM ${MessageDbo.TABLE_NAME}")
    fun messages(): Maybe<List<MessageDbo>>

    @Query("SELECT * FROM ${MessageDbo.TABLE_NAME} WHERE ${MessageDbo.COLUMN_CHAT_ID} = :chatId")
    fun messages(chatId: String): Maybe<List<MessageDbo>>  // Maybe calls onComplete() rather than Single

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addMessage(message: MessageDbo)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addMessages(messages: Collection<MessageDbo>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertMessage(message: MessageDbo)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertMessages(messages: Collection<MessageDbo>)

    @Query("UPDATE ${MessageDbo.TABLE_NAME} SET ${MessageDbo.COLUMN_UNREAD} = 0 WHERE ${MessageDbo.COLUMN_CHAT_ID} = :chatId")
    fun markMessagesAsRead(chatId: String): Int

    @Query("DELETE FROM ${MessageDbo.TABLE_NAME}")
    fun deleteMessages()

    @Query("DELETE FROM ${MessageDbo.TABLE_NAME} WHERE ${MessageDbo.COLUMN_CHAT_ID} = :chatId")
    fun deleteMessages(chatId: String)

    @Delete
    fun deleteMessages(messages: Collection<MessageDbo>)
}
