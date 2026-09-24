package com.mrl.pixiv.report

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.maxLength
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mrl.pixiv.common.router.ReportType
import com.mrl.pixiv.common.router.currentNavigationManager
import com.mrl.pixiv.common.util.RStrings
import com.mrl.pixiv.common.util.ToastUtil
import com.mrl.pixiv.common.viewmodel.asState
import com.mrl.pixiv.strings.please_select
import com.mrl.pixiv.strings.report
import com.mrl.pixiv.strings.report_detail
import com.mrl.pixiv.strings.report_detail_hint
import com.mrl.pixiv.strings.report_reason
import com.mrl.pixiv.strings.report_success
import com.mrl.pixiv.strings.send
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.overlay.OverlayBottomSheet
import top.yukonga.miuix.kmp.theme.MiuixTheme

private const val MAX_REPORT_CONTENT_LENGTH = 3000

@Composable
fun ReportScreen(
    id: Long,
    type: ReportType,
    modifier: Modifier = Modifier,
    viewModel: ReportCommentViewModel = koinViewModel { parametersOf(id, type) }
) {
    val navigationManager = currentNavigationManager()
    val state = viewModel.asState()
    val reportContent = viewModel.reportContent
    var showTopicSheet by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .imePadding(),
        topBar = {
            TopAppBar(
                title = stringResource(RStrings.report),
                navigationIcon = {
                    IconButton(
                        onClick = { navigationManager.popBackStack() }
                    ) {
                        Icon(
                            imageVector = MiuixIcons.Back,
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        if (state.topicList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = stringResource(RStrings.report_reason),
                    style = MiuixTheme.textStyles.subtitle,
                    color = MiuixTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )

                val selectedTopic = state.topicList.find { it.topicId == state.selectedTopicId }
                Text(
                    text = selectedTopic?.topicTitle ?: stringResource(RStrings.please_select),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showTopicSheet = true }
                        .padding(vertical = 12.dp),
                    style = MiuixTheme.textStyles.main
                )

                Text(
                    text = stringResource(RStrings.report_detail),
                    style = MiuixTheme.textStyles.subtitle,
                    color = MiuixTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    TextField(
                        state = reportContent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        label = stringResource(RStrings.report_detail_hint),
                        useLabelAsPlaceholder = true,
                        inputTransformation = InputTransformation.maxLength(MAX_REPORT_CONTENT_LENGTH),
                    )
                    Text(
                        text = "${reportContent.text.length}/$MAX_REPORT_CONTENT_LENGTH",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End
                    )
                }

                Button(
                    onClick = {
                        viewModel.submitReport()
                        ToastUtil.safeShortToast(RStrings.report_success)
                        navigationManager.popBackStack()
                    },
                    enabled = state.selectedTopicId != null && reportContent.text.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    Text(text = stringResource(RStrings.send))
                }
            }
        }
    }

    OverlayBottomSheet(
        show = showTopicSheet,
        onDismissRequest = { showTopicSheet = false },
    ) {
        LazyColumn {
            items(
                items = state.topicList,
                key = { it.topicId }
            ) { topic ->
                BasicComponent(
                    title = topic.topicTitle,
                    onClick = {
                        viewModel.selectTopic(topic.topicId)
                        showTopicSheet = false
                    },
                )
                HorizontalDivider()
            }
        }
    }
}
