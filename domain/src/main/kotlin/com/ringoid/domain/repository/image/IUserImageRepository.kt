package com.ringoid.domain.repository.image

import com.ringoid.domain.misc.ImageResolution
import com.ringoid.domain.model.essence.image.ImageDeleteEssence
import com.ringoid.domain.model.image.UserImage
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject

interface IUserImageRepository : IImageRepository {

    val imageCreate: PublishSubject<String>
    val imageDelete: PublishSubject<String>
    val imageIdChange: PublishSubject<Pair<String, String>>
    val totalUserImages: PublishSubject<Int>

    // --------------------------------------------------------------------------------------------
    fun countUserImages(): Single<Int>

    fun getUserImage(id: String): Single<UserImage>

    fun getUserImages(resolution: ImageResolution): Single<List<UserImage>>

    fun deleteUserImage(essence: ImageDeleteEssence): Completable
    fun deleteLocalUserImages(): Completable
}
