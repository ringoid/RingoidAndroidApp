package com.ringoid.data.repository.image

import com.ringoid.domain.model.image.IImage
import io.reactivex.Observable
import io.reactivex.SingleTransformer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageRequestSet @Inject constructor() {

    private val added = mutableMapOf<Int, CreateImageRequest>()
    private val removed = mutableMapOf<Int, DeleteImageRequest>()

    fun add(request: CreateImageRequest) {
        added[request.id] = request
    }

    fun remove(request: DeleteImageRequest) {
        removed[request.id] = request
    }

    fun fulfilled(id: Int) {
        added.remove(id)
        removed.remove(id)
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
