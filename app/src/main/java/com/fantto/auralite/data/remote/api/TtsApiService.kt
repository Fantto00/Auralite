package com.fantto.auralite.data.remote.api

import com.fantto.auralite.data.remote.dto.MimoTtsRequest
import com.fantto.auralite.data.remote.dto.MimoTtsResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/** MiMo TTS API的Retrofit接口定义，使用 chat/completions 端点进行语音合成  **/
interface TtsApiService {

    @POST("v1/chat/completions")
    suspend fun synthesizeSpeech(
        @Body request: MimoTtsRequest
    ): Response<MimoTtsResponse>
}