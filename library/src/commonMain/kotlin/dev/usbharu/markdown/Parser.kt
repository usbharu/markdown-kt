package dev.usbharu.markdown

import dev.usbharu.markdown.AstNode.*
import dev.usbharu.markdown.Token.*
import kotlin.collections.List
import kotlin.js.JsExport

@JsExport
class Parser {
    fun parse(tokens: List<Token>): AstNode {
        val iterator = PeekableTokenIterator(tokens)


        val nodes = mutableListOf<AstNode>()
        while (iterator.hasNext()) {
            val node = when (val next = iterator.next()) {
                is Asterisk, is InlineCodeBlock, is Strike,
                is Text, is Whitespace, Exclamation, ParenthesesEnd, ParenthesesStart,
                SquareBracketStart, SquareBracketEnd, is Url, is UrlTitle -> paragraph(
                    next,
                    iterator
                )

                is Break -> null //todo ただの改行と段落分けの改行のトークンを分ける
                is CheckBox -> TODO()
                is CodeBlock -> TODO()
                is CodeBlockLanguage -> TODO()
                is Header -> header(next, iterator)
                is Html -> TODO()
                is Token.List -> TODO()
                is Quote -> TODO()
                is Separator -> separator(next, iterator)
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
            Exclamation -> image(Exclamation, iterator)
            is InlineCodeBlock -> TODO()
            ParenthesesEnd -> PlainText(")")
            ParenthesesStart -> PlainText("(")
            SquareBracketEnd -> PlainText("]")
            SquareBracketStart -> url(SquareBracketStart, iterator)
            is Strike -> TODO()
            is Text -> plainText(token, iterator)
            is Url -> TODO()
            is UrlTitle -> PlainText("\"${token.title}\"")
            is Whitespace -> whitespace(token, iterator)
            else -> TODO()
        }

        return mutableListOf(node)
    }

    fun whitespace(token: Whitespace, iterator: PeekableTokenIterator): InlineNode {
        return PlainText(token.whitespace.toString().repeat(token.count))
    }

    fun plainText(token: Text, iterator: PeekableTokenIterator): PlainText {
        return PlainText(token.text)
    }

    fun image(exclamation: Exclamation, iterator: PeekableTokenIterator): InlineNode {
        val squareBracketStartToken = iterator.peekOrNull()
        if (squareBracketStartToken !is SquareBracketStart) {
            TODO()
        }
        val url = url(squareBracketStartToken, iterator)
        if (url !is UrlNode) {
            return InlineNodes(mutableListOf(PlainText("!"), url))
        }
        return ImageNode(url)
    }

    fun url(squareBracketStart: SquareBracketStart, iterator: PeekableTokenIterator): InlineNode {
        val urlNameToken = iterator.peekOrNull()
        if (urlNameToken !is Text) {
            return PlainText("[")
        }
        val text = iterator.next() as Text //text
        val urlName = urlName(urlNameToken, iterator)
        if (iterator.peekOrNull() !is SquareBracketEnd) {
            return InlineNodes(mutableListOf(PlainText("["), PlainText(text.text)))
        }
        iterator.skip() // ]
        if (iterator.peekOrNull() !is ParenthesesStart) {
            return InlineNodes(mutableListOf(PlainText("["), PlainText(text.text), PlainText("]")))
        }
        iterator.skip() //(
        if (iterator.peekOrNull() !is Url && iterator.peekOrNull() !is Text) {
            return InlineNodes(mutableListOf(PlainText("[${text.text}](")))
        }
        val textOrUrl = iterator.next()
        val urlUrlNode = if (textOrUrl is Text) {
            UrlUrlNode(textOrUrl.text)
        } else if (textOrUrl is Url) {
            UrlUrlNode(textOrUrl.url)
        } else {
            TODO()
        }
        val whitespace = if (iterator.peekOrNull() is Whitespace) {
            val whitespace = iterator.next() as Whitespace
            whitespace.whitespace.toString().repeat(whitespace.count)
        } else {
            ""
        }
        val urlTitle = if (iterator.peekOrNull() is UrlTitle) {
            UrlTitleNode((iterator.next() as UrlTitle).title)
        } else {
            null
        }
        if (iterator.peekOrNull() !is ParenthesesEnd) {
            return InlineNodes(mutableListOf(PlainText("[${text.text}](${urlTitle?.title.orEmpty()}$whitespace")))
        }
        iterator.skip()
        return UrlNode(urlUrlNode, urlName, urlTitle)
    }

    fun urlName(text: Text, iterator: PeekableTokenIterator): UrlNameNode {
        return UrlNameNode(text.text)
    }

    fun asterisk(token: Asterisk, iterator: PeekableTokenIterator): InlineNode {
        var count = token.count
        var node: InlineNode? = null

        //todo **a*を正しくパースできないので閉じカウンタ的なものを追加し、token.countと閉じカウンタが一致しない場合plaintextに置き換える
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