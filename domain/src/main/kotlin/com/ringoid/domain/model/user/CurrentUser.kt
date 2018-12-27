package com.ringoid.domain.model.user

data class CurrentUser(override val id: String, val accessToken: String) : IUser
