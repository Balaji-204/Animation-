package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID

class AnimationRepository(private val dao: OfflineAnimDao) {

    val allProjects: Flow<List<ProjectEntity>> = dao.getAllProjects()
    val allRenderJobs: Flow<List<RenderJobEntity>> = dao.getAllRenderJobs()

    fun getScenesForProject(projectId: String): Flow<List<SceneEntity>> {
        return dao.getScenesForProject(projectId)
    }

    suspend fun getProject(projectId: String): ProjectEntity? {
        return dao.getProjectById(projectId)
    }

    suspend fun seedSampleIfEmpty() {
        val existing = dao.getAllProjects().firstOrNull()
        if (existing.isNullOrEmpty()) {
            val sampleId = "sample_robot_flower"
            val sampleProject = ProjectEntity(
                id = sampleId,
                title = "The Desert Bloom",
                ideaPrompt = "A small robot finds a vibrant flower in an abandoned desert.",
                stylePreset = "low_poly_cartoon",
                fps = 24,
                resolution = "1920x1080",
                status = "draft",
                sceneCount = 2,
                totalDurationSec = 9.5
            )
            dao.insertProject(sampleProject)

            val scene1 = SceneEntity(
                id = "scene_01_${UUID.randomUUID().toString().take(6)}",
                projectId = sampleId,
                orderIndex = 0,
                sceneTitle = "Dune Reconnaissance",
                summary = "Robot wanders the vast dunes and spots a strange thermal signature.",
                durationSec = 4.5,
                locationId = "loc_desert_dunes",
                timeOfDay = "golden_hour",
                lightingMood = "warm_sunlight",
                shotType = "medium",
                cameraMovement = "dolly",
                charactersJson = "[{\"id\":\"robot_scout\",\"action\":\"walk\",\"pos\":[-1.5,0,0]}]",
                dialogueJson = "[{\"speaker\":\"robot_scout\",\"emotion\":\"inquisitive\",\"text\":\"Thermal signature detected. Sand temperature dropping.\"}]",
                propsJson = "[{\"id\":\"flower\",\"pos\":[0.3,1.2,0]}]"
            )

            val scene2 = SceneEntity(
                id = "scene_02_${UUID.randomUUID().toString().take(6)}",
                projectId = sampleId,
                orderIndex = 1,
                sceneTitle = "A Gentle Touch",
                summary = "The robot approaches the glowing desert flower and extends a hand.",
                durationSec = 5.0,
                locationId = "loc_desert_dunes",
                timeOfDay = "golden_hour",
                lightingMood = "dramatic_sunbeam",
                shotType = "close_up",
                cameraMovement = "orbit",
                charactersJson = "[{\"id\":\"robot_scout\",\"action\":\"reach_out\",\"pos\":[0.1,0.6,0]}]",
                dialogueJson = "[{\"speaker\":\"robot_scout\",\"emotion\":\"wonder\",\"text\":\"Life persists. I will shield you from the sandstorms.\"}]",
                propsJson = "[{\"id\":\"flower\",\"pos\":[0.3,1.2,0]}]"
            )

            dao.insertScenes(listOf(scene1, scene2))
        }
    }

    suspend fun createProjectFromIdea(
        title: String,
        ideaPrompt: String,
        stylePreset: String,
        fps: Int = 24,
        resolution: String = "1920x1080"
    ): String {
        val projectId = "proj_${UUID.randomUUID().toString().take(8)}"
        val project = ProjectEntity(
            id = projectId,
            title = title.ifBlank { ideaPrompt.take(24).trim().capitalizeFirst() },
            ideaPrompt = ideaPrompt,
            stylePreset = stylePreset,
            fps = fps,
            resolution = resolution,
            status = "draft",
            sceneCount = 2,
            totalDurationSec = 9.0
        )
        dao.insertProject(project)

        val sc1 = SceneEntity(
            id = "sc1_${UUID.randomUUID().toString().take(6)}",
            projectId = projectId,
            orderIndex = 0,
            sceneTitle = "Establishing Horizon",
            summary = "Opening shot establishing the environment for: $ideaPrompt",
            durationSec = 4.5,
            locationId = "loc_desert_dunes",
            timeOfDay = "golden_hour",
            lightingMood = "warm_sunlight",
            shotType = "wide",
            cameraMovement = "dolly",
            charactersJson = "[{\"id\":\"robot_scout\",\"action\":\"walk\",\"pos\":[-1.0,0,0]}]",
            dialogueJson = "[{\"speaker\":\"robot_scout\",\"emotion\":\"curious\",\"text\":\"Perimeter scan initiated.\"}]",
            propsJson = "[{\"id\":\"flower\",\"pos\":[0.4,0.8,0]}]"
        )
        val sc2 = SceneEntity(
            id = "sc2_${UUID.randomUUID().toString().take(6)}",
            projectId = projectId,
            orderIndex = 1,
            sceneTitle = "Climax & Discovery",
            summary = "Character interacts with central narrative focus.",
            durationSec = 4.5,
            locationId = "loc_desert_dunes",
            timeOfDay = "golden_hour",
            lightingMood = "warm_sunlight",
            shotType = "close_up",
            cameraMovement = "orbit",
            charactersJson = "[{\"id\":\"robot_scout\",\"action\":\"look_at\",\"pos\":[0.1,0.5,0]}]",
            dialogueJson = "[{\"speaker\":\"robot_scout\",\"emotion\":\"wonder\",\"text\":\"Discovery verified.\"}]",
            propsJson = "[{\"id\":\"flower\",\"pos\":[0.4,0.8,0]}]"
        )
        dao.insertScenes(listOf(sc1, sc2))
        return projectId
    }

    suspend fun duplicateProject(source: ProjectEntity) {
        val newId = "proj_${UUID.randomUUID().toString().take(8)}"
        val copy = source.copy(
            id = newId,
            title = "${source.title} (Copy)",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        dao.insertProject(copy)
    }

    suspend fun deleteProject(id: String) {
        dao.deleteScenesForProject(id)
        dao.deleteProject(id)
    }

    suspend fun updateScene(scene: SceneEntity) {
        dao.updateScene(scene)
    }

    suspend fun addRenderJob(job: RenderJobEntity) {
        dao.insertRenderJob(job)
    }

    suspend fun updateRenderJob(job: RenderJobEntity) {
        dao.updateRenderJob(job)
    }

    suspend fun deleteRenderJob(jobId: String) {
        dao.deleteRenderJob(jobId)
    }
}

private fun String.capitalizeFirst(): String {
    return replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}
