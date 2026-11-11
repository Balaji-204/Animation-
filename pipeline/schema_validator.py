"""Schema validation and whitelist enforcement for OfflineAnimStudio.

Validates LLM-generated script and scene graphs. Provides automatic repair
and retry prompt generation when the LLM outputs unknown actions or malformed structure.
"""

from dataclasses import dataclass
from typing import Any, Dict, List, Optional, Set, Tuple
import json

VALID_ACTIONS: Set[str] = {
    "walk", "run", "wave", "sit", "look_at", "pick_up", "idle", "jump", "reach_out"
}

VALID_CAMERA_MOVEMENTS: Set[str] = {
    "static", "pan", "dolly", "orbit", "zoom"
}

VALID_SHOT_TYPES: Set[str] = {
    "wide", "medium", "close_up", "over_the_shoulder", "low_angle", "bird_eye"
}

VALID_TIMES_OF_DAY: Set[str] = {
    "dawn", "noon", "golden_hour", "dusk", "night"
}

ACTION_SYNONYMS: Dict[str, str] = {
    "walking": "walk",
    "stroll": "walk",
    "running": "run",
    "sprint": "run",
    "waving": "wave",
    "greeting": "wave",
    "sitting": "sit",
    "sit_down": "sit",
    "look": "look_at",
    "watching": "look_at",
    "pickup": "pick_up",
    "grab": "pick_up",
    "take": "pick_up",
    "stand": "idle",
    "standing": "idle",
    "wait": "idle",
    "jumping": "jump",
    "hop": "jump",
    "reach": "reach_out",
    "touch": "reach_out",
    "extend_hand": "reach_out",
}

CAMERA_SYNONYMS: Dict[str, str] = {
    "truck": "pan",
    "tilt": "pan",
    "tracking": "dolly",
    "push_in": "dolly",
    "pull_out": "dolly",
    "rotate": "orbit",
    "circle": "orbit",
    "zoom_in": "zoom",
    "zoom_out": "zoom",
    "fixed": "static",
    "still": "static",
}


@dataclass
class ValidationResult:
    is_valid: bool
    errors: List[str]
    repaired_data: Optional[Dict[str, Any]] = None
    retry_prompt: Optional[str] = None


class SchemaValidator:
    """Validates and repairs scene graphs and scripts against strict rules."""

    def __init__(self, allowed_asset_ids: Optional[Set[str]] = None):
        self.allowed_assets = allowed_asset_ids or {
            "char_robot_scout", "char_alien_creature", "prop_alien_flower",
            "prop_space_crate", "loc_desert_dunes", "loc_ancient_ruins", "loc_scifi_pad"
        }

    def validate_and_repair_scene(self, scene: Dict[str, Any]) -> ValidationResult:
        errors: List[str] = []
        repaired = json.loads(json.dumps(scene))  # deep copy

        # 1. Required top-level keys
        for key in ["scene_id", "duration_sec", "location", "camera", "characters"]:
            if key not in repaired:
                errors.append(f"Missing required key: '{key}'")

        if errors:
            return ValidationResult(
                is_valid=False,
                errors=errors,
                retry_prompt=f"Your scene output was missing fields: {errors}. Please return complete JSON."
            )

        # 2. Duration check
        duration = float(repaired.get("duration_sec", 0.0))
        if duration <= 0.0 or duration > 120.0:
            repaired["duration_sec"] = max(1.0, min(duration, 30.0))
            errors.append(f"Invalid duration: {duration}s clamped to {repaired['duration_sec']}s")

        # 3. Time of day
        tod = str(repaired.get("time_of_day", "golden_hour")).lower()
        if tod not in VALID_TIMES_OF_DAY:
            repaired["time_of_day"] = "golden_hour"

        # 4. Camera validation & repair
        camera = repaired.get("camera", {})
        shot_type = str(camera.get("shot_type", "medium")).lower()
        if shot_type not in VALID_SHOT_TYPES:
            camera["shot_type"] = "medium"

        movement = str(camera.get("movement", "static")).lower()
        if movement in CAMERA_SYNONYMS:
            camera["movement"] = CAMERA_SYNONYMS[movement]
        elif movement not in VALID_CAMERA_MOVEMENTS:
            camera["movement"] = "dolly"
            errors.append(f"Unknown camera movement '{movement}', defaulted to 'dolly'")

        # Ensure start and end transforms exist
        if "start_transform" not in camera:
            camera["start_transform"] = {"position": [0.0, -3.0, 1.5], "rotation": [10.0, 0.0, 0.0]}
        if "end_transform" not in camera:
            camera["end_transform"] = {"position": [0.0, -2.5, 1.2], "rotation": [8.0, 0.0, 0.0]}

        # 5. Character and action whitelisting
        characters = repaired.get("characters", [])
        for char in characters:
            actions = char.get("actions", [])
            for action_item in actions:
                act = str(action_item.get("action", "idle")).lower()
                if act in ACTION_SYNONYMS:
                    action_item["action"] = ACTION_SYNONYMS[act]
                elif act not in VALID_ACTIONS:
                    errors.append(f"Invalid action '{act}'. Allowed: {sorted(list(VALID_ACTIONS))}")
                    action_item["action"] = "idle"

        # 6. Dialogue checks
        dialogue = repaired.get("dialogue", [])
        for line in dialogue:
            if "speaker" not in line or "text" not in line:
                errors.append("Dialogue item must have 'speaker' and 'text'")

        # Create retry prompt if errors occurred
        retry_prompt = None
        if errors:
            retry_prompt = (
                f"Validation warnings encountered: {'; '.join(errors)}. "
                f"Please ensure all actions are within {sorted(list(VALID_ACTIONS))} "
                f"and camera movements within {sorted(list(VALID_CAMERA_MOVEMENTS))}."
            )

        return ValidationResult(
            is_valid=len(errors) == 0,
            errors=errors,
            repaired_data=repaired,
            retry_prompt=retry_prompt
        )
