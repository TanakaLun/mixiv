package com.mrl.pixiv.search

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import kotlin.test.Test
import kotlin.test.assertEquals

class SearchCompletionTest {
    @Test
    fun additionalKeywordIsSuggestedAndReplacedWithoutLosingTheFirstKeyword() {
        val query = TextFieldValue("初音 かわ", TextRange(5))
        val token = query.completionToken()
        assertEquals("かわ", token.text)
        assertEquals("初音 かわいい", token.replaceIn(query.text, "かわいい"))
    }

    @Test
    fun editingAMiddleKeywordPreservesTheSuffixAndExclusionOperator() {
        val query = TextFieldValue("猫 -犬 manga", TextRange(4))
        val token = query.completionToken()
        assertEquals("犬", token.text)
        assertEquals("猫 -狗 manga", token.replaceIn(query.text, "狗"))
    }

    @Test
    fun trailingWhitespaceDoesNotRequestTheEntireQueryAgain() {
        assertEquals("", TextFieldValue("猫　", TextRange(2)).completionToken().text)
        assertEquals("", TextFieldValue("").completionToken().text)
    }

    @Test
    fun reverseSelectionStillReplacesTheSelectedKeyword() {
        val query = TextFieldValue("猫 dog 鳥", TextRange(5, 2))
        assertEquals("猫 犬 鳥", query.completionToken().replaceIn(query.text, "犬"))
    }
}
