package dev.usbharu.markdown

import kotlin.js.JsExport

@JsExport
sealed class Token {
    data class Text(var text: String) : Token()
    data class LineBreak(var count: Int) : Token()
    data object BlockBreak : Token()
    data object InQuoteBreak : Token()
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
    data object ParenthesesStart : Token()
    data object ParenthesesEnd : Token()
    data class Url(var url: String) : Token()
    data class Asterisk(var count: Int, var char: Char) : Token()
    data object Exclamation : Token()
    data class UrlTitle(val title: String) : Token()
    data class InlineCodeBlock(val text: String) : Token()
    data class CodeBlock(val text: String) : Token()
    data class CodeBlockLanguage(val language: String, val filename: String) : Token()
    data class Strike(val strike: String) : Token()

    abstract class Html() : Token()
    data class StartTagStart(var tag: String, val void: Boolean) : Html()
    data class EndTagStart(var tag: String) : Html()
    data class TagEnd(var tag: String) : Html()
    data class AttributeName(val name: String) : Html()
    data class AttributeValue(val value: String) : Html()
    data class HtmlValue(val value: String) : Html()
}

