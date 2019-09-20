package com.ringoid.data.local.database.dao.messenger

import androidx.room.*
import com.ringoid.config.AppMigrationFrom
import com.ringoid.data.local.database.model.messenger.MessageDbo
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.model.messenger.MessageReadStatus
import com.ringoid.domain.model.messenger.READ_BY_PEER
import com.ringoid.domain.model.messenger.READ_BY_USER
import com.ringoid.domain.model.messenger.UNREAD_BY_USER
import com.ringoid.utility.DebugOnly
import io.reactivex.Maybe
import io.reactivex.Single

@Dao
interface MessageDao {

    /**
     * Counts total number of messages.
     */
    @Query("SELECT COUNT(*) FROM ${MessageDbo.TABLE_NAME}")
    fun countChatMessages(): Single<Int>

    /**
     * Counts total number of messages for chat, given by [chatId].
     */
    @Query("SELECT COUNT(*) FROM ${MessageDbo.TABLE_NAME} WHERE ${MessageDbo.COLUMN_CHAT_ID} = :chatId")
    fun countChatMessages(chatId: String): Single<Int>

    /**
     * Counts total number of peer's messages.
     */
    @Query("SELECT COUNT(*) FROM ${MessageDbo.TABLE_NAME} WHERE ${MessageDbo.COLUMN_PEER_ID} != '${DomainUtil.CURRENT_USER_ID}'")
    fun countPeerMessages(): Single<Int>

    /**
     * Counts total number of peer's messages for chat, given by [chatId].
     *
     * @note: [chatId] is normally equal to 'peerId', but 'peerId' could be equal to [DomainUtil.CURRENT_USER_ID].
     */
    @Query("SELECT COUNT(*) FROM ${MessageDbo.TABLE_NAME} WHERE ${MessageDbo.COLUMN_CHAT_ID} = :chatId AND ${MessageDbo.COLUMN_PEER_ID} != '${DomainUtil.CURRENT_USER_ID}'")
    fun countPeerMessages(chatId: String): Single<Int>

    /**
     * Counts total number of current user's messages.
     */
    @Query("SELECT COUNT(*) FROM ${MessageDbo.TABLE_NAME} WHERE ${MessageDbo.COLUMN_PEER_ID} = '${DomainUtil.CURRENT_USER_ID}'")
    fun countUserMessages(): Single<Int>

    /**
     * Counts total number of current user's messages for chat, given by [chatId].
     */
    @Query("SELECT COUNT(*) FROM ${MessageDbo.TABLE_NAME} WHERE ${MessageDbo.COLUMN_CHAT_ID} = :chatId AND ${MessageDbo.COLUMN_PEER_ID} = '${DomainUtil.CURRENT_USER_ID}'")
    fun countUserMessages(chatId: String): Single<Int>

    /**
     * Counts total number of messages that are from peers and haven't been read by the current user.
     *
     * @note: Since [Query] requires compile-time constant, we use direct values from [MessageReadStatus].
     */
    @Query("SELECT COUNT(*) FROM ${MessageDbo.TABLE_NAME} WHERE ${MessageDbo.COLUMN_PEER_ID} != '${DomainUtil.CURRENT_USER_ID}' AND ${MessageDbo.COLUMN_READ_STATUS} = $UNREAD_BY_USER")
    fun countUnreadByUserMessages(): Single<Int>

    /**
     * Get all messages.
     */
    @Query("SELECT * FROM ${MessageDbo.TABLE_NAME}")
    fun messages(): Maybe<List<MessageDbo>>  // Maybe calls onComplete() rather than Single

    /**
     * Get all messages for chat, given by [chatId].
     */
    @Query("SELECT * FROM ${MessageDbo.TABLE_NAME} WHERE ${MessageDbo.COLUMN_CHAT_ID} = :chatId")
    fun messages(chatId: String): Maybe<List<MessageDbo>>  // Maybe calls onComplete() rather than Single

    /**
     * Get all messages for chat, given by [chatId], that are from peer, given by [peerId],
     * and have [readStatus] specified.
     *
     * @note: [chatId] is normally equal to [peerId], but [peerId] could be equal to [DomainUtil.CURRENT_USER_ID].
     *
     * @note: If [peerId] is equal to [DomainUtil.CURRENT_USER_ID], this method will retrieve all
     *        such messages that belong to the current user.
     */
    @Query("SELECT * FROM ${MessageDbo.TABLE_NAME} WHERE ${MessageDbo.COLUMN_CHAT_ID} = :chatId AND ${MessageDbo.COLUMN_PEER_ID} = :peerId AND ${MessageDbo.COLUMN_READ_STATUS} = :readStatus")
    fun messages(chatId: String, peerId: String, readStatus: Int): Maybe<List<MessageDbo>>  // Maybe calls onComplete() rather than Single

    /**
     * Get all messages for chat, given by [chatId], that are from peer and have [readStatus] specified.
     */
    @Query("SELECT * FROM ${MessageDbo.TABLE_NAME} WHERE ${MessageDbo.COLUMN_CHAT_ID} = :chatId AND ${MessageDbo.COLUMN_PEER_ID} != '${DomainUtil.CURRENT_USER_ID}' AND ${MessageDbo.COLUMN_READ_STATUS} = :readStatus")
    fun messagesPeer(chatId: String, readStatus: Int): Maybe<List<MessageDbo>>  // Maybe calls onComplete() rather than Single

    /**
     * Get all messages for chat, given by [chatId], that are from the current user and have [readStatus] specified.
     */
    @Query("SELECT * FROM ${MessageDbo.TABLE_NAME} WHERE ${MessageDbo.COLUMN_CHAT_ID} = :chatId AND ${MessageDbo.COLUMN_PEER_ID} = '${DomainUtil.CURRENT_USER_ID}' AND ${MessageDbo.COLUMN_READ_STATUS} = :readStatus")
    fun messagesUser(chatId: String, readStatus: Int): Maybe<List<MessageDbo>>  // Maybe calls onComplete() rather than Single

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addMessage(message: MessageDbo)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addMessages(messages: Collection<MessageDbo>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertMessage(message: MessageDbo)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertMessages(messages: Collection<MessageDbo>)

    /**
     * Marks current user's messages that are unread as read. Doesn't change read status for peer's messages.
     *
     * @note: Since [Query] requires compile-time constant, we use direct values from [MessageReadStatus].
     */
    @Query("UPDATE ${MessageDbo.TABLE_NAME} SET ${MessageDbo.COLUMN_READ_STATUS} = $READ_BY_USER WHERE ${MessageDbo.COLUMN_CHAT_ID} = :chatId AND ${MessageDbo.COLUMN_READ_STATUS} = $UNREAD_BY_USER")
    fun markMessagesAsReadByUser(chatId: String): Int

    @Query("DELETE FROM ${MessageDbo.TABLE_NAME}")
    fun deleteMessages()

    @Query("DELETE FROM ${MessageDbo.TABLE_NAME} WHERE ${MessageDbo.COLUMN_CHAT_ID} = :chatId")
    fun deleteMessages(chatId: String)

    @Delete
    fun deleteMessages(messages: Collection<MessageDbo>)

    // App migration
    // --------------------------------------------------------------------------------------------
    @AppMigrationFrom(version = 255)
    @Query("UPDATE ${MessageDbo.TABLE_NAME} SET ${MessageDbo.COLUMN_READ_STATUS} = $READ_BY_PEER WHERE ${MessageDbo.COLUMN_PEER_ID} = '${DomainUtil.CURRENT_USER_ID}'")
    fun migrateMarkAllUserMessagesAsReadByPeer()

    /* Debug */
    // --------------------------------------------------------------------------------------------
    @DebugOnly
    @Query("UPDATE ${MessageDbo.TABLE_NAME} SET ${MessageDbo.COLUMN_READ_STATUS} = $UNREAD_BY_USER WHERE ${MessageDbo.COLUMN_CHAT_ID} = :chatId AND ${MessageDbo.COLUMN_PEER_ID} != '${DomainUtil.CURRENT_USER_ID}'")
    fun debugMarkPeerMessagesAsUnreadByUser(chatId: String)
}
