package com.ringoid.data.local.model.feed

import androidx.room.Embedded
import androidx.room.Relation
import com.ringoid.data.local.model.image.BaseImageDbo

/**
 * For one-to-many relations (one Profile to many BaseImages):
 *
 * @see https://android.jlelse.eu/setting-android-room-in-real-project-58a77469737c
 * @see https://android.jlelse.eu/android-architecture-components-room-relationships-bf473510c14a
 * @see https://androidkt.com/database-relationships/
 */
data class ProfileWithImagesDbo(
    @Embedded val profile: ProfileDbo,
    @Relation(parentColumn = ProfileDbo.COLUMN_ID,
              entityColumn = BaseImageDbo.COLUMN_PROFILE_ID,
              entity = BaseImageDbo::class)
    val images: List<BaseImageDbo>)
