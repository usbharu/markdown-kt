package dev.usbharu.markdown

import kotlin.js.JsExport

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
            return body.joinToString("\n") { it.print() }
        }
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

    sealed interface QuotableNode
    data class QuoteNode(val nodes: List<QuotableNode>) : AstNode(), QuotableNode
    data object SeparatorNode : BlockNode() {
        override fun print(): String {
            return "---"
        }
    }

    sealed class ListNode : BlockNode()
    data class DiscListNode(val node: InlineNode, val childList: List<ListNode>) : ListNode()
    data class DecimalListNode(val node: InlineNode, val childList: List<ListNode>) : ListNode()
    data class ParagraphNode(val nodes: List<InlineNode>) : ListNode() {
        override fun print(): String {
            return nodes.joinToString("\n") { it.print() }
        }
    }

    sealed class InlineNode : AstNode(), QuotableNode
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
}

