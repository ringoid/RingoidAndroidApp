package com.ringoid.data.remote.facade

import com.ringoid.data.remote.api.RingoidCloud
import com.ringoid.datainterface.remote.IRingoidCloudFacade
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RingoidCloudFacadeImpl @Inject constructor(private val cloud: RingoidCloud) : IRingoidCloudFacade {

}
