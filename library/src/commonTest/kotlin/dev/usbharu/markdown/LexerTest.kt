package dev.usbharu.markdown

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

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

        assertContentEquals(listOf(Text("abcd"), Break(1), Text("efg"), Text("h")), actual)
    }

    @Test
    fun プレーンテキストと複数の改行() {
        val lexer = Lexer()

        val actual = lexer.lex("abcd\n\nefgh")

        println(actual)

        assertContentEquals(listOf(Text("abcd"), Break(2), Text("efg"), Text("h")), actual)
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

        assertContentEquals(listOf(Text("---aiueo")), actual)
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

        assertContentEquals(listOf(DiscList, Text("[xa"), Whitespace(1, ' '), Text("a")), actual)
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

        assertContentEquals(listOf(Text("-aiueo")), actual)
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

    @Test
    fun url() {
        val lexer = Lexer()

        val actual = lexer.lex("https://example.com")

        println(actual)

        assertContentEquals(listOf(Url("https://example.com")), actual)
    }

    @Test
    fun url2() {
        val lexer = Lexer()

        val actual =
            lexer.lex("https://ja.wikipedia.org/wiki/%E3%83%A4%E3%83%B3%E3%83%90%E3%83%AB%E3%82%AF%E3%82%A4%E3%83%8A#%E6%8E%A1%E9%A4%8C")

        println(actual)

        assertContentEquals(
            listOf(Url("https://ja.wikipedia.org/wiki/%E3%83%A4%E3%83%B3%E3%83%90%E3%83%AB%E3%82%AF%E3%82%A4%E3%83%8A#%E6%8E%A1%E9%A4%8C")),
            actual
        )
    }

    @Test
    fun 文中にurl() {
        val lexer = Lexer()

        val actual = lexer.lex("こんにちは～ https://example.com\nあいうえお")

        println(actual)

        assertContentEquals(
            listOf(
                Text("こんにちは～"),
                Whitespace(1, ' '),
                Url("https://example.com"),
                Break(1),
                Text("あいうえお")
            ), actual
        )
    }

    @Test
    fun urlかと思ったら違った() {
        val lexer = Lexer()

        val actual = lexer.lex("httppppp")

        println(actual)

        assertContentEquals(
            listOf(
                Text("httppppp")
            ), actual
        )
    }

    @Test
    fun urlかと思ったら違った2() {
        val lexer = Lexer()

        val actual = lexer.lex("ha")

        println(actual)

        assertContentEquals(
            listOf(
                Text("ha")
            ), actual
        )
    }

    @Test
    fun アスタリスク() {
        val lexer = Lexer()

        val actual = lexer.lex("*a*")

        println(actual)

        assertContentEquals(
            listOf(
                Asterisk(1, '*'),
                Text("a"),
                Asterisk(1, '*')
            ), actual
        )
    }

    @Test
    fun アスタリスク2() {
        val lexer = Lexer()

        val actual = lexer.lex("> *a*")

        println(actual)

        assertContentEquals(
            listOf(
                Quote(1),
                Asterisk(1, '*'),
                Text("a"),
                Asterisk(1, '*')
            ), actual
        )
    }

    @Test
    fun アスタリスク複数() {
        val lexer = Lexer()

        val actual = lexer.lex("**a**")

        println(actual)

        assertContentEquals(
            listOf(
                Asterisk(2, '*'),
                Text("a"),
                Asterisk(2, '*')
            ), actual
        )
    }

    @Test
    fun アンダーバー() {
        val lexer = Lexer()

        val actual = lexer.lex("__a__")

        println(actual)

        assertContentEquals(
            listOf(
                Asterisk(2, '_'),
                Text("a"),
                Asterisk(2, '_')
            ), actual
        )
    }

    @Test
    fun urlかと思ったらアスタリスク() {
        val lexer = Lexer()

        val actual = lexer.lex("h*a*")

        println(actual)

        assertContentEquals(
            listOf(
                Text("h"), Asterisk(1, '*'), Text("a"), Asterisk(1, '*')
            ), actual
        )
    }

    @Test
    fun 画像() {
        val lexer = Lexer()

        val actual = lexer.lex("![alt](https://example.com)")

        println(actual)

        assertContentEquals(
            listOf(
                Exclamation,
                SquareBracketStart,
                Text("alt"),
                SquareBracketEnd,
                ParenthesesStart,
                Url("https://example.com"),
                ParenthesesEnd
            ), actual
        )
    }

    @Test
    fun url3() {
        val lexer = Lexer()

        val actual = lexer.lex("[alt](https://example.com)")

        println(actual)

        assertContentEquals(
            listOf(
                SquareBracketStart,
                Text("alt"),
                SquareBracketEnd,
                ParenthesesStart,
                Url("https://example.com"),
                ParenthesesEnd
            ), actual
        )
    }

    @Test
    fun urlとタイトル() {
        val lexer = Lexer()

        val actual = lexer.lex("[alt](https://example.com \"example\")")

        println(actual)

        assertContentEquals(
            listOf(
                SquareBracketStart,
                Text("alt"),
                SquareBracketEnd,
                ParenthesesStart,
                Url("https://example.com"),
                UrlTitle("example"),
                ParenthesesEnd
            ), actual
        )
    }

    @Test
    fun urlとタイトル全角() {
        val lexer = Lexer()

        val actual = lexer.lex("[alt](https://example.com \"example)")

        println(actual)

        assertContentEquals(
            listOf(
                SquareBracketStart,
                Text("alt"),
                SquareBracketEnd,
                ParenthesesStart,
                Url("https://example.com"),
                UrlTitle("example"),
                ParenthesesEnd
            ), actual
        )
    }

    @Test
    fun インラインコードブロック() {
        val lexer = Lexer()

        val actual = lexer.lex("`code`")

        println(actual)

        assertContentEquals(
            listOf(
                InlineCodeBlock("code"),
            ), actual
        )
    }

    @Test
    fun インラインコードブロック2() {
        val lexer = Lexer()

        val actual = lexer.lex("aiueo`code`abcd")

        println(actual)

        assertContentEquals(
            listOf(
                Text("aiueo"),
                InlineCodeBlock("code"),
                Text("abcd"),
            ), actual
        )
    }


    @Test
    fun コードブロック() {
        val lexer = Lexer()

        val actual = lexer.lex(
            """```
            |code
            |```
        """.trimMargin()
        )

        println(actual)

        assertContentEquals(
            listOf(
                CodeBlock("code"),
            ), actual
        )
    }

    @Test
    fun 言語指定付きコードブロック() {
        val lexer = Lexer()

        val actual = lexer.lex(
            """```hoge
            |code
            |```
        """.trimMargin()
        )

        println(actual)

        assertContentEquals(
            listOf(
                CodeBlockLanguage("hoge", ""),
                CodeBlock("code"),
            ), actual
        )
    }

    @Test
    fun ファイル名と言語コードブロック() {
        val lexer = Lexer()

        val actual = lexer.lex(
            """```hoge:fuga
            |code
            |```
        """.trimMargin()
        )

        println(actual)

        assertContentEquals(
            listOf(
                CodeBlockLanguage("hoge", "fuga"),
                CodeBlock("code"),
            ), actual
        )
    }

    @Test
    fun コードブロックかと思ったら違った() {
        val lexer = Lexer()

        val actual = lexer.lex("````aiueo")

        println(actual)

        assertContentEquals(
            listOf(
                Text("````aiueo")
            ), actual
        )
    }

    @Test
    fun 唐突のヘッダー() {
        val lexer = Lexer()

        val actual = lexer.lex("aiueo #a")

        println(actual)

        assertContentEquals(
            listOf(
                Text("aiueo"), Whitespace(1, ' '), Text("#a")
            ), actual
        )
    }

    @Test
    fun 唐突のリスト() {
        val lexer = Lexer()

        val actual = lexer.lex("aiueo - a")

        println(actual)

        assertContentEquals(
            listOf(
                Text("aiueo"), Whitespace(1, ' '), Text("-"), Whitespace(1, ' '), Text("a")
            ), actual
        )
    }

    @Test
    fun 唐突のコードブロック() {
        val lexer = Lexer()

        val actual = lexer.lex("aiueo ```abcd")

        println(actual)

        assertContentEquals(
            listOf(
                Text("aiueo"), Whitespace(1, ' '), Text("```abcd")
            ), actual
        )
    }

    @Test
    fun コードブロックかと思ったらアスタリスク() {
        val lexer = Lexer()

        val actual = lexer.lex("aiueo ```abcd*a*")

        println(actual)

        assertContentEquals(
            listOf(
                Text("aiueo"),
                Whitespace(1, ' '),
                Text("```abcd"),
                Asterisk(1, '*'),
                Text("a"),
                Asterisk(1, '*')
            ), actual
        )
    }

    @Test
    fun peekStringTest() {
        val lexer = Lexer()
        val iterator = PeekableCharIterator("*a**a***".toCharArray())
        val peekString = lexer.peekString(iterator, '*', '*', '*')

        println(peekString)

        assertEquals("*a**a", peekString)
    }

    @Test
    fun 打ち消し線() {
        val lexer = Lexer()

        val actual = lexer.lex("~~aiueo~~")

        println(actual)

        assertContentEquals(
            listOf(
                Strike("aiueo")
            ), actual
        )
    }

    @Test
    fun 打ち消し線2() {
        val lexer = Lexer()

        val actual = lexer.lex("aiueo ~~aiueo~~bcde")

        println(actual)

        assertContentEquals(
            listOf(
                Text("aiueo"),
                Whitespace(1, ' '),
                Strike("aiueo"),
                Text("bcde")
            ), actual
        )
    }

    @Test
    fun 打ち消し線かと思ったら違った() {
        val lexer = Lexer()

        val actual = lexer.lex("aiueo~~abcd")

        println(actual)

        assertContentEquals(
            listOf(
                Text("aiueo~~abcd")
            ), actual
        )
    }

    @Test
    fun 打ち消し線かと思ったら違った2() {
        val lexer = Lexer()

        val actual = lexer.lex("aiueo~abcd")

        println(actual)

        assertContentEquals(
            listOf(
                Text("aiueo~abcd")
            ), actual
        )
    }
}