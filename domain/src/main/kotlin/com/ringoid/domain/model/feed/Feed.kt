package com.ringoid.domain.model.feed

data class Feed(val profiles: List<Profile>) {

    fun isEmpty(): Boolean = profiles.isEmpty()

    fun append(other: Feed): Feed =
        if (other.isEmpty()) this
        else Feed(profiles = ArrayList(profiles).apply { addAll(other.profiles) })
}
