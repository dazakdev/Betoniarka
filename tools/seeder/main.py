from __future__ import annotations

import argparse
import json
import logging
import os
from pathlib import Path


def main():
    parser = argparse.ArgumentParser(
        prog="library-seeder",
        description="Seed the Biblioteka service via HTTP endpoints.",
    )
    default_state_file = str((Path(__file__).resolve().parent / "seed-state.json"))

    parser.add_argument(
        "--base-url",
        default=os.environ.get("BASE_URL", "http://localhost:8080"),
        help="Service base URL (env: BASE_URL).",
    )
    parser.add_argument(
        "--admin-username",
        default=os.environ.get("ADMIN_USERNAME", "admin"),
        help="Admin username for HTTP Basic (env: ADMIN_USERNAME).",
    )
    parser.add_argument(
        "--admin-password",
        default=os.environ.get("ADMIN_PASSWORD", "admin"),
        help="Admin password for HTTP Basic (env: ADMIN_PASSWORD).",
    )
    parser.add_argument(
        "--dataset-path",
        default=os.environ.get("DATASET_PATH"),
        help="Optional path to dataset directory (env: DATASET_PATH).",
    )
    parser.add_argument(
        "--dataset-id",
        default=os.environ.get("DATASET_ID", "arashnic/book-recommendation-dataset"),
        help="KaggleHub dataset id (env: DATASET_ID).",
    )

    parser.add_argument("--categories", type=int, default=0, help="Extra categories to create.")
    parser.add_argument("--authors", type=int, default=200, help="Authors to create from dataset.")
    parser.add_argument("--books", type=int, default=500, help="Books to create from dataset.")
    parser.add_argument("--users", type=int, default=100, help="Borrower users to register.")
    parser.add_argument("--borrows", type=int, default=200, help="Borrow records to create.")
    parser.add_argument("--returns", type=int, default=50, help="How many borrows to return.")
    parser.add_argument("--reviews", type=int, default=200, help="Reviews to create (requires borrows).")
    parser.add_argument("--queue-joins", type=int, default=50, help="Queue joins to create (as APP_USER).")
    parser.add_argument(
        "--queue-scenarios",
        type=int,
        default=10,
        help="How many 'borrow out + queue + return' scenarios to create for notifications.",
    )

    parser.add_argument(
        "--user-password",
        default=os.environ.get("USER_PASSWORD", "password"),
        help="Password for created borrower users (env: USER_PASSWORD).",
    )
    parser.add_argument(
        "--state-file",
        default=os.environ.get("SEED_STATE_FILE", default_state_file),
        help="Where to persist seeding state (env: SEED_STATE_FILE).",
    )
    parser.add_argument(
        "--random-seed",
        type=int,
        default=int(os.environ.get("RANDOM_SEED", "42")),
        help="Deterministic random seed (env: RANDOM_SEED).",
    )
    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="Don't send mutating requests, only log planned operations.",
    )
    parser.add_argument(
        "--log-level",
        default=os.environ.get("LOG_LEVEL", "INFO"),
        help="Python logging level (env: LOG_LEVEL).",
    )

    args = parser.parse_args()

    logging.basicConfig(
        level=getattr(logging, args.log_level.upper(), logging.INFO),
        format="%(levelname)s %(message)s",
    )

    try:
        from seeder_http import BasicAuth, HttpClient
        from seeder_seed import Seeder, SeederConfig
    except ModuleNotFoundError as e:
        raise SystemExit(
            "Missing Python dependencies for seeder.\n"
            "Run from `tools/seeder` with uv:\n"
            "  `uv sync`\n"
            "  `uv run python main.py --help`\n"
            f"\nOriginal error: {e}"
        ) from e

    state_path = Path(args.state_file)
    state_path.parent.mkdir(parents=True, exist_ok=True)

    client = HttpClient(
        base_url=args.base_url,
        default_auth=BasicAuth(args.admin_username, args.admin_password),
        dry_run=args.dry_run,
    )

    config = SeederConfig(
        dataset_id=args.dataset_id,
        dataset_path=args.dataset_path,
        extra_categories=args.categories,
        author_target=args.authors,
        book_target=args.books,
        user_target=args.users,
        borrow_target=args.borrows,
        return_target=args.returns,
        review_target=args.reviews,
        queue_join_target=args.queue_joins,
        queue_scenario_target=args.queue_scenarios,
        user_password=args.user_password,
        random_seed=args.random_seed,
        state_file=state_path,
    )

    seeder = Seeder(client=client, config=config)
    result = seeder.run()
    print(json.dumps(result, indent=2, ensure_ascii=False))


if __name__ == "__main__":
    main()
