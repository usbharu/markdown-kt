package dev.usbharu.markdown

import kotlin.collections.List

sealed class AstNode
data class RootNode(val node: AstNode) : AstNode()
data class BodyNode(val body: List<AstNode>) : AstNode()
sealed class BlockNode : AstNode()

data class HeaderNode(val header: Int, val headerText: HeaderText?) : BlockNode()
data class HeaderText(val text: String) : BlockNode()
sealed interface QuotableNode
data class QuoteNode(val nodes: List<QuotableNode>) : AstNode(), QuotableNode
data object SeparatorNode : BlockNode()
sealed class ListNode : BlockNode()
data class DiscListNode(val node: InlineNode, val childList: List<ListNode>) : ListNode()
data class DecimalListNode(val node: InlineNode, val childList: List<ListNode>) : ListNode()

sealed class InlineNode : AstNode(), QuotableNode