from __future__ import annotations

import json
import logging
import random
import time
from dataclasses import dataclass
from pathlib import Path
from typing import Any

from faker import Faker

from seeder_dataset import iter_books, resolve_dataset_dir
from seeder_http import BasicAuth, HttpClient, HttpError


logger = logging.getLogger(__name__)


@dataclass(frozen=True)
class SeederConfig:
    dataset_id: str
    dataset_path: str | None
    extra_categories: int
    author_target: int
    book_target: int
    user_target: int
    borrow_target: int
    return_target: int
    review_target: int
    queue_join_target: int
    queue_scenario_target: int
    user_password: str
    random_seed: int
    state_file: Path


class Seeder:
    def __init__(self, *, client: HttpClient, config: SeederConfig):
        self.client = client
        self.config = config
        self.faker = Faker("pl_PL")
        self.rng = random.Random(config.random_seed)

    def run(self) -> dict[str, Any]:
        self._wait_for_service()
        self._login_admin()

        state = self._load_state()
        dataset_dir = resolve_dataset_dir(self.config.dataset_id, self.config.dataset_path)
        dataset_books = list(iter_books(dataset_dir))
        self.rng.shuffle(dataset_books)

        categories = self._ensure_categories(state, extra=self.config.extra_categories)
        authors = self._ensure_authors(state, dataset_books, target=self.config.author_target)
        books = self._ensure_books(
            state,
            dataset_books,
            authors_by_name=authors,
            category_ids=list(categories.values()),
            target=self.config.book_target,
        )
        users = self._ensure_users(state, target=self.config.user_target, password=self.config.user_password)

        borrows = self._create_borrows(
            users_by_username=users,
            book_ids=list(books.values()),
            target=self.config.borrow_target,
        )
        returned = self._return_some(borrows, target=self.config.return_target)

        reviews = self._create_reviews(users_by_username=users, created_borrows=borrows, target=self.config.review_target)

        queue_joins = self._create_queue_joins(
            users_by_username=users,
            user_password=self.config.user_password,
            book_ids=list(books.values()),
            target=self.config.queue_join_target,
        )

        scenarios = self._create_queue_notification_scenarios(
            users_by_username=users,
            user_password=self.config.user_password,
            book_ids=list(books.values()),
            target=self.config.queue_scenario_target,
        )

        state["summary"] = {
            "categories": len(categories),
            "authors": len(authors),
            "books": len(books),
            "users": len(users),
            "created_borrows": len(borrows),
            "returned_borrows": returned,
            "created_reviews": reviews,
            "queue_joins": queue_joins,
            "queue_scenarios": scenarios,
        }
        self._save_state(state)
        return state["summary"]

    def _login_admin(self) -> None:
        self.client.request("GET", "/auth/login", expected={200})

    def _wait_for_service(self) -> None:
        timeout_s = 180.0
        deadline = time.monotonic() + timeout_s
        last_error: Exception | None = None
        while time.monotonic() < deadline:
            try:
                # Use the same endpoint + auth as the seeding flow.
                self.client.request("GET", "/auth/login", expected={200})
                return
            except HttpError as e:
                # App up but not ready / auth not wired yet.
                last_error = e
                time.sleep(2.0)
            except Exception as e:
                last_error = e
                time.sleep(2.0)

        raise RuntimeError(f"Service not ready after {timeout_s:.0f}s (base_url={self.client.base_url}): {last_error}")

    def _load_state(self) -> dict[str, Any]:
        if self.config.state_file.exists():
            try:
                return json.loads(self.config.state_file.read_text(encoding="utf-8"))
            except Exception:
                return {}
        return {}

    def _save_state(self, state: dict[str, Any]) -> None:
        self.config.state_file.write_text(json.dumps(state, ensure_ascii=False, indent=2), encoding="utf-8")

    def _ensure_categories(self, state: dict[str, Any], *, extra: int) -> dict[str, int]:
        existing = self.client.request("GET", "/categories") or []
        categories = {c["name"]: int(c["id"]) for c in existing}

        created = 0
        for _ in range(extra):
            name = self.faker.unique.word().title()
            if name in categories:
                continue
            try:
                resp = self.client.request("POST", "/categories", json_body={"name": name}, expected={201})
                if resp:
                    categories[resp["name"]] = int(resp["id"])
                    created += 1
            except HttpError as e:
                logger.warning("Create category failed (%s): %s", e.status, e.body)
        logger.info("Categories: %d (created %d)", len(categories), created)
        state["categories"] = categories
        return categories

    def _ensure_authors(self, state: dict[str, Any], dataset_books: list[Any], *, target: int) -> dict[str, int]:
        existing = self.client.request("GET", "/authors") or []
        authors = {a["name"]: int(a["id"]) for a in existing}

        created = 0
        for b in dataset_books:
            if len(authors) >= target:
                break
            if b.author in authors:
                continue
            try:
                resp = self.client.request("POST", "/authors", json_body={"name": b.author}, expected={201})
                if resp:
                    authors[resp["name"]] = int(resp["id"])
                    created += 1
            except HttpError as e:
                if e.status in {409, 400}:
                    continue
                logger.warning("Create author failed (%s): %s", e.status, e.body)
        logger.info("Authors: %d (created %d)", len(authors), created)
        state["authors"] = authors
        return authors

    def _ensure_books(
        self,
        state: dict[str, Any],
        dataset_books: list[Any],
        *,
        authors_by_name: dict[str, int],
        category_ids: list[int],
        target: int,
    ) -> dict[str, int]:
        existing = self.client.request("GET", "/books") or []
        books_by_title: dict[str, int] = {b["title"]: int(b["id"]) for b in existing}

        created = 0
        for b in dataset_books:
            if len(books_by_title) >= target:
                break
            if b.title in books_by_title:
                continue
            author_id = authors_by_name.get(b.author)
            if not author_id:
                continue

            count = self.rng.randint(1, 3)
            cats = set(self.rng.sample(category_ids, k=min(len(category_ids), self.rng.randint(0, 2)))) if category_ids else set()

            try:
                resp = self.client.request(
                    "POST",
                    "/books",
                    json_body={
                        "title": b.title,
                        "count": count,
                        "authorId": author_id,
                        "categoryIds": list(cats),
                    },
                    expected={201},
                )
                if resp:
                    books_by_title[resp["title"]] = int(resp["id"])
                    created += 1
            except HttpError as e:
                if e.status in {409, 400}:
                    continue
                logger.warning("Create book failed (%s): %s", e.status, e.body)

        logger.info("Books: %d (created %d)", len(books_by_title), created)
        state["books"] = books_by_title
        return books_by_title

    def _ensure_users(self, state: dict[str, Any], *, target: int, password: str) -> dict[str, int]:
        users = self._fetch_users_by_username()
        created = 0

        for _ in range(target):
            username = self.faker.unique.user_name()[:24]
            if username in users:
                continue
            payload = {
                "username": username,
                "firstname": self.faker.first_name(),
                "lastname": self.faker.last_name(),
                "email": self.faker.unique.email(),
                "password": password,
            }
            try:
                self.client.request("POST", "/auth/register", json_body=payload, expected={204})
                created += 1
            except HttpError as e:
                if e.status in {409, 400}:
                    continue
                logger.warning("Register user failed (%s): %s", e.status, e.body)

        # Refresh to get IDs for newly created users
        users = self._fetch_users_by_username()
        logger.info("Users: %d (created %d)", len(users), created)
        state["users"] = users
        state["user_password_hint"] = password
        return users

    def _fetch_users_by_username(self) -> dict[str, int]:
        all_users = self.client.request("GET", "/appusers") or []
        return {u["username"]: int(u["id"]) for u in all_users}

    def _create_borrows(self, *, users_by_username: dict[str, int], book_ids: list[int], target: int) -> list[dict[str, Any]]:
        if not users_by_username or not book_ids:
            return []

        users = list(users_by_username.items())
        created: list[dict[str, Any]] = []
        attempts = 0

        while len(created) < target and attempts < target * 6:
            attempts += 1
            username, user_id = self.rng.choice(users)
            book_id = self.rng.choice(book_ids)
            duration_days = self.rng.choice([7, 14, 21, 28])
            payload = {"borrowDuration": f"PT{duration_days * 24}H", "bookId": book_id, "appUserId": user_id}
            try:
                resp = self.client.request("POST", "/borrows", json_body=payload, expected={201})
                if resp:
                    created.append(resp)
            except HttpError as e:
                if e.status in {409, 400}:
                    continue
                logger.warning("Create borrow failed (%s): %s", e.status, e.body)

        logger.info("Borrows: created %d (attempts %d)", len(created), attempts)
        return created

    def _return_some(self, borrows: list[dict[str, Any]], *, target: int) -> int:
        if not borrows:
            return 0
        self.rng.shuffle(borrows)
        returned = 0
        for b in borrows[:target]:
            borrow_id = b.get("id")
            if not borrow_id:
                continue
            try:
                self.client.request("POST", f"/borrows/{borrow_id}/return", expected={200})
                returned += 1
            except HttpError as e:
                if e.status in {409, 400}:
                    continue
                logger.warning("Return borrow failed (%s): %s", e.status, e.body)
        logger.info("Borrows: returned %d", returned)
        return returned

    def _create_reviews(
        self, *, users_by_username: dict[str, int], created_borrows: list[dict[str, Any]], target: int
    ) -> int:
        if not created_borrows:
            return 0

        # Group borrowed books by user, so reviews can satisfy "user has borrowed book" constraint.
        borrowed_by_user: dict[int, set[int]] = {}
        for b in created_borrows:
            try:
                user_id = int(b["appUser"]["id"])
                book_id = int(b["book"]["id"])
            except Exception:
                continue
            borrowed_by_user.setdefault(user_id, set()).add(book_id)

        created = 0
        attempts = 0
        user_ids = list(borrowed_by_user.keys())
        while created < target and attempts < target * 6 and user_ids:
            attempts += 1
            user_id = self.rng.choice(user_ids)
            book_id = self.rng.choice(list(borrowed_by_user[user_id]))
            payload = {
                "rating": self.rng.randint(1, 5),
                "comment": self.faker.sentence(nb_words=12),
                "appUserId": user_id,
                "bookId": book_id,
            }
            try:
                self.client.request("POST", "/review", json_body=payload, expected={201})
                created += 1
            except HttpError as e:
                if e.status in {409, 400}:
                    continue
                logger.warning("Create review failed (%s): %s", e.status, e.body)
        logger.info("Reviews: created %d (attempts %d)", created, attempts)
        return created

    def _create_queue_joins(
        self, *, users_by_username: dict[str, int], user_password: str, book_ids: list[int], target: int
    ) -> int:
        if not users_by_username or not book_ids:
            return 0

        created = 0
        attempts = 0
        users = list(users_by_username.keys())
        self.rng.shuffle(users)

        while created < target and attempts < target * 6:
            attempts += 1
            username = self.rng.choice(users)
            book_id = self.rng.choice(book_ids)

            try:
                self.client.request(
                    "POST",
                    f"/books/{book_id}/queue/join",
                    auth=BasicAuth(username, user_password),
                    expected={201},
                )
                created += 1
            except HttpError as e:
                if e.status in {409, 400}:
                    continue
                logger.warning("Queue join failed (%s): %s", e.status, e.body)

        logger.info("Queue joins: created %d (attempts %d)", created, attempts)
        return created

    def _create_queue_notification_scenarios(
        self, *, users_by_username: dict[str, int], user_password: str, book_ids: list[int], target: int
    ) -> int:
        if not users_by_username or not book_ids:
            return 0

        # Prefer books with count=1, so borrow-out + return reliably produces a notification
        # for the first queued user (queue[count-1]).
        try:
            all_books = self.client.request("GET", "/books") or []
            one_copy = [int(b["id"]) for b in all_books if int(b.get("count", 0)) == 1]
            if one_copy:
                book_ids = one_copy
        except Exception:
            pass

        users = list(users_by_username.keys())
        scenarios = 0
        attempts = 0

        while scenarios < target and attempts < target * 10:
            attempts += 1
            book_id = self.rng.choice(book_ids)

            # Ensure we can borrow out the book by borrowing it once with an arbitrary user.
            borrow_username = self.rng.choice(users)
            borrow_user_id = users_by_username[borrow_username]
            try:
                borrow = self.client.request(
                    "POST",
                    "/borrows",
                    json_body={"borrowDuration": "PT336H", "bookId": book_id, "appUserId": borrow_user_id},
                    expected={201},
                )
                if not borrow or "id" not in borrow:
                    continue
            except HttpError:
                continue

            # Fill a small queue (up to 3, due to user constraint) with distinct users.
            queued_users = self.rng.sample(users, k=min(3, len(users)))
            for username in queued_users:
                try:
                    self.client.request(
                        "POST",
                        f"/books/{book_id}/queue/join",
                        auth=BasicAuth(username, user_password),
                        expected={201},
                    )
                except HttpError:
                    pass

            # Return -> should create a notification for an eligible queued user.
            try:
                self.client.request("POST", f"/borrows/{borrow['id']}/return", expected={200})
                scenarios += 1
            except HttpError:
                continue

        logger.info("Queue scenarios: created %d (attempts %d)", scenarios, attempts)
        return scenarios
