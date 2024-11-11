package dev.usbharu.markdown

import kotlin.test.Test
import kotlin.test.assertContentEquals

class LexerTest {

    @Test
    fun `改行はBreak`() {
        val lexer = Lexer()

        val actual = lexer.lex("\n")

        println(actual)

        assertContentEquals(listOf(Break(1)), actual)
    }

    @Test
    fun 複数の改行() {
        val lexer = Lexer()

        val actual = lexer.lex("\n\n")

        println(actual)

        assertContentEquals(listOf(Break(2)), actual)
    }

    @Test
    fun プレーンテキスト() {
        val lexer = Lexer()

        val actual = lexer.lex("abcd")

        println(actual)

        assertContentEquals(listOf(Text("abcd")), actual)
    }

    @Test
    fun プレーンテキストと改行() {
        val lexer = Lexer()

        val actual = lexer.lex("abcd\nefgh")

        println(actual)

        assertContentEquals(listOf(Text("abcd"), Break(1), Text("efgh")), actual)
    }

    @Test
    fun プレーンテキストと複数の改行() {
        val lexer = Lexer()

        val actual = lexer.lex("abcd\n\nefgh")

        println(actual)

        assertContentEquals(listOf(Text("abcd"), Break(2), Text("efgh")), actual)
    }

    @Test
    fun ヘッダー() {
        val lexer = Lexer()

        val actual = lexer.lex("# abcd efgh")

        println(actual)

        assertContentEquals(listOf(Header(1), Text("abcd efgh")), actual)
    }

    @Test
    fun ヘッダー2() {
        val lexer = Lexer()

        val actual = lexer.lex("## abcd efgh")

        println(actual)

        assertContentEquals(listOf(Header(2), Text("abcd efgh")), actual)
    }

    @Test
    fun `ヘッダーの中にヘッダー`() {
        val lexer = Lexer()

        val actual = lexer.lex("# #a")

        println(actual)

        assertContentEquals(listOf(Header(1), Text("#a")), actual)
    }

    @Test
    fun 引用() {
        val lexer = Lexer()

        val actual = lexer.lex("> a")

        println(actual)

        assertContentEquals(listOf(Quote(1), Text("a")), actual)
    }

    @Test
    fun 引用のネスト() {
        val lexer = Lexer()

        val actual = lexer.lex(">> abcd")

        println(actual)

        assertContentEquals(listOf(Quote(2), Text("abcd")), actual)
    }

    @Test
    fun 引用の中に引用() {
        val lexer = Lexer()

        val actual = lexer.lex(">> >abcd")

        println(actual)

        assertContentEquals(listOf(Quote(2), Text(">abcd")), actual)
    }
}