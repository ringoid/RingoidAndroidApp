package com.ringoid.data.action_storage

import com.ringoid.domain.action_storage.IActionObjectPool
import com.ringoid.domain.model.actions.*
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActionObjectPool @Inject constructor() : IActionObjectPool {

    private val queue: Queue<ActionObject> = ConcurrentLinkedQueue()

    fun put(aobj: ActionObject) {
        queue.offer(aobj)

        when (aobj) {
            is BlockActionObject -> {}
            is LikeActionObject, is UnlikeActionObject -> {}
            is MessageActionObject -> {}
            is OpenChatActionObject -> {}
            is ViewActionObject -> {}
        }
    }
}
