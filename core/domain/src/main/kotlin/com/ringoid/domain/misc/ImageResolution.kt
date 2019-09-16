package com.ringoid.domain.misc

enum class ImageResolution(val resolution: String, val w: Int, val h: Int) {
    _480x640("480x640", w = 480, h = 640),
    _640x852("640x852", w = 640, h = 852),
    _720x960("720x960", w = 720, h = 960),
    _750x1000("750x1000", w = 750, h = 1000),
    _828x1104("828x1104", w = 828, h = 1104),
    _1080x1440("1080x1440", w = 1080, h = 1440),
    _1125x1500("1125x1500", w = 1125, h = 1500),
    _1242x1656("1242x1656", w = 1242, h = 1656),
    _1440x1920("1440x1920", w = 1440, h = 1920);

    override fun toString(): String = resolution
}
