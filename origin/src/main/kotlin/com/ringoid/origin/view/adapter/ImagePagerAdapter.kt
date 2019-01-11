package com.ringoid.origin.view.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.ringoid.domain.model.image.IImage
import com.ringoid.origin.view.common.EmptyFragment
import com.ringoid.origin.view.image.ImagePageFragment

abstract class ImagePagerAdapter(fm: FragmentManager, private val emptyInput: EmptyFragment.Companion.Input)
    : FragmentStatePagerAdapter(fm) {

    private val images: MutableList<IImage> = mutableListOf()

    protected abstract fun createImagePageFragment(image: IImage): ImagePageFragment<*>

    override fun getCount(): Int = images.takeIf { !it.isEmpty() }?.size ?: 1

    override fun getItem(position: Int): Fragment =
        if (position == 0 && isEmpty()) EmptyFragment.newInstance(input = emptyInput)
        else createImagePageFragment(images[position])

    // --------------------------------------––-----––-––-––––--–----––----------------------------
    fun isEmpty(): Boolean = images.isEmpty()

    fun add(item: IImage) {
        images.add(item)
        notifyDataSetChanged()
    }

    fun remove(itemId: String) {
        images.find { it.id == itemId }
              ?.let { images.remove(it) ; notifyDataSetChanged() }
    }

    fun set(items: List<IImage>) {
        images.let {
            it.clear()
            it.addAll(items)
        }
        notifyDataSetChanged()
    }
}
