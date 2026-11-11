"""Unit tests for scene schema validation and whitelist repair."""

import unittest
from pipeline.schema_validator import SchemaValidator, VALID_ACTIONS, VALID_CAMERA_MOVEMENTS


class TestSchemaValidator(unittest.TestCase):
    def setUp(self):
        self.validator = SchemaValidator()

    def test_valid_scene(self):
        valid_scene = {
            "scene_id": "scene_01",
            "duration_sec": 5.0,
            "location": "loc_desert_dunes",
            "time_of_day": "golden_hour",
            "lighting_mood": "warm_sunlight",
            "camera": {
                "shot_type": "medium",
                "movement": "dolly",
                "start_transform": {"position": [0.0, -3.0, 1.5], "rotation": [10.0, 0.0, 0.0]},
                "end_transform": {"position": [0.0, -2.5, 1.2], "rotation": [8.0, 0.0, 0.0]}
            },
            "characters": [
                {
                    "character_id": "robot_scout",
                    "asset_id": "char_robot_scout",
                    "actions": [{"action": "walk", "start_sec": 0.0, "end_sec": 3.0}]
                }
            ],
            "dialogue": [{"speaker": "robot", "text": "Testing 123"}]
        }
        res = self.validator.validate_and_repair_scene(valid_scene)
        self.assertTrue(res.is_valid)
        self.assertEqual(len(res.errors), 0)

    def test_repair_synonym_actions(self):
        dirty_scene = {
            "scene_id": "scene_02",
            "duration_sec": 4.0,
            "location": "loc_desert_dunes",
            "camera": {
                "movement": "tracking"  # Synonym for dolly
            },
            "characters": [
                {
                    "character_id": "robot",
                    "actions": [
                        {"action": "walking"},  # Synonym for walk
                        {"action": "stroll"},   # Synonym for walk
                        {"action": "touch"}     # Synonym for reach_out
                    ]
                }
            ]
        }
        res = self.validator.validate_and_repair_scene(dirty_scene)
        repaired = res.repaired_data
        self.assertEqual(repaired["camera"]["movement"], "dolly")
        actions = [a["action"] for a in repaired["characters"][0]["actions"]]
        self.assertEqual(actions, ["walk", "walk", "reach_out"])


if __name__ == "__main__":
    unittest.main()
