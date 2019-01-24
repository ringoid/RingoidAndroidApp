package com.ringoid.domain.model

interface IModel : IListModel {

    val id: String

    override fun getModelId(): Long = id.hashCode().toLong()
}
