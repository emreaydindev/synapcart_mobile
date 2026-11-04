package com.neilb.synapcart.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle

/**
 * Renders the small markdown subset the agent writes: `**bold**` / `***bold***`, `` `code` ``,
 * `#` headings (shown bold) and `-` / `*` / `•` bullets (shown as "•"). Everything else is
 * kept as plain text, and unmatched markers are left as they are.
 */
fun chatMarkdown(
    input: String?,
    bold: SpanStyle,
    code: SpanStyle = bold,
    heading: SpanStyle = bold
): AnnotatedString = buildAnnotatedString {
    if (input.isNullOrBlank()) return@buildAnnotatedString
    val lines = input.trim().replace("\r\n", "\n").split('\n')
    lines.forEachIndexed { index, raw ->
        val line = raw.trimEnd()
        val trimmed = line.trimStart()
        when {
            HEADING.matches(trimmed) -> withStyle(heading) {
                appendInline(trimmed.replace(HEADING_PREFIX, ""), bold, code)
            }
            BULLET.containsMatchIn(trimmed) -> {
                val indent = if (line.length - trimmed.length >= 2) "    " else ""
                append(indent)
                append("• ")
                appendInline(trimmed.replace(BULLET, ""), bold, code)
            }
            else -> appendInline(line, bold, code)
        }
        if (index < lines.lastIndex) append('\n')
    }
}

private val HEADING = Regex("^#{1,6}\\s+.+")
private val HEADING_PREFIX = Regex("^#{1,6}\\s+")
private val BULLET = Regex("^[-*•]\\s+")
private val INLINE = Regex("\\*\\*\\*(.+?)\\*\\*\\*|\\*\\*(.+?)\\*\\*|`([^`]+)`")

private fun AnnotatedString.Builder.appendInline(text: String, bold: SpanStyle, code: SpanStyle) {
    var last = 0
    for (m in INLINE.findAll(text)) {
        if (m.range.first > last) append(text.substring(last, m.range.first))
        val strong = m.groups[1]?.value ?: m.groups[2]?.value
        val mono = m.groups[3]?.value
        when {
            strong != null -> withStyle(bold) { append(strong) }
            mono != null -> withStyle(code) { append(mono) }
        }
        last = m.range.last + 1
    }
    if (last < text.length) append(text.substring(last))
}
