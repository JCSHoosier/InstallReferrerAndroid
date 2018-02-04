package com.taptaptap.referrercasestudy.api

import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface AllTheApps {
    @POST("mobile/install/")
    fun postData(@Body json: JSONObject) : Call<ResponseBody>
}