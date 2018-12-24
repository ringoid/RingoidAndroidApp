package com.orcchg.githubuser.domain.executor

import io.reactivex.Scheduler

interface UseCasePostExecutor {

    fun scheduler(): Scheduler
}
