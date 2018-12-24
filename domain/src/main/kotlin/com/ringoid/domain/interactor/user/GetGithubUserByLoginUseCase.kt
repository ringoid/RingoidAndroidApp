package com.ringoid.domain.interactor.user

import com.ringoid.domain.executor.UseCasePostExecutor
import com.ringoid.domain.executor.UseCaseThreadExecutor
import com.ringoid.domain.interactor.base.SingleUseCase
import com.ringoid.domain.model.GithubUser
import com.ringoid.domain.repository.IGithubUserRepository
import io.reactivex.Single

@Deprecated("Sample")
class GetGithubUserByLoginUseCase(private val login: String, private val repository: IGithubUserRepository,
                                  threadExecutor: UseCaseThreadExecutor, postExecutor: UseCasePostExecutor
)
    : SingleUseCase<GithubUser>(threadExecutor, postExecutor) {

    override fun sourceImpl(): Single<GithubUser> = repository.user(login)
}
