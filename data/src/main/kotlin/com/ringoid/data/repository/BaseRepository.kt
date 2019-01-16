package com.ringoid.data.repository

import com.ringoid.data.action_storage.ActionObjectPool
import com.ringoid.data.remote.RingoidCloud
import com.ringoid.domain.repository.ISharedPrefsManager

abstract class BaseRepository(protected val cloud: RingoidCloud, protected val spm: ISharedPrefsManager,
                              protected val aObjPool: ActionObjectPool)
