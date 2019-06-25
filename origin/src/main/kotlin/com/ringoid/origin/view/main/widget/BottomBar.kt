package com.ringoid.origin.view.main.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import com.jakewharton.rxbinding3.view.clicks
import com.ringoid.origin.R
import com.ringoid.origin.view.main.NavTab
import com.ringoid.utility.changeVisibility
import com.ringoid.utility.clickDebounce
import com.ringoid.utility.getAttributeDrawable
import kotlinx.android.synthetic.main.widget_bottom_bar.view.*

class BottomBar : LinearLayout {

    private lateinit var ivItemFeed: ImageView
    private lateinit var ivItemLmm: ImageView
    private lateinit var ivItemProfile: ImageView

    var prevSelectedItem: NavTab? = null
        private set
    var selectedItem: NavTab? = null
        set(newValue) {
            if (newValue == null) {
                return
            }

            prevSelectedItem = field
            field = newValue
            if (prevSelectedItem == newValue) {
                reSelectListener?.invoke(newValue)
            } else {
                changeItemAppearance()
                selectListener?.invoke(newValue)
            }
        }

    private var selectListener: ((itemName: NavTab) -> Unit)? = null
    private var reSelectListener: ((itemName: NavTab) -> Unit)? = null

    private var feedIcon: Drawable? = null
    private var feedSelectIcon: Drawable? = null
    private var lmmIcon: Drawable? = null
    private var lmmSelectIcon: Drawable? = null
    private var profileIcon: Drawable? = null
    private var profileSelectIcon: Drawable? = null

    private var countOnLmm: Int = 0

    constructor(context: Context): this(context, null)

    constructor(context: Context, attributes: AttributeSet?): this(context, attributes, 0)

    constructor(context: Context, attributes: AttributeSet?, defStyleAttr: Int): super(context, attributes, defStyleAttr) {
        init(context)
    }

    // ------------------------------------------
    @Suppress("CheckResult")
    private fun init(context: Context) {
        feedIcon = context.getAttributeDrawable(R.attr.refDrawableBottomBarExplore)
        feedSelectIcon = context.getAttributeDrawable(R.attr.refDrawableBottomBarExplorePressed)
        lmmIcon = context.getAttributeDrawable(R.attr.refDrawableBottomBarLmm)
        lmmSelectIcon = context.getAttributeDrawable(R.attr.refDrawableBottomBarLmmPressed)
        profileIcon = context.getAttributeDrawable(R.attr.refDrawableBottomBarProfile)
        profileSelectIcon = context.getAttributeDrawable(R.attr.refDrawableBottomBarProfilePressed)

        minimumHeight = resources.getDimensionPixelSize(R.dimen.main_bottom_bar_height)
        orientation = HORIZONTAL
        LayoutInflater.from(context).inflate(R.layout.widget_bottom_bar, this, true)
        fl_item_feed.clicks().compose(clickDebounce()).subscribe { selectedItem = NavTab.EXPLORE }
        fl_item_lmm.clicks().compose(clickDebounce()).subscribe { selectedItem = NavTab.LMM }
        fl_item_profile.clicks().compose(clickDebounce()).subscribe { selectedItem = NavTab.PROFILE }

        ivItemLmm = findViewById(R.id.iv_item_lmm)
        ivItemProfile = findViewById(R.id.iv_item_profile)
        ivItemFeed = findViewById(R.id.iv_item_feed)
    }

    /* API */
    // --------------------------------------------------------------------------------------------
    fun decrementCountOnLmm(decrementBy: Int) {
        countOnLmm -= decrementBy
        showCountOnLmm(countOnLmm)
    }

    fun setOnNavigationItemSelectedListener(l: ((item: NavTab) -> Unit)?) {
        selectListener = l
    }

    fun setOnNavigationItemReselectedListener(l: ((item: NavTab) -> Unit)?) {
        reSelectListener = l
    }

    fun showBadgeOnLmm(isVisible: Boolean) {
        iv_item_badge_lmm.changeVisibility(isVisible, soft = true)
    }

    fun showCountOnLmm(count: Int) {
        countOnLmm = maxOf(count, 0)  // omit negative values
        tv_lmm_count.text = if (count > 0) " $count" else ""
    }

    fun showWarningOnProfile(isVisible: Boolean) {
        iv_item_warning_profile.changeVisibility(isVisible, soft = true)
    }

    // --------------------------------------------------------------------------------------------
    private fun changeItemAppearance() {
        when (prevSelectedItem) {
            NavTab.EXPLORE -> ivItemFeed.apply { setImageDrawable(feedIcon) }
            NavTab.LMM -> ivItemLmm.apply { setImageDrawable(lmmIcon) }
            NavTab.PROFILE -> ivItemProfile.apply { setImageDrawable(profileIcon) }
            else -> null
        }?.also { it.isSelected = false }

        when (selectedItem) {
            NavTab.EXPLORE -> ivItemFeed.apply { setImageDrawable(feedSelectIcon) }
            NavTab.LMM -> ivItemLmm.apply { setImageDrawable(lmmSelectIcon) }
            NavTab.PROFILE -> ivItemProfile.apply { setImageDrawable(profileSelectIcon) }
            else -> null
        }?.also { it.isSelected = true }
    }
}
