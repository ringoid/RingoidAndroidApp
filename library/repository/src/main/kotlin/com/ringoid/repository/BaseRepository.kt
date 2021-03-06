package com.ringoid.repository

import com.ringoid.datainterface.remote.IRingoidCloudFacade
import com.ringoid.domain.action_storage.IActionObjectPool
import com.ringoid.domain.manager.ISharedPrefsManager

abstract class BaseRepository(protected val cloud: IRingoidCloudFacade, protected val spm: ISharedPrefsManager,
                              protected val aObjPool: IActionObjectPool)
