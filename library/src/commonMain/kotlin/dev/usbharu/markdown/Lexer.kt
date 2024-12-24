package dev.usbharu.markdown

import dev.usbharu.markdown.Token.*
import kotlin.collections.List
import kotlin.js.JsExport

@JsExport
class Lexer {
    fun lex(input: String): List<Token> {
        val tokens = mutableListOf<Token>()
        val lines = PeekableStringIterator(input.lines())

        var inQuote = false //引用中の判断
        var inCode = false //コードブロック内の判断
        var inline = false //行頭の判断
        var htmlNest = 0
        val codeBuffer = StringBuilder()

        line@ while (lines.hasNext()) {
            inline = false //改行時にリセット
            if (lines.peekOrNull() == "") {
                blankLine(lines, tokens)
            } else {
                val line = lines.next()
                val iterator = PeekableCharIterator(line.toCharArray())
                char@ while (iterator.hasNext()) {
                    val next = iterator.next().toString()
                    when {
                        next == "`" || next == "｀" -> {
                            inCode = codeblock(iterator, next[0], tokens, inCode, codeBuffer, inline)
                        }

                        inCode -> codeBuffer.append(next)

                        next == "<" -> htmlNest = html(iterator, htmlNest, codeBuffer, tokens, next[0])

                        htmlNest != 0 -> codeBuffer.append(next)

                        (next == "#" || next == "＃") && !inline -> header(iterator, tokens)
                        (next == ">" || next == "＞") && !inQuote && !inline -> {
                            inQuote = true
                            quote(iterator, tokens)
                        }

                        (next == "-" || next == "=" || next == "ー" || next == "＝") && !inline -> {
                            if (iterator.peekOrNull()?.isWhitespace() == true) { //-の直後がスペースならリストの可能性
                                list(iterator, tokens)
                            } else {//それ以外ならセパレーターの可能性
                                separator(next[0], iterator, tokens)
                            }
                        }

                        (next in "0".."9" || next in "０".."９") && !inline ->
                            decimalList(iterator, tokens, next[0])

                        next == "[" || next == "「" -> tokens.add(SquareBracketStart)
                        next == "]" || next == "」" -> tokens.add(SquareBracketEnd)
                        next == "（" || next == "(" -> tokens.add(ParenthesesStart)
                        next == ")" || next == "）" -> tokens.add(ParenthesesEnd)
                        next.isBlank() -> tokens.add(
                            Whitespace(
                                skipWhitespace(iterator) + 1,
                                next[0]
                            )
                        ) //nextの分1足す
                        next == "h" -> url(next[0], iterator, tokens)
                        next == "*" || next == "_" -> asterisk(iterator, next[0], tokens)

                        next == "!" -> {
                            if (iterator.peekOrNull() == '[') {
                                tokens.add(Exclamation)
                            } else {
                                addText(tokens, "!")
                            }
                        }

                        next == "~" || next == "～" -> strike(iterator, next[0], tokens)

                        else -> addText(tokens, next)
                    }
                    if (!inline && tokens.lastOrNull() !is Whitespace) { //行頭が空白の場合は一旦無視する
                        inline = true
                    }
                }


                if (inCode) {
                    codeBuffer.append("\n")
                } else if (htmlNest != 0) {
                    codeBuffer.append(" ")
                } else {
                    addBreak(tokens, inQuote)
                }
            }
            inQuote = false
        }

        val lastToken = tokens.lastOrNull()
        if (lastToken is LineBreak) {
            if (lastToken.count == 1) {
                tokens.removeLast()
            } else {
                lastToken.count--
            }
        }
        if (lastToken is BlockBreak) {
            tokens.removeLast()
            tokens.add(LineBreak(1))
        }
        if (lastToken is InQuoteBreak) {
            tokens.removeLast()
        }
        return tokens
    }

    private fun html(
        iterator: PeekableCharIterator,
        htmlNest: Int,
        codeBuffer: StringBuilder,
        tokens: MutableList<Token>,
        next: Char
    ): Int {
        var htmlNest1 = htmlNest
        var endTag = false
        var counter = 0
        if (iterator.peekOrNull() == '/') {
            endTag = true
            counter++
        }
        val tagNameBuilder = StringBuilder()
        counter = skipPeekWhitespace(iterator, counter)

        while (iterator.peekOrNull(counter) != null &&
            iterator.peekOrNull(counter)?.isWhitespace() != true &&
            iterator.peekOrNull(counter) != '>' &&
            iterator.peekOrNull(counter) != '/'
        ) {
            tagNameBuilder.append(iterator.peekOrNull(counter))
            counter++
        }

        counter = skipPeekWhitespace(iterator, counter)
        val attributeList = mutableListOf<Token>()
        intag@ while (iterator.peekOrNull(counter) != null &&
            (iterator.peekOrNull(counter) != '/' && iterator.peekOrNull(counter) != '>')
        ) {
            val attrBuilder = StringBuilder()
            attr@ while (iterator.peekOrNull(counter) != null && (iterator.peekOrNull(counter) != '=' && iterator.peekOrNull(
                    counter
                )?.isWhitespace() != true)
            ) {
                attrBuilder.append(iterator.peekOrNull(counter))
                counter++
            }
            attributeList.add(AttributeName(attrBuilder.toString()))
            counter = skipPeekWhitespace(iterator, counter)
            if (iterator.peekOrNull(counter) == '=') {
                counter++
                if (iterator.peekOrNull(counter) == '"') {
                    counter++
                    //todo エスケープシーケンス
                    val peekString = offsetPeekString(iterator, counter, '"')
                    counter = peekString?.second?.minus(1) ?: counter
                    if (peekString != null) {
                        attributeList.add(AttributeValue(peekString.first))
                    } else {
                        break@intag
                    }
                }
            }
            counter++
            counter = skipPeekWhitespace(iterator, counter)
        }

        val void = if (iterator.peekOrNull(counter) == '/') {//閉じタグ省略
            counter = skipPeekWhitespace(iterator, counter)
            val peekString = offsetPeekString(iterator, counter, '>')
            counter = peekString?.second?.minus(1) ?: counter
            htmlNest1-- //あとで1増えるので相殺するためにあらかじめ1減らしておく
            true
        } else {
            false
        }
        if (iterator.peekOrNull(counter) == '>') { //タグか判定
            if (codeBuffer.isNotBlank()) { //タグ間に文字があれば追加する
                tokens.add(HtmlValue(codeBuffer.toString().trim()))
                codeBuffer.clear()
            }
            if (endTag) {//閉じタグ判定
                htmlNest1-- //閉じタグならネストを一つ減らす
                tokens.add(EndTagStart(tagNameBuilder.toString()))
            } else {
                htmlNest1++
                tokens.add(StartTagStart(tagNameBuilder.toString(), void))
            }
            tokens.addAll(attributeList)
            tokens.add(TagEnd(tagNameBuilder.toString()))
            iterator.skip(counter + 1)
        } else {
            addText(tokens, next.toString())
        }
        return htmlNest1
    }

    private fun strike(
        iterator: PeekableCharIterator,
        next: Char,
        tokens: MutableList<Token>,
    ) {
        if (iterator.peekOrNull() == next) {
            iterator.next()
            val peekString = peekString(iterator, next, next)
            if (peekString == null) {
                addText(tokens, "$next$next")
            } else {
                tokens.add(Strike(peekString))
                iterator.skip(peekString.length + 2)
            }
        } else {
            addText(tokens, next.toString())
        }
    }

    private fun addText(tokens: MutableList<Token>, next: String) {
        val lastToken = tokens.lastOrNull()
        if (lastToken is Text) {
            lastToken.text += next
        } else {
            tokens.add(Text(next))
        }
    }

    private fun codeblock(
        iterator: PeekableCharIterator,
        next: Char,
        tokens: MutableList<Token>,
        inCode: Boolean,
        codeBuffer: StringBuilder,
        inline: Boolean,
    ): Boolean {
        var inCode1 = inCode
        if (iterator.peekOrNull() == next && !inline) { //行頭かつ次の文字が`
            val codeBlockBuilder = StringBuilder()
            codeBlockBuilder.append(next)
            codeBlockBuilder.append(iterator.next())
            if (iterator.peekOrNull() == next) {
                codeBlockBuilder.append(iterator.next())
                if (iterator.peekOrNull() == next) {
                    tokens.add(Text(codeBlockBuilder.toString()))
                } else {
                    if (inCode1) {
                        inCode1 = false
                        tokens.add(CodeBlock(codeBuffer.toString().trimStart('\n').trimEnd('\n')))
                        codeBuffer.clear()
                    } else {
                        inCode1 = true
                        var inFilename = false
                        val language = StringBuilder()
                        val filename = StringBuilder()
                        if (iterator.hasNext()) {
                            codeBlock@ while (iterator.hasNext()) {
                                val nextLanguage = iterator.next()
                                if ((nextLanguage == ':' || nextLanguage == '：') && !inFilename) {
                                    inFilename = true
                                    continue@codeBlock
                                }
                                if (inFilename) {
                                    filename.append(nextLanguage)
                                } else {
                                    language.append(nextLanguage)
                                }

                            }
                            tokens.add(CodeBlockLanguage(language.toString(), filename.toString()))
                        }

                    }
                }

            } else if (iterator.peekOrNull() == null) {
                tokens.add(Text(codeBlockBuilder.toString()))
            }

        } else {
            val peekString = peekString(iterator, next)
            if (peekString != null && peekString.isEmpty()) {
                addText(tokens, "$next$next")
                iterator.next()
            } else if (peekString != null) {
                tokens.add(InlineCodeBlock(peekString))
                iterator.skip(peekString.length + 1)
            } else {
                addText(tokens, next.toString())
            }
        }
        return inCode1
    }

    private fun asterisk(
        iterator: PeekableCharIterator,
        next: Char,
        tokens: MutableList<Token>,
    ) {
        var count = 1
        while (iterator.peekOrNull() == next) {
            count++
            iterator.next()
        }
        tokens.add(Asterisk(count, next))
    }

    private fun url(
        next: Char,
        iterator: PeekableCharIterator,
        tokens: MutableList<Token>,
    ) {
        //todo httpにも対応
        val charIterator = PeekableCharIterator("ttps://".toCharArray())
        val urlBuilder = StringBuilder()
        urlBuilder.append(next)
        while (charIterator.hasNext() && iterator.hasNext()) {
            val nextC = charIterator.peekOrNull() ?: return
            val nextC2 = iterator.peekOrNull() ?: return
            if (nextC != nextC2) {
                addText(tokens, urlBuilder.toString())
                return
            }
            urlBuilder.append(nextC2)
            charIterator.next()
            iterator.next()
        }
        if (urlBuilder.length == 1) {
            addText(tokens, urlBuilder.toString()) //hだけのときはURLじゃないのでテキストとして追加
        } else {
            while (iterator.hasNext() && (iterator.peekOrNull()
                    ?.isWhitespace() != true && iterator.peekOrNull() != ')')
            ) {
                urlBuilder.append(iterator.next())
            }
            tokens.add(Url(urlBuilder.toString()))
            skipWhitespace(iterator)
            val doubleQuotation = iterator.peekOrNull()
            if (iterator.peekOrNull() == '"' || doubleQuotation == '”') {
                iterator.next()
                doubleQuotation!!
                val titleBuilder = StringBuilder()
                while (iterator.hasNext() && iterator.peekOrNull() != doubleQuotation && iterator.peekOrNull() != ')') {
                    titleBuilder.append(iterator.next())
                }
                if (iterator.peekOrNull() == doubleQuotation) {
                    iterator.next()
                }
                tokens.add(UrlTitle(titleBuilder.toString()))
            }
        }
    }

    private fun decimalList(
        iterator: PeekableCharIterator,
        tokens: MutableList<Token>,
        next: Char,
    ) {
        val comma = iterator.peekOrNull()
        if (comma == null) {
            addText(tokens, next.toString())
            return
        }
        if (comma == '.' || comma == '。' || comma == '、') {
            iterator.next()
            if (iterator.peekOrNull()?.isWhitespace() == true) {
                iterator.next()
                tokens.add(DecimalList(next))
                return
            }
        }
        addText(tokens, next + "" + comma)
    }

    private fun list(
        iterator: PeekableCharIterator,
        tokens: MutableList<Token>,
    ) {

        if (iterator.peekOrNull()?.isWhitespace() == true) {
            tokens.add(DiscList)
        }

        skipWhitespace(iterator)
        if (iterator.peekOrNull() == '[') {
            iterator.next()
            val checkedChar = iterator.peekOrNull() ?: return
            iterator.next()
            if ((checkedChar == 'x' || checkedChar == ' ' || checkedChar == '　').not()) {
                addText(tokens, "[$checkedChar")
                return
            }
            val checked = checkedChar == 'x'
            if (iterator.peekOrNull() == ']') {
                iterator.next()
                if (iterator.peekOrNull()?.isWhitespace() == true) {
                    iterator.next()
                    tokens.add(CheckBox(checked))
                    return
                }
            }
            addText(tokens, "[$checkedChar")
        }
    }

    private fun separator(
        next: Char,
        iterator: PeekableCharIterator,
        tokens: MutableList<Token>,
    ) {
        val builder = StringBuilder()
        builder.append(next)

        while (iterator.peekOrNull() == next) {
            builder.append(iterator.next())
        }
        if (iterator.peekOrNull() == null && builder.length >= 3) { //行末まで到達していてかつ長さが3以上か
            tokens.add(Separator(builder.length, next)) //セパレーターとして追加
        } else {
            addText(tokens, builder.toString())
        }
    }

    private fun quote(
        iterator: PeekableCharIterator,
        tokens: MutableList<Token>,
    ) {
        var count = 1
        while (iterator.peekOrNull()?.isWhitespace() == false) {
            iterator.next()
            count++
        }
        tokens.add(Quote(count))
        skipWhitespace(iterator)
    }

    private fun header(
        iterator: PeekableCharIterator,
        tokens: MutableList<Token>,
    ) {
        var count = 1
        while (iterator.peekOrNull()?.isWhitespace() == false) {
            iterator.next()
            count++
        }
        tokens.add(Header(count))
        skipWhitespace(iterator)
        tokens.add(Text(collect(iterator)))
    }

    fun skipWhitespace(iterator: PeekableCharIterator): Int {
        var count = 0
        while (iterator.peekOrNull()?.isWhitespace() == true) {
            iterator.next()
            count++
        }
        return count
    }

    fun skipPeekWhitespace(iterator: PeekableCharIterator, currentOffset: Int = 0): Int {
        var offset = currentOffset
        while (iterator.peekOrNull(offset)?.isWhitespace() == true) {
            offset++
        }
        return offset
    }

    fun offsetPeekString(iterator: PeekableCharIterator, offset: Int = 0, vararg chars: Char): Pair<String, Int>? {
        var counter = offset
        val stringBuilder = StringBuilder()
        var checkCounter = 0
        while (iterator.peekOrNull(counter) != null && checkCounter < chars.size) {
            stringBuilder.append(iterator.peekOrNull(counter))
            if (iterator.peekOrNull(counter) == chars[checkCounter]) {
                checkCounter++
            } else {
                checkCounter = 0
            }
            counter++
        }
        if (iterator.peekOrNull(counter) == null && checkCounter != chars.size) {
            return null
        }
        val string = stringBuilder.toString()
        return string.substring(0, string.length - chars.size) to counter
    }

    fun peekString(iterator: PeekableCharIterator, vararg char: Char): String? {
        var counter = 0
        val stringBuilder = StringBuilder()
        var checkCounter = 0
        while (iterator.peekOrNull(counter) != null && checkCounter < char.size) {
            stringBuilder.append(iterator.peekOrNull(counter))
            if (iterator.peekOrNull(counter) == char[checkCounter]) {
                checkCounter++
            } else {
                checkCounter = 0
            }
            counter++
        }
        if (iterator.peekOrNull(counter) == null && checkCounter != char.size) {
            return null
        }
        val string = stringBuilder.toString()
        return string.substring(0, string.length - char.size)
    }

    fun collect(iterator: PeekableCharIterator): String {
        val char = mutableListOf<Char>()
        while (iterator.hasNext()) {
            char.add(iterator.next())
        }
        return char.joinToString("")
    }

    /**
     * 完全な空行
     */
    private fun blankLine(
        lines: PeekableStringIterator,
        tokens: MutableList<Token>,
    ) {
        while (lines.peekOrNull() == "") {
            lines.skip()
            addBreak(tokens)
        }
    }

    fun addBreak(tokens: MutableList<Token>, inQuote: Boolean = false) {
        if (inQuote) {
            tokens.add(InQuoteBreak)
            return
        }
        val lastOrNull = tokens.lastOrNull()
        if (lastOrNull is LineBreak && 1 <= lastOrNull.count) {
            tokens.removeLast()
            tokens.add(BlockBreak)
        } else {
            tokens.add(LineBreak(1))
        }
    }
}