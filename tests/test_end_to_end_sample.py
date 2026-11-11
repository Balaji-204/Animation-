"""End-to-end unit test executing a 10-second sample animation through the pipeline."""

import shutil
import tempfile
import unittest
from pathlib import Path

from pipeline.pipeline_orchestrator import AnimationPipeline


class TestEndToEndSample(unittest.TestCase):
    def setUp(self):
        self.test_dir = tempfile.mkdtemp(prefix="anim_test_")

    def tearDown(self):
        shutil.rmtree(self.test_dir, ignore_errors=True)

    def test_pipeline_execution(self):
        pipeline = AnimationPipeline(project_dir=self.test_dir)
        progress_events = []

        def on_progress(p):
            progress_events.append(p.stage_name)

        final_out = Path(self.test_dir) / "output.mp4"
        result = pipeline.run_full_pipeline(
            idea="A small scout robot finds a flower in a desert",
            output_mp4_path=str(final_out),
            progress_cb=on_progress
        )

        self.assertEqual(result, str(final_out))
        self.assertTrue(len(progress_events) >= 3)
        self.assertTrue((Path(self.test_dir) / "subtitles.srt").exists())
        self.assertTrue((Path(self.test_dir) / "audio").exists())


if __name__ == "__main__":
    unittest.main()
