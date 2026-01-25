from __future__ import annotations

import csv
import logging
import os
from dataclasses import dataclass
from pathlib import Path
from typing import Iterable

import kagglehub


logger = logging.getLogger(__name__)


@dataclass(frozen=True)
class DatasetBook:
    title: str
    author: str


def resolve_dataset_dir(dataset_id: str, dataset_path: str | None) -> Path:
    if dataset_path:
        p = Path(dataset_path).expanduser().resolve()
        if not p.exists() or not p.is_dir():
            raise FileNotFoundError(f"--dataset-path does not exist or is not a directory: {p}")
        return p

    try:
        downloaded = kagglehub.dataset_download(dataset_id)
        p = Path(downloaded).resolve()
        if not p.exists() or not p.is_dir():
            raise FileNotFoundError(f"kagglehub returned a non-directory path: {p}")
        return p
    except Exception as e:
        raise RuntimeError(
            "Couldn't resolve dataset directory. Provide --dataset-path (recommended) "
            f"or ensure KaggleHub can download '{dataset_id}'. Original error: {e}"
        ) from e


def iter_books(dataset_dir: Path, *, limit: int | None = None) -> Iterable[DatasetBook]:
    books_csv = _find_books_csv(dataset_dir)
    logger.info("Using dataset file: %s", books_csv)

    with open(books_csv, "r", newline="", encoding=_guess_encoding(books_csv)) as f:
        reader = csv.DictReader(f)
        for idx, row in enumerate(reader):
            if limit is not None and idx >= limit:
                break

            title = (row.get("Book-Title") or "").strip()
            author = (row.get("Book-Author") or "").strip()
            if not title or not author:
                continue
            yield DatasetBook(title=_normalize_title(title), author=_normalize_name(author))


def _find_books_csv(dataset_dir: Path) -> Path:
    candidates = []
    for p in dataset_dir.rglob("*.csv"):
        if p.name.lower() == "books.csv":
            return p
        candidates.append(p)

    # Fallback: detect by header
    for p in candidates:
        try:
            with open(p, "r", newline="", encoding=_guess_encoding(p)) as f:
                header = f.readline()
            if "Book-Title" in header and "Book-Author" in header:
                return p
        except Exception:
            continue

    raise FileNotFoundError(f"Couldn't find Books.csv in {dataset_dir}")


def _guess_encoding(path: Path) -> str:
    # Many Kaggle CSVs are ISO-8859-1; using it avoids decode errors without extra deps.
    return os.environ.get("DATASET_ENCODING", "ISO-8859-1")


def _normalize_name(name: str) -> str:
    name = " ".join(name.replace("\u00a0", " ").split())
    return name[:150]


def _normalize_title(title: str) -> str:
    title = " ".join(title.replace("\u00a0", " ").split())
    # default JPA varchar length is often 255
    return title[:240]

