package com.ringoid.origin.view.base

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.ringoid.base.view.IListScrollCallback
import com.ringoid.domain.DomainUtil
import com.ringoid.origin.viewmodel.BasePermissionViewModel
import com.ringoid.utility.linearLayoutManager

abstract class BaseListFragment<T : BasePermissionViewModel> : BasePermissionFragment<T>() {

    protected abstract fun getRecyclerView(): RecyclerView

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getRecyclerView().addOnScrollListener(pagingScrollListener)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        getRecyclerView().removeOnScrollListener(pagingScrollListener)
    }

    // --------------------------------------------------------------------------------------------
    protected fun scrollListToPosition(position: Int) {
        getRecyclerView().scrollToPosition(position)
    }

    protected fun scrollListToPositionAndPost(position: Int): RecyclerView =
        getRecyclerView().also { it.scrollToPosition(position) }

    protected fun scrollToTopOfItemAtPosition(position: Int, offset: Int = 0) {
        getRecyclerView().linearLayoutManager()?.scrollToPositionWithOffset(position, offset)
    }

    protected fun scrollToTopOfItemAtPositionAndPost(position: Int): RecyclerView =
        getRecyclerView().also { it.linearLayoutManager()?.scrollToPositionWithOffset(position, 0) }

    // ------------------------------------------
    private var lastVisible = DomainUtil.BAD_POSITION

    private val pagingScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(rv, dx, dy)
            if (dy <= 0) {
                onScrollTop()
                return   // skip scroll up
            }

            val lm = rv.linearLayoutManager()
            val last = lm?.findLastVisibleItemPosition() ?: DomainUtil.BAD_POSITION
            if (lastVisible == last) {
                return   // skip scroll due to layout
            }

            lastVisible = last
            val total = lm?.itemCount ?: 0
            onScroll(total - last)
        }
    }

    protected open fun onScroll(itemsLeftToEnd: Int) {
        vm.takeIf { it is IListScrollCallback }
            ?.let { it as IListScrollCallback }
            ?.onScroll(itemsLeftToEnd)
    }

    protected open fun onScrollTop() {
        // override in subclasses
    }
}
