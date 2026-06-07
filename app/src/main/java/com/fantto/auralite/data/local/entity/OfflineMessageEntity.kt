package com.fantto.auralite.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "offline_messages")
data class OfflineMessageEntity(
    @PrimaryKey val id: String,
    val content: String,
    val createdAt: Long
)
