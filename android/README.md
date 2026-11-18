# Kaaj — Android app (Kotlin + Jetpack Compose)

The Android version of Kaaj. It talks to the **same Spring Boot backend** as the web app
(`../src`, port 1027), with the same accounts, data, Bangla/English error messages and rules.

| | |
|---|---|
| Language / UI | Kotlin 2.4, Jetpack Compose, Material 3 |
| Build | Android Gradle Plugin 9.4.1 (built-in Kotlin), Gradle 9.6.0, compileSdk 37, minSdk 26 |
| Network | Retrofit 3 + OkHttp 4 + kotlinx.serialization, session cookie + CSRF header |
| Map | osmdroid (OpenStreetMap, free, no API key) |
| Tests | JUnit 4 + OkHttp MockWebServer (`app/src/test`) |

## Run it

1. **Start the backend** from the project root (the folder above this one):
   `.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"`
   The dev profile loads the demo data: employers karim@demo.kaaj and tongigarments@demo.kaaj, job seekers
   rahim@demo.kaaj, sumi@demo.kaaj and jamal@demo.kaaj (password `Kaaj@12345` for all), and the admin
   `admin@kaaj.local` / `Admin@12345`. Debug builds list them on the sign-in screen — tap one to fill the form.
2. **Open this `android` folder** in Android Studio (File → Open → `studentapi\android`), not the project root.
   Let Gradle sync finish.
3. **Run** on an emulator. The emulator reaches your computer at `http://10.0.2.2:1027` — that is the default.
4. **On a real phone** (same Wi-Fi as the computer): find the computer's IP with `ipconfig`
   (for example `192.168.0.105`), then in the app tap the server address under the sign-in card
   (or Settings → Server address) and enter `http://192.168.0.105:1027`. Allow Java through
   Windows Firewall the first time. Debug builds allow plain HTTP for this.

Run the unit tests: `.\gradlew.bat test` (or right-click `app/src/test` → Run tests).

## What's in it

| Screen | Who | Backend |
|---|---|---|
| Sign in / Create account | everyone | `POST /api/v1/auth/login`, `/register`, `GET /me` |
| Dashboard | employer | `/employer/profile`, `/employer/jobs/summary`, `/employer/jobs` |
| My business (profile, map pin, document by camera/photo/PDF, verification status) | employer | `/employer/profile`, `/employer/profile/document`, `/employer/profile/submit` |
| Post a job (4 steps) / Edit | employer | `POST /employer/jobs` (Idempotency-Key), `PUT /employer/jobs/{id}` |
| My jobs (filters, search, actions) / Job details | employer | `/employer/jobs`, `PATCH /employer/jobs/{id}`, `DELETE /employer/jobs/{id}` |
| Verification queue / Employer review (document preview, approve/reject) | admin | `/admin/employers`, `/admin/employers/{id}/verification`, `/files/{key}` |
| Applicants (ranked by match, with reasons and gaps) / Applicant (shortlist, interview, hire, not selected) | employer | `/employer/jobs/{id}/applicants`, `/employer/applications/{id}`, `/employer/applications/{id}/status` |
| Find jobs (keyword, near me, category, type, shift, wage, sort) | job seeker | `GET /jobs` |
| Job page with Save and one-click Apply | job seeker | `GET /jobs/{id}`, `PUT/DELETE /me/saved-jobs/{id}`, `POST /jobs/{id}/applications` (Idempotency-Key) |
| Saved jobs | job seeker | `GET /me/saved-jobs` |
| My applications / Application (timeline, withdraw) | job seeker | `GET /me/applications`, `GET /me/applications/{id}`, `POST /me/applications/{id}/withdraw` |
| My profile (occupations, skills, licences, home area, wage, shifts, completeness meter) | job seeker | `GET/PUT /me/profile` |
| Settings (language, server address, sign out) | everyone | `POST /auth/logout` |

## Code map

```
app/src/main/java/app/kaaj/android/
├── KaajApp.kt, MainActivity.kt      app start, OpenStreetMap setup, Compose entry
├── data/
│   ├── model/Models.kt              JSON records (same as web/src/lib/types.ts)
│   ├── net/                         Retrofit API, cookie jar, CSRF / language / server interceptors, errors
│   ├── Repositories.kt              one class per backend area
│   ├── AppContainer.kt              settings, language, session, hand-written dependency container
│   └── DocumentFiles.kt             reads, rotates and shrinks picked documents (≤ 3 MB)
├── domain/                          plain Kotlin rules, unit-tested: Format, Validation, ProfileForm, JobForm, SeekerForm
└── ui/                              Compose screens + ViewModels, one folder per area
    ├── auth/  employer/  jobs/  applicants/  admin/  seeker/  settings/
    ├── common/                      shared widgets: cards, chips, map picker, document picker, labels
    └── nav/Routes.kt                type-safe Navigation Compose routes
```

### How sign-in works on a phone

The backend uses a session cookie (`JSESSIONID`) and Spring Security's CSRF protection, like a browser.
`KaajCookieJar` keeps both cookies (saved in SharedPreferences so you stay signed in), and
`CsrfInterceptor` copies the `XSRF-TOKEN` cookie into the `X-XSRF-TOKEN` header on every
POST/PUT/PATCH/DELETE — fetching one from `GET /api/v1/auth/csrf` first if needed, and retrying once
after a 403. A 401 on a normal request takes you back to the sign-in screen.

Icons in `res/drawable/ic_*.xml` are Google's Material icons (Apache License 2.0).
