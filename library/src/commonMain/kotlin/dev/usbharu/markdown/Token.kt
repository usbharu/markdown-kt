package dev.usbharu.markdown

sealed class Token()

data class Text(var text: String) : Token()
data class Break(var count: Int) : Token()
data class Header(var count: Int) : Token()
data class Quote(var count: Int) : Token()