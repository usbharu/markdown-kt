package dev.usbharu.markdown

import kotlin.collections.List

class PeekableCharIterator(private val charArray: CharArray) : Iterator<Char> {
    private var index = 0
    override fun hasNext(): Boolean = index < charArray.size

    override fun next(): Char = try {
        charArray[index++]
    } catch (e: IndexOutOfBoundsException) {
        index -= 1; throw NoSuchElementException(e.message)
    }

    fun peekOrNull(): Char? = charArray.getOrNull(index)

    fun current(): Int = index
}

class PeekableStringIterator(private val list: List<String>) : Iterator<String> {
    private var index = 0
    override fun hasNext(): Boolean = index < list.size
    override fun next(): String = try {
        list[index++]
    } catch (e: IndexOutOfBoundsException) {
        index -= 1; throw NoSuchElementException(e.message)
    }

    fun peekOrNull(): String? = list.getOrNull(index)

    fun current(): Int = index
}