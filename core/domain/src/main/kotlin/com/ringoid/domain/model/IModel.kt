package com.ringoid.domain.model

interface IModel : IListModel {

    val id: String
    val isRealModel: Boolean

    override fun getModelId(): Long = id.hashCode().toLong()

    fun idWithFirstN(N: Int = 3): String = id.substring(0..minOf(N, id.length - 1))
}

interface IStubModel : IModel
