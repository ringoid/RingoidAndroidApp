package com.ringoid.domain.model

@Deprecated("Sample")
data class GithubUser(val id: Int, val login: String, val name: String? = null, val bio: String? = null,
                      val avatarUrl: String? = null)
