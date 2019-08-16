package com.ringoid.datainterface.remote

interface IRingoidCloudFacade {

    /* User (Auth, Profile) */
    // --------------------------------------------------------------------------------------------
//    fun createUserProfile(essence: AuthCreateProfileEssence): Single<AuthCreateProfileResponse>
//
//    fun deleteUserProfile(accessToken: String): Single<BaseResponse>
//
//    fun getUserSettings(accessToken: String): Single<UserSettingsResponse>
//
//    fun updateUserSettings(essence: UpdateUserSettingsEssence): Single<BaseResponse>
//
//    fun updateUserProfile(essence: UpdateUserProfileEssence): Single<BaseResponse>
//
//    // ------------------------------------------
//    fun applyReferralCode(essence: ReferralCodeEssence): Single<BaseResponse>
//
//    /* Actions */
//    // --------------------------------------------------------------------------------------------
//    fun commitActions(essence: CommitActionsEssence): Single<CommitActionsResponse>
//
//    /* Image */
//    // --------------------------------------------------------------------------------------------
//    fun getImageUploadUrl(essence: ImageUploadUrlEssence): Single<ImageUploadUrlResponse>
//
//    fun getUserImages(accessToken: String, resolution: ImageResolution): Single<UserImageListResponse>
//
//    fun deleteUserImage(essence: ImageDeleteEssence): Single<BaseResponse>
//
//    fun uploadImage(url: String, image: File): Completable
//
//    /* Feed */
//    // --------------------------------------------------------------------------------------------
//    fun getChat(accessToken: String, resolution: ImageResolution, peerId: String, lastActionTime: Long = 0L)
//
//    fun getDiscover(accessToken: String, resolution: ImageResolution, limit: Int?, filter: Filters?,
//                    lastActionTime: Long = 0L): Single<FeedResponse>
//
//    @Deprecated("LMM -> LC")
//    fun getNewFaces(accessToken: String, resolution: ImageResolution, limit: Int?, lastActionTime: Long = 0L)
//
//    @Deprecated("LMM -> LC")
//    fun getLmm(accessToken: String, resolution: ImageResolution, source: String?, lastActionTime: Long = 0L)
//
//    fun getLc(accessToken: String, resolution: ImageResolution, limit: Int?, filter: Filters?,
//              source: String?, lastActionTime: Long = 0L): Single<LmmResponse>
//
//    /* Push */
//    // --------------------------------------------------------------------------------------------
//    fun updatePushToken(essence: PushTokenEssence): Single<BaseResponse>
//
//    /* Test */
//    // --------------------------------------------------------------------------------------------
//    fun debugTimeout(): Completable
//    fun debugInvalidToken(): Completable
//    fun debugNotSuccess(): Completable
//    fun debugResponseWith404(): Completable
//    fun debugOldVersion(): Completable
//    fun debugServerError(): Completable
}
