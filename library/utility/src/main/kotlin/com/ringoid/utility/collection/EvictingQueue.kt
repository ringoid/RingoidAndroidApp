package com.ringoid.utility.collection

import java.util.*

class EvictingQueue<T>(private val capacity: Int) : Deque<T> {

    private val delegate: Deque<T> = ArrayDeque(capacity)

    private fun remainingCapacity() = capacity - size

    /**
     * Adds the given element to this queue. If the queue is currently full, the element at the head
     * of the queue is evicted to make room.
     *
     * @return {@code true} always
     */
    override fun offer(element: T): Boolean = add(element)
    override fun offerFirst(e: T): Boolean {
        addFirst(e)
        return true
    }
    override fun offerLast(e: T): Boolean = offer(e)

    /**
     * Adds the given element to this queue. If the queue is currently full, the element at the head
     * of the queue is evicted to make room.
     *
     * @return {@code true} always
     */
    override fun add(element: T): Boolean {
        if (capacity <= 0) {
            return true
        }
        if (size == capacity) {
            remove()
        }
        delegate.add(element)
        return true
    }

    override fun addFirst(e: T) {
        if (capacity <= 0) {
            return
        }
        if (size == capacity) {
            removeFirst()
        }
        delegate.addFirst(e)
    }

    override fun addLast(e: T) {
        add(e)
    }

    override fun addAll(elements: Collection<T>): Boolean {
        if (elements.size > remainingCapacity()) {
            delegate.addAll(elements.toMutableList().subList(0, remainingCapacity()))
        } else {
            delegate.addAll(elements)
        }
        return true
    }

    override fun clear() {
        delegate.clear()
    }

    override fun contains(element: T): Boolean = delegate.contains(element)
    override fun descendingIterator(): MutableIterator<T> = delegate.descendingIterator()
    override fun isEmpty(): Boolean = delegate.isEmpty()
    override fun iterator(): MutableIterator<T> = delegate.iterator()
    override fun element(): T = delegate.element()
    override fun getFirst(): T = delegate.first
    override fun getLast(): T = delegate.last
    override fun peek(): T = delegate.peek()
    override fun peekFirst(): T = delegate.peekFirst()
    override fun peekLast(): T = delegate.peekLast()
    override fun poll(): T = delegate.poll()
    override fun pollFirst(): T = delegate.pollFirst()
    override fun pollLast(): T = delegate.pollLast()

    override val size: Int
        get() = delegate.size

    override fun containsAll(elements: Collection<T>): Boolean =
        if (elements.size > size) false
        else delegate.containsAll(elements)

    override fun retainAll(elements: Collection<T>): Boolean = delegate.retainAll(elements)

    override fun push(e: T) {
        addFirst(e)
    }

    override fun pop(): T = delegate.pop()

    override fun remove(): T = delegate.remove()
    override fun remove(element: T): Boolean = delegate.remove(element)
    override fun removeAll(elements: Collection<T>): Boolean = delegate.removeAll(elements)
    override fun removeFirst(): T = delegate.removeFirst()
    override fun removeLast(): T = delegate.removeLast()
    override fun removeFirstOccurrence(o: Any): Boolean = delegate.removeFirstOccurrence(o)
    override fun removeLastOccurrence(o: Any): Boolean = delegate.removeLastOccurrence(o)
}
