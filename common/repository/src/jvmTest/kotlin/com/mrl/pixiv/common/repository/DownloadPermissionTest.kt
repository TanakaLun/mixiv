package com.mrl.pixiv.common.repository

import com.mrl.pixiv.common.datasource.local.dao.DownloadDao
import com.mrl.pixiv.common.util.PictureType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.test.runTest
import java.lang.reflect.Proxy
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DownloadPermissionTest {
    @Test
    fun deniedPermissionDoesNotCreateAQueuedDownloadAndReleasesTheWaitingCaller() = runTest {
        val dao = Proxy.newProxyInstance(
            DownloadDao::class.java.classLoader,
            arrayOf(DownloadDao::class.java),
        ) { _, method, _ -> error("Storage was denied, but DAO.${method.name} was called") } as DownloadDao
        var notified = false
        val manager = DownloadManager(dao, DeniedDownloadStrategy)
        val enqueued = manager.enqueueDownload(1, 0, "title", 1, "author", "preview", "original") {
            assertTrue(it == null)
            notified = true
        }
        assertFalse(enqueued)
        assertTrue(notified)
    }

    private object DeniedDownloadStrategy : DownloadStrategy {
        override val downloadFolder = ""
        override suspend fun prepareDownload() = false
        override suspend fun enqueue(illustId: Long, index: Int, url: String, subFolder: String?) {
            error("Denied download must not start")
        }
        override suspend fun cancel(illustId: Long, index: Int) = Unit
        override suspend fun cancelAll() = Unit
        override fun getDownloadState(illustId: Long, index: Int): Flow<DownloadState> = error("No work queued")
        override suspend fun getExistingFileInfo(
            illustId: Long, index: Int, fileName: String, type: PictureType, subFolder: String?,
        ): Pair<String, String>? = error("No storage access")
    }
}
