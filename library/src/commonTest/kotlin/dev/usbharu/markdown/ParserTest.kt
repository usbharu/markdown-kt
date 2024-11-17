package dev.usbharu.markdown

import dev.usbharu.markdown.AstNode.*
import dev.usbharu.markdown.Token.*
import kotlin.test.Test
import kotlin.test.assertEquals

class ParserTest {
    @Test
    fun header() {
        val parser = Parser()

        val actual = parser.parse(listOf(Header(1), Text("a b c")))

        println(actual)
        println(actual.print())

        assertEquals(
            RootNode(
                BodyNode(
                    listOf(
                        HeaderNode(1, HeaderTextNode("a b c"))
                    )
                )
            ), actual
        )
    }

    @Test
    fun header複数() {
        val parser = Parser()

        val actual = parser.parse(listOf(Header(1), Text("a b c"), LineBreak(1), Header(2), Text("d e f")))

        println(actual)
        println(actual.print())

        assertEquals(
            RootNode(
                BodyNode(
                    listOf(
                        HeaderNode(1, HeaderTextNode("a b c")),
                        HeaderNode(2, HeaderTextNode("d e f")),
                    )
                )
            ), actual
        )
    }

    @Test
    fun asterisk() {
        val parser = Parser()

        val actual = parser.parse(listOf(Asterisk(1, '*'), Text("a"), Asterisk(1, '*')))

        println(actual)
        println(actual.print())

        assertEquals(
            RootNode(
                BodyNode(
                    listOf(
                        ParagraphNode(listOf(ItalicNode(mutableListOf(PlainText("a")))))
                    )
                )
            ), actual
        )
    }

    @Test
    fun asterisk2() {
        val parser = Parser()

        val actual = parser.asterisk(
            Asterisk(1, '*'), PeekableTokenIterator(listOf(Text("a"), Asterisk(1, '*')))
        )

        println(actual)
        println(actual.print())
    }

    @Test
    fun bold() {
        val parser = Parser()

        val actual = parser.parse(listOf(Asterisk(2, '*'), Text("a"), Asterisk(2, '*')))

        println(actual)
        println(actual.print())

        assertEquals(
            RootNode(
                BodyNode(
                    listOf(
                        ParagraphNode(listOf(BoldNode(mutableListOf(PlainText("a")))))
                    )
                )
            ), actual
        )
    }

    @Test
    fun italicBold() {
        val parser = Parser()

        val actual = parser.parse(listOf(Asterisk(3, '*'), Text("a"), Asterisk(3, '*')))

        println(actual)
        println(actual.print())

        assertEquals(
            RootNode(
                BodyNode(
                    listOf(
                        ParagraphNode(listOf(ItalicNode(mutableListOf(BoldNode(mutableListOf(PlainText("a")))))))
                    )
                )
            ), actual
        )
    }

    @Test
    fun italicとbold() {
        val parser = Parser()

        val actual = parser.parse(
            listOf(
                Asterisk(3, '*'), Text("a"), Asterisk(2, '*'), Text("b"), Asterisk(1, '*')
            )
        )

        println(actual)
        println(actual.print())

        assertEquals(
            RootNode(
                BodyNode(
                    listOf(
                        ParagraphNode(
                            listOf(
                                BoldNode(
                                    mutableListOf(
                                        PlainText("a"), ItalicNode(mutableListOf(PlainText("b")))
                                    )
                                )
                            )
                        )
                    )
                )
            ), actual
        )
    }

    @Test
    fun italicとbold2() {
        val parser = Parser()

        val actual = parser.parse(
            listOf(
                Asterisk(3, '*'), Text("a"), Asterisk(1, '*'), Text("b"), Asterisk(2, '*')
            )
        )

        println(actual)
        println(actual.print())

        assertEquals(
            RootNode(
                BodyNode(
                    listOf(
                        ParagraphNode(
                            listOf(
                                ItalicNode(
                                    mutableListOf(
                                        PlainText("a"), BoldNode(mutableListOf(PlainText("b")))
                                    )
                                )
                            )
                        )
                    )
                )
            ), actual
        )
    }

    @Test
    fun plainText() {
        val parser = Parser()

        val actual = parser.parse(
            listOf(
                Text("hello")
            )
        )

        println(actual)
        println(actual.print())

        assertEquals(
            RootNode(
                BodyNode(
                    listOf(
                        ParagraphNode(
                            listOf(
                                PlainText("hello")
                            )
                        )
                    )
                )
            ), actual
        )
    }

    @Test
    fun separator() {
        val parser = Parser()

        val actual = parser.parse(listOf(Separator(3, '-')))

        println(actual)
        println(actual.print())

        assertEquals(
            RootNode(
                BodyNode(
                    listOf(
                        SeparatorNode
                    )
                )
            ), actual
        )
    }

    @Test
    fun separator2() {
        val parser = Parser()

        val actual = parser.parse(listOf(Separator(3, '-'), LineBreak(1), Separator(3, '-')))

        println(actual)
        println(actual.print())

        assertEquals(
            RootNode(
                BodyNode(
                    listOf(
                        SeparatorNode,
                        SeparatorNode,
                    )
                )
            ), actual
        )
    }

    @Test
    fun アスタリスク不正() {
        val parser = Parser()

        val actual = parser.parse(listOf(Asterisk(2, '*'), Text("a"), Asterisk(1, '*')))

        println(actual)
        println(actual.print())

        assertEquals(
            RootNode(
                BodyNode(
                    listOf(
                        ParagraphNode(listOf(BoldNode(mutableListOf(PlainText("a")))))
                    )
                )
            ), actual
        )
    }

    @Test
    fun url() {
        val parser = Parser()

        val actual = parser.parse(
            listOf(
                SquareBracketStart,
                Text("alt"),
                SquareBracketEnd,
                ParenthesesStart,
                Url("https://example.com"),
                UrlTitle("example"),
                ParenthesesEnd
            )
        )

        println(actual)
        println(actual.print())

        assertEquals(
            RootNode(
                BodyNode(
                    listOf(
                        ParagraphNode(
                            listOf(
                                UrlNode(
                                    UrlUrlNode("https://example.com"),
                                    UrlNameNode("alt"), UrlTitleNode("example")
                                )

                            )
                        )
                    )
                )
            ), actual
        )
    }

    @Test
    fun 複数段落() {
        val parser = Parser()

        val actual = parser.parse(
            listOf(
                Text("aiueo"), LineBreak(1), Text("abcd"), BlockBreak, Text("hoge")
            )
        )

        println(actual)
        println(actual.print())

        assertEquals(
            RootNode(
                BodyNode(
                    listOf(
                        ParagraphNode(
                            listOf(
                                PlainText("aiueo"),
                                PlainText(("abcd"))
                            )
                        ),
                        ParagraphNode(
                            listOf(
                                PlainText("hoge")
                            )
                        )
                    )
                )
            ), actual
        )
    }

    @Test
    fun createNest() {
        val quoteNode = QuoteNode(mutableListOf(PlainText("aa")))
        println(Parser.createNest(3, quoteNode))
        println(quoteNode)
    }

    @Test
    fun createNest2() {
        val quoteNode = QuoteNode(mutableListOf(PlainText("aaa")))
        Parser.createNest2(3, quoteNode, quoteNode)
        println(quoteNode)
    }

    @Test
    fun quote() {
        val parser = Parser()

        val actual = parser.parse(
            listOf(
                Quote(1), Text("a")
            )
        )

        println(actual)
        println(actual.print())

        assertEquals(
            RootNode(
                BodyNode(
                    listOf(
                        QuoteNode(
                            mutableListOf(
                                PlainText("a")
                            )
                        )
                    )
                )
            ), actual
        )
    }

    @Test
    fun quote2() {
        val parser = Parser()

        val actual = parser.parse(
            listOf(
                Quote(2), Text("a")
            )
        )

        println(actual)
        println(actual.print())

        assertEquals(
            RootNode(
                BodyNode(
                    listOf(
                        QuoteNode(
                            mutableListOf(
                                QuoteNode(
                                    mutableListOf(
                                        PlainText("a")
                                    )
                                )
                            )
                        )
                    )
                )
            ), actual
        )
    }

    @Test
    fun quote3() {
        val parser = Parser()

        val actual = parser.parse(
            listOf(
                Quote(2), Text("a"), InQuoteBreak, Quote(2), Text("abcd")
            )
        )

        println(actual)
        println(actual.print())

        assertEquals(
            RootNode(
                BodyNode(
                    listOf(
                        QuoteNode(
                            mutableListOf(
                                QuoteNode(
                                    mutableListOf(
                                        PlainText("a"), BreakNode, PlainText("abcd")
                                    )
                                )
                            )
                        )
                    )
                )
            ), actual
        )
    }

    @Test
    fun quote4() {
        val parser = Parser()

        val actual = parser.parse(
            listOf(
                Quote(1),
                Text("aiueo"),
                InQuoteBreak,
                Quote(2),
                Text(">abcd"),
                InQuoteBreak,
                Quote(1),
                Text("hoge"),
                InQuoteBreak,
                Text("fuga")
            )
        )

        println(actual)
        println(actual.print())

        assertEquals(
            RootNode(
                BodyNode(
                    listOf(
                        QuoteNode(
                            mutableListOf(
                                PlainText("aiueo"),
                                BreakNode,
                                QuoteNode(
                                    mutableListOf(
                                        PlainText(">abcd"),
                                        BreakNode
                                    )
                                ),
                                PlainText("hoge"),
                                BreakNode
                            )
                        ),
                        ParagraphNode(
                            mutableListOf(
                                PlainText("fuga")
                            )
                        )

                    )
                )
            ), actual
        )
    }

    @Test
    fun quoteと装飾() {

        val parser = Parser()

        val actual = parser.parse(
            listOf(
                Quote(1), Asterisk(1, '*'), Text("a"), Asterisk(1, '*')
            )
        )

        println(actual)
        println(actual.print())

        assertEquals(
            RootNode(
                BodyNode(
                    listOf(
                        QuoteNode(
                            mutableListOf(
                                ItalicNode(
                                    mutableListOf(PlainText("a"))
                                )

                            )
                        )
                    )
                )
            ), actual
        )

    }

    @Test
    fun list() {
        val parser = Parser()

        val actual = parser.parse(
            listOf(DiscList, Text("aiueo"), Whitespace(1, ' '), Text("aa"), LineBreak(1), DiscList, Text("abcd"))
        )

        println(actual)
        println(actual.print())

        assertEquals(
            RootNode(
                BodyNode(
                    listOf(
                        DiscListNode(
                            listOf(
                                ListItemNode(
                                    mutableListOf(
                                        PlainText("aiueo"),
                                        PlainText(" "),
                                        PlainText("aa")
                                    )
                                ),
                                ListItemNode(
                                    mutableListOf(
                                        PlainText("abcd")
                                    )
                                )
                            )

                        )
                    )
                )
            ), actual
        )
    }

    @Test
    fun listネスト() {
        val parser = Parser()

        val actual = parser.parse(
            listOf(
                DiscList,
                Text("aiueo"),
                LineBreak(1),
                Whitespace(4, ' '),
                DiscList,
                Text("abcd"),
                LineBreak(1),
                Whitespace(4, ' '),
                DiscList,
                Text("efgh"),
                LineBreak(1),
                DiscList,
                Text("hoge")
            )
        )

        println(actual)
        println(actual.print())

        assertEquals(
            RootNode(
                BodyNode(
                    DiscListNode(
                        ListItemNode(
                            PlainText("aiueo"),
                            DiscListNode(
                                ListItemNode(
                                    PlainText("abcd"),
                                ),
                                ListItemNode(
                                    PlainText("efgh"),
                                )
                            )
                        ),
                        ListItemNode(
                            PlainText("hoge")
                        )
                    )
                )
            ), actual
        )
    }

    @Test
    fun 異種listネスト() {
        val parser = Parser()

        val actual = parser.parse(
            listOf(
                DiscList,
                Text("aiueo"),
                LineBreak(1),
                Whitespace(4, ' '),
                DecimalList('1'),
                Text("abcd"),
                LineBreak(1),
                Whitespace(4, ' '),
                DecimalList('1'),
                Text("efgh"),
                LineBreak(1),
                DiscList,
                Text("hoge")
            )
        )

        println(actual)
        println(actual.print())

        assertEquals(
            RootNode(
                BodyNode(
                    DiscListNode(
                        ListItemNode(
                            PlainText("aiueo"),
                            DecimalListNode(
                                ListItemNode(
                                    PlainText("abcd"),
                                ),
                                ListItemNode(
                                    PlainText("efgh"),
                                )
                            )
                        ),
                        ListItemNode(
                            PlainText("hoge")
                        )
                    )
                )
            ), actual
        )
    }

    @Test
    fun 異種list() {
        val parser = Parser()

        val actual = parser.parse(
            listOf(
                DiscList,
                Text("aiueo"),
                LineBreak(1),
                DiscList,
                Text("abcd"),
                LineBreak(1),
                DecimalList('1'),
                Text("efgh"),
                LineBreak(1),
                DecimalList('1'),
                Text("hoge")
            )
        )

        println(actual)
        println(actual.print())

        assertEquals(
            RootNode(
                BodyNode(
                    DiscListNode(
                        ListItemNode(PlainText("aiueo")),
                        ListItemNode(PlainText("abcd"))
                    ),
                    DecimalListNode(
                        ListItemNode(PlainText("efgh")),
                        ListItemNode(PlainText("hoge"))
                    )
                )
            ), actual
        )
    }
}