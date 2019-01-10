package com.ringoid.data.repository.image

import com.ringoid.domain.model.image.IImage
import io.reactivex.Observable
import io.reactivex.SingleTransformer
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

    private val created = mutableMapOf<Int, CreateImageRequest>()
    private val removed = mutableMapOf<Int, DeleteImageRequest>()

    fun create(request: CreateImageRequest) {
        created[request.id] = request
    }

    fun remove(request: DeleteImageRequest) {
        removed[request.id] = request
    }

    fun fulfilled(id: Int) {
        created.remove(id)
        removed.remove(id)
    }

    fun addCreatedImages(): SingleTransformer<List<IImage>, List<IImage>> =
        SingleTransformer {
            val createdIds = created.values.map { it.image.id }
            val createdImages = created.values.map { it.image }
            it.flatMap {
                Observable.fromIterable(it)
                    .filter { !createdIds.contains(it.id) }
                    .toList(it.size)
                    .map { it.toMutableList().apply { addAll(createdImages) } }
            }
        }

    fun filterOutRemovedImages(): SingleTransformer<List<IImage>, List<IImage>> =
        SingleTransformer {
            val removedIds = removed.values.map { it.imageId }
            it.flatMap {
                Observable.fromIterable(it)
                    .filter { !removedIds.contains(it.id) }
                    .toList(it.size)
            }
        }
}
