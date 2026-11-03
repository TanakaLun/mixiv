package com.mrl.pixiv.setting.network.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mrl.pixiv.common.data.setting.UserPreference
import com.mrl.pixiv.common.util.RStrings
import com.mrl.pixiv.common.util.isIOS
import com.mrl.pixiv.common.util.platform
import com.mrl.pixiv.strings.cancel
import com.mrl.pixiv.strings.ech_doh_url
import com.mrl.pixiv.strings.network_advanced_settings
import com.mrl.pixiv.strings.network_advanced_settings_desc
import com.mrl.pixiv.strings.network_plan
import com.mrl.pixiv.strings.protocol_http
import com.mrl.pixiv.strings.protocol_socks
import com.mrl.pixiv.strings.proxy_host
import com.mrl.pixiv.strings.proxy_port
import com.mrl.pixiv.strings.proxy_type
import com.mrl.pixiv.strings.use_ech
import com.mrl.pixiv.strings.use_ech_desc
import com.mrl.pixiv.strings.use_none
import com.mrl.pixiv.strings.use_none_desc
import com.mrl.pixiv.strings.use_proxy
import com.mrl.pixiv.strings.use_proxy_desc
import com.mrl.pixiv.strings.use_system_proxy
import com.mrl.pixiv.strings.use_system_proxy_desc
import io.ktor.http.URLProtocol
import io.ktor.http.parseUrl
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.RadioButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.ExpandLess
import top.yukonga.miuix.kmp.icon.extended.ExpandMore
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.OverlayDropdownPreference

/**
 * IPv4：0.0.0.0 ~ 255.255.255.255
 */
private val IPV4 = Regex(
    pattern = """^((25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)\.){3}(25[0-5]|2[0-4]\d|1\d\d|[1-9]?\d)$"""
)

/**
 * 域名（ASCII，不含协议、不含端口）：
 * - 标准域名：至少一个点，TLD 2~63 字母
 * - 或者：单段主机名（无点），1~63，规则同 label（不以 - 开头/结尾）
 * - 总长度 1~253（正则里不好精确卡死，这里更偏实用）
 */
private val DOMAIN = Regex(
    pattern = """^(?=.{1,253}$)((?:[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?\.)+[A-Za-z]{2,63}|[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?)$"""
)

/**
 * “合法 IP 或域名”（不含端口）
 */
private val HOST = Regex(
    pattern = """^(?:${IPV4.pattern.drop(1).dropLast(1)}|${DOMAIN.pattern.drop(1).dropLast(1)})$"""
)

@Composable
fun BypassSettingEditor(
    bypassSetting: UserPreference.BypassSetting,
    onUpdate: (UserPreference.BypassSetting) -> Unit,
    modifier: Modifier = Modifier
) {
    var advancedExpanded by rememberSaveable { mutableStateOf(false) }
    var showEditDoh by rememberSaveable { mutableStateOf(false) }

    if (showEditDoh && bypassSetting is UserPreference.BypassSetting.ECH) {
        EchDohEditDialog(
            setting = bypassSetting,
            onUpdate = onUpdate,
            onDismiss = { showEditDoh = false },
        )
    }

    val types = remember {
        listOfNotNull(
            UserPreference.BypassSetting.System,
            UserPreference.BypassSetting.Direct,
            UserPreference.BypassSetting.Proxy(),
            if (platform.isIOS()) null else UserPreference.BypassSetting.ECH()
        )
    }
    val selectedIndex = types.indexOfFirst { type ->
        when (bypassSetting) {
            UserPreference.BypassSetting.System -> type == UserPreference.BypassSetting.System
            UserPreference.BypassSetting.Direct -> type == UserPreference.BypassSetting.Direct
            is UserPreference.BypassSetting.Proxy -> type is UserPreference.BypassSetting.Proxy
            is UserPreference.BypassSetting.ECH -> type is UserPreference.BypassSetting.ECH
        }
    }.coerceAtLeast(0)
    val labels = types.map { type ->
        stringResource(
            when (type) {
                UserPreference.BypassSetting.System -> RStrings.use_system_proxy
                UserPreference.BypassSetting.Direct -> RStrings.use_none
                is UserPreference.BypassSetting.Proxy -> RStrings.use_proxy
                is UserPreference.BypassSetting.ECH -> RStrings.use_ech
            }
        )
    }
    val description = stringResource(
        when (bypassSetting) {
            UserPreference.BypassSetting.System -> RStrings.use_system_proxy_desc
            UserPreference.BypassSetting.Direct -> RStrings.use_none_desc
            is UserPreference.BypassSetting.Proxy -> RStrings.use_proxy_desc
            is UserPreference.BypassSetting.ECH -> RStrings.use_ech_desc
        }
    )

    Column(modifier = modifier) {
        OverlayDropdownPreference(
            items = labels,
            selectedIndex = selectedIndex,
            title = stringResource(RStrings.network_plan),
            summary = description,
            onSelectedIndexChange = { index ->
                val type = types.getOrNull(index) ?: return@OverlayDropdownPreference
                val selected = when (bypassSetting) {
                    UserPreference.BypassSetting.System -> type == UserPreference.BypassSetting.System
                    UserPreference.BypassSetting.Direct -> type == UserPreference.BypassSetting.Direct
                    is UserPreference.BypassSetting.Proxy -> type is UserPreference.BypassSetting.Proxy
                    is UserPreference.BypassSetting.ECH -> type is UserPreference.BypassSetting.ECH
                }
                if (!selected) {
                    onUpdate(type)
                }
            },
        )

        AnimatedContent(
            targetState = bypassSetting::class,
            transitionSpec = {
                slideInVertically { -it / 3 } + expandVertically(expandFrom = Alignment.Top) + fadeIn() togetherWith
                    slideOutVertically { -it / 3 } + shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut()
            },
            label = "bypass_setting_content"
        ) {
            when (bypassSetting) {
                UserPreference.BypassSetting.System, UserPreference.BypassSetting.Direct -> {}
                is UserPreference.BypassSetting.Proxy -> {
                    ProxyEditor(bypassSetting, onUpdate)
                }

                is UserPreference.BypassSetting.ECH -> {
                    EchEditor(
                        dohUrl = bypassSetting.url,
                        advancedExpanded = advancedExpanded,
                        onToggleAdvanced = { advancedExpanded = !advancedExpanded },
                        onEditDoh = { showEditDoh = true },
                    )
                }
            }
        }
    }
}

@Composable
fun ProxyEditor(
    setting: UserPreference.BypassSetting.Proxy,
    onUpdate: (UserPreference.BypassSetting) -> Unit
) {
    var showEditHost by remember { mutableStateOf(false) }
    var showEditPort by remember { mutableStateOf(false) }
    var showEditType by remember { mutableStateOf(false) }

    if (showEditHost) {
        EditDialog(
            title = stringResource(RStrings.proxy_host),
            initialValue = setting.host,
            onConfirm = {
                onUpdate(setting.copy(host = it))
                showEditHost = false
            },
            onDismiss = { showEditHost = false },
            isValid = { it.matches(HOST) }
        )
    }

    if (showEditPort) {
        EditDialog(
            title = stringResource(RStrings.proxy_port),
            initialValue = if (setting.port == 0) "" else setting.port.toString(),
            onConfirm = {
                val port = it.filter { char -> char.isDigit() }.toIntOrNull() ?: 0
                onUpdate(setting.copy(port = port))
                showEditPort = false
            },
            onDismiss = { showEditPort = false },
            isValid = { it.toIntOrNull() != null && it.toInt() in 0..65535 },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
    }

    if (showEditType) {
        OverlayDialog(
            show = true,
            title = stringResource(RStrings.proxy_type),
            onDismissRequest = { showEditType = false },
            content = {
                Column {
                    val types = remember { UserPreference.BypassSetting.Proxy.ProxyType.entries }
                    types.forEach { type ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onUpdate(setting.copy(proxyType = type))
                                    showEditType = false
                                }
                                .padding(vertical = 12.dp)
                        ) {
                            RadioButton(
                                selected = setting.proxyType == type,
                                onClick = null
                            )
                            Text(
                                text = stringResource(
                                    when (type) {
                                        UserPreference.BypassSetting.Proxy.ProxyType.HTTP -> RStrings.protocol_http
                                        UserPreference.BypassSetting.Proxy.ProxyType.SOCKS -> RStrings.protocol_socks
                                    }
                                ),
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    TextButton(
                        text = stringResource(RStrings.cancel),
                        onClick = { showEditType = false },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
        )
    }

    Column {
        ArrowPreference(
            title = stringResource(RStrings.proxy_host),
            summary = setting.host,
            onClick = { showEditHost = true },
        )
        ArrowPreference(
            title = stringResource(RStrings.proxy_port),
            summary = setting.port.toString(),
            onClick = { showEditPort = true },
        )
        ArrowPreference(
            title = stringResource(RStrings.proxy_type),
            summary = stringResource(
                when (setting.proxyType) {
                    UserPreference.BypassSetting.Proxy.ProxyType.HTTP -> RStrings.protocol_http
                    UserPreference.BypassSetting.Proxy.ProxyType.SOCKS -> RStrings.protocol_socks
                }
            ),
            onClick = { showEditType = true },
        )
    }
}

@Composable
private fun EchEditor(
    dohUrl: String,
    advancedExpanded: Boolean,
    onToggleAdvanced: () -> Unit,
    onEditDoh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        ArrowPreference(
            title = stringResource(RStrings.network_advanced_settings),
            summary = stringResource(RStrings.network_advanced_settings_desc),
            endActions = {
                Icon(
                    imageVector = if (advancedExpanded) MiuixIcons.ExpandLess else MiuixIcons.ExpandMore,
                    contentDescription = null,
                )
            },
            onClick = onToggleAdvanced,
        )
        AnimatedVisibility(visible = advancedExpanded) {
            ArrowPreference(
                title = stringResource(RStrings.ech_doh_url),
                summary = dohUrl,
                onClick = onEditDoh,
            )
        }
    }
}

@Composable
internal fun EchDohEditDialog(
    setting: UserPreference.BypassSetting.ECH,
    onUpdate: (UserPreference.BypassSetting) -> Unit,
    onDismiss: () -> Unit,
) {
    EditDialog(
        title = stringResource(RStrings.ech_doh_url),
        initialValue = setting.url,
        onConfirm = {
            onUpdate(setting.copy(url = it.trim()))
            onDismiss()
        },
        isValid = {
            parseUrl(it.trim())?.let { url ->
                url.protocol == URLProtocol.HTTPS && url.host.isNotBlank()
            } ?: false
        },
        onDismiss = onDismiss,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
    )
}
