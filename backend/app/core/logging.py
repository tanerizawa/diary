# Lokasi: ./app/core/logging.py
"""Konfigurasi logging terstruktur menggunakan structlog."""

import logging
import os
import sys

import structlog


def setup_logging() -> None:
    """Inisialisasi konfigurasi logging standar dan structlog."""
    log_level = os.getenv("LOG_LEVEL", "INFO").upper()

    logging.basicConfig(
        level=log_level,
        format="%(message)s",
        stream=sys.stdout,
    )

    structlog.configure(
        logger_factory=structlog.stdlib.LoggerFactory(),
        wrapper_class=structlog.make_filtering_bound_logger(logging.getLevelName(log_level)),
        processors=[
            structlog.processors.TimeStamper(fmt="iso"),
            structlog.processors.JSONRenderer(),
        ],
    )

