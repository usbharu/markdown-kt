package dev.usbharu.markdown

import dev.usbharu.markdown.Token.*
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class LexerTest {

    @Test
    fun `改行はBreak`() {
        val lexer = Lexer()

        val actual = lexer.lex("\n")

        println(actual)

        assertContentEquals(listOf(LineBreak(1, 0, 0)), actual)
    }

    @Test
    fun 改行2() {
        val lexer = Lexer()

        val actual = lexer.lex("\r\n")

        println(actual)

        assertContentEquals(listOf(LineBreak(1, 0, 0)), actual)
    }

    @Test
    fun 複数の改行() {
        val lexer = Lexer()

        val actual = lexer.lex("\n\n")

        println(actual)

        assertContentEquals(listOf(BlockBreak(0, 0)), actual)
    }

    @Test
    fun プレーンテキスト() {
        val lexer = Lexer()

        val actual = lexer.lex("abcd")

        println(actual)

        assertContentEquals(listOf(Text("abcd", 0, 0)), actual)
    }

    @Test
    fun プレーンテキスト2() {
        val lexer = Lexer()

        val actual = lexer.lex("abcd aiueo")

        println(actual)

        assertContentEquals(listOf(Text("abcd", 0, 0), Whitespace(1, ' ', 0, 4), Text("aiueo", 0, 5)), actual)
    }

    @Test
    fun プレーンテキストと改行() {
        val lexer = Lexer()

        val actual = lexer.lex("abcd\nefgh")

        println(actual)

        assertContentEquals(listOf(Text("abcd", 0, 0), LineBreak(1, 0, 4), Text("efgh", 1, 0)), actual)
    }

    @Test
    fun プレーンテキストと複数の改行() {
        val lexer = Lexer()

        val actual = lexer.lex("abcd\n\nefgh")

        println(actual)

        assertContentEquals(listOf(Text("abcd", 0, 0), BlockBreak(0, 4), Text("efgh", 2, 0)), actual)
    }

    @Test
    fun ヘッダー() {
        val lexer = Lexer()

        val actual = lexer.lex("# abcd efgh")

        println(actual)

        assertContentEquals(listOf(Header(1, 0, 0), Text("abcd efgh", 0, 2)), actual)
    }

    @Test
    fun ヘッダー2() {
        val lexer = Lexer()

        val actual = lexer.lex("## abcd efgh")

        println(actual)

        assertContentEquals(listOf(Header(2, 0, 0), Text("abcd efgh", 0, 3)), actual)
    }

    @Test
    fun ヘッダー後の空白は無視() {

        val lexer = Lexer()

        val actual = lexer.lex("##       abcd efgh")

        println(actual)

        assertContentEquals(listOf(Header(2, 0, 0), Text("abcd efgh", 0, 9)), actual)
    }

    @Test
    fun 全角ヘッダー() {
        val lexer = Lexer()

        val actual = lexer.lex("＃ abcd efgh")

        println(actual)

        assertContentEquals(listOf(Header(1, 0, 0), Text("abcd efgh", 0, 2)), actual)
    }

    @Test
    fun `ヘッダーの中にヘッダー`() {
        val lexer = Lexer()

        val actual = lexer.lex("# #a")

        println(actual)

        assertContentEquals(listOf(Header(1, 0, 0), Text("#a", 0, 2)), actual)
    }

    //
    @Test
    fun ヘッダー後の改行() {
        val lexer = Lexer()

        val actual = lexer.lex("# a\n# b")

        println(actual)

        assertContentEquals(
            listOf(
                Header(1, 0, 0), Text("a", 0, 2), LineBreak(1, 0, 3), Header(1, 1, 0), Text("b", 1, 2)
            ), actual
        )
    }

    //
    @Test
    fun ヘッダー複数() {
        val lexer = Lexer()

        val actual = lexer.lex("# a a a")

        println(actual)

        assertContentEquals(
            listOf(
                Header(1, 0, 0),
                Text("a a a", 0, 2),
            ), actual
        )
    }

    //
    @Test
    fun 引用() {
        val lexer = Lexer()

        val actual = lexer.lex("> a")

        println(actual)

        assertContentEquals(listOf(Quote(1, 0, 0), Text("a", 0, 2)), actual)
    }

    //
    @Test
    fun 引用のネスト() {
        val lexer = Lexer()

        val actual = lexer.lex(">> abcd")

        println(actual)

        assertContentEquals(listOf(Quote(2, 0, 0), Text("abcd", 0, 3)), actual)
    }

    //
    @Test
    fun 引用の中に引用() {
        val lexer = Lexer()

        val actual = lexer.lex(">> >abcd")

        println(actual)

        assertContentEquals(listOf(Quote(2, 0, 0), Text(">abcd", 0, 3)), actual)
    }

    //
    @Test
    fun 全角引用() {
        val lexer = Lexer()

        val actual = lexer.lex("＞ >abcd")

        println(actual)

        assertContentEquals(listOf(Quote(1, 0, 0), Text(">abcd", 0, 2)), actual)
    }

    //
    @Test
    fun 引用後の空白は無視() {
        val lexer = Lexer()

        val actual = lexer.lex(">>        >abcd")

        println(actual)

        assertContentEquals(listOf(Quote(2, 0, 0), Text(">abcd", 0, 10)), actual)
    }

    //
    @Test
    fun 引用複数行() {
        val lexer = Lexer()

        val actual = lexer.lex("> aiueo\n>> >abcd\n> hoge\nfuga")

        println(actual)

        assertContentEquals(
            listOf(
                Quote(1, 0, 0),
                Text("aiueo", 0, 2),
                InQuoteBreak(0, 7),
                Quote(2, 1, 0),
                Text(">abcd", 1, 3),
                InQuoteBreak(1, 8),
                Quote(1, 2, 0),
                Text("hoge", 2, 2),
                InQuoteBreak(2, 6),
                Text("fuga", 3, 0)
            ), actual
        )
    }

    //
    @Test
    fun セパレーター() {
        val lexer = Lexer()

        val actual = lexer.lex("---")

        println(actual)

        assertContentEquals(listOf(Separator(3, '-', 0, 0)), actual)
    }

    //
    @Test
    fun セパレーター2() {
        val lexer = Lexer()

        val actual = lexer.lex("===")

        println(actual)

        assertContentEquals(listOf(Separator(3, '=', 0, 0)), actual)
    }

    //
    @Test
    fun セパレーター混在() {
        val lexer = Lexer()

        val actual = lexer.lex("-=-")

        println(actual)

        assertContentEquals(listOf(Text("-=-", 0, 0)), actual)
    }

    @Test
    fun セパレーターかと思ったら本文だった() {
        val lexer = Lexer()

        val actual = lexer.lex("---aiueo")

        println(actual)

        assertContentEquals(listOf(Text("---aiueo", 0, 0)), actual)
    }

    //
    @Test
    fun チェックボックス() {
        val lexer = Lexer()

        val actual = lexer.lex("- [x] a")

        println(actual)

        assertContentEquals(listOf(DiscList(0, 0), CheckBox(true, 0, 2), Text("a", 0, 6)), actual)
    }

    //
    @Test
    fun チェックボックス2() {
        val lexer = Lexer()

        val actual = lexer.lex("- [ ] a")

        println(actual)

        assertContentEquals(
            listOf(
                DiscList(0, 0), CheckBox(false, 0, 2), Text("a", 0, 6)
            ), actual
        )
    }

    //
    @Test
    fun チェックボックスかと思ったら違った() {
        val lexer = Lexer()

        val actual = lexer.lex("- [xa a")

        println(actual)

        assertContentEquals(
            listOf(
                DiscList(0, 0), Text("[xa", 0, 2), Whitespace(1, ' ', 0, 5), Text("a", 0, 6)
            ), actual
        )
    }

    @Test
    fun チェックボックスかと思ったら違った2() {
        val lexer = Lexer()

        val actual = lexer.lex("- [a a")

        println(actual)

        assertContentEquals(
            listOf(
                DiscList(0, 0), Text("[a", 0, 2), Whitespace(1, ' ', 0, 4), Text("a", 0, 5)
            ), actual
        )
    }

    @Test
    fun チェックボックスかと思ったら違った3() {
        val lexer = Lexer()

        val actual = lexer.lex("-aiueo")

        println(actual)

        assertContentEquals(listOf(Text("-aiueo", 0, 0)), actual)
    }


    @Test
    fun チェックボックスいっぱい() {
        val lexer = Lexer()

        val actual = lexer.lex("- [ ] a\n- [x] b\n- [ ] c\n- [x] d")

        println(actual)

        assertContentEquals(
            listOf(
                DiscList(0, 0),
                CheckBox(false, 0, 2),
                Text("a", 0, 6),
                LineBreak(1, 0, 7),
                DiscList(1, 0),
                CheckBox(true, 1, 2),
                Text("b", 1, 6),
                LineBreak(1, 1, 7),
                DiscList(2, 0),
                CheckBox(false, 2, 2),
                Text("c", 2, 6),
                LineBreak(1, 2, 7),
                DiscList(3, 0),
                CheckBox(true, 3, 2),
                Text("d", 3, 6),
            ), actual
        )
    }

    //
    @Test
    fun ディスクリスト() {
        val lexer = Lexer()

        val actual = lexer.lex("- aiueo")

        println(actual)

        assertContentEquals(listOf(DiscList(0, 0), Text("aiueo", 0, 2)), actual)
    }

    @Test
    fun ディスクリストいっぱい() {
        val lexer = Lexer()

        val actual = lexer.lex("- aiueo\n- abcd")

        println(actual)

        assertContentEquals(
            listOf(
                DiscList(0, 0), Text("aiueo", 0, 2), LineBreak(1, 0, 7), DiscList(1, 0), Text("abcd", 1, 2)
            ), actual
        )
    }

    @Test
    fun ディスクリストネスト() {
        val lexer = Lexer()

        val actual = lexer.lex("- aiueo\n    - abcd")

        println(actual)

        assertContentEquals(
            listOf(
                DiscList(0, 0),
                Text("aiueo", 0, 2),
                LineBreak(1, 0, 7),
                Whitespace(4, ' ', 1, 0),
                DiscList(1, 4),
                Text("abcd", 1, 6)
            ), actual
        )
    }

    @Test
    fun 数字リスト() {
        val lexer = Lexer()

        val actual = lexer.lex("1. aiueo\n    2. abcd")

        println(actual)

        assertContentEquals(
            listOf(
                DecimalList('1', 0, 0),
                Text("aiueo", 0, 3),
                LineBreak(1, 0, 8),
                Whitespace(4, ' ', 1, 0),
                DecimalList('2', 1, 4),
                Text("abcd", 1, 7)
            ), actual
        )
    }

    @Test
    fun 全角数字リスト() {
        val lexer = Lexer()

        val actual = lexer.lex("１. aiueo\n    ２. abcd")

        println(actual)

        assertContentEquals(
            listOf(
                DecimalList('１', 0, 0),
                Text("aiueo", 0, 3),
                LineBreak(1, 0, 8),
                Whitespace(4, ' ', 1, 0),
                DecimalList('２', 1, 4),
                Text("abcd", 1, 7)
            ), actual
        )
    }


    @Test
    fun 全角コンマリスト() {
        val lexer = Lexer()

        val actual = lexer.lex("1。 aiueo\n    2、 abcd")

        println(actual)

        assertContentEquals(
            listOf(
                DecimalList('1', 0, 0),
                Text("aiueo", 0, 3),
                LineBreak(1, 0, 8),
                Whitespace(4, ' ', 1, 0),
                DecimalList('2', 1, 4),
                Text("abcd", 1, 7)
            ), actual
        )
    }

    @Test
    fun url() {
        val lexer = Lexer()

        val actual = lexer.lex("https://example.com")

        println(actual)

        assertContentEquals(listOf(Url("https://example.com", 0, 0)), actual)
    }

    @Test
    fun url2() {
        val lexer = Lexer()

        val actual =
            lexer.lex("https://ja.wikipedia.org/wiki/%E3%83%A4%E3%83%B3%E3%83%90%E3%83%AB%E3%82%AF%E3%82%A4%E3%83%8A#%E6%8E%A1%E9%A4%8C")

        println(actual)

        assertContentEquals(
            listOf(
                Url(
                    "https://ja.wikipedia.org/wiki/%E3%83%A4%E3%83%B3%E3%83%90%E3%83%AB%E3%82%AF%E3%82%A4%E3%83%8A#%E6%8E%A1%E9%A4%8C",
                    0,
                    0
                )
            ), actual
        )
    }

    @Test
    fun 文中にurl() {
        val lexer = Lexer()

        val actual = lexer.lex("こんにちは～ https://example.com\nあいうえお")

        println(actual)

        assertContentEquals(
            listOf(
                Text("こんにちは～", 0, 0),
                Whitespace(1, ' ', 0, 6),
                Url("https://example.com", 0, 7),
                LineBreak(1, 0, 26),
                Text("あいうえお", 1, 0)
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
                Text("httppppp", 0, 0)
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
                Text("ha", 0, 0)
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
                Asterisk(1, '*', 0, 0), Text("a", 0, 1), Asterisk(1, '*', 0, 2)
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
                Quote(1, 0, 0),
                Asterisk(1, '*', 0, 2),
                Text("a", 0, 3),
                Asterisk(1, '*', 0, 4)
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
                Asterisk(2, '*', 0, 0), Text("a", 0, 2), Asterisk(2, '*', 0, 3)
            ), actual
        )
    }

    @Test
    fun アスタリスク複数2() {
        val lexer = Lexer()

        val actual = lexer.lex("***a**b*")

        println(actual)

        assertContentEquals(
            listOf(
                Asterisk(3, '*', 0, 0),
                Text("a", 0, 3),
                Asterisk(2, '*', 0, 4),
                Text("b", 0, 6),
                Asterisk(1, '*', 0, 7),
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
                Asterisk(2, '_', 0, 0), Text("a", 0, 2), Asterisk(2, '_', 0, 3)
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
                Text("h", 0, 0), Asterisk(1, '*', 0, 1), Text("a", 0, 2), Asterisk(1, '*', 0, 3)
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
                Exclamation(0, 0),
                SquareBracketStart(0, 1),
                Text("alt", 0, 2),
                SquareBracketEnd(0, 5),
                ParenthesesStart(0, 6),
                Url("https://example.com", 0, 7),
                ParenthesesEnd(0, 26)
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
                SquareBracketStart(0, 0),
                Text("alt", 0, 1),
                SquareBracketEnd(0, 4),
                ParenthesesStart(0, 5),
                Url("https://example.com", 0, 6),
                ParenthesesEnd(0, 25)
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
                SquareBracketStart(0, 0),
                Text("alt", 0, 1),
                SquareBracketEnd(0, 4),
                ParenthesesStart(0, 5),
                Url("https://example.com", 0, 6),
                UrlTitle("example", 0, 26),
                ParenthesesEnd(0, 35)
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
                SquareBracketStart(0, 0),
                Text("alt", 0, 1),
                SquareBracketEnd(0, 4),
                ParenthesesStart(0, 5),
                Url("https://example.com", 0, 6),
                UrlTitle("example", 0, 26),
                ParenthesesEnd(0, 34)
            ), actual
        )
    }

    @Test
    fun 不正urlとタイトル() {
        val lexer = Lexer()

        val actual = lexer.lex("[alt](../hoge.html \"example\")")

        println(actual)

        assertContentEquals(
            listOf(
                SquareBracketStart(0, 0),
                Text("alt", 0, 1),
                SquareBracketEnd(0, 4),
                ParenthesesStart(0, 5),
                Url("https://example.com", 0, 6),
                UrlTitle("example", 0, 0),
                ParenthesesEnd(0, 0)
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
                InlineCodeBlock("code", 0, 0),
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
                Text("aiueo", 0, 0),
                InlineCodeBlock("code", 0, 5),
                Text("abcd", 0, 11),
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
                CodeBlock("code", 0, 0),
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
                CodeBlockLanguage("hoge", "", 0, 3),
                CodeBlock("code", 0, 0),
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
                CodeBlockLanguage("hoge", "fuga", 0, 3),
                CodeBlock("code", 0, 0),
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
                Text("````aiueo", 0, 0)
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
                Text("aiueo", 0, 0), Whitespace(1, ' ', 0, 5), Text("#a", 0, 6)
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
                Text("aiueo", 0, 0),
                Whitespace(1, ' ', 0, 5),
                Text("-", 0, 6),
                Whitespace(1, ' ', 0, 7),
                Text("a", 0, 8)
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
                Text("aiueo", 0, 0), Whitespace(1, ' ', 0, 5), Text("```abcd", 0, 6)
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
                Text("aiueo", 0, 0),
                Whitespace(1, ' ', 0, 5),
                Text("```abcd", 0, 6),
                Asterisk(1, '*', 0, 13),
                Text("a", 0, 14),
                Asterisk(1, '*', 0, 15)
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
                Strike("aiueo", 0, 0)
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
                Text("aiueo", 0, 0), Whitespace(1, ' ', 0, 5), Strike("aiueo", 0, 6), Text("bcde", 0, 15)
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
                Text("aiueo~~abcd", 0, 0)
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
                Text("aiueo~abcd", 0, 0)
            ), actual
        )
    }

    @Test
    fun html() {
        val lexer = Lexer()

        val actual = lexer.lex("<tagName attr=\"value\">")

        println(actual)

        assertContentEquals(
            listOf(
                StartTagStart("tagName", false, 0, 0),
                AttributeName("attr", 0, 9),
                AttributeValue("value", 0, 14),
                TagEnd("tagName", 0, 21)
            ), actual
        )
    }

    @Test
    fun html2() {
        val lexer = Lexer()

        val actual = lexer.lex("<tagName>")

        println(actual)

        assertContentEquals(
            listOf(
                StartTagStart("tagName", false, 0, 0), TagEnd("tagName", 0, 8)
            ), actual
        )
    }

    @Test
    fun html閉じタグ() {
        val lexer = Lexer()

        val actual = lexer.lex("<tagName attr=\"value\"></tagName>")

        println(actual)

        assertContentEquals(
            listOf(
                StartTagStart("tagName", false, 0, 0),
                AttributeName("attr", 0, 9),
                AttributeValue("value", 0, 14),
                TagEnd("tagName", 0, 21),
                EndTagStart("tagName", 0, 22),
                TagEnd("tagName", 0, 31)
            ), actual
        )
    }

    @Test
    fun html内容() {
        val lexer = Lexer()

        val actual = lexer.lex("<tagName attr=\"value\">hello</tagName>")

        println(actual)

        assertContentEquals(
            listOf(
                StartTagStart("tagName", false, 0, 0),
                AttributeName("attr", 0, 9),
                AttributeValue("value", 0, 14),
                TagEnd("tagName", 0, 21),
                HtmlValue("hello", 0, 22),
                EndTagStart("tagName", 0, 27),
                TagEnd("tagName", 0, 36)
            ), actual
        )
    }

    @Test
    fun htmlネスト() {
        val lexer = Lexer()

        val actual = lexer.lex("<tagName attr=\"value\"><tagB>hello</tagB></tagName>")

        println(actual)

        assertContentEquals(
            listOf(
                StartTagStart("tagName", false, 0, 0),
                AttributeName("attr", 0, 9),
                AttributeValue("value", 0, 14),
                TagEnd("tagName", 0, 21),
                StartTagStart("tagB", false, 0, 22),
                TagEnd("tagB", 0, 27),
                HtmlValue("hello", 0, 28),
                EndTagStart("tagB", 0, 33),
                TagEnd("tagB", 0, 39),
                EndTagStart("tagName", 0, 40),
                TagEnd("tagName", 0, 49)
            ), actual
        )
    }

    @Test
    fun htmlかと思ったら違った() {
        val lexer = Lexer()

        val actual = lexer.lex("<tagName attr=\"value\"")

        println(actual)

        assertContentEquals(
            listOf(
                Text("<tagName", 0, 0), Whitespace(1, ' ', 0, 8), Text("attr=\"value\"", 0, 9)
            ), actual
        )
    }

    @Test
    fun htmlのアトリビュートかと思ったら違った() {
        val lexer = Lexer()

        val actual = lexer.lex("<tagName attr=\"value>")

        println(actual)

        assertContentEquals(
            listOf(
                Text("<tagName", 0, 0), Whitespace(1, ' ', 0, 8), Text("attr=\"value>", 0, 9)
            ), actual
        )
    }

    @Test
    fun html複数行() {
        val lexer = Lexer()

        val actual = lexer.lex("<tagName attr=\"value\">\nvalue\n</tagName>")

        println(actual)

        assertContentEquals(
            listOf(
                StartTagStart("tagName", false, 0, 0),
                AttributeName("attr", 0, 9),
                AttributeValue("value", 0, 14),
                TagEnd("tagName", 0, 21),
                HtmlValue("value", 1, 0),
                EndTagStart("tagName", 2, 0),
                TagEnd("tagName", 2, 9)
            ), actual
        )
    }

    @Test
    fun html改行() {
        val lexer = Lexer()

        val actual = lexer.lex("<tagName attr=\"value\">\nvalue\nfaaaa</tagName>")

        println(actual)

        assertContentEquals(
            listOf(
                StartTagStart("tagName", false, 0, 0),
                AttributeName("attr", 0, 9),
                AttributeValue("value", 0, 14),
                TagEnd("tagName", 0, 21),
                HtmlValue("value\nfaaaa", 1, 0),
                EndTagStart("tagName", 2, 5),
                TagEnd("tagName", 2, 14)
            ), actual
        )
    }

    @Test
    fun htmlアトリビュートいっぱい() {
        val lexer = Lexer()

        val actual = lexer.lex("<tagName attr=\"value\" attr2=\"aaaaaaa\">")

        println(actual)

        assertContentEquals(
            listOf(
                StartTagStart("tagName", false, 0, 0),
                AttributeName("attr", 0, 9),
                AttributeValue("value", 0, 14),
                AttributeName("attr2", 0, 22),
                AttributeValue("aaaaaaa", 0, 28),
                TagEnd("tagName", 0, 37)
            ), actual
        )
    }

    @Test
    fun `html騙し続ける`() {
        val lexer = Lexer()

        val actual = lexer.lex("<<<<<<")

        println(actual)

        assertContentEquals(
            listOf(
                Text("<<<<<<", 0, 0)
            ), actual
        )
    }

    @Test
    fun html閉じタグ省略() {
        val lexer = Lexer()

        val actual = lexer.lex("<tagName/>")

        println(actual)

        assertContentEquals(
            listOf(
                StartTagStart("tagName", true, 0, 0),
                TagEnd("tagName", 0, 8),

                ), actual
        )
    }

    @Test
    fun html閉じタグ省略ネスト() {
        val lexer = Lexer()

        val actual = lexer.lex("<b><a/></b>")

        println(actual)

        assertContentEquals(
            listOf(
                StartTagStart("b", false, 0, 0),
                TagEnd("b", 0, 2),
                StartTagStart("a", true, 0, 3),
                TagEnd("a", 0, 5),
                EndTagStart("b", 0, 7),
                TagEnd("b", 0, 10),
            ), actual
        )
    }

    @Test
    fun html閉じタグ省略ネストと内容() {
        val lexer = Lexer()

        val actual = lexer.lex("<b><a/>aaaa</b>")

        println(actual)

        assertContentEquals(
            listOf(
                StartTagStart("b", false, 0, 0),
                TagEnd("b", 0, 2),
                StartTagStart("a", true, 0, 3),
                TagEnd("a", 0, 5),
                HtmlValue("aaaa", 0, 7),
                EndTagStart("b", 0, 11),
                TagEnd("b", 0, 14),
            ), actual
        )
    }

    @Test
    fun インラインhtml() {
        val lexer = Lexer()

        val actual = lexer.lex("aaaaa<b><a/>aaaa</b>")

        println(actual)

        assertContentEquals(
            listOf(
                Text("aaaaa", 0, 0),
                StartTagStart("b", false, 0, 5),
                TagEnd("b", 0, 7),
                StartTagStart("a", true, 0, 8),
                TagEnd("a", 0, 10),
                HtmlValue("aaaa", 0, 12),
                EndTagStart("b", 0, 16),
                TagEnd("b", 0, 19),
            ), actual
        )
    }

    //
    @Test
    fun test() {
        val lexer = Lexer()

        val actual = lexer.lex(
            """[![official project](http://jb.gg/badges/official.svg)](https://github.com/JetBrains#jetbrains-on-github)

# Multiplatform library template

## What is it?

***abc**d**e**f*

This repository contains a simple library project, intended to demonstrate
a [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html) library that is deployable
to [Maven Central](https://central.sonatype.com/).

The library has only one function: generate the [Fibonacci sequence](https://en.wikipedia.org/wiki/Fibonacci_sequence)
starting from platform-provided numbers. Also, it has a test for each platform just to be sure that tests run.

Note that no other actions or tools usually required for the library development are set up, such
as [tracking of backwards compatibility](https://kotlinlang.org/docs/jvm-api-guidelines-backward-compatibility.html#tools-designed-to-enforce-backward-compatibility),
explicit API mode, licensing, contribution guideline, code of conduct and others. You can find a guide for best
practices for designing Kotlin libraries [here](https://kotlinlang.org/docs/api-guidelines-introduction.html).

## How to publish?

This guide describes the steps of publishing a library built with Kotlin Multiplatform to
the [Maven Central repository](https://central.sonatype.com/). To publish your library, you’ll need to:

* Set up credentials, including an account on Maven Central and a PGP key to use for signing.
* Configure the publishing plugin in your library’s project.
* Provide your credentials to the publishing plugin so it can sign and upload your artifacts.
* Run the publication task, either locally or using continuous integration.

This guide assumes that you are:

- Creating an open-source library.
- Using macOS or Linux. If you are a Windows user, use [GnuPG or Gpg4win](https://gnupg.org/download) to generate a key
  pair.
- Either not registered on Maven Central yet, or have an existing account that’s suitable
  for [publishing to the Central Portal](https://central.sonatype.org/publish-ea/publish-ea-guide/) (created after March
  12th, 2024, or migrated to the Central Portal by their support).
- Publishing your library in a GitHub repository.
- Using GitHub Actions for continuous integration.

Most of the steps here are still applicable if you’re using a different setup, but there might be some differences you
need to account for.
An [important limitation](https://kotlinlang.org/docs/multiplatform-publish-lib.html#host-requirements) is that Apple
targets must be built on a machine with macOS.

Throughout this guide, we’ll use
the [https://github.com/kotlin-hands-on/fibonacci](https://github.com/kotlin-hands-on/fibonacci) repository as an
example. You can refer to the code of this repository to see how the publishing setup works. You **must replace all
example values with your own** as you’re configuring your project.

### Prepare accounts and credentials

#### Register a namespace

Artifacts published to Maven repositories are identified by their coordinates, for example `com.example:library:1.0.0`.
These coordinates are made up of three parts, separated by colons: the `groupId`, `artifactId`, and `version`.

As a first step for publishing to Maven Central, you’ll need to have a verified namespace. The `groupId` of the
artifacts you publish will have to start with the name of your verified namespace. For example, if you register the
`com.example` namespace, you’ll be able to publish artifacts with the `groupId` set to `com.example` or
`com.example.libraryname`.

To get started with publishing to Maven Central, sign in (or create a new account) on
the [Maven Central](https://central.sonatype.com/) portal. Once signed in, navigate
to [Namespaces](https://central.sonatype.com/publishing/namespaces) under your profile, and click the Add Namespace
button. Here, you can register a namespace for your artifacts, either based on your GitHub account or a domain name that
you own.

**For a GitHub repository**
Using your GitHub account to create a namespace is a good option if you don’t own a domain name to use for publication.
To create a namespace based on your GitHub account:

1. Enter `io.github.<your username>` as your namespace. For example, `io.github.kotlin-hands-on`.
2. Copy the Verification Key displayed.
3. On GitHub, create a new repository with your GitHub account with the verification key as the repository’s name. For
   example, `http://github.com/kotlin-hands-on/ex4mpl3c0d`.
4. Navigate back to Maven Central, and click on the Verify Namespace button. After verification succeeds you can delete
   the repository you’ve created.

**For a domain name**
To use a domain name that you own as your namespace:

1. Enter your domain as the namespace using a reverse-DNS form. If your domain is `example.com`, enter `com.example`.
2. Copy the Verification Key displayed.
3. Create a new DNS TXT record with the verification key as its contents.
   See [Maven Central’s FAQ](https://central.sonatype.org/faq/how-to-set-txt-record/) for more information on how to do
   this with various domain registrars.
4. Navigate back to Maven Central, and click on the Verify Namespace button. After verification succeeds you can delete
   the TXT record you’ve created.

#### Generate a Key Pair

Artifacts published to Maven
Central [must be signed with a PGP signature](https://central.sonatype.org/publish/requirements/gpg/), which allows
users to validate the origin of artifacts.

To get started with signing, you’ll need to generate a key pair:

* The **private key** is used to sign your artifacts, and should never be shared with others.
* The **public key** can be used by others to validate the signature of the artifacts, and should be published.

The `gpg` tool that can manage signatures for you is available
from [their website](https://gnupg.org/download/index.html). You can also install it using package managers such
as [Homebrew](https://brew.sh/):

```bash
brew install gpg
```

Generate a key pair with the following command, and fill in the required details when prompted.

```bash
gpg --full-generate-key
```

Choose the recommended defaults for the type of key to be created. You can leave these selections empty and press Enter
to accept the default values.

> [!NOTE]
> At the time of writing, this is `ECC (sign and encrypt)` with `Curve 25519`. Older versions of `gpg` might default to
`RSA` with a `3072` bit key size.

Next, you’ll be prompted to set the expiration of the key. If you choose to create a key that automatically expires
after a set amount of time, you’ll need
to [extend its validity](https://central.sonatype.org/publish/requirements/gpg/#dealing-with-expired-keys) when it
expires.

You will be asked for your real name, email, and a comment. You can leave the comment empty.

```text
Please select what kind of key you want:
    (1) RSA and RSA
    (2) DSA and Elgamal
    (3) DSA (sign only)
    (4) RSA (sign only)
    (9) ECC (sign and encrypt) *default*
    (10) ECC (sign only)
    (14) Existing key from card
Your selection? 9

Please select which elliptic curve you want:
    (1) Curve 25519 *default*
    (4) NIST P-384
    (6) Brainpool P-256
Your selection? 1

Please specify how long the key should be valid.
    0 = key does not expire
    <n>  = key expires in n days
    <n>w = key expires in n weeks
    <n>m = key expires in n months
    <n>y = key expires in n years
Key is valid for? (0) 0
Key does not expire at all

Is this correct? (y/N) y
GnuPG needs to construct a user ID to identify your key.
```

You will be asked for a passphrase to encrypt the key, which you have to repeat. Keep this passphrase stored securely
and privately. You’ll be using it later to access the private key.

Let’s take a look at the key we’ve created with the following command:

```bash
gpg --list-keys
```

The output will look something like this:

```text
pub   ed25519 2024-10-06 [SC]
      F175482952A225BFC4A07A715EE6B5F76620B385CE
uid   [ultimate] Your name <your email address>
      sub   cv25519 2024-10-06 [E]
```

You’ll need to use the long alphanumerical identifier of your key displayed here in the following steps.

#### Upload the public key

You need
to [upload the public key to a keyserver](https://central.sonatype.org/publish/requirements/gpg/#distributing-your-public-key)
for it to be accepted by Maven Central. There are multiple available keyservers, we’ll use `keyserver.ubuntu.com` as a
default choice.

Run the following command to upload your public key using `gpg`, **substituting your own keyid** in the parameters:

```bash
gpg --keyserver keyserver.ubuntu.com --send-keys F175482952A225BFC4A07A715EE6B5F76620B385CE
```

#### Export your private key

To let your Gradle project access your private key, you’ll need to export it to a file. Use the following command, *
*passing in your own keyid** as a parameter. You will be prompted to enter the passphrase you’ve used when creating the
key.

```bash
gpg --armor --export-secret-keys F175482952A225BFC4A07A715EE6B5F76620B385CE > key.gpg
```

This will create a `key.gpg` file which contains your private key.

> [!CAUTION]
> Never share a private key with anyone.

If you check the contents of the file, you should see contents similar to this:

```text
-----BEGIN PGP PRIVATE KEY BLOCK-----
lQdGBGby2X4BEACvFj7cxScsaBpjty60ehgB6xRmt8ayt+zmgB8p+z8njF7m2XiN
...
bpD/h7ZI7FC0Db2uCU4CYdZoQVl0MNNC1Yr56Pa68qucadJhY0sFNiB63KrBUoiO
-----END PGP PRIVATE KEY BLOCK-----
```

#### Generate the user token

Your project will also need to authenticate with Maven Central to upload artifacts. On the Central Portal, navigate to
the [Account](https://central.sonatype.com/account) page, and click on *Generate User Token*.

The output will look like the example below, containing a username and a password. Store this information securely, as
it can’t be viewed again on the Central Portal. If you lose these credentials, you’ll need to generate new ones later.

```xml

<server>
    <id>\$\{server}</id>
    <username>l3nfaPmz</username>
    <password>gh9jT9XfnGtUngWTZwTu/8241keYdmQpipqLPRKeDLTh</password>
</server>
```

### Configure the project

#### Prepare your library project

If you started developing your library from a template project, this is a good time to change any default names in the
project to match your own library’s name. This includes the name of your library module, and the name of the root
project in your top-level `build.gradle.kts` file.

If you have an Android target in your project, you should follow
the [steps to prepare your Android library release](https://developer.android.com/build/publish-library/prep-lib-release).
This, at a minimum, requires you
to [specify an appropriate namespace](https://developer.android.com/build/publish-library/prep-lib-release#choose-namespace)
for your library, so that a unique R class will be generated when their resources are compiled. Notice that the
namespace is different from the Maven namespace created in the [Register a namespace](#register-a-namespace) section
above.

```kotlin
// build.gradle.kts

android {
    namespace = "io.github.kotlinhandson.fibonacci"
}
```

#### Set up the publishing plugin

This guide uses [vanniktech/gradle-maven-publish-plugin](https://github.com/vanniktech/gradle-maven-publish-plugin) to
help with publications to Maven Central. You can read more about the advantages of the
plugin [here](https://vanniktech.github.io/gradle-maven-publish-plugin/#advantages-over-maven-publish). See
the [plugin’s documentation](https://vanniktech.github.io/gradle-maven-publish-plugin/central/) to learn more about its
usage and available configuration options.

To add the plugin to your project, add the following line in the plugins block, in your library module’s
`build.gradle.kts` file:

```kotlin
// build.gradle.kts

plugins {
    id("com.vanniktech.maven.publish") version "0.29.0"
}
```

*Note: for the latest available version of the plugin, check
its [releases page](https://github.com/vanniktech/gradle-maven-publish-plugin/releases).*

In the same file, add the following configuration. Customize all these values appropriately for your library.

```kotlin
// build.gradle.kts

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

    signAllPublications()

    coordinates(group.toString(), "fibonacci", version.toString())

    pom {
        name = "Fibonacci library"
        description = "A mathematics calculation library."
        inceptionYear = "2024"
        url = "https://github.com/kotlin-hands-on/fibonacci/"
        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                distribution = "https://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }
        developers {
            developer {
                id = "kotlin-hands-on"
                name = "Kotlin Developer Advocate"
                url = "https://github.com/kotlin-hands-on/"
            }
        }
        scm {
            url = "https://github.com/kotlin-hands-on/fibonacci/"
            connection = "scm:git:git://github.com/kotlin-hands-on/fibonacci.git"
            developerConnection = "scm:git:ssh://git@github.com/kotlin-hands-on/fibonacci.git"
        }
    }
}
```

Note that it’s also possible to use Gradle properties instead.

Some of the most important, required settings here are:

* The `coordinates`, which specify the `groupId`, `artifactId`, and `version` of your library.
* The [license](https://central.sonatype.org/publish/requirements/#license-information) that you’re publishing your
  library under.
* The [developer information](https://central.sonatype.org/publish/requirements/#developer-information) which lists the
  authors of the library.
* [SCM (Source Code Management) information](https://central.sonatype.org/publish/requirements/#scm-information), which
  specifies where the sources of your library are available.

### Publish to Maven Central from Continuous Integration

#### Add a GitHub Actions workflow to your project

You can set up continuous integration which builds and publishes your library for you. We’ll
use [GitHub Actions](https://docs.github.com/en/actions) as an example.

To get started, add the following workflow to your repository, in the `.github/workflows/publish.yml` file.

```yaml
# .github/workflows/publish.yml

name: Publish
on:
  release:
    types: [ released, prereleased ]
jobs:
  publish:
    name: Release build and publish
    runs-on: macOS-latest
    steps:
      - name: Check out code
        uses: actions/checkout@v4
      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          distribution: 'zulu'
          java-version: 21
      - name: Publish to MavenCentral
        run: ./gradlew publishToMavenCentral --no-configuration-cache
        env:
          ORG_GRADLE_PROJECT_mavenCentralUsername: \$\{{ secrets.MAVEN_CENTRAL_USERNAME }}
          ORG_GRADLE_PROJECT_mavenCentralPassword: \$\{{ secrets.MAVEN_CENTRAL_PASSWORD }}
          ORG_GRADLE_PROJECT_signingInMemoryKeyId: \$\{{ secrets.SIGNING_KEY_ID }}
          ORG_GRADLE_PROJECT_signingInMemoryKeyPassword: \$\{{ secrets.SIGNING_PASSWORD }}
          ORG_GRADLE_PROJECT_signingInMemoryKey: \$\{{ secrets.GPG_KEY_CONTENTS }}
```

After committing and pushing this change, this workflow will run automatically when you create a release (including a
pre-release) in the GitHub repository hosting your project. It checks out the current version of your code, sets up a
JDK, and then runs the `publishToMavenCentral` Gradle task.

> [!NOTE]
> Alternatively, you could configure the workflow
> to [trigger when a tag is pushed](https://stackoverflow.com/a/61892639) to your repository.
>
> The script above disables
> Gradle [configuration cache](https://docs.gradle.org/current/userguide/configuration_cache.html) for the publication
> task by adding `--no-configuration-cache` to the Gradle command, as the publication plugin does not support it (see
> this [open issue](https://github.com/gradle/gradle/issues/22779)).
>
> Reminder: When using `publishToMavenCentral`, you’ll still need to check and release your deployment manually on the
> website, as described in the previous section. You may use `publishAndReleaseToMavenCentral` instead for a fully
> automated release.

This action will need your signing details and your Maven Central credentials. These will be configured as GitHub
Actions secrets in the next section. The configuration of the workflow above takes these secrets and places them into
environment variables, which will make them available to the Gradle build automatically.

### Add secrets to GitHub

To use the keys and credentials required for publication in your GitHub Action workflow while keeping them private, you
need to place those values into secrets. From your GitHub repository, go to `Settings` \>
`(Security) Secrets and variables > Actions`.

Click on the `New repository secret` button, and add the following secrets:

- `MAVEN_CENTRAL_PASSWORD` and `MAVEN_CENTRAL_PASSWORD` are the values generated by the Central Portal website in
  the [Generate User Token](#generate-the-user-token) section.
- `SIGNING_KEY_ID` is **the last 8 characters** of your signing key’s identifier.
- `SIGNING_PASSWORD` is the passphrase you’ve provided when generating your signing key.
- `GPG_KEY_CONTENTS` should contain the contents of your GPG private key file, which you’ve created earlier in
  the [Export your private key](#export-your-private-key) section.

![](/images/github_secrets.png)

Note again that the names used for these secrets must match those used by the workflow that accesses their values.

#### Create a release on GitHub

With the workflow and secrets set up, you’re now ready
to [create a release](https://docs.github.com/en/repositories/releasing-projects-on-github/managing-releases-in-a-repository#creating-a-release)
that will trigger the publication of your library.

Go to your GitHub repository’s main page, and click on Releases in the menu in the right sidebar.

![](/images/github_releases.png)

Click *Draft a new release*.

![](/images/draft_release.png)

Each release creates a new tag. Set the name for the tag to be created, and set a name for the release (these may be
identical). Note that setting a version here does not change the version of your coordinates configured in your
`build.gradle.kts` file, so you should update that version before creating a new release.

![](/images/create_release_and_tag.png)

Double-check the branch you want to target with the release (especially if you want to release from a branch that’s
different from your default), and add appropriate release notes for your new version.

The checkboxes below allow you to mark a release as a pre-release (useful for alpha, beta, or RC versions of a library),
or to set the release as the latest available one:

![](/images/release_settings.png)

Click the *Publish release* button to create the new release. This will immediately show up on your GitHub repository’s
main page.

Click the Actions tab on the top of your GitHub repository. Here you’ll see the new workflow was triggered by the GitHub
release. Click it to see the outputs of the publication task.

After this task completes successfully, navigate to
the [Deployments](https://central.sonatype.com/publishing/deployments) dashboard. You should see a new deployment here.
This deployment will be in the *pending* and *validating* states for some time while Maven Central performs checks on
it.

Once your deployment moves to a *validated* state, you should see that it contains all the artifacts you’ve uploaded. If
everything looks correct, click the *Publish* button to release these artifacts.

![](/images/published_on_maven_central.png)

Note that it will take some time (about 15–30 minutes, usually) after the release for the artifacts to be available
publicly on Maven Central.
Also note that the library may be available for use before they are indexed
on [the Maven Central website](https://central.sonatype.com/).

There’s also another task available which both uploads and releases the artifacts automatically once the deployment is
verified, without having to manually release them on the website:

```bash
./gradlew publishAndReleaseToMavenCentral
```

**Et voilà, you have successfully published your library to Maven Central.**

# Next steps

- Share your library with the Kotlin Community in the `#feed` channel in
  the [Kotlin Slack](https://kotlinlang.slack.com/) (To sign up visit https://kotl.in/slack.)
- Add [shield.io badges](https://shields.io/badges/maven-central-version) to your README.
- Create a documentation site for your project using [Writerside](https://www.jetbrains.com/writerside/).
- Share API documentation for your project using [Dokka](https://kotl.in/dokka).
- Add [Renovate](https://docs.renovatebot.com/) to automatically update dependencies.

# Other resources

* [Publishing via the Central Portal](https://central.sonatype.org/publish-ea/publish-ea-guide/)
* [Gradle Maven Publish Plugin \- Publishing to Maven Central](https://vanniktech.github.io/gradle-maven-publish-plugin/central/)
"""
        )


        println(actual)
    }
}