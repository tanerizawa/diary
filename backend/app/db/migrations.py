from pathlib import Path
import site
import sys


def upgrade_to_latest() -> None:
    """Apply all pending Alembic migrations."""
    for p in site.getsitepackages():
        if p in sys.path:
            sys.path.remove(p)
        sys.path.insert(0, p)
    for module in list(sys.modules):
        if module.startswith("alembic"):
            sys.modules.pop(module)
    from alembic.config import Config
    from alembic import command

    root = Path(__file__).resolve().parents[2]
    cfg_path = root / "alembic.ini"
    cfg = Config(str(cfg_path))
    cfg.set_main_option("script_location", str(root / "alembic"))
    command.upgrade(cfg, "head")
