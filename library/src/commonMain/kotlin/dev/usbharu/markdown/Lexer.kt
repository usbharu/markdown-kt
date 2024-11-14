package dev.usbharu.markdown

import kotlin.collections.List

class Lexer {
    fun lex(input: String): List<Token> {
        val tokens = mutableListOf<Token>()
        val lines = PeekableStringIterator(input.lines())

        var inQuote = false //引用中の判断
        var inCode = false //コードブロック内の判断
        var inline = false //行頭の判断
        val codeBuffer = StringBuilder()

        line@ while (lines.hasNext()) {
            inline = false //改行時にリセット
            if (lines.peekOrNull() == "") {
                blankLine(lines, tokens)
            } else {
                val line = lines.next()
                val iterator = PeekableCharIterator(line.toCharArray())
                char@ while (iterator.hasNext()) {
                    val next = iterator.next()
                    when {
                        next == '`' || next == '｀' -> {
                            inCode = codeblock(iterator, next, tokens, inCode, codeBuffer, inline)
                        }

                        inCode -> {
                            codeBuffer.append(next)
                        }

                        (next == '#' || next == '＃') && !inline -> header(iterator, tokens)
                        (next == '>' || next == '＞') && !inQuote && !inline -> {
                            inQuote = true
                            quote(iterator, tokens)
                        }

                        (next == '-' || next == '=' || next == 'ー' || next == '＝') && !inline -> {
                            if (iterator.peekOrNull()?.isWhitespace() == true) { //-の直後がスペースならリストの可能性
                                list(iterator, tokens)
                            } else {//それ以外ならセパレーターの可能性
                                separator(next, iterator, tokens)
                            }
                        }

                        (next in '0'..'9' || next in '０'..'９') && !inline ->
                            decimalList(iterator, tokens, next)

                        next == '[' || next == '「' -> tokens.add(SquareBracketStart)
                        next == ']' || next == '」' -> tokens.add(SquareBracketEnd)
                        next == '（' || next == '(' -> tokens.add(ParenthesesStart)
                        next == ')' || next == '）' -> tokens.add(ParenthesesEnd)
                        next.isWhitespace() -> tokens.add(
                            Whitespace(
                                skipWhitespace(iterator) + 1,
                                next
                            )
                        ) //nextの分1足す
                        next == 'h' -> url(next, iterator, tokens)
                        next == '*' || next == '_' -> asterisk(iterator, next, tokens)

                        next == '!' -> {
                            if (iterator.peekOrNull() == '[') {
                                tokens.add(Exclamation)
                            } else {
                                addText(tokens, "!")
                            }
                        }

                        next == '~' || next == '～' -> strike(iterator, next, tokens)

                        else -> addText(tokens, next.toString())
                    }
                    if (!inline && tokens.lastOrNull() !is Whitespace) { //行頭が空白の場合は一旦無視する
                        inline = true
                    }
                }


                if (inCode) {
                    codeBuffer.append("\n")
                } else {
                    tokens.add(Break(1))
                }
            }
            inQuote = false
        }

        val lastToken = tokens.lastOrNull()
        if (lastToken is Break) {
            if (lastToken.count == 1) {
                tokens.removeLast()
            } else {
                lastToken.count--
            }
        }
        return tokens
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
                tokens.add(Text(urlBuilder.toString()))
                return
            }
            urlBuilder.append(nextC2)
            charIterator.next()
            iterator.next()
        }
        if (urlBuilder.length == 1) {
            tokens.add(Text(urlBuilder.toString())) //hだけのときはURLじゃないのでテキストとして追加
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
        var count = 0
        while (lines.peekOrNull() == "") {
            lines.next()
            count++
        }
        if (tokens.lastOrNull() is Break) {
            tokens[tokens.lastIndex] = Break(count + 1)
        } else {
            tokens.add(Break(count))
        }
    }
}