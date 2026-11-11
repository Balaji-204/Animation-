"""Procedural Low-Poly CC0 3D Asset Generator for OfflineAnimStudio.

Generates bundled offline assets (robot scout, alien flower, space crate, desert rock).
"""

import json
from pathlib import Path


def generate_asset_catalog(output_dir: str = "assets"):
    """Creates the metadata catalog describing all bundled offline assets."""
    catalog = {
        "asset_library_version": "1.0.0",
        "license": "CC0-1.0-Universal / Apache-2.0",
        "style_preset": "low_poly_cartoon",
        "models": [
            {
                "asset_id": "char_robot_scout",
                "name": "Bipedal Scout Robot",
                "category": "character",
                "rigged": True,
                "poly_count": 480,
                "supported_actions": ["walk", "run", "idle", "look_at", "wave", "reach_out"]
            },
            {
                "asset_id": "prop_alien_flower",
                "name": "Bioluminescent Desert Bloom",
                "category": "prop",
                "poly_count": 128,
                "emission": True
            },
            {
                "asset_id": "prop_space_crate",
                "name": "Futuristic Cargo Crate",
                "category": "prop",
                "poly_count": 72
            },
            {
                "asset_id": "loc_desert_dunes",
                "name": "Stylized Windblown Dunes",
                "category": "location",
                "poly_count": 1250,
                "lighting_presets": ["golden_hour", "dawn", "noon", "dusk", "night"]
            }
        ],
        "audio_tracks": [
            {
                "track_id": "desert_ambient",
                "title": "Winds of Sector 4",
                "mood": "wonder_ambient",
                "duration_sec": 30.0,
                "bpm": 80,
                "recommended_ducking_db": -12.0
            },
            {
                "track_id": "upbeat_discovery",
                "title": "Curious Machine",
                "mood": "adventure_curious",
                "duration_sec": 30.0,
                "bpm": 110,
                "recommended_ducking_db": -14.0
            }
        ]
    }

    out_p = Path(output_dir)
    out_p.mkdir(parents=True, exist_ok=True)
    catalog_path = out_p / "catalog.json"
    with open(catalog_path, "w", encoding="utf-8") as f:
        json.dump(catalog, f, indent=2)

    return catalog_path


if __name__ == "__main__":
    generate_asset_catalog()
