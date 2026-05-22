package com.fantto.auralite.util

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipInputStream

/** Vosk模型管理器，负责解压模型文件以及提供模型路径，防止重复解压 **/
object VoskModelManager {

    private const val MODEL_DIR_NAME = "vosk-model"
    private const val ASSETS_ZIP_NAME = "vosk-model-small-cn.zip"

    suspend fun getModelPath(context: Context): String = withContext(Dispatchers.IO) {
        val modelDir = File(context.filesDir, MODEL_DIR_NAME)

        if (!modelDir.exists() || modelDir.listFiles().isNullOrEmpty()) {
            modelDir.mkdirs()
            extractModel(context, modelDir)
        }

        modelDir.absolutePath
    }

    // 解压模型zip文件到指定目录
    private fun extractModel(context: Context, targetDir: File) {
        context.assets.open(ASSETS_ZIP_NAME).use { inputStream ->
            ZipInputStream(inputStream).use { zipInputStream ->
                var entry = zipInputStream.nextEntry
                while (entry != null) {
                    val file = File(targetDir, entry.name)

                    if (entry.isDirectory) {
                        file.mkdirs()
                    } else {
                        file.parentFile?.mkdirs()
                        FileOutputStream(file).use { outputStream ->
                            zipInputStream.copyTo(outputStream)
                        }
                    }

                    zipInputStream.closeEntry()
                    entry = zipInputStream.nextEntry
                }
            }
        }
    }

    fun isModelReady(context: Context): Boolean {
        val modelDir = File(context.filesDir, MODEL_DIR_NAME)
        return modelDir.exists() && !modelDir.listFiles().isNullOrEmpty()
    }
}