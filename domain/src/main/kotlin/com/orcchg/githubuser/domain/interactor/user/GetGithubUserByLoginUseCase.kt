package com.orcchg.githubuser.domain.interactor.user

import com.orcchg.githubuser.domain.executor.UseCasePostExecutor
import com.orcchg.githubuser.domain.executor.UseCaseThreadExecutor
import com.orcchg.githubuser.domain.interactor.base.SingleUseCase
import com.orcchg.githubuser.domain.model.GithubUser
import com.orcchg.githubuser.domain.repository.IGithubUserRepository
import io.reactivex.Single

class GetGithubUserByLoginUseCase(private val login: String, private val repository: IGithubUserRepository,
                                  threadExecutor: UseCaseThreadExecutor, postExecutor: UseCasePostExecutor)
    : SingleUseCase<GithubUser>(threadExecutor, postExecutor) {

    override fun sourceImpl(): Single<GithubUser> = repository.user(login)
}
