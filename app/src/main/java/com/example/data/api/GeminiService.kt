package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.Bouquet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.TimeUnit

object GeminiService {
    private const val TAG = "GeminiService"
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun generateCustomBouquet(
        occasion: String,
        colorTheme: String,
        details: String
    ): Result<Bouquet> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e(TAG, "Gemini API key is placeholder or empty!")
            return@withContext Result.failure(Exception("Gemini API key is not configured. Please add your key in the Secrets panel in AI Studio."))
        }

        val prompt = """
            Create a custom flower bouquet design.
            Occasion: $occasion
            Color Palette Preference: $colorTheme
            Additional specifics/recipient details: $details
        """.trimIndent()

        val systemInstruction = """
            You are Bloom's AI Master Bouquet Designer. Your job is to design a unique, luxurious, and highly custom same-day flower bouquet based on a special occasion, color palette, and user details.
            
            Based on the inputs provided, design a beautiful arrangement. Do not invent non-existent flowers. Include details on premium wrapping and matching ribbons. Create a custom reasonable price (Double between 35.00 and 110.00) based on the cost of the flowers included.
            
            You MUST return a raw JSON object matching the following structure exactly. Do NOT wrap the JSON in Markdown fences (like ```json), just return raw JSON text. Make sure all values are properly quoted.
            
            JSON Structure:
            {
              "bouquetName": "Name of Bouquet (e.g. Lavender Romance)",
              "description": "Short poetic florist's description (max 100 characters)",
              "price": 54.99,
              "wrapping": "Type of wrapping material (e.g. Sage Matte Paper)",
              "ribbon": "Type of ribbon (e.g. Dusty Rose Satin Ribbon)",
              "stems": {
                "Lavender Rose": 12,
                "White Lily": 3,
                "Eucalyptus": 4,
                "Gypsophila": 3
              }
            }
        """.trimIndent()

        // Construct the request body JSON
        val requestJson = JSONObject().apply {
            // contents list
            val contentsArr = org.json.JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", org.json.JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                    })
                })
            }
            put("contents", contentsArr)

            // systemInstruction if supported
            put("systemInstruction", JSONObject().apply {
                put("parts", org.json.JSONArray().apply {
                    put(JSONObject().apply {
                        put("text", systemInstruction)
                    })
                })
            })

            // generationConfig specifying JSON responses
            put("generationConfig", JSONObject().apply {
                put("responseMimeType", "application/json")
                put("temperature", 0.7)
            })
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = requestJson.toString().toRequestBody(mediaType)

        // Model: 'gemini-3.5-flash'
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        try {
            val response = client.newCall(request).execute()
            val responseBodyString = response.body?.string()
            
            if (!response.isSuccessful || responseBodyString == null) {
                Log.e(TAG, "Request failed: Code=${response.code}, Body=$responseBodyString")
                return@withContext Result.failure(Exception("Gemini API call failed with code: ${response.code}"))
            }

            // Parse response
            val responseJson = JSONObject(responseBodyString)
            val candidates = responseJson.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                return@withContext Result.failure(Exception("Gemini returned empty candidate response."))
            }

            val parts = candidates.getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
            
            if (parts.length() == 0) {
                return@withContext Result.failure(Exception("Gemini returned empty parts."))
            }

            val rawJsonText = parts.getJSONObject(0).optString("text")?.trim() ?: ""
            Log.d(TAG, "Raw returned JSON text: $rawJsonText")

            // Parse custom bouquet JSON
            val cleanJson = if (rawJsonText.startsWith("```")) {
                // Strips out ```json or ``` prefixes/suffixes just in case
                rawJsonText
                    .removePrefix("```json")
                    .removePrefix("```JSON")
                    .removePrefix("```")
                    .removeSuffix("```")
                    .trim()
            } else {
                rawJsonText
            }

            val bouquetJson = JSONObject(cleanJson)
            val name = bouquetJson.getString("bouquetName")
            val description = bouquetJson.getString("description")
            val price = bouquetJson.getDouble("price")
            val wrapping = bouquetJson.getString("wrapping")
            val ribbon = bouquetJson.getString("ribbon")
            
            // Extract stems
            val stemsJson = bouquetJson.getJSONObject("stems")
            val stemsMap = mutableMapOf<String, Int>()
            val keys = stemsJson.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                stemsMap[key] = stemsJson.getInt(key)
            }

            val bouquet = Bouquet(
                id = "custom_" + UUID.randomUUID().toString().take(8),
                name = name,
                description = description,
                price = price,
                occasion = occasion,
                stems = stemsMap,
                wrapping = wrapping,
                ribbon = ribbon,
                isCustom = true
            )

            Result.success(bouquet)

        } catch (e: Exception) {
            Log.e(TAG, "Failed during generation", e)
            Result.failure(e)
        }
    }
}
