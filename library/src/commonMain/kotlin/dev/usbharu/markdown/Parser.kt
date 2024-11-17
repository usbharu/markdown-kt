package dev.usbharu.markdown

import dev.usbharu.markdown.AstNode.*
import dev.usbharu.markdown.Token.*
import dev.usbharu.markdown.Token.List.ListType.DECIMAL
import dev.usbharu.markdown.Token.List.ListType.DISC
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
                SquareBracketStart, SquareBracketEnd, is Url, is UrlTitle, is LineBreak -> paragraph(
                    next,
                    iterator
                )

                is BlockBreak -> null
                is CheckBox -> TODO()
                is CodeBlock -> TODO()
                is CodeBlockLanguage -> TODO()
                is Header -> header(next, iterator)
                is Html -> TODO()
                is Token.List -> list(next, iterator)
                is Quote -> quote(next, iterator)
                is Separator -> separator(next, iterator)
                InQuoteBreak -> null
            }
            if (node != null) {
                nodes.add(node)
            }
        }
        return RootNode(BodyNode(nodes))
    }

    fun list(list: Token.List, iterator: PeekableTokenIterator): ListNode {
        return internalList(list, iterator, 1)
    }

    fun internalList(list: Token.List, iterator: PeekableTokenIterator, currentNest: Int): ListNode {
        val listItems = mutableListOf<ListItemNode>()
        list@ while (iterator.hasNext() && (iterator.peekOrNull() is Token.List || isInline(iterator.peekOrNull()))) {
            val item = mutableListOf<ListableNode>()
            listItem@ while (isInline(iterator.peekOrNull()) && iterator.peekOrNull() !is Token.List && iterator.peekOrNull() !is LineBreak) {
                val next = iterator.next()
                val inline = inline(next, iterator)
                println("internalList inline: " + inline)
                item.addAll(inline)
            }
            while (iterator.peekOrNull() is LineBreak) {
                iterator.skip()
            }
            val count =
                if (iterator.peekOrNull() is Whitespace) {
                    val whitespace = iterator.next() as Whitespace
                    whitespace.count
                } else {
                    1
                }
            println("count = $count,currentNest = $currentNest,peek = ${iterator.peekOrNull()}")
            if (currentNest < count && iterator.peekOrNull() is Token.List) {
                item.add(internalList(iterator.next() as Token.List, iterator, count))
            }
            if (currentNest > count && iterator.peekOrNull() is Token.List) {
                iterator.skip()
                if (item.isNotEmpty()) {
                    listItems.add(ListItemNode(item))
                }
                break
            }
            val peekOrNull = iterator.peekOrNull()
            if (peekOrNull is Token.List) {

                if (peekOrNull.type != list.type) {
                    if (item.isNotEmpty()) {
                        listItems.add(ListItemNode(item))
                    }
                    break
                } else {
                    iterator.skip()
                }
            }

            if (item.isNotEmpty()) {
                listItems.add(ListItemNode(item))
            }
        }
        println("end $currentNest")
        return when (list.type) {
            DISC -> DiscListNode(listItems)
            DECIMAL -> DecimalListNode(listItems)
        }
    }

    tailrec fun addQuote(quoteNode: QuoteNode, quotableNode: List<QuotableNode>, nest: Int) {
        if (nest == 1) {
            quoteNode.nodes.addAll(quotableNode)
            return
        }
        addQuote(quoteNode.nodes.findLast { it is QuoteNode } as QuoteNode, quotableNode, nest.dec())
    }

    fun quote(quote: Quote, iterator: PeekableTokenIterator): QuoteNode {
        var quote2 = quote
        var maxNest = quote.count
        val quoteNode = createNest(maxNest)
        while (true) {
            val list = mutableListOf<QuotableNode>()
            while (isInline(iterator.peekOrNull()) && iterator.peekOrNull() !is InQuoteBreak) {
                println("next token: " + iterator.peekOrNull())
                list.addAll(inline(iterator.next(), iterator))
            }
            if (iterator.peekOrNull() is InQuoteBreak) {
                list.add(BreakNode)
                iterator.skip()
            }
            if (maxNest < quote2.count) {
                addQuote(quoteNode, mutableListOf(createNest(quote2.count - maxNest)), maxNest)
                maxNest = quote2.count
            }
            addQuote(quoteNode, list, quote2.count)
            if (iterator.peekOrNull() is Quote) {
                quote2 = iterator.next() as Quote
            } else {
                break
            }

        }
        return quoteNode
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

    fun paragraph(token: Token, iterator: PeekableTokenIterator): AstNode? {
        val list = mutableListOf<InlineNode>()
        var token2: Token? = token
        do {
            list.addAll(inline(token2!!, iterator))
            if (iterator.hasNext() && isInline(iterator.peekOrNull())) {
                token2 = iterator.next()
            } else {
                token2 = iterator.peekOrNull()
            }
        } while (iterator.hasNext() && isInline(token2))
        if (list.isEmpty()) {
            return null
        }
        return ParagraphNode(list)
    }

    fun isInline(token: Token?): Boolean {
        return when (token) {
            is Asterisk, is InlineCodeBlock, is Strike,
            is Text, is Whitespace, Exclamation, ParenthesesEnd, ParenthesesStart,
            SquareBracketStart, SquareBracketEnd, is Url, is UrlTitle, is LineBreak, is InQuoteBreak -> true

            else -> false
        }
    }

    fun inline(token: Token, iterator: PeekableTokenIterator): MutableList<InlineNode> {
        println("inline start token:$token")
        iterator.print()
        val node = when (token) {
            is Asterisk -> asterisk(token, iterator)
            Exclamation -> image(Exclamation, iterator)
            is InlineCodeBlock -> inlineCodeBlock(token, iterator)
            ParenthesesEnd -> PlainText(")")
            ParenthesesStart -> PlainText("(")
            SquareBracketEnd -> PlainText("]")
            SquareBracketStart -> url(SquareBracketStart, iterator)
            is Strike -> TODO()
            is Text -> plainText(token, iterator)
            is Url -> inlineUrl(token, iterator)
            is UrlTitle -> PlainText("\"${token.title}\"")
            is Whitespace -> whitespace(token, iterator)
            is LineBreak -> null
            else -> {
                println("error" + token)
                TODO()
            }
        }

        if (node != null) {
            return mutableListOf(node)
        }
        return mutableListOf()
    }

    fun inlineUrl(url: Url, iterator: PeekableTokenIterator): SimpleUrlNode {
        return SimpleUrlNode(url.url)
    }

    fun inlineCodeBlock(inlineCodeBlock: InlineCodeBlock, iterator: PeekableTokenIterator): InlineCodeNode {
        return InlineCodeNode(inlineCodeBlock.text)
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
            iterator.peekOrNull(counter) !is LineBreak &&
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

    companion object {
        fun createNest(nest: Int, quoteNode: QuoteNode = QuoteNode(mutableListOf())): QuoteNode {
            createNest2(nest, quoteNode, quoteNode)
            return quoteNode
        }

        tailrec fun createNest2(nest: Int, current: QuoteNode, quoteNode: QuoteNode): QuoteNode {
            if (nest == 1) {
                return quoteNode
            }
            val element = QuoteNode(mutableListOf())
            current.nodes.add(element)
            return createNest2(nest - 1, element, quoteNode)
        }
    }
}