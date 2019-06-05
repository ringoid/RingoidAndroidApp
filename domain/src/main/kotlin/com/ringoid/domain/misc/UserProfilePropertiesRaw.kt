package com.ringoid.domain.misc

import com.ringoid.domain.DomainUtil

data class UserProfilePropertiesRaw(
    var children: Int = DomainUtil.UNKNOWN_VALUE,  // ChildrenProfileProperty
    var education: Int = DomainUtil.UNKNOWN_VALUE,  // EducationProfileProperty
    var hairColor: Int = DomainUtil.UNKNOWN_VALUE,  // HairColorProfileProperty
    var height: Int = DomainUtil.UNKNOWN_VALUE,
    var income: Int = DomainUtil.UNKNOWN_VALUE,  // IncomeProfileProperty
    var property: Int = DomainUtil.UNKNOWN_VALUE,  // PropertyProfileProperty
    var transport: Int = DomainUtil.UNKNOWN_VALUE)  // TransportProfileProperty
