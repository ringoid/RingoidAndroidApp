package com.ringoid.base.view

sealed class ViewState {

    override fun toString(): String = javaClass.simpleName

    object NO_STATE : ViewState()  // used for initialization of LiveData
    object IDLE : ViewState()
    object LOADING : ViewState()
    object CLOSE : ViewState()
    object PAGING : ViewState()

    data class CLEAR(val mode: Int = MODE_DEFAULT) : ViewState() {
        companion object {
            const val MODE_DEFAULT = 0
            const val MODE_EMPTY_DATA = 1
            const val MODE_NEED_REFRESH = 2
            const val MODE_CHANGE_FILTERS = 3
        }
    }
    data class ERROR(val e: Throwable) : ViewState()
}
