package com.ringoid.domain.model

data class GithubUser(val id: Int, val login: String, val name: String? = null, val bio: String? = null,
                      val avatarUrl: String? = null)
