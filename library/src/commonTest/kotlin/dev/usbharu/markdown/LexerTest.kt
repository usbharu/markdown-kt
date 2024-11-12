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
    fun 改行2() {
        val lexer = Lexer()

        val actual = lexer.lex("\r\n")

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
    fun ヘッダー後の空白は無視() {

        val lexer = Lexer()

        val actual = lexer.lex("##       abcd efgh")

        println(actual)

        assertContentEquals(listOf(Header(2), Text("abcd efgh")), actual)
    }

    @Test
    fun 全角ヘッダー() {
        val lexer = Lexer()

        val actual = lexer.lex("＃ abcd efgh")

        println(actual)

        assertContentEquals(listOf(Header(1), Text("abcd efgh")), actual)
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

    @Test
    fun 全角引用() {
        val lexer = Lexer()

        val actual = lexer.lex("＞ >abcd")

        println(actual)

        assertContentEquals(listOf(Quote(1), Text(">abcd")), actual)
    }

    @Test
    fun 引用後の空白は無視() {
        val lexer = Lexer()

        val actual = lexer.lex(">>        >abcd")

        println(actual)

        assertContentEquals(listOf(Quote(2), Text(">abcd")), actual)
    }

    @Test
    fun セパレーター() {
        val lexer = Lexer()

        val actual = lexer.lex("---")

        println(actual)

        assertContentEquals(listOf(Separator(3, '-')), actual)
    }

    @Test
    fun セパレーター2() {
        val lexer = Lexer()

        val actual = lexer.lex("===")

        println(actual)

        assertContentEquals(listOf(Separator(3, '=')), actual)
    }

    @Test
    fun セパレーター混在() {
        val lexer = Lexer()

        val actual = lexer.lex("-=-")

        println(actual)

        assertContentEquals(listOf(Text("-=-")), actual)
    }

    @Test
    fun セパレーターかと思ったら本文だった() {
        val lexer = Lexer()

        val actual = lexer.lex("---aiueo")

        println(actual)

        assertContentEquals(listOf(Text("---"), Text("aiueo")), actual)
    }

    @Test
    fun チェックボックス() {
        val lexer = Lexer()

        val actual = lexer.lex("- [x] a")

        println(actual)

        assertContentEquals(listOf(DiscList, CheckBox(true), Text("a")), actual)
    }

    @Test
    fun チェックボックス2() {
        val lexer = Lexer()

        val actual = lexer.lex("- [ ] a")

        println(actual)

        assertContentEquals(listOf(DiscList, CheckBox(false), Text("a")), actual)
    }

    @Test
    fun チェックボックスかと思ったら違った() {
        val lexer = Lexer()

        val actual = lexer.lex("- [xa a")

        println(actual)

        assertContentEquals(listOf(DiscList, Text("[x"), Text("a a")), actual)
    }

    @Test
    fun チェックボックスかと思ったら違った2() {
        val lexer = Lexer()

        val actual = lexer.lex("- [a a")

        println(actual)

        assertContentEquals(listOf(DiscList, Text("[a"), Whitespace(1, ' '), Text("a")), actual)
    }

    @Test
    fun チェックボックスかと思ったら違った3() {
        val lexer = Lexer()

        val actual = lexer.lex("-aiueo")

        println(actual)

        assertContentEquals(listOf(Text("-"), Text("aiueo")), actual)
    }

    @Test
    fun チェックボックスいっぱい() {
        val lexer = Lexer()

        val actual = lexer.lex("- [ ] a\n- [x] b\n- [ ] c\n- [x] d")

        println(actual)

        assertContentEquals(
            listOf(
                DiscList,
                CheckBox(false),
                Text("a"),
                Break(1),
                DiscList,
                CheckBox(true),
                Text("b"),
                Break(1),
                DiscList,
                CheckBox(false),
                Text("c"),
                Break(1),
                DiscList,
                CheckBox(true),
                Text("d"),
            ), actual
        )
    }

    @Test
    fun ディスクリスト() {
        val lexer = Lexer()

        val actual = lexer.lex("- aiueo")

        println(actual)

        assertContentEquals(listOf(DiscList, Text("aiueo")), actual)
    }

    @Test
    fun ディスクリストいっぱい() {
        val lexer = Lexer()

        val actual = lexer.lex("- aiueo\n- abcd")

        println(actual)

        assertContentEquals(listOf(DiscList, Text("aiueo"), Break(1), DiscList, Text("abcd")), actual)
    }

    @Test
    fun ディスクリストネスト() {
        val lexer = Lexer()

        val actual = lexer.lex("- aiueo\n    - abcd")

        println(actual)

        assertContentEquals(
            listOf(DiscList, Text("aiueo"), Break(1), Whitespace(4, ' '), DiscList, Text("abcd")),
            actual
        )
    }

    @Test
    fun 数字リスト() {
        val lexer = Lexer()

        val actual = lexer.lex("1. aiueo\n    2. abcd")

        println(actual)

        assertContentEquals(
            listOf(
                DecimalList('1'),
                Text("aiueo"),
                Break(1),
                Whitespace(4, ' '),
                DecimalList('2'),
                Text("abcd")
            ),
            actual
        )
    }

    @Test
    fun 全角数字リスト() {
        val lexer = Lexer()

        val actual = lexer.lex("１. aiueo\n    ２. abcd")

        println(actual)

        assertContentEquals(
            listOf(
                DecimalList('１'),
                Text("aiueo"),
                Break(1),
                Whitespace(4, ' '),
                DecimalList('２'),
                Text("abcd")
            ),
            actual
        )
    }

    @Test
    fun 全角コンマリスト() {
        val lexer = Lexer()

        val actual = lexer.lex("1。 aiueo\n    2、 abcd")

        println(actual)

        assertContentEquals(
            listOf(
                DecimalList('1'),
                Text("aiueo"),
                Break(1),
                Whitespace(4, ' '),
                DecimalList('2'),
                Text("abcd")
            ),
            actual
        )
    }
}