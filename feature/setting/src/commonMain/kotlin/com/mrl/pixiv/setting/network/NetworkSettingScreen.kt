package com.mrl.pixiv.setting.network

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mrl.pixiv.common.compose.LocalToaster
import com.mrl.pixiv.common.compose.ui.pageScrollModifiers
import com.mrl.pixiv.common.repository.requireUserPreferenceFlow
import com.mrl.pixiv.common.router.NavigationManager
import com.mrl.pixiv.common.router.currentNavigationManager
import com.mrl.pixiv.common.util.RStrings
import com.mrl.pixiv.setting.SettingViewModel
import com.mrl.pixiv.setting.network.components.BypassSettingEditor
import com.mrl.pixiv.setting.network.components.PictureSourceWidget
import com.mrl.pixiv.strings.network_setting
import com.mrl.pixiv.strings.restart_app_to_take_effect
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back

@Composable
fun NetworkSettingScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingViewModel = koinViewModel(),
    navigationManager: NavigationManager = currentNavigationManager(),
) {
    val userPreference by requireUserPreferenceFlow.collectAsStateWithLifecycle()
    val toaster = LocalToaster.current
    LocalNetworkPermissionEffect(userPreference.bypassSetting)

    val scrollBehavior = MiuixScrollBehavior()
    Scaffold(
        modifier = modifier.pageScrollModifiers(scrollBehavior),
        topBar = {
            TopAppBar(
                title = stringResource(RStrings.network_setting),
                navigationIcon = {
                    IconButton(onClick = navigationManager::popBackStack) {
                        Icon(MiuixIcons.Back, contentDescription = null)
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(it)
                .imePadding()
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Card(modifier = Modifier.padding(horizontal = 12.dp)) {
                BypassSettingEditor(
                    bypassSetting = userPreference.bypassSetting,
                    onUpdate = { setting -> viewModel.updateBypassSetting(setting) },
                )
            }

            Card(modifier = Modifier.padding(horizontal = 12.dp)) {
                PictureSourceWidget(
                    currentSelected = userPreference.imageHost,
                    savePictureSourceHost = { host ->
                        viewModel.savePictureSourceHost(host)
                        toaster.show(RStrings.restart_app_to_take_effect)
                    },
                )
            }
        }
    }
}
