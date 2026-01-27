package com.example.apptest1

import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

object PhishingApi {

    private val client = OkHttpClient()

//    private const val SERVER_URL = "http://192.168.68.110:8000/predict"
    private const val SERVER_URL = "http://192.168.1.10:8000/predict"
    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    fun checkPhishing(text: String, callback: (Boolean, String) -> Unit) {
        // Dùng JSONObject để tránh JSON lỗi khi text có xuống dòng / dấu "
        val jsonObject = JSONObject().apply {
            put("text", text)
        }

        val requestBody = jsonObject
            .toString()
            .toRequestBody(JSON_MEDIA_TYPE)

        val request = Request.Builder()
            .url(SERVER_URL)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                callback(false, e.message ?: "Network error")
            }

            override fun onResponse(call: Call, response: Response) {
                val bodyString = response.body?.string()

                if (!response.isSuccessful || bodyString == null) {
                    callback(false, "Server error: ${response.code}")
                    return
                }

                try {
                    val json = JSONObject(bodyString)
                    android.util.Log.d("PhishingApi", "Response JSON (raw): $bodyString")
                    android.util.Log.d(
                        "PhishingApi",
                        "Response JSON (pretty):\n${json.toString(2)}"
                    )

                    callback(true, json.toString())
                } catch (e: Exception) {
                    // Trường hợp backend trả string thuần
                    android.util.Log.d("PhishingApi", "Response is not JSON: $bodyString")
                    callback(true, bodyString)
                }
            }
        })
    }
}
