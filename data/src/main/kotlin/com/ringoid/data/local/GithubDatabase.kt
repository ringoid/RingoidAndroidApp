package com.ringoid.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Deprecated("Sample")
@Database(entities = [GithubUser::class], version = 1)
abstract class GithubDatabase : RoomDatabase() {

    abstract fun userDao(): GithubUserDao
}
