package com.ringoid.domain.model.feed

import com.ringoid.domain.DomainUtil
import com.ringoid.domain.misc.Gender
import com.ringoid.domain.model.image.IImage
import com.ringoid.domain.model.messenger.Message
import com.ringoid.utility.randomString

data class FeedItem(
    override val id: String,
    override val distanceText: String? = null,
    override val images: List<IImage>,
    override val messages: MutableList<Message>,
    override val lastOnlineStatus: String? = null,
    override val lastOnlineText: String? = null,
    override val age: Int = DomainUtil.UNKNOWN_VALUE,
    override val children: Int = DomainUtil.UNKNOWN_VALUE,
    override val education: Int = DomainUtil.UNKNOWN_VALUE,
    override val gender: Gender = Gender.UNKNOWN,
    override val hairColor: Int = DomainUtil.UNKNOWN_VALUE,
    override val height: Int = DomainUtil.UNKNOWN_VALUE,
    override val income: Int = DomainUtil.UNKNOWN_VALUE,
    override val property: Int = DomainUtil.UNKNOWN_VALUE,
    override val transport: Int = DomainUtil.UNKNOWN_VALUE,
    val isNotSeen: Boolean,
    override val isRealModel: Boolean = true) : IFeedItem {

    fun copyWith(messages: List<Message>): FeedItem =
        FeedItem(
            id = id,
            distanceText = distanceText,
            images = images,
            messages = messages.toMutableList(),
            lastOnlineStatus = lastOnlineStatus,
            lastOnlineText = lastOnlineText,
            age = age,
            children = children,
            education = education,
            gender = gender,
            hairColor = hairColor,
            height = height,
            income = income,
            property = property,
            transport = transport,
            isNotSeen = isNotSeen,
            isRealModel = isRealModel)

    fun messagesToString(): String = messages.joinToString(", ", "{", "}", transform = { it.text })
    fun messagesDetailsToString(): String = messages.joinToString(",", "[", "]", transform = { "{${it.peerId.substring(0..3)}:${it.text}" })

    fun toShortString(): String = "FeedItem(id=${id.substring(0..3)}, ${if (isNotSeen) "Not Seen" else "Seen"}, img=[${images.size}], msg=[${messages.size}])"
}

val EmptyFeedItem = FeedItem(isNotSeen = false, id = randomString(), images = emptyList(), messages = mutableListOf(), isRealModel = false)
