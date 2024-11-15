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

    fun peekOrNull(offset: Int): Char? = charArray.getOrNull(index + offset)

    fun skip(count: Int = 0) {
        index += count
    }
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

class PeekableTokenIterator(private val tokens: List<Token>) : Iterator<Token> {
    private var index = 0
    override fun hasNext(): Boolean = index < tokens.size
    override fun next(): Token = try {
        tokens[index++]
    } catch (e: IndexOutOfBoundsException) {
        index -= 1; throw NoSuchElementException(e.message)
    }

    fun peekOrNull(): Token? = tokens.getOrNull(index)

    fun peekOrNull(offset: Int): Token? = tokens.getOrNull(index + offset)

    fun current(): Int = index
    fun skip(count: Int = 0) {
        index += count
    }

    fun print() {
        println("token: $tokens\nindex: $index")

    }
}