package com.ringoid.domain.model.user

data class User(override val id: String, override val isRealModel: Boolean = true) : IUser
