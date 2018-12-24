package com.orcchg.githubuser.data.local

import androidx.room.Dao
import androidx.room.Query
import com.orcchg.githubuser.data.local.model.GithubUserDbo
import io.reactivex.Single

@Dao
interface GithubUserDao {

    @Query("SELECT * FROM ${GithubUserDbo.TABLE_NAME}")
    fun users(): Single<List<GithubUserDbo>>

    @Query("SELECT * FROM ${GithubUserDbo.TABLE_NAME} WHERE :${GithubUserDbo.COLUMN_LOGIN} = :login")
    fun user(login: String): Single<GithubUserDbo>
}
