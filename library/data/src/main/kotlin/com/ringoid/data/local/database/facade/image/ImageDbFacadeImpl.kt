package com.ringoid.data.local.database.facade.image

import com.ringoid.data.local.database.dao.image.ImageDao
import com.ringoid.data.local.database.model.image.ImageDbo
import com.ringoid.datainterface.image.IImageDbFacade
import com.ringoid.domain.model.feed.FeedItem
import javax.inject.Inject

class ImageDbFacadeImpl @Inject constructor(private val dao: ImageDao) : IImageDbFacade {

    override fun addImages(feedItem: FeedItem) {
        feedItem.images.map { ImageDbo.from(profileId = feedItem.id, image = it) }
            .also { dao.addImages(it) }
    }

    override fun addImages(feedItems: Collection<FeedItem>) {
        mutableListOf<ImageDbo>().apply {
            feedItems.forEach { feedItem ->
                addAll(feedItem.images.map { ImageDbo.from(profileId = feedItem.id, image = it) })
            }
        }
        .also { dao.addImages(it) }
    }

    override fun deleteImages(): Int = dao.deleteImages()
}
