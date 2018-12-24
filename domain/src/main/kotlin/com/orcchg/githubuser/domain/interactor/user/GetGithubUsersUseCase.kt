package com.orcchg.githubuser.domain.interactor.user

import com.orcchg.githubuser.domain.executor.UseCasePostExecutor
import com.orcchg.githubuser.domain.executor.UseCaseThreadExecutor
import com.orcchg.githubuser.domain.interactor.base.SingleUseCase
import com.orcchg.githubuser.domain.model.GithubUser
import com.orcchg.githubuser.domain.repository.IGithubUserRepository
import io.reactivex.Single

class GetGithubUsersUseCase(private val repository: IGithubUserRepository,
                            threadExecutor: UseCaseThreadExecutor, postExecutor: UseCasePostExecutor)
    : SingleUseCase<List<GithubUser>>(threadExecutor, postExecutor) {

    override fun sourceImpl(): Single<List<GithubUser>> = repository.users()
}
