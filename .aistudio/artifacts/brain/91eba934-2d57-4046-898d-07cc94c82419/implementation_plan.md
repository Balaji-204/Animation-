# OfflineAnimStudio - Local Text-to-3D Animation Studio

A fully offline, zero-network animation studio that translates text ideas into finished 3D animated videos with character staging, camera trajectories, synthetic voiceover, subtitle alignment, and background audio. The solution provides both a high-fidelity interactive studio application running on device and a complete headless Python/Blender pipeline ready for standalone desktop execution.

## User Review & Critical Decisions

> [!IMPORTANT]
> The following parameters were confirmed during clarification and will govern the build:

- **Confirmed Target Platform & Hardware**: Primary execution on Android for mobile studio workflow with adaptive CPU/GPU profile detection; accompanied by portable headless desktop scripts (`pipeline/`, `blender_scripts/`, `assets/`).
- **Confirmed Licensing Standard**: Permissive open-source licenses (Apache 2.0 and MIT) across scripts, schemas, and asset definitions.
- **Confirmed Visual Aesthetic**: Stylized low-poly and vibrant cartoon aesthetic for the bundled asset catalog (characters, props, desert/nature biomes, and lighting setups).
- **Offline Guarantee**: Absolute zero runtime network requests; self-contained local models, bundled CC0/MIT audio clips, and pre-rigged procedural low-poly rigs.

---

## 1. Overview & Core Concept

**OfflineAnimStudio** removes the barrier between an imaginative story premise and a rendered 3D cinematic clip without depending on cloud servers, external subscriptions, or active internet connectivity. 

### Core Workflows
1. **Idea Ingestion & Script Drafting**: Users provide an idea (e.g., *"A small robot finds a vibrant flower in an abandoned desert"*), selecting an artistic style and duration target.
2. **Deterministic Scene-Graph Decomposition**: The system breaks the script into typed scene graphs specifying camera movement (dolly, orbit, pan, static), character poses, lighting moods, and dialogue.
3. **Rigged 3D Staging & Headless Blender Engine**: Characters and environment assets are placed into 3D scenes with coordinate transforms and keyframed actions.
4. **Audio & Subtitle Synthesis**: Speech lines are paired with offline phoneme durations and subtitle timestamps, cross-faded over mood-tagged musical beds.
5. **Interactive Studio & Render Pipeline**: Users can preview scenes, adjust camera angles and dialogue on an interactive timeline, inspect render queues, and trigger final video compilation.

---

## 2. User Experience & Visual Design

### Design Language & Atmosphere
- **Visual Aesthetic**: Studio Darkroom theme with deep slate charcoal surfaces (`#121316`), vibrant amber/cyber-gold highlights (`#E5A93C`), and soft cyan accents (`#4ECDC4`) reminiscent of professional 3D editing suites (Blender/Unreal Editor).
- **Layout Rhythm**: Dynamic split workspace supporting dual-pane viewport layouts, scrubbable horizontal timeline tracks, and collapsible scene inspector sheets.
- **Micro-Interactions**: Smooth spring-animated playback indicators, frame-scrub ripples, render progress rings, and real-time scene validation badges.

```
┌──────────────────────────────────────────────────────────────┐
│  OfflineAnimStudio               [Status: 100% Offline 🛡️]   │
├──────────────────────────────┬───────────────────────────────┤
│ 🎬 Idea & Prompt Studio      │ 👁️ Real-Time 3D Viewport      │
│ ┌──────────────────────────┐ │ ┌───────────────────────────┐ │
│ │ "A small robot finds a   │ │ │       [ 3D Scene ]        │ │
│ │  flower in the desert"   │ │ │     Robot 🤖   🌸 Flower  │ │
│ └──────────────────────────┘ │ │ Camera: Dolly-In (24fps)  │ │
│ Style: [Low-Poly Cartoon ▼] │ └───────────────────────────┘ │
│ [ Generate Scene Graph ⚡ ] │ Camera Controls: [Orbit][Pan] │
├──────────────────────────────┴───────────────────────────────┤
│ ⏱️ Interactive Timeline & Audio Tracks                       │
│ Scene 1: Robot Scan ───────▶ Scene 2: Approach ───────────▶  │
│ 🗣️ Voice: "What is this?"    🗣️ Voice: "It's alive!"        │
│ 🎵 Music: Desert Ambient (Ducking -12dB)                     │
├──────────────────────────────────────────────────────────────┤
│ 🚀 Render Queue | ⚙️ Offline Model Manager | 📊 Diagnostics │
└──────────────────────────────────────────────────────────────┘
```

---

## 3. Key Product Decisions & Trade-Offs

- **Hybrid Studio Engine**: 
  - *Chosen Approach*: Deliver a native Kotlin Compose mobile studio app with built-in Canvas 3D isometric scene previewer, SQLite/Room persistence, and schema validator, combined with the complete Python headless Blender/Ollama execution bundle for cross-platform desktop execution.
  - *Why*: Gives immediate interactive capability directly in AI Studio, while fulfilling the complete desktop Python/Blender pipeline deliverables.
- **Strict JSON-Schema Scene Graphs**:
  - *Chosen Approach*: Strictly enforce action whitelists (`walk`, `look_at`, `idle`, `reach_out`, `wave`), asset IDs, and camera shot types with automated schema repair.
  - *Why*: Eliminates hallucinated commands that crash Blender headless scripts or audio synchronizers.
- **Audio-First Timing Normalization**:
  - *Chosen Approach*: Scene durations automatically stretch or pad based on synthesized TTS audio clip lengths rather than rigid arbitrary timestamps.
  - *Why*: Guarantees that dialogue and emotional delivery are never cut off during cinematic sequences.

---

## 4. Technical Architecture & Data Strategy

```
┌─────────────────────────────────────────────────────────────────┐
│                       OfflineAnimStudio                         │
├───────────────────────────────┬─────────────────────────────────┤
│      Interactive UI Layer     │      Offline Pipeline Core      │
│  - Story Idea Editor          │  - Ollama Prompt Formatter      │
│  - 3D Canvas Scene Viewport   │  - JSON Schema Validator        │
│  - Multi-track Timeline       │  - Audio Timing & Ducking Sync  │
│  - Render Queue & Diagnostics │  - Headless Blender Generator   │
├───────────────────────────────┴─────────────────────────────────┤
│                     Local Persistence Engine                    │
│      Room SQLite DB: Projects • Scenes • Characters • Assets    │
├─────────────────────────────────────────────────────────────────┤
│                     Desktop Pipeline Bundle                     │
│  - pipeline/ (Ollama, validator, TTS, FFmpeg orchestrator)      │
│  - blender_scripts/ (Headless bpy scene builder & EEVEE render) │
│  - assets/ & models/ (Low-poly catalogs, audio library, setup)  │
└─────────────────────────────────────────────────────────────────┘
```

### Data Entities
- **Project**: `id`, `title`, `ideaPrompt`, `stylePreset`, `fps`, `resolution`, `status`, `createdAt`.
- **SceneGraph**: `sceneId`, `orderIndex`, `durationSec`, `locationId`, `lightingMood`, `timeOfDay`, `cameraConfig` (shot type, movement, coordinates).
- **SceneCharacter**: `characterId`, `assetModelId`, `startPos`, `endPos`, `actionSequence`.
- **DialogueTrack**: `id`, `speakerId`, `text`, `emotion`, `audioDurationSec`, `subtitleSrt`.
- **AudioMix**: `musicMood`, `duckingDb`, `soundEffectsJson`.

---

## 5. Milestone Build Plan

1. **Milestone 1: Project Setup & Schema Engine**
   - Initialize project identity in metadata and strings.
   - Implement the validated JSON schema models, Ollama system prompts, and repair loop.
2. **Milestone 2: Data Persistence & Architecture Layer**
   - Implement Room Database entities, DAOs, and repository for offline animation projects.
3. **Milestone 3: 3D Scene Builder & Interactive Viewport**
   - Build the interactive low-poly 3D projection viewport with camera movement, character staging, and lighting simulations.
4. **Milestone 4: Multi-Track Timeline & Audio Synchronizer**
   - Build timeline scrubbing with video, voiceover, and background music tracks with auto-ducking.
5. **Milestone 5: Desktop Pipeline Deliverables**
   - Generate production Python headless Blender scripts (`blender_scripts/`), pipeline orchestrator (`pipeline/`), setup scripts, and test suite.
6. **Milestone 6: Verification & Compilation**
   - Verify Android build with `compile_applet`, run unit tests, and confirm offline isolation.
