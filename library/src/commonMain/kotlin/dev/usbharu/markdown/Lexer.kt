package dev.usbharu.markdown

import kotlin.collections.List

class Lexer {
    fun lex(input: String): List<Token> {
        val tokens = mutableListOf<Token>()
        val lines = PeekableStringIterator(input.lines())
        while (lines.hasNext()) {

            if (lines.peekOrNull() == "") {
                blankLine(lines, tokens)
            } else {
                val line = lines.next()

                val iterator = PeekableCharIterator(line.toCharArray())
                while (iterator.hasNext()) {
                    when (val next = iterator.next()) {
                        '#', '＃' -> header(iterator, tokens)
                        '>', '＞' -> quote(iterator, tokens)
                        '-', '=', 'ー', '＝' -> {
                            if (iterator.peekOrNull()?.isWhitespace() == true) { //-の直後がスペースならリストの可能性
                                list(iterator, tokens)
                            } else {//それ以外ならセパレーターの可能性
                                separator(next, iterator, tokens)
                            }
                        }

                        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '０', '１', '２', '３', '４', '５', '６', '７', '８', '９' ->
                            decimalList(iterator, tokens, next)

                        '[', '「' -> {
                            tokens.add(SquareBracketStart)
                        }

                        ']', '」' -> {
                            tokens.add(SquareBracketEnd)
                        }

                        ' ', '　' -> {
                            tokens.add(Whitespace(skipWhitespace(iterator) + 1, next)) //nextの分1足す
                        }

                        else -> {
                            tokens.add(Text(next + collect(iterator)))
                            tokens.add(Break(1))
                        }
                    }
                }


            }

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

    private fun decimalList(
        iterator: PeekableCharIterator,
        tokens: MutableList<Token>,
        next: Char
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
        tokens: MutableList<Token>
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
        tokens: MutableList<Token>
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
        tokens: MutableList<Token>
    ) {
        var count = 1
        while (iterator.peekOrNull()?.isWhitespace() == false) {
            iterator.next()
            count++
        }
        tokens.add(Quote(count))
        skipWhitespace(iterator)
        tokens.add(Text(collect(iterator)))
        tokens.add(Break(1))
    }

    private fun header(
        iterator: PeekableCharIterator,
        tokens: MutableList<Token>
    ) {
        var count = 1
        while (iterator.peekOrNull()?.isWhitespace() == false) {
            iterator.next()
            count++
        }
        tokens.add(Header(count))
        skipWhitespace(iterator)
        tokens.add(Text(collect(iterator)))
        tokens.add(Break(1))
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
        tokens: MutableList<Token>
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