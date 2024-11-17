package dev.usbharu.markdown

import kotlin.js.JsExport
import kotlin.js.JsName

@JsExport
sealed class AstNode {
    open fun print(): String {
        return toString()
    }

    data class RootNode(val node: AstNode) : AstNode() {
        override fun print(): String {
            return node.print()
        }
    }

    data class BodyNode(val body: List<AstNode>) : AstNode() {
        override fun print(): String {
            return body.joinToString("") {
                if (it is BlockNode) {
                    it.print() + "\n\n"
                } else {
                    it.print()
                }
            }
        }

        @JsName("with")
        constructor(vararg nodes: AstNode) : this(nodes.toList())
    }

    sealed class BlockNode : AstNode()

    data class HeaderNode(val header: Int, val headerTextNode: HeaderTextNode?) : BlockNode() {
        override fun print(): String {
            return "#".repeat(header) + " " + headerTextNode?.print().orEmpty()
        }
    }

    data class HeaderTextNode(val text: String) : BlockNode() {
        override fun print(): String {
            return text
        }
    }

    sealed interface QuotableNode {
        fun print(): String
    }

    data class QuoteNode(val nodes: MutableList<QuotableNode>) : AstNode(), QuotableNode {
        override fun print(): String {
            return printNest(1)
        }

        fun printNest(nest: Int): String {
            val builder = StringBuilder()
            for (node in nodes) {
                if (node is QuoteNode) {
                    builder.append(node.printNest(nest + 1))
                } else if (node is BreakNode) {
                    builder.append(node.print())
                } else {
                    builder.append(">".repeat(nest)).append(' ').append(node.print())
                }
            }
            return builder.toString()
        }
    }

    data object SeparatorNode : BlockNode() {
        override fun print(): String {
            return "---"
        }
    }

    sealed interface ListableNode {
        fun print(): String
    }

    sealed class ListNode(open val itemNode: List<ListItemNode>) : BlockNode(), ListableNode {
        override fun print(): String {
            return nestedPrint(0)
        }

        fun nestedPrint(nest: Int): String {
            val builder = StringBuilder()
            for (node in itemNode) {
                builder.append(" ".repeat(nest)).append("- ").append(node.nestedPrint(nest)).append("\n")
            }
            return builder.toString().trim('\n')
        }
    }

    data class DecimalListNode(override val itemNode: List<ListItemNode>) : ListNode(itemNode)
    data class DiscListNode(override val itemNode: List<ListItemNode>) : ListNode(itemNode) {
        @JsName("with")
        constructor(vararg nodes: ListItemNode) : this(nodes.toList())
    }

    data class ListItemNode(val nodes: MutableList<ListableNode>) : BlockNode() {
        override fun print(): String {
            return nestedPrint(0)
        }

        fun nestedPrint(nest: Int): String {
            val builder = StringBuilder()
            for (node in nodes) {
                if (node is ListNode) {
                    builder.append("\n").append(node.nestedPrint(nest + 1))
                } else {
                    builder.append(node.print())
                }
            }
            return builder.toString()
        }

        @JsName("with")
        constructor(vararg nodes: ListableNode) : this(nodes.toMutableList())
    }

    data class ParagraphNode(val nodes: List<InlineNode>) : BlockNode() {
        override fun print(): String {
            return nodes.joinToString("\n") { it.print() }
        }
    }

    sealed class InlineNode : AstNode(), QuotableNode, ListableNode
    data class InlineNodes(val nodes: MutableList<InlineNode>) : InlineNode() {
        override fun print(): String {
            return nodes.joinToString("") { it.print() }
        }
    }

    data class ItalicNode(val nodes: MutableList<InlineNode>) : InlineNode() {
        override fun print(): String {
            return nodes.joinToString("", prefix = "*", postfix = "*") { it.print() }
        }
    }

    data class BoldNode(val nodes: MutableList<InlineNode>) : InlineNode() {
        override fun print(): String {
            return nodes.joinToString("", prefix = "**", postfix = "**") { it.print() }
        }
    }

    data class StrikeThroughNode(val nodes: List<InlineNode>) : InlineNode()
    data class PlainText(val text: String) : InlineNode() {
        override fun print(): String {
            return text
        }
    }

    data class ImageNode(val urlUrlNode: UrlNode) : InlineNode() {
        override fun print(): String {
            return "!" + urlUrlNode.print()
        }
    }

    data class UrlNode(val url: UrlUrlNode, val urlNameNode: UrlNameNode, val urlTitleNode: UrlTitleNode?) :
        InlineNode() {
        override fun print(): String {
            return "[${urlNameNode.print()}](${url.print()} ${urlTitleNode?.print().orEmpty()})"
        }
    }

    data class UrlUrlNode(val url: String) : InlineNode() {
        override fun print(): String {
            return url
        }
    }

    data class UrlTitleNode(val title: String) : InlineNode() {
        override fun print(): String {
            return "\"$title\""
        }
    }

    data class UrlNameNode(val name: String) : InlineNode() {
        override fun print(): String {
            return name
        }
    }

    data class InlineCodeNode(val code: String) : InlineNode() {
        override fun print(): String {
            return "`$code`"
        }
    }

    data class SimpleUrlNode(val url: String) : InlineNode() {
        override fun print(): String {
            return url
        }
    }

    data object BreakNode : InlineNode() {
        override fun print(): String {
            return "\n"
        }
    }
}

