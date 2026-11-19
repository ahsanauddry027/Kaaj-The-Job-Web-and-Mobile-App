# Kaaj – The Job · Web app: what Member-3 has built

**From:** Ahsan Habib (22201027), Member-3 · **To:** Md. Imran Ali (Member-1), Shafin Ahmed (Member-2) · CSE471, Group 08, Lab Section 01
**Date:** 19 November 2026 · Web app only

---

## TL;DR

- All **four of my features** are built in the web app: Module 1 *Employer profile and verification* (M1-3) and *Post and manage
  jobs* (M1-4), Module 2 *Scam screening and moderation* (M2-4), and Module 3 *AI job-post assistant* (M3-4).
- I also built the **shared setup** the rest of the app stands on: the Spring Boot project, roles and session sign-in (a temporary
  email + password sign-up for now), CSRF, the first admin account, the audit log, file upload, the occupation/skill search,
  Bangla/English messages, and the web app's layout, components and translations. You can build your features on top of it.
- The exact versions of everything (Java, Spring Boot, Maven, Node, TypeScript, React, Vite, PostgreSQL…) are in section 2.
- My code will arrive in our group repository as **three pull requests**, one per module (section 7). It contains **none** of your
  features: there are clearly marked places where yours plug in (section 4).
- Still to do on my side: run the full backend test suite and click through everything on the real backend before I open the pull
  requests (section 8).

---

## 1. My four features

### M1-3 · Employer profile and verification (Module 1)

**What it does:** an employer creates a business profile (name, type, address with a **map pin**, contact person, phone, email) and
uploads a **trade licence or national ID** (photo or PDF). It goes to the admin's verification queue. The admin opens the document
and **approves**, or **rejects with a reason**; a rejected employer fixes it and sends it again. Verified employers get a
**Verified** badge on every job post. Changing the business name or the document after approval sends it back for review.

| | |
|---|---|
| Pages | `/employer/profile` (form, map, upload, status panel), `/admin/verification` (queue with the document preview) |
| API | `GET/PUT /api/v1/employer/profile` · `POST /api/v1/employer/profile/document` · `POST /api/v1/employer/profile/submit` · `GET /api/v1/admin/employers?status=`, `/admin/employers/counts`, `/admin/employers/{id}` · `POST /api/v1/admin/employers/{id}/verification` (`APPROVE` / `REJECT` + reason) |
| Backend | module `employer/` (`Employer`, `EmployerProfileService`, `EmployerVerificationService`, the two controllers) |
| Table | `employer` (`V130__employer.sql`) |
| States | `UNVERIFIED → PENDING → VERIFIED` or `REJECTED` |

### M1-4 · Post and manage jobs (Module 1)

**What it does:** a 4-step form (details → wage and hours → location → requirements) with the **wage range required**, Bangla
digits accepted, and a live preview card. Employers save a draft or publish, then **edit, pause, resume, close or repost**. A post
**closes automatically 30 days** after it is first published (checked every hour). An unverified employer's post waits as a
draft and is submitted by itself once the admin verifies the business. A dashboard shows the posts per status.

| | |
|---|---|
| Pages | `/employer` (dashboard), `/employer/jobs` (My jobs: filters, search, actions menu), `/employer/jobs/new`, `/employer/jobs/:id/edit` (the form), `/employer/jobs/:id` (one post), `/jobs/:id` (the public page anyone can open) |
| API | `GET/POST /api/v1/employer/jobs` (`?status=&q=&cursor=`, `?submit=true`, `Idempotency-Key` header) · `GET /api/v1/employer/jobs/summary` · `GET/PUT/PATCH/DELETE /api/v1/employer/jobs/{id}` (`SUBMIT`, `PAUSE`, `RESUME`, `CLOSE`, `REPOST`) · `GET /api/v1/jobs/{id}` (public) |
| Backend | module `job/` (`Job`, `JobStatus`, `JobRules`, `JobService`, `JobExpiryScheduler`, `EmployerJobController`, `PublicJobController`) |
| Table | `job` (`V140__job.sql`) |
| States | `DRAFT → IN_REVIEW → PUBLISHED` or `BLOCKED`; `PUBLISHED ↔ PAUSED`; `→ CLOSED`; repost copies a closed post into a new draft |

### M2-4 · Scam screening and moderation (Module 2)

**What it does:** every submitted post is checked **before it goes live**, in Bangla, Banglish and English:

| The post… | Result |
|---|---|
| asks job seekers for money (registration/processing fee, deposit, "জামানত", bKash before joining…) | **Blocked**, with the reason shown to the employer |
| has phone numbers, "WhatsApp only", easy-money promises, or an overseas job without a recruiting licence number | **In review** until an admin decides |
| leaves out the wage | Can't be submitted at all (the form and the server both require it) |
| passes the rules | Published at once; an **optional free AI check** (Gemini or Groq) can still send it to review |

Signed-in users can **report** a post (asked for money, fake job, harassment, other); **three reports** take it out of view until
an admin decides. Admins work a **moderation queue** with the suspicious words **highlighted** in the post, the rule findings, the
AI's opinion and the reports, then **remove the post**, **suspend the employer** (both need a note; suspension signs the
employer out at their next click) or **dismiss**. Every decision goes to the audit log. The employer sees **why** their post is
in review or blocked.

| | |
|---|---|
| Pages | `/admin/cases` (moderation queue), the report form on `/jobs/:id`, the "why is my post in review" notice on `/employer/jobs/:id` |
| API | `POST /api/v1/reports` · `GET /api/v1/admin/cases?status=OPEN\|REMOVED\|DISMISSED`, `/admin/cases/counts`, `/admin/cases/{id}` · `POST /api/v1/admin/cases/{id}/decision` (`REMOVE_POST`, `SUSPEND_EMPLOYER`, `DISMISS`) · `GET /api/v1/employer/jobs/{id}/screening` |
| Backend | module `trust/` (`Screening`, `ScamReviewer`, `TrustService`, `CaseStore`); `job/JobRuleChecks`, `shared/ScamPatterns`; suspension in `identity/` (`UserAdmin`, `SuspendedUserFilter`) |
| Tables | `trust_case`, `report` (`V240__trust.sql`) |

### M3-4 · AI job-post assistant (Module 3)

**What it does:** above the job form, **"Write it for me"**: the employer types one sentence, for example
`2 jon pickup driver lagbe, Tongi, beton 16-20 hajar, sokal 8ta theke sondha 6ta`. Kaaj drafts the **title and description in
Bangla and English**, fills in openings (2), wage (Tk 16,000–20,000 a month), hours (08:00–18:00) and place (Tongi), asks about
what is missing (overtime for a 10-hour day, weekly holiday, licence type, duties) and runs the **same scam rules** as screening:
a sentence asking for a fee is blocked. **"Use this draft"** fills the form; nothing is saved until the employer publishes.
It works **without any AI key** (it reads the sentence itself and uses templates); with a free Gemini or Groq key it uses AI.
Limit: 20 drafts per employer per day. Phone numbers in the sentence are removed before anything is sent to an AI.

| | |
|---|---|
| Page | the assistant panel on `/employer/jobs/new` |
| API | `POST /api/v1/employer/jobs/drafts` `{ "text": "..." }` |
| Backend | `job/internal/JobDrafts`, `SentenceFacts`, `DraftTemplate`, `OccupationFinder`, `JobDraftController` |
| Tables | none |

---

## 2. Tech stack and exact versions

Everything below is free and open source. The numbers are the ones the project actually uses: `pom.xml`, the Maven wrapper
(`.mvn/wrapper/maven-wrapper.properties`), `web/package-lock.json`, and the versions Spring Boot 4.1.2 manages for us. **Please use
the same ones**, so we don't get "works on my PC" problems.

### What to install

| Tool | Version | Notes |
|---|---|---|
| **JDK** | **25** (Eclipse Temurin recommended) | `java -version` must say 25 |
| **Maven** | **3.10.0**, through the Maven Wrapper **3.3.4** | Don't install Maven: `.\mvnw.cmd` downloads the right one |
| **Node.js** | **24 LTS** (npm comes with it) | `-Pweb` builds use Node **v24.11.0** (frontend-maven-plugin **2.0.2**) |
| **PostgreSQL** + **pgAdmin 4** | **14 or newer** (tested on 16; 18 for a new install) | Extension `pg_trgm` (created by `V1`); the end-to-end tests use the `postgres:18` Docker image |
| **Docker Desktop** | any recent | Optional: only for the end-to-end tests (Testcontainers) |
| **Git** | any recent | |

### Backend

| Part | Version |
|---|---|
| Java | **25** |
| Spring Boot | **4.1.2** |
| Spring Framework | 7.0.10 |
| Spring Security | 7.1.2 (session + `csrf.spa()`) |
| Spring Data (release train) | 2026.0.2 |
| Hibernate ORM / Hibernate Validator | 7.4.13.Final / 9.1.5.Final |
| Jackson | 3.1.7 (`tools.jackson`) |
| Flyway | 12.4.0 (+ `flyway-database-postgresql`) |
| PostgreSQL JDBC driver | 42.7.14 |
| HikariCP / embedded Tomcat | 7.0.2 / 11.0.27 |
| Spring Modulith | **2.1.2** |
| springdoc-openapi (Swagger UI) | **3.2.0** |
| Apache Tika (file type checks) | **3.3.3** |
| Tests | JUnit Jupiter 6.0.3 · AssertJ 3.27.7 · Mockito 5.23.0 · Testcontainers 2.0.5 |

The versions without bold are managed by Spring Boot 4.1.2; we don't set them ourselves, so upgrading Spring Boot upgrades them.

### Web app (`web/`)

| Part | Version |
|---|---|
| TypeScript | **7.0.2** |
| React / React DOM | **19.3.1** |
| Vite (+ `@vitejs/plugin-react`) | **8.3.8** (6.3.0) |
| React Router | 8.5.1 |
| TanStack Query | 5.108.0 |
| react-hook-form (+ `@hookform/resolvers`) / Zod | 7.92.0 (7.0.7) / 4.6.7 |
| i18next / react-i18next | 27.1.5 / 18.0.2 |
| Tailwind CSS (+ `@tailwindcss/vite`) | **4.3.5** |
| Leaflet (OpenStreetMap maps) | 1.9.4 |
| lucide-react (icons) | 1.74.0 |
| Noto Sans Bengali font (`@fontsource/noto-sans-bengali`) | 5.3.0 |
| Tests and tools | Vitest 5.1.0 · jsdom 31.0.1 · Testing Library React 16.3.3 · jest-dom 7.0.1 · user-event 14.6.12 · oxlint 1.94.0 |

Use **`npm ci`** (not `npm install`) so you get exactly the versions in `package-lock.json`.

### AI (optional, free tiers)

| Provider | Model | Setting in `.env` |
|---|---|---|
| Google Gemini (OpenAI-compatible endpoint) | `gemini-2.5-flash` | `GEMINI_API_KEY`, `GEMINI_MODEL` |
| Groq (OpenAI-compatible endpoint) | `openai/gpt-oss-120b` | `GROQ_API_KEY` |

Without any key the app still works: the assistant uses templates and screening uses its rules.

---

## 3. Shared pieces you can use

**Backend** (Spring Boot 4.1, Java 25, Spring Modulith). Use other modules **only through their top-level types**, never their
`internal` package; `ModularityTests` fails the build otherwise.

| Need | Use | Where |
|---|---|---|
| The signed-in user | `CurrentUser.id()`, `idIfSignedIn()`, `has(Role)` | `identity/` |
| Users' email, role, active status | `UserDirectory` | `identity/` |
| Suspend / reactivate an account (with audit) | `UserAdmin.suspend(...)`, `reactivate(...)` | `identity/` |
| Write to the audit log | `AuditLog` | `identity/` |
| An employer's name and Verified status | `EmployerDirectory`, `EmployerSummary` | `employer/` |
| A job's summary (title, wage, place, status) | `JobDirectory`, `JobSummary` | `job/` |
| Store an uploaded file (type and size checked) and decide who may download it | `FileStore`, `FileKind`, your own `FileAccessPolicy` | `shared/` |
| Occupation and skill search (Bangla, Banglish, English, typo-tolerant) | `Taxonomy`; `GET /api/v1/taxonomy/occupations?q=`, `/skills?q=` | `shared/` |
| Call a free AI (Gemini first, then Groq; skipped without a key) | `AiText.json(system, user)` | `shared/` |
| Scam phrases in three languages | `ScamPatterns` | `shared/` |
| Errors in Bangla/English (ProblemDetail with a `code`) | `KaajException.notFound/badRequest/conflict/forbidden/tooManyRequests/invalid(...)` + keys in **both** `messages.properties` and `messages_bn.properties` | `shared/` |
| Wages compared fairly (day vs month), cursor paging, UUIDv7 | `Wages`, `WagePeriod`, `Cursor`, `CursorPage`, `UuidV7` | `shared/` |
| Events you can listen to | `EmployerVerified`, `EmployerRejected`, `JobSubmitted`, `JobPublished`, `JobRemovedAsScam` | `employer/`, `job/`, `trust/` |

**Web** (React 19, TypeScript, Vite, TanStack Query, react-hook-form + Zod, i18next, Tailwind 4).

| Need | Use |
|---|---|
| Calling the API (session cookie, CSRF token, Bangla/English errors) | `lib/api.ts`, through a hook in your feature's `api.ts` |
| Page layout and navigation by role | `components/AppLayout.tsx` (add your links there), `features/auth/RequireRole.tsx` |
| Buttons, cards, inputs, alerts, empty states, badges | `components/ui.tsx`, `StatusBadge.tsx`, `Toast.tsx` |
| Map pin (Leaflet + OpenStreetMap) | `components/MapPicker.tsx` |
| Occupation / skill picker | `components/TaxonomyPicker.tsx` |
| Photo or document upload with checks | `components/FileUpload.tsx` |
| Bangla digits, taka, lakh grouping | `lib/format.ts` |
| Texts | `i18n/bn.json` (default) and `i18n/en.json`: **the same keys in both** (`i18n.test.ts` checks it) |
| Report button (reusable for chat messages) | `components/ReportButton.tsx` |

---

## 4. Where your features plug in

| Feature | Owner | What is already there | What to do |
|---|---|---|---|
| Sign-up and login (Google, email code); account admin | Member-1 (M1-1) | A **temporary** email + password sign-up (`POST /api/v1/auth/register`), session login/logout, `/api/v1/me`, roles, the first admin from `.env`; `email_code` table (V1); suspended users are refused at sign-in and signed out (`SuspendedUserFilter`); `UserAdmin.suspend/reactivate` | Add Google sign-in and email codes; set `kaaj.auth.simple-sign-up=false`. The admin page for suspend / reactivate / change role can call `UserAdmin` (reactivate and role change have no page yet) |
| Job seeker profile | Member-2 (M1-2) | A placeholder page at `/seeker`; `TaxonomyPicker`, `MapPicker`, `FileUpload`, the taxonomy API | Replace the placeholder; your tables in `V120`–`V129` |
| Job search and filters; saved jobs | Member-1 (M2-1) | The public post page `/jobs/:id` and `GET /api/v1/jobs/{id}`; `JobDirectory`; posts carry latitude/longitude and a monthly wage (`wage_monthly_max`) for filtering | Link your results to `/jobs/{id}`. Only show `PUBLISHED` posts: screening and reports hold or block the others |
| Applicant review | Member-1 (M2-2) | My jobs shows "Applicants: —" (*"Applicants appear here once applying is built"*) | Fill in the count and link to your applicants page from `/employer/jobs/:id` |
| One-click apply and tracking | Member-2 (M2-3) | `JobRemovedAsScam(jobId, employerId)` is published when an admin removes a scam post | Listen to it and warn the people who applied |
| AI job matching | Member-1 (M3-1) | `AiText` (free Gemini/Groq client); job fields and occupations | Reuse `AiText`, or add your own provider. Note: the database is plain PostgreSQL 16+ today (no pgvector or PostGIS; distance is Haversine in SQL). If you need pgvector, add the extension in your migration and tell us to install it |
| AI voice profile | Member-2 (M3-2) | `AiText`, `FileStore` (a `VOICE` kind up to 3 MB) | Groq's free Whisper can share the same Groq key |
| Messages and notifications | Member-2 (M3-3) | `trust_case` already accepts `MESSAGE` as a subject type; `ReportButton` | Reports on messages can go to the same moderation queue; notify employers about screening results and removals |

---

## 5. Team rules I followed (please keep them)

1. **Modules:** one package per module under `app.kaaj`; other modules use only its top-level types.
2. **MVC:** controllers only read the request and call a service (no repositories or SQL in controllers); pages call the server only
   through hooks in `features/*/api.ts`. Two tests check this: `MvcLayersTest` and `web/src/architecture.test.ts`.
3. **Flyway:** never edit a merged migration; add a new one **in your feature's range**: feature MX-Y uses `VXY0`–`VXY9`.

   | Range | Feature | Owner |
   |---|---|---|
   | `V1`–`V9` | Shared setup (`V1`–`V3` used) | — |
   | `V110`, `V120` | M1-1, M1-2 | Member-1, Member-2 |
   | `V130`, `V140` | M1-3, M1-4 (**used**) | Member-3 |
   | `V210`, `V220`, `V230` | M2-1, M2-2, M2-3 | Member-1, Member-1, Member-2 |
   | `V240` | M2-4 (**used**) | Member-3 |
   | `V310`, `V320`, `V330` | M3-1, M3-2, M3-3 | Member-1, Member-2, Member-2 |

   The `dev` profile allows out-of-order migrations, so we can merge in any order.
4. **Two languages:** every API message key in both `messages*.properties`; every UI text in both `bn.json` and `en.json`.
5. **API style:** everything under `/api/v1`; errors as ProblemDetail with a `code`; lists with cursors; `Idempotency-Key` on
   creates; IDs are UUIDv7.
6. **Secrets:** never commit `.env`. Everything we use is free.

---

## 6. Run it

You need **JDK 25**, **Node.js 24 LTS** and **PostgreSQL 14+ with pgAdmin 4** (section 2). Maven comes with the project (`mvnw`).

**Database, in pgAdmin 4 (once):**

1. Open **pgAdmin 4**, expand **Servers**, click your server (for example **PostgreSQL 14**) and type the `postgres` password you set
   when installing PostgreSQL (tick **Save password**).
2. Right-click **Databases → Create → Database…** → **Database** `kaaj`, **Owner** `postgres`; on the **Definition** tab check
   **Encoding** is `UTF8` (if not: **Template** `template0`, **Encoding** `UTF8`) → **Save**.
3. Don't create tables: the backend creates all of them on its first start. Afterwards you can see them under
   **kaaj → Schemas → public → Tables** (right-click → **Refresh**), and look at rows with right-click → **View/Edit Data → All Rows**.

If pgAdmin can't connect, the PostgreSQL service is stopped: Start menu → **Services** → **postgresql-x64-14** (your version) → **Start**.
If your server isn't on port 5432 (pgAdmin: server → **Properties → Connection → Port**), add `DB_PORT=...` to `.env`.

**Then:**

```powershell
# 1. once: your settings (the same password pgAdmin accepts)
Copy-Item .env.example .env            # set DB_PASSWORD (optional: GEMINI_API_KEY from aistudio.google.com)

# 2. backend with demo data (port 1027; set PORT to change it)
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"

# 3. web app, in a second window
cd web
npm ci                                 # exact versions from package-lock.json
npm run dev                            # http://localhost:5173 (set KAAJ_BACKEND if your backend is not on 1027)
```

API docs: <http://localhost:1027/swagger-ui.html>. Tests: `.\mvnw.cmd test` (the end-to-end tests need Docker Desktop) and
`cd web; npm test`.

**Demo accounts** (password `Kaaj@12345`):

| Account | What to try |
|---|---|
| `karim@demo.kaaj` | Verified employer: post a job, try **Write it for me**, pause/close/repost |
| `tongigarments@demo.kaaj` | Not verified: upload a licence (sample: `postman/sample-trade-licence.png` if you have it), see the post wait as a draft |
| `quickjobs@demo.kaaj` | One post **blocked** by screening, one **in review**; open them to see why |
| `rahim@demo.kaaj`, `sumi@demo.kaaj` | Job seekers (only for reporting a post for now); "Warehouse helper" was already reported by both, so use a new account for the third report |
| `admin@kaaj.local` / `Admin@12345` | **Employer verification** and **Moderation** (from `.env`) |

---

## 7. What arrives in the group repository

Three branches, merged in order, each through a pull request so you can review it:

| Pull request | Branch | Files | Contents |
|---|---|---|---|
| Shared setup + Module 1 | `member3/module-1` | 161 | The Spring Boot project and settings, `identity`, `shared`, `employer`, `job`, migrations V1–V3, V130, V140, Module 1 demo data, the whole web app as of Module 1, tests |
| Module 2 | `member3/module-2` | 57 (32 new, 25 changed) | `trust`, the rule checks, the AI client, suspension, V240, demo data, the moderation page, the report form, the screening notice, tests |
| Module 3 | `member3/module-3` | 22 (10 new, 12 changed) | The assistant (backend and panel), tests |

Each pull request builds on its own (I checked: after each one the web app type-checks, its tests pass and it builds, and the
database migrations run). If you have already pushed your own project setup, tell me before I open the first one so we can merge
the shared files instead of overwriting them.

---

## 8. Status

| Checked | Result |
|---|---|
| Web app | 33 tests pass; type-check, lint and production build pass; every page clicked through in Chrome against a mock of the API (Bangla and English, phone width) |
| Database | All migrations and demo data run on PostgreSQL 16 in order |
| Backend | Compile check of our own classes; the scam rules and the assistant's sentence reading run in plain Java (Karim's sentence gives exactly the values above) |
| **Still to do (me)** | Run the full backend test suite (`mvnw test`, with Docker) and click through on the **real** backend; then open the pull requests |

**Different from the proposal, so no surprises:** no PostGIS/pgvector yet (plain PostgreSQL, distance in SQL); the AI calls go
straight to Gemini's and Groq's OpenAI-compatible APIs instead of through Spring AI; sign-in sessions are kept in memory (a backend
restart signs everyone out); Docker is optional (only for the end-to-end tests and an optional database).

Questions, or something you need from my side (an endpoint, an event, a component)? Message me. — Ahsan
