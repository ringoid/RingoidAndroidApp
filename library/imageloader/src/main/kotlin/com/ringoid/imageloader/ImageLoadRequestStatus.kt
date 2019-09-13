package com.ringoid.imageloader

import com.facebook.drawee.interfaces.DraweeController

enum class ImageLoadRequestStatus { NoImageUri, InvalidImageViewRef, Ok }

data class ImageLoadStatus(val status: ImageLoadRequestStatus, val controller: DraweeController? = null)
