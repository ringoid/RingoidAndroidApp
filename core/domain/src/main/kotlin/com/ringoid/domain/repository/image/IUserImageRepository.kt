package com.ringoid.domain.repository.image

import com.ringoid.domain.misc.ImageResolution
import com.ringoid.domain.model.essence.image.ImageDeleteEssenceUnauthorized
import com.ringoid.domain.model.image.UserImage
import com.ringoid.utility.DebugOnly
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface IUserImageRepository : IImageRepository {

    fun imageBlockedSource(): Observable<String>
    fun imageCreatedSource(): Observable<String>
    fun imageDeletedSource(): Observable<String>
    fun totalUserImagesSource(): Observable<Int>

    // --------------------------------------------------------------------------------------------
    fun countUserImages(): Single<Int>

    fun getUserImage(id: String): Single<UserImage>
    fun getUserImages(resolution: ImageResolution): Single<List<UserImage>>
    fun getUserImagesAsync(resolution: ImageResolution): Observable<List<UserImage>>

    @DebugOnly
    fun deleteUserImageFail(essence: ImageDeleteEssenceUnauthorized): Completable
    fun deleteUserImage(essence: ImageDeleteEssenceUnauthorized): Completable

    fun deleteLocalUserImages(): Completable
    fun deleteLocalUserImageRequests(): Completable
}
