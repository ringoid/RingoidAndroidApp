package com.ringoid.domain.model.feed

data class Feed(val profiles: List<Profile>) {

    fun isEmpty(): Boolean = profiles.isEmpty()
}
