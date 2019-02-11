package com.ringoid.origin.messenger.view

import com.ringoid.base.view.Residual
import com.ringoid.domain.model.messenger.Message

data class CHAT_MESSAGE_SENT(val message: Message) : Residual()
