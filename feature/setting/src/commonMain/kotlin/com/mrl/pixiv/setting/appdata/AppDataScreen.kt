package com.mrl.pixiv.setting.appdata

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mrl.pixiv.common.compose.rememberThrottleClick
import com.mrl.pixiv.common.compose.ui.pageScrollModifiers
import com.mrl.pixiv.common.router.NavigationManager
import com.mrl.pixiv.common.router.currentNavigationManager
import com.mrl.pixiv.common.util.RStrings
import com.mrl.pixiv.common.viewmodel.asState
import com.mrl.pixiv.strings.app_data
import com.mrl.pixiv.strings.cancel
import com.mrl.pixiv.strings.clear_cache
import com.mrl.pixiv.strings.confirm
import com.mrl.pixiv.strings.export_data
import com.mrl.pixiv.strings.history_import_user_mismatch_desc
import com.mrl.pixiv.strings.history_import_user_mismatch_title
import com.mrl.pixiv.strings.import_data
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.dialogs.compose.rememberFileSaverLauncher
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import kotlin.time.Clock

private data class HistoryImportDialogData(
    val requestId: Long,
    val currentUserId: Long,
    val importUserId: Long,
)

@Composable
fun AppDataScreen(
    modifier: Modifier = Modifier,
    navigationManager: NavigationManager = currentNavigationManager(),
    viewModel: AppDataViewModel = koinViewModel(),
) {
    val state = viewModel.asState()
    var dialogData by remember { mutableStateOf<HistoryImportDialogData?>(null) }

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            if (effect is ConfirmHistoryImportEffect) {
                dialogData = HistoryImportDialogData(
                    requestId = effect.requestId,
                    currentUserId = effect.currentUserId,
                    importUserId = effect.importUserId,
                )
            }
        }
    }

    val exportLauncher = rememberFileSaverLauncher(
        dialogSettings = FileKitDialogSettings.createDefault()
    ) { file ->
        file?.let { viewModel.exportData(it) }
    }

    val importLauncher = rememberFilePickerLauncher(
        type = FileKitType.File(extensions = listOf("zip"))
    ) { file ->
        file?.let { viewModel.importData(it) }
    }


    val scrollBehavior = MiuixScrollBehavior()
    Scaffold(
        modifier = modifier.pageScrollModifiers(scrollBehavior),
        topBar = {
            TopAppBar(
                title = stringResource(RStrings.app_data),
                navigationIcon = {
                    IconButton(onClick = navigationManager::popBackStack) {
                        Icon(MiuixIcons.Back, contentDescription = null)
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(vertical = 8.dp)
                .verticalScroll(rememberScrollState())
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MigrationCard(
                state = state,
                migrateData = viewModel::migrateData,
                viewModel = viewModel
            )

            Card(modifier = Modifier.padding(horizontal = 12.dp)) {
                BasicComponent(
                    title = stringResource(RStrings.export_data),
                    startAction = {
                        Icon(
                            imageVector = Icons.Rounded.Upload,
                            contentDescription = null
                        )
                    },
                    onClick = {
                        val fileName = "pixiv_data_backup_${
                            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                                .format(LocalDateTime.Formats.ISO)
                        }"
                        exportLauncher.launch(suggestedName = fileName, defaultExtension = "zip")
                    },
                )

                BasicComponent(
                    title = stringResource(RStrings.import_data),
                    startAction = {
                        Icon(
                            imageVector = Icons.Rounded.Download,
                            contentDescription = null
                        )
                    },
                    onClick = { importLauncher.launch() },
                )

                BasicComponent(
                    title = stringResource(RStrings.clear_cache, viewModel.cacheDirSize),
                    startAction = {
                        Icon(
                            imageVector = Icons.Rounded.Delete,
                            contentDescription = null
                        )
                    },
                    onClick = rememberThrottleClick {
                        viewModel.clearCache()
                    },
                )
            }
        }

        dialogData?.let { data ->
            OverlayDialog(
                show = true,
                title = stringResource(RStrings.history_import_user_mismatch_title),
                onDismissRequest = {
                    viewModel.onHistoryImportConfirm(data.requestId, false)
                    dialogData = null
                },
                content = {
                    Column {
                        Text(
                            text = stringResource(
                                RStrings.history_import_user_mismatch_desc,
                                data.currentUserId,
                                data.importUserId
                            )
                        )
                        Spacer(Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            TextButton(
                                text = stringResource(RStrings.cancel),
                                onClick = {
                                    viewModel.onHistoryImportConfirm(data.requestId, false)
                                    dialogData = null
                                },
                                modifier = Modifier.weight(1f),
                            )
                            Spacer(Modifier.width(16.dp))
                            TextButton(
                                text = stringResource(RStrings.confirm),
                                onClick = {
                                    viewModel.onHistoryImportConfirm(data.requestId, true)
                                    dialogData = null
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.textButtonColorsPrimary(),
                            )
                        }
                    }
                },
            )
        }
    }
}

@Composable
expect fun MigrationCard(
    state: AppDataState,
    migrateData: () -> Unit,
    viewModel: AppDataViewModel,
)
