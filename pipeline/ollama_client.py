"""Local Ollama client with retry-and-repair loop and auto-detection."""

import json
import logging
import subprocess
import time
import urllib.error
import urllib.request
from typing import Any, Dict, List, Optional

logger = logging.getLogger("OfflineAnimStudio.Ollama")


class OllamaClient:
    """Interfaces with a local Ollama daemon without any external internet traffic."""

    def __init__(self, base_url: str = "http://127.0.0.1:11434", default_model: str = "llama3.1:8b"):
        self.base_url = base_url.rstrip("/")
        self.default_model = default_model

    def is_running(self) -> bool:
        """Check if local Ollama daemon is reachable."""
        try:
            req = urllib.request.Request(f"{self.base_url}/api/tags")
            with urllib.request.urlopen(req, timeout=2.0) as resp:
                return resp.status == 200
        except Exception:
            return False

    def ensure_started(self) -> bool:
        """Attempts to start the Ollama service if not already active."""
        if self.is_running():
            return True
        try:
            logger.info("Local Ollama daemon not responding; spawning 'ollama serve'...")
            subprocess.Popen(
                ["ollama", "serve"],
                stdout=subprocess.DEVNULL,
                stderr=subprocess.DEVNULL,
                start_new_session=True
            )
            for _ in range(10):
                time.sleep(0.5)
                if self.is_running():
                    return True
        except Exception as e:
            logger.warning(f"Could not automatically spawn ollama: {e}")
        return self.is_running()

    def list_models(self) -> List[str]:
        """Queries local Ollama for installed models."""
        try:
            req = urllib.request.Request(f"{self.base_url}/api/tags")
            with urllib.request.urlopen(req, timeout=3.0) as resp:
                data = json.loads(resp.read().decode("utf-8"))
                models = [m.get("name", "") for m in data.get("models", [])]
                return [m for m in models if m]
        except Exception as e:
            logger.error(f"Failed to list local models: {e}")
            return []

    def generate_json(
        self,
        prompt: str,
        system_prompt: str,
        model: Optional[str] = None,
        max_retries: int = 3,
        timeout_sec: float = 60.0
    ) -> Dict[str, Any]:
        """Generates structured JSON using local Ollama with format: json and repair loop."""
        selected_model = model or self.default_model
        current_prompt = prompt

        for attempt in range(1, max_retries + 1):
            payload = {
                "model": selected_model,
                "prompt": current_prompt,
                "system": system_prompt,
                "format": "json",
                "stream": False,
                "options": {
                    "temperature": 0.3,
                    "top_p": 0.9
                }
            }
            body = json.dumps(payload).encode("utf-8")
            req = urllib.request.Request(
                f"{self.base_url}/api/generate",
                data=body,
                headers={"Content-Type": "application/json"}
            )

            try:
                with urllib.request.urlopen(req, timeout=timeout_sec) as resp:
                    resp_data = json.loads(resp.read().decode("utf-8"))
                    raw_text = resp_data.get("response", "").strip()

                    try:
                        parsed = json.loads(raw_text)
                        return parsed
                    except json.JSONDecodeError as jerr:
                        logger.warning(f"JSON decode failed on attempt {attempt}: {jerr}")
                        current_prompt = (
                            f"{prompt}\n\n"
                            f"PREVIOUS ATTEMPT FAILED WITH INVALID JSON: {jerr}.\n"
                            "Return valid, clean JSON only without markdown fences."
                        )
            except urllib.error.URLError as uerr:
                logger.error(f"Ollama connection error (attempt {attempt}): {uerr}")
                time.sleep(1.0)
            except Exception as ex:
                logger.error(f"Unexpected error querying Ollama: {ex}")
                time.sleep(1.0)

        raise RuntimeError(f"Failed to generate valid JSON from Ollama after {max_retries} attempts.")
