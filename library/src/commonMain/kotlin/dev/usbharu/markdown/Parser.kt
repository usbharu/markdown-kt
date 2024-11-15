package dev.usbharu.markdown

import kotlin.collections.List

class Parser {
    fun parse(tokens: List<Token>): AstNode {
        val iterator = PeekableTokenIterator(tokens)


        val nodes = mutableListOf<AstNode>()
        while (iterator.hasNext()) {
            val node = when (val next = iterator.next()) {
                is Asterisk -> paragraph(next, iterator)
                is Break -> null
                is CheckBox -> TODO()
                is CodeBlock -> TODO()
                is CodeBlockLanguage -> TODO()
                Exclamation -> TODO()
                is Header -> header(next, iterator)
                is Html -> TODO()
                is InlineCodeBlock -> TODO()
                is dev.usbharu.markdown.List -> TODO()
                ParenthesesEnd -> TODO()
                ParenthesesStart -> TODO()
                is Quote -> TODO()
                is Separator -> separator(next, iterator)
                SquareBracketEnd -> TODO()
                SquareBracketStart -> TODO()
                is Strike -> TODO()
                is Text -> paragraph(next, iterator)
                is Url -> TODO()
                is UrlTitle -> TODO()
                is Whitespace -> TODO()
            }
            if (node != null) {
                nodes.add(node)
            }
        }
        return RootNode(BodyNode(nodes))
    }

    fun separator(separator: Separator, iterator: PeekableTokenIterator): AstNode {
        return SeparatorNode
    }

    fun header(header: Header, iterator: PeekableTokenIterator): AstNode {
        val peekOrNull = iterator.peekOrNull()
        val headerTextNode = if (peekOrNull is Text) {
            iterator.next()
            HeaderTextNode(peekOrNull.text)
        } else {
            null
        }
        return HeaderNode(header.count, headerTextNode)
    }

    fun paragraph(token: Token, iterator: PeekableTokenIterator): AstNode {
        return ParagraphNode(inline(token, iterator))
    }

    fun inline(token: Token, iterator: PeekableTokenIterator): MutableList<InlineNode> {
        println("inline start token:$token")
        iterator.print()
        val node = when (token) {
            is Asterisk -> asterisk(token, iterator)
            Exclamation -> TODO()
            is InlineCodeBlock -> TODO()
            ParenthesesEnd -> TODO()
            ParenthesesStart -> TODO()
            SquareBracketEnd -> TODO()
            SquareBracketStart -> TODO()
            is Strike -> TODO()
            is Text -> plainText(token, iterator)
            is Url -> TODO()
            is UrlTitle -> TODO()
            is Whitespace -> TODO()
            else -> TODO()
        }

        return mutableListOf(node)
    }

    fun plainText(token: Text, iterator: PeekableTokenIterator): PlainText {
        return PlainText(token.text)
    }

    fun asterisk(token: Asterisk, iterator: PeekableTokenIterator): InlineNode {
        var count = token.count
        var node: InlineNode? = null
        while ((count > 0)) {
            if (count == 3) {
                val italicBold = italic(token, iterator, 3)
                if (italicBold != null) {
                    return italicBold
                }
                count--
            }
            if (count == 2) {
                val italicBold = italic(token, iterator, 2)
                if (italicBold != null) {
                    if (node == null) {
                        node = italicBold
                        count = 1
                        continue
                    } else {
                        when (node) {
                            is BoldNode -> node.nodes.add(italicBold)
                            is ItalicNode -> node.nodes.add(italicBold)
                            else -> TODO()
                        }
                        return node
                    }
                }
                count--
            }
            if (count == 1) {
                val italicBold = italic(token, iterator, 1)
                if (italicBold != null) {
                    if (node == null) {
                        node = italicBold
                        count = 2
                        continue
                    } else {
                        when (node) {
                            is BoldNode -> node.nodes.add(italicBold)
                            is ItalicNode -> node.nodes.add(italicBold)
                            else -> TODO()
                        }
                        return node
                    }
                }
                count--
            }
        }

        return node!!
    }

    fun italic(token: Asterisk, iterator: PeekableTokenIterator, count: Int): InlineNode? {
        println("italic $count")
        iterator.print()
        var counter = 0
        val tokens = mutableListOf<Token>()
        while (iterator.peekOrNull(counter) != null &&
            iterator.peekOrNull(counter) !is Break &&
            iterator.peekOrNull(counter) !is Asterisk
        ) {
            tokens.add(iterator.peekOrNull(counter)!!)
            println(tokens)
            counter++
        }
        if (iterator.peekOrNull(counter) != null &&
            (iterator.peekOrNull(counter) is Asterisk &&
                    (iterator.peekOrNull(counter) as Asterisk).count == count)
        ) {
            println("italic found!!! $count")
            iterator.skip(counter + 1)
            val inline = inline(tokens.first(), PeekableTokenIterator(tokens))

            return when (count) {
                1 -> ItalicNode(inline)
                2 -> BoldNode(inline)
                3 -> ItalicNode(mutableListOf(BoldNode(inline)))
                else -> {
                    TODO()
                }
            }

        }
        println("return null")
        iterator.print()
        return null
    }
}