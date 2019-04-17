package com.ringoid.data.local.database.model.feed

import androidx.room.Embedded
import androidx.room.Relation
import com.ringoid.data.local.database.model.image.ImageDbo
import com.ringoid.data.local.database.model.messenger.MessageDbo
import com.ringoid.domain.DomainUtil

/**
 * For one-to-many relations (one FeedItem to many Images, Messages):
 *
 * @see https://android.jlelse.eu/setting-android-room-in-real-project-58a77469737c
 * @see https://android.jlelse.eu/android-architecture-components-room-relationships-bf473510c14a
 * @see https://androidkt.com/database-relationships/
 */
data class FullFeedItemDbo(
    @Embedded var feedItem: FeedItemDbo = FeedItemDbo(id = DomainUtil.BAD_ID, isNotSeen = false),
    @Relation(parentColumn = FeedItemDbo.COLUMN_ID,
              entityColumn = ImageDbo.COLUMN_PROFILE_ID,
              entity = ImageDbo::class)
    var images: List<ImageDbo> = emptyList(),
    @Relation(parentColumn = FeedItemDbo.COLUMN_ID,
              entityColumn = MessageDbo.COLUMN_CHAT_ID,
              entity = MessageDbo::class)
    var messages: List<MessageDbo> = emptyList())
