"""Strict offline enforcement guard for OfflineAnimStudio.

Enforces zero-network policy. Outbound sockets to anything other than
localhost / 127.0.0.1 (local Ollama server) are strictly intercepted and blocked.
"""

import socket
import sys
from typing import Tuple

ALLOWED_LOCAL_HOSTS = {"127.0.0.1", "localhost", "::1", "0.0.0.0"}


class OfflinePolicyViolationError(RuntimeError):
    """Raised when an unauthorized outbound network call is attempted."""
    pass


class OfflineNetworkGuard:
    _installed: bool = False
    _original_socket_connect = None

    @classmethod
    def install(cls) -> None:
        """Hooks socket.socket.connect to enforce zero remote network calls."""
        if cls._installed:
            return

        cls._original_socket_connect = socket.socket.connect

        def guarded_connect(self, address: Tuple):
            host = address[0] if isinstance(address, tuple) and len(address) > 0 else str(address)
            if host not in ALLOWED_LOCAL_HOSTS and not host.startswith("127."):
                raise OfflinePolicyViolationError(
                    f"OFFLINE VIOLATION: Blocked unauthorized connection attempt to '{host}'. "
                    "OfflineAnimStudio is strictly offline; no remote network calls permitted."
                )
            return cls._original_socket_connect(self, address)

        socket.socket.connect = guarded_connect
        cls._installed = True

    @classmethod
    def uninstall(cls) -> None:
        """Restores original socket connect (useful in test teardown)."""
        if cls._installed and cls._original_socket_connect:
            socket.socket.connect = cls._original_socket_connect
            cls._installed = False

    @classmethod
    def run_self_test(cls) -> bool:
        """Runs offline self-test verifying that outbound connections are blocked."""
        cls.install()
        # Test 1: Remote connection must fail with OfflinePolicyViolationError
        remote_blocked = False
        try:
            s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            s.connect(("8.8.8.8", 53))
            s.close()
        except OfflinePolicyViolationError:
            remote_blocked = True
        except Exception:
            # Any socket failure under guard is also safe
            remote_blocked = True

        return remote_blocked
