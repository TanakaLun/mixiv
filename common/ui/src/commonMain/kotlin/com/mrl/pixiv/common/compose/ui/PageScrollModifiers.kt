package com.mrl.pixiv.common.compose.ui

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic

/**
 * Page scroll modifiers from the miuix example (`pageScrollModifiers`): scroll-end haptic,
 * miuix overscroll effect and the [ScrollBehavior] nested-scroll hook that drives top-bar
 * collapse. Apply to the root of the page's scrollable content (not the Scaffold).
 */
fun Modifier.pageScrollModifiers(scrollBehavior: ScrollBehavior): Modifier =
    this
        .scrollEndHaptic()
        .overScrollVertical()
        .nestedScroll(scrollBehavior.nestedScrollConnection)
