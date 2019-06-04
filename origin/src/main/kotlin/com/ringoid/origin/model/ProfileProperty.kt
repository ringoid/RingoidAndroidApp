package com.ringoid.origin.model

import androidx.annotation.StringRes
import com.ringoid.domain.misc.Gender
import com.ringoid.origin.R
import com.ringoid.utility.manager.LocaleManager
import com.ringoid.utility.model.INamedEnum
import com.ringoid.widget.model.IListItem
import com.ringoid.widget.model.ListItem

const val AGE_PROPERTY_ID = 97
const val DISTANCE_PROPERTY_ID = 98
const val HEIGHT_PROPERTY_ID = 99

enum class EducationProfileProperty(override val id: Int, @StringRes val resId: Int, override val isDefault: Boolean = false) : IListItem, INamedEnum {
    Unknown(0, R.string.common_unchosen, isDefault = true),
    School(10, R.string.profile_property_education_0),
    College(20, R.string.profile_property_education_1),
    Bachelor(30, R.string.profile_property_education_2),
    Master(40, R.string.profile_property_education_3),
    Multi(50, R.string.profile_property_education_4),
    PhD(60, R.string.profile_property_education_5);

    override fun getLabelResId(): Int = resId

    override fun getNameId(): Int = EDUCATION_PROPERTY_ID
    override fun getName(): String = EDUCATION_PROPERTY

    companion object {
        const val EDUCATION_PROPERTY = "edu"
        const val EDUCATION_PROPERTY_ID = 101

        val values: Array<EducationProfileProperty> = values()

        fun from(id: Int): EducationProfileProperty = values.find { it.id == id } ?: Unknown
        fun valuesForLocale(locale: String?): List<EducationProfileProperty> =
            when (locale) {
                LocaleManager.LANG_EN -> listOf(Unknown, School, College, Master, PhD)
                else -> values.toList()
            }
    }
}

enum class HairColorProfileProperty(val id: Int, @StringRes private val maleResId: Int, @StringRes private val femaleResId: Int, val isDefault: Boolean = false) : INamedEnum {
    Unknown(0, R.string.common_unchosen, R.string.common_unchosen, isDefault = true),
    Black(10, R.string.profile_property_hair_color_m_0, R.string.profile_property_hair_color_f_0),
    Blonde(20, R.string.profile_property_hair_color_m_1, R.string.profile_property_hair_color_f_1),
    Brown(30, R.string.profile_property_hair_color_m_2, R.string.profile_property_hair_color_f_2),
    Red(40, R.string.profile_property_hair_color_m_3, R.string.profile_property_hair_color_f_3),
    Gray(50, R.string.profile_property_hair_color_m_4, R.string.profile_property_hair_color_f_4),
    White(60, R.string.profile_property_hair_color_m_5, R.string.profile_property_hair_color_f_5);

    override fun getNameId(): Int = HAIR_COLOR_PROPERTY_ID
    override fun getName(): String = HAIR_COLOR_PROPERTY

    companion object {
        const val HAIR_COLOR_PROPERTY = "hair"
        const val HAIR_COLOR_PROPERTY_ID = 102

        val values: Array<HairColorProfileProperty> = values()

        fun from(id: Int): HairColorProfileProperty = values.find { it.id == id } ?: Unknown
        fun from(item: ListItem): HairColorProfileProperty = from(id = item.id)

        fun valuesForGender(gender: Gender): List<IListItem> =
            values.map { ListItem(id = it.id, isDefault = it.isDefault, l = { it.resId(gender) }) }
    }

    @StringRes
    fun resId(gender: Gender): Int =
        when (gender) {
            Gender.MALE -> maleResId
            Gender.FEMALE -> femaleResId
            else -> maleResId
        }

    fun valueForGender(gender: Gender): IListItem =
        ListItem(id = id, isDefault = isDefault, l = { resId(gender) })
}

enum class IncomeProfileProperty(override val id: Int, @StringRes val resId: Int, override val isDefault: Boolean = false) : IListItem, INamedEnum {
    Unknown(0, R.string.common_unchosen, isDefault = true),
    Low(10, R.string.profile_property_income_0),
    Middle(20, R.string.profile_property_income_1),
    High(30, R.string.profile_property_income_2),
    Ultra(40, R.string.profile_property_income_3);

    override fun getLabelResId(): Int = resId

    override fun getNameId(): Int = INCOME_PROPERTY_ID
    override fun getName(): String = INCOME_PROPERTY

    companion object {
        const val INCOME_PROPERTY = "income"
        const val INCOME_PROPERTY_ID = 103

        val values: Array<IncomeProfileProperty> = values()

        fun from(id: Int): IncomeProfileProperty = values.find { it.id == id } ?: Unknown
    }
}

enum class PropertyProfileProperty(override val id: Int, @StringRes val resId: Int, override val isDefault: Boolean = false) : IListItem, INamedEnum {
    Unknown(0, R.string.common_unchosen, isDefault = true),
    LiveWithParents(10, R.string.profile_property_property_0),
    LiveInDormitory(20, R.string.profile_property_property_1),
    RentRoom(30, R.string.profile_property_property_2),
    RentFlat(40, R.string.profile_property_property_3),
    RentHouse(50, R.string.profile_property_property_4),
    OwnFlat(60, R.string.profile_property_property_5),
    OwnHouse(70, R.string.profile_property_property_6);

    override fun getLabelResId(): Int = resId

    override fun getNameId(): Int = PROPERTY_PROPERTY_ID
    override fun getName(): String = PROPERTY_PROPERTY

    companion object {
        const val PROPERTY_PROPERTY = "prop"
        const val PROPERTY_PROPERTY_ID = 104

        val values: Array<PropertyProfileProperty> = values()

        fun from(id: Int): PropertyProfileProperty = values.find { it.id == id } ?: Unknown
    }
}

enum class TransportProfileProperty(override val id: Int, @StringRes val resId: Int, override val isDefault: Boolean = false) : IListItem, INamedEnum {
    Unknown(0, R.string.common_unchosen, isDefault = true),
    Walk(10, R.string.profile_property_transport_0),
    PublicTransport(20, R.string.profile_property_transport_1),
    Cycle(30, R.string.profile_property_transport_2),
    Motorcycle(40, R.string.profile_property_transport_3),
    Car(50, R.string.profile_property_transport_4),
    Taxi(60, R.string.profile_property_transport_5),
    HireChauffeur(70, R.string.profile_property_transport_6);

    override fun getLabelResId(): Int = resId

    override fun getNameId(): Int = TRANSPORT_PROPERTY_ID
    override fun getName(): String = TRANSPORT_PROPERTY

    companion object {
        const val TRANSPORT_PROPERTY = "tran"
        const val TRANSPORT_PROPERTY_ID = 105

        val values: Array<TransportProfileProperty> = values()

        fun from(id: Int): TransportProfileProperty = values.find { it.id == id } ?: Unknown
    }
}
