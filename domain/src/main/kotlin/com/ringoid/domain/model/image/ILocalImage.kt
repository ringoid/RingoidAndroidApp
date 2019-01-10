package com.ringoid.domain.model.image

import com.ringoid.domain.model.IModel
import java.io.File

interface ILocalImage : IModel {

    val file: File?
}
