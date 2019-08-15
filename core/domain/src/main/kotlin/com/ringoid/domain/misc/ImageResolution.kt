package com.ringoid.domain.misc

enum class ImageResolution(val resolution: String, val w: Int, val h: Int) {
    _480x640("480x640", w = 480, h = 640),
    _720x960("720x960", w = 720, h = 960),
    _750x1000("750x1000", w = 750, h = 1000),
    _828x1344("828x1344", w = 828, h = 1344),
    _1080x1440("1080x1440", w = 1080, h = 1440),
    _1125x1827("1125x1827", w = 1125, h = 1827),
    _1242x2016("1242x2016", w = 1242, h = 2016),
    _1440x1920("1440x1920", w = 1440, h = 1920);

    override fun toString(): String = resolution
}
