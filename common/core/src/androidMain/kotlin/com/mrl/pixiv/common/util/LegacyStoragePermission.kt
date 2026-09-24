package com.mrl.pixiv.common.util

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** 权限申请由 Activity 管理；并发下载共用同一次权限申请。 */
object LegacyStoragePermission {
    private var launcher: ActivityResultLauncher<String>? = null
    private var pending: CompletableDeferred<Boolean>? = null

    fun bind(launcher: ActivityResultLauncher<String>) {
        this.launcher = launcher
    }

    fun unbind(launcher: ActivityResultLauncher<String>, changingConfigurations: Boolean) {
        if (this.launcher === launcher) this.launcher = null
        if (!changingConfigurations) onResult(false)
    }

    fun onResult(granted: Boolean) {
        pending?.complete(granted)
        pending = null
    }

    suspend fun request(): Boolean = withContext(Dispatchers.Main.immediate) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ||
            ContextCompat.checkSelfPermission(AppUtil.appContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
            PackageManager.PERMISSION_GRANTED
        ) return@withContext true

        pending?.let { return@withContext it.await() }
        val launcher = launcher ?: return@withContext false
        val result = CompletableDeferred<Boolean>()
        pending = result
        try {
            launcher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } catch (_: IllegalStateException) {
            onResult(false)
        }
        result.await()
    }
}
