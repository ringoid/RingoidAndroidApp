package com.ringoid.origin.feed.view.dialog

import com.ringoid.utility.ICommunicator

interface IFeedItemContextMenuActivity : ICommunicator {

    fun onClose()

    fun onBlock()
    fun onReport(reason: Int)
    fun onReportSheetOpen()
    fun onSendLike()
    fun onSendMatch()
    fun openChat()
    fun openSocialInstagram()
    fun openSocialTiktok()
}
