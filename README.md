# OfflineAnimStudio

> **Zero Cloud. Zero Subscriptions. 100% Offline 3D Text-to-Animation Suite.**

OfflineAnimStudio transforms simple natural-language story ideas into finished 3D animated cinematic videos (MP4) with camera choreography, character staging, speech synthesis, burned-in subtitles, and mood audio ducking.

---

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                       OfflineAnimStudio                         │
├───────────────────────────────┬─────────────────────────────────┤
│   Idea & Story Premise        │  Local Ollama Daemon (Port 11434│
│   "Robot finds desert bloom"  │  - Mistral / LLaMA 3.1 / Qwen   │
└───────────────┬───────────────┴────────────────┬────────────────┘
                ▼                                ▼
┌───────────────────────────────┐  ┌──────────────────────────────┐
│   JSON Schema Validator       │  │ Strict Whitelist & Repair    │
│   - Whitelist actions/cameras │◀─┤ - Auto-correction of synonyms│
└───────────────┬───────────────┘  └──────────────────────────────┘
                ▼
┌───────────────────────────────┬─────────────────────────────────┐
│ 3D Staging Engine             │ Headless Blender (bpy)          │
│ - Low-poly CC0 asset catalog  │ - Camera trajectories & lighting│
│ - Character coordinate anim   │ - EEVEE preview / Cycles final  │
└───────────────┬───────────────┴────────────────┬────────────────┘
                ▼                                ▼
┌───────────────────────────────┬─────────────────────────────────┐
│ Voice & Audio Synchronization │ Video Assembly & Delivery       │
│ - Piper Neural Offline TTS    │ - FFmpeg concat & sidechain duck│
│ - Whisper SRT subtitle timing │ - Burned-in subtitles & MP4 out │
└───────────────────────────────┴─────────────────────────────────┘
```

---

## Hardware Requirements

| Tier | System RAM | GPU / VRAM | Recommended Preset | Render Engine |
| :--- | :--- | :--- | :--- | :--- |
| **High Quality** | 32 GB RAM | NVIDIA RTX (8GB+ VRAM) | 1080p / 4K @ 30fps | Cycles / EEVEE Next |
| **Standard** | 16 GB RAM | Dedicated GPU (4-8GB VRAM) | 1080p @ 24fps | EEVEE Next |
| **CPU-Only** | 16 GB RAM | Integrated / CPU Fallback | 720p @ 24fps | EEVEE Fast Preview |

---

## Offline Installation & Air-Gapped Deployment

1. **Copy Binaries**:
   Ensure `ffmpeg`, `blender`, and `ollama` are in system `PATH`.
2. **Download Local Model Weights** (one-time on an internet-connected workstation):
   - Ollama: `ollama pull llama3.1:8b` (or `qwen2.5:7b`)
   - Piper TTS: `en_US-lessac-medium.onnx` into `models/piper_voices/`
3. **Verify Integrity**:
   Run the offline bundle verifier:
   ```bash
   python3 setup_bundle.py
   ```
4. **Offline Security Guarantee**:
   The built-in `OfflineNetworkGuard` intercepts outbound sockets and prevents network traffic to external IPs or domains.

---

## Running Unit Tests

```bash
python3 -m unittest discover tests
```

---

## License

Licensed under the **Apache-2.0** and **MIT** licenses. All bundled starter models, audio clips, and procedural shaders are CC0 1.0 Universal / Public Domain.
