package com.ringoid.utility.collection

class EqualRange<T>(val from: Int, val to: Int, items: Collection<T>) : ArrayList<T>(items) {

    fun range(): Pair<Int, Int> = from to to

    @Suppress("Unchecked_Cast")
    override fun equals(other: Any?): Boolean{
        fun equalRange(from: Int, to: Int): Boolean =
            this.from == from && this.to == to

        if (this === other) return true
        if (other == null) return false
        if (other.javaClass != javaClass) return false

        val o = other as EqualRange<T>
        if (!equalRange(o.from, o.to)) return false

        return true
    }

    override fun hashCode(): Int {
        var hash = 7
        hash = 31 * hash + from
        hash = 31 * hash + to
        return hash
    }
}
