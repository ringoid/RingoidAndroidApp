package com.ringoid.utility.collection

class EqualRange<T>(val from: Int, val to: Int, items: List<T>) : ArrayList<T>(items) {

    companion object {
        fun <T> empty(): EqualRange<T> = EqualRange(-1, -1, emptyList())
    }

    init {
        if (from >= 0 && to >= 0) {
            if (from > to) {
                throw IllegalArgumentException("From $from exceeds To $to")
            }
            if (to - from + 1 != items.size) {
                throw IllegalArgumentException("Inconsistency between size of items [${items.size} and range [$from, $to]")
            }
        } else if (!items.isEmpty()) {
            throw IllegalArgumentException("Invalid size for empty range")
        }
    }

    fun isRangeEmpty(): Boolean = from < 0 && to < 0 && isEmpty()

    fun range(): Pair<Int, Int> = from to to

    /**
     *  [a   b]  [c  d] -> [a, b]
     *  [c   d]  [a  b] -> [a, b]
     *  [a  [c   b]  d] -> [a, c)
     *  [a  [c   d]  b] -> [a, c), (d, b]
     *  [c  [a   d]  b] -> (d, b]
     *  [c  [a   b]  d] -> []
     */
    fun delta(new: EqualRange<T>): EqualRange<T> {
        if (new.from in from..to && to in new.from..new.to) {
            if (from < new.from) return EqualRange(from, new.from - 1, subList(from, new.from))
            if (from == new.from) return EqualRange.empty()
        }
        if (new.from in from..to && to > new.to) {
            val l = subList(from, new.from).apply { addAll(subList(to + 1, to)) }
            return EqualRange(from, from + l.size + 1, l)
        }
        if (from in new.from..new.to && new.to in from..to) {
            if (new.to < to) return EqualRange(new.to + 1, to + 1, subList(new.to + 1, to))
            if (new.to == to) return EqualRange.empty()
        }
        if (to < new.from || from > new.to) return EqualRange(from, to, this)
        if (to <= new.from) return EqualRange(from, to - 1, subList(from, to))
        if (from >= new.to) return EqualRange(from + 1, to, subList(from + 1, to + 1))

        return this
    }

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
