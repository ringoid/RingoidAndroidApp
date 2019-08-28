package com.ringoid.origin.feed.view.lc

import com.ringoid.base.view.Residual

data class PUSH_NEW_MESSAGES(val profileId: String) : Residual()

object PUSH_NEW_LIKES_TOTAL : Residual()
object PUSH_NEW_MATCHES_TOTAL : Residual()
object PUSH_NEW_MESSAGES_TOTAL : Residual()

object ON_TRANSFER_PROFILE_COMPLETE : Residual()