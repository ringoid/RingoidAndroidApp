package com.ringoid.domain.model.user

class CurrentUser(val accessToken: String, id: String) : User(id = id)
