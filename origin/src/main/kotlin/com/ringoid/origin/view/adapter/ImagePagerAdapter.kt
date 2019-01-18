package com.ringoid.origin.view.adapter

import android.database.DataSetObserver
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.PagerAdapter
import com.ringoid.domain.model.image.IImage
import com.ringoid.origin.view.common.EmptyFragment
import com.ringoid.origin.view.image.ImagePageFragment

abstract class ImagePagerAdapter(fm: FragmentManager, private val emptyInput: EmptyFragment.Companion.Input)
    : FragmentStatePagerAdapter(fm) {

    interface IImagePage {
        var adapterPosition: Int
        fun notifyUpdate(image: IImage)
    }

    private val images: MutableList<IImage> = mutableListOf()
    private var structuralChange: Boolean = false
    var tabsObserver: DataSetObserver? = null

    protected abstract fun createImagePageFragment(image: IImage): ImagePageFragment<*>

    override fun getCount(): Int = images.takeIf { !it.isEmpty() }?.size ?: 1

    override fun getItem(position: Int): Fragment =
        if (position == 0 && isEmpty()) EmptyFragment.newInstance(input = emptyInput)
        else createImagePageFragment(images[position])

    override fun getItemPosition(`object`: Any): Int = PagerAdapter.POSITION_NONE
//        if (structuralChange) PagerAdapter.POSITION_NONE
//        else {
//            when (`object`) {
//                is IImagePage -> {
//                    `object`.notifyUpdate(image = images[`object`.adapterPosition])
//                    super.getItemPosition(`object`)
//                }
//                else -> PagerAdapter.POSITION_NONE
//            }
//        }

    // --------------------------------------––-----––-––-––––--–----––----------------------------
    fun isEmpty(): Boolean = images.isEmpty()

    fun add(item: IImage) {
        checkValidState()
        images.add(item)
        structuralChange = true
        notifyDataSetChanged()
    }

    fun add(vararg items: IImage) {
        checkValidState()
        images.addAll(items.toList())
        structuralChange = true
        notifyDataSetChanged()
    }

    fun prepend(item: IImage) {
        checkValidState()
        images.add(0, item)
        structuralChange = true
        notifyDataSetChanged()
    }

    fun remove(itemId: String) {
        checkValidState()
        images.find { it.id == itemId }
              ?.let { images.remove(it) }
              ?.takeIf { it }
              ?.let { structuralChange = it }
               .also { notifyDataSetChanged() }
    }

    fun set(items: List<IImage>) {
        checkValidState()
        structuralChange = images.size != items.size

        images.let {
            it.clear()
            it.addAll(items)
        }
        notifyDataSetChanged()
    }

    fun updateItemId(oldId: String, newId: String) {
        checkValidState()
        images.indexOfFirst { it.id == oldId }
              .takeIf { it != -1 }
              ?.let {
                  images.apply {
                      val item = get(it)
                      removeAt(it)
                      add(it, item.copyWithId(newId))
                  }
              }
        notifyDataSetChanged()
    }

    fun updateItemId(ids: Pair<String, String>) {
        updateItemId(oldId = ids.first, newId = ids.second)
    }

    /* Internal */
    // --------------------------------------––-----––-––-––––--–----––----------------------------
    override fun notifyDataSetChanged() {
        super.notifyDataSetChanged()
        tabsObserver?.onChanged()
        structuralChange = false  // consume previous change, if any
    }

    private fun checkValidState() {
        if (structuralChange) throw IllegalStateException("Need to call 'notifyDataSetChanged()' to apply pending structural changes")
    }
}
