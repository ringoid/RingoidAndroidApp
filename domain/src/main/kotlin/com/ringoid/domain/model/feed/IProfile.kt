package com.ringoid.domain.model.feed

import com.ringoid.domain.model.IModel
import com.ringoid.domain.model.image.IImage

interface IProfile : IModel {

    val images: List<IImage>
}
