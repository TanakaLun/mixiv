package com.mrl.pixiv.login.oauth

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import be.digitalia.compose.htmlconverter.htmlToAnnotatedString
import com.mrl.pixiv.common.kts.spaceBy
import com.mrl.pixiv.common.router.NavigationManager
import com.mrl.pixiv.common.router.currentNavigationManager
import com.mrl.pixiv.common.util.RStrings
import com.mrl.pixiv.common.viewmodel.asState
import com.mrl.pixiv.strings.cookies
import com.mrl.pixiv.strings.recommend_cookie_export_plugin
import com.mrl.pixiv.strings.sign_in
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back

@Composable
fun WebCookieLoginScreen(
    modifier: Modifier = Modifier,
    viewModel: OAuthLoginViewModel = koinViewModel(),
    navigationManager: NavigationManager = currentNavigationManager(),
) {
    var phpSessionId by remember { mutableStateOf("") }
    val state = viewModel.asState()
    val focusManager = LocalFocusManager.current
    LaunchedEffect(state.isLogin) {
        if (state.isLogin) {
            navigationManager.loginToMainScreen()
        }
    }
    Scaffold(
        modifier = modifier,
        topBar = {
            SmallTopAppBar(
                title = "",
                navigationIcon = {
                    IconButton(
                        onClick = { navigationManager.popBackStack() },
                    ) {
                        Icon(
                            imageVector = MiuixIcons.Back,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) {
        Box(
            modifier = Modifier
                .padding(it)
                .imePadding()
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxSize(),
                verticalArrangement = 10f.spaceBy
            ) {
                TextField(
                    value = phpSessionId,
                    onValueChange = { phpSessionId = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = stringResource(RStrings.cookies),
                    maxLines = 5
                )
                Button(
                    onClick = click@{
                        if (state.loading || phpSessionId.isEmpty()) return@click
                        focusManager.clearFocus()
                        viewModel.loginWithCookies(phpSessionId)
                    },
                    colors = ButtonDefaults.buttonColorsPrimary(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(RStrings.sign_in)
                    )
                }
                Text(
                    text = htmlToAnnotatedString(stringResource(RStrings.recommend_cookie_export_plugin)),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
            if (state.loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}
