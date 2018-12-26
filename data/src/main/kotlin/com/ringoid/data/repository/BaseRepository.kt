package com.ringoid.data.repository

import com.ringoid.data.local.SharedPrefsManager
import com.ringoid.data.remote.RingoidCloud

abstract class BaseRepository(protected val cloud: RingoidCloud, protected val spm: SharedPrefsManager)
