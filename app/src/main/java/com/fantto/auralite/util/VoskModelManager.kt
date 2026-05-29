package com.fantto.auralite.util

import android.content.Context
import com.elvishew.xlog.XLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipInputStream

/** Vosk模型管理器，或者说是解压器，负责解压模型文件以及提供模型路径，防止重复解压 **/
object VoskModelManager {

    private const val MODEL_DIR_NAME = "vosk-model"
    private const val ASSETS_ZIP_NAME = "vosk-model-small-cn.zip"

    suspend fun getModelPath(context: Context): String = withContext(Dispatchers.IO) {
        val modelDir = File(context.filesDir, MODEL_DIR_NAME)

        if (!modelDir.exists() || modelDir.listFiles().isNullOrEmpty()) {
            modelDir.mkdirs()
            extractModel(context, modelDir)
        }

        // ZIP文件内部有顶级目录 vosk-model-small-cn-0.22/，需要返回实际的模型路径
        val actualModelDir = findActualModelDir(modelDir)
        XLog.d("VoskModelManager：模型路径 ${actualModelDir.absolutePath}")

        actualModelDir.absolutePath
    }

    // 查找实际的模型目录（跳过ZIP顶级目录）
    private fun findActualModelDir(modelDir: File): File {
        val files = modelDir.listFiles()
        // 如果只有一个子目录，且该子目录包含模型文件（如conf目录），则使用该子目录
        if (files != null && files.size == 1 && files[0].isDirectory) {
            val subDir = files[0]
            if (File(subDir, "conf").exists() || File(subDir, "am").exists()) {
                XLog.d("VoskModelManager：检测到ZIP顶级目录，使用子目录 ${subDir.name}")
                return subDir
            }
        }
        return modelDir
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