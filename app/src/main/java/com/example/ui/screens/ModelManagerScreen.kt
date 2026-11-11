package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.SciFiTeal
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StudioCardElevated
import com.example.ui.theme.StudioDarkBg
import com.example.ui.theme.StudioOutline
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceVariant
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

data class LocalModelItem(
    val name: String,
    val category: String,
    val sizeStr: String,
    val isInstalled: Boolean,
    val isActive: Boolean
)

@Composable
fun ModelManagerScreen() {
    var importedCount by remember { mutableStateOf(0) }
    var statusNotice by remember { mutableStateOf<String?>(null) }

    val models = listOf(
        LocalModelItem("llama3.1:8b (Q4_K_M)", "Ollama LLM", "4.7 GB", true, true),
        LocalModelItem("qwen2.5:7b-instruct", "Ollama LLM", "4.4 GB", true, false),
        LocalModelItem("mistral:7b-instruct", "Ollama LLM", "4.1 GB", false, false),
        LocalModelItem("en_US-lessac-medium.onnx", "Piper Neural TTS", "64 MB", true, true),
        LocalModelItem("en_GB-alan-low.onnx", "Piper Neural TTS", "28 MB", true, false),
        LocalModelItem("ggml-base.en.bin", "Whisper Subtitles", "142 MB", true, true),
        LocalModelItem("sd-v1-5-pruned-ema.safetensors", "Stable Diffusion (Optional)", "4.0 GB", false, false)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StudioDarkBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Model Manager",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Local Weights & Voice Checkpoints",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            Button(
                onClick = {
                    importedCount++
                    statusNotice = "Imported custom weight package from local storage."
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyberAmber, contentColor = StudioDarkBg),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("import_local_model_button")
            ) {
                Icon(imageVector = Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Import Local", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        if (statusNotice != null) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = StudioCardElevated
            ) {
                Text(
                    text = statusNotice!!,
                    color = StatusSuccess,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }

        // Offline notice
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            color = StudioSurfaceVariant,
            border = androidx.compose.foundation.BorderStroke(1.dp, StudioOutline)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Memory,
                    contentDescription = null,
                    tint = SciFiTeal,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Models are stored in local project directory 'models/'. No network download is permitted at runtime.",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }

        // Model Cards
        models.forEach { item ->
            ModelCard(item = item)
        }
    }
}

@Composable
fun ModelCard(item: LocalModelItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = StudioSurface),
        shape = RoundedCornerShape(10.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(StudioOutline))
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = when (item.category) {
                        "Ollama LLM" -> Icons.Default.Psychology
                        "Piper Neural TTS" -> Icons.Default.Mic
                        else -> Icons.Default.Subtitles
                    },
                    contentDescription = null,
                    tint = if (item.isActive) CyberAmber else TextSecondary,
                    modifier = Modifier.size(28.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = item.name,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "${item.category} • ${item.sizeStr}",
                        fontSize = 10.sp,
                        color = TextTertiary,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            if (item.isActive) {
                Badge(
                    containerColor = StatusSuccess.copy(alpha = 0.2f),
                    contentColor = StatusSuccess
                ) {
                    Text(
                        text = "ACTIVE",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            } else if (item.isInstalled) {
                Badge(
                    containerColor = StudioCardElevated,
                    contentColor = TextSecondary
                ) {
                    Text(
                        text = "READY",
                        fontSize = 9.sp,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            } else {
                Text(
                    text = "NOT INSTALLED",
                    fontSize = 9.sp,
                    color = TextTertiary,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
