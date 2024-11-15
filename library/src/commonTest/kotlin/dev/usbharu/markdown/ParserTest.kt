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

        val actual = parser.parse(listOf(Header(1), Text("a b c"), Break(1), Header(2), Text("d e f")))

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

        val actual = parser.parse(listOf(Separator(3, '-'), Break(1), Separator(3, '-')))

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
}