package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.data.ProjectEntity
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

@Composable
fun DashboardScreen(
    projects: List<ProjectEntity>,
    onOpenProject: (String) -> Unit,
    onCreateProject: (String, String, String) -> Unit,
    onDuplicateProject: (ProjectEntity) -> Unit,
    onDeleteProject: (String) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = StudioDarkBg,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = CyberAmber,
                contentColor = StudioDarkBg,
                modifier = Modifier.testTag("create_project_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Create New Project")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "OfflineAnimStudio",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Local 3D Text-to-Video Engine",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = StudioCardElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, StatusSuccess.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = StatusSuccess,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "100% OFFLINE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = StatusSuccess,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Projects List
            if (projects.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Movie,
                            contentDescription = null,
                            tint = TextTertiary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No animation projects yet.",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Tap + to start from an idea.",
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
                    items(projects, key = { it.id }) { proj ->
                        ProjectCard(
                            project = proj,
                            onClick = { onOpenProject(proj.id) },
                            onDuplicate = { onDuplicateProject(proj) },
                            onDelete = { onDeleteProject(proj.id) }
                        )
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateProjectDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { title, idea, style ->
                onCreateProject(title, idea, style)
                showCreateDialog = false
            }
        )
    }
}

@Composable
fun ProjectCard(
    project: ProjectEntity,
    onClick: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("project_card_${project.id}"),
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
                    text = project.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Badge(
                    containerColor = StudioCardElevated,
                    contentColor = CyberAmber
                ) {
                    Text(
                        text = project.stylePreset.replace("_", " ").uppercase(),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = project.ideaPrompt,
                fontSize = 13.sp,
                color = TextSecondary,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${project.sceneCount} SCENES",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = SciFiTeal,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = " • ",
                        color = TextTertiary,
                        fontSize = 10.sp
                    )
                    Text(
                        text = "${project.totalDurationSec}s @ ${project.fps}FPS",
                        fontSize = 10.sp,
                        color = TextTertiary,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = " • ",
                        color = TextTertiary,
                        fontSize = 10.sp
                    )
                    Text(
                        text = project.resolution,
                        fontSize = 10.sp,
                        color = TextTertiary,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row {
                    IconButton(
                        onClick = onDuplicate,
                        modifier = Modifier.size(32.dp).testTag("duplicate_btn_${project.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Duplicate Project",
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp).testTag("delete_btn_${project.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Project",
                            tint = TextTertiary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CreateProjectDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var idea by remember { mutableStateOf("") }
    var selectedStyle by remember { mutableStateOf("low_poly_cartoon") }

    val styleOptions = listOf(
        "low_poly_cartoon" to "Low-Poly Cartoon",
        "claymation" to "Claymation",
        "anime_cel" to "Anime Cel-Shaded",
        "stylized_scifi" to "Sci-Fi Realistic"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = StudioSurface,
        title = {
            Text(
                text = "New 3D Animation",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Project Title") },
                    placeholder = { Text("The Desert Bloom") },
                    modifier = Modifier.fillMaxWidth().testTag("project_title_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = CyberAmber,
                        unfocusedBorderColor = StudioOutline
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = idea,
                    onValueChange = { idea = it },
                    label = { Text("Story Idea / Premise") },
                    placeholder = { Text("A small robot finds a flower in a desert...") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth().testTag("story_idea_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = CyberAmber,
                        unfocusedBorderColor = StudioOutline
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Suggestion chips for fast testing
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    SuggestionChip(
                        onClick = {
                            title = "Desert Bloom"
                            idea = "A small scout robot finds a vibrant flower in an abandoned desert."
                        },
                        label = { Text("Robot & Flower", fontSize = 10.sp) },
                        colors = SuggestionChipDefaults.suggestionChipColors(containerColor = StudioSurfaceVariant)
                    )
                    SuggestionChip(
                        onClick = {
                            title = "Orbital Janitor"
                            idea = "An orbital cleaning drone bumps into a mysterious ancient cargo crate."
                        },
                        label = { Text("Space Drone", fontSize = 10.sp) },
                        colors = SuggestionChipDefaults.suggestionChipColors(containerColor = StudioSurfaceVariant)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "VISUAL STYLE PRESET",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberAmber,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(6.dp))

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    styleOptions.forEach { (key, label) ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedStyle = key },
                            shape = RoundedCornerShape(6.dp),
                            color = if (selectedStyle == key) CyberAmber.copy(alpha = 0.2f) else StudioSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (selectedStyle == key) CyberAmber else StudioOutline
                            )
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                color = if (selectedStyle == key) CyberAmber else TextPrimary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val finalIdea = idea.ifBlank { "A small robot finds a flower in a desert." }
                    val finalTitle = title.ifBlank { "New Animation" }
                    onConfirm(finalTitle, finalIdea, selectedStyle)
                },
                modifier = Modifier.testTag("submit_create_project_btn")
            ) {
                Text("Generate Scene Graph", color = CyberAmber, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}
