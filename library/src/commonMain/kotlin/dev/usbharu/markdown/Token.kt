package dev.usbharu.markdown

import kotlin.js.JsExport

@JsExport
sealed class Token {
    abstract var lineAt: Int
    abstract var codePointAt: Int

    data class Text(
        var text: String,
        override var lineAt: Int,
        override var codePointAt: Int
    ) : Token()

    data class LineBreak(
        var count: Int,
        override var lineAt: Int,
        override var codePointAt: Int
    ) : Token()

    data class BlockBreak(override var lineAt: Int, override var codePointAt: Int) : Token()
    data class InQuoteBreak(override var lineAt: Int, override var codePointAt: Int) : Token()
    data class Header(
        var count: Int,
        override var lineAt: Int,
        override var codePointAt: Int
    ) : Token()

    data class Quote(
        var count: Int,
        override var lineAt: Int,
        override var codePointAt: Int
    ) : Token()

    data class Separator(
        var count: Int, val char: Char,
        override var lineAt: Int,
        override var codePointAt: Int
    ) : Token()

    data class Whitespace(
        var count: Int, val whitespace: Char,
        override var lineAt: Int,
        override var codePointAt: Int
    ) : Token()

    abstract class List(val type: ListType) : Token() {
        enum class ListType {
            DISC,
            DECIMAL
        }
    }

    data class DiscList(override var lineAt: Int, override var codePointAt: Int) :
        List(ListType.DISC)

    data class DecimalList(
        val number: Char,
        override var lineAt: Int,
        override var codePointAt: Int
    ) : List(ListType.DECIMAL)

    data class CheckBox(
        val checked: Boolean,
        override var lineAt: Int,
        override var codePointAt: Int
    ) : Token()

    data class SquareBracketStart(override var lineAt: Int, override var codePointAt: Int) :
        Token()

    data class SquareBracketEnd(override var lineAt: Int, override var codePointAt: Int) :
        Token()

    data class ParenthesesStart(override var lineAt: Int, override var codePointAt: Int) :
        Token()

    data class ParenthesesEnd(override var lineAt: Int, override var codePointAt: Int) :
        Token()

    data class Url(var url: String, override var lineAt: Int, override var codePointAt: Int) :
        Token()

    data class Asterisk(
        var count: Int, var char: Char,
        override var lineAt: Int,
        override var codePointAt: Int
    ) : Token()

    data class Exclamation(override var lineAt: Int, override var codePointAt: Int) : Token()
    data class UrlTitle(
        val title: String,
        override var lineAt: Int,
        override var codePointAt: Int
    ) : Token()

    data class InlineCodeBlock(
        val text: String,
        override var lineAt: Int,
        override var codePointAt: Int
    ) : Token()

    data class CodeBlock(
        val text: String,
        override var lineAt: Int,
        override var codePointAt: Int
    ) : Token()

    data class CodeBlockLanguage(
        val language: String, val filename: String,
        override var lineAt: Int,
        override var codePointAt: Int
    ) : Token()

    data class Strike(
        val strike: String,
        override var lineAt: Int,
        override var codePointAt: Int
    ) : Token()

    abstract class Html() : Token()
    data class StartTagStart(
        var tag: String, val void: Boolean,
        override var lineAt: Int,
        override var codePointAt: Int
    ) : Html()

    data class EndTagStart(
        var tag: String,
        override var lineAt: Int,
        override var codePointAt: Int
    ) : Html()

    data class TagEnd(
        var tag: String,
        override var lineAt: Int,
        override var codePointAt: Int
    ) : Html()

    data class AttributeName(
        val name: String,
        override var lineAt: Int,
        override var codePointAt: Int
    ) : Html()

    data class AttributeValue(
        val value: String,
        override var lineAt: Int,
        override var codePointAt: Int
    ) : Html()

    data class HtmlValue(
        val value: String,
        override var lineAt: Int,
        override var codePointAt: Int
    ) : Html()
}

