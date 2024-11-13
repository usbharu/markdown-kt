package dev.usbharu.markdown

import kotlin.collections.List

class Lexer {
    fun lex(input: String): List<Token> {
        val tokens = mutableListOf<Token>()
        val lines = PeekableStringIterator(input.lines())

        var inQuote = false

        line@ while (lines.hasNext()) {

            if (lines.peekOrNull() == "") {
                blankLine(lines, tokens)
            } else {
                val line = lines.next()

                val iterator = PeekableCharIterator(line.toCharArray())
                char@ while (iterator.hasNext()) {
                    val next = iterator.next()
                    when {
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

                tokens.add(Break(1))
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
        //todo nextでみずにpeekでみて確認してからnextする hのあとにアスタリスク等が来たときに対応できない
        val charIterator = "ttps://".iterator()
        val urlBuilder = StringBuilder()
        urlBuilder.append(next)
        while (charIterator.hasNext() && iterator.hasNext()) {
//            charIterator
            val nextC = charIterator.next()
            val nextC2 = iterator.next()
            urlBuilder.append(nextC2)
            if (nextC != nextC2) {
                tokens.add(Text(urlBuilder.toString()))
                return
            }
        }
        if (urlBuilder.length == 1) {
            tokens.add(Text(urlBuilder.toString())) //hだけのときはURLじゃないのでテキストとして追加
        } else {
            while (iterator.hasNext() && iterator.peekOrNull()?.isWhitespace() != true) {
                urlBuilder.append(iterator.next())
            }
            tokens.add(Url(urlBuilder.toString()))
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