import tomllib
from pathlib import Path
from typing import Any


def _deep_merge(base: dict[str, Any], override: dict[str, Any]) -> dict[str, Any]:
    """
    递归合并字典：override 覆盖 base（同名 key 若都是 dict 则继续递归）。
    """
    merged = dict(base)
    for k, v in override.items():
        if isinstance(v, dict) and isinstance(merged.get(k), dict):
            merged[k] = _deep_merge(merged[k], v)  # type: ignore[arg-type]
        else:
            merged[k] = v
    return merged


class Config:
    """
    配置加载顺序：
    1) config.toml（默认配置，必须存在）
    2) config-local.toml（本地覆盖配置，可选）

    合并策略：后加载的覆盖前加载的。
    """

    def __init__(
        self,
        base_path: str | Path = "config.toml",
        local_path: str | Path = "config-local.toml",
    ):
        base_path = Path(base_path)
        local_path = Path(local_path)

        if not base_path.exists():
            raise FileNotFoundError(f"找不到配置文件: {base_path}")

        with base_path.open("rb") as f:
            base_cfg: dict[str, Any] = tomllib.load(f)

        local_cfg: dict[str, Any] = {}
        if local_path.exists():
            with local_path.open("rb") as f:
                local_cfg = tomllib.load(f)

        self._data: dict[str, Any] = _deep_merge(base_cfg, local_cfg)

    def get(self, key: str, default: Any = None) -> Any:
        """
        获取顶层 key，例如：
        - bot_config.get("bot-api") -> dict
        """
        return self._data.get(key, default)


# 全局初始化
config = Config()


if __name__ == "__main__":
    print(config.get("bot-api")["base_url"])
    print(config.get("bot-api")["token"])
    print(config.get("ai")["base_url"])
    print(config.get("ai")["api_key"])
    print(config.get("ai")["model"])