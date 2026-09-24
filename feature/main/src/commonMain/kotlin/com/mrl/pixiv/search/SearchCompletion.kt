package com.mrl.pixiv.search

import androidx.compose.ui.text.input.TextFieldValue

internal data class SearchCompletionToken(val text: String, val start: Int, val end: Int) {
    fun replaceIn(query: String, suggestion: String): String = query.replaceRange(start, end, suggestion)
}

/** 只补全光标所在的关键词，保留多关键词搜索中的其余内容。 */
internal fun TextFieldValue.completionToken(): SearchCompletionToken {
    val cursor = selection.min.coerceIn(0, text.length)
    var start = cursor
    var end = selection.max.coerceIn(cursor, text.length)
    while (start > 0 && !text[start - 1].isWhitespace()) start--
    while (end < text.length && !text[end].isWhitespace()) end++
    // 替换关键词时保留排除运算符。
    if (start < end && text[start] == '-') start++
    return SearchCompletionToken(text.substring(start, end), start, end)
}
