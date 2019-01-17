package com.ringoid.origin.feed.view.lmm

import android.app.Application
import com.ringoid.base.viewmodel.BaseViewModel
import com.ringoid.domain.interactor.feed.GetLmmUseCase
import javax.inject.Inject

class LmmViewModel @Inject constructor(private val getLmmUseCase: GetLmmUseCase, app: Application) : BaseViewModel(app)
