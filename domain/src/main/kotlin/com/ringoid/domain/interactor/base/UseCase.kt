package com.ringoid.domain.interactor.base

import com.ringoid.domain.executor.UseCasePostExecutor
import com.ringoid.domain.executor.UseCaseThreadExecutor

abstract class UseCase(protected val threadExecutor: UseCaseThreadExecutor,
                       protected val postExecutor: UseCasePostExecutor
)
