package com.ringoid.domain.model.image

import android.os.Parcelable
import com.ringoid.domain.model.IModel

interface IImage : IModel, Parcelable {

    val uri: String?
}
