package com.mrl.pixiv.common.datasource.local

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import com.mrl.pixiv.common.datasource.local.entity.DownloadEntity
import com.mrl.pixiv.common.datasource.local.entity.NovelHistoryEntity
import com.mrl.pixiv.common.datasource.local.entity.NovelTranslationEntity
import java.io.File
import java.nio.file.Files
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class Room2DatabaseCompatibilityTest {
    @Test
    fun `room 3 opens room 2 version 9 without losing data and persists new writes`() = runTest {
        withLegacyDatabase(version = 9) { file ->
            withRoomDatabase(file) { database ->
                assertEquals(
                    legacyTranslation,
                    database.novelTranslationDao().getByNovelIdAndLanguage(1L, 10L, "en"),
                )
                assertLegacyDownloadAndHistory(database)

                database.novelTranslationDao().upsert(
                    legacyTranslation.copy(translatedText = "Updated with Room 3", updatedAtMillis = 200L)
                )
                database.downloadDao().updateProgress(20L, 0, 0.75f)
                database.browsingHistoryDao().upsertNovel(legacyHistory.copy(novelId = 11L))
            }

            // Reopen the same on-disk file to check that Room 3 writes are durable.
            withRoomDatabase(file) { database ->
                assertEquals(
                    legacyTranslation.copy(translatedText = "Updated with Room 3", updatedAtMillis = 200L),
                    database.novelTranslationDao().getByNovelIdAndLanguage(1L, 10L, "en"),
                )
                assertEquals(
                    legacyDownload.copy(progress = 0.75f),
                    database.downloadDao().getDownload(20L, 0),
                )
                assertEquals(
                    setOf(10L, 11L),
                    database.browsingHistoryDao().getAllNovels(1L).map { it.novelId }.toSet(),
                )
            }
            assertDatabaseVersion(file, 9L)
        }
    }

    @Test
    fun `room 3 automatically migrates version 8 and retains existing data`() = runTest {
        withLegacyDatabase(version = 8) { file ->
            withRoomDatabase(file) { database ->
                // Reading through the production builder must execute MIGRATION_8_9 and
                // validate the entire schema; the test never invokes migrate() directly.
                val migratedTranslation = assertNotNull(
                    database.novelTranslationDao().getByNovelIdAndLanguage(1L, 10L, "en")
                )
                assertEquals(
                    legacyTranslation.copy(translatedTitle = "", translatedCaption = "", metadataSourceMd5 = ""),
                    migratedTranslation,
                )
                assertLegacyDownloadAndHistory(database)

                // Exercise the new columns after the registered migration has added them.
                database.novelTranslationDao().upsert(legacyTranslation)
            }

            withRoomDatabase(file) { database ->
                assertEquals(
                    legacyTranslation,
                    database.novelTranslationDao().getByNovelIdAndLanguage(1L, 10L, "en"),
                )
                assertLegacyDownloadAndHistory(database)
            }
            assertDatabaseVersion(file, 9L)
        }
    }

    private suspend fun assertLegacyDownloadAndHistory(database: PixivDatabase) {
        assertEquals(legacyDownload, database.downloadDao().getDownload(20L, 0))
        assertEquals(listOf(legacyHistory), database.browsingHistoryDao().getAllNovels(1L))
    }

    private suspend fun withLegacyDatabase(version: Int, block: suspend (File) -> Unit) {
        val directory = Files.createTempDirectory("pixiv-room2-compatibility-").toFile()
        try {
            val file = File(directory, "pixiv_db")
            val schema = checkNotNull(javaClass.getResource("/room2/pixiv-v9.sql")) {
                "Missing frozen Room 2 schema fixture"
            }.readText()
            val connection = BundledSQLiteDriver().open(file.absolutePath)
            try {
                // The fixture intentionally has one complete SQL statement per non-comment line.
                schema.lineSequence()
                    .map(String::trim)
                    .filter { it.isNotEmpty() && !it.startsWith("--") }
                    .forEach(connection::execSQL)
                connection.prepare("SELECT identity_hash FROM room_master_table WHERE id = 42").use {
                    assertTrue(it.step())
                    assertEquals("115367479a96f680e52d2ee7d6dc6421", it.getText(0))
                }
                if (version == 8) {
                    // Reconstruct the version 8 migration precondition from the frozen
                    // Room 2 schema. Only MIGRATION_8_9's three added columns differ.
                    // The historical version 8 identity hash is unknown, so omit it
                    // rather than attribute version 9's hash to a version 8 database.
                    connection.execSQL("ALTER TABLE novel_translation DROP COLUMN translatedTitle")
                    connection.execSQL("ALTER TABLE novel_translation DROP COLUMN translatedCaption")
                    connection.execSQL("ALTER TABLE novel_translation DROP COLUMN metadataSourceMd5")
                    connection.execSQL("DROP TABLE room_master_table")
                    connection.execSQL("PRAGMA user_version = 8")
                }
                connection.execSQL(
                    """
                    INSERT INTO novel_translation (
                        novelId, userId, targetLanguage, provider, model, configFingerprint,
                        sourceMd5, translatedText, updatedAtMillis
                    ) VALUES (10, 1, 'en', 'OPENAI', 'legacy-model', 'legacy-config',
                        'body-md5', 'Saved translation', 100)
                    """.trimIndent()
                )
                if (version == 9) {
                    connection.execSQL(
                        """
                        UPDATE novel_translation SET translatedTitle = 'Saved title',
                            translatedCaption = 'Saved caption', metadataSourceMd5 = 'metadata-md5'
                        WHERE novelId = 10 AND userId = 1 AND targetLanguage = 'en'
                        """.trimIndent()
                    )
                }
                connection.execSQL(
                    """
                    INSERT INTO download (
                        illustId, `index`, title, userId, userName, thumbnailUrl, originalUrl,
                        subFolder, status, progress, filePath, fileUri, createTime
                    ) VALUES (20, 0, 'Saved download', 2, 'Artist', 'https://example.com/thumb.jpg',
                        'https://example.com/original.jpg', NULL, 1, 0.25, '/saved/image.jpg',
                        'file:///saved/image.jpg', 90)
                    """.trimIndent()
                )
                connection.execSQL(
                    """
                    INSERT INTO browsing_history_novel (novelId, userId, viewedAtMillis, novelJson)
                    VALUES (10, 1, 80, '{"id":10,"title":"Saved novel"}')
                    """.trimIndent()
                )
            } finally {
                connection.close()
            }
            assertDatabaseVersion(file, version.toLong())
            block(file)
        } finally {
            directory.deleteRecursively()
        }
    }

    private suspend fun withRoomDatabase(file: File, block: suspend (PixivDatabase) -> Unit) {
        val database = provideDatabase(Room.databaseBuilder<PixivDatabase>(name = file.absolutePath))
        try {
            block(database)
        } finally {
            database.close()
        }
    }

    private fun assertDatabaseVersion(file: File, expected: Long) {
        val connection = BundledSQLiteDriver().open(file.absolutePath)
        try {
            connection.prepare("PRAGMA user_version").use {
                assertTrue(it.step())
                assertEquals(expected, it.getLong(0))
            }
        } finally {
            connection.close()
        }
    }

    private val legacyTranslation = NovelTranslationEntity(
        novelId = 10L,
        userId = 1L,
        targetLanguage = "en",
        provider = "OPENAI",
        model = "legacy-model",
        configFingerprint = "legacy-config",
        sourceMd5 = "body-md5",
        translatedText = "Saved translation",
        updatedAtMillis = 100L,
        translatedTitle = "Saved title",
        translatedCaption = "Saved caption",
        metadataSourceMd5 = "metadata-md5",
    )

    private val legacyDownload = DownloadEntity(
        illustId = 20L,
        index = 0,
        title = "Saved download",
        userId = 2L,
        userName = "Artist",
        thumbnailUrl = "https://example.com/thumb.jpg",
        originalUrl = "https://example.com/original.jpg",
        status = 1,
        progress = 0.25f,
        filePath = "/saved/image.jpg",
        fileUri = "file:///saved/image.jpg",
        createTime = 90L,
    )

    private val legacyHistory = NovelHistoryEntity(
        novelId = 10L,
        userId = 1L,
        viewedAtMillis = 80L,
        novelJson = """{"id":10,"title":"Saved novel"}""",
    )
}
