package com.ringoid.data.local.database.dao.feed.property

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ringoid.data.local.database.model.feed.property.LikedFeedItemIdDbo
import com.ringoid.data.local.database.model.feed.property.UserMessagedFeedItemIdDbo
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

    /* Messaged Feed Item */
    // --------------------------------------------------------------------------------------------
    @Query("SELECT * FROM ${UserMessagedFeedItemIdDbo.TABLE_NAME}")
    fun userMessagedFeedItemIds(): Single<List<UserMessagedFeedItemIdDbo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addUserMessagedFeedItemId(item: UserMessagedFeedItemIdDbo)

    @Query("DELETE FROM ${UserMessagedFeedItemIdDbo.TABLE_NAME}")
    fun deleteUserMessagedFeedItemIds(): Int
}
