package com.ringoid.origin.model

import com.ringoid.domain.DomainUtil
import com.ringoid.domain.misc.UserProfilePropertiesRaw
import com.ringoid.domain.model.Mappable

data class UserProfileProperties(
    var children: ChildrenProfileProperty = ChildrenProfileProperty.Unknown,
    var education: EducationProfileProperty = EducationProfileProperty.Unknown,
    var hairColor: HairColorProfileProperty = HairColorProfileProperty.Unknown,
    var height: Int = DomainUtil.UNKNOWN_VALUE,
    var income: IncomeProfileProperty = IncomeProfileProperty.Unknown,
    var property: PropertyProfileProperty = PropertyProfileProperty.Unknown,
    var transport: TransportProfileProperty = TransportProfileProperty.Unknown,
    var about: String = "",
    var company: String = "",
    var jobTitle: String = "",
    var name: String = "",
    var socialInstagram: String = "",
    var socialTikTok: String = "",
    var university: String = "",
    var whereFrom: String = "",
    var whereLive: String = "")
    : Mappable<UserProfilePropertiesRaw> {

    companion object {
        fun from(raw: UserProfilePropertiesRaw): UserProfileProperties =
            UserProfileProperties(
                children = ChildrenProfileProperty.from(raw.children),
                education = EducationProfileProperty.from(raw.education),
                hairColor = HairColorProfileProperty.from(raw.hairColor),
                height = raw.height,
                income = IncomeProfileProperty.from(raw.income),
                property = PropertyProfileProperty.from(raw.property),
                transport = TransportProfileProperty.from(raw.transport),
                about = raw.about,
                company = raw.company,
                jobTitle = raw.jobTitle,
                name = raw.name,
                socialInstagram = raw.socialInstagram,
                socialTikTok = raw.socialTikTok,
                university = raw.university,
                whereFrom = raw.whereFrom,
                whereLive = raw.whereLive)
    }

    fun isAllUnknown(): Boolean =
        children == ChildrenProfileProperty.Unknown &&
        education == EducationProfileProperty.Unknown &&
        hairColor == HairColorProfileProperty.Unknown &&
        height == DomainUtil.UNKNOWN_VALUE &&
        income == IncomeProfileProperty.Unknown &&
        property == PropertyProfileProperty.Unknown &&
        transport == TransportProfileProperty.Unknown &&
        about.isBlank() &&
        company.isBlank() &&
        jobTitle.isBlank() &&
        // name is not included
        socialInstagram.isBlank() &&
        socialTikTok.isBlank() &&
        university.isBlank() &&
        whereFrom.isBlank() &&
        whereLive.isBlank()

    override fun map(): UserProfilePropertiesRaw =
        UserProfilePropertiesRaw(
            children = children.id,
            education = education.id,
            hairColor = hairColor.id,
            height = height,
            income = income.id,
            property = property.id,
            transport = transport.id,
            about = about,
            company = company,
            jobTitle = jobTitle,
            name = name,
            socialInstagram = socialInstagram,
            socialTikTok = socialTikTok,
            university = university,
            whereFrom = whereFrom,
            whereLive = whereLive)
}
