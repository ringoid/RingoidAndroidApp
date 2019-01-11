package com.ringoid.domain.misc

enum class ImageResolution(val resolution: String) {
    _480x640("480x640"),
    _720x960("720x960"),
    _1080x1440("1080x1440"),
    _1440x1920("1440x1920");

    override fun toString(): String = resolution
}
