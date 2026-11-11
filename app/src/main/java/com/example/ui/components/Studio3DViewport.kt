package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Badge
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pipeline.LowPolyMeshBuilder
import com.example.pipeline.Polygon3D
import com.example.pipeline.Vec3
import com.example.pipeline.projectToScreen
import com.example.pipeline.rotatePoint
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.SciFiTeal
import com.example.ui.theme.StudioCardElevated
import com.example.ui.theme.StudioDarkBg
import com.example.ui.theme.StudioOutline
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun Studio3DViewport(
    modifier: Modifier = Modifier,
    sceneTitle: String = "Scene 1: Dune Reconnaissance",
    cameraMovement: String = "dolly",
    shotType: String = "medium",
    characterAction: String = "walk",
    lightingMood: String = "warm_sunlight"
) {
    var yaw by remember { mutableFloatStateOf(0.785f) } // 45 deg isometric
    var pitch by remember { mutableFloatStateOf(0.610f) } // ~35 deg tilt
    var zoomScale by remember { mutableFloatStateOf(95f) }
    var isWireframe by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(true) }
    var playbackProgress by remember { mutableFloatStateOf(0.4f) }

    val infiniteTransition = rememberInfiniteTransition(label = "animLoop")
    val autoProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "playbackLoop"
    )

    val currentProgress = if (isPlaying) autoProgress else playbackProgress

    val flowerPulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flowerPulse"
    )

    // Compute dynamic character position
    val robotPos = when (characterAction) {
        "walk" -> Vec3(-1.2f + currentProgress * 1.5f, 0f, 0f)
        "reach_out" -> Vec3(0.1f, 0.5f, 0f)
        else -> Vec3(0f, 0f, 0f)
    }

    // Sky colors based on lighting mood
    val skyColor = when (lightingMood) {
        "warm_sunlight", "golden_hour" -> Color(0xFF261D18)
        "dusk" -> Color(0xFF1E172B)
        "night" -> Color(0xFF0B0E14)
        else -> Color(0xFF121B24)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StudioDarkBg)
            .padding(12.dp)
    ) {
        // Viewport Header HUD
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "VIEWPORT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberAmber,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Badge(
                        containerColor = StudioCardElevated,
                        contentColor = SciFiTeal
                    ) {
                        Text(
                            text = if (isWireframe) "WIREFRAME" else "EEVEE PREVIEW",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = sceneTitle,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
            }

            // Quick View Controls
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { isWireframe = !isWireframe },
                    modifier = Modifier.size(36.dp).testTag("toggle_wireframe_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = "Toggle Wireframe",
                        tint = if (isWireframe) CyberAmber else TextSecondary
                    )
                }
                IconButton(
                    onClick = { zoomScale = (zoomScale + 15f).coerceAtMost(160f) },
                    modifier = Modifier.size(36.dp).testTag("zoom_in_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ZoomIn,
                        contentDescription = "Zoom In",
                        tint = TextSecondary
                    )
                }
                IconButton(
                    onClick = { zoomScale = (zoomScale - 15f).coerceAtLeast(50f) },
                    modifier = Modifier.size(36.dp).testTag("zoom_out_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ZoomOut,
                        contentDescription = "Zoom Out",
                        tint = TextSecondary
                    )
                }
                IconButton(
                    onClick = {
                        yaw = 0.785f
                        pitch = 0.610f
                        zoomScale = 95f
                    },
                    modifier = Modifier.size(36.dp).testTag("reset_view_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.RestartAlt,
                        contentDescription = "Reset Camera View",
                        tint = TextSecondary
                    )
                }
            }
        }

        // Main 3D Canvas Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(skyColor, RoundedCornerShape(12.dp))
                .border(1.dp, StudioOutline, RoundedCornerShape(12.dp))
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        yaw += dragAmount.x * 0.008f
                        pitch = (pitch - dragAmount.y * 0.008f).coerceIn(0.1f, 1.4f)
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val cy = size.height / 2f

                // Assemble scene meshes
                val allPolygons = mutableListOf<Polygon3D>()
                allPolygons.addAll(LowPolyMeshBuilder.createTerrain())
                allPolygons.addAll(LowPolyMeshBuilder.createAlienFlower(Vec3(0.4f, 1.0f, 0f), flowerPulse))
                allPolygons.addAll(LowPolyMeshBuilder.createRobot(robotPos, currentProgress, characterAction))

                // Depth Sort (Painter's Algorithm)
                val sortedPolys = allPolygons.sortedByDescending { it.getAverageDepth(yaw, pitch) }

                for (poly in sortedPolys) {
                    if (poly.vertices.size < 3) continue

                    val path = Path()
                    val p0 = projectToScreen(rotatePoint(poly.vertices[0], yaw, pitch), cx, cy, zoomScale)
                    path.moveTo(p0.x, p0.y)

                    for (i in 1 until poly.vertices.size) {
                        val pi = projectToScreen(rotatePoint(poly.vertices[i], yaw, pitch), cx, cy, zoomScale)
                        path.lineTo(pi.x, pi.y)
                    }
                    path.close()

                    if (isWireframe) {
                        drawPath(
                            path = path,
                            color = if (poly.isEmission) SciFiTeal else Color(0x88CBD5E1),
                            style = Stroke(width = 1.2f)
                        )
                    } else {
                        drawPath(
                            path = path,
                            color = poly.baseColor
                        )
                        // Polygon edge wire
                        drawPath(
                            path = path,
                            color = Color(0x33000000),
                            style = Stroke(width = 0.8f)
                        )
                    }
                }

                // Camera trajectory path
                val camStart = Vec3(0f, -3.5f, 1.4f)
                val camEnd = Vec3(0f, -2.2f, 1.0f)
                val s0 = projectToScreen(rotatePoint(camStart, yaw, pitch), cx, cy, zoomScale)
                val s1 = projectToScreen(rotatePoint(camEnd, yaw, pitch), cx, cy, zoomScale)
                drawLine(
                    color = CyberAmber,
                    start = s0,
                    end = s1,
                    strokeWidth = 2.5f
                )
                drawCircle(
                    color = CyberAmber,
                    radius = 4f,
                    center = s0
                )
                drawCircle(
                    color = SciFiTeal,
                    radius = 5f,
                    center = s1
                )
            }

            // HUD Overlays
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp),
                shape = RoundedCornerShape(6.dp),
                color = StudioSurface.copy(alpha = 0.85f)
            ) {
                Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                    Text(
                        text = "CAM: $cameraMovement ($shotType)",
                        fontSize = 10.sp,
                        color = CyberAmber,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "ACTION: $characterAction | LIGHT: $lightingMood",
                        fontSize = 9.sp,
                        color = TextSecondary,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Playback Play/Pause Floating overlay
            IconButton(
                onClick = { isPlaying = !isPlaying },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .background(CyberAmber, CircleShape)
                    .size(40.dp)
                    .testTag("play_pause_viewport_button")
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Play or Pause Preview",
                    tint = StudioDarkBg
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Time Scrubber & Frame Counter
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${(currentProgress * 5.0).format(1)}s",
                fontSize = 11.sp,
                color = CyberAmber,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.width(36.dp)
            )
            Slider(
                value = currentProgress,
                onValueChange = {
                    isPlaying = false
                    playbackProgress = it
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("viewport_timeline_slider"),
                colors = SliderDefaults.colors(
                    thumbColor = CyberAmber,
                    activeTrackColor = CyberAmber,
                    inactiveTrackColor = StudioOutline
                )
            )
            Text(
                text = "5.0s",
                fontSize = 11.sp,
                color = TextSecondary,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.width(36.dp)
            )
        }
    }
}

private fun Double.format(digits: Int) = String.format("%.${digits}f", this)
