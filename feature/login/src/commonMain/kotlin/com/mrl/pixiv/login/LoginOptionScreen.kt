package com.mrl.pixiv.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.mrl.pixiv.common.kts.spaceBy
import com.mrl.pixiv.common.router.Destination
import com.mrl.pixiv.common.router.NavigationManager
import com.mrl.pixiv.common.router.currentNavigationManager
import com.mrl.pixiv.common.util.RDrawables
import com.mrl.pixiv.common.util.RStrings
import com.mrl.pixiv.strings.ic_launcher
import com.mrl.pixiv.strings.sign_in
import com.mrl.pixiv.strings.sign_in_with_cookie
import com.mrl.pixiv.strings.sign_in_with_token
import com.mrl.pixiv.strings.sign_up
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.FloatingActionButton
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Settings

@Composable
fun LoginOptionScreen(
    modifier: Modifier = Modifier,
    navigationManager: NavigationManager = currentNavigationManager(),
) {
    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navigationManager.navigateToNetworkSettingScreen()
                }
            ) {
                Icon(
                    imageVector = MiuixIcons.Settings,
                    contentDescription = null
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .padding(start = 16.dp, top = 75.dp, end = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = 10f.spaceBy,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(RDrawables.ic_launcher),
                contentDescription = null,
                modifier = Modifier
                    .size(75.dp)
                    .clip(CircleShape)
            )
            Button(
                onClick = {
                    navigationManager.navigate(Destination.Login(generateWebViewUrl(false)))
                },
                colors = ButtonDefaults.buttonColorsPrimary(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = stringResource(RStrings.sign_in)
                )
            }
            Button(
                onClick = {
                    navigationManager.navigate(Destination.Login(generateWebViewUrl(true)))
                },
                colors = ButtonDefaults.buttonColorsPrimary(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = stringResource(RStrings.sign_up)
                )
            }
            Button(
                onClick = {
                    navigationManager.navigate(Destination.OAuthLogin)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(RStrings.sign_in_with_token)
                )
            }
            Button(
                onClick = {
                    navigationManager.navigate(Destination.WebCookieLogin)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(RStrings.sign_in_with_cookie)
                )
            }
        }
    }
}
