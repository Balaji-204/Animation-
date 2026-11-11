"""FFmpeg video and audio assembly engine for OfflineAnimStudio.

Combines rendered PNG sequences / scene video clips, dialogue tracks,
mood music with dynamic speech ducking, and subtitle burn-in.
"""

import logging
import os
import shutil
import subprocess
from dataclasses import dataclass
from pathlib import Path
from typing import List, Optional

logger = logging.getLogger("OfflineAnimStudio.FFmpeg")


@dataclass
class AssemblyOptions:
    resolution: str = "1920x1080"  # or "1280x720", "3840x2160"
    fps: int = 24
    video_codec: str = "libx264"
    audio_codec: str = "aac"
    crf: int = 18
    preset: str = "medium"
    burn_subtitles: bool = True
    ducking_attenuation_db: float = -12.0
    check_files_exist: bool = True


class FFmpegAssembler:
    """Manages video stitching, audio mixing, and subtitle encoding via FFmpeg."""

    def __init__(self, ffmpeg_bin: str = "ffmpeg"):
        self.ffmpeg_bin = shutil.which(ffmpeg_bin) or ffmpeg_bin

    def is_available(self) -> bool:
        return shutil.which(self.ffmpeg_bin) is not None

    def build_scene_video_command(
        self,
        png_pattern: str,
        output_mp4: str,
        fps: int = 24,
        resolution: str = "1920x1080"
    ) -> List[str]:
        """Creates FFmpeg command to turn an image sequence into an intermediate MP4."""
        return [
            self.ffmpeg_bin,
            "-y",
            "-framerate", str(fps),
            "-i", png_pattern,
            "-c:v", "libx264",
            "-pix_fmt", "yuv420p",
            "-vf", f"scale={resolution.replace('x', ':')}:force_original_aspect_ratio=decrease,pad={resolution.replace('x', ':')}:(ow-iw)/2:(oh-ih)/2",
            output_mp4
        ]

    def build_master_assembly_command(
        self,
        scene_video_files: List[str],
        dialogue_audio_file: Optional[str],
        music_audio_file: Optional[str],
        subtitle_srt_file: Optional[str],
        output_final_mp4: str,
        options: AssemblyOptions
    ) -> List[str]:
        """Assembles multi-scene video with audio ducking and subtitle burn-in."""
        cmd = [self.ffmpeg_bin, "-y"]

        # Input videos via concat file or list
        filter_complex_parts = []
        input_count = 0

        for vid in scene_video_files:
            cmd.extend(["-i", vid])
            input_count += 1

        # Video concatenation
        if input_count > 1:
            concat_str = "".join([f"[{i}:v]" for i in range(input_count)])
            filter_complex_parts.append(f"{concat_str}concat=n={input_count}:v=1:a=0[vbase]")
            current_video_label = "[vbase]"
        elif input_count == 1:
            current_video_label = "[0:v]"
        else:
            raise ValueError("At least one scene video required for assembly.")

        # Subtitle burn-in filter
        if options.burn_subtitles and subtitle_srt_file and os.path.exists(subtitle_srt_file):
            # Escape path for FFmpeg filter syntax
            escaped_srt = str(Path(subtitle_srt_file).resolve()).replace("\\", "/").replace(":", "\\:")
            filter_complex_parts.append(
                f"{current_video_label}subtitles=filename='{escaped_srt}':force_style='FontSize=20,PrimaryColour=&H00FFFFFF,OutlineColour=&H00000000,BackColour=&H80000000'[vsub]"
            )
            current_video_label = "[vsub]"

        # Audio inputs
        dialogue_idx = None
        music_idx = None

        dialogue_valid = bool(dialogue_audio_file and (not options.check_files_exist or os.path.exists(dialogue_audio_file)))
        music_valid = bool(music_audio_file and (not options.check_files_exist or os.path.exists(music_audio_file)))

        if dialogue_valid:
            cmd.extend(["-i", dialogue_audio_file])
            dialogue_idx = input_count
            input_count += 1

        if music_valid:
            cmd.extend(["-i", music_audio_file])
            music_idx = input_count
            input_count += 1

        # Audio ducking and mixing
        if dialogue_idx is not None and music_idx is not None:
            # Duck music under speech using sidechaincompress filter
            filter_complex_parts.append(
                f"[{music_idx}:a][{dialogue_idx}:a]sidechaincompress=threshold=0.08:ratio=6:attack=50:release=350[ducked_music];"
                f"[{dialogue_idx}:a][ducked_music]amix=inputs=2:duration=first:dropout_transition=2[aout]"
            )
            audio_map = "[aout]"
        elif dialogue_idx is not None:
            audio_map = f"{dialogue_idx}:a"
        elif music_idx is not None:
            audio_map = f"{music_idx}:a"
        else:
            audio_map = None

        # Build full filter_complex
        if filter_complex_parts:
            cmd.extend(["-filter_complex", ";".join(filter_complex_parts)])
            cmd.extend(["-map", current_video_label])
        else:
            cmd.extend(["-map", "0:v"])

        if audio_map:
            cmd.extend(["-map", audio_map])

        cmd.extend([
            "-c:v", options.video_codec,
            "-preset", options.preset,
            "-crf", str(options.crf),
            "-c:a", options.audio_codec,
            "-b:a", "192k",
            output_final_mp4
        ])

        return cmd

    def assemble(self, command: List[str]) -> bool:
        """Executes the assembled FFmpeg command."""
        logger.info(f"Running FFmpeg: {' '.join(command)}")
        try:
            res = subprocess.run(command, stdout=subprocess.PIPE, stderr=subprocess.PIPE, check=True)
            return res.returncode == 0
        except Exception as e:
            logger.error(f"FFmpeg assembly failed: {e}")
            return False
