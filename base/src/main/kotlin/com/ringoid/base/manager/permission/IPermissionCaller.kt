package com.ringoid.base.manager.permission

interface IPermissionCaller {

    fun onGranted()
    fun onDenied()
}
