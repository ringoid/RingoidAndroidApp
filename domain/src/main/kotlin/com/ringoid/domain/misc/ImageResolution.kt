package com.ringoid.domain.misc

enum class ImageResolution(val resolution: String, val w: Int, val h: Int) {
    _480x640("480x640", w = 480, h = 640),
    _720x960("720x960", w = 720, h = 960),
    _1080x1440("1080x1440", w = 1080, h = 1440),
    _1440x1920("1440x1920", w = 1440, h = 1920);

    override fun toString(): String = resolution
}
