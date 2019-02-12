package com.ringoid.data.repository.image

import android.net.Uri
import com.ringoid.data.remote.model.image.UserImageEntity
import com.ringoid.data.remote.model.image.UserImageListResponse
import com.ringoid.domain.model.image.Image
import io.reactivex.Single
import io.reactivex.SingleTransformer
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Collection of image requests (create, delete image) that has been issued but not yet finished
 * with response. Actual response of list of images could then be filtered with this collection
 * becoming actualized with the most recent user's actions on images - add whatever created images
 * that hasn't been added on backend side yet, delete whatever deleted images that hasn't been
 * deleted on backend side yet.
 */
@Singleton
class ImageRequestSet @Inject constructor() {

    private val created = mutableMapOf<String, CreateImageRequest>()
    private val removed = mutableMapOf<String, DeleteImageRequest>()

    fun create(request: CreateImageRequest) {
        created[request.id] = request
    }

    fun create(request: CreateLocalImageRequest) {
        val uri = Uri.parse(request.image.file?.toURI().toString()).toString()
        val image = Image(id = request.id, uri = uri)
        created[request.id] = CreateImageRequest(id = request.id, image = image)
    }

    fun remove(request: DeleteImageRequest) {
        removed[request.id] = request
    }

    fun fulfilled(id: String) {
        created.remove(id)
        removed.remove(id)
    }

    fun clear() {
        created.clear()
        removed.clear()
    }

    fun addCreatedImagesInResponse(): SingleTransformer<UserImageListResponse, UserImageListResponse> =
        SingleTransformer {
            val createdIds = created.values.map { it.image.id }.toMutableList()
            it.flatMap {
                Timber.v("Response analysis: add images to response that have been created locally but not yet provided by the backend")
                createdIds.removeAll(it.images.map { it.originId })
                if (createdIds.isEmpty()) {
                    Single.just(it)
                } else {
                    val list = mutableListOf<UserImageEntity>()
                        .apply {
                            addAll(created.values.map { UserImageEntity.from(it.image) })
                            addAll(it.images)
                        }
                    Single.just(it.copyWith(images = list))
                }
            }
        }

    fun filterOutRemovedImagesInResponse(): SingleTransformer<UserImageListResponse, UserImageListResponse> =
        SingleTransformer {
            val removedIds = removed.values.map { it.imageId }
            it.flatMap {
                Timber.v("Response analysis: remove images from response that have been deleted locally but not yet on the backend")
                if (removedIds.isEmpty()) {
                    Single.just(it)
                } else {
                    val list = mutableListOf<UserImageEntity>()
                        .apply { addAll(it.images.filter { !removedIds.contains(it.originId) }) }
                    Single.just(it.copyWith(images = list))
                }
            }
        }
}
