package com.ringoid.utility.collection

class EqualRange<T>(val from: Int, val to: Int, val orientation: Orientation, items: List<T>) : ArrayList<T>(items) {

    enum class Orientation { HORIZONTAL, VERTICAL, NONE }

    companion object {
        fun <T> empty(orientation: Orientation = Orientation.NONE): EqualRange<T> =
            EqualRange(-1, -1, orientation, emptyList())
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
        if (orientation != new.orientation) {
            throw IllegalArgumentException("Orientations are different while calculating delta")
        }
        if (isRangeEmpty()) {
            return this
        }
        /**
         * [a  [c  b]  d] => [a  c)
         */
        if (new.from in from..to && to in new.from..new.to) {
            if (from < new.from) return EqualRange(from, new.from - 1, orientation, subList(0, new.from - from))
            if (from == new.from) return EqualRange.empty(orientation)
        }
        /**
         * [a|c  d]  b] => (d  b]
         */
        if (from == new.from && new.to < to) {
            return EqualRange(new.to + 1, to, orientation, subList(new.to + 1 - from, to - from + 1))
        }
        /**
         * [a  [c  d|b] => [a  c)
         */
        if (from < new.from && to == new.to) {
            return EqualRange(from, new.to - 1, orientation, subList(0, new.to - from))
        }
        /**
         * [a  [c   d]  b] => [a  c)(d  b]
         */
        if (new.from in from..to && to > new.to) {
            val l = subList(0, new.from + 1 - from).apply { addAll(subList(new.to + 1 - from, to + 1 - from)) }
            return EqualRange(from, from + l.size + 1, orientation, l)
        }
        /**
         * [c  [a  d]  b] => (d  b]
         */
        if (from in new.from..new.to && new.to in from..to) {
            if (new.to < to) return EqualRange(new.to + 1, to, orientation, subList(new.to + 1 - from, to + 1 - from))
            if (new.to == to) return EqualRange.empty(orientation)
        }
        /**
         * [a  b][c  d], [c  d][a  b] => [a  b]
         */
        if (to < new.from || from > new.to) return EqualRange(from, to, orientation, this)
        /**
         * [a  b|c  d] => [a  b)
         */
        if (to <= new.from) return EqualRange(from, to - 1, orientation, dropLast(1))
        /**
         * [c  d|a  b] => (a  b]
         */
        if (from >= new.to) return EqualRange(from + 1, to, orientation, drop(1))

        return this
    }

    fun dropItems(n: Int): EqualRange<T> {
        if (n < 0) throw IllegalArgumentException("Number of first items to drop must not be negative: $n")
        if (n == 0) return this
        if (n >= size) return empty(orientation)
        return EqualRange(from + n, to, orientation, drop(n))
    }

    fun dropLastItems(n: Int): EqualRange<T> {
        if (n < 0) throw IllegalArgumentException("Number of first items to drop must not be negative: $n")
        if (n == 0) return this
        if (n >= size) return empty(orientation)
        return EqualRange(from, to - n, orientation, dropLast(n))
    }

    // ------------------------------------------
    @Suppress("Unchecked_Cast")
    override fun equals(other: Any?): Boolean{
        fun equalRange(from: Int, to: Int): Boolean =
            this.from == from && this.to == to

        if (this === other) return true
        if (other == null) return false
        if (other.javaClass != javaClass) return false

        val o = other as EqualRange<T>
        if (o.orientation != orientation) return false
        if (!equalRange(o.from, o.to)) return false

        return true
    }

    override fun hashCode(): Int {
        var hash = 7
        hash = 31 * hash + from
        hash = 31 * hash + to
        hash = 31 * hash + orientation.ordinal
        return hash
    }

    // ------------------------------------------
    override fun toString(): String = (from..to step 1).joinToString(", ", "[", "]")

    fun payloadToString(): String = "${toString()} ${joinToString(", ", "{", "}")}"
}
