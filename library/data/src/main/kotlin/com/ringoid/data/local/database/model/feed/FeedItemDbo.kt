package com.ringoid.data.local.database.model.feed

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ringoid.domain.DomainUtil
import com.ringoid.domain.model.feed.FeedItem

@Entity(tableName = FeedItemDbo.TABLE_NAME)
data class FeedItemDbo(
    @PrimaryKey @ColumnInfo(name = COLUMN_ID, index = true) val id: String,
    @ColumnInfo(name = COLUMN_DISTANCE_TEXT) val distanceText: String? = null,
    @ColumnInfo(name = COLUMN_TOTAL_LIKES) val totalLikes: Int = DomainUtil.UNKNOWN_VALUE,
    @ColumnInfo(name = COLUMN_FLAG_NOT_SEEN) val isNotSeen: Boolean = false,
    @ColumnInfo(name = COLUMN_LAST_ONLINE_STATUS) val lastOnlineStatus: String? = null,
    @ColumnInfo(name = COLUMN_LAST_ONLINE_TEXT) val lastOnlineText: String? = null,
    @ColumnInfo(name = COLUMN_PROPERTY_AGE) val age: Int = DomainUtil.UNKNOWN_VALUE,
    @ColumnInfo(name = COLUMN_PROPERTY_CHILDREN) val children: Int = DomainUtil.UNKNOWN_VALUE,
    @ColumnInfo(name = COLUMN_PROPERTY_EDUCATION) val education: Int = DomainUtil.UNKNOWN_VALUE,
    @ColumnInfo(name = COLUMN_PROPERTY_GENDER) val gender: String = "",  // empty for Gender.UNKNOWN
    @ColumnInfo(name = COLUMN_PROPERTY_HAIR_COLOR) val hairColor: Int = DomainUtil.UNKNOWN_VALUE,
    @ColumnInfo(name = COLUMN_PROPERTY_HEIGHT) val height: Int = DomainUtil.UNKNOWN_VALUE,
    @ColumnInfo(name = COLUMN_PROPERTY_INCOME) val income: Int = DomainUtil.UNKNOWN_VALUE,
    @ColumnInfo(name = COLUMN_PROPERTY_PROPERTY) val property: Int = DomainUtil.UNKNOWN_VALUE,
    @ColumnInfo(name = COLUMN_PROPERTY_TRANSPORT) val transport: Int = DomainUtil.UNKNOWN_VALUE,
    @ColumnInfo(name = COLUMN_CUSTOM_PROPERTY_ABOUT) val about: String? = null,
    @ColumnInfo(name = COLUMN_CUSTOM_PROPERTY_COMPANY) val company: String? = null,
    @ColumnInfo(name = COLUMN_CUSTOM_PROPERTY_JOB_TITLE) val jobTitle: String? = null,
    @ColumnInfo(name = COLUMN_CUSTOM_PROPERTY_NAME) val name: String? = null,
    @ColumnInfo(name = COLUMN_CUSTOM_PROPERTY_INSTAGRAM) val instagram: String? = null,
    @ColumnInfo(name = COLUMN_CUSTOM_PROPERTY_TIKTOK) val tiktok: String? = null,
    @ColumnInfo(name = COLUMN_CUSTOM_PROPERTY_STATUS_TEXT) val status: String? = null,
    @ColumnInfo(name = COLUMN_CUSTOM_PROPERTY_UNIVERSITY) val university: String? = null,
    @ColumnInfo(name = COLUMN_CUSTOM_PROPERTY_WHERE_FROM) val whereFrom: String? = null,
    @ColumnInfo(name = COLUMN_CUSTOM_PROPERTY_WHERE_LIVE) val whereLive: String? = null,
    @ColumnInfo(name = COLUMN_SOURCE_FEED) val sourceFeed: String = "") {

    companion object {
        const val COLUMN_ID = "id"
        const val COLUMN_DISTANCE_TEXT = "distanceText"
        const val COLUMN_TOTAL_LIKES = "totalLikes"
        const val COLUMN_FLAG_NOT_SEEN = "notSeen"
        const val COLUMN_LAST_ONLINE_STATUS = "lastOnlineFlag"
        const val COLUMN_LAST_ONLINE_TEXT = "lastOnlineText"
        const val COLUMN_PROPERTY_AGE = "age"
        const val COLUMN_PROPERTY_CHILDREN = "children"
        const val COLUMN_PROPERTY_EDUCATION = "education"
        const val COLUMN_PROPERTY_GENDER = "sex"
        const val COLUMN_PROPERTY_HAIR_COLOR = "hairColor"
        const val COLUMN_PROPERTY_HEIGHT = "height"
        const val COLUMN_PROPERTY_INCOME = "income"
        const val COLUMN_PROPERTY_PROPERTY = "property"
        const val COLUMN_PROPERTY_TRANSPORT = "transport"
        const val COLUMN_CUSTOM_PROPERTY_ABOUT = "about"
        const val COLUMN_CUSTOM_PROPERTY_COMPANY = "company"
        const val COLUMN_CUSTOM_PROPERTY_JOB_TITLE = "jobTitle"
        const val COLUMN_CUSTOM_PROPERTY_NAME = "name"
        const val COLUMN_CUSTOM_PROPERTY_INSTAGRAM = "instagram"
        const val COLUMN_CUSTOM_PROPERTY_TIKTOK = "tiktok"
        const val COLUMN_CUSTOM_PROPERTY_STATUS_TEXT = "statusText"
        const val COLUMN_CUSTOM_PROPERTY_UNIVERSITY = "university"
        const val COLUMN_CUSTOM_PROPERTY_WHERE_FROM = "whereFrom"
        const val COLUMN_CUSTOM_PROPERTY_WHERE_LIVE = "whereLive"
        const val COLUMN_SOURCE_FEED = "sourceFeed"

        const val TABLE_NAME = "FeedItems"

        fun from(feedItem: FeedItem, sourceFeed: String): FeedItemDbo =
            FeedItemDbo(
                id = feedItem.id,
                distanceText = feedItem.distanceText,
                totalLikes = feedItem.totalLikes,
                lastOnlineStatus = feedItem.lastOnlineStatus,
                lastOnlineText = feedItem.lastOnlineText,
                age = feedItem.age,
                children = feedItem.children,
                education = feedItem.education,
                gender = feedItem.gender.string,
                hairColor = feedItem.hairColor,
                height = feedItem.height,
                income = feedItem.income,
                property = feedItem.property,
                transport = feedItem.transport,
                about = feedItem.about,
                company = feedItem.company,
                jobTitle = feedItem.jobTitle,
                name = feedItem.name,
                instagram = feedItem.instagram,
                tiktok = feedItem.tiktok,
                status = feedItem.status,
                university = feedItem.university,
                whereFrom = feedItem.whereFrom,
                whereLive = feedItem.whereLive,
                isNotSeen = feedItem.isNotSeen,
                sourceFeed = sourceFeed)
    }
}
