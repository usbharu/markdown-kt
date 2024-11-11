package dev.usbharu.markdown

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
                        '#' -> header(iterator, tokens)
                        '>' -> quote(iterator, tokens)
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
        iterator.next() //スペースを無視
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
        iterator.next() //スペースを無視
        tokens.add(Text(collect(iterator)))
        tokens.add(Break(1))
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