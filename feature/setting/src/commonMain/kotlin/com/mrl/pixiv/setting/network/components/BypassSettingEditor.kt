package com.mrl.pixiv.setting.network.components

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mrl.pixiv.common.data.setting.UserPreference
import com.mrl.pixiv.common.kts.spaceBy
import com.mrl.pixiv.common.serialize.fromJson
import com.mrl.pixiv.common.serialize.toJson
import com.mrl.pixiv.common.util.RStrings
import com.mrl.pixiv.common.util.isIOS
import com.mrl.pixiv.common.util.platform
import com.mrl.pixiv.strings.cancel
import com.mrl.pixiv.strings.internal_ip_pool
import com.mrl.pixiv.strings.internal_ip_pool_desc
import com.mrl.pixiv.strings.network_plan
import com.mrl.pixiv.strings.protocol_http
import com.mrl.pixiv.strings.protocol_socks
import com.mrl.pixiv.strings.proxy_host
import com.mrl.pixiv.strings.proxy_port
import com.mrl.pixiv.strings.proxy_type
import com.mrl.pixiv.strings.sni_doh_url
import com.mrl.pixiv.strings.sni_non_strict_ssl
import com.mrl.pixiv.strings.sni_timeout
import com.mrl.pixiv.strings.use_none
import com.mrl.pixiv.strings.use_none_desc
import com.mrl.pixiv.strings.use_proxy
import com.mrl.pixiv.strings.use_proxy_desc
import com.mrl.pixiv.strings.use_sni
import com.mrl.pixiv.strings.use_sni_desc
import io.ktor.http.URLProtocol
import io.ktor.http.parseUrl
import org.jetbrains.compose.resources.stringResource
import androidx.compose.material3.FilterChip
import top.yukonga.miuix.kmp.basic.RadioButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme

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
    Column(modifier = modifier) {
        Text(
            text = stringResource(RStrings.network_plan),
            style = MiuixTheme.textStyles.subtitle,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = 8.spaceBy
        ) {
            val types = remember {
                listOfNotNull(
                    UserPreference.BypassSetting.None,
                    UserPreference.BypassSetting.Proxy(),
                    if (platform.isIOS()) null else UserPreference.BypassSetting.SNI()
                )
            }

            types.forEach { type ->
                val selected = when (bypassSetting) {
                    is UserPreference.BypassSetting.None -> type is UserPreference.BypassSetting.None
                    is UserPreference.BypassSetting.Proxy -> type is UserPreference.BypassSetting.Proxy
                    is UserPreference.BypassSetting.SNI -> type is UserPreference.BypassSetting.SNI
                }

                FilterChip(
                    selected = selected,
                    onClick = {
                        if (!selected) {
                            onUpdate(type)
                        }
                    },
                    label = {
                        Text(
                            text = stringResource(
                                when (type) {
                                    is UserPreference.BypassSetting.None -> RStrings.use_none
                                    is UserPreference.BypassSetting.Proxy -> RStrings.use_proxy
                                    is UserPreference.BypassSetting.SNI -> RStrings.use_sni
                                }
                            )
                        )
                    }
                )
            }
        }

        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            AnimatedContent(
                targetState = bypassSetting::class,
                transitionSpec = { slideInVertically() togetherWith slideOutVertically() },
                label = "bypass_setting_content"
            ) {
                when (bypassSetting) {
                    is UserPreference.BypassSetting.None -> {}
                    is UserPreference.BypassSetting.Proxy -> {
                        ProxyEditor(bypassSetting, onUpdate)
                    }

                    is UserPreference.BypassSetting.SNI -> {
                        SniEditor(bypassSetting, onUpdate)
                    }
                }
            }
            Text(
                text = stringResource(
                    when (bypassSetting) {
                        is UserPreference.BypassSetting.None -> RStrings.use_none_desc
                        is UserPreference.BypassSetting.Proxy -> RStrings.use_proxy_desc
                        is UserPreference.BypassSetting.SNI -> RStrings.use_sni_desc
                    }
                ),
                modifier = Modifier.padding(start = 16.dp),
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                style = MiuixTheme.textStyles.body1,
            )
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
fun SniEditor(
    setting: UserPreference.BypassSetting.SNI,
    onUpdate: (UserPreference.BypassSetting) -> Unit
) {
    var showEditUrl by remember { mutableStateOf(false) }
    var showEditTimeout by remember { mutableStateOf(false) }
    var showEditInternalIpPool by remember { mutableStateOf(false) }


    if (showEditUrl) {
        EditDialog(
            title = stringResource(RStrings.sni_doh_url),
            initialValue = setting.url,
            onConfirm = {
                onUpdate(setting.copy(url = it))
                showEditUrl = false
            },
            isValid = {
                parseUrl(it)?.let {
                    it.protocol == URLProtocol.HTTPS
                } ?: false
            },
            onDismiss = { showEditUrl = false }
        )
    }

    if (showEditTimeout) {
        EditDialog(
            title = stringResource(RStrings.sni_timeout),
            initialValue = setting.dohTimeout.toString(),
            onConfirm = {
                val timeout = it.filter { char -> char.isDigit() }.toIntOrNull() ?: 5
                onUpdate(setting.copy(dohTimeout = timeout))
                showEditTimeout = false
            },
            onDismiss = { showEditTimeout = false },
            isValid = { it.toIntOrNull() != null },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
    }

    if (showEditInternalIpPool) {
        EditDialog(
            title = stringResource(RStrings.internal_ip_pool),
            initialValue = setting.fallback.toJson(),
            onConfirm = onConfirm@{
                val fallback = try {
                    it.fromJson<Map<String, String>>()
                } catch (_: Exception) {
                    showEditInternalIpPool = false
                    return@onConfirm
                }
                onUpdate(setting.copy(fallback = fallback))
                showEditInternalIpPool = false
            },
            onDismiss = { showEditInternalIpPool = false },
            isValid = {
                runCatching {
                    it.fromJson<Map<String, String>>()
                    true
                }.getOrElse {
                    false
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
        )
    }

    Column {
        ArrowPreference(
            title = stringResource(RStrings.sni_doh_url),
            summary = setting.url,
            onClick = { showEditUrl = true },
        )
        ArrowPreference(
            title = stringResource(RStrings.sni_timeout),
            summary = setting.dohTimeout.toString(),
            onClick = { showEditTimeout = true },
        )
        SwitchPreference(
            checked = setting.nonStrictSSL,
            onCheckedChange = { onUpdate(setting.copy(nonStrictSSL = it)) },
            title = stringResource(RStrings.sni_non_strict_ssl),
        )
        ArrowPreference(
            title = stringResource(RStrings.internal_ip_pool),
            summary = stringResource(RStrings.internal_ip_pool_desc),
            onClick = { showEditInternalIpPool = true },
        )
    }
}
