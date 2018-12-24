package com.ringoid.data.local.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ringoid.data.local.model.GithubUserDbo.Companion.TABLE_NAME
import com.ringoid.domain.model.Mappable

@Deprecated("Sample")
@Entity(tableName = TABLE_NAME)
data class GithubUserDbo(
    @PrimaryKey @ColumnInfo(name = COLUMN_ID) val id: Int,
    @ColumnInfo(name = COLUMN_LOGIN) val login: String,
    @ColumnInfo(name = COLUMN_NAME) val name: String? = null,
    @ColumnInfo(name = COLUMN_BIO) val bio: String? = null,
    @ColumnInfo(name = COLUMN_AVATAR_URL) val avatarUrl: String? = null) :
    Mappable<GithubUser> {

    companion object {
        const val COLUMN_ID = "id"
        const val COLUMN_AVATAR_URL = "avatar_url"
        const val COLUMN_BIO = "bio"
        const val COLUMN_LOGIN = "login"
        const val COLUMN_NAME = "name"

        const val TABLE_NAME = "GithubUsers"
    }

    override fun map(): GithubUser =
        GithubUser(id = id, login = login, name = name, bio = bio, avatarUrl = avatarUrl)
}
