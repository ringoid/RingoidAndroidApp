package com.ringoid.origin.view.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.ringoid.domain.model.image.Image
import com.ringoid.origin.view.common.EmptyFragment
import com.ringoid.origin.view.image.ImagePageFragment

class ImagePagerAdapter(fm: FragmentManager, val emptyInput: EmptyFragment.Companion.Input) : FragmentStatePagerAdapter(fm) {

    private val images: MutableList<Image> = mutableListOf()

    override fun getCount(): Int = images.takeIf { !it.isEmpty() }?.let { it.size } ?: 1

    override fun getItem(position: Int): Fragment =
        if (position == 0 && isEmpty()) EmptyFragment.newInstance(input = emptyInput)
        else ImagePageFragment.newInstance(images[position])

    // --------------------------------------––-----––-––-––––--–----––----------------------------
    fun isEmpty(): Boolean = images.isEmpty()

    fun add(item: Image) {
        images.add(item)
        notifyDataSetChanged()
    }

    fun remove(itemId: String) {
        images.find { it.id == itemId }
              ?.let { images.remove(it) ; notifyDataSetChanged() }
    }
}
