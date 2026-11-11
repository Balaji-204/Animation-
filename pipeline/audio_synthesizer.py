"""Offline audio synthesis and ducking calculator using Piper TTS."""

import json
import logging
import math
import os
import shutil
import struct
import subprocess
import wave
from dataclasses import dataclass
from pathlib import Path
from typing import Dict, List, Optional, Tuple

logger = logging.getLogger("OfflineAnimStudio.Audio")


@dataclass
class AudioDialogueClip:
    speaker: str
    text: str
    emotion: str
    output_wav_path: str
    duration_sec: float
    start_time_in_scene: float


class AudioSynthesizer:
    """Generates offline dialogue speech with Piper TTS and measures clip durations."""

    def __init__(self, piper_binary: str = "piper", voices_dir: str = "models/piper_voices"):
        self.piper_bin = shutil.which(piper_binary) or piper_binary
        self.voices_dir = Path(voices_dir)

    def is_piper_installed(self) -> bool:
        return shutil.which(self.piper_bin) is not None

    def synthesize_line(
        self,
        text: str,
        output_path: str,
        voice_model: Optional[str] = None
    ) -> float:
        """Synthesizes a line of text to WAV and returns the duration in seconds."""
        out_file = Path(output_path)
        out_file.parent.mkdir(parents=True, exist_ok=True)

        # If piper binary is available and model file exists
        model_path = self.voices_dir / f"{voice_model or 'en_US-lessac-medium'}.onnx"

        if self.is_piper_installed() and model_path.exists():
            try:
                cmd = [
                    self.piper_bin,
                    "--model", str(model_path),
                    "--output_file", str(out_file)
                ]
                proc = subprocess.Popen(cmd, stdin=subprocess.PIPE, stderr=subprocess.PIPE)
                proc.communicate(input=text.encode("utf-8"), timeout=15)
                if out_file.exists():
                    return self._get_wav_duration(str(out_file))
            except Exception as e:
                logger.warning(f"Piper execution failed: {e}. Generating procedural reference WAV.")

        # Standalone offline reference audio generation (valid PCM WAV)
        duration = self._estimate_spoken_duration(text)
        self._generate_synthetic_tone_wav(str(out_file), duration_sec=duration)
        return duration

    def _estimate_spoken_duration(self, text: str) -> float:
        """Calculates natural speaking duration (~150 words per minute plus punctuation pauses)."""
        words = text.split()
        word_count = max(1, len(words))
        # 150 words/min -> 2.5 words/sec -> 0.4s per word
        base_duration = word_count * 0.38
        punctuation_pause = (text.count(".") + text.count("!") + text.count("?")) * 0.3
        comma_pause = text.count(",") * 0.15
        return round(max(1.2, base_duration + punctuation_pause + comma_pause), 2)

    def _get_wav_duration(self, wav_path: str) -> float:
        try:
            with wave.open(wav_path, "rb") as wf:
                frames = wf.getnframes()
                rate = wf.getframerate()
                return round(frames / float(rate), 2)
        except Exception:
            return 2.0

    def _generate_synthetic_tone_wav(self, file_path: str, duration_sec: float, sample_rate: int = 24000):
        """Generates a soft, pleasant speech-frequency modulated sine wave (valid offline WAV)."""
        num_samples = int(sample_rate * duration_sec)
        with wave.open(file_path, "w") as wav:
            wav.setnchannels(1)
            wav.setsampwidth(2)
            wav.setframerate(sample_rate)
            frames = bytearray()
            freq = 220.0  # A3 speech formant tone
            for i in range(num_samples):
                t = i / sample_rate
                # Envelope: 0.05s attack, 0.05s decay
                env = 1.0
                if t < 0.05:
                    env = t / 0.05
                elif t > duration_sec - 0.05:
                    env = max(0.0, (duration_sec - t) / 0.05)
                # Formant modulation
                val = int(env * 8000.0 * math.sin(2 * math.pi * freq * t) * (0.8 + 0.2 * math.sin(2 * math.pi * 4 * t)))
                frames.extend(struct.pack("<h", max(-32768, min(32767, val))))
            wav.writeframes(frames)
