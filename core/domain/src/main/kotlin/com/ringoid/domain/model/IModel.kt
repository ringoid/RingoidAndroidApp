package com.ringoid.domain.model

import com.ringoid.utility.goodHashCode

interface IModel : IListModel {

    val id: String
    val isRealModel: Boolean

    override fun getModelId(): Long = id.goodHashCode()

    fun idWithFirstN(N: Int = 3): String = id.substring(0..minOf(N, id.length - 1))
}

interface IStubModel : IModel
