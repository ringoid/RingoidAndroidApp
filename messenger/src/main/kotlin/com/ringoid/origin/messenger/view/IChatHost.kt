package com.ringoid.origin.messenger.view

import com.ringoid.origin.messenger.ChatPayload
import com.ringoid.utility.ICommunicator

interface IChatHost : ICommunicator {

    fun onBlockFromChat(tag: String, payload: ChatPayload)
    fun onReportFromChat(tag: String, payload: ChatPayload, reasonNumber: Int)
}
