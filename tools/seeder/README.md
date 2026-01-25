## Biblioteka seeder (HTTP)

Seeder w `tools/seeder` zasila serwis biblioteczny przez jego endpointy (HTTP Basic), wykorzystując:
- dataset `arashnic/book-recommendation-dataset` (KaggleHub) do tworzenia autorów i książek
- `faker` do generowania użytkowników, kategorii, komentarzy itp.

### Wymagania
- uruchomiony backend (domyślnie `http://localhost:8080`)
- domyślny admin istnieje po starcie aplikacji: `admin/admin` (patrz `src/main/java/.../config/DataSeeder.java`)

### Uruchomienie
Najprościej przez `uv` (z `tools/seeder`):
- `cd tools/seeder`
- `uv run -m seeder --base-url http://localhost:8080`

Alternatywnie:
- `uv run --script main.py --base-url http://localhost:8080`

Przykład z większą ilością danych:
- `uv run -m seeder --books 800 --users 200 --borrows 400 --reviews 300 --queue-joins 150 --queue-scenarios 25`

### Dataset
Domyślnie seeder spróbuje pobrać dataset przez KaggleHub. Jeśli wolisz offline:
- pobierz dataset ręcznie / upewnij się, że masz go lokalnie
- uruchom z `--dataset-path /sciezka/do/datasetu`

Seeder szuka pliku `Books.csv` (kolumny `Book-Title`, `Book-Author`).

### Docker Compose (flaga)
W `docker-compose.yml` jest opcjonalny serwis `seeder` w profilu `seed`.

- backend + seed jednocześnie: `docker compose --profile seed up --build`
- backend bez seeda: `docker compose up --build`

Uwaga: pierwszy seed może wymagać pobrania datasetu przez KaggleHub (cache jest w wolumenie `kagglehub_cache`).

### Konfiguracja (ENV)
- `BASE_URL` (np. `http://localhost:8080`)
- `ADMIN_USERNAME`, `ADMIN_PASSWORD` (domyślnie `admin/admin`)
- `DATASET_PATH` (opcjonalnie)
- `DATASET_ID` (domyślnie `arashnic/book-recommendation-dataset`)
- `USER_PASSWORD` (hasło dla tworzonych `APP_USER`, domyślnie `password`)
- `SEED_STATE_FILE` (domyślnie `tools/seeder/seed-state.json`)
- `RANDOM_SEED` (domyślnie `42`)
- `LOG_LEVEL` (domyślnie `INFO`)

### Output
Seeder zapisuje stan do `tools/seeder/seed-state.json` (mapy `authors/books/users/categories`), żeby ułatwić kolejne uruchomienia.
