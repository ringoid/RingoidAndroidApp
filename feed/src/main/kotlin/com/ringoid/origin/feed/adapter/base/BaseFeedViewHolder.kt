package com.ringoid.origin.feed.adapter.base

import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader
import com.bumptech.glide.util.ViewPreloadSizeProvider
import com.ringoid.base.adapter.BaseViewHolder
import com.ringoid.domain.BuildConfig
import com.ringoid.origin.AppRes
import com.ringoid.origin.feed.OriginR_drawable
import com.ringoid.origin.feed.R
import com.ringoid.origin.feed.adapter.profile.ProfileImageAdapter
import com.ringoid.origin.feed.model.FeedItemVO
import com.ringoid.origin.feed.model.OnlineStatus
import com.ringoid.origin.feed.model.ProfileImageVO
import com.ringoid.origin.model.*
import com.ringoid.origin.view.common.visibility_tracker.TrackingBus
import com.ringoid.utility.changeVisibility
import com.ringoid.utility.collection.EqualRange
import com.ringoid.utility.linearLayoutManager
import com.ringoid.widget.view.LabelView
import com.ringoid.widget.view.rv.EnhancedPagerSnapHelper
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.rv_item_feed_profile_content.view.*
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper
import timber.log.Timber

interface IFeedViewHolder {

    var onBeforeLikeListener: (() -> Boolean)?
    var onImageTouchListener: ((x: Float, y: Float) -> Unit)?
    var snapPositionListener: ((snapPosition: Int) -> Unit)?
    var trackingBus: TrackingBus<EqualRange<ProfileImageVO>>?

    fun getCurrentImagePosition(): Int
}

abstract class OriginFeedViewHolder(view: View, viewPool: RecyclerView.RecycledViewPool? = null)
    : BaseViewHolder<FeedItemVO>(view), IFeedViewHolder {

    override var onBeforeLikeListener: (() -> Boolean)? = null
    override var onImageTouchListener: ((x: Float, y: Float) -> Unit)? = null
    override var snapPositionListener: ((snapPosition: Int) -> Unit)? = null
    override var trackingBus: TrackingBus<EqualRange<ProfileImageVO>>? = null

    internal var scrollListener: RecyclerView.OnScrollListener? = null
    internal var imagePreloadListener: RecyclerView.OnScrollListener? = null
    internal var subscription: Disposable? = null

    override fun getCurrentImagePosition(): Int = 0
}

abstract class BaseFeedViewHolder(view: View, viewPool: RecyclerView.RecycledViewPool? = null)
    : OriginFeedViewHolder(view, viewPool) {

    override var onBeforeLikeListener: (() -> Boolean)? = null
        set(value) {
            field = value
            profileImageAdapter.onBeforeLikeListener = value
        }

    override var onImageTouchListener: ((x: Float, y: Float) -> Unit)? = null
        set(value) {
            field = value
            profileImageAdapter.onImageTouchListener = value
        }

    internal val profileImageAdapter = ProfileImageAdapter(view.context)

    private val snapHelper = EnhancedPagerSnapHelper(duration = 30)

    init {
        itemView.rv_items.apply {
            adapter = profileImageAdapter
                .also {
                    it.onBeforeLikeListener = onBeforeLikeListener
                    it.onImageTouchListener = onImageTouchListener
                    it.tabsObserver = itemView.tabs.adapterDataObserver
                }
            isNestedScrollingEnabled = false
            layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
                .also { it.initialPrefetchItemCount = 2 }
            snapHelper.attachToRecyclerView(this)
            itemView.tabs.attachToRecyclerView(this, snapHelper)
            setHasFixedSize(true)
            setRecycledViewPool(viewPool)
            setScrollingTouchSlop(RecyclerView.TOUCH_SLOP_PAGING)
            OverScrollDecoratorHelper.setUpOverScroll(this, OverScrollDecoratorHelper.ORIENTATION_HORIZONTAL)

            // horizontal scroll listener
            scrollListener?.let { removeOnScrollListener(it) }
            scrollListener = object : RecyclerView.OnScrollListener() {
                override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(rv, dx, dy)
                    rv.linearLayoutManager()?.let {
                        val from = it.findFirstVisibleItemPosition()
                        val to = it.findLastVisibleItemPosition()
                        val items = profileImageAdapter.getItemsExposed(from = from, to = to)
                        Timber.v("Visible profile images [${items.size}] [$from, $to]: $items")
                        trackingBus?.postViewEvent(EqualRange(from = from, to = to, items = items))
                        snapPositionListener?.invoke(from)
                    }
                }
            }
            .also { listener -> addOnScrollListener(listener) }

            // image prefetch listener
            imagePreloadListener?.let { removeOnScrollListener(it) }
            imagePreloadListener = RecyclerViewPreloader(Glide.with(this), profileImageAdapter,
                ViewPreloadSizeProvider<ProfileImageVO>(),
//                FixedPreloadSizeProvider<ProfileImageVO>(AppRes.SCREEN_WIDTH, AppRes.FEED_IMAGE_HEIGHT),
                10)
//                .also { listener -> addOnScrollListener(listener) }
        }
        itemView.tv_profile_id.changeVisibility(isVisible = BuildConfig.IS_STAGING)
    }

    override fun bind(model: FeedItemVO) {
        fun createLabelView(@StringRes textResId: Int, @DrawableRes iconResId: Int): LabelView =
            LabelView(itemView.context).apply {
                setText(textResId)
                setIcon(iconResId)
            }

        fun createLabelView(text: String?, @DrawableRes iconResId: Int): LabelView =
            LabelView(itemView.context).apply {
                setText(text)
                setIcon(iconResId)
            }

        // --------------------------------------
        showControls()  // cancel any effect caused by applied payloads
        val positionOfImage = model.positionOfImage
        profileImageAdapter.apply {
            clear()  // clear old items, preventing animator to animate change upon async diff calc finishes

            subscription?.dispose()
            subscription = insertSubject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    itemView.rv_items.linearLayoutManager()?.scrollToPosition(positionOfImage)
                    itemView.tabs.alpha = if (model.images.size < 2) 0.0f else 1.0f
                }, Timber::e)

            submitList(model.images.map { ProfileImageVO(profileId = model.id, image = it, isLiked = model.isLiked(imageId = it.id)) })
        }

        // left section
        with (itemView.ll_left_section) {
            // transport property
            model.transport().let { transport ->
                findViewById<LabelView>(TransportProfileProperty.TRANSPORT_PROPERTY_ID)?.let { removeView(it) }
                if (transport != TransportProfileProperty.Unknown) {
                    val view = createLabelView(textResId = transport.resId, iconResId = OriginR_drawable.ic_transport_white_18dp)
                        .apply { id = TransportProfileProperty.TRANSPORT_PROPERTY_ID }
                    addView(view, 0)  // prepend
                }
            }

            // education property
            model.education().let { education ->
                findViewById<LabelView>(EducationProfileProperty.EDUCATION_PROPERTY_ID)?.let { removeView(it) }
                if (education != EducationProfileProperty.Unknown) {
                    val view = createLabelView(textResId = education.resId, iconResId = OriginR_drawable.ic_education_white_18dp)
                        .apply { id = EducationProfileProperty.EDUCATION_PROPERTY_ID }
                    addView(view, 0)  // prepend
                }
            }

            // property property
            model.property().let { property ->
                findViewById<LabelView>(PropertyProfileProperty.PROPERTY_PROPERTY_ID)?.let { removeView(it) }
                if (property != PropertyProfileProperty.Unknown) {
                    val view = createLabelView(textResId = property.resId, iconResId = OriginR_drawable.ic_home_property_white_18dp)
                        .apply { id = PropertyProfileProperty.PROPERTY_PROPERTY_ID }
                    addView(view, 0)  // prepend
                }
            }

            // income property
            model.income().let { income ->
                findViewById<LabelView>(IncomeProfileProperty.INCOME_PROPERTY_ID)?.let { removeView(it) }
                if (income != IncomeProfileProperty.Unknown) {
                    val view = createLabelView(textResId = income.resId, iconResId = OriginR_drawable.ic_income_white_18dp)
                        .apply { id = IncomeProfileProperty.INCOME_PROPERTY_ID }
                    addView(view, 0)  // prepend
                }
            }
        }

        // right section
        with (itemView.ll_right_section) {
            // distance
            findViewById<LabelView>(DISTANCE_PROPERTY_ID)?.let { removeView(it) }
            if (!model.distanceText.isNullOrBlank() && model.distanceText != "unknown") {
                val view = createLabelView(text = model.distanceText, iconResId = R.drawable.ic_location_white_18dp)
                    .apply { id = DISTANCE_PROPERTY_ID }
                addView(view, 0)  // prepend
            }

            // hair color property
            model.hairColor().let { hairColor ->
                findViewById<LabelView>(HairColorProfileProperty.HAIR_COLOR_PROPERTY_ID)?.let { removeView(it) }
                if (hairColor != HairColorProfileProperty.Unknown) {
                    val view = createLabelView(textResId = hairColor.resId(model.gender), iconResId = OriginR_drawable.ic_hair_color_white_18dp)
                        .apply { id = HairColorProfileProperty.HAIR_COLOR_PROPERTY_ID }
                    addView(view, 0)  // prepend
                }
            }

            // height property
            model.height.let { height ->
                findViewById<LabelView>(HEIGHT_PROPERTY_ID)?.let { removeView(it) }
                if (height > 0) {
                    val view = createLabelView(text = "${model.height} ${AppRes.LENGTH_CM}", iconResId = OriginR_drawable.ic_height_property_white_18dp)
                        .apply { id = HEIGHT_PROPERTY_ID }
                    addView(view, 0)  // prepend
                }
            }

            // age, sex
            model.age.let { age ->
                findViewById<LabelView>(AGE_PROPERTY_ID)?.let { removeView(it) }
                if (age >= 18) {
                    val view = createLabelView(text = "$age", iconResId = model.gender.resId)
                        .apply { id = AGE_PROPERTY_ID }
                    addView(view, 0)  // prepend
                }
            }
        }

        with (itemView.label_online_status) {
            alpha = if (model.lastOnlineStatusX == OnlineStatus.UNKNOWN) 0.0f else 1.0f
            setIcon(model.lastOnlineStatusX.resId)
            setText(model.lastOnlineText)
        }

        if (BuildConfig.IS_STAGING) {
            itemView.tv_profile_id.text = "Profile: ${model.idWithFirstN()}"
        }
    }

    override fun bind(model: FeedItemVO, payloads: List<Any>) {
        fun hideLabelInZone(container: ViewGroup, zone: Int) {
            with (container) {
                (zone - (4 - childCount))
                    .takeIf { it >= 0 }
                    ?.let { getChildAt(it)?.changeVisibility(isVisible = false, soft = true) }
            }
        }

        fun showLabelInZone(container: ViewGroup, zone: Int) {
            with (container) {
                (zone - (4 - childCount))
                    .takeIf { it >= 0 }
                    ?.let { getChildAt(it)?.changeVisibility(isVisible = true) }
            }
        }

        // --------------------------------------
        if (payloads.contains(FeedViewHolderHideControls)) {
            hideControls()
            return
        }
        if (payloads.contains(FeedViewHolderShowControls)) {
            showControls()
            return
        }

        // scroll affected
        if (payloads.contains(FeedViewHolderHideOnlineStatusOnScroll)) {
            itemView.label_online_status.changeVisibility(isVisible = false, soft = true)
        }
        if (payloads.contains(FeedViewHolderShowOnlineStatusOnScroll)) {
            itemView.label_online_status.changeVisibility(isVisible = true)
        }
        if (payloads.contains(FeedViewHolderHideSettingsBtnOnScroll)) {
            itemView.ibtn_settings.changeVisibility(isVisible = false)
        }
        if (payloads.contains(FeedViewHolderShowSettingsBtnOnScroll)) {
            itemView.ibtn_settings.changeVisibility(isVisible = true)
        }
        if (payloads.contains(FeedViewHolderHideTabsIndicatorOnScroll)) {
            itemView.tabs.changeVisibility(isVisible = false, soft = true)
        }
        if (payloads.contains(FeedViewHolderShowTabsIndicatorOnScroll)) {
            itemView.tabs.changeVisibility(isVisible = true)
        }
        payloads.filter { it is FeedViewHolderHideOnScroll }
            .forEach {
                (it as FeedViewHolderHideOnScroll)
                    .let { it.index }
                    .also { zone -> hideLabelInZone(itemView.ll_left_section, zone) }
                    .also { zone -> hideLabelInZone(itemView.ll_right_section, zone) }
            }
        payloads.filter { it is FeedViewHolderShowOnScroll }
            .forEach {
                (it as FeedViewHolderShowOnScroll)
                    .let { it.index }
                    .also { zone -> showLabelInZone(itemView.ll_left_section, zone) }
                    .also { zone -> showLabelInZone(itemView.ll_right_section, zone) }
            }
    }

    // ------------------------------------------------------------------------
    protected open fun hideControls() {
        itemView.apply {
            tabs.changeVisibility(isVisible = false)
            ibtn_settings.changeVisibility(isVisible = false)
            label_online_status.changeVisibility(isVisible = false)
            ll_left_section.changeVisibility(isVisible = false)
            ll_right_section.changeVisibility(isVisible = false)
        }
        profileImageAdapter.notifyItemChanged(getCurrentImagePosition(), FeedViewHolderHideControls)
    }

    protected open fun showControls() {
        itemView.apply {
            tabs.changeVisibility(isVisible = true)
            ibtn_settings.changeVisibility(isVisible = true)
            label_online_status.changeVisibility(isVisible = true)
            ll_left_section.changeVisibility(isVisible = true)
            ll_right_section.changeVisibility(isVisible = true)
        }
        profileImageAdapter.notifyItemChanged(getCurrentImagePosition(), FeedViewHolderShowControls)
    }

    override fun getCurrentImagePosition(): Int =
        itemView.rv_items.linearLayoutManager()?.findFirstVisibleItemPosition() ?: 0
}

class FeedViewHolder(view: View, viewPool: RecyclerView.RecycledViewPool? = null)
    : BaseFeedViewHolder(view, viewPool)

class HeaderFeedViewHolder(view: View) : OriginFeedViewHolder(view), IFeedViewHolder {

    override fun bind(model: FeedItemVO) {
        // no-op
    }
}

class FooterFeedViewHolder(view: View) : OriginFeedViewHolder(view), IFeedViewHolder {

    override fun bind(model: FeedItemVO) {
        showControls()
    }

    override fun bind(model: FeedItemVO, payloads: List<Any>) {
        if (payloads.contains(FeedFooterViewHolderHideControls)) {
            hideControls()
        }
        if (payloads.contains(FeedFooterViewHolderShowControls)) {
            showControls()
        }
    }

    // ------------------------------------------
    private fun hideControls() {
//        itemView.tv_end_item.changeVisibility(isVisible = false)
    }

    private fun showControls() {
//        itemView.tv_end_item.changeVisibility(isVisible = true)
    }
}
