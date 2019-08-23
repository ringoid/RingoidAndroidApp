package com.ringoid.data.local.database.dao.feed

import androidx.room.*
import com.ringoid.data.local.database.model.feed.FeedItemDbo
import com.ringoid.data.local.database.model.feed.FullFeedItemDbo
import io.reactivex.Single

@Dao
interface FeedDao {

    @Query("SELECT COUNT(*) FROM ${FeedItemDbo.TABLE_NAME}")
    fun countFeedItems(): Single<Int>

    @Query("SELECT COUNT(*) FROM ${FeedItemDbo.TABLE_NAME} WHERE ${FeedItemDbo.COLUMN_SOURCE_FEED} = :sourceFeed")
    fun countFeedItems(sourceFeed: String): Single<Int>

    @Transaction
    @Query("SELECT * FROM ${FeedItemDbo.TABLE_NAME} WHERE ${FeedItemDbo.COLUMN_ID} = :profileId")
    fun feedItem(profileId: String): Single<FullFeedItemDbo>

    @Transaction
    @Query("SELECT * FROM ${FeedItemDbo.TABLE_NAME}")
    fun feedItems(): Single<List<FullFeedItemDbo>>

    @Transaction
    @Query("SELECT * FROM ${FeedItemDbo.TABLE_NAME} WHERE ${FeedItemDbo.COLUMN_SOURCE_FEED} = :sourceFeed")
    fun feedItems(sourceFeed: String): Single<List<FullFeedItemDbo>>

    @Query("SELECT ${FeedItemDbo.COLUMN_ID} FROM ${FeedItemDbo.TABLE_NAME}")
    fun feedItemIds(): Single<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addFeedItems(feedItems: Collection<FeedItemDbo>)

    @Query("UPDATE ${FeedItemDbo.TABLE_NAME} SET ${FeedItemDbo.COLUMN_FLAG_NOT_SEEN} = :isNotSeen WHERE ${FeedItemDbo.COLUMN_ID} = :feedItemId")
    fun markFeedItemAsSeen(feedItemId: String, isNotSeen: Boolean): Int

    @Query("UPDATE ${FeedItemDbo.TABLE_NAME} SET ${FeedItemDbo.COLUMN_SOURCE_FEED} = :sourceFeed WHERE ${FeedItemDbo.COLUMN_ID} = :feedItemId")
    fun updateSourceFeed(feedItemId: String, sourceFeed: String): Int

    @Query("DELETE FROM ${FeedItemDbo.TABLE_NAME}")
    fun deleteFeedItems(): Int
}
