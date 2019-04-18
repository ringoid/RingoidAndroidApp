package com.ringoid.data.local.database.dao.feed

import androidx.room.*
import com.ringoid.data.local.database.model.feed.FeedItemDbo
import com.ringoid.data.local.database.model.feed.FullFeedItemDbo
import io.reactivex.Single

@Dao
interface FeedDao {

    @Transaction
    @Query("SELECT * FROM ${FeedItemDbo.TABLE_NAME}")
    fun feedItems(): Single<List<FullFeedItemDbo>>

    @Transaction
    @Query("SELECT * FROM ${FeedItemDbo.TABLE_NAME} WHERE ${FeedItemDbo.COLUMN_SOURCE_FEED} = :sourceFeed")
    fun feedItems(sourceFeed: String): Single<List<FullFeedItemDbo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addFeedItems(feedItems: Collection<FeedItemDbo>)

    @Query("DELETE FROM ${FeedItemDbo.TABLE_NAME}")
    fun deleteFeedItems()
}
