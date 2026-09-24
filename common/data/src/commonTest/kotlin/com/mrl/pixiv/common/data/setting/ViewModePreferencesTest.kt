package com.mrl.pixiv.common.data.setting

import com.mrl.pixiv.common.data.AppViewMode
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class ViewModePreferencesTest {
    @Test
    fun oldPreferencesKeepIndependentDefaults() {
        val restored = Json.decodeFromString<UserPreference>("""{"appViewMode":"novel"}""")
        assertEquals(AppViewMode.NOVEL, restored.appViewMode)
        assertEquals(AppViewMode.ILLUST, restored.collectionViewMode)
        assertEquals(AppViewMode.ILLUST, restored.historyViewMode)
    }

    @Test
    fun collectionAndHistorySurviveSerializationIndependently() {
        for (collection in AppViewMode.entries) {
            for (history in AppViewMode.entries) {
                val saved = UserPreference(collectionViewMode = collection, historyViewMode = history)
                val restored = Json.decodeFromString<UserPreference>(Json.encodeToString(saved))
                assertEquals(collection, restored.collectionViewMode)
                assertEquals(history, restored.historyViewMode)
                assertEquals(AppViewMode.ILLUST, restored.appViewMode)
            }
        }
    }
}
