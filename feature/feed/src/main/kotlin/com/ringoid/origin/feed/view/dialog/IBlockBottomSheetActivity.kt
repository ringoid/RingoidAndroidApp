package com.ringoid.origin.feed.view.dialog

import com.ringoid.utility.ICommunicator

interface IBlockBottomSheetActivity : ICommunicator {

    fun onClose()

    fun onBlock()
    fun onReport(reason: Int)
    fun onReportSheetOpen()
}
