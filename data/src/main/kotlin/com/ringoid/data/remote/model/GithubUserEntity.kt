package com.ringoid.data.remote.model

import com.google.gson.annotations.SerializedName
import com.ringoid.domain.model.GithubUser
import com.ringoid.domain.model.Mappable

@Deprecated("Sample")
data class GithubUserEntity(
    @SerializedName(COLUMN_ID) val id: Int,
    @SerializedName(COLUMN_LOGIN) val login: String,
    @SerializedName(COLUMN_NAME) val name: String? = null,
    @SerializedName(COLUMN_BIO) val bio: String? = null,
    @SerializedName(COLUMN_AVATAR_URL) val avatarUrl: String? = null) :
    Mappable<GithubUser> {

    companion object {
        const val COLUMN_ID = "id"
        const val COLUMN_AVATAR_URL = "avatar_url"
        const val COLUMN_BIO = "bio"
        const val COLUMN_LOGIN = "login"
        const val COLUMN_NAME = "name"
    }

    override fun map(): GithubUser =
        GithubUser(id = id, login = login, name = name, bio = bio, avatarUrl = avatarUrl)
}
