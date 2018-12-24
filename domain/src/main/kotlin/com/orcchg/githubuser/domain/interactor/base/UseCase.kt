package com.orcchg.githubuser.domain.interactor.base

import com.orcchg.githubuser.domain.executor.UseCasePostExecutor
import com.orcchg.githubuser.domain.executor.UseCaseThreadExecutor

abstract class UseCase(protected val threadExecutor: UseCaseThreadExecutor,
                       protected val postExecutor: UseCasePostExecutor)
