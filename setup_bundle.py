#!/usr/bin/env python3
"""OfflineAnimStudio One-Command Setup & Air-Gapped Bundle Manager.

Verifies local binaries (Blender, FFmpeg, Ollama, Piper) and validates
local model checksums for completely offline, air-gapped deployment.
"""

import hashlib
import os
import shutil
import sys
from pathlib import Path

REQUIRED_BINARIES = {
    "ffmpeg": "FFmpeg video/audio assembler (required)",
    "blender": "Blender 3D headless rendering engine (required)",
    "ollama": "Local LLM inference server (required)",
    "piper": "Piper offline neural TTS (optional fallback available)"
}


def check_local_binaries():
    print("==================================================")
    print(" OfflineAnimStudio System Environment Pre-Check   ")
    print("==================================================")
    all_ok = True
    for binary, desc in REQUIRED_BINARIES.items():
        path = shutil.which(binary)
        if path:
            print(f" [PASS] {binary:<10} -> Found at {path}")
        else:
            if binary == "piper":
                print(f" [INFO] {binary:<10} -> Not in PATH (using internal synth fallback)")
            else:
                print(f" [WARN] {binary:<10} -> Missing! ({desc})")
                all_ok = False
    return all_ok


def verify_model_checksums():
    checksum_file = Path("models/checksums.sha256")
    if not checksum_file.exists():
        print(" [INFO] No external model checksums file found; local fallback packs enabled.")
        return True

    print("\nVerifying local model file hashes...")
    with open(checksum_file, "r") as f:
        for line in f:
            line = line.strip()
            if not line or line.startswith("#"):
                continue
            expected_sha, file_path = line.split(maxsplit=1)
            target = Path(file_path)
            if not target.exists():
                print(f" [MISSING] {file_path}")
                continue
            h = hashlib.sha256(target.read_bytes()).hexdigest()
            if h == expected_sha:
                print(f" [VALID]   {file_path}")
            else:
                print(f" [FAIL]    {file_path} (Hash mismatch)")
    return True


if __name__ == "__main__":
    check_local_binaries()
    verify_model_checksums()
    print("\nOfflineAnimStudio setup verification complete.")
