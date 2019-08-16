package com.ringoid.repository

import com.ringoid.data.remote.RingoidCloud
import com.ringoid.domain.action_storage.IActionObjectPool
import com.ringoid.domain.manager.ISharedPrefsManager

abstract class BaseRepository(protected val cloud: RingoidCloud, protected val spm: ISharedPrefsManager,
                              protected val aObjPool: IActionObjectPool)
