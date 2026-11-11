package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val ideaPrompt: String,
    val stylePreset: String = "low_poly_cartoon",
    val fps: Int = 24,
    val resolution: String = "1920x1080",
    val status: String = "draft", // draft, rendering, completed
    val sceneCount: Int = 2,
    val totalDurationSec: Double = 9.5,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "scenes")
data class SceneEntity(
    @PrimaryKey
    val id: String,
    val projectId: String,
    val orderIndex: Int,
    val sceneTitle: String,
    val summary: String,
    val durationSec: Double,
    val locationId: String = "loc_desert_dunes",
    val timeOfDay: String = "golden_hour",
    val lightingMood: String = "warm_sunlight",
    val shotType: String = "medium",
    val cameraMovement: String = "dolly",
    val charactersJson: String = "",
    val dialogueJson: String = "",
    val propsJson: String = ""
)

@Entity(tableName = "render_jobs")
data class RenderJobEntity(
    @PrimaryKey
    val id: String,
    val projectId: String,
    val projectName: String,
    val status: String, // queued, rendering, paused, completed, failed
    val progressPercent: Float,
    val etaSeconds: Int,
    val currentStage: String,
    val outputPath: String = "",
    val logOutput: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
