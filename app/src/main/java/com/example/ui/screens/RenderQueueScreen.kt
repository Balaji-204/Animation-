package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.RenderJobEntity
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.SciFiTeal
import com.example.ui.theme.StatusRunning
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StudioCardElevated
import com.example.ui.theme.StudioDarkBg
import com.example.ui.theme.StudioOutline
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceVariant
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun RenderQueueScreen(
    renderJobs: List<RenderJobEntity>,
    onTogglePause: (RenderJobEntity) -> Unit,
    onDeleteJob: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StudioDarkBg)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Render Queue",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Offline Blender EEVEE & FFmpeg Jobs",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            Badge(
                containerColor = StudioCardElevated,
                contentColor = CyberAmber
            ) {
                Text(
                    text = "${renderJobs.count { it.status == "rendering" }} ACTIVE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (renderJobs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.HourglassTop,
                        contentDescription = null,
                        tint = TextTertiary,
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No rendering jobs queued.",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Open any project and tap 'Render MP4' to start.",
                        color = TextTertiary,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(renderJobs, key = { it.id }) { job ->
                    RenderJobCard(
                        job = job,
                        onTogglePause = { onTogglePause(job) },
                        onDelete = { onDeleteJob(job.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun RenderJobCard(
    job: RenderJobEntity,
    onTogglePause: () -> Unit,
    onDelete: () -> Unit
) {
    var showLogs by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("render_job_${job.id}"),
        colors = CardDefaults.cardColors(containerColor = StudioSurface),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(StudioOutline))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = job.projectName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                val statusColor = when (job.status) {
                    "completed" -> StatusSuccess
                    "paused" -> CyberAmber
                    else -> StatusRunning
                }

                Badge(
                    containerColor = statusColor.copy(alpha = 0.2f),
                    contentColor = statusColor
                ) {
                    Text(
                        text = job.status.uppercase(),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "STAGE: ${job.currentStage}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = SciFiTeal,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { job.progressPercent },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = if (job.status == "completed") StatusSuccess else CyberAmber,
                trackColor = StudioOutline
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${(job.progressPercent * 100).toInt()}% • ETA ${job.etaSeconds}s",
                    fontSize = 11.sp,
                    color = TextTertiary,
                    fontFamily = FontFamily.Monospace
                )

                Row {
                    IconButton(
                        onClick = { showLogs = !showLogs },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = "Toggle Logs",
                            tint = if (showLogs) CyberAmber else TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    if (job.status != "completed") {
                        IconButton(
                            onClick = onTogglePause,
                            modifier = Modifier.size(32.dp).testTag("pause_resume_btn_${job.id}")
                        ) {
                            Icon(
                                imageVector = if (job.status == "paused") Icons.Default.PlayArrow else Icons.Default.Pause,
                                contentDescription = "Pause or Resume",
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp).testTag("delete_job_btn_${job.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Job",
                            tint = TextTertiary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Collapsible Logs Console
            if (showLogs) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(6.dp),
                    color = StudioDarkBg
                ) {
                    Text(
                        text = job.logOutput.ifBlank { "[Blender headless]: Initialized scene graph...\n[Piper TTS]: Synthesizing dialogue WAVs...\n[FFmpeg]: Concatenating frames with -pix_fmt yuv420p" },
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFFA0AEC0),
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}
