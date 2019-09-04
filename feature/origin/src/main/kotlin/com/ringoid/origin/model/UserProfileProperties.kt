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
    internal var about: String = "",
    internal var company: String = "",
    internal var jobTitle: String = "",
    internal var name: String = "",
    internal var socialInstagram: String = "",
    internal var socialTikTok: String = "",
    internal var statusText: String = "",
    internal var university: String = "",
    internal var whereFrom: String = "",
    internal var whereLive: String = "")
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
                statusText = raw.statusText,
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
        statusText.isBlank() &&
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
            statusText = statusText,
            university = university,
            whereFrom = whereFrom,
            whereLive = whereLive)

    // custom property accessors
    fun about(): String = if (about != DomainUtil.BAD_PROPERTY) about else ""
    fun about(value: String) { about = value }
    fun company(): String = if (company != DomainUtil.BAD_PROPERTY) company else ""
    fun company(value: String) { company = value }
    fun jobTitle(): String = if (jobTitle != DomainUtil.BAD_PROPERTY) jobTitle else ""
    fun jobTitle(value: String) { jobTitle = value }
    fun name(): String = if (name != DomainUtil.BAD_PROPERTY) name else ""
    fun name(value: String) { name = value }
    fun status(): String = if (statusText != DomainUtil.BAD_PROPERTY) statusText else ""
    fun status(value: String) { statusText = value }
    fun instagram(): String = if (socialInstagram != DomainUtil.BAD_PROPERTY) socialInstagram else ""
    fun instagram(value: String) { socialInstagram = value }
    fun tiktok(): String = if (socialTikTok != DomainUtil.BAD_PROPERTY) socialTikTok else ""
    fun tiktok(value: String) { socialTikTok = value }
    fun university(): String = if (university != DomainUtil.BAD_PROPERTY) university else ""
    fun university(value: String) { university = value }
    fun whereFrom(): String = if (whereFrom != DomainUtil.BAD_PROPERTY) whereFrom else ""
    fun whereFrom(value: String) { whereFrom = value }
    fun whereLive(): String = if (whereLive != DomainUtil.BAD_PROPERTY) whereLive else ""
    fun whereLive(value: String) { whereLive = value }
}
