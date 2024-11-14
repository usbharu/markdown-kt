package dev.usbharu.markdown

import kotlin.collections.List

class Lexer {
    fun lex(input: String): List<Token> {
        val tokens = mutableListOf<Token>()
        val lines = PeekableStringIterator(input.lines())

        var inQuote = false
        var inCode = false

        val codeBuffer = StringBuilder()

        line@ while (lines.hasNext()) {

            if (lines.peekOrNull() == "") {
                blankLine(lines, tokens)
            } else {
                val line = lines.next()

                val iterator = PeekableCharIterator(line.toCharArray())
                char@ while (iterator.hasNext()) {
                    val next = iterator.next()
                    when {
                        next == '`' || next == '｀' -> {
                            //todo ````` のようなやつが来たときのことを考える
                            if (iterator.peekOrNull() == next) {
                                val codeBlockBuilder = StringBuilder()
                                codeBlockBuilder.append(next)
                                codeBlockBuilder.append(iterator.next())
                                if (iterator.peekOrNull() == next) {
                                    codeBlockBuilder.append(iterator.next())
                                    if (iterator.peekOrNull() == next) {
                                        tokens.add(Text(codeBlockBuilder.toString()))
                                    } else {
                                        if (inCode) {
                                            inCode = false
                                            tokens.add(CodeBlock(codeBuffer.toString().trimStart('\n').trimEnd('\n')))
                                            codeBuffer.clear()
                                        } else {
                                            inCode = true
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
                                val codeBuilder = StringBuilder()
                                while (iterator.hasNext() && iterator.peekOrNull() != next) {
                                    codeBuilder.append(iterator.next())
                                }
                                if (iterator.hasNext() && iterator.next() == next) { //インラインコードブロックかと思ったら違った
                                    tokens.add(InlineCodeBlock(codeBuilder.toString()))
                                } else {
                                    tokens.add(Text(codeBuilder.insert(0, next).toString()))
                                }

                            }
                        }

                        inCode -> {
                            codeBuffer.append(next)
                        }

                        next == '#' || next == '＃' -> header(iterator, tokens)
                        (next == '>' || next == '＞') && !inQuote -> {
                            inQuote = true
                            quote(iterator, tokens)
                        }

                        next == '-' || next == '=' || next == 'ー' || next == '＝' -> {
                            if (iterator.peekOrNull()?.isWhitespace() == true) { //-の直後がスペースならリストの可能性
                                list(iterator, tokens)
                            } else {//それ以外ならセパレーターの可能性
                                separator(next, iterator, tokens)
                            }
                        }

                        next in '0'..'9' || next in '０'..'９' ->
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
                                tokens.add(Text("!"))
                            }
                        }


                        else -> {
                            val lastToken = tokens.lastOrNull()
                            if (lastToken is Text) {
                                lastToken.text += next.toString()
                            } else {
                                tokens.add(Text(next.toString()))
                            }
                        }
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

        println(tokens)
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
            tokens.add(Text(next.toString()))
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
        tokens.add(Text(next + "" + comma))
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
                tokens.add(Text("[$checkedChar"))
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
            tokens.add(Text("[$checkedChar"))
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
            val token = tokens.lastOrNull()  //ただの文字として追加
            if (token is Text) {
                tokens[tokens.lastIndex] = Text(token.text + builder.toString())
            } else {
                tokens.add(Text(builder.toString()))
            }
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