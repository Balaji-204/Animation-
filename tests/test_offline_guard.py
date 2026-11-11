"""Unit tests for offline network policy enforcement."""

import socket
import unittest
from pipeline.offline_guard import OfflineNetworkGuard, OfflinePolicyViolationError


class TestOfflineGuard(unittest.TestCase):
    def setUp(self):
        OfflineNetworkGuard.install()

    def tearDown(self):
        OfflineNetworkGuard.uninstall()

    def test_remote_outbound_blocked(self):
        with self.assertRaises(OfflinePolicyViolationError):
            s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            s.connect(("1.1.1.1", 80))

    def test_domain_name_outbound_blocked(self):
        with self.assertRaises(OfflinePolicyViolationError):
            s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            s.connect(("api.openai.com", 443))

    def test_self_test_method(self):
        self.assertTrue(OfflineNetworkGuard.run_self_test())


if __name__ == "__main__":
    unittest.main()
