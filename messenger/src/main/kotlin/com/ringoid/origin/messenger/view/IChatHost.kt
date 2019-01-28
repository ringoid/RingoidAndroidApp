package com.ringoid.origin.messenger.view

import com.ringoid.origin.messenger.ChatPayload
import com.ringoid.utility.ICommunicator

interface IChatHost : ICommunicator {

    fun onBlockFromChat(payload: ChatPayload)
    fun onReportFromChat(payload: ChatPayload, reasonNumber: Int)
}
