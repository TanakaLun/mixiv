package com.mrl.pixiv.common.data.setting

import kotlinx.serialization.Serializable

@Serializable
enum class AiProvider {
    OPENAI,
    CLAUDE,
    GEMINI,
}

/**
 * Built-in text models, verified against the provider catalogs on 2026-09-24.
 *
 * https://developers.openai.com/api/docs/models
 * https://platform.claude.com/docs/en/models/overview
 * https://ai.google.dev/gemini-api/docs/models
 */
sealed interface Model {
    val modelId: String

    enum class OpenAI(override val modelId: String) : Model {
        GPT_6_ASTRA("gpt-6-astra"),
        GPT_6_SOL("gpt-6-sol"),
        GPT_6_LUNA("gpt-6-luna"),
    }

    enum class Claude(override val modelId: String) : Model {
        CLAUDE_FABLE_5_1("claude-fable-5-1"),
        CLAUDE_OPUS_5_5("claude-opus-5-5"),
        CLAUDE_SONNET_5("claude-sonnet-5"),
        CLAUDE_4_5_HAIKU("claude-haiku-4-5"),
    }

    enum class Gemini(override val modelId: String) : Model {
        GEMINI_3_8_FLASH("gemini-3.8-flash"),
        GEMINI_3_1_PRO_PREVIEW("gemini-3.1-pro-preview"),
        GEMINI_3_5_FLASH_LITE("gemini-3.5-flash-lite"),
    }
}

@Serializable
data class AiTranslationConfig(
    val provider: AiProvider = AiProvider.OPENAI,
    val endpoint: String = defaultEndpoint(AiProvider.OPENAI),
    val apiKey: String = "",
    val model: String = defaultModel(AiProvider.OPENAI).modelId,
    val responseApi: Boolean = false,
    val extraBody: String = "",
    val generationTimeoutSeconds: Int = GENERATION_TIMEOUT_DEFAULT_SECONDS,
    val maxConcurrentRequests: Int = MAX_CONCURRENT_REQUESTS_DEFAULT,
) {
    companion object {
        const val GENERATION_TIMEOUT_MIN_SECONDS = 30
        const val GENERATION_TIMEOUT_DEFAULT_SECONDS = 180
        const val GENERATION_TIMEOUT_MAX_SECONDS = 1800
        const val MAX_CONCURRENT_REQUESTS_MIN = 1
        const val MAX_CONCURRENT_REQUESTS_DEFAULT = 2

        fun defaultEndpoint(provider: AiProvider): String = when (provider) {
            AiProvider.OPENAI -> "https://api.openai.com/v1"
            AiProvider.CLAUDE -> "https://api.anthropic.com"
            AiProvider.GEMINI -> "https://generativelanguage.googleapis.com"
        }

        fun defaultModel(provider: AiProvider): Model = when (provider) {
            AiProvider.OPENAI -> Model.OpenAI.GPT_6_LUNA
            AiProvider.CLAUDE -> Model.Claude.CLAUDE_4_5_HAIKU
            AiProvider.GEMINI -> Model.Gemini.GEMINI_3_8_FLASH
        }

        fun suggestedModels(provider: AiProvider): List<Model> = when (provider) {
            AiProvider.OPENAI -> listOf(
                Model.OpenAI.GPT_6_ASTRA,
                Model.OpenAI.GPT_6_SOL,
                Model.OpenAI.GPT_6_LUNA,
            )
            AiProvider.CLAUDE -> listOf(
                Model.Claude.CLAUDE_FABLE_5_1,
                Model.Claude.CLAUDE_OPUS_5_5,
                Model.Claude.CLAUDE_SONNET_5,
                Model.Claude.CLAUDE_4_5_HAIKU,
            )

            AiProvider.GEMINI -> listOf(
                Model.Gemini.GEMINI_3_8_FLASH,
                Model.Gemini.GEMINI_3_1_PRO_PREVIEW,
                Model.Gemini.GEMINI_3_5_FLASH_LITE,
            )
        }
    }
}
