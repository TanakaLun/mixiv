package com.mrl.pixiv.common.compose.ui

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic

/**
 * Page scroll modifiers from the miuix example (`pageScrollModifiers`): scroll-end haptic,
 * miuix overscroll effect, the [ScrollBehavior] nested-scroll hook that drives top-bar collapse
 * and the trailing [fillMaxHeight] that makes the scrollable span the whole content area.
 *
 * The trailing [fillMaxHeight] is required: `overScrollVertical` clips its content to its own
 * bounds, so a scrollable that wraps its content would clip short pages to the content size and
 * cut them while pulling down.
 *
 * Apply to the root of the page's scrollable content (not the Scaffold).
 */
fun Modifier.pageScrollModifiers(scrollBehavior: ScrollBehavior): Modifier =
    this
        .scrollEndHaptic()
        .overScrollVertical()
        .nestedScroll(scrollBehavior.nestedScrollConnection)
        .fillMaxHeight()
