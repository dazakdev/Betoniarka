# System do obsługi biblioteki

Aplikacja webowa (REST) oparta o Spring Boot do obsługi biblioteki: użytkownicy, książki, autorzy, kategorie, wypożyczenia, kolejki, recenzje oraz raporty.

## Technologie

- Java `25` (Gradle toolchain)
- Spring Boot `4.0.0` + Spring MVC
- Spring Data JPA + PostgreSQL
- Spring Security (HTTP Basic) + autoryzacja ról przez `@PreAuthorize`
- Powiadomienia e-mail: `spring-boot-starter-mail` + Mailpit (SMTP + UI)
- MapStruct + Lombok
- OpenAPI/Swagger UI (`org.springdoc:springdoc-openapi-starter-webmvc-ui`)

## Uruchomienie

- Docker (PostgreSQL + Mailpit + aplikacja): `docker compose up --build`
- Lokalnie:
  - wymagane: PostgreSQL + SMTP (np. Mailpit)
  - uruchomienie: `SPRING_PROFILES_ACTIVE=development ./gradlew bootRun`
- Build: `./gradlew clean build`
- Jeżeli środowisko blokuje zapis do `~/.gradle`, ustaw: `GRADLE_USER_HOME="$(pwd)/.gradle"`

## Konfiguracja

- Profile Springa:
  - `development`: `src/main/resources/application-development.properties` (DB `localhost:5432`, Mailpit `localhost:1025`)
  - `production`: `src/main/resources/application-production.properties` (DB `db:5432`, Mailpit `mailpit:1025`, pod `docker compose`)
- Baza danych (domyślnie): `spring.jpa.hibernate.ddl-auto=update`
- Czas w serwisach jest wstrzykiwany przez `java.time.Clock` (bean Springa) w `src/main/java/com/betoniarka/biblioteka/config/TimeConfiguration.java` jako `Clock.systemUTC()`.
- Automatyczne wypożyczenie z kolejki: `library.queue.autoBorrowDurationDays` (domyślnie `14`).

## Dane startowe (seed)

`src/main/java/com/betoniarka/biblioteka/config/DataSeeder.java` przy starcie aplikacji:

- tworzy domyślnego admina: login/hasło `admin/admin` (jeśli nie istnieje),
- seeduje przykładowe kategorie (jeśli tabela `category` jest pusta).

## Bezpieczeństwo (auth)

- Mechanizm: HTTP Basic (Spring Security).
- Publiczne endpointy (bez logowania): `POST /auth/register`, `GET /auth/login`, `GET /v3/api-docs/**`, `GET /swagger-ui/**`, `GET /swagger-ui.html`.
- Wszystkie pozostałe endpointy wymagają uwierzytelnienia.
- Role aplikacyjne: `src/main/java/com/betoniarka/biblioteka/appuser/AppUserRole.java` (`APP_USER`, `EMPLOYEE`, `ADMIN`).

Przykład wywołania z Basic Auth:

```bash
curl -u admin:admin http://localhost:8080/books
```

## API (kontrolery)

Poniżej ścieżki i operacje zgodne z kodem kontrolerów w `src/main/java/com/betoniarka/biblioteka/**`.

### Auth (`/auth`)

- `POST /auth/register` – rejestracja użytkownika (`AppUserRegisterDto`), rola ustawiana na `APP_USER`.
- `GET /auth/login` – test logowania (zwraca nazwę zalogowanego użytkownika).

### Użytkownicy (`/appusers`)

- `GET /appusers` – lista użytkowników (`ADMIN`, `EMPLOYEE`)
- `GET /appusers/{id}` – szczegóły użytkownika (`ADMIN`, `EMPLOYEE`)
- `POST /appusers` – utworzenie użytkownika (`ADMIN`)
- `PATCH /appusers/{id}` – aktualizacja użytkownika (adminowa) (`ADMIN`)
- `DELETE /appusers/{id}` – usunięcie użytkownika (wymaga zalogowania; brak `@PreAuthorize` w kodzie)
- `GET /appusers/me` – podgląd własnego konta (wymaga zalogowania)
- `PATCH /appusers/me` – aktualizacja własnego konta (wymaga zalogowania)
- `DELETE /appusers/me` – usunięcie własnego konta (wymaga zalogowania)

### Książki (`/books`)

- `GET /books?search=...` – lista książek (opcjonalne wyszukiwanie; wymaga zalogowania)
- `GET /books/{id}` – szczegóły książki (wymaga zalogowania)
- `POST /books` – dodanie książki (`ADMIN`, `EMPLOYEE`)
- `PATCH /books/{id}` – aktualizacja książki (`ADMIN`, `EMPLOYEE`)
- `DELETE /books/{id}` – usunięcie książki (`ADMIN`, `EMPLOYEE`)

### Autorzy (`/authors`)

- `GET /authors` – lista autorów (wymaga zalogowania)
- `GET /authors/{id}` – szczegóły autora (wymaga zalogowania)
- `POST /authors` – dodanie autora (`ADMIN`, `EMPLOYEE`)
- `PATCH /authors/{id}` – aktualizacja autora (`ADMIN`, `EMPLOYEE`)
- `DELETE /authors/{id}` – usunięcie autora (`ADMIN`, `EMPLOYEE`)

### Kategorie (`/categories`)

- `GET /categories` – lista kategorii (wymaga zalogowania)
- `GET /categories/{id}` – szczegóły kategorii (wymaga zalogowania)
- `POST /categories` – dodanie kategorii (`ADMIN`, `EMPLOYEE`)
- `PATCH /categories/{id}` – aktualizacja kategorii (`ADMIN`, `EMPLOYEE`)
- `DELETE /categories/{id}` – usunięcie kategorii (`ADMIN`, `EMPLOYEE`)

### Wypożyczenia (`/borrows`)

- `GET /borrows` – lista wypożyczeń (`ADMIN`, `EMPLOYEE`)
- `GET /borrows/{id}` – szczegóły wypożyczenia (`ADMIN`, `EMPLOYEE`)
- `POST /borrows` – wypożyczenie książki (`ADMIN`, `EMPLOYEE`)
- `PATCH /borrows/{id}` – aktualizacja wypożyczenia (`ADMIN`, `EMPLOYEE`)
- `POST /borrows/{id}/return` – zwrot książki (`ADMIN`, `EMPLOYEE`); po zwrocie uruchamia auto-wypożyczenie z kolejki (jeżeli są chętni).

### Kolejka do książki (`/books/{bookId}/queue`)

- `GET /books/{bookId}/queue` – podgląd kolejki (`ADMIN`, `EMPLOYEE`)
- `POST /books/{bookId}/queue/join` – dołączenie do kolejki (`APP_USER`)
- `DELETE /books/{bookId}/queue/leave` – opuszczenie kolejki (`APP_USER`)

### Recenzje (`/review`)

- `GET /review` – lista recenzji (wymaga zalogowania)
- `GET /review/{id}` – szczegóły recenzji (wymaga zalogowania)
- `POST /review` – dodanie recenzji (wymaga zalogowania)
- `PATCH /review/{id}` – aktualizacja recenzji (wymaga zalogowania)
- `DELETE /review/{id}` – usunięcie recenzji (wymaga zalogowania)

### Powiadomienia (`/appusers/{id}/notifications`)

- `GET /appusers/{id}/notifications` – lista powiadomień użytkownika (wymaga zalogowania)

### Raporty (`/report/**`)

- `/report/appuser/*` – raporty użytkowników (`ADMIN`, `EMPLOYEE`)
  - `GET /report/appuser/summary`
  - `GET /report/appuser/overdue`
  - `GET /report/appuser/most-active?limit=10`
  - `GET /report/appuser/dead?days=100`
- `/report/book/*` – raporty książek (`ADMIN`, `EMPLOYEE`)
  - `GET /report/book/summary`
  - `GET /report/book/availability`
  - `GET /report/book/most-reviewed?limit=10`
  - `GET /report/book/most-popular-categories?limit=10`
- `/report/borrow/*` – raporty wypożyczeń (`ADMIN`, `EMPLOYEE`)
  - `GET /report/borrow/summary`
  - `GET /report/borrow/most-borrowed?limit=10&from=<Instant>&to=<Instant>`

## Model domenowy (encje)

Encje i relacje (JPA) znajdują się w `src/main/java/com/betoniarka/biblioteka/**`:

- `AppUser` (`app_user`)
  - relacje: `borrows` (1..N do `Borrow`), `queuedBooks` (1..N do `QueueEntry`), `reviews` (1..N do `Review`)
  - reguły domenowe:
    - limit aktywnych wypożyczeń: maks. 3 (`getCurrentBorrows()`)
    - blokada wypożyczenia tej samej książki drugi raz (gdy wypożyczenie aktywne)
    - blokada wypożyczenia, gdy inny użytkownik jest pierwszy w kolejce dla danej książki
- `Book` (`book`)
  - pola: `title` (unikalny), `count` (liczba dostępnych egzemplarzy)
  - relacje: `author` (N..1), `categories` (N..N), `borrowedBy` (1..N), `queue` (1..N), `reviews` (1..N)
- `Author` (`author`)
  - relacja: `books` (1..N)
- `Category` (`category`)
  - relacja: `books` (N..N)
- `Borrow` (`borrowed_book`)
  - pola: `borrowedAt`, `returnedAt`, `borrowDuration`
  - relacje: `appUser` (N..1), `book` (N..1)
- `QueueEntry` (`queue_entry`)
  - unikalność: `(app_user_id, book_id)` (jeden wpis w kolejce per użytkownik i książka)
  - pole `timestamp` ustawiane w `QueueEntryService` przez `Instant.now(clock)`
- `Review` (`review`)
  - reguły domenowe w `AppUser.addReview(...)`:
    - recenzję można dodać tylko dla książki, którą użytkownik kiedykolwiek wypożyczył
    - użytkownik może dodać tylko jedną recenzję per książka
- `Notification` (`notifications`)
  - tworzone w serwisach jako zapis w DB + wysyłka e-mail (SMTP skonfigurowany przez `spring.mail.*`)

## Dokumentacja interfejsu

- Swagger UI: `/swagger-ui/`
- OpenAPI JSON: `/v3/api-docs`
- Postman: kolekcja i środowisko w `postman/Biblioteka_Collection.json` oraz `postman/Biblioteka_Environment.json`

## Narzędzia dev (docker)

- Mailpit UI: `http://localhost:8025` (SMTP: `localhost:1025`)
