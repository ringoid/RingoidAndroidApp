package com.ringoid.domain.repository.image

import com.ringoid.domain.model.essence.image.IImageUploadUrlEssence
import com.ringoid.domain.model.essence.image.ImageDeleteEssence
import com.ringoid.domain.model.essence.image.ImageUploadUrlEssence
import com.ringoid.domain.model.image.Image
import com.ringoid.domain.model.image.UserImage
import io.reactivex.Completable
import io.reactivex.Single
import java.io.File

interface IImageRepository {

    fun getUserImages(resolution: String?): Single<List<UserImage>>

    fun deleteUserImage(essence: ImageDeleteEssence): Completable

    // ------------------------------------------------------------------------
    fun createImage(essence: IImageUploadUrlEssence, image: File): Single<Image>

    fun getImageUploadUrl(essence: ImageUploadUrlEssence): Single<Image>

    fun uploadImage(url: String, image: File): Completable
}
