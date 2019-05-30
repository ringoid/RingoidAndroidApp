package com.ringoid.origin.profile.adapter

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.ListPreloader
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.request.RequestOptions
import com.ringoid.base.adapter.BaseDiffCallback
import com.ringoid.base.adapter.BaseListAdapter
import com.ringoid.domain.model.image.EmptyUserImage
import com.ringoid.domain.model.image.UserImage
import com.ringoid.origin.profile.R
import com.ringoid.utility.image.ImageLoader

class UserProfileImageAdapter(private val context: Context)
    : BaseListAdapter<UserImage, BaseUserProfileImageViewHolder>(UserProfileImageDiffCallback()),
    ListPreloader.PreloadModelProvider<UserImage> {

    var onInsertListener: ((count: Int) -> Unit)? = null
    var onRemoveListener: (() -> Unit)? = null
    var tabsObserver: RecyclerView.AdapterDataObserver? = null

    override fun getLayoutId(): Int = R.layout.rv_item_user_profile_image

    override fun instantiateViewHolder(view: View): BaseUserProfileImageViewHolder =
        UserProfileImageViewHolder(view)

    override fun instantiateHeaderViewHolder(view: View) = HeaderUserProfileImageViewHolder(view)
    override fun instantiateFooterViewHolder(view: View) = FooterUserProfileImageViewHolder(view)  // so use same ViewHolder for stubs

    override fun getExposedCb(): (() -> Unit)? = { tabsObserver?.onChanged() }

    override fun getOnInsertedCb(): ((position: Int, count: Int) -> Unit)? =
        { _, count: Int -> onInsertListener?.invoke(count) }

    override fun getOnRemovedCb(): ((position: Int, count: Int) -> Unit)? =
        { _, _ -> onRemoveListener?.invoke() }

    // ------------------------------------------
    override fun getStubItem(): UserImage = EmptyUserImage

    /* Data Access */
    // --------------------------------------------------------------------------------------------
    fun remove(imageId: String) {
        getModels()
            .find { it.id == imageId }
            ?.let { submitList(ArrayList(getModels()).apply { remove(it) }) }
    }

    // ------------------------------------------
    override fun getPreloadItems(position: Int): List<UserImage> =
        getModels().subList(position, position + 1)

    override fun getPreloadRequestBuilder(item: UserImage): RequestBuilder<*>? =
        ImageLoader.loadRequest(uri = item.uri, thumbnailUri = item.uriLocal ?: item.thumbnailUri,
            context = context, options = RequestOptions().centerCrop())
}

// ------------------------------------------------------------------------------------------------
class UserProfileImageDiffCallback : BaseDiffCallback<UserImage>() {

    override fun areItemsTheSame(oldItem: UserImage, newItem: UserImage): Boolean = oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: UserImage, newItem: UserImage): Boolean = oldItem == newItem
}
