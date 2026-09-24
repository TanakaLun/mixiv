package com.mrl.pixiv.di

import com.mrl.pixiv.common.network.ApiClient
import com.mrl.pixiv.common.network.AuthClient
import com.mrl.pixiv.common.network.ImageClient
import com.mrl.pixiv.common.network.apiHttpClient
import com.mrl.pixiv.common.network.authHttpClient
import com.mrl.pixiv.common.network.imageHttpClient
import io.ktor.client.HttpClient
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Explicit registration for the three HTTP clients.
 *
 * These previously relied on KSP @Single + cross-module @ExternalDefinition bridges
 * into AndroidAppModule. After the Android-only trim those bridges were silently
 * dropped, causing NoDefinitionFoundException for named<AuthClient>() at runtime.
 */
val networkClientsModule = module {
    single<HttpClient>(qualifier = named<AuthClient>()) { authHttpClient() }
    single<HttpClient>(qualifier = named<ApiClient>(), createdAtStart = true) { apiHttpClient() }
    single<HttpClient>(qualifier = named<ImageClient>(), createdAtStart = true) { imageHttpClient() }
}
