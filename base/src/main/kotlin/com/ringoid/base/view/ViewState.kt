package com.ringoid.base.view

sealed class IResidual
open class Residual : IResidual()

sealed class ViewState {

    override fun toString(): String = javaClass.simpleName

    object IDLE : ViewState()
    object LOADING : ViewState()
    object PROGRESS : ViewState()  // extends LOADING in some cases
    object CLOSE : ViewState()
    object PAGING : ViewState()

    data class CLEAR(val mode: Int = MODE_DEFAULT) : ViewState() {
        companion object {
            const val MODE_DEFAULT = 0
            const val MODE_EMPTY_DATA = 1
            const val MODE_NEED_REFRESH = 2
        }
    }
    data class DONE(val residual: IResidual) : ViewState()
    data class ERROR(val e: Throwable) : ViewState()
}
