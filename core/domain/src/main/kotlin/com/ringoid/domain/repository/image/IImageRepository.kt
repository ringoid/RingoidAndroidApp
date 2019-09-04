package com.ringoid.domain.repository.image

import com.ringoid.domain.model.essence.image.IImageUploadUrlEssence
import com.ringoid.domain.model.essence.image.ImageUploadUrlEssence
import com.ringoid.domain.model.image.Image
import com.ringoid.utility.DebugOnly
import io.reactivex.Completable
import io.reactivex.Single
import java.io.File

interface IImageRepository {

    @DebugOnly
    fun createImageFail(essence: IImageUploadUrlEssence, imageFilePath: String): Completable
    fun createImage(essence: IImageUploadUrlEssence, imageFilePath: String): Single<Image>

    fun getImageUploadUrl(essence: ImageUploadUrlEssence): Single<Image>

    fun uploadImage(url: String, image: File): Completable
}
