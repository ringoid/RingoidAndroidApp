package com.ringoid.utility.collection

import com.ringoid.utility.DebugOnly
import com.ringoid.utility.model.DebugHashItem

typealias callback = (old: DebugHashItem, new: DebugHashItem) -> Unit

@DebugOnly
class DebugHashItemSet(private val l: callback?) : MutableSet<DebugHashItem> {

    private val delegate = mutableSetOf<DebugHashItem>()

    override fun add(element: DebugHashItem): Boolean {
        val result = delegate.add(element)
        if (!result) {
            delegate.find { it.hashCode() == element.hashCode() }
                    ?.let { old -> l?.invoke(old, element) }
        }
        return result
    }

    override fun addAll(elements: Collection<DebugHashItem>): Boolean {
        val oldSize = size
        elements.forEach { add(it) }
        return oldSize < size
    }

    fun addAllString(elements: Collection<String>): Boolean =
        addAll(elements.map { DebugHashItem(id = it) })

    override fun contains(element: DebugHashItem): Boolean = delegate.contains(element)

    override fun containsAll(elements: Collection<DebugHashItem>): Boolean = delegate.containsAll(elements)

    override fun clear() = delegate.clear()

    override fun isEmpty(): Boolean = delegate.isEmpty()

    override fun iterator(): MutableIterator<DebugHashItem> = delegate.iterator()

    override fun remove(element: DebugHashItem): Boolean = delegate.remove(element)

    override fun removeAll(elements: Collection<DebugHashItem>): Boolean = delegate.removeAll(elements)

    override fun retainAll(elements: Collection<DebugHashItem>): Boolean = delegate.retainAll(elements)

    override val size: Int = delegate.size
}
