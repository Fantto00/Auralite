package com.fantto.auralite.data.remote.api

import com.fantto.auralite.data.remote.dto.TtsRequest
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Streaming

/** TTS API的Retrofit接口定义，包含一个方法synthesizeSpeech用于发送文本转语音请求并接收流式响应  **/
interface TtsApiService {

    @POST("v1/audio/speech")
    @Streaming
    suspend fun synthesizeSpeech(
        @Body request: TtsRequest
    ): Response<ResponseBody>
}