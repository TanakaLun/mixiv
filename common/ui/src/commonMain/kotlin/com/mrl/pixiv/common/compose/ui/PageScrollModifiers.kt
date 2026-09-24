package com.mrl.pixiv.common.compose.ui

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.utils.overScrollVertical

/**
 * Page scroll modifiers from the miuix example (`pageScrollModifiers`): miuix overscroll
 * effect plus the [ScrollBehavior] nested-scroll hook that drives top-bar collapse.
 * Apply to the root of the page's scrollable content.
 */
fun Modifier.pageScrollModifiers(scrollBehavior: ScrollBehavior): Modifier =
    this
        .overScrollVertical()
        .nestedScroll(scrollBehavior.nestedScrollConnection)
