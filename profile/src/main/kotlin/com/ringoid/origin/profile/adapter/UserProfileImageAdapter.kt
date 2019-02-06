package com.ringoid.origin.profile.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.ringoid.base.adapter.BaseDiffCallback
import com.ringoid.base.adapter.BaseListAdapter
import com.ringoid.domain.model.image.EmptyUserImage
import com.ringoid.domain.model.image.UserImage
import com.ringoid.origin.profile.R

class UserProfileImageAdapter : BaseListAdapter<UserImage, BaseUserProfileImageViewHolder>(UserProfileImageDiffCallback()) {

    var onEmptyImagesListener: ((isEmpty: Boolean) -> Unit)? = null
    var tabsObserver: RecyclerView.AdapterDataObserver? = null

    override fun getLayoutId(): Int = R.layout.rv_item_user_profile_image

    override fun instantiateViewHolder(view: View): BaseUserProfileImageViewHolder =
        UserProfileImageViewHolder(view)

    override fun instantiateHeaderViewHolder(view: View) = HeaderUserProfileImageViewHolder(view)

    override fun getExposedCb(): (() -> Unit)? = { tabsObserver?.onChanged() }

    // ------------------------------------------
    override fun getStubItem(): UserImage = EmptyUserImage

    /* Data Access */
    // --------------------------------------------------------------------------------------------
    fun remove(imageId: String) {
        getModels()
            .find { it.id == imageId }
            ?.let { submitList(ArrayList(getModels()).apply { remove(it) }) }
    }

    override fun onSubmitList(list: List<UserImage>?) {
        super.onSubmitList(list)
        onEmptyImagesListener?.invoke(list.isNullOrEmpty())
    }

    fun updateItemId(ids: Pair<String, String>) {
        updateItemId(oldId = ids.first, newId = ids.second)
    }

    private fun updateItemId(oldId: String, newId: String) {
        getModels()
            .indexOfFirst { it.id == oldId }
            .takeIf { it != -1 }
            ?.let {
                val l = ArrayList(getModels())
                l.apply {
                    val item = get(it)
                    removeAt(it)
                    add(it, item.copyWithId(newId) as UserImage)
                }
                submitList(l)
            }
    }
}

// ------------------------------------------------------------------------------------------------
class UserProfileImageDiffCallback : BaseDiffCallback<UserImage>() {

    override fun areItemsTheSame(oldItem: UserImage, newItem: UserImage): Boolean = oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: UserImage, newItem: UserImage): Boolean = oldItem.uri == newItem.uri
}
