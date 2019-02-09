package com.ringoid.base.view

import com.ringoid.utility.ICommunicator
import com.ringoid.utility.manager.KeyboardStatus
import io.reactivex.Observable

interface IBaseActivity : ICommunicator {

    fun keyboard(): Observable<KeyboardStatus>
}
