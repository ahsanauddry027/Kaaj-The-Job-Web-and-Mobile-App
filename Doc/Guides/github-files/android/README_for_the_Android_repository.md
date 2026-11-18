<div align="center">

# কাজ · Kaaj – The Job (Android)

### Kotlin + Jetpack Compose Android app · Spring Boot backend

*Verified work nearby. Wages always shown. Job seekers never pay.*

**Kotlin/Android course** · single-member project · Ahsan Habib (22201027)

</div>

---

**Milestone 2 video (5–8 min):** *add the link here*
**Final video (8–10 min):** *add the link here*

## What this is

Kaaj connects job seekers in Bangladesh, first-time and blue-collar workers first, with **verified employers**, in Bangla and English.
The Android app (`android/`) talks to the Spring Boot backend in this repository (`src/`).

| Role | What the app does |
|---|---|
| Everyone | Register and sign in, switch বাংলা/English, set the server address |
| Employer | Business profile with a map pin; licence or NID photo/PDF for verification; post jobs (4 steps), pause, close, repost; applicants ranked by a match score; shortlist, interview, hire or decline |
| Admin | Verification queue: open the document, approve, or reject with a reason |
| Job seeker | Profile with a completeness meter; search near me by keyword, distance, wage, type and shift; save jobs; apply with one tap; track each application on a timeline |

| Milestone | Tag | What it contains |
|---|---|---|
| Milestone 2 (Sat 5 Dec 2026) | `milestone-2` | Registration and sign-in, employer profile and verification, post and manage jobs, the admin queue (the job seeker side is in the code too) |
| Final (Sat 12 Dec 2026) | `final` | The job seeker side and ranked applicants (shown in the final video), the report, the wireframe and schema diagrams |

## Run it

You need **JDK 25**, **PostgreSQL** (16 or newer) and **Android Studio**.

1. Create an empty database named `kaaj`, copy `.env.example` to `.env` and set `DB_PASSWORD`.
2. Start the backend with the demo data (port **1027**):
   ```powershell
   .\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
   ```
3. Open the **`android`** folder in Android Studio, wait for the Gradle sync, and run the app on an emulator
   (it talks to `http://10.0.2.2:1027`). For a real phone, see [`android/README.md`](android/README.md).

Demo accounts (password `Kaaj@12345`): `karim@demo.kaaj` (verified employer), `tongigarments@demo.kaaj` (not verified yet),
`rahim@demo.kaaj`, `sumi@demo.kaaj`, `jamal@demo.kaaj` (job seekers); the admin comes from `.env` (`admin@kaaj.local` / `Admin@12345` by default).

Step-by-step instructions with fixes: [`Doc/Guides/04_Run_Backend_Web_Android.pdf`](Doc/Guides/04_Run_Backend_Web_Android.pdf).

## Tests

```powershell
.\mvnw.cmd test                       # backend (the end-to-end tests need Docker Desktop)
cd android; .\gradlew.bat test        # app: form rules, formatting, and the HTTP layer against a fake server
```

## How it is built

```
android/app/src/main/java/app/kaaj/android/
├── data/      Retrofit API (KaajApi), session cookie + CSRF interceptors, repositories, models
├── domain/    form rules and formatting (JobForm, ProfileForm, SeekerForm, Validation, Format)
└── ui/        Compose screens per role: auth, employer, jobs, admin, seeker, applicants, settings
src/main/java/app/kaaj/   Spring Boot backend: identity, employer, job, seeker, application, trust, shared
```

The app keeps the server's session cookie and sends the CSRF token on every change, so it uses exactly the same API as the web app.

## Documents (`Doc/`)

| File | What it is |
|---|---|
| [`Doc/AI_Declaration.md`](Doc/AI_Declaration.md) | The AI declaration, with its sources |
| [`Doc/AI_Prompts.md`](Doc/AI_Prompts.md) | Every prompt given to the AI, with links to the chats |
| [`Doc/Submission_Checklist.md`](Doc/Submission_Checklist.md) | Files per milestone, video outlines, viva topics |
| [`Doc/diagrams/`](Doc/diagrams/) | Wireframe and database diagrams (Milestone 1) and the schema as built |
| `Doc/Report.pdf` | *(Final)* the report |
| [`Doc/PROJECT_CONTEXT.md`](Doc/PROJECT_CONTEXT.md) | The whole project: what is built, how it was checked, what is left |
