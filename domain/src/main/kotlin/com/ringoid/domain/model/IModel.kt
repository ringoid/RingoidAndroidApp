package com.ringoid.domain.model

interface IModel : IListModel {

    val id: String
    val isRealModel: Boolean

    override fun getModelId(): Long = id.hashCode().toLong()
}

interface IStubModel : IModel
