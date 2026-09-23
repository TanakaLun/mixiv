package com.mrl.pixiv.common.datasource.local.entity

import androidx.room3.Entity
import kotlinx.serialization.Serializable

@Entity(tableName = "block_illust", primaryKeys = ["illustId"])
@Serializable
data class BlockIllustEntity(
    val illustId: Long,
    val title: String = "",
)
