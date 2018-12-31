package com.ringoid.base.view

sealed class ViewState {
    object IDLE : ViewState()
    object LOADING : ViewState()
    object CLOSE : ViewState()
    object PAGING : ViewState()

    class ERROR(val e: Throwable) : ViewState()
}
