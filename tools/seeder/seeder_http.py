from __future__ import annotations

import base64
import json
import logging
import time
import urllib.error
import urllib.request
from dataclasses import dataclass
from typing import Any


logger = logging.getLogger(__name__)


@dataclass(frozen=True)
class BasicAuth:
    username: str
    password: str

    def header_value(self) -> str:
        token = base64.b64encode(f"{self.username}:{self.password}".encode("utf-8")).decode("ascii")
        return f"Basic {token}"


class HttpError(RuntimeError):
    def __init__(self, method: str, url: str, status: int, body: str | None):
        super().__init__(f"{method} {url} -> {status}")
        self.method = method
        self.url = url
        self.status = status
        self.body = body


class HttpClient:
    def __init__(
        self,
        *,
        base_url: str,
        default_auth: BasicAuth | None = None,
        dry_run: bool = False,
        timeout_s: float = 30.0,
        retries: int = 3,
    ):
        self.base_url = base_url.rstrip("/")
        self.default_auth = default_auth
        self.dry_run = dry_run
        self.timeout_s = timeout_s
        self.retries = retries

    def request(
        self,
        method: str,
        path: str,
        *,
        auth: BasicAuth | None = None,
        json_body: Any | None = None,
        headers: dict[str, str] | None = None,
        expected: set[int] | None = None,
    ) -> Any:
        url = f"{self.base_url}/{path.lstrip('/')}"

        is_mutating = method.upper() not in {"GET", "HEAD", "OPTIONS"}
        if self.dry_run and is_mutating:
            logger.info("DRY-RUN %s %s body=%s", method.upper(), url, _compact(json_body))
            return None

        final_headers = {"Accept": "application/json"}
        if json_body is not None:
            final_headers["Content-Type"] = "application/json"
        if headers:
            final_headers.update(headers)

        used_auth = auth or self.default_auth
        if used_auth:
            final_headers["Authorization"] = used_auth.header_value()

        data = None if json_body is None else json.dumps(json_body).encode("utf-8")
        req = urllib.request.Request(url, data=data, method=method.upper(), headers=final_headers)

        if expected is None:
            expected = {200, 201, 204}

        last_exc: Exception | None = None
        for attempt in range(self.retries + 1):
            try:
                with urllib.request.urlopen(req, timeout=self.timeout_s) as resp:
                    status = resp.getcode()
                    raw = resp.read()
                    body_text = raw.decode("utf-8", errors="replace") if raw else ""

                    if status not in expected:
                        raise HttpError(method.upper(), url, status, body_text or None)

                    if status == 204 or not body_text:
                        return None
                    try:
                        return json.loads(body_text)
                    except json.JSONDecodeError:
                        return body_text
            except urllib.error.HTTPError as e:
                body = None
                try:
                    body = e.read().decode("utf-8", errors="replace")
                except Exception:
                    pass
                last_exc = HttpError(method.upper(), url, int(e.code), body)
                if int(e.code) >= 500 and attempt < self.retries:
                    time.sleep(0.4 * (2**attempt))
                    continue
                raise last_exc
            except Exception as e:
                last_exc = e
                if attempt < self.retries:
                    time.sleep(0.4 * (2**attempt))
                    continue
                raise

        raise last_exc or RuntimeError("unreachable")


def _compact(value: Any) -> str:
    if value is None:
        return "null"
    try:
        return json.dumps(value, ensure_ascii=False, separators=(",", ":"), sort_keys=True)
    except Exception:
        return str(value)

