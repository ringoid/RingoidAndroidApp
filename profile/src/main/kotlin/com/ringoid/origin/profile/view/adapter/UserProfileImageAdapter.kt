package com.ringoid.origin.profile.view.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding3.view.clicks
import com.ringoid.base.adapter.BaseDiffCallback
import com.ringoid.base.adapter.BaseListAdapter
import com.ringoid.domain.model.image.UserImage
import com.ringoid.origin.profile.R
import com.ringoid.utility.clickDebounce
import kotlinx.android.synthetic.main.rv_item_user_profile_image.view.*

class UserProfileImageAdapter : BaseListAdapter<UserImage, UserProfileImageViewHolder>(UserProfileImageDiffCallback()) {

    var onDeleteImageListener: ((model: UserImage, position: Int) -> Unit)? = null
    var onEmptyImagesListener: ((isEmpty: Boolean) -> Unit)? = null
    var tabsObserver: RecyclerView.AdapterDataObserver? = null

    override fun getLayoutId(): Int = R.layout.rv_item_user_profile_image

    override fun instantiateViewHolder(view: View): UserProfileImageViewHolder =
        UserProfileImageViewHolder(view).also { vh ->
            vh.itemView.ibtn_delete_image.clicks().compose(clickDebounce())
                .subscribe { wrapOnItemClickListener(vh, onDeleteImageListener).onClick(vh.itemView.ibtn_delete_image) }
        }

    fun remove(imageId: String) {
        getItems().find { it.id == imageId }
            ?.let { submitList(ArrayList(getItems()).apply { remove(it) }) }
    }

    override fun submitList(list: List<UserImage>?) {
        super.submitList(list)
        onEmptyImagesListener?.invoke(list.isNullOrEmpty())
        tabsObserver?.onChanged()
    }

    fun updateItemId(ids: Pair<String, String>) {
        updateItemId(oldId = ids.first, newId = ids.second)
    }

    fun updateItemId(oldId: String, newId: String) {
        getItems()
            .indexOfFirst { it.id == oldId }
            .takeIf { it != -1 }
            ?.let {
                val l = ArrayList(getItems())
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

    override fun areContentsTheSame(oldItem: UserImage, newItem: UserImage): Boolean = oldItem == newItem  // as 'data class'
}
