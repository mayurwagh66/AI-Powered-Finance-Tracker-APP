package com.example.myapp.finance

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class GeminiClient(private val apiKey: String) {

    private val client = OkHttpClient()

    suspend fun querySpending(query: String): String? {
        return makeApiCall("spending", query)
    }

    suspend fun querySaving(query: String): String? {
        return makeApiCall("saving", query)
    }

    private suspend fun makeApiCall(endpoint: String, query: String): String? {
        return withContext(Dispatchers.IO) {
            val url = "https://api.gemini.com/v1/$endpoint"
            val json = JSONObject()
            json.put("query", query)

            val requestBody = json.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .addHeader("Authorization", "Bearer $apiKey")
                .build()

            return@withContext try {
                val response: Response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    response.body?.string()
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
    }
}