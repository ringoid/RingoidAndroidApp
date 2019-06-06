package com.ringoid.origin.feed.misc

import com.ringoid.origin.feed.adapter.base.FeedViewHolderPayload

data class OffsetScrollStrategy(val tag: String? = null,
    val type: Type, val deltaOffset: Int, val hide: FeedViewHolderPayload, val show: FeedViewHolderPayload,
    private val hiddenAtPositions: MutableSet<Int> = mutableSetOf(),
    private val shownAtPositions: MutableSet<Int> = mutableSetOf()) {

    enum class Type { TOP, BOTTOM }

    fun isHiddenAtAndSync(position: Int): Boolean =
        if (!hiddenAtPositions.contains(position)) {
            hiddenAtPositions.add(position)
            shownAtPositions.remove(position)
            false
        } else true

    fun isShownAtAndSync(position: Int): Boolean =
        if (!shownAtPositions.contains(position)) {
            shownAtPositions.add(position)
            hiddenAtPositions.remove(position)
            false
        } else true

    /**
     * Compare these values to detect whether two different strategies operate on the same target.
     */
    fun target(): Int {
        var hash = 7
        hash = 31 * hash + hide.hashCode()
        hash = 31 * hash + show.hashCode()
        return hash
    }

    fun hidePositions(): String = hiddenAtPositions.joinToString(", ","[", "]")
    fun showPositions(): String = shownAtPositions.joinToString(", ","[", "]")

    override fun toString(): String = "[$tag hide=${hidePositions()} show=${showPositions()}]"
}
