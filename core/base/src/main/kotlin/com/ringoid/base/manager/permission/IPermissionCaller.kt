package com.ringoid.base.manager.permission

interface IPermissionCaller {

    fun onGranted(handleCode: Int)
    fun onDenied(handleCode: Int): Boolean
    fun onShowRationale(handleCode: Int)
}
