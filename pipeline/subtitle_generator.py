"""Offline subtitle generation and SRT timestamp alignment."""

from dataclasses import dataclass
from pathlib import Path
from typing import List, Optional


@dataclass
class SubtitleCue:
    index: int
    start_sec: float
    end_sec: float
    speaker: str
    text: str


def format_srt_time(seconds: float) -> str:
    """Formats float seconds into SRT timestamp string HH:MM:SS,mmm."""
    hrs = int(seconds // 3600)
    mins = int((seconds % 3600) // 60)
    secs = int(seconds % 60)
    millis = int(round((seconds - int(seconds)) * 1000))
    return f"{hrs:02d}:{mins:02d}:{secs:02d},{millis:03d}"


class SubtitleGenerator:
    """Creates standards-compliant .srt files from timed dialogue events."""

    def __init__(self, whisper_binary: Optional[str] = None):
        self.whisper_bin = whisper_binary

    def build_srt(self, cues: List[SubtitleCue], output_path: str) -> str:
        """Writes cues into an SRT subtitle file."""
        lines = []
        for cue in cues:
            start_str = format_srt_time(cue.start_sec)
            end_str = format_srt_time(cue.end_sec)
            speaker_prefix = f"[{cue.speaker.upper()}]: " if cue.speaker else ""
            lines.append(f"{cue.index}")
            lines.append(f"{start_str} --> {end_str}")
            lines.append(f"{speaker_prefix}{cue.text}")
            lines.append("")

        content = "\n".join(lines)
        out_file = Path(output_path)
        out_file.parent.mkdir(parents=True, exist_ok=True)
        with open(out_file, "w", encoding="utf-8") as f:
            f.write(content)
        return str(out_file)
