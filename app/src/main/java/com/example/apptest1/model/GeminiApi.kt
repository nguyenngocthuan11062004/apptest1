package com.example.apptest1.model

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import com.example.apptest1.BuildConfig

object GeminiApi {

    val apiKey = BuildConfig.API_KEY

    private const val ENDPOINT =
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=${BuildConfig.API_KEY}"

    private val client = OkHttpClient()
    private val JSON = "application/json; charset=utf-8".toMediaType()

    /**
     * Generate content from Gemini
     *
     * @param prompt prompt đã được build sẵn (explain / ask)
     * @param callback (success, resultText)
     */
    fun generate(
        prompt: String,
        callback: (Boolean, String) -> Unit
    ) {
        try {

            val partsArray = org.json.JSONArray().apply {
                put(
                    JSONObject().apply {
                        put("text", prompt)
                    }
                )
            }

            val contentsArray = org.json.JSONArray().apply {
                put(
                    JSONObject().apply {
                        put("parts", partsArray)
                    }
                )
            }

            val bodyJson = JSONObject().apply {
                put("contents", contentsArray)
            }


            val requestBody = bodyJson
                .toString()
                .toRequestBody(JSON)

            val request = Request.Builder()
                .url(ENDPOINT)
                .post(requestBody)
                .build()

            client.newCall(request).enqueue(object : Callback {

                override fun onFailure(call: Call, e: IOException) {
                    callback(false, e.message ?: "Gemini network error")
                }

                override fun onResponse(call: Call, response: Response) {
                    val bodyString = response.body?.string()

                    android.util.Log.d("GeminiApi", "HTTP code = ${response.code}")
                    android.util.Log.d("GeminiApi", "Raw response = $bodyString")

                    if (!response.isSuccessful || bodyString == null) {
                        callback(false, "Gemini HTTP error: ${response.code}")
                        return
                    }

                    try {
                        val json = JSONObject(bodyString)

                        android.util.Log.d(
                            "GeminiApi",
                            "Parsed JSON:\n${json.toString(2)}"
                        )

                        val text =
                            json.getJSONArray("candidates")
                                .getJSONObject(0)
                                .getJSONObject("content")
                                .getJSONArray("parts")
                                .getJSONObject(0)
                                .getString("text")

                        callback(true, text.trim())

                    } catch (e: Exception) {
                        android.util.Log.e("GeminiApi", "Parse error", e)
                        callback(false, "Parse error: ${e.message}")
                    }
                }
            })

        } catch (e: Exception) {
            callback(false, e.message ?: "Gemini internal error")
        }
    }
}
