package dev.usbharu.markdown

import dev.usbharu.markdown.AstNode.*
import dev.usbharu.markdown.Token.*
import kotlin.test.Test
import kotlin.test.assertEquals

class ParserTest {
    @Test
    fun header() {
        val parser = Parser()

        val actual = parser.parse(listOf(Header(1, 0, 0), Text("a b c", 0, 0)))

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

        val actual = parser.parse(
            listOf(
                Header(1, 0, 0),
                Text("a b c", 0, 0),
                LineBreak(1, 0, 0),
                Header(2, 0, 0),
                Text("d e f", 0, 0)
            )
        )

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

        val actual = parser.parse(
            listOf(
                Asterisk(1, '*', 0, 0),
                Text("a", 0, 0),
                Asterisk(1, '*', 0, 0)
            )
        )

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
            Asterisk(1, '*', 0, 0),
            PeekableTokenIterator(listOf(Text("a", 0, 0), Asterisk(1, '*', 0, 0)))
        )

        println(actual)
        println(actual.print())
    }

    @Test
    fun bold() {
        val parser = Parser()

        val actual = parser.parse(
            listOf(
                Asterisk(2, '*', 0, 0),
                Text("a", 0, 0),
                Asterisk(2, '*', 0, 0)
            )
        )

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

        val actual = parser.parse(
            listOf(
                Asterisk(3, '*', 0, 0),
                Text("a", 0, 0),
                Asterisk(3, '*', 0, 0)
            )
        )

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
                Asterisk(3, '*', 0, 0),
                Text("a", 0, 0),
                Asterisk(2, '*', 0, 0),
                Text("b", 0, 0),
                Asterisk(1, '*', 0, 0)
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
                                BoldNode(mutableListOf(PlainText("a"))),
                                PlainText("b"),
                                PlainText("*")
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
                Asterisk(3, '*', 0, 0),
                Text("a", 0, 0),
                Asterisk(1, '*', 0, 0),
                Text("b", 0, 0),
                Asterisk(2, '*', 0, 0)
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
                                ItalicNode(mutableListOf(PlainText("a"))),
                                PlainText("b"),
                                PlainText("**")
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
                Text("hello", 0, 0)
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

        val actual = parser.parse(listOf(Separator(3, '-', 0, 0)))

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

        val actual = parser.parse(
            listOf(
                Separator(3, '-', 0, 0),
                LineBreak(1, 0, 0),
                Separator(3, '-', 0, 0)
            )
        )

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

        val actual = parser.parse(
            listOf(
                Asterisk(2, '*', 0, 0),
                Text("a", 0, 0),
                Asterisk(1, '*', 0, 0)
            )
        )

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
    fun url() {
        val parser = Parser()

        val actual = parser.parse(
            listOf(
                SquareBracketStart(0, 0),
                Text("alt", 0, 0),
                SquareBracketEnd(0, 0),
                ParenthesesStart(0, 0),
                Url("https://example.com", 0, 0),
                UrlTitle("example", 0, 0),
                ParenthesesEnd(0, 0)
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
                Text("aiueo", 0, 0),
                LineBreak(1, 0, 0),
                Text("abcd", 0, 0),
                BlockBreak(0, 0),
                Text("hoge", 0, 0)
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
                Quote(1, 0, 0), Text("a", 0, 0)
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
                Quote(2, 0, 0), Text("a", 0, 0)
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
                Quote(2, 0, 0),
                Text("a", 0, 0),
                InQuoteBreak(0, 0),
                Quote(2, 0, 0),
                Text("abcd", 0, 0)
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
                Quote(1, 0, 0),
                Text("aiueo", 0, 0),
                InQuoteBreak(0, 0),
                Quote(2, 0, 0),
                Text(">abcd", 0, 0),
                InQuoteBreak(0, 0),
                Quote(1, 0, 0),
                Text("hoge", 0, 0),
                InQuoteBreak(0, 0),
                Text("fuga", 0, 0)
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
                            listOf(
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
                Quote(1, 0, 0),
                Asterisk(1, '*', 0, 0),
                Text("a", 0, 0),
                Asterisk(1, '*', 0, 0)
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
            listOf(
                DiscList(0, 0),
                Text("aiueo", 0, 0),
                Whitespace(1, ' ', 0, 0),
                Text("aa", 0, 0),
                LineBreak(1, 0, 0),
                DiscList(0, 0),
                Text("abcd", 0, 0)
            )
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
                DiscList(0, 0),
                Text("aiueo", 0, 0),
                LineBreak(1, 0, 0),
                Whitespace(4, ' ', 0, 0),
                DiscList(0, 0),
                Text("abcd", 0, 0),
                LineBreak(1, 0, 0),
                Whitespace(4, ' ', 0, 0),
                DiscList(0, 0),
                Text("efgh", 0, 0),
                LineBreak(1, 0, 0),
                DiscList(0, 0),
                Text("hoge", 0, 0)
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
                DiscList(0, 0),
                Text("aiueo", 0, 0),
                LineBreak(1, 0, 0),
                Whitespace(4, ' ', 0, 0),
                DecimalList('1', 0, 0),
                Text("abcd", 0, 0),
                LineBreak(1, 0, 0),
                Whitespace(4, ' ', 0, 0),
                DecimalList('1', 0, 0),
                Text("efgh", 0, 0),
                LineBreak(1, 0, 0),
                DiscList(0, 0),
                Text("hoge", 0, 0)
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
    fun strike() {
        val parser = Parser()

        val actual = parser.parse(listOf(Strike("a", 0, 0)))

        println(actual)
        println(actual.print())

        assertEquals(
            RootNode(
                BodyNode(
                    listOf(
                        ParagraphNode(listOf(StrikeThroughNode(listOf(PlainText("a")))))
                    )
                )
            ), actual
        )
    }

    @Test
    fun アスタリスク閉じ無し() {
        val parser = Parser()

        val actual = parser.parse(listOf(Asterisk(1, '*', 0, 0), Text("a", 0, 0)))

        println(actual)
        println(actual.print())

        assertEquals(
            RootNode(
                BodyNode(
                    listOf(
                        ParagraphNode(listOf(PlainText("*"), PlainText("a")))
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
                DiscList(0, 0),
                Text("aiueo", 0, 0),
                LineBreak(1, 0, 0),
                DiscList(0, 0),
                Text("abcd", 0, 0),
                LineBreak(1, 0, 0),
                DecimalList('1', 0, 0),
                Text("efgh", 0, 0),
                LineBreak(1, 0, 0),
                DecimalList('1', 0, 0),
                Text("hoge", 0, 0)
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
