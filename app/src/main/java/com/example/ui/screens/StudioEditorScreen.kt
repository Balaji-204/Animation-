package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.ProjectEntity
import com.example.data.SceneEntity
import com.example.ui.components.Studio3DViewport
import com.example.ui.components.StudioTimelineView
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudioEditorScreen(
    project: ProjectEntity,
    scenes: List<SceneEntity>,
    onBack: () -> Unit,
    onUpdateScene: (SceneEntity) -> Unit,
    onRenderProject: (ProjectEntity) -> Unit
) {
    BackHandler { onBack() }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var selectedSceneIndex by remember { mutableIntStateOf(0) }

    val currentScene = scenes.getOrNull(selectedSceneIndex) ?: scenes.firstOrNull()

    Scaffold(
        containerColor = StudioDarkBg,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = project.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${project.stylePreset.replace('_', ' ')} • ${project.fps}fps • ${project.resolution}",
                                fontSize = 11.sp,
                                color = TextTertiary,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("editor_back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = { onRenderProject(project) },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberAmber, contentColor = StudioDarkBg),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("render_movie_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Movie,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Render MP4", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = StudioSurface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Mode Tabs: Viewport, Scene Graph, Timeline
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = StudioSurface,
                contentColor = CyberAmber,
                edgePadding = 12.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = CyberAmber
                    )
                }
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ViewInAr, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("3D Viewport")
                        }
                    }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Scene Graph")
                        }
                    }
                )
                Tab(
                    selected = selectedTabIndex == 2,
                    onClick = { selectedTabIndex = 2 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.GraphicEq, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Timeline")
                        }
                    }
                )
            }

            // Scene Switcher Strip
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(StudioSurfaceVariant)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(scenes) { index, sc ->
                    val isSelected = index == selectedSceneIndex
                    Surface(
                        modifier = Modifier
                            .clickable { selectedSceneIndex = index }
                            .testTag("scene_chip_$index"),
                        shape = RoundedCornerShape(6.dp),
                        color = if (isSelected) CyberAmber else StudioCardElevated,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) CyberAmber else StudioOutline
                        )
                    ) {
                        Text(
                            text = "Scene ${index + 1}: ${sc.shotType.uppercase()}",
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) StudioDarkBg else TextPrimary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            // Tab Content
            when (selectedTabIndex) {
                0 -> {
                    // 3D Viewport Tab
                    Studio3DViewport(
                        modifier = Modifier.weight(1f),
                        sceneTitle = currentScene?.sceneTitle ?: "3D Scene Preview",
                        cameraMovement = currentScene?.cameraMovement ?: "dolly",
                        shotType = currentScene?.shotType ?: "medium",
                        characterAction = if (selectedSceneIndex == 0) "walk" else "reach_out",
                        lightingMood = currentScene?.lightingMood ?: "warm_sunlight"
                    )
                }
                1 -> {
                    // Scene Graph Inspector Tab
                    if (currentScene != null) {
                        SceneGraphInspector(
                            scene = currentScene,
                            onSave = { updated -> onUpdateScene(updated) }
                        )
                    }
                }
                2 -> {
                    // Timeline Tab
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                    ) {
                        StudioTimelineView(
                            totalDurationSec = project.totalDurationSec
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SceneGraphInspector(
    scene: SceneEntity,
    onSave: (SceneEntity) -> Unit
) {
    var title by remember(scene) { mutableStateOf(scene.sceneTitle) }
    var summary by remember(scene) { mutableStateOf(scene.summary) }
    var duration by remember(scene) { mutableStateOf(scene.durationSec.toString()) }
    var shotType by remember(scene) { mutableStateOf(scene.shotType) }
    var cameraMovement by remember(scene) { mutableStateOf(scene.cameraMovement) }
    var lightingMood by remember(scene) { mutableStateOf(scene.lightingMood) }
    var isRegenerating by remember { mutableStateOf(false) }

    val cameraMovements = listOf("dolly", "orbit", "pan", "static", "zoom")
    val shotTypes = listOf("medium", "close_up", "wide", "low_angle", "bird_eye")
    val lightingMoods = listOf("warm_sunlight", "golden_hour", "dramatic_sunbeam", "dusk", "night")
    val characterActions = listOf("walk", "run", "wave", "sit", "look_at", "pick_up", "idle", "reach_out")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Regenerate Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SCENE GRAPH PARAMETERS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = CyberAmber,
                fontFamily = FontFamily.Monospace
            )

            Button(
                onClick = {
                    isRegenerating = true
                    // Simulate fast local LLM scene repair & regeneration
                    val repaired = scene.copy(
                        summary = "${scene.summary} (Refined by local LLM)",
                        cameraMovement = if (cameraMovement == "dolly") "orbit" else "dolly"
                    )
                    cameraMovement = repaired.cameraMovement
                    onSave(repaired)
                    isRegenerating = false
                },
                colors = ButtonDefaults.buttonColors(containerColor = StudioCardElevated, contentColor = SciFiTeal),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.testTag("regenerate_scene_button")
            ) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (isRegenerating) "Refining..." else "Regenerate Scene Only", fontSize = 11.sp)
            }
        }

        // Editable Scene Title
        OutlinedTextField(
            value = title,
            onValueChange = {
                title = it
                onSave(scene.copy(sceneTitle = it))
            },
            label = { Text("Scene Title") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedBorderColor = CyberAmber,
                unfocusedBorderColor = StudioOutline
            )
        )

        // Summary
        OutlinedTextField(
            value = summary,
            onValueChange = {
                summary = it
                onSave(scene.copy(summary = it))
            },
            label = { Text("Action Synopsis") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedBorderColor = CyberAmber,
                unfocusedBorderColor = StudioOutline
            )
        )

        // Duration Input
        OutlinedTextField(
            value = duration,
            onValueChange = {
                duration = it
                val durDouble = it.toDoubleOrNull() ?: 5.0
                onSave(scene.copy(durationSec = durDouble))
            },
            label = { Text("Duration (seconds)") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedBorderColor = CyberAmber,
                unfocusedBorderColor = StudioOutline
            )
        )

        // Camera Shot Type Chips
        Text(text = "SHOT TYPE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SciFiTeal)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            shotTypes.forEach { st ->
                FilterChip(
                    selected = shotType == st,
                    onClick = {
                        shotType = st
                        onSave(scene.copy(shotType = st))
                    },
                    label = { Text(st.replace('_', ' ').uppercase(), fontSize = 10.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SciFiTeal.copy(alpha = 0.3f),
                        selectedLabelColor = SciFiTeal
                    )
                )
            }
        }

        // Camera Movement
        Text(text = "CAMERA MOVEMENT (WHITELIST)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CyberAmber)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            cameraMovements.forEach { mv ->
                FilterChip(
                    selected = cameraMovement == mv,
                    onClick = {
                        cameraMovement = mv
                        onSave(scene.copy(cameraMovement = mv))
                    },
                    label = { Text(mv.uppercase(), fontSize = 10.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyberAmber.copy(alpha = 0.3f),
                        selectedLabelColor = CyberAmber
                    )
                )
            }
        }

        // Lighting Mood
        Text(text = "LIGHTING & ATMOSPHERE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            lightingMoods.take(3).forEach { lm ->
                FilterChip(
                    selected = lightingMood == lm,
                    onClick = {
                        lightingMood = lm
                        onSave(scene.copy(lightingMood = lm))
                    },
                    label = { Text(lm.replace('_', ' ').uppercase(), fontSize = 10.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = StudioCardElevated,
                        selectedLabelColor = TextPrimary
                    )
                )
            }
        }
    }
}
