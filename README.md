# System do obsługi biblioteki

Aplikacja webowa oparta o Spring Boot, służąca do obsługi biblioteki: zarządzania czytelnikami, książkami, autorami, kategoriami oraz procesem wypożyczania (w tym kolejkami do książek). Dokumentacja poniżej opisuje stan faktyczny na podstawie kodu w pakiecie `com.betoniarka.biblioteka`.

---

## 1. Cel i założenia projektu

Temat projektu: **System do obsługi biblioteki**

Zakładane funkcjonalności:
- wprowadzanie nowego czytelnika (imię, nazwisko, email, login, hasło) z walidacją danych,
- różne uprawnienia użytkowników (czytelnik, pracownik, administrator),
- zarządzanie książkami (CRUD) z kategoryzacją,
- możliwość przypisania książki do wielu kategorii,
- obsługa kolejki chętnych do wypożyczenia książki,
- ograniczenie maksymalnej liczby wypożyczonych książek na użytkownika,
- wyszukiwanie, polecanie i statystyki (planowane w dalszych iteracjach).

---

## 2. Model domenowy (stan na M1)

W projekcie zaimplementowano kompletny model obiektowy i bazodanowy dla kluczowych pojęć biblioteki. Wszystkie encje są mapowane przy użyciu JPA.

### 2.1 Użytkownik systemu – `AppUser`

Plik: `src/main/java/com/betoniarka/biblioteka/appuser/AppUser.java`

Reprezentuje dowolnego użytkownika systemu (czytelnik, pracownik, admin).

Najważniejsze pola:
- `id` – klucz główny,
- `username` – unikalny login (wymagany),
- `password` – hasło (wymagane, przechowywane po zakodowaniu),
- `firstname`, `lastname` – imię i nazwisko,
- `email` – unikalny adres email (wymagany),
- `role` – rola użytkownika (enum `AppRole`).

Relacje:
- `borrowedBooks` – lista wypożyczonych książek (`BorrowedBook`) - jeden do wielu,
- `queuedBooks` – wpisy użytkownika w kolejkach do książek (`QueueEntry`) - jeden do wielu,
- `reviews` – recenzje wystawione przez użytkownika (`Review`) - jeden do wielu.

### 2.2 Role użytkowników – `AppRole`

Plik: `src/main/java/com/betoniarka/biblioteka/appuser/AppRole.java`

Enum określający uprawnienia:
- `APP_USER` – zwykły czytelnik,
- `EMPLOYEE` – pracownik biblioteki,
- `ADMIN` – administrator systemu.

Rola jest używana w module bezpieczeństwa do nadawania uprawnień (`ROLE_APP_USER`, `ROLE_EMPLOYEE`, `ROLE_ADMIN`).

### 2.3 Książka – `Book`

Plik: `src/main/java/com/betoniarka/biblioteka/book/Book.java`

Reprezentuje książkę dostępną w bibliotece.

Najważniejsze pola:
- `id`,
- `title` – unikalny tytuł (wymagany),
- `count` – liczba dostępnych egzemplarzy (wymagana).

Relacje:
- `categories` – lista kategorii (`Category`) – wiele do wielu,
- `author` – jeden autor (`Author`) - wiele do jednego,
- `borrowedBy` – wypożyczenia tej książki (`BorrowedBook`) - jeden do wielu,
- `queue` – kolejka do tej książki (`QueueEntry`) - jeden do wielu,
- `reviews` – recenzje książki (`Review`) - jeden do wielu.

Model spełnia wymagania:
- książka może należeć do wielu kategorii,
- na podstawie `count` i powiązań `BorrowedBook` można ograniczać liczbę aktywnych wypożyczeń.

### 2.4 Kategoria – `Category`

Plik: `src/main/java/com/betoniarka/biblioteka/category/Category.java`

Reprezentuje kategorię tematyczną książek.

Pola:
- `id`,
- `name` – unikalna nazwa kategorii (wymagana).

Relacje:
- `books` – lista książek w tej kategorii - wiele do wielu.

### 2.5 Autor – `Author`

Plik: `src/main/java/com/betoniarka/biblioteka/author/Author.java`

Reprezentuje autora książek.

Pola:
- `id`,
- `name` – nazwa/imie i nazwisko autora (wymagane).

Relacje:
- `books` – lista książek napisanych przez danego autora - jeden do wielu.

### 2.6 Wypożyczona książka – `BorrowedBook`

Plik: `src/main/java/com/betoniarka/biblioteka/borrowedbook/BorrowedBook.java`

Reprezentuje pojedyncze wypożyczenie egzemplarza książki przez użytkownika.

Pola:
- `id`,
- `timestamp` – czas wypożyczenia (wymagany),
- `borrowedTime` – czas trwania wypożyczenia (wymagane).

Relacje:
- `appUser` – użytkownik, który wypożyczył książkę - wiele do jednego,
- `book` – wypożyczona książka - wiele do jednego.

Na bazie tej encji możliwe jest:
- liczenie aktualnej liczby wypożyczeń użytkownika (limit x książek),
- wyliczanie statystyk (najczęściej wypożyczane książki, aktywność).

### 2.7 Kolejka do książki – `QueueEntry`

Plik: `src/main/java/com/betoniarka/biblioteka/queueentry/QueueEntry.java`

Reprezentuje zapis użytkownika w kolejce do wypożyczania konkretnej książki.

Pola:
- `id`,
- `timestamp` – moment zapisania się do kolejki (wymagany).

Relacje:
- `appUser` – użytkownik oczekujący w kolejce - wiele do jednego,
- `book` – książka, na którą czeka - wiele do jednego.

### 2.8 Recenzja – `Review`

Plik: `src/main/java/com/betoniarka/biblioteka/review/Review.java`

Reprezentuje ocenę książki wystawioną przez użytkownika.

Pola:
- `id`,
- `rating` – ocena (wymagana),
- `comment` – opcjonalny komentarz.

Relacje:
- `appUser` – użytkownik wystawiający recenzję - wiele do jednego,
- `book` – oceniana książka - wiele do jednego.

---

## 3. Warstwa REST – użytkownicy (M1)

W M1 skupiono się na pełnym CRUD i rejestracji użytkownika poprzez REST.

### 3.1 DTO do tworzenia użytkownika – `UserCreateRequestDto`

Plik: `src/main/java/com/betoniarka/biblioteka/appuser/UserCreateRequestDto.java`

Reprezentuje dane wejściowe przy rejestracji:
- `username` – wymagany,
- `email` – wymagany, poprawny adres email,
- `password` – wymagane hasło.

Pola są walidowane adnotacjami `@NotBlank` i `@Email`.

### 3.2 DTO do aktualizacji użytkownika – `UserUpdateAppUserDto`

Plik: `src/main/java/com/betoniarka/biblioteka/appuser/UserUpdateAppUserDto.java`

Pola:
- `id` – identyfikator aktualizowanego użytkownika,
- `username`,
- `firstname`,
- `lastname`,
- `email`,
- `password`.

Umożliwia modyfikację danych użytkownika w jednym żądaniu.

### 3.3 DTO odpowiedzi – `UserResponseDto`

Plik: `src/main/java/com/betoniarka/biblioteka/appuser/UserResponseDto.java`

Zwracany klientowi przy operacjach na użytkownikach:
- `appUserId`,
- `username`,
- `firstname`,
- `lastname`,
- `email`,
- `appRole`.

### 3.4 Serwis użytkowników – `AppUserService`

Plik: `src/main/java/com/betoniarka/biblioteka/appuser/AppUserService.java`

Odpowiada za logikę biznesową użytkowników:
- `getAll()` – pobiera listę wszystkich użytkowników,
- `create(UserCreateRequestDto)` – tworzy nowego użytkownika:
  - koduje hasło przy użyciu `PasswordEncoder`,
  - ustawia domyślną rolę `APP_USER`,
  - zapisuje użytkownika w bazie,
  - zwraca `UserResponseDto`,
- `update(UserUpdateAppUserDto)` – aktualizuje dane istniejącego użytkownika (w tym hasło),
- `delete(Long id)` – usuwa użytkownika po identyfikatorze,
- `toDto(AppUser)` – konwertuje encję na DTO odpowiedzi.

Repozytorium: `AppUserRepository` (rozszerza `JpaRepository<AppUser, Long>`) zapewnia podstawowe operacje CRUD oraz wyszukiwanie po `username`.

### 3.5 Kontroler użytkowników – `AppUserController`

Plik: `src/main/java/com/betoniarka/biblioteka/appuser/AppUserController.java`

Wystawia REST API pod ścieżką `/appusers`:
- `GET /appusers` – odczyt listy użytkowników,
- `POST /appusers` – tworzenie nowego użytkownika,
- `PUT /appusers` – aktualizacja danych użytkownika,
- `DELETE /appusers` – usuwanie użytkownika (na podstawie przekazanego `id`).

Walidacja wejścia odbywa się poprzez `@Valid` na DTO.

---

## 4. Rejestracja, logowanie i bezpieczeństwo (autentykacja/autoryzacja)

### 4.1 Rejestracja i logowanie – `AuthController`

Plik: `src/main/java/com/betoniarka/biblioteka/auth/AuthController.java`

Endpointy:
- `POST /auth/register` – rejestracja użytkownika:
  - przyjmuje `UserCreateRequestDto` i deleguje tworzenie do `AppUserService`,
  - jest publicznie dostępny (brak wymogu logowania),
- `GET /auth/login` – logowanie:
  - wykorzystuje standardową autentykację Spring Security (HTTP Basic),
  - zwraca informację o zalogowanym użytkowniku (`authentication.getName()`).

### 4.2 Integracja z Spring Security – `SecurityConfig`

Plik: `src/main/java/com/betoniarka/biblioteka/security/SecurityConfig.java`

Najważniejsze elementy:
- Bean `PasswordEncoder` (`BCryptPasswordEncoder`) – do bezpiecznego przechowywania haseł,
- Konfiguracja endpointów:
  - endpointy publiczne (nie wymagające uwierzytelnienia):
    - `/auth/register`,
    - `/auth/login`,
    - `/v3/api-docs/**`, `/swagger-ui/**`, `/swagger-ui.html`,
  - pozostałe żądania wymagają uwierzytelnienia,

### 4.3 Szczegóły użytkownika w Security – `AppUserDetails` i `AppUserDetailsService`

Pliki:
- `src/main/java/com/betoniarka/biblioteka/security/AppUserDetails.java`
- `src/main/java/com/betoniarka/biblioteka/security/AppUserDetailsService.java`

Funkcje:
- `AppUserDetails` implementuje `UserDetails`:
  - zwraca login, hasło oraz kolekcję uprawnień (`GrantedAuthority`) na podstawie roli (`ROLE_<nazwa_roli>`),
- `AppUserDetailsService` implementuje `UserDetailsService`:
  - w metodzie `loadUserByUsername` wyszukuje użytkownika po `username` w `AppUserRepository`,
  - jeśli użytkownik nie zostanie znaleziony, rzucany jest `UsernameNotFoundException`.

Razem zapewniają one integrację encji `AppUser` z mechanizmem autentykacji i autoryzacji Spring Security.

---

## 5. Zgodność z wymaganiami M1

Zgodnie z założeniami M1:

- **Kompletny model:**
  - obiektowy – encje `AppUser`, `Book`, `Category`, `Author`, `BorrowedBook`, `QueueEntry`, `Review` odzwierciedlają kluczowe pojęcia systemu biblioteki,
  - bazodanowy – wszystkie encje są zmapowane na tabele JPA z relacjami (w tym wiele–do–wielu dla książek i kategorii).

- **Rejestracja użytkownika (REST):**
  - zaimplementowana przez `AuthController` (`POST /auth/register`) i `AppUserService.create`.

- **Operacje CRUD na użytkownikach:**
  - tworzenie, odczyt, modyfikacja i usuwanie użytkowników za pośrednictwem `AppUserController` i `AppUserService`.

- **Autentykacja i autoryzacja:**
  - integracja Spring Security z encją `AppUser`,
  - publiczne endpointy rejestracji/logowania,
  - ochrona pozostałych zasobów,
  - role użytkowników mapowane na uprawnienia (możliwość dalszego doprecyzowania reguł dostępu).
