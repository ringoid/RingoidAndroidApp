package com.ringoid.origin.view.adapter.feed

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ringoid.base.adapter.BaseViewHolder
import com.ringoid.domain.model.feed.Profile
import kotlinx.android.synthetic.main.rv_item_feed_profile.view.*

class ProfileViewHolder(view: View) : BaseViewHolder<Profile>(view) {

    private val profileImageAdapter = ProfileImageAdapter()

    init {
        itemView.rv_items.apply {
            adapter = profileImageAdapter
            layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        }
    }

    override fun bind(model: Profile) {
        profileImageAdapter.submitList(model.images)
    }
}
