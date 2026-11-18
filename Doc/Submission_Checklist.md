# What to submit, and when — Kaaj (Kotlin/Android version, single member)

Everything goes into **one GitHub repository** that is either **public** or shared with the GitHub
user **`rahman9909`** (repository → Settings → Collaborators → Add people → `rahman9909`).
The Android app needs the Spring Boot backend to run, so the repository holds both.

| Date | What |
|---|---|
| ~~14 Nov 2026~~ | Milestone 1 — wireframe + schema diagrams (10%) — **done** |
| Tue 1 Dec 2026 (lab) | Lab exam: build an app in the lab (study the course Drive code and theory) |
| **Sat 5 Dec 2026, 11:59 pm** | **Milestone 2** — registration and login + at least 2–3 features (50%) + 5–8 min video |
| Sun 6 Dec 2026 (lab) | Milestone 2 shown in the lab; viva possible |
| **Sat 12 Dec 2026, 11:59 pm** | **Final** — code, report, wireframe and schema diagrams (40%) + 8–10 min video |
| Sun 13 Dec 2026 (lab) | Final shown in the lab; viva possible |

No reminders will come before the deadlines.

---

## Milestone 2 — files in the repository

| Path | What it is | Required? |
|---|---|---|
| `android/` | The Kotlin + Jetpack Compose app (open this folder in Android Studio) | **Yes** |
| `android/README.md` | How to run the app (emulator and real phone) | **Yes** |
| `src/`, `pom.xml`, `mvnw`, `mvnw.cmd`, `.mvn/` | The Spring Boot backend the app talks to | **Yes** |
| `.env.example` | Settings template (the real `.env` stays on your PC) | **Yes** |
| `compose.yaml` | Optional PostgreSQL in Docker | Optional |
| `README.md` | Project overview, how to run everything, the API list | **Yes** |
| `Doc/AI_Declaration.md` | AI declaration — fill in section 2 and sign section 5 | **Yes** |
| `Doc/AI_Prompts.md` | Every prompt, with links to the chats | **Yes** |
| `Doc/source-documents/` | The documents given to the AI (context file, requirements, proposal, build prompts) | **Yes** (the declaration's sources) |
| `postman/Kaaj_Member3.postman_collection.json` | API requests for testing | Optional |
| `web/` | The group web app (CSE471) — not needed for this course | Optional |
| Video link (5–8 min) | YouTube *unlisted* or Google Drive (anyone with the link) — put the link at the top of `README.md` and wherever the course asks for it | **Yes** |

Which files go in which commit, and the commands: `Guides/05_GitHub_Files_Per_Module.md`, Part A.

**Never commit** `.env`, `android/local.properties`, `data/`, `target/`, `build/` or `node_modules/` (all are in `.gitignore`).

Milestone 2 features to show (the course asks for login + 2–3 features):

1. **Registration and login** — email + password, role (job seeker or employer), session kept between app starts.
2. **Employer profile and verification** — business form with map pin, trade licence or NID by camera/photo/PDF, status Unverified → Under review → Verified/Rejected.
3. **Post and manage jobs** — 4-step form with checks, publish, filters, pause/resume/close/repost, 30-day expiry.
4. **Admin verification queue** — document preview, approve, reject with a reason.

Mark the snapshot after the last push before the deadline:

```powershell
git tag -a milestone-2 -m "Milestone 2: login + employer verification + post/manage jobs + admin queue"
git push origin milestone-2
```

### Milestone 2 video (5–8 minutes) — suggested outline

| Time | Show |
|---|---|
| 0:00–0:30 | Name, ID, the app in one sentence, the stack (Kotlin/Compose ↔ Spring Boot ↔ PostgreSQL) |
| 0:30–1:15 | Architecture: Android → Retrofit → REST API; session cookie + CSRF header; Bangla/English |
| 1:15–2:15 | Create an employer account, sign out, sign in; switch language |
| 2:15–3:45 | Business profile: form, map pin, photo of a licence → "Under review"; admin rejects with a reason; employer fixes and resends; admin approves → Verified |
| 3:45–5:15 | Post a job (show a validation error), publish; My jobs filters; pause, close, repost; an unverified employer's post waits as a draft |
| 5:15–6:45 | Code: `KaajApi.kt`, `CsrfInterceptor`/`KaajCookieJar`, `JobForm` rules and their unit tests, one ViewModel + screen, backend `JobService` |
| 6:45–7:30 | Next plan: job seeker side, testing on a real phone, the report |

---

## Final — files in the repository

Everything from Milestone 2, updated, plus:

| Path | What it is | Required? |
|---|---|---|
| `android/` | Now with the job seeker side and the employer's applicants | **Yes** |
| `Doc/Report.pdf` (and `.docx`) | The report, on the course template | **Yes** — the template wasn't attached to the announcement; get it from the course page or the instructor |
| `Doc/diagrams/Kaaj_The_Job_Android_Wireframe_One_Page.pdf` (+ `.drawio`, `.png`) | Wireframe diagram (from Milestone 1) | **Yes** |
| `Doc/diagrams/Kaaj_The_Job_Database_ERD_One_Page.pdf` (+ `.drawio`, `.png`) | Full schema diagram (from Milestone 1) | **Yes** |
| `Doc/diagrams/Kaaj_Schema_As_Built.pdf` (+ `.png`, `.svg`) | The schema as actually built (15 tables, V1–V240) | Recommended |
| `Doc/AI_Declaration.md`, `Doc/AI_Prompts.md` | Updated with any new prompts since Milestone 2 | **Yes** |
| Video link (8–10 min) | Features, code, and the challenges you faced | **Yes** |

```powershell
git tag -a final -m "Final submission"
git push origin final
```

### Final video (8–10 minutes) — suggested outline

| Time | Show |
|---|---|
| 0:00–0:40 | Recap: the problem, the three roles, the stack |
| 0:40–2:30 | Job seeker: profile (completeness meter, map, licences), search near me with a Banglish typo ("draiver"), save a job, apply with one tap, "My applications" timeline |
| 2:30–4:00 | Employer: verification, post a job, applicants ranked by match score with reasons and gaps, shortlist (phone appears), call for interview |
| 4:00–4:40 | Back to the job seeker: status and interview time on the timeline; withdraw |
| 4:40–7:00 | Code: backend modules (`seeker`, `application`, `job`), the search SQL and the match score, security rules, tests; Android layers (data → domain → ui), navigation, interceptors |
| 7:00–9:00 | Challenges (below) and how you solved them |
| 9:00–9:40 | What you would add next (AI job-post assistant, scam screening, chat) |

**Challenges worth telling:**

- Keeping a session on Android: a cookie jar saved to SharedPreferences, and Spring Security's CSRF token copied from the `XSRF-TOKEN` cookie into a header, with one retry after a 403.
- The emulator can't reach `localhost`: `10.0.2.2`, cleartext HTTP allowed only in debug builds, and a server address setting for a real phone.
- Bangla: digits typed in Bangla (`১৮০০০`), South-Asian number grouping (1,00,000), and error messages coming back in the app's language.
- Android Gradle Plugin 9 compiles Kotlin by itself ("built-in Kotlin"), so the usual `kotlin-android` plugin had to go.
- Distance search without PostGIS: the haversine formula in SQL, and home points rounded to about 500 m for privacy.
- One application per person per job, safe to retry: a unique constraint plus an `Idempotency-Key`.
- One backend for two front ends (web and Android) without breaking either.

### Viva — be ready to explain

How sign-in and CSRF work · why statuses can only move in certain ways (`JobStatus`, `ApplicationStatus`) ·
how the match score is calculated · how search pages work (cursor) · what `ViewModel` and `StateFlow`/state
do in Compose · how the app reaches the server · what each unit test checks · what the AI wrote and what you changed.
