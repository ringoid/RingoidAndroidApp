package com.ringoid.domain.model.actions

interface IDurableAction {

    var timeInMillis: Long
    var isHidden: Boolean
}
