package com.ringoid.origin.view.main.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import com.jakewharton.rxbinding3.view.clicks
import com.ringoid.domain.debug.DebugLogUtil
import com.ringoid.origin.R
import com.ringoid.origin.view.main.NavTab
import com.ringoid.utility.changeVisibility
import com.ringoid.utility.clickDebounce
import com.ringoid.utility.getAttributeDrawable
import kotlinx.android.synthetic.main.widget_bottom_bar.view.*

class BottomBar : LinearLayout {

    private lateinit var ivItemFeed: ImageView
    private lateinit var ivItemLikes: ImageView
    private lateinit var ivItemMessages: ImageView
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
                DebugLogUtil.v("Tab reselect: $newValue")
                reSelectListener?.invoke(newValue)
            } else {
                changeItemAppearance()
                DebugLogUtil.v("Tab select: $newValue")
                selectListener?.invoke(newValue)
            }
        }

    private var selectListener: ((itemName: NavTab) -> Unit)? = null
    private var reSelectListener: ((itemName: NavTab) -> Unit)? = null

    private var feedIcon: Drawable? = null
    private var feedSelectIcon: Drawable? = null
    private var likesIcon: Drawable? = null
    private var likesSelectIcon: Drawable? = null
    private var messagesIcon: Drawable? = null
    private var messagesSelectIcon: Drawable? = null
    private var profileIcon: Drawable? = null
    private var profileSelectIcon: Drawable? = null

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
        likesIcon = context.getAttributeDrawable(R.attr.refDrawableBottomBarLikes)
        likesSelectIcon = context.getAttributeDrawable(R.attr.refDrawableBottomBarLikesPressed)
        messagesIcon = context.getAttributeDrawable(R.attr.refDrawableBottomBarMessages)
        messagesSelectIcon = context.getAttributeDrawable(R.attr.refDrawableBottomBarMessagesPressed)
        profileIcon = context.getAttributeDrawable(R.attr.refDrawableBottomBarProfile)
        profileSelectIcon = context.getAttributeDrawable(R.attr.refDrawableBottomBarProfilePressed)

        minimumHeight = resources.getDimensionPixelSize(R.dimen.main_bottom_bar_height)
        orientation = HORIZONTAL
        LayoutInflater.from(context).inflate(R.layout.widget_bottom_bar, this, true)
        fl_item_feed.clicks().compose(clickDebounce()).subscribe { selectedItem = NavTab.EXPLORE      ; DebugLogUtil.v("Selected tab: $selectedItem") }
        fl_item_likes.clicks().compose(clickDebounce()).subscribe { selectedItem = NavTab.LIKES       ; DebugLogUtil.v("Selected tab: $selectedItem") }
        fl_item_messages.clicks().compose(clickDebounce()).subscribe { selectedItem = NavTab.MESSAGES ; DebugLogUtil.v("Selected tab: $selectedItem") }
        fl_item_profile.clicks().compose(clickDebounce()).subscribe { selectedItem = NavTab.PROFILE   ; DebugLogUtil.v("Selected tab: $selectedItem") }

        ivItemFeed = findViewById(R.id.iv_item_feed)
        ivItemLikes = findViewById(R.id.iv_item_likes)
        ivItemMessages = findViewById(R.id.iv_item_messages)
        ivItemProfile = findViewById(R.id.iv_item_profile)
    }

    /* API */
    // --------------------------------------------------------------------------------------------
    fun setOnNavigationItemSelectedListener(l: ((item: NavTab) -> Unit)?) {
        selectListener = l
    }

    fun setOnNavigationItemReselectedListener(l: ((item: NavTab) -> Unit)?) {
        reSelectListener = l
    }

    fun showBadgeOnLikes(isVisible: Boolean) {
        iv_item_badge_likes.changeVisibility(isVisible, soft = true)
    }

    fun showBadgeOnMessages(isVisible: Boolean) {
        iv_item_badge_messages.changeVisibility(isVisible, soft = true)
    }

    fun showWarningOnProfile(isVisible: Boolean) {
        iv_item_warning_profile.changeVisibility(isVisible, soft = true)
    }

    // --------------------------------------------------------------------------------------------
    private fun changeItemAppearance() {
        when (prevSelectedItem) {
            NavTab.EXPLORE -> ivItemFeed.apply { setImageDrawable(feedIcon) }
            NavTab.LIKES -> ivItemLikes.apply { setImageDrawable(likesIcon) }
            NavTab.MESSAGES -> ivItemMessages.apply { setImageDrawable(messagesIcon) }
            NavTab.PROFILE -> ivItemProfile.apply { setImageDrawable(profileIcon) }
            else -> null
        }
        ?.also { it.isSelected = false }

        when (selectedItem) {
            NavTab.EXPLORE -> ivItemFeed.apply { setImageDrawable(feedSelectIcon) }
            NavTab.LIKES -> ivItemLikes.apply { setImageDrawable(likesSelectIcon) }
            NavTab.MESSAGES -> ivItemMessages.apply { setImageDrawable(messagesSelectIcon) }
            NavTab.PROFILE -> ivItemProfile.apply { setImageDrawable(profileSelectIcon) }
            else -> null
        }
        ?.also { it.isSelected = true }
    }
}
