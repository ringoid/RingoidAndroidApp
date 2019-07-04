package com.ringoid.origin.error

import android.net.Uri

class DynamicLinkNotExistsException(uri: Uri?) : RuntimeException("Link not found: $uri")
