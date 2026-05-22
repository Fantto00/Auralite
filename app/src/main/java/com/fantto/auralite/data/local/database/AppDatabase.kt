package com.fantto.auralite.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.fantto.auralite.data.local.dao.ConversationDao
import com.fantto.auralite.data.local.entity.ConversationEntity
import com.fantto.auralite.data.local.entity.MessageEntity

@Database(
    entities = [ConversationEntity::class, MessageEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
}