<div align="center">

# কাজ · Kaaj – The Job

### Spring Boot backend · React (TypeScript) web app · Kotlin (Jetpack Compose) Android app

*Verified work nearby. Wages always shown. Job seekers never pay.*

**CSE471: System Analysis and Design** · Fall 2026 · Group 08 · Member-3: Ahsan Habib (22201027)

</div>

---

> **Start here:** [`Doc/PROJECT_CONTEXT.md`](Doc/PROJECT_CONTEXT.md) describes the whole project (backend, web app, Android app),
> what has been built and checked, and what is still left. Guides: [`Doc/Guides/`](Doc/Guides/).

This repository holds **the complete project in one place**: one Spring Boot backend, the React web app and the Kotlin Android app,
with every document. The two apps are also submitted separately (see [Where each app is submitted](#where-each-app-is-submitted)).

| Folder | What is inside |
|---|---|
| [`src/`](src/) | **Spring Boot backend** (Java 25, Spring Boot 4.1, PostgreSQL), shared by both apps; port 1027 |
| [`web/`](web/) | **Web app** (React + TypeScript): the CSE471 group project, Member-3's three modules |
| [`android/`](android/) | **Android app** (Kotlin + Jetpack Compose): the Kotlin course project, every role ([`android/README.md`](android/README.md)) |
| [`Doc/`](Doc/) | Project context, guides 01–07, AI declaration and prompts, diagrams, screenshots, the planning documents |
| [`docs/`](docs/) | The first build guide (PDF) and the plan for Member-3's first two features |
| [`postman/`](postman/) | API requests for testing (`Kaaj_Member3.postman_collection.json`) |
| `pom.xml`, `mvnw`, `mvnw.cmd`, `.mvn/` | Maven build; `.\mvnw.cmd -Pweb package` also builds the web app into the jar |
| `.env.example`, `compose.yaml` | Settings template (copy to `.env`, which is never committed) and an optional PostgreSQL in Docker |

## What this is

The Kaaj web app with all of Member-3's features from the CSE471 functional requirements (Group 08):

| # | Feature | What it does |
|---|---------|--------------|
| 3 | **Employer profile and verification** | Employers create a business profile (name, type, address, map pin, contact person, phone) and upload a trade licence or national ID. Admins approve or reject it with a reason. Approved employers get a **Verified** badge on all their job posts. |
| 4 | **Post and manage jobs** | Verified employers post a job with a title, category, openings, **required** wage range, location, hours, shift and requirements; they edit, pause, close or repost it. A post closes automatically 30 days after it is first published. An unverified employer's post waits as a draft and goes live as soon as the admin approves the business. |
| Module 2 | **Scam screening and moderation** | Every submitted post is checked before it goes live: asking job seekers for money **blocks** it; phone numbers / "WhatsApp only", easy-money promises or an overseas job without a licence number send it to **review**; an optional free AI check can add a review flag. Users report posts (three reports hide one). Admins work a moderation queue with the red flags highlighted, and remove the post, suspend the employer, or dismiss, always with a note and an audit entry. |
| Module 3 | **AI job-post assistant** | The employer types one sentence in Bangla, Banglish or English ("2 jon pickup driver lagbe, Tongi, beton 16-20 hajar, sokal 8ta theke sondha 6ta"); Kaaj drafts the title and description in both languages, fills in openings, wage, hours and place, asks what is missing, and flags problems. "Use this draft" fills the form. Uses free Gemini or Groq when a key is set, templates otherwise. |

**Two front ends, one backend.** The same Spring Boot API serves:

- the **web app** (`web/`, React + TypeScript) — the CSE471 group version, with Member-3's features above;
- the **Android app** (`android/`, Kotlin + Jetpack Compose) — the single-member version, with every role:
  sign-in, employer verification, posting and managing jobs, the admin queue, and the job seeker side
  (profile, job search with distance, saved jobs, one-click apply and tracking, and the employer's ranked applicants).
  See [`android/README.md`](android/README.md).

Also included, from Member-3's shared-setup work: the database migrations, the taxonomy search (Bangla, Banglish and English, spelling-tolerant), the first admin account, the audit log and the shared file upload. A simple email-and-password sign-in **stands in** for Member-1's Google sign-in and Member-2's email codes until they are merged.

## Run it

You need **JDK 25**, **Node.js 24+** and **PostgreSQL** (16 or newer; 18 recommended).

1. Create an empty database named `kaaj` (pgAdmin → Databases → Create, or `psql -U postgres -c "CREATE DATABASE kaaj;"`).
2. Copy `.env.example` to `.env` and set `DB_PASSWORD` (and the admin email and password if you like).
3. Start the backend (port **1027**), with the demo data:

   ```powershell
   .\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
   ```

   Flyway creates every table on the first start. API docs: http://localhost:1027/swagger-ui.html

4. In a second terminal, start the web app:

   ```powershell
   cd web
   npm install
   npm run dev
   ```

   Open http://localhost:5173. Sign in as `karim@demo.kaaj` (verified employer), `tongigarments@demo.kaaj` (not verified yet), `quickjobs@demo.kaaj` (posts caught by scam screening) or the admin from `.env` (**Moderation** in the menu). Demo password: `Kaaj@12345`.

   Optional: put a free `GEMINI_API_KEY` (https://aistudio.google.com) in `.env` so the job-post assistant and the scam check use AI. Without it they use templates and rules.

5. For the **Android app**, open the `android` folder in Android Studio and run it on an emulator
   (it talks to http://10.0.2.2:1027). Job seeker demo accounts: `rahim@demo.kaaj`, `sumi@demo.kaaj`, `jamal@demo.kaaj`.

**One jar for everything:** `.\mvnw.cmd -Pweb package` builds the React app into the jar; then `java -jar target\kaaj-0.1.0-SNAPSHOT.jar` serves both at http://localhost:1027.

## Tests

```powershell
.\mvnw.cmd test        # Java: rules, status flows, file checks, module boundaries; the end-to-end API tests need Docker Desktop (skipped without it)
cd web; npm test        # TypeScript: form rules, formatting, the verification banner, translations, red-flag highlighting, the assistant
```

Both test runs also check the MVC layers: `MvcLayersTest` fails if a controller reaches the database, and `web/src/architecture.test.ts`
fails if a page calls the server directly.

Android (in `android/`): `.\gradlew.bat test` — form rules, formatting, and the HTTP layer (cookies, CSRF, errors) against a fake server.

Postman: import `postman/Kaaj_Member3.postman_collection.json` and run the folders in order (folder 5 is the job seeker side).

## How it is built

```
src/main/java/app/kaaj/
├── KaajApplication.java
├── shared/      common types, errors (ProblemDetail in bn/en), FileStore upload, taxonomy search; scam patterns; free AI client
├── identity/    users, roles, session sign-in, first admin, audit log, suspension   (shared setup)
├── employer/    Feature 3: business profile, document, verification, admin queue
├── job/         Feature 4: job posts, status rules, events, 30-day expiry; rule checks; Module 3 job-post assistant;
│                job search and saved jobs (JobSearchController)
├── trust/       Module 2: screening of every post, reports, moderation queue and decisions
├── seeker/      job seeker profile with completeness meter                    (Android build)
└── application/ one-click apply, tracking timeline, ranked applicants        (Android build)
src/main/resources/
├── db/migration/   V1–V3 shared setup · V130 employer · V140 job · V150 seeker profile + saved jobs · V160 applications · V240 trust (cases, reports)
├── db/demo/        demo data (dev profile only)
└── messages*.properties   API messages in English and Bangla
android/                         Kotlin + Jetpack Compose app for every role (see android/README.md)
web/src/
├── features/employer-profile/   Feature 3 pages
├── features/employer-jobs/      Feature 4 pages (4-step "Post a job", My jobs, job page, dashboard) + the assistant panel
├── features/admin-trust/        admin verification queue and moderation queue
├── features/jobs/               public job page with the report form
├── components/                  layout, map picker (Leaflet + OpenStreetMap), upload, taxonomy picker, badges
└── i18n/                        bn.json (default) and en.json
```

Each module is a package under `app.kaaj`; other modules use only its top-level types (`EmployerDirectory`, `JobDirectory`, events), never its `internal` package. `ModularityTests` (Spring Modulith) fails the build if that rule is broken.

**MVC:** controllers (`*Controller.java`) only read the request and call a service; the model is the services, entities and repositories; the views are the JSON records (`JobViews`, `TrustViews`) drawn by the React pages. In the web app, pages call the server only through the `features/*/api.ts` hooks. `MvcLayersTest` and `web/src/architecture.test.ts` check both rules. See [`Doc/Guides/06_MVC_Architecture.md`](Doc/Guides/06_MVC_Architecture.md).

### API

| Method | Path | Who | Purpose |
|---|---|---|---|
| GET | `/api/v1/auth/csrf` | anyone | Sets the `XSRF-TOKEN` cookie (send it back as `X-XSRF-TOKEN`) |
| POST | `/api/v1/auth/login`, `/auth/register`, `/auth/logout` | anyone | Session sign-in (stand-in) |
| GET | `/api/v1/me` | signed in | The current user |
| GET | `/api/v1/taxonomy/occupations?q=`, `/taxonomy/skills?q=` | anyone | Bangla/Banglish/English search |
| GET, PUT | `/api/v1/employer/profile` | employer | Business profile |
| POST | `/api/v1/employer/profile/document` | employer | Upload trade licence or NID (sends for review) |
| POST | `/api/v1/employer/profile/submit` | employer | Send for review again |
| GET | `/api/v1/admin/employers?status=PENDING`, `/admin/employers/counts`, `/admin/employers/{id}` | admin | Verification queue |
| POST | `/api/v1/admin/employers/{id}/verification` | admin | `APPROVE`, or `REJECT` with a reason |
| GET, POST | `/api/v1/employer/jobs` (`?status=&q=&cursor=`, `?submit=true`) | employer | My jobs; create (with `Idempotency-Key`) |
| GET | `/api/v1/employer/jobs/summary` | employer | Posts per status |
| GET, PUT, PATCH, DELETE | `/api/v1/employer/jobs/{id}` | employer | One post; edit; `SUBMIT`/`PAUSE`/`RESUME`/`CLOSE`/`REPOST`; delete a draft |
| GET | `/api/v1/jobs/{id}` | anyone | A published post with the Verified badge |
| GET | `/api/v1/jobs?q=&occupation=&lat=&lng=&radiusKm=&wageMin=&type=&shift=&sort=&cursor=` | anyone | Job search: keyword (Bangla/Banglish/English), distance, monthly wage, type, shift; `NEWEST`/`NEAREST`/`WAGE` |
| GET, PUT | `/api/v1/me/profile` | job seeker | Job seeker profile with completeness meter |
| GET, PUT, DELETE | `/api/v1/me/saved-jobs`, `/me/saved-jobs/{jobId}` | job seeker | Saved jobs |
| POST | `/api/v1/jobs/{id}/applications` | job seeker | One-click apply (`Idempotency-Key`) |
| GET, POST | `/api/v1/me/applications`, `/me/applications/{id}`, `/me/applications/{id}/withdraw` | job seeker | Track applications with the timeline; withdraw |
| GET | `/api/v1/employer/jobs/{id}/applicants` | employer | Applicants ranked by match score, with reasons and gaps |
| GET, POST | `/api/v1/employer/applications/{id}`, `/employer/applications/{id}/status` | employer | Open one (marks it viewed); shortlist, interview, hire or not selected |
| POST | `/api/v1/employer/jobs/drafts` | employer | Job-post assistant: `{text}` → a two-language draft with questions and findings (nothing is saved; 20 a day) |
| GET | `/api/v1/employer/jobs/{id}/screening` | employer | Why the employer's own post is in review or blocked |
| POST | `/api/v1/reports` | signed in | Report a post (`ASKED_FOR_MONEY`, `FAKE_JOB`, `HARASSMENT`, `OTHER`); one per post, 10 a day |
| GET | `/api/v1/admin/cases?status=OPEN\|REMOVED\|DISMISSED&cursor=`, `/admin/cases/counts`, `/admin/cases/{id}` | admin | Moderation queue (most reported first) and one case with findings, AI check, reports and the employer |
| POST | `/api/v1/admin/cases/{id}/decision` | admin | `REMOVE_POST` or `SUSPEND_EMPLOYER` (a note is required), or `DISMISS` |
| GET | `/api/v1/files/{key}` | depends | A stored file, after its owner module's check (documents: admins only) |

## Where each app is submitted

| App | Course | Repository | Pushed per |
|---|---|---|---|
| Web app (`web/` + the backend modules of Member-3) | CSE471, Group 08 | The group's repository | **Module**: 1, 2, 3 (one branch and pull request each) |
| Android app (`android/` + the backend) | Kotlin/Android course (instructor `rahman9909`) | A separate repository of its own | **Milestone**: `milestone-2` (Sat 5 Dec 2026), `final` (Sat 12 Dec 2026) |
| Everything | — | **This repository** | The full project and its history |

How to make the two copies, which files go in each push, and the Git commands:
[`Doc/Guides/07_Push_Both_Apps_Separately.md`](Doc/Guides/07_Push_Both_Apps_Separately.md) (exact file lists in
[`Doc/Guides/05_GitHub_Files_Per_Module.md`](Doc/Guides/05_GitHub_Files_Per_Module.md) and [`Doc/Guides/github-files/`](Doc/Guides/github-files/)).

## Guides

| Guide | What it covers |
|---|---|
| [`docs/Kaaj_Member3_Build_Guide.pdf`](docs/Kaaj_Member3_Build_Guide.pdf) | The first guide: start the project, or build Member-3's first two features step by step |
| [`01_One_Backend_Two_Apps`](Doc/Guides/01_One_Backend_Two_Apps.md) | How one backend serves the web app and the Android app; problems and fixes |
| [`02_GitHub_Milestone_Pushes`](Doc/Guides/02_GitHub_Milestone_Pushes.md) | Tags, the instructor's access and Git fixes for the Android course |
| [`03_Business_Model`](Doc/Guides/03_Business_Model.md) | Business model for both apps |
| [`04_Run_Backend_Web_Android`](Doc/Guides/04_Run_Backend_Web_Android.md) | Run the backend, the web app and the Android app on Windows 11 |
| [`05_GitHub_Files_Per_Module`](Doc/Guides/05_GitHub_Files_Per_Module.md) | Exactly which files go in each web module push and each Android commit |
| [`06_MVC_Architecture`](Doc/Guides/06_MVC_Architecture.md) | How the web app follows Model–View–Controller, file by file |
| [`07_Push_Both_Apps_Separately`](Doc/Guides/07_Push_Both_Apps_Separately.md) | Copy the project, then push the web app per module and the Android app per milestone |

Every guide has a PDF next to it. The AI declaration and every prompt are in [`Doc/AI_Declaration.md`](Doc/AI_Declaration.md) and
[`Doc/AI_Prompts.md`](Doc/AI_Prompts.md).

## History of this repository

This repository started as Lab Assignment 03 (a first Spring Boot REST API, package `com.example.studentapi`). That code was replaced
by Kaaj on 19 November 2026; it is still in the history (commit `84a5a39` and earlier).
