package com.mrl.pixiv.common.datasource.local.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import com.mrl.pixiv.common.datasource.local.entity.NovelTranslationEntity

@Dao
interface NovelTranslationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: NovelTranslationEntity)

    @Query(
        "SELECT * FROM novel_translation WHERE userId = :userId AND novelId = :novelId AND targetLanguage = :targetLanguage LIMIT 1"
    )
    suspend fun getByNovelIdAndLanguage(
        userId: Long,
        novelId: Long,
        targetLanguage: String
    ): NovelTranslationEntity?

    @Query(
        "DELETE FROM novel_translation WHERE userId = :userId AND novelId = :novelId AND targetLanguage = :targetLanguage"
    )
    suspend fun deleteByNovelIdAndLanguage(
        userId: Long,
        novelId: Long,
        targetLanguage: String
    )
}
