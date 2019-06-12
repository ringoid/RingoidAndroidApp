package com.ringoid.data.executor

import com.ringoid.domain.executor.UseCaseThreadExecutor
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UseCaseThreadExecutorImpl @Inject constructor() : UseCaseThreadExecutor {

    private val executor = ThreadPoolExecutor(4, 8, 10, TimeUnit.SECONDS,
                                              LinkedBlockingQueue(), UseCaseThreadFactory(executorName = "$this"))

    override fun execute(command: Runnable) {
        executor.execute(command)
    }

    override fun toString(): String = "Ex${hashCode()}"
}

class UseCaseThreadFactory(private val executorName: String) : ThreadFactory {

    companion object {
        private var INDEX = 0
    }

    override fun newThread(command: Runnable): Thread = Thread(command, "UCThread_$executorName-${INDEX++}")
}
