package dev.usbharu.markdown

sealed class Token()

data class Text(var text: String) : Token()
data class Break(var count: Int) : Token()
data class Header(var count: Int) : Token()
data class Quote(var count: Int) : Token()
data class Separator(var count: Int, val char: Char) : Token()
data class Whitespace(var count: Int, val whitespace: Char) : Token()
abstract class List(val type: ListType) : Token() {
    enum class ListType {
        DISC,
        DECIMAL
    }
}

data object DiscList : List(ListType.DISC)
data class DecimalList(val number: Char) : List(ListType.DECIMAL)
data class CheckBox(val checked: Boolean) : Token()
data object SquareBracketStart : Token()
data object SquareBracketEnd : Token()