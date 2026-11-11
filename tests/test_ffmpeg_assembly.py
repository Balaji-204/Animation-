"""Unit tests for FFmpeg command construction and audio ducking graphs."""

import unittest
from pipeline.ffmpeg_assembler import AssemblyOptions, FFmpegAssembler


class TestFFmpegAssembler(unittest.TestCase):
    def setUp(self):
        self.assembler = FFmpegAssembler()

    def test_single_scene_command(self):
        cmd = self.assembler.build_master_assembly_command(
            scene_video_files=["scene_01.mp4"],
            dialogue_audio_file="dial.wav",
            music_audio_file="music.wav",
            subtitle_srt_file="subs.srt",
            output_final_mp4="final.mp4",
            options=AssemblyOptions(burn_subtitles=False, check_files_exist=False)
        )
        self.assertIn("final.mp4", cmd)
        self.assertIn("-filter_complex", cmd)
        # Verify sidechain ducking filter is present
        filter_str = " ".join(cmd)
        self.assertIn("sidechaincompress", filter_str)


if __name__ == "__main__":
    unittest.main()
