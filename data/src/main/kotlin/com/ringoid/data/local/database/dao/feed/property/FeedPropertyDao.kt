package com.ringoid.data.local.database.dao.feed.property

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ringoid.data.local.database.model.feed.property.LikedFeedItemIdDbo
import io.reactivex.Single

@Dao
interface FeedPropertyDao {

    /* Liked Images on Feed Item */
    // --------------------------------------------------------------------------------------------
    @Query("SELECT * FROM ${LikedFeedItemIdDbo.TABLE_NAME}")
    fun likedFeedItemIds(): Single<List<LikedFeedItemIdDbo>>

    @Query("SELECT * FROM ${LikedFeedItemIdDbo.TABLE_NAME} WHERE ${LikedFeedItemIdDbo.COLUMN_ID} = :feedItemId")
    fun likedImagesForFeedItemId(feedItemId: String): Single<List<LikedFeedItemIdDbo>>

    @Query("SELECT * FROM ${LikedFeedItemIdDbo.TABLE_NAME} WHERE ${LikedFeedItemIdDbo.COLUMN_ID} IN (:feedItemIds)")
    fun likedImagesForFeedItemIds(feedItemIds: List<String>): Single<List<LikedFeedItemIdDbo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addLikedFeedItemId(item: LikedFeedItemIdDbo)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addLikedFeedItemIds(items: Collection<LikedFeedItemIdDbo>)

    @Query("DELETE FROM ${LikedFeedItemIdDbo.TABLE_NAME} WHERE ${LikedFeedItemIdDbo.COLUMN_IMAGE_ID} = :imageId")
    fun deleteLikedImage(imageId: String): Int

    @Query("DELETE FROM ${LikedFeedItemIdDbo.TABLE_NAME}")
    fun deleteLikedFeedItemIds(): Int
}
