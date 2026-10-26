package com.example.ui.screens.ai

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.data.remote.AiChatMessage
import com.example.data.remote.GeminiModelOption
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun AiAssistantScreen(
    messages: List<AiChatMessage>,
    selectedModel: GeminiModelOption,
    enableSearchGrounding: Boolean,
    isSending: Boolean,
    isApiKeyConfigured: Boolean,
    onSelectModel: (GeminiModelOption) -> Unit,
    onToggleSearchGrounding: (Boolean) -> Unit,
    onSendMessage: (String) -> Unit,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    var promptInput by remember { mutableStateOf("") }
    var voiceModeEnabled by remember { mutableStateOf(false) }
    var isListeningVoice by remember { mutableStateOf(false) }
    var voiceStatusMessage by remember { mutableStateOf<String?>(null) }

    // TextToSpeech engine for conversational audio responses
    var ttsEngine by remember { mutableStateOf<TextToSpeech?>(null) }
    DisposableEffect(context) {
        var engine: TextToSpeech? = null
        engine = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                engine?.language = Locale.US
            }
        }
        ttsEngine = engine
        onDispose {
            engine.stop()
            engine.shutdown()
        }
    }

    // Automatically speak newest AI message when Voice Mode is enabled
    val lastMessageId = messages.lastOrNull()?.id
    LaunchedEffect(lastMessageId, voiceModeEnabled) {
        val lastMsg = messages.lastOrNull()
        if (voiceModeEnabled && lastMsg != null && lastMsg.role == "model") {
            val cleanSpeech = lastMsg.text
                .replace("*", "")
                .replace("#", "")
                .take(650)
            ttsEngine?.speak(cleanSpeech, TextToSpeech.QUEUE_FLUSH, null, lastMsg.id)
        }
    }

    val speechRecognizer = remember {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            SpeechRecognizer.createSpeechRecognizer(context)
        } else {
            null
        }
    }

    DisposableEffect(speechRecognizer) {
        val listener = object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                isListeningVoice = true
                voiceStatusMessage = "Listening... Speak your cybersecurity or crypto question."
            }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                isListeningVoice = false
            }
            override fun onError(error: Int) {
                isListeningVoice = false
                voiceStatusMessage = "Voice input finished or unavailable on this device."
            }
            override fun onResults(results: Bundle?) {
                isListeningVoice = false
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val spokenText = matches?.firstOrNull()?.trim()
                if (!spokenText.isNullOrBlank()) {
                    voiceStatusMessage = "Heard: \"$spokenText\""
                    onSendMessage(spokenText)
                }
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
        speechRecognizer?.setRecognitionListener(listener)
        onDispose {
            speechRecognizer?.destroy()
        }
    }

    fun startVoiceListening() {
        if (speechRecognizer == null) {
            voiceStatusMessage = "Speech recognition service is not installed on this device."
            return
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
        speechRecognizer.startListening(intent)
    }

    val recordAudioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            voiceModeEnabled = true
            startVoiceListening()
        } else {
            voiceStatusMessage = "Microphone permission is required for live voice conversation."
        }
    }

    val quickPrompts = listOf(
        "Explain Bitcoin's 24h market movement & volatility",
        "How do I spot a fake crypto airdrop or phishing link?",
        "What does SSRF mean and why are private IPs blocked?",
        "Best security checklist for a hardware & hot crypto wallet"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberNavyDark)
            .padding(16.dp)
    ) {
        // Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "ShieldX Gemini AI Analyst",
                    style = MaterialTheme.typography.headlineSmall,
                    color = CyberTextPrimary,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = if (isApiKeyConfigured) {
                        "Gemini REST + Google Search Grounding Active"
                    } else {
                        "Firebase AI Logic Active • Set GEMINI_API_KEY in Secrets for Search Grounding"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = CyberCyan
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                IconButton(
                    onClick = {
                        voiceModeEnabled = !voiceModeEnabled
                        if (!voiceModeEnabled) {
                            ttsEngine?.stop()
                        }
                    },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (voiceModeEnabled) CyberCyan.copy(alpha = 0.2f) else CyberCardSurface)
                        .testTag("toggle_voice_mode_button")
                ) {
                    Icon(
                        imageVector = if (voiceModeEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        contentDescription = "Toggle Voice Output",
                        tint = if (voiceModeEnabled) CyberCyan else CyberTextSecondary
                    )
                }

                IconButton(
                    onClick = onClearHistory,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(CyberCardSurface)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Clear conversation",
                        tint = CyberTextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Model Selector Row + Search Grounding Toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GeminiModelOption.entries.forEach { option ->
                val selected = selectedModel == option
                FilterChip(
                    selected = selected,
                    onClick = { onSelectModel(option) },
                    label = {
                        Text(
                            text = "${option.modelId} (${option.badge})",
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyberCyan.copy(alpha = 0.22f),
                        selectedLabelColor = CyberCyan
                    )
                )
            }

            if (selectedModel.supportsSearchGrounding) {
                FilterChip(
                    selected = enableSearchGrounding,
                    onClick = { onToggleSearchGrounding(!enableSearchGrounding) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Public,
                            contentDescription = "Google Search Grounding",
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    label = { Text("Google Search Grounding", style = MaterialTheme.typography.labelSmall) }
                )
            }
        }

        if (voiceStatusMessage != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = CyberPurple.copy(alpha = 0.16f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = "Voice status",
                        tint = CyberCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = voiceStatusMessage ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberTextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Quick Starter Prompts
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            quickPrompts.forEach { suggestion ->
                AssistChip(
                    onClick = {
                        if (!isSending) {
                            onSendMessage(suggestion)
                        }
                    },
                    label = {
                        Text(
                            text = suggestion,
                            style = MaterialTheme.typography.labelSmall,
                            color = CyberTextPrimary
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Chat Message List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                val isUser = msg.role == "user"
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
                ) {
                    Card(
                        modifier = Modifier
                            .widthIn(max = 520.dp)
                            .border(
                                width = 1.dp,
                                color = if (isUser) CyberBlue else CyberBorder,
                                shape = RoundedCornerShape(16.dp)
                            ),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isUser) CyberBlue.copy(alpha = 0.24f) else CyberCardSurface
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = if (isUser) Icons.Default.Person else Icons.Default.AutoAwesome,
                                    contentDescription = if (isUser) "User" else "ShieldX AI",
                                    tint = if (isUser) CyberCyan else CyberGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (isUser) "You" else msg.modelUsed,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CyberTextSecondary,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = msg.text,
                                style = MaterialTheme.typography.bodyMedium,
                                color = CyberTextPrimary
                            )

                            if (msg.groundingSources.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(color = CyberBorder)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Google Search Grounding Sources:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CyberCyan,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                msg.groundingSources.take(4).forEach { src ->
                                    Text(
                                        text = "↗ ${src.title}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = CyberCyan,
                                        modifier = Modifier
                                            .clickable {
                                                try {
                                                    uriHandler.openUri(src.uri)
                                                } catch (_: Exception) {
                                                }
                                            }
                                            .padding(vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (isSending) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = CyberCyan,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "ShieldX AI (${selectedModel.modelId}) is analyzing...",
                            style = MaterialTheme.typography.bodySmall,
                            color = CyberTextSecondary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Input Composer + Live Voice Mic Button + Send Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = {
                    val hasAudioPerm = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED
                    if (hasAudioPerm) {
                        voiceModeEnabled = true
                        startVoiceListening()
                    } else {
                        recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (isListeningVoice) CyberDanger else CyberCardSurface)
                    .testTag("ai_voice_mic_button")
            ) {
                Icon(
                    imageVector = if (isListeningVoice) Icons.Default.Mic else Icons.Default.MicNone,
                    contentDescription = "Speak question with voice",
                    tint = if (isListeningVoice) Color.White else CyberCyan
                )
            }

            OutlinedTextField(
                value = promptInput,
                onValueChange = { promptInput = it },
                placeholder = { Text("Ask about crypto markets, URL threats, or wallet security...") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("ai_chat_input"),
                shape = RoundedCornerShape(16.dp),
                maxLines = 3
            )

            IconButton(
                onClick = {
                    val trimmed = promptInput.trim()
                    if (trimmed.isNotEmpty() && !isSending) {
                        onSendMessage(trimmed)
                        promptInput = ""
                    }
                },
                enabled = promptInput.isNotBlank() && !isSending,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (promptInput.isNotBlank() && !isSending) CyberCyan else CyberCardSurface)
                    .testTag("ai_send_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send prompt to Gemini AI",
                    tint = if (promptInput.isNotBlank() && !isSending) CyberNavyDark else CyberTextMuted
                )
            }
        }
    }
}
