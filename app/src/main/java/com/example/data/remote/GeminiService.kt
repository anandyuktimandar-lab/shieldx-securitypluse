package com.example.data.remote

import com.example.BuildConfig
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

enum class GeminiModelOption(
    val modelId: String,
    val displayName: String,
    val badge: String,
    val supportsSearchGrounding: Boolean
) {
    FLASH_SEARCH(
        modelId = "gemini-2.5-flash",
        displayName = "Gemini 2.5 Flash (Search Grounding)",
        badge = "Live Web Search",
        supportsSearchGrounding = true
    ),
    PRO_THINKING(
        modelId = "gemini-3.1-pro-preview",
        displayName = "Gemini 3.1 Pro (Deep Analysis)",
        badge = "Complex Reasoning",
        supportsSearchGrounding = false
    ),
    FLASH_LITE(
        modelId = "gemini-3.1-flash-lite-preview",
        displayName = "Gemini 3.1 Flash-Lite (Fast)",
        badge = "Low Latency",
        supportsSearchGrounding = false
    )
}

data class GroundingSource(
    val title: String,
    val uri: String
)

data class AiChatMessage(
    val id: String,
    val role: String, // "user" or "model"
    val text: String,
    val modelUsed: String,
    val groundingSources: List<GroundingSource> = emptyList(),
    val timestampMillis: Long = System.currentTimeMillis()
)

class GeminiService {
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .build()

    private val systemPrompt = """
        You are ShieldX AI by SecuriPulse — an expert cybersecurity analyst and cryptocurrency market intelligence advisor.
        Follow these strict guidelines:
        1. Provide clear, accurate, beginner-friendly explanations of cybersecurity threats, URL reputation, phishing indicators, device hygiene, and cryptocurrency market dynamics.
        2. When explaining cryptocurrency prices, market cap, volume, or trends, NEVER promise guaranteed returns or give reckless financial advice. Always remind users about market volatility and wallet safety.
        3. Never help with illegal hacking, malware creation, or credential theft; focus strictly on defensive security and educational guidance.
        4. Format key takeaways cleanly using concise bullet points where helpful.
    """.trimIndent()

    fun isApiKeyConfigured(): Boolean {
        val key = BuildConfig.GEMINI_API_KEY
        return key.isNotBlank() && key != "MY_GEMINI_API_KEY"
    }

    suspend fun sendMessage(
        history: List<AiChatMessage>,
        userPrompt: String,
        modelOption: GeminiModelOption,
        enableSearchGrounding: Boolean,
        marketContextSummary: String = ""
    ): AiChatMessage = withContext(Dispatchers.IO) {
        val enrichedUserPrompt = if (marketContextSummary.isNotBlank()) {
            "$userPrompt\n\n[Live ShieldX Market & Security Telemetry Context:\n$marketContextSummary]"
        } else {
            userPrompt
        }

        // 1. If GEMINI_API_KEY is configured in BuildConfig (.env / Secrets panel), use direct Gemini REST API
        //    which supports Google Search Grounding tools and groundingMetadata extraction.
        if (isApiKeyConfigured()) {
            try {
                return@withContext callGeminiRestApi(
                    history = history,
                    userPrompt = enrichedUserPrompt,
                    modelOption = modelOption,
                    enableSearchGrounding = enableSearchGrounding && modelOption.supportsSearchGrounding
                )
            } catch (restError: Exception) {
                // Fall through to Firebase AI Logic SDK if REST call fails
            }
        }

        // 2. Fallback / Primary provisioned backend: Firebase AI Logic SDK (GenerativeBackend.googleAI())
        try {
            val generativeModel = Firebase.ai(backend = GenerativeBackend.googleAI())
                .generativeModel(
                    modelName = modelOption.modelId,
                    systemInstruction = content { text(systemPrompt) }
                )

            val fullPrompt = buildString {
                if (history.isNotEmpty()) {
                    appendLine("Conversation History:")
                    history.takeLast(6).forEach { msg ->
                        val speaker = if (msg.role == "user") "User" else "ShieldX AI"
                        appendLine("$speaker: ${msg.text}")
                    }
                    appendLine()
                }
                append("User: $enrichedUserPrompt")
            }

            val response = generativeModel.generateContent(fullPrompt)
            val replyText = response.text?.takeIf { it.isNotBlank() }
                ?: "No response generated. Please try rephrasing your question."

            return@withContext AiChatMessage(
                id = "ai_${System.currentTimeMillis()}",
                role = "model",
                text = replyText,
                modelUsed = "${modelOption.displayName} (Firebase AI)"
            )
        } catch (firebaseAiError: Exception) {
            val missingKeyHint = if (!isApiKeyConfigured()) {
                "\n\nTip: To enable direct Gemini REST API with Google Search Grounding, add your `GEMINI_API_KEY` in the **Secrets panel in AI Studio**."
            } else {
                ""
            }
            return@withContext AiChatMessage(
                id = "ai_err_${System.currentTimeMillis()}",
                role = "model",
                text = "ShieldX AI encountered an issue connecting to ${modelOption.modelId}: ${firebaseAiError.localizedMessage ?: "Service unavailable"}.$missingKeyHint",
                modelUsed = modelOption.displayName
            )
        }
    }

    private fun callGeminiRestApi(
        history: List<AiChatMessage>,
        userPrompt: String,
        modelOption: GeminiModelOption,
        enableSearchGrounding: Boolean
    ): AiChatMessage {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val url = "https://generativelanguage.googleapis.com/v1beta/models/${modelOption.modelId}:generateContent"

        val contentsArray = JSONArray()
        history.takeLast(8).forEach { msg ->
            val role = if (msg.role == "user") "user" else "model"
            val part = JSONObject().put("text", msg.text)
            val turn = JSONObject()
                .put("role", role)
                .put("parts", JSONArray().put(part))
            contentsArray.put(turn)
        }
        contentsArray.put(
            JSONObject()
                .put("role", "user")
                .put("parts", JSONArray().put(JSONObject().put("text", userPrompt)))
        )

        val payload = JSONObject().apply {
            put(
                "systemInstruction",
                JSONObject().put(
                    "parts",
                    JSONArray().put(JSONObject().put("text", systemPrompt))
                )
            )
            put("contents", contentsArray)
            if (enableSearchGrounding) {
                put(
                    "tools",
                    JSONArray().put(JSONObject().put("googleSearch", JSONObject()))
                )
            }
        }

        val request = Request.Builder()
            .url(url)
            .addHeader("x-goog-api-key", apiKey)
            .addHeader("Content-Type", "application/json")
            .post(payload.toString().toRequestBody("application/json".toMediaType()))
            .build()

        httpClient.newCall(request).execute().use { response ->
            val bodyString = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                throw IllegalStateException("HTTP ${response.code}: $bodyString")
            }

            val root = JSONObject(bodyString)
            val candidates = root.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val contentObj = firstCandidate?.optJSONObject("content")
            val parts = contentObj?.optJSONArray("parts")

            val textBuilder = StringBuilder()
            if (parts != null) {
                for (i in 0 until parts.length()) {
                    val partText = parts.optJSONObject(i)?.optString("text", "") ?: ""
                    if (partText.isNotEmpty()) {
                        textBuilder.append(partText)
                    }
                }
            }

            val sources = mutableListOf<GroundingSource>()
            val groundingMetadata = firstCandidate?.optJSONObject("groundingMetadata")
            val chunks = groundingMetadata?.optJSONArray("groundingChunks")
            if (chunks != null) {
                for (i in 0 until chunks.length()) {
                    val webObj = chunks.optJSONObject(i)?.optJSONObject("web")
                    if (webObj != null) {
                        val uri = webObj.optString("uri", "")
                        val title = webObj.optString("title", uri)
                        if (uri.isNotBlank()) {
                            sources.add(GroundingSource(title = title, uri = uri))
                        }
                    }
                }
            }

            val finalReply = textBuilder.toString().ifBlank {
                "Analysis complete, but no text output was returned."
            }

            return AiChatMessage(
                id = "ai_${System.currentTimeMillis()}",
                role = "model",
                text = finalReply,
                modelUsed = modelOption.displayName,
                groundingSources = sources.distinctBy { it.uri }
            )
        }
    }
}
