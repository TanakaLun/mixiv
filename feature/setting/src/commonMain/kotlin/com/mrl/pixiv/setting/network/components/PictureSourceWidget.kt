package com.mrl.pixiv.setting.network.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.mrl.pixiv.common.data.Constants.IMAGE_HOST
import com.mrl.pixiv.common.util.RStrings
import com.mrl.pixiv.strings.image_source
import com.mrl.pixiv.strings.label_default
import com.mrl.pixiv.strings.self_defined_source
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.DropdownEntry
import top.yukonga.miuix.kmp.basic.DropdownItem
import top.yukonga.miuix.kmp.preference.OverlayDropdownPreference

@Composable
fun PictureSourceWidget(
    modifier: Modifier = Modifier,
    currentSelected: String = "",
    savePictureSourceHost: (String) -> Unit = {}
) {
    val sources = mapOf(
        IMAGE_HOST to "${stringResource(RStrings.label_default)}: $IMAGE_HOST",
        "i.pixiv.cat" to "i.pixiv.cat",
        "i.pixiv.re" to "i.pixiv.re"
    )
    val selectedHost = currentSelected.ifEmpty { IMAGE_HOST }
    val isCustomSource = selectedHost !in sources
    var showCustomSourceDialog by remember { mutableStateOf(false) }
    val selfDefinedLabel = stringResource(RStrings.self_defined_source)

    if (showCustomSourceDialog) {
        EditDialog(
            title = selfDefinedLabel,
            initialValue = if (isCustomSource) selectedHost else "",
            onConfirm = { host ->
                savePictureSourceHost(host.trim())
                showCustomSourceDialog = false
            },
            onDismiss = { showCustomSourceDialog = false },
            isValid = { it.isNotBlank() },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
        )
    }

    val entry = remember(sources, selectedHost, isCustomSource, selfDefinedLabel) {
        DropdownEntry(
            buildList {
                sources.forEach { (host, label) ->
                    add(
                        DropdownItem(
                            text = label,
                            selected = host == selectedHost,
                            onClick = { savePictureSourceHost(host) },
                        )
                    )
                }
                add(
                    DropdownItem(
                        text = selfDefinedLabel,
                        selected = isCustomSource,
                        onClick = { showCustomSourceDialog = true },
                    )
                )
            }
        )
    }

    OverlayDropdownPreference(
        entry = entry,
        title = stringResource(RStrings.image_source),
        modifier = modifier,
        summary = sources[selectedHost] ?: selectedHost,
    )
}
