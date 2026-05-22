package com.fantto.auralite.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** 对话的实体类，包含对话的基本信息，即id、标题、创建时间和更新时间 **/
@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long
)