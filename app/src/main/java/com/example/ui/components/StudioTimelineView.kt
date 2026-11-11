package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentPink
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.SciFiTeal
import com.example.ui.theme.StudioCardElevated
import com.example.ui.theme.StudioDarkBg
import com.example.ui.theme.StudioOutline
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceVariant
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun StudioTimelineView(
    modifier: Modifier = Modifier,
    totalDurationSec: Double = 9.5
) {
    val scrollState = rememberScrollState()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("studio_timeline_view"),
        colors = CardDefaults.cardColors(containerColor = StudioSurface),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(StudioOutline))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Timeline Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = "Timeline Icon",
                        tint = CyberAmber,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    Text(
                        text = "MULTI-TRACK TIMELINE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Badge(
                    containerColor = StudioCardElevated,
                    contentColor = SciFiTeal
                ) {
                    Text(
                        text = "AUTO-DUCKING -12dB ACTIVE",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Scrollable Timeline Tracks
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(StudioDarkBg, RoundedCornerShape(8.dp))
                    .border(1.dp, StudioOutline, RoundedCornerShape(8.dp))
                    .padding(8.dp)
                    .horizontalScroll(scrollState)
            ) {
                Column(modifier = Modifier.width(520.dp)) {
                    // Time Ruler Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (sec in 0..10 step 2) {
                            Text(
                                text = "00:0${sec}",
                                fontSize = 9.sp,
                                color = TextTertiary,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // Track 1: Video Scenes
                    TrackRow(label = "SCENE", icon = Icons.Default.Movie, color = CyberAmber) {
                        SceneBlock(name = "01: Desert Horizon", widthDp = 240, durationStr = "4.5s")
                        Spacer(modifier = Modifier.width(4.dp))
                        SceneBlock(name = "02: The Bloom", widthDp = 260, durationStr = "5.0s")
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Track 2: Dialogue / Voiceover
                    TrackRow(label = "VOICE", icon = Icons.Default.RecordVoiceOver, color = SciFiTeal) {
                        Spacer(modifier = Modifier.width(20.dp))
                        VoiceClipBlock(
                            speaker = "SCOUT",
                            emotion = "curious",
                            text = "Thermal signature detected...",
                            widthDp = 180
                        )
                        Spacer(modifier = Modifier.width(60.dp))
                        VoiceClipBlock(
                            speaker = "SCOUT",
                            emotion = "wonder",
                            text = "Life persists in the sand.",
                            widthDp = 200
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Track 3: Background Music with Ducking Envelope
                    TrackRow(label = "MUSIC", icon = Icons.Default.GraphicEq, color = AccentPink) {
                        MusicDuckingWaveform(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Track 4: Subtitles
                    TrackRow(label = "SUBS", icon = Icons.Default.Subtitles, color = TextSecondary) {
                        Spacer(modifier = Modifier.width(20.dp))
                        SubtitlePill(text = "[SCOUT]: Thermal signature detected", widthDp = 180)
                        Spacer(modifier = Modifier.width(60.dp))
                        SubtitlePill(text = "[SCOUT]: Life persists in the sand", widthDp = 200)
                    }
                }
            }
        }
    }
}

@Composable
private fun TrackRow(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    content: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.width(64.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.padding(end = 4.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = color,
                fontFamily = FontFamily.Monospace
            )
        }
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            content()
        }
    }
}

@Composable
private fun SceneBlock(name: String, widthDp: Int, durationStr: String) {
    Surface(
        modifier = Modifier
            .width(widthDp.dp)
            .height(30.dp),
        shape = RoundedCornerShape(4.dp),
        color = CyberAmber.copy(alpha = 0.2f),
        border = androidx.compose.foundation.BorderStroke(1.dp, CyberAmber)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 6.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = name, fontSize = 10.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
            Text(text = durationStr, fontSize = 9.sp, color = CyberAmber, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
private fun VoiceClipBlock(speaker: String, emotion: String, text: String, widthDp: Int) {
    Surface(
        modifier = Modifier
            .width(widthDp.dp)
            .height(28.dp),
        shape = RoundedCornerShape(4.dp),
        color = SciFiTeal.copy(alpha = 0.2f),
        border = androidx.compose.foundation.BorderStroke(1.dp, SciFiTeal)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 6.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "[$emotion] ", fontSize = 9.sp, color = SciFiTeal, fontWeight = FontWeight.Bold)
            Text(text = text, fontSize = 9.sp, color = TextPrimary, maxLines = 1)
        }
    }
}

@Composable
private fun SubtitlePill(text: String, widthDp: Int) {
    Surface(
        modifier = Modifier
            .width(widthDp.dp)
            .height(20.dp),
        shape = RoundedCornerShape(2.dp),
        color = StudioSurfaceVariant
    ) {
        Text(
            text = text,
            fontSize = 8.sp,
            color = TextSecondary,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
            maxLines = 1
        )
    }
}

@Composable
private fun MusicDuckingWaveform(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.background(AccentPink.copy(alpha = 0.1f), RoundedCornerShape(4.dp))) {
        val totalBars = 70
        val barWidth = size.width / totalBars
        val midY = size.height / 2f

        for (i in 0 until totalBars) {
            val x = i * barWidth
            // Ducking simulation: dip amplitude in speech zones (bars 4-26 and 35-62)
            val isSpeechZone = (i in 4..26) || (i in 35..62)
            val amplitudeFactor = if (isSpeechZone) 0.25f else 0.85f
            val barH = (size.height * 0.45f * amplitudeFactor) * (0.6f + 0.4f * kotlin.math.sin(i * 0.5f))

            drawLine(
                color = AccentPink.copy(alpha = if (isSpeechZone) 0.5f else 0.9f),
                start = Offset(x, midY - barH),
                end = Offset(x, midY + barH),
                strokeWidth = barWidth * 0.7f
            )
        }
    }
}
