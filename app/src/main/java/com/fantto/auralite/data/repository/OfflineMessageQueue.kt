package com.fantto.auralite.data.repository

import com.elvishew.xlog.XLog
import com.fantto.auralite.data.local.dao.ConversationDao
import com.fantto.auralite.data.local.entity.OfflineMessageEntity
import java.util.UUID

class OfflineMessageQueue(
    private val conversationDao: ConversationDao
) {

    suspend fun enqueue(content: String) {
        val entity = OfflineMessageEntity(
            id = UUID.randomUUID().toString(),
            content = content,
            createdAt = System.currentTimeMillis()
        )
        conversationDao.insertOfflineMessage(entity)
        XLog.d("XLog OfflineMessageQueue：消息已入队，id=${entity.id}")
    }

    suspend fun peekAll(): List<OfflineMessageEntity> {
        return conversationDao.getAllOfflineMessages()
    }

    suspend fun remove(id: String) {
        conversationDao.deleteOfflineMessageById(id)
        XLog.d("XLog OfflineMessageQueue：消息已移除，id=$id")
    }

    suspend fun clear() {
        conversationDao.clearOfflineMessages()
        XLog.d("XLog OfflineMessageQueue：队列已清空")
    }

    suspend fun size(): Int {
        return conversationDao.getOfflineMessageCount()
    }
}
