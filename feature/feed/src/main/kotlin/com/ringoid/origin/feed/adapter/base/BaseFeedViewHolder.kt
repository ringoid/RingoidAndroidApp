package com.ringoid.origin.feed.adapter.base

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ringoid.base.adapter.BaseViewHolder
import com.ringoid.domain.BuildConfig
import com.ringoid.domain.debug.DebugLogUtil
import com.ringoid.domain.misc.Gender
import com.ringoid.domain.misc.UserProfilePropertyId
import com.ringoid.origin.AppInMemory
import com.ringoid.origin.feed.adapter.profile.ProfileImageAdapter
import com.ringoid.origin.feed.misc.OffsetScrollStrategy
import com.ringoid.origin.feed.model.FeedItemVO
import com.ringoid.origin.feed.model.ProfileImageVO
import com.ringoid.origin.feed.view.FeedScreenUtils
import com.ringoid.origin.model.OnlineStatus
import com.ringoid.origin.view.common.visibility_tracker.TrackingBus
import com.ringoid.utility.changeVisibility
import com.ringoid.utility.collection.EqualRange
import com.ringoid.utility.linearLayoutManager
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

    internal val profileImageAdapter = ProfileImageAdapter()

    private val snapHelper = EnhancedPagerSnapHelper(duration = 30)
    private var withAbout: Boolean = false

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
                        if (dx != 0) {
                            snapPositionListener?.invoke(from)
                        }
                    }
                }

                override fun onScrollStateChanged(rv: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(rv, newState)
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        rv.linearLayoutManager()?.let {
                            val positionOfImage = it.findFirstCompletelyVisibleItemPosition()
                            onImageSelect(positionOfImage = positionOfImage)
                        }
                    }
                }
            }
            .also { listener -> addOnScrollListener(listener) }

            // image prefetch listener
//            imagePreloadListener?.let { removeOnScrollListener(it) }
//            imagePreloadListener = RecyclerViewPreloader(Glide.with(this), profileImageAdapter,
//                ViewPreloadSizeProvider<ProfileImageVO>(),
//                FixedPreloadSizeProvider<ProfileImageVO>(AppRes.SCREEN_WIDTH, AppRes.FEED_IMAGE_HEIGHT),
//                10)
//                .also { listener -> addOnScrollListener(listener) }
        }
        itemView.tv_profile_id.changeVisibility(isVisible = BuildConfig.IS_STAGING)
    }

    override fun bind(model: FeedItemVO) {
        showControls()  // cancel any effect caused by applied payloads
        showOnlineStatus(model)  // apply updates, if any

        val positionOfImage = model.positionOfImage
        profileImageAdapter.apply {
            clear()  // clear old items, preventing animator to animate change upon async diff calc finishes

            subscription?.dispose()
            subscription = insertSubject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    itemView.rv_items.linearLayoutManager()?.scrollToPosition(positionOfImage)
                    itemView.tabs.alpha = if (model.images.size < 2) 0.0f else 1.0f
                }, DebugLogUtil::e)

            submitList(model.images.map { ProfileImageVO(profileId = model.id, image = it) })
        }

        setPropertyFields(model)
        onImageSelect(positionOfImage)

        if (BuildConfig.IS_STAGING) {
            itemView.tv_profile_id.text = "Profile: ${model.idWithFirstN()}"
        }
    }

    override fun bind(model: FeedItemVO, payloads: List<Any>) {
        fun showLabelInZone(container: ViewGroup, zone: Int, isVisible: Boolean) {
            val alpha = if (isVisible) 1.0f else 0.0f
            with (container) {
                (zone - (2 - countLabelsOnPosition(container, model.positionOfImage)))
                    .takeIf { it >= 0 }
                    ?.let { it + fixupPage(model.positionOfImage) * FeedScreenUtils.COUNT_LABELS_ON_PAGE }
                    ?.let { getChildAt(it)?.alpha = alpha }
            }
        }

        fun showNameInZone(zone: Int, isVisible: Boolean) {
            val alpha = if (isVisible) 1.0f else 0.0f
            if (isAboutPage(model)) {
                model.about()  // approximate number of lines
                    ?.let { minOf(3, it.length / 50 + 1) }
                    ?.takeIf { zone == it - 1 }
                    ?.let { itemView.tv_name_age.alpha = alpha }
            } else {
                val countOfLabels = countLabelsOnPosition(itemView.ll_left_section, model.positionOfImage)
                if (zone == countOfLabels) {
                    itemView.tv_name_age.alpha = alpha
                }
            }
        }

        fun hideLabelInZone(container: ViewGroup, zone: Int) {
            showLabelInZone(container, zone, isVisible = false)
        }

        fun hideNameInZone(zone: Int) {
            showNameInZone(zone, isVisible = false)
        }

        fun showLabelInZone(container: ViewGroup, zone: Int) {
            showLabelInZone(container, zone, isVisible = true)
        }

        fun showNameInZone(zone: Int) {
            showNameInZone(zone, isVisible = true)
        }

        // --------------------------------------
        showOnlineStatus(model)  // apply updates, if any

        if (payloads.contains(FeedViewHolderHideControls)) {
            hideControls()
            return
        }
        if (payloads.contains(FeedViewHolderShowControls)) {
            showControls()
            return
        }

        // scroll affected
        if (payloads.contains(FeedViewHolderHideAboutOnScroll)) {
            itemView.tv_about.alpha = 0.0f
        }
        if (payloads.contains(FeedViewHolderShowAboutOnScroll)) {
            itemView.tv_about.alpha = 1.0f
        }
        if (payloads.contains(FeedViewHolderHideStatusOnScroll)) {
            itemView.tv_status.alpha = 0.0f
        }
        if (payloads.contains(FeedViewHolderShowStatusOnScroll)) {
            itemView.tv_status.alpha = 1.0f
        }
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
        payloads
            .filterIsInstance<FeedViewHolderHideOnScroll>()
            .distinct()
            .forEach {
                it.index
                    .also { zone -> hideLabelInZone(itemView.ll_left_section, zone) }
                    .also { zone -> hideLabelInZone(itemView.ll_right_section, zone) }
            }
        payloads
            .filterIsInstance<FeedViewHolderShowOnScroll>()
            .distinct()
            .forEach {
                it.index
                    .also { zone -> showLabelInZone(itemView.ll_left_section, zone) }
                    .also { zone -> showLabelInZone(itemView.ll_right_section, zone) }
            }

        // grouped payloads
        payloads
            .filterIsInstance<FeedViewHolderHideNameOnScroll>()
            .distinct()
            .let { list ->
                val top = list.filter { it.type == OffsetScrollStrategy.Type.TOP }
                val bottom = list.filter { it.type == OffsetScrollStrategy.Type.BOTTOM }
                top.sortedByDescending { it.index } to bottom.sortedBy { it.index }
            }
            .let { (top, bottom) ->
                top.forEach { hideNameInZone(it.index) }
                bottom.forEach { hideNameInZone(it.index) }
            }
        payloads
            .filterIsInstance<FeedViewHolderShowNameOnScroll>()
            .distinct()
            .let { list ->
                val top = list.filter { it.type == OffsetScrollStrategy.Type.TOP }
                val bottom = list.filter { it.type == OffsetScrollStrategy.Type.BOTTOM }
                top.sortedBy { it.index } to bottom.sortedByDescending { it.index }
            }
            .let { (top, bottom) ->
                top.forEach { showNameInZone(it.index) }
                bottom.forEach { showNameInZone(it.index) }
            }

        onImageSelect(model.positionOfImage)
    }

    // ------------------------------------------------------------------------
    protected open fun hideControls() {
        itemView.apply {
            tabs.changeVisibility(isVisible = false)
            ibtn_settings.changeVisibility(isVisible = false)
            label_online_status.changeVisibility(isVisible = false)
            ll_left_container.changeVisibility(isVisible = false)
            ll_right_container.changeVisibility(isVisible = false)
        }
        profileImageAdapter.notifyItemChanged(getCurrentImagePosition(), FeedViewHolderHideControls)
    }

    protected open fun showControls() {
        itemView.apply {
            tabs.changeVisibility(isVisible = true)
            ibtn_settings.changeVisibility(isVisible = true)
            label_online_status.changeVisibility(isVisible = true)
            ll_left_container.changeVisibility(isVisible = true)
            ll_right_container.changeVisibility(isVisible = true)
        }
        profileImageAdapter.notifyItemChanged(getCurrentImagePosition(), FeedViewHolderShowControls)
    }

    private fun showOnlineStatus(model: FeedItemVO) {
        with (itemView.label_online_status) {
            alpha = if (model.lastOnlineStatusX == OnlineStatus.UNKNOWN) 0.0f else 1.0f
            setIcon(model.lastOnlineStatusX.resId)
            setText(model.lastOnlineText)
        }
    }

    override fun getCurrentImagePosition(): Int =
        itemView.rv_items.linearLayoutManager()?.findFirstVisibleItemPosition() ?: 0

    // --------------------------------------------------------------------------------------------
    private fun countLabelsOnPosition(container: ViewGroup, positionOfImage: Int): Int {
        val page = fixupPage(positionOfImage)
        val startIndex = page * FeedScreenUtils.COUNT_LABELS_ON_PAGE
        return if (startIndex < container.childCount) {
            val endIndex = startIndex + FeedScreenUtils.COUNT_LABELS_ON_PAGE
            minOf(endIndex, container.childCount) - startIndex
        } else 0  // no labels on page
    }

    private fun onImageSelect(positionOfImage: Int) {
        fun showLabels(containerView: ViewGroup, startIndex: Int, endIndex: Int) {
            with (containerView) {
                for (i in 0 until childCount) {
                    getChildAt(i)?.changeVisibility(isVisible = false)
                }
                if (startIndex < childCount) {
                    changeVisibility(isVisible = true)
                    for (i in startIndex until minOf(endIndex, childCount)) {
                        getChildAt(i)?.changeVisibility(isVisible = true)
                    }
                } else {
                    changeVisibility(isVisible = false)
                }
            }
        }

        fun showAbout() {
            itemView.tv_about.changeVisibility(isVisible = true)
            itemView.ll_left_section.changeVisibility(isVisible = false)
            itemView.ll_right_section.changeVisibility(isVisible = false)
        }

        fun showLabels(startIndex: Int, endIndex: Int) {
            itemView.tv_about.changeVisibility(isVisible = false)
            showLabels(itemView.ll_left_section, startIndex, endIndex)
            showLabels(itemView.ll_right_section, startIndex, endIndex)
        }

        // --------------------------------------
        val page = fixupPage(positionOfImage)
        val startIndex = page * FeedScreenUtils.COUNT_LABELS_ON_PAGE
        val endIndex = startIndex + FeedScreenUtils.COUNT_LABELS_ON_PAGE

        if (withAbout) {
            when (AppInMemory.oppositeUserGender()) {
                Gender.FEMALE -> {
                    when (positionOfImage) {
                        0 -> showAbout()
                        else -> showLabels(startIndex, endIndex)
                    }
                }
                else -> {
                    when (positionOfImage) {
                        1 -> showAbout()
                        else -> showLabels(startIndex, endIndex)
                    }
                }
            }
        } else {
            showLabels(startIndex, endIndex)
        }
    }

    private fun isAboutPage(model: FeedItemVO): Boolean =
        if (withAbout) {
            when (model.gender) {
                Gender.FEMALE -> model.positionOfImage == 0
                else -> model.positionOfImage == 1
            }
        } else false

    private fun fixupPage(positionOfImage: Int): Int =
        if (withAbout) maxOf(0, positionOfImage - 1)
        else positionOfImage

    private fun setPropertyFields(model: FeedItemVO) {
        fun addLabelView(
                containerView: ViewGroup,
                propertyId: UserProfilePropertyId,
                properties: FeedItemVO) {
            FeedScreenUtils.createLabelView(
                container = containerView,
                gender = properties.gender,
                propertyId = propertyId,
                properties = properties)
            ?.let { labelView ->
                containerView.addView(labelView)
                labelView.changeVisibility(isVisible = false)
            }
        }

        // --------------------------------------
        // about
        model.about()
            ?.takeIf { it.isNotBlank() }
            ?.let { itemView.tv_about.text = it.trim() }

        withAbout = !model.about().isNullOrBlank()

        // name, age
        (model.name()?.takeIf { it.isNotBlank() } ?: AppInMemory.genderString(model.gender))
            ?.let { name ->
                mutableListOf<String>().apply {
                    add(name.trim())
                    model.age.takeIf { it >= 18 }?.let { age -> add("$age") }
                }
                .let { itemView.tv_name_age.text = it.joinToString() }
            }

        // status
        model.status()
            ?.takeIf { it.isNotBlank() }
            ?.let { itemView.tv_status.text = it.trim() }

        // labels in sections
        itemView.ll_left_section?.let { containerView ->
            containerView.removeAllViews()
            when (model.gender) {
                Gender.FEMALE -> FeedScreenUtils.propertiesFemale
                else -> FeedScreenUtils.propertiesMale
            }
            .forEach { propertyId -> addLabelView(containerView, propertyId, model) }
        }
        itemView.ll_right_section?.let { containerView ->
            containerView.removeAllViews()
            FeedScreenUtils.propertiesRight
                .forEach { propertyId -> addLabelView(containerView, propertyId, model) }
        }
    }
}
