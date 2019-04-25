package com.ringoid.data.local.database.dao.messenger

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ringoid.data.local.database.model.messenger.MessageDbo
import com.ringoid.domain.DomainUtil
import io.reactivex.Maybe
import io.reactivex.Single

@Dao
interface MessageDao {

    @Query("SELECT COUNT(*) FROM ${MessageDbo.TABLE_NAME} WHERE ${MessageDbo.COLUMN_SOURCE_FEED} = :sourceFeed")
    fun countChatMessages(sourceFeed: String = DomainUtil.SOURCE_FEED_MESSAGES): Single<Int>

    @Query("SELECT COUNT(*) FROM ${MessageDbo.TABLE_NAME} WHERE ${MessageDbo.COLUMN_CHAT_ID} = :chatId AND ${MessageDbo.COLUMN_SOURCE_FEED} = :sourceFeed")
    fun countChatMessages(chatId: String, sourceFeed: String = DomainUtil.SOURCE_FEED_MESSAGES): Single<Int>

    @Query("SELECT COUNT(*) FROM ${MessageDbo.TABLE_NAME} WHERE ${MessageDbo.COLUMN_PEER_ID} != '${DomainUtil.CURRENT_USER_ID}' AND ${MessageDbo.COLUMN_SOURCE_FEED} = :sourceFeed")
    fun countPeerMessages(sourceFeed: String = DomainUtil.SOURCE_FEED_MESSAGES): Single<Int>

    // 'chatId' is normally equal to 'peerId', but 'peerId' could be equal to CURRENT_USER_ID
    @Query("SELECT COUNT(*) FROM ${MessageDbo.TABLE_NAME} WHERE ${MessageDbo.COLUMN_CHAT_ID} = :peerId AND ${MessageDbo.COLUMN_PEER_ID} = :peerId AND ${MessageDbo.COLUMN_SOURCE_FEED} = :sourceFeed")
    fun countPeerMessages(peerId: String, sourceFeed: String = DomainUtil.SOURCE_FEED_MESSAGES): Single<Int>

    @Query("SELECT COUNT(*) FROM ${MessageDbo.TABLE_NAME} WHERE ${MessageDbo.COLUMN_CHAT_ID} = :chatId AND ${MessageDbo.COLUMN_PEER_ID} = '${DomainUtil.CURRENT_USER_ID}' AND ${MessageDbo.COLUMN_SOURCE_FEED} = :sourceFeed")
    fun countUserMessages(chatId: String, sourceFeed: String = DomainUtil.SOURCE_FEED_MESSAGES): Single<Int>

    @Query("SELECT COUNT(*) FROM ${MessageDbo.TABLE_NAME} WHERE ${MessageDbo.COLUMN_PEER_ID} != '${DomainUtil.CURRENT_USER_ID}' AND ${MessageDbo.COLUMN_UNREAD} != 0 AND ${MessageDbo.COLUMN_SOURCE_FEED} = :sourceFeed")
    fun countUnreadMessages(sourceFeed: String = DomainUtil.SOURCE_FEED_MESSAGES): Single<Int>

    @Query("SELECT * FROM ${MessageDbo.TABLE_NAME} WHERE ${MessageDbo.COLUMN_CHAT_ID} = :chatId AND ${MessageDbo.COLUMN_SOURCE_FEED} = :sourceFeed")
    fun messages(chatId: String, sourceFeed: String = DomainUtil.SOURCE_FEED_MESSAGES): Maybe<List<MessageDbo>>  // Maybe calls onComplete() rather than Single

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addMessage(message: MessageDbo)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addMessages(messages: Collection<MessageDbo>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertMessage(message: MessageDbo)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertMessages(message: Collection<MessageDbo>)

    @Query("UPDATE ${MessageDbo.TABLE_NAME} SET ${MessageDbo.COLUMN_UNREAD} = 0 WHERE ${MessageDbo.COLUMN_CHAT_ID} = :chatId AND ${MessageDbo.COLUMN_SOURCE_FEED} = :sourceFeed")
    fun markMessagesAsRead(chatId: String, sourceFeed: String = DomainUtil.SOURCE_FEED_MESSAGES): Int

    @Query("DELETE FROM ${MessageDbo.TABLE_NAME}")
    fun deleteMessages()

    @Query("DELETE FROM ${MessageDbo.TABLE_NAME} WHERE ${MessageDbo.COLUMN_CHAT_ID} = :chatId")
    fun deleteMessages(chatId: String)
}
