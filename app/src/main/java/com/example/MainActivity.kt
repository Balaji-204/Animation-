package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Hardware
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.data.AnimationRepository
import com.example.data.AppDatabase
import com.example.data.ProjectEntity
import com.example.data.RenderJobEntity
import com.example.data.SceneEntity
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DiagnosticsScreen
import com.example.ui.screens.ModelManagerScreen
import com.example.ui.screens.RenderQueueScreen
import com.example.ui.screens.StudioEditorScreen
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.OfflineAnimStudioTheme
import com.example.ui.theme.StudioDarkBg
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextTertiary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

enum class AppNavDestination {
    PROJECTS,
    RENDER_QUEUE,
    MODELS,
    DIAGNOSTICS
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = AppDatabase.getDatabase(applicationContext)
        val repository = AnimationRepository(db.dao())

        setContent {
            OfflineAnimStudioTheme {
                MainApp(repository = repository)
            }
        }
    }
}

@Composable
fun MainApp(repository: AnimationRepository) {
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        repository.seedSampleIfEmpty()
    }

    val projects by repository.allProjects.collectAsState(initial = emptyList())
    val renderJobs by repository.allRenderJobs.collectAsState(initial = emptyList())

    var activeDestination by remember { mutableStateOf(AppNavDestination.PROJECTS) }
    var activeProjectId by remember { mutableStateOf<String?>(null) }
    val activeProject = projects.firstOrNull { it.id == activeProjectId }

    val activeProjectScenes by remember(activeProjectId) {
        if (activeProjectId != null) {
            repository.getScenesForProject(activeProjectId!!)
        } else {
            kotlinx.coroutines.flow.flowOf(emptyList())
        }
    }.collectAsState(initial = emptyList())

    // If a project is open in the Studio Editor, show editor screen
    if (activeProject != null) {
        StudioEditorScreen(
            project = activeProject,
            scenes = activeProjectScenes,
            onBack = { activeProjectId = null },
            onUpdateScene = { updatedScene ->
                scope.launch { repository.updateScene(updatedScene) }
            },
            onRenderProject = { proj ->
                val jobId = "job_${UUID.randomUUID().toString().take(8)}"
                val newJob = RenderJobEntity(
                    id = jobId,
                    projectId = proj.id,
                    projectName = proj.title,
                    status = "rendering",
                    progressPercent = 0.1f,
                    etaSeconds = 25,
                    currentStage = "1/4: Staging 3D Meshes & Camera",
                    logOutput = "[Blender]: Initialized headless session\n[Staging]: Staging desert terrain and scout robot..."
                )
                scope.launch {
                    repository.addRenderJob(newJob)
                    activeDestination = AppNavDestination.RENDER_QUEUE
                    activeProjectId = null

                    // Simulate stage progression
                    delay(1200)
                    repository.updateRenderJob(
                        newJob.copy(
                            progressPercent = 0.35f,
                            etaSeconds = 18,
                            currentStage = "2/4: Blender EEVEE Shading & Keyframes",
                            logOutput = newJob.logOutput + "\n[EEVEE]: Rendering 240 frames @ 24fps..."
                        )
                    )
                    delay(1500)
                    repository.updateRenderJob(
                        newJob.copy(
                            progressPercent = 0.70f,
                            etaSeconds = 8,
                            currentStage = "3/4: Piper Neural Voice & Subtitle Sync",
                            logOutput = newJob.logOutput + "\n[Piper]: Synthesized dialogue WAVs\n[Whisper]: Generated SRT timestamps..."
                        )
                    )
                    delay(1500)
                    repository.updateRenderJob(
                        newJob.copy(
                            progressPercent = 1.0f,
                            etaSeconds = 0,
                            status = "completed",
                            currentStage = "4/4: Complete (MP4 Rendered)",
                            logOutput = newJob.logOutput + "\n[FFmpeg]: Sidechain audio ducking applied (-12dB)\n[Output]: final_animation.mp4 ready!"
                        )
                    )
                }
            }
        )
        return
    }

    Scaffold(
        containerColor = StudioDarkBg,
        bottomBar = {
            NavigationBar(
                containerColor = StudioSurface,
                modifier = Modifier.testTag("main_navigation_bar")
            ) {
                NavigationBarItem(
                    selected = activeDestination == AppNavDestination.PROJECTS,
                    onClick = { activeDestination = AppNavDestination.PROJECTS },
                    icon = { Icon(Icons.Default.Movie, contentDescription = "Projects") },
                    label = { Text("Projects") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyberAmber,
                        selectedTextColor = CyberAmber,
                        indicatorColor = StudioDarkBg,
                        unselectedIconColor = TextTertiary,
                        unselectedTextColor = TextTertiary
                    ),
                    modifier = Modifier.testTag("nav_item_projects")
                )
                NavigationBarItem(
                    selected = activeDestination == AppNavDestination.RENDER_QUEUE,
                    onClick = { activeDestination = AppNavDestination.RENDER_QUEUE },
                    icon = { Icon(Icons.Default.HourglassTop, contentDescription = "Queue") },
                    label = { Text("Render Queue") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyberAmber,
                        selectedTextColor = CyberAmber,
                        indicatorColor = StudioDarkBg,
                        unselectedIconColor = TextTertiary,
                        unselectedTextColor = TextTertiary
                    ),
                    modifier = Modifier.testTag("nav_item_queue")
                )
                NavigationBarItem(
                    selected = activeDestination == AppNavDestination.MODELS,
                    onClick = { activeDestination = AppNavDestination.MODELS },
                    icon = { Icon(Icons.Default.Layers, contentDescription = "Models") },
                    label = { Text("Models") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyberAmber,
                        selectedTextColor = CyberAmber,
                        indicatorColor = StudioDarkBg,
                        unselectedIconColor = TextTertiary,
                        unselectedTextColor = TextTertiary
                    ),
                    modifier = Modifier.testTag("nav_item_models")
                )
                NavigationBarItem(
                    selected = activeDestination == AppNavDestination.DIAGNOSTICS,
                    onClick = { activeDestination = AppNavDestination.DIAGNOSTICS },
                    icon = { Icon(Icons.Default.Speed, contentDescription = "Diagnostics") },
                    label = { Text("Diagnostics") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CyberAmber,
                        selectedTextColor = CyberAmber,
                        indicatorColor = StudioDarkBg,
                        unselectedIconColor = TextTertiary,
                        unselectedTextColor = TextTertiary
                    ),
                    modifier = Modifier.testTag("nav_item_diagnostics")
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (activeDestination) {
                AppNavDestination.PROJECTS -> {
                    DashboardScreen(
                        projects = projects,
                        onOpenProject = { id -> activeProjectId = id },
                        onCreateProject = { title, idea, style ->
                            scope.launch {
                                val newId = repository.createProjectFromIdea(title, idea, style)
                                activeProjectId = newId
                            }
                        },
                        onDuplicateProject = { proj ->
                            scope.launch { repository.duplicateProject(proj) }
                        },
                        onDeleteProject = { id ->
                            scope.launch { repository.deleteProject(id) }
                        }
                    )
                }
                AppNavDestination.RENDER_QUEUE -> {
                    RenderQueueScreen(
                        renderJobs = renderJobs,
                        onTogglePause = { job ->
                            scope.launch {
                                val newStatus = if (job.status == "paused") "rendering" else "paused"
                                repository.updateRenderJob(job.copy(status = newStatus))
                            }
                        },
                        onDeleteJob = { jobId ->
                            scope.launch { repository.deleteRenderJob(jobId) }
                        }
                    )
                }
                AppNavDestination.MODELS -> {
                    ModelManagerScreen()
                }
                AppNavDestination.DIAGNOSTICS -> {
                    DiagnosticsScreen()
                }
            }
        }
    }
}
