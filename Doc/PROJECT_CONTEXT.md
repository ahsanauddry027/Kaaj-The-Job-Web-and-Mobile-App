# Kaaj – The Job · Project context and work log (Android app + web app + Spring Boot backend)

**Owner:** Ahsan Habib (22201027) · GitHub `ahsanauddry027` · `ahsan.habib1@g.bracu.ac.bd`
**Project folder:** `C:\Users\UseR\Desktop\Projects\studentapi`
**Last updated:** 18 November 2026

This file is the single place to start from. It describes the whole project as it stands: what was built, how it fits together,
which decisions were made and why, how it was checked, where every generated file is, and **what is still left**, especially for the web app.

> **Using this file with an AI assistant or a teammate:** give them this file first. For the full product design (every feature's rules,
> the complete data model and the AI design), also give them `Doc/source-documents/Kaaj_The_Job_Project_Context_1.md`.
> Section 13 has a ready-made prompt to continue the work.

## Contents

1. [The project in one page](#1-the-project-in-one-page)
2. [Two courses, two front ends, one backend](#2-two-courses-two-front-ends-one-backend)
3. [People, accounts and places](#3-people-accounts-and-places)
4. [Repository map](#4-repository-map)
5. [Tech stack and versions](#5-tech-stack-and-versions)
6. [The backend](#6-the-backend)
7. [The web app](#7-the-web-app)
8. [The Android app](#8-the-android-app)
9. [How everything was checked (and what was not)](#9-how-everything-was-checked-and-what-was-not)
10. [Work log: everything that was done, in order](#10-work-log-everything-that-was-done-in-order)
11. [Index of documents and generated files](#11-index-of-documents-and-generated-files)
12. [What is still left](#12-what-is-still-left)
13. [Continuing the work with an AI assistant](#13-continuing-the-work-with-an-ai-assistant)

---

## 1. The project in one page

**Kaaj – The Job** (কাজ) is a job platform for Bangladesh. It connects job seekers, with blue-collar and first-time workers given
priority, to **verified employers**, in **Bangla first** (English too). Its promises: *verified work nearby, wages always shown,
job seekers never pay.*

Three roles:

| Role | Can do (today) |
|---|---|
| **Job seeker** | Profile with a completeness meter; search jobs by keyword (Bangla, Banglish or English), distance, wage, type and shift; save jobs; apply with one click; track each application on a timeline; withdraw; **report a post** (web) |
| **Employer** | Business profile with a map pin; upload a trade licence or NID for verification; post jobs (4-step form, or **one sentence → a two-language draft** with the job-post assistant, web), edit, pause, resume, close, repost; see why a post is in review or blocked; see applicants ranked by a match score; shortlist, interview, hire or decline |
| **Admin** | Verification queue: open the document, approve, or reject with a reason; **moderation queue** (web): posts flagged by scam screening or reported by users, red flags highlighted, remove the post, suspend the employer, or dismiss |

The idea started as "KajKhoj" and became *Kaaj – The Job* (October 2026). The full vision (AI voice profiles, AI job matching,
chat) is in `Doc/source-documents/`; section 12 lists which parts are not built yet.

## 2. Two courses, two front ends, one backend

| | CSE471 group project | Kotlin/Android course project |
|---|---|---|
| Course | CSE471 System Analysis and Design, Fall 2026, Group 08, Lab Section 01 | Android course with the Kotlin milestone plan (instructor's GitHub: `rahman9909`) |
| Team | 3 members (Ahsan is **Member-3**) | **Ahsan alone** (single member) |
| Front end | **Web app**: React + TypeScript (`web/`) | **Android app**: Kotlin + Jetpack Compose (`android/`) |
| Backend | The same Spring Boot backend (`src/`) | The same Spring Boot backend (`src/`) |
| Scope built so far | **All of Member-3's features**: Module 1 (employer profile and verification; post and manage jobs), Module 2 (scam screening and moderation), Module 3 (AI job-post assistant) + admin queues + shared setup | Every role: sign-in, employer verification, jobs, admin queue, and the whole job seeker side with ranked applicants |
| Deadlines | Per the CSE471 course plan | Milestone 1 (wireframe + schema) **submitted**; lab exam **Tue 1 Dec 2026**; **Milestone 2 Sat 5 Dec 2026, 11:59 pm** (registration/login + 2–3 features + 5–8 min video, 50%); **Final Sat 12 Dec 2026, 11:59 pm** (code, report, wireframe and schema diagrams + 8–10 min video, 40%) |
| Submission | Group repository (to be agreed with the team); one push per module, see `Doc/Guides/05_GitHub_Files_Per_Module.md` and guide 07 | GitHub, **public or shared with `rahman9909`** (its own repository, `Kaaj-The-Job-Android`, see guide 07); AI declaration with all prompts and documents in `/Doc` |

**How one backend serves both:** both apps call the same `/api/v1` endpoints with the same session cookie, the same CSRF token and the same
JSON. The browser handles cookies by itself; the Android app uses `KaajCookieJar` + `CsrfInterceptor`. Full details, differences and fixes:
`Doc/Guides/01_One_Backend_Two_Apps.md`.

## 3. People, accounts and places

| Who / what | Details |
|---|---|
| Member-1 | Md. Imran Ali (22299402): skill certification, job search, AI job matching, ranked applicants; Google sign-in, security and **account administration** (suspend an account, change a user's role) in the shared setup |
| Member-2 | Shafin Ahmed (22299405): job seeker profile, apply and track, AI voice profile, messages and notifications; email sign-up with codes in the shared setup |
| Member-3 | **Ahsan Habib (22201027)**: post and manage jobs, employer profile and verification, AI job-post assistant, scam screening and moderation; roles, the admin account and the shared file upload in the shared setup. (Suspending an employer was built only as far as moderation needs it; general account administration is Member-1's.) |
| Kotlin course instructor | GitHub `rahman9909` |
| Backend port | **1027** (last four digits of the student ID) |
| Inputs folder | `C:\Users\UseR\Desktop\Projects\Kaaj The Job` (requirements, context file, proposal, build prompts, Figma screen PNGs, Milestone 1 ERD and wireframe sources) |
| Git | `studentapi` is a Git repo; `origin` = `https://github.com/ahsanauddry027/SpringBoot_PostgreSQL_Api.git` (first used for Assignment 03, now the **full Kaaj project**). On 19 Nov 2026 everything was committed on branch `kaaj-the-job` (five commits on top of Assignment 03's `84a5a39`); you push it and merge it into `Main` (`Doc/Guides/07_Push_Both_Apps_Separately.md`, Part 0). The Android app goes to its own repository `Kaaj-The-Job-Android` from a copy, per milestone (guide 07, Part A); the web app to the CSE471 group repository, per module (guide 07, Part W) |
| Secrets | `.env` (git-ignored, never committed): `DB_PASSWORD`, `KAAJ_ADMIN_EMAIL`, `KAAJ_ADMIN_PASSWORD`, optional free AI keys `GEMINI_API_KEY` (aistudio.google.com) and `GROQ_API_KEY` (console.groq.com); template in `.env.example` |

**Demo accounts** (loaded only with the `dev` profile; password `Kaaj@12345` for all demo users):

| Account | Role | State |
|---|---|---|
| `karim@demo.kaaj` | Employer (Karim Transport) | Verified, with sample jobs |
| `tongigarments@demo.kaaj` | Employer (Tongi Garments) | Not verified yet (its post waits as a draft) |
| `quickjobs@demo.kaaj` | Employer (Quick Jobs & Overseas, Uttara) | Verified; three posts for moderation: "Malaysia factory job" **blocked** by screening (asks for a processing fee), "Online data entry" **in review** (WhatsApp, earn from home), "Warehouse helper" published and **reported twice** (by rahim and sumi; a third report from another account hides it) |
| `rahim@demo.kaaj`, `sumi@demo.kaaj`, `jamal@demo.kaaj` | Job seekers | Profiles filled; applications in Shortlisted, Applied and Viewed states |
| Admin from `.env` (`admin@kaaj.local` / `Admin@12345` by default in `dev`) | Admin | Created at start-up when no admin exists |

## 4. Repository map

```
studentapi/
├── src/main/java/app/kaaj/          Spring Boot backend (Spring Modulith modules, see section 6)
├── src/main/resources/
│   ├── application.properties       port 1027, database, upload limits, session cookie, Kaaj settings
│   ├── application-dev.properties   demo data, DEBUG logging, default admin
│   ├── db/migration/                Flyway V1, V2, V3, V130, V140, V150, V160, V240
│   ├── db/demo/                     R__demo_member3.sql, R__demo_seeker.sql, R__demo_trust.sql (dev profile only)
│   └── messages*.properties         API messages in English and Bangla
├── src/test/java/app/kaaj/          JUnit tests (24 files; the flow tests need Docker)
├── web/                             React + TypeScript web app (CSE471 group version)
├── android/                         Kotlin + Jetpack Compose Android app (open THIS folder in Android Studio)
├── postman/                         Kaaj_Member3.postman_collection.json (5 folders, 53 requests), sample-trade-licence.png
├── docs/
│   ├── Kaaj_Member3_Build_Guide.pdf  the first guide: start the project, or build it from scratch step by step
│   └── plans/M1-3_M1-4_Member3.md    decisions for Member-3's two features
├── Doc/                             everything the courses ask for in /Doc (see section 11)
├── pom.xml, mvnw, mvnw.cmd, .mvn/   Maven build (Java 25); `-Pweb` builds the web app into the jar
├── compose.yaml                     optional PostgreSQL in Docker
├── .env.example                     settings template (copy to .env)
├── .gitignore, .gitattributes       .env, data/, target/, node_modules/, build/, "Claude outputs/", "Generated Documents/" are ignored
├── data/uploads/                    uploaded files (git-ignored)
├── Claude outputs/                  your Assignment 03 report (git-ignored, kept out of the repo)
└── Generated Documents/             copies of every guide, PDF and Markdown document in one place, for reading (git-ignored)
```

## 5. Tech stack and versions

Everything is free and publicly available.

| Part | Technology |
|---|---|
| Backend | Java 25, **Spring Boot 4.1.2**, Spring Modulith 2.1.2, Spring Security 7 (session + `csrf.spa()`), Spring Data JPA + JdbcClient, Flyway, PostgreSQL 16+ (18 recommended; `pg_trgm`, no PostGIS needed), springdoc 3.2.0 (Swagger UI), Apache Tika 3.3.3 (file type checks), virtual threads, Testcontainers |
| Web | React 19.3, TypeScript 7.0, Vite 8.3, React Router 8.5, TanStack Query 5, react-hook-form 7 + Zod 4, i18next 27 (`bn` default, `en`), Tailwind CSS 4.3, Leaflet 1.9 + OpenStreetMap, lucide-react icons, Vitest 5 + Testing Library, oxlint |
| Android | Android Gradle Plugin **9.4.1** with built-in Kotlin (Kotlin 2.4.21), Gradle 9.6.0, compileSdk/targetSdk 37, minSdk 26, Java 17; Jetpack Compose BOM 2026.02.01 (Material 3 1.4.0), Navigation Compose 2.10.2 (type-safe routes), Retrofit 3.0.0 + kotlinx-serialization 1.13.0, OkHttp 4.12.0, coroutines 1.10.2, osmdroid 6.1.20 (OpenStreetMap), AppCompat (per-app language) |
| AI (optional, free) | Google Gemini (`gemini-2.5-flash`) first, Groq (`openai/gpt-oss-120b`) second, both through their OpenAI-compatible chat endpoints with Spring's `RestClient`; without keys the assistant uses templates and screening uses rules |
| Tools | Maven wrapper, Gradle wrapper, Node.js 24, Postman, Android Studio, Docker Desktop (optional, for Testcontainers) |

## 6. The backend

### 6.1 Modules (`src/main/java/app/kaaj/`)

| Module | What it holds | Built for |
|---|---|---|
| `shared` | Common types (`WagePeriod`, `Wages`, `DocumentCode`, `Cursor`, `CursorPage`, `UuidV7`), ProblemDetail errors in Bangla/English, `FileStore` (disk, Tika type checks, per-kind limits: photo 2 MB, document 3 MB, voice 3 MB), `/api/v1/files/{key}` with a `FileAccessPolicy` per owner module, taxonomy search (occupations and skills, Bangla/Banglish/English, spelling-tolerant with `pg_trgm`), `SpaForwardController`; **`ScamPatterns`** (Bangla/Banglish/English scam phrases, NFC-normalised, Bangla digits) and the **free AI client** (`AiText`, `OpenAiCompatibleText`: Gemini then Groq, skipped without keys) | Shared setup (Member-3); patterns and AI client for Modules 2 and 3 |
| `identity` | Users and roles (SEEKER, EMPLOYER, ADMIN), session sign-in, `/auth/csrf`, stand-in email+password registration, first admin at start-up, audit log, `SecurityConfig`; `UserAdmin` (suspend/reactivate with audit) and `SuspendedUserFilter` (a suspended user's next request is refused with 403 `auth.suspended` and their session ends) | Shared setup; suspension for Module 2 |
| `employer` | Feature: employer profile and verification; admin queue; `EmployerVerified` / `EmployerRejected` events; trust score 0.5 → 0.8 when verified | Member-3 (web + Android) |
| `job` | Feature: post and manage jobs; status rules; 30-day expiry (checked hourly); **`JobRuleChecks`** (block / review / question findings) and `JobModeration` (publish, hold, block, restore) for screening; the **job-post assistant** (`JobDrafts`, `SentenceFacts`, `DraftTemplate`, `OccupationFinder`, `POST /employer/jobs/drafts`); **public job search** (`JobSearch`, Haversine distance in SQL) and **saved jobs**, served by `JobSearchController` and `JobSearchService` (split out of `PublicJobController` so the group copy builds without them) | Member-3 (Modules 1–3); search and saved jobs added for Android (Member-1's in CSE471) |
| `trust` | Module 2: `Screening` decides every submitted post (rules, then the optional AI check); reports (one per user per post, 10 a day, 3 hide a post); one open case per post; admin decisions REMOVE_POST, SUSPEND_EMPLOYER (both need a note), DISMISS; `JobRemovedAsScam` event | Member-3 (Module 2) |
| `seeker` | Job seeker profile with completeness meter; home location rounded to 0.005° (about 500 m) for privacy | Android build |
| `application` | One-click apply (Idempotency-Key), timeline events, withdraw, ranked applicants with a **rule-based** match score, employer decisions, audit log | Android build |

Other modules may use only a module's top-level types (for example `JobDirectory`, `EmployerDirectory`, `SeekerDirectory`), never its
`internal` package. `ModularityTests` fails the build if that rule is broken.

**MVC.** Inside each module the code is split into Model (entities, services, repositories/SQL stores), View (the records sent as JSON,
such as `JobViews`, `TrustViews`) and Controller (`@RestController` classes, which only read the request and call a service). In the web
app, pages and components are the View, the `features/*/api.ts` hooks and `router.tsx` the Controller, and `lib/types.ts`, `schema.ts`
and `lib/trust.ts` the Model. `MvcLayersTest` (Java) and `web/src/architecture.test.ts` fail the build if a controller reaches the
database or a page calls the server directly. Full map and viva answers: `Doc/Guides/06_MVC_Architecture.md`.

### 6.2 Database (Flyway)

| Migration | Tables |
|---|---|
| `V1__core.sql` | `app_user`, `audit_log`, `email_code` |
| `V2__taxonomy.sql`, `V3__seed_taxonomy.sql` | `occupation`, `occupation_synonym`, `skill`, `skill_synonym` + seed data |
| `V130__employer.sql` | `employer` |
| `V140__job.sql` | `job` |
| `V150__seeker.sql` | `seeker_profile`, `saved_job` (+ index `job_occupation_published_idx`) |
| `V160__application.sql` | `application` (unique per job and seeker; unique `idempotency_key`), `application_event` |
| `V240__trust.sql` | `trust_case` (one open case per subject, partial unique index), `report` (one per reporter and subject) |

15 tables. The schema as built is drawn in `Doc/diagrams/Kaaj_Schema_As_Built.*` by `Doc/diagrams/draw_schema.py`.
Never edit a migration that has already run; add a new one.

### 6.3 Status flows

- **Employer verification:** `UNVERIFIED` → (document sent) `PENDING` → `VERIFIED` or `REJECTED` (a reason is required). A rejected employer who
  fixes the profile or sends a new document goes back to `PENDING`; changing the business name or document after approval also goes back to `PENDING`.
- **Job post:** `DRAFT` → `IN_REVIEW` → `PUBLISHED` or `BLOCKED`; `PUBLISHED` ↔ `PAUSED`; `PUBLISHED`/`PAUSED` → `CLOSED`; editing a live post sends it
  to review again; **repost** copies a closed post into a new draft. A post closes 30 days after its **first** publication. An unverified employer's
  complete post waits as a draft (`AWAITING_VERIFICATION`) and is submitted automatically when the employer is verified. Every
  submitted post goes through **scam screening**: a money request blocks it (`BLOCKED`, `SCREENING_BLOCKED`); contact details, easy money or an
  overseas job without a licence number hold it `IN_REVIEW` (`SCREENING_REVIEW`); otherwise it is published at once. Three reports hold a live
  post (`REPORTED`); an admin's removal blocks it (`REMOVED`) and dismissal publishes it again. (`kaaj.jobs.auto-publish=true` skips screening; only
  for running the job module on its own.)
- **Application:** `APPLIED` → `VIEWED` (when the employer opens it) → `SHORTLISTED` → `INTERVIEW` → `HIRED`, or `DECLINED` from any open state;
  the seeker can `WITHDRAW` an open application and apply again later. The seeker's phone number is shown to the employer only from
  `SHORTLISTED` onwards.
- **Match score** (rule-based, 0–1): occupation matches 0.35; share of asked skills up to 0.25; enough experience 0.15; holds every required
  document 0.15; within travel radius or remote 0.10. Each applicant gets reasons and gaps.

### 6.4 API conventions

- Everything under `/api/v1`; the full endpoint table is in the root `README.md`.
- Errors: RFC 9457 ProblemDetail with `code`, `detail` in the request's language (`Accept-Language: bn` or `en`) and `errors: {field: message}`.
- Session cookie (`JSESSIONID`, HttpOnly, SameSite=Lax, 7 days) + CSRF (`XSRF-TOKEN` cookie → `X-XSRF-TOKEN` header).
- Role rules: `/admin/**` ADMIN; `/employer/**` EMPLOYER; `/me` any signed-in user; `/me/**` SEEKER; `POST /jobs/*/applications` SEEKER;
  `POST /reports` any signed-in user; `/auth/**`, `/taxonomy/**`, `/jobs/**` public.
- Lists are paged with opaque cursors; creating a job and applying take an `Idempotency-Key` header.
- IDs are UUIDv7; times are UTC instants.

### 6.5 Important settings (`application.properties`)

| Setting | Value | Meaning |
|---|---|---|
| `server.port` | 1027 | |
| `kaaj.auth.simple-sign-up` | true | Stand-in email+password sign-up until Google sign-in and email codes are merged |
| `kaaj.jobs.auto-publish` | false | Scam screening decides every post (true only to run the job module without the `trust` module) |
| `kaaj.jobs.drafts-per-day` | 20 | Job-post assistant drafts per employer per day (protects the free AI quota) |
| `kaaj.trust.reports-to-hold`, `reports-per-day` | 3, 10 | Reports that hide a post; reports one person may send a day |
| `kaaj.trust.ai-check`, `when-ai-fails` | true, REVIEW | The optional AI check after the rules, and what happens if the AI call fails |
| `kaaj.ai.providers[0..1]` | Gemini, Groq | Keys from `.env`; a provider without a key is skipped |
| `kaaj.jobs.require-verified-employer` | true | Unverified employers' posts wait as drafts |
| `kaaj.jobs.expiry-days`, `kaaj.jobs.expiry-cron` | 30, `0 5 * * * *` | |
| `spring.servlet.multipart.max-file-size` / `max-request-size` | 3MB / 4MB | |
| `spring.flyway.out-of-order` | true in `dev` | Teammates merge migrations in any order |

## 7. The web app

`web/` · React + TypeScript · run with `cd web; npm install; npm run dev` → <http://localhost:5173> (Vite forwards `/api` to port 1027).
`.\mvnw.cmd -Pweb package` puts the built app inside the jar, served at <http://localhost:1027>.

| Page | Path | State |
|---|---|---|
| Home | `/` | Done |
| Sign in, register (role: job seeker or employer) | `/login`, `/register` | Done (stand-in email + password) |
| Employer dashboard | `/employer` | Done: posts per status, verification banner |
| Business profile + verification panel | `/employer/profile` | Done: form, Leaflet map pin, document upload, status and rejection reason |
| My jobs | `/employer/jobs` | Done: status filters, search, actions menu (pause, resume, close, repost, delete draft) |
| Post a job / edit (4 steps) | `/employer/jobs/new`, `/employer/jobs/:id/edit` | Done: Zod rules, taxonomy picker, map, wage range required |
| **Job-post assistant** ("Write it for me", above the form on a new post) | `/employer/jobs/new` | Done (Module 3): one sentence in Bangla, Banglish or English → title and description in both languages, facts, questions to answer, red flags; "Use this draft" fills the form; blocked when the sentence asks job seekers for money |
| Job page (employer) | `/employer/jobs/:id` | Done; shows **why a post is in review or blocked** (the rule findings and the admin's note) |
| Public job page with the Verified badge | `/jobs/:id` | Done; "Kaaj never asks job seekers for money" note and a **Report this post** form (Module 2) |
| Admin verification queue | `/admin/verification` | Done: document preview, approve, reject with a reason |
| **Admin moderation queue** | `/admin/cases` | Done (Module 2): Open / Removed / Dismissed tabs, most-reported first, red flags highlighted in both languages, rule and AI findings, reports, employer card; remove the post or suspend the employer (a note is required; suspending asks to confirm) or dismiss |
| Job seeker home | `/seeker` | **Placeholder only** (see section 12.3) |

Shared pieces: `AppLayout` (navigation, language switch), `FileUpload`, `MapPicker`, `TaxonomyPicker`, `StatusBadge`, `Toast`, `lib/api.ts`
(cookies, CSRF, ProblemDetail), `lib/format.ts` (Bangla digits, lakh grouping, taka), `i18n/bn.json` and `en.json`.
Module 2 and 3 pieces: `AssistantPanel`, `FindingList`, `ReportButton`, `ModerationPage`, `lib/trust.ts` (highlighting that never splits a Bangla
letter from its vowel sign, note rules), `draftToForm` in `employer-jobs/schema.ts`.
Tests: 9 Vitest files, 33 tests (form rules, formatting, verification banner, upload checks, translations, highlighting and note rules, the
assistant filling the form, and the MVC check that no page calls the server directly). Screenshots of every page (33): `Doc/screenshots/web/` (20–33 are the Module 2 and 3 screens).

## 8. The Android app

`android/` · package `app.kaaj.android` · open the **`android`** folder in Android Studio · default server `http://10.0.2.2:1027`
(the computer, seen from the emulator); change it in **Settings** for a real phone.

| Role | Screens |
|---|---|
| Everyone | Sign in / register (demo accounts listed in debug builds), Settings (server address, language বাংলা/English, sign out), start-up error with a server-address dialog |
| Employer | Dashboard; business profile with osmdroid map pin and document picker (camera, gallery or PDF; photos turned upright and shrunk below 3 MB); My jobs with filters and actions; 4-step job form; job details; **applicants ranked by match score**, applicant details with reasons and gaps, and decisions |
| Admin | Verification queue with counts; employer details with the document drawn by `PdfRenderer`/`BitmapFactory`; approve or reject with a reason |
| Job seeker | Bottom bar: **Find jobs** (keyword, distance from current location or home, wage, type, shift; newest, nearest or best wage; pull to refresh), **Saved**, **Applications** (timeline, withdraw), **Profile** (completeness meter, home location); job page with an apply dialog |

Architecture: one `MainActivity` (AppCompat, per-app locales), Compose + Material 3, Navigation Compose with `@Serializable` routes,
ViewModels holding Compose state, a hand-written `AppContainer` (no DI library), repositories over Retrofit (`KaajApi.kt`), OkHttp interceptors
(server address, language, CSRF with one retry, session expiry), cookies saved in SharedPreferences, form rules in `domain/` (`JobForm`,
`ProfileForm`, `SeekerForm`, `Validation`, `Format`), strings in `values/` and `values-bn/` (about 416 keys), Material icons as vector drawables.
Debug builds allow plain HTTP to any address (`src/debug/res/xml/network_security_config.xml`).
Tests: 5 JUnit files, 29 tests (formatting, validation, job form, seeker form, and the HTTP layer against MockWebServer).

With scam screening on, an employer's post may come back **in review** or **blocked**; the app shows the reason
(`job_reason_screening_review`, `_blocked`, `_reported`, `_removed`). The app has no moderation screen: an admin decides on the web page.

## 9. How everything was checked (and what was not)

| Part | Checked by the AI | Still to do yourself |
|---|---|---|
| SQL migrations and demo data | Ran on PostgreSQL 16 in order; search queries and demo data run | — |
| Backend Java | Parsed and checked with `javac` (without the Spring jars) and a filter for real errors; risky Spring Boot 4.1 / Security 7 / Modulith / Testcontainers APIs matched against their source; match score and rounding run in plain Java | **`.\mvnw.cmd test` has never been run** (Maven Central was not reachable from the AI's workspace) |
| Web app | `npm run build`, Vitest tests passed (33), lint clean for the new files, clicked through in Chrome against a **mock API** with the same JSON (screenshots in `Doc/screenshots/web/`) | Run it once against the **real** backend |
| Module 2 and 3 logic | The scam patterns and the assistant's sentence reading run in plain Java through a small harness (Karim's sentence gives 2 openings, Tk 16,000–20,000 a month, 08:00–18:00, Tongi); the trust SQL ran on PostgreSQL 16 | The Spring tests `TrustFlowTests`, `JobDraftFlowTests` (Docker) |
| Group-repository pushes | Pushes 1, 2 and 3 rebuilt one after another: after each, the web app type-checks, tests pass and it builds, migrations and demo data run, and every Java class used is present | Run `Copy-Push.ps1` on Windows (it was not run here) |
| Android app | Static checks only: brace balance, imports, every `R.string`/`R.drawable` reference exists, library APIs checked against their source and the Android docs | **Never compiled.** Run `.\gradlew.bat test` and the app in Android Studio. If sync fails on `compileSdk { version = release(37) }`, use `compileSdk = 37` |

## 10. Work log: everything that was done, in order

All prompts are word for word in `Doc/AI_Prompts.md`, with links to the chats.

**Before this build (other chats):** the idea and name; the proposal, the functional requirements (12 features, 3 modules), the project context
file and the module build prompts; Figma screens and an HTML employer console for Member-3's two features; Lab Assignment 03 (the first Spring
Boot REST API in `studentapi`, package `com.example.studentapi`, port 1027, with a Postman collection and a report); Milestone 1 ERD and Android
wireframe (submitted).

**Request 1 — analyse the folder; build Member-3's first two features in TypeScript + Spring Boot; a detailed PDF guide.**
Read every input; planned the two features (`docs/plans/M1-3_M1-4_Member3.md`); wrote the modular backend (`shared`, `identity`, `employer`, `job`),
migrations V1–V3, V130, V140, demo data, messages in two languages and tests; wrote the React web app (pages in section 7); a Postman collection; and
`docs/Kaaj_Member3_Build_Guide.pdf` (start the project in about 15 minutes, or build it from scratch step by step, with screenshots, demo script,
troubleshooting and settings).

**Request 2 — implement everything in `studentapi` and delete what's not needed.**
Removed the Assignment 03 code (`com.example.studentapi`, its Postman collection, `HELP.md`, `target/`, old uploads; still in Git history), kept
`.env`, `.mvn`, `mvnw*`, `.vscode`, `Claude outputs/`; added the admin settings to `.env`; copied in the whole project.

**Request 3 — the Kotlin version, single member, same backend; milestone files list.**
Choices you made: Milestone 2 first then Final, Jetpack Compose, an `android/` folder. Built the whole Android app (section 8) in two steps
(Milestone 2 scope, then the job seeker side and applicants for the Final). Extended the backend for it: `seeker` and `application` modules,
V150 and V160, job search and saved jobs, `CurrentUser.idIfSignedIn()`, new security rules, more messages, more demo data (Sumi, Jamal, two more
jobs, three applications), more tests and Postman folder 5. Wrote `android/README.md`, updated the root `README.md`, and created the `Doc/` folder:
`AI_Declaration.md`, `AI_Prompts.md`, `Submission_Checklist.md` (every file per milestone, deadlines, video outlines, viva topics),
`source-documents/` and `diagrams/` (the Milestone 1 diagrams plus the schema as built).

**Request 4 — guides: one backend for two apps; GitHub pushes per milestone; a business model.**
`Doc/Guides/01_One_Backend_Two_Apps` (how both apps use the backend, step-by-step running incl. real phones and a Cloudflare quick tunnel,
13 backend differences, 26 problems with fixes, a checklist for new features, later options), `02_GitHub_Milestone_Pushes` (PowerShell: a new
`Kaaj-The-Job` repo so Assignment 03 stays untouched, first push in five commits, `milestone-2` and `final` tags, releases with the APK, fixes),
`03_Business_Model` (customers and which app each uses, value propositions, Business Model Canvas, prices, payments on the web only because of
Google Play's billing rules, unit economics, year-one scenario, costs of two apps, go-to-market, KPIs, risks, roadmap, sources). Each as Markdown + PDF.

**Request 5 — this file.** Wrote `Doc/PROJECT_CONTEXT.md`; saved the remaining generated files into the project (web screenshots and the
generator and check scripts, section 11); added the last two prompts to `Doc/AI_Prompts.md`; updated `Doc/AI_Declaration.md`, `Doc/README.md` and `README.md`.

**Request 6 — "So the Mobile app is completed?"** Answered: every planned screen is written, but the app has never been compiled or run.

**Request 7 — a PDF to run everything.** `Doc/Guides/04_Run_Backend_Web_Android` (.md + .pdf): install, database, `.env`, backend, web app,
Android on the emulator and a real phone, demo accounts, fixes.

**Request 8 — "Have you saved everything in that directory?"** Checked the folder against the AI's workspace file by file and saved the rest.

**Request 9 — complete Member-3's remaining web features; a guide to which files to push per module (web) and per milestone (Android).**
Module 2, scam screening and moderation: `ScamPatterns` and `JobRuleChecks` (Bangla/Banglish/English rules), the optional AI check (Gemini, then
Groq; free keys, optional), the `trust` module (screening of every post, reports, the moderation queue, decisions with notes and audit),
suspending an employer (`UserAdmin`, `SuspendedUserFilter`), V240 and demo data (`quickjobs@demo.kaaj`), the web moderation page, report form
and "why is my post in review" notice. Module 3, the AI job-post assistant: `JobDrafts`, `SentenceFacts`, `DraftTemplate`, `OccupationFinder`,
`POST /employer/jobs/drafts` and the "Write it for me" panel. Tests for both, 14 new screenshots, the Android status texts. For the group
repository: job search moved into `JobSearchController`, the Module 3 flow test into `JobDraftFlowTests`, `R__demo_trust.sql` made independent of
the seeker demo data; then `Doc/Guides/05_GitHub_Files_Per_Module` (.md + .pdf) with exact file lists and `Copy-Push.ps1` in
`Doc/Guides/github-files/`. The request was sent twice; it was done once.

**Request 10 — "So my mobile version is complete?" and a `Generated Documents` folder.** Answered: every planned screen is written;
what is left is the first compile and the fixes it needs, testing, and the course deliverables (section 12.1). Created `Generated Documents/`
(git-ignored copies of every PDF and Markdown document, in five folders with a `README.md`) and redrew `Doc/diagrams/Kaaj_Schema_As_Built.*`
with the two Module 2 tables (15 tables, V1–V240).

**Request 11 — "Make sure you have followed MVC architecture for the WebApp".** Checked every controller, service, page and component.
Fixed five places that broke the layers: sign-up rules moved from `AuthController` into a new `AuthService`; search and saved jobs from
`JobSearchController` into a new `JobSearchService`; `PublicJobPage`, `ReportButton` and `TaxonomyPicker` now use hooks (`usePublicJob`,
`useReportPost`, `useTaxonomySearch`) instead of calling the server. Added `MvcLayersTest` and `web/src/architecture.test.ts`, updated the
group-push lists (guide 05: 161, 57 and 22 files), and wrote `Doc/Guides/06_MVC_Architecture` (.md + .pdf).

**Request 12 — upload the whole folder to `SpringBoot_PostgreSQL_Api`, update the README, and a guide for pushing the two apps separately.**
Rewrote the root `README.md` for the full-project repository (folders, where each app is submitted, the guides, the repository's history);
committed everything in `studentapi` on branch `kaaj-the-job` in five commits (Claude has no access to the GitHub account, so you push it:
guide 07, Part 0); wrote `Doc/Guides/07_Push_Both_Apps_Separately` (.md + .pdf) and the Android repository's README template.

### Main decisions and why

| Decision | Why |
|---|---|
| One Spring Boot backend for both apps, session cookie + CSRF for both | One security chain, no token code; OkHttp handles cookies fine |
| Spring Modulith modules with `internal` packages | Three members can work in parallel without tangling code |
| Flyway owns the schema (`ddl-auto=none`); feature ranges V130 (employer), V140 (job); V150 (seeker) and V160 (application) for the Android build | Teammates never pick the same version number |
| Latitude/longitude + Haversine in SQL, no PostGIS | Runs on any local PostgreSQL |
| `pg_trgm` similarity for search | Bangla, Banglish and English, tolerant of spelling |
| Rule-based match score | Works without AI keys; the AI version (M3-1) can replace it |
| Stand-ins: simple sign-up | Features work on their own before teammates' parts are merged |
| Scam screening: rules first, AI second and optional | The rules catch the common Bangladeshi scams (fees, deposits, WhatsApp-only, overseas without a licence) for free and explainably; the AI only adds a review flag, never publishes on its own |
| Assistant: AI or templates, and the same rule checks as screening | Works without keys or internet; a draft that asks for money can't be used, so screening and the assistant never disagree |
| Group pushes keep earlier versions of shared files | Each module's push builds on its own and its pull request shows only that module's changes |
| Android: Compose, no DI library, Retrofit + kotlinx-serialization, osmdroid, `PdfRenderer` | Simple to explain in a viva; free; no Google Maps key |
| Library versions picked from your own Gradle cache | Fewer download problems on the first sync |
| Android in `android/` inside the same repo | One repository for the Kotlin course, with the backend it needs |
| Payments on the web only (business model) | Google Play requires its billing for in-app digital purchases but allows consumption-only apps |

## 11. Index of documents and generated files

| Path | What |
|---|---|
| `README.md` | Overview, how to run, tests, module tree, API table |
| `android/README.md` | Running the Android app (emulator, real phone, tests) |
| `docs/Kaaj_Member3_Build_Guide.pdf` | First guide (request 1): start or build the project step by step |
| `docs/plans/M1-3_M1-4_Member3.md` | Decisions for Member-3's two features |
| `postman/Kaaj_Member3.postman_collection.json` | 53 API requests in 5 folders |
| `Doc/README.md` | Index of the `Doc/` folder |
| `Doc/PROJECT_CONTEXT.md` | **This file** |
| `Doc/Submission_Checklist.md` | Files per milestone, deadlines, video outlines, viva topics |
| `Doc/AI_Declaration.md`, `Doc/AI_Prompts.md` | AI declaration (fill in and sign) and every prompt |
| `Doc/Guides/01_One_Backend_Two_Apps.md` + `.pdf` | One backend for both apps |
| `Doc/Guides/02_GitHub_Milestone_Pushes.md` + `.pdf` | GitHub steps per milestone |
| `Doc/Guides/03_Business_Model.md` + `.pdf` | Business model |
| `Doc/Guides/04_Run_Backend_Web_Android.md` + `.pdf` | Step by step: run the backend, the web app and the Android app |
| `Doc/Guides/05_GitHub_Files_Per_Module.md` + `.pdf` | Which files go to GitHub per module (web, CSE471 group repo) and per milestone (Android), with the commands |
| `Doc/Guides/github-files/` | The file lists for guide 05, earlier versions of shared files, and `Copy-Push.ps1` |
| `Doc/Guides/07_Push_Both_Apps_Separately.md` + `.pdf` | The three repositories: the full project in `SpringBoot_PostgreSQL_Api`, the Android app per milestone, the web app per module; copy commands, file table, Git commands |
| `Doc/Guides/github-files/android/README_for_the_Android_repository.md` | The README to use in the Android course repository |
| `Doc/Guides/06_MVC_Architecture.md` + `.pdf` | How the web app follows MVC: every file's layer, each module layer by layer, a click traced through the layers, the tests that keep it, viva answers |
| `Doc/source-documents/` | Requirements, context file, proposal, module build prompts |
| `Doc/diagrams/` | Milestone 1 ERD and Android wireframe (`.drawio`, one-page PDF, PNG); schema as built (PDF, PNG, SVG) + `draw_schema.py` |
| `Doc/screenshots/web/` | 33 screenshots of the web app: 01–19 Module 1 (sign-in, dashboard, profile, my jobs, the 4-step form, job page, admin queue, public page, English UI, phone-width layout); 20–33 Modules 2 and 3 (the assistant empty, drafted, filled form, blocked; screening notice; report form; moderation queue, highlights, note required, suspend, English) |
| `Doc/generator-scripts/` | The scripts the AI used to make the guides, the Android strings, the screenshots and the checks (see its `README.md`) |
| `Claude outputs/` | Assignment 03 report (git-ignored) |
| `Generated Documents/` | Copies of all the PDFs and Markdown documents above, sorted into five folders with a `README.md`; for reading only (git-ignored). Edit the originals in `Doc/` and `docs/` |

## 12. What is still left

### 12.1 Before Milestone 2 (Kotlin course, Sat 5 Dec 2026, 11:59 pm)

1. Follow `Doc/Guides/04_Run_Backend_Web_Android.pdf`: run `.\mvnw.cmd test` and fix anything that fails (first real compile of the backend,
   including today's `trust` module and the assistant). Optional: put a free `GEMINI_API_KEY` in `.env` to see "Drafted by AI".
2. Open `android/` in Android Studio, sync, run `.\gradlew.bat test`, run the app on the emulator; fix compile errors (first real compile of the app).
3. Click through the demo with the real backend on both apps (web and Android).
4. Fill in and sign `Doc/AI_Declaration.md` (section 2 "Other AI tools", the checking sentence in section 3, section 5).
5. Push the full project to `SpringBoot_PostgreSQL_Api` and merge it (`Doc/Guides/07_Push_Both_Apps_Separately.md`, Part 0); then make the
   `Kaaj-The-Job-Android` copy and push Milestone 2 (guide 07, Part A).
6. Record the 5–8 minute video (outline in `Doc/Submission_Checklist.md`), add the link to `README.md`, tag `milestone-2`.

### 12.2 Before the Final (Sat 12 Dec 2026, 11:59 pm)

1. **Report** on the course template: the template was never attached; get it from the course page or the instructor (`Doc/Report.pdf` + `.docx`).
2. Test on a real phone (`adb reverse` or same Wi-Fi).
3. Give every enum field in `Models.kt` a default or an `UNKNOWN` entry (`ApplicationStatus` in `MyApplication`/`Applicant`, `VerificationStatus`
   in `AdminEmployer`, the `allowedActions` list) so a new server value can't break the app.
4. Optional: keep sessions across backend restarts (Spring Session JDBC), response compression (guide 01, sections 4.10 and 7.1).
5. Update `Doc/AI_Prompts.md` with any new prompts, record the 8–10 minute video, tag `final`.

### 12.3 What is left for the **web app** (CSE471 group version)

**A. Member-3's own work (Ahsan): all three modules are built**

| Item | State |
|---|---|
| Module 1: employer profile and verification; post and manage jobs | Done |
| Module 2: scam screening and moderation | **Done** (18 Nov 2026): rules + optional AI check, reports, moderation queue, remove / suspend / dismiss, employer notice. Still to do: run `TrustFlowTests` and try it against the real backend |
| Module 3: AI job-post assistant | **Done** (18 Nov 2026): sentence → two-language draft (AI or templates), questions, red flags, "Use this draft". Still to do: run `JobDraftFlowTests`, try it with a Gemini key |
| Pushing to the group repository | Ready: `Doc/Guides/05_GitHub_Files_Per_Module.md`, Part W (one branch and pull request per module) |
| Finish tasks (context file §22) | Deployment on a free server, the demo script and a backup video |

Account administration (suspend, reactivate or change a user's role from an admin page) is **Member-1's** in the final CSE471 plan.
The backend already has `UserAdmin.suspend`/`reactivate` with the audit log (used by moderation); a general admin page and role changes are not built.

**B. Pages for backend features that exist but have no web screens yet** (the backend and the Android app have them):

| Web page to build | Backend endpoints already there |
|---|---|
| Job seeker profile with completeness meter (replace the `/seeker` placeholder) | `GET/PUT /api/v1/me/profile` |
| Find jobs (keyword, distance, wage, type, shift, sort) | `GET /api/v1/jobs?...` |
| Saved jobs | `GET/PUT/DELETE /api/v1/me/saved-jobs` |
| Apply from the job page; My applications with the timeline; withdraw | `POST /api/v1/jobs/{id}/applications`, `GET /api/v1/me/applications[/{id}]`, `POST .../withdraw` |
| Employer: applicants for a job, ranked, with reasons and gaps; open one; shortlist / interview / hire / decline | `GET /api/v1/employer/jobs/{id}/applicants`, `GET /api/v1/employer/applications/{id}`, `POST .../status` |

In the CSE471 plan these features belong to teammates: the job seeker profile and apply/track to **Member-2**, job search and ranked
applicants to **Member-1**. The backend modules for them (`seeker`, `application`, job search, V150, V160) were written for the single-member
Android course. Agree with your teammates (and check with the CSE471 faculty) whether the group uses, adapts or replaces them. Each member is graded
on their own features, and only one version of each table can exist in the group's database.

**C. Teammates' features not built anywhere yet**

| Owner | Feature |
|---|---|
| Member-1 | Google sign-in (shared setup); M1-1 skill certification and document verification (AI reads licences and certificates); M3-1 AI job matching with embeddings and reasons (today's match score is rule-based) |
| Member-2 | Email sign-up with 6-digit codes (shared setup; the `email_code` table exists); M3-3 AI voice profile; M3-4 messages and notifications (chat after shortlisting, in-app notifications) |

When Google sign-in and email codes are merged, set `kaaj.auth.simple-sign-up=false`; the session and `CurrentUser` stay the same.

**D. Whole-web-app work**

- Run the web app against the real backend (so far only against the mock API).
- Merge into the group repository with a branch and a pull request per module (`Doc/Guides/05_GitHub_Files_Per_Module.md`, Part W); CI on GitHub Actions running `mvnw test` and `npm test`.
- Bangla copy review and an accessibility check (keyboard use, contrast, labels).
- If the group hosts the web app on a different address from the API, add CORS and cross-site cookies (guide 01, section 4.3); serving it from the jar avoids this.
- Deeper page addresses than four parts need another pattern in `SpaForwardController`.

### 12.4 Known risks

- The backend and the Android app have **not been compiled** yet (section 9).
- Sessions live in memory: a backend restart signs everyone out on both apps.
- Old Android versions break if the API renames or removes fields (guide 01, section 4.7).
- The Kotlin course report template is still missing.
- Free AI quotas and model names change: if Google retires `gemini-2.5-flash`, set `GEMINI_MODEL` in `.env`; without any key everything still
  works with templates and rules.
- The Android app has no moderation screen: a post held for review waits until an admin decides on the web.

## 13. Continuing the work with an AI assistant

Paste this at the start of a new chat, with this file and the context file attached:

```text
I'm Ahsan Habib (22201027). My project is Kaaj – The Job in C:\Users\UseR\Desktop\Projects\studentapi:
a Spring Boot 4.1 backend (Java 25, Spring Modulith, PostgreSQL, port 1027) shared by a React + TypeScript
web app (web/, CSE471 group project, I'm Member-3) and a Kotlin + Jetpack Compose Android app (android/,
my single-member Kotlin course project). Doc/PROJECT_CONTEXT.md describes everything built so far and
what is left; Doc/source-documents/Kaaj_The_Job_Project_Context_1.md has the full design.
Rules: everything free; never commit .env; keep module boundaries (other modules use only top-level types);
new Flyway migrations only, never edit old ones; every message key in both messages files; only add API
fields, never rename them (old Android apps must keep working).
Task: <what you want next, for example "add an admin page to reactivate suspended employers">.
First read the files you need, then show me a short plan before writing code.
```
