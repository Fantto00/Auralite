package com.fantto.auralite.data.remote.api

import com.fantto.auralite.data.remote.dto.LlmRequest
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Streaming

/** 大模型API的Retrofit接口定义，包含一个方法streamChat用于发送聊天请求并接收流式响应  **/
interface LlmApiService {

    @POST("v1/chat/completions")
    @Streaming
    suspend fun streamChat(
        @Body request: LlmRequest
    ): Response<ResponseBody>
}