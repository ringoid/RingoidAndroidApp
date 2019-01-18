package com.ringoid.base.view

sealed class IResidual
open class Residual : IResidual()

sealed class ViewState {
    object IDLE : ViewState()
    object LOADING : ViewState()
    object CLEAR : ViewState()
    object CLOSE : ViewState()
    object PAGING : ViewState()

    class DONE(val residual: IResidual) : ViewState()
    class ERROR(val e: Throwable) : ViewState()
}
