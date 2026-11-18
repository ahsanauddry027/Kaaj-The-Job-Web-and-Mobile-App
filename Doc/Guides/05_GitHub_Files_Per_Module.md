# Which files to push, module by module (web) and milestone by milestone (Android)

**Kaaj – The Job** · Ahsan Habib (22201027) · GitHub: `ahsanauddry027` · written 18 November 2026

You have **two repositories** to feed from the same `studentapi` folder:

| | **Web app → CSE471 group repository** (Part W) | **Android app → your own `Kaaj-The-Job` repository** (Part A) |
|---|---|---|
| Course | CSE471, Group 08 (you are **Member-3**) | Kotlin/Android course, single member (instructor `rahman9909`) |
| What goes in | Only **your** modules: the shared setup + Module 1, then Module 2, then Module 3 | The **whole project**: backend, `android/`, `Doc/` (web optional) |
| How | Copy one module's files into a clone of the group repository → branch → pull request | Commit straight from `studentapi` (it is already a Git repository) → tag each milestone |
| Unit of work | **Push 1, 2, 3** (one per module) | **Milestone 2** (Sat 5 Dec 2026), **Final** (Sat 12 Dec 2026) |

Everything this guide needs is in **`Doc/Guides/github-files/`**:

```
Doc/Guides/github-files/
├── web/
│   ├── push-1-setup-and-module-1.txt   161 files: shared setup + Module 1
│   ├── push-2-module-2.txt              57 files: Module 2 (32 new, 25 changed)
│   ├── push-3-module-3.txt              22 files: Module 3 (10 new, 12 changed)
│   ├── versions/push-1/, versions/push-2/   earlier versions of shared files (see W2)
│   └── Copy-Push.ps1                    copies one push into your clone of the group repository
└── android/
    ├── 1-app-shell-and-sign-in.txt      67 files
    ├── 2-employer-profile-and-verification.txt   7 files
    ├── 3-post-and-manage-jobs.txt        9 files
    ├── 4-admin-verification-queue.txt    3 files
    └── 5-job-seeker-and-applicants.txt  13 files
```

**How the lists were checked (18 Nov 2026).** The three web pushes were rebuilt one after another in a scratch folder, exactly as
`Copy-Push.ps1` lays them out. After **each** push the web app type-checked, its tests passed (25, then 31, then 33 tests) and it
built; the database migrations and demo data of each push ran on a fresh PostgreSQL 16; and every Java class the code uses was
present (a `javac` check without the Spring libraries). After push 3 every file is identical to today's `studentapi`. The Android
lists cover all 99 files in `android/` exactly once. Not done here: running the Spring tests (`.\mvnw.cmd test`) and the Android
build. `Copy-Push.ps1` itself was not run either (there is no PowerShell in the AI's workspace); a Python copy of its logic was.

> **19 November 2026:** you now push from a separate folder, `Kaaj-Submissions` (guide 07). The file lists and `Copy-Push.ps1` here work
> unchanged from there; guide 07 has the copy commands and the Android repository steps.

> Commands are for **PowerShell** on Windows 11. `C:\Users\UseR\Desktop\Projects\studentapi` is your project folder. Guide 02
> (`02_GitHub_Milestone_Pushes.md`) has the one-time Git setup (name, email, the new `Kaaj-The-Job` repository); do that first.

---

## Part W — Web app → the CSE471 group repository

### W1. What each push contains

| Push | Module (CSE471 plan, Member-3) | What it adds | Files | Branch |
|---|---|---|---|---|
| **1** | **Shared setup + Module 1**: employer profile and verification; post and manage jobs | The Spring Boot project (Maven, settings, sign-in, roles, the admin account, file upload, Bangla/English messages), the `employer` and `job` modules, migrations V1–V3, V130, V140, Module 1 demo data, and the whole React web app as it was after Module 1 | 161 | `member3/module-1` |
| **2** | **Module 2**: scam screening and moderation | Rule checks (Bangla/Banglish/English), optional AI check, the `trust` module (cases, reports, admin decisions), suspending an employer, migration V240, the moderation page, the report form, "why is my post in review" | 57 | `member3/module-2` |
| **3** | **Module 3**: AI job-post assistant | One sentence → a two-language draft (AI or templates), questions and red flags, "Use this draft" fills the form | 22 | `member3/module-3` |

**Not pushed to the group repository** (they stay in your own repository for the Android course):

| Left out | Why |
|---|---|
| `seeker/` and `application/` modules, `V150__seeker.sql`, `V160__application.sql`, `R__demo_seeker.sql`, their tests | Job seeker profile, apply/track and ranked applicants are **Member-1's and Member-2's** features in the CSE471 plan |
| `JobSearch.java`, `SavedJobs.java`, `JobSearchController.java`, `JobSearchService.java`, `JobSearchTest.java` | Job search is **Member-1's**. (Today search was moved out of `PublicJobController` into its own `JobSearchController`, so your job module builds without it. The endpoints did not change, so the Android app is not affected.) |
| `android/`, `Doc/`, `docs/`, `postman/`, `README.md` | The Android course and your own documents. Add your module notes to the group's own README instead. |
| `.env`, `data/`, `target/`, `web/node_modules/`, `web/dist/` | Secrets and generated files: never pushed (push 1's `.gitignore` already ignores them) |

### W2. Why some files are in two pushes

Some shared files were changed again by a later module (for example `application.properties`, `messages.properties`,
`web/src/lib/types.ts`, `web/src/i18n/en.json`, `JobFormPage.tsx`). If push 1 took today's version, it would already contain
Module 2 and 3 code that **does not build** without Module 2 and 3's new files.

So `Copy-Push.ps1` copies, for push 1, the version of each such file **as it was after Module 1** (`versions/push-1/`, 29 files,
taken from your folder before today's update), and for push 2 the version **as it is after Module 2** (`versions/push-2/`,
8 files: today's version minus the Module 3 lines). Push 3 copies today's versions. Result: every push builds on its own,
and each pull request shows only its own module's changes (Module 3's pull request shows exactly the assistant lines being
added to `types.ts`, the two `messages` files, and so on).

Two settings change on the way, on purpose:

| Setting | After push 1 | After push 2 and 3 |
|---|---|---|
| `kaaj.jobs.auto-publish` | `true`: a verified employer's post goes live at once (there is no screening yet) | `false`: scam screening decides every post (honest posts still go live at once) |
| `kaaj.jobs.drafts-per-day` | — | push 3 adds it (20 assistant drafts per employer per day) |

### W3. Module 1 files (push 1, 161 files)

Read the exact list in `push-1-setup-and-module-1.txt`. By area:

| Area | Files |
|---|---|
| Project root (8) | `pom.xml`, `mvnw`, `mvnw.cmd`, `.mvn/wrapper/maven-wrapper.properties`, `.gitignore`, `.gitattributes`, `.env.example`, `compose.yaml` |
| Backend code (73) | `KaajApplication.java`; `shared/` (22: errors, files, taxonomy, cursors, JSON); `identity/` (16: sign-in, roles, admin account, security, audit log); `employer/` (14: profile, document, verification); `job/` (20: posts, statuses, rules, expiry, the employer and public controllers) |
| Resources (10) | `application.properties`, `application-dev.properties`, `messages.properties`, `messages_bn.properties`, `db/migration/V1__core.sql`, `V2__taxonomy.sql`, `V3__seed_taxonomy.sql`, `V130__employer.sql`, `V140__job.sql`, `db/demo/R__demo_member3.sql` |
| Backend tests (13) | `Member3FlowTests`, `ModularityTests`, `MvcLayersTest`, `EmployerTest`, `JobStatusTest`, `JobRulesTest`, `JobTest`, `CursorTest`, `DocumentCodeTest`, `JsonTest`, `UuidV7Test`, `WagesTest`, `DiskFileStoreTest` |
| Web app (57) | `web/` settings (`package.json`, `package-lock.json`, `vite.config.ts`, `tsconfig*.json`, `index.html`, `.oxlintrc.json`, `.gitignore`, `public/favicon.svg`) and `web/src/`: layout, sign-in, the employer profile and verification panel, the 4-step job form, My jobs, job detail, the public job page, the admin verification queue, Bangla/English texts, the `api.ts` hooks, and their tests (including `architecture.test.ts`) |

### W4. Module 2 files (push 2, 57 files) — what each one does

**New (32)**

| File | What it does |
|---|---|
| `shared/ScamPatterns.java` | The scam patterns in Bangla, Banglish and English (fees, deposits, "WhatsApp only", easy money, overseas jobs), with Unicode normalising and Bangla digits |
| `job/JobRuleChecks.java`, `job/Finding.java`, `job/JobContent.java` | The rules on a post's text: asking job seekers for money **blocks**; contact details, easy money, overseas without a licence number → **review**; missing wage, a day over 8 hours → **questions** |
| `shared/AiText.java`, `shared/internal/AiProperties.java`, `shared/internal/OpenAiCompatibleText.java` | The free AI client: Gemini first, then Groq; a provider without a key is skipped; works without any key |
| `trust/package-info.java`, `trust/JobRemovedAsScam.java` | The new `trust` module and its event when an admin removes a post |
| `trust/internal/Screening.java` | Decides every submitted post: publish, hold for review, or block |
| `trust/internal/ScamReviewer.java`, `ScamAssessment.java` | The optional AI check of posts that pass the rules |
| `trust/internal/CaseStore.java`, `TrustService.java`, `TrustControllers.java`, `TrustViews.java`, `TrustProperties.java` | Cases and reports (SQL), the moderation queue, decisions (remove, suspend, dismiss), the endpoints and settings |
| `identity/UserAdmin.java`, `identity/internal/UserAdminService.java`, `UserStatusCache.java`, `SuspendedUserFilter.java` | Suspending an employer: written to the audit log, and their next request signs them out |
| `db/migration/V240__trust.sql`, `db/demo/R__demo_trust.sql` | Tables `trust_case` and `report`; demo employer `quickjobs@demo.kaaj` with a blocked, an in-review and a reported post |
| `TrustFlowTests.java`, `JobRuleChecksTest.java`, `ScamPatternsTest.java`, `trust/internal/ScreeningTest.java` | Tests (the flow test needs Docker Desktop) |
| `web/src/features/admin-trust/ModerationPage.tsx` | The admin's moderation queue: tabs, red flags highlighted in the post, reports, decision with a note |
| `web/src/components/ReportButton.tsx`, `FindingList.tsx`, `web/src/lib/trust.ts` (+ `trust.test.ts`) | The job seeker's report form; the list of rule findings; highlighting and note checks |

**Changed (25):** `.env.example` (AI keys), `AppUser.java` (status can change), `SecurityConfig.java` (adds the suspended-user
filter), `JobModeration.java`, `JobStatus.java`, `Job.java`, `JobEventListeners.java`, `JobService.java`, `job/package-info.java`
(posts can be held, blocked and restored), `JobProperties.java` + `application.properties` (auto-publish off; `kaaj.trust.*`;
`kaaj.ai.*`), `KaajException.java` (403 and 429 errors), `messages*.properties` (report, case and suspension messages),
`JobStatusTest.java`; web: `AppLayout.tsx` (Moderation link), `router.tsx` (`/admin/cases`), `admin-trust/api.ts`,
`employer-jobs/api.ts`, `jobs/api.ts` (`useReportPost`), `JobDetailPage.tsx` (why a post is in review), `PublicJobPage.tsx` (report button, "never pay"),
`types.ts`, `i18n/en.json`, `i18n/bn.json`.

### W5. Module 3 files (push 3, 22 files) — what each one does

| File | What it does |
|---|---|
| `job/internal/JobDrafts.java` | The assistant: checks the sentence (3–500 characters, 20 a day), removes phone numbers, asks the AI or uses the templates, then runs the same rule checks as screening |
| `job/internal/SentenceFacts.java` | Reads the facts from the sentence: openings, wage and period ("16-20 hajar", "15k"), hours ("sokal 8ta theke sondha 6ta"), shift, place |
| `job/internal/DraftTemplate.java` | Writes the Bangla and English title and description when there is no AI |
| `job/internal/OccupationFinder.java`, `shared/TermNames.java` | Finds the occupation and skills in the sentence (every Bangla/English name) |
| `job/internal/JobDraftController.java` | `POST /api/v1/employer/jobs/drafts` |
| `JobDraftsTest.java`, `JobDraftFlowTests.java` | Tests (Karim's sentence; the flow test needs Docker Desktop) |
| `web/src/features/employer-jobs/AssistantPanel.tsx` (+ `assistant.test.tsx`) | "Write it for me": example sentences, the draft in both languages, questions, red flags, "Use this draft" |
| Changed (12) | `Taxonomy.java`, `JdbcTaxonomy.java` (all names per occupation), `JobProperties.java` + `application.properties` (drafts per day), `messages*.properties` (the questions), `JobFormPage.tsx` (shows the panel), `schema.ts` (draft → form), `employer-jobs/api.ts`, `types.ts`, `i18n/en.json`, `i18n/bn.json` |

### W6. Step by step

#### Step 0 — Agree with your team first (once)

Ask in your group chat, and settle these before your first push:

1. The group repository's address (for example `https://github.com/<owner>/<repo>`) and that you are a collaborator on it.
2. Whether the group uses **this project's structure** (one Spring Boot backend + `web/`). If a teammate already pushed their
   own setup, see W8.
3. Whether you merge with **pull requests** (recommended) or push to `main` directly.

#### Step 1 — Clone the group repository next to `studentapi` (once)

```powershell
cd C:\Users\UseR\Desktop\Projects
git clone https://github.com/<owner>/<repo>.git cse471-kaaj
cd cse471-kaaj
git config user.name          # must print ahsanauddry027 (guide 02, Part A step 1)
git branch --show-current     # usually main
```

#### Step 2 — Push 1: shared setup + Module 1

```powershell
cd C:\Users\UseR\Desktop\Projects\cse471-kaaj
git switch main
git pull
git switch -c member3/module-1
powershell -ExecutionPolicy Bypass -File ..\studentapi\Doc\Guides\github-files\web\Copy-Push.ps1 -Push 1 -To .
```

It prints `Push 1: copied 161 files into ... (29 of them as they were at that point).` If it lists missing files, stop and check
that `studentapi` is up to date. `-ExecutionPolicy Bypass` is only for this one run; it does not change your PC's settings.

Check, test, commit and push:

```powershell
git status                                   # only the copied files; no .env, node_modules, target
Copy-Item ..\studentapi\.env .env            # for running it here; .env is git-ignored
notepad .env                                 # change DB_NAME=kaaj to DB_NAME=kaaj_group (see the note below)
.\mvnw.cmd test
cd web; npm ci; npm test; npm run build; cd ..
git add -A
git status --short | Select-String '\.env$'  # must print NOTHING
git commit -m "Member-3 Module 1: employer profile and verification, post and manage jobs (with the shared setup)"
git push -u origin member3/module-1
```

> **Use a separate database for the group copy.** Your `kaaj` database already has the job seeker tables (V150, V160) that the
> group copy does not have, and Flyway stops when the database has migrations it can't find. Create an empty database
> `kaaj_group` once (pgAdmin → Databases → Create, or `psql -U postgres -c "CREATE DATABASE kaaj_group"`) and put
> `DB_NAME=kaaj_group` in the group copy's `.env`.

Then open a pull request: on GitHub, the yellow **Compare & pull request** banner → title "Member-3 Module 1" → describe what it
adds (copy the W1 row) → **Create pull request**. After a teammate approves (or straight away, if your group doesn't review),
click **Merge pull request**. Optional, a tag your faculty can open: after merging,

```powershell
git switch main; git pull
git tag -a member3-module-1 -m "Member-3 Module 1"
git push origin member3-module-1
```

#### Step 3 — Push 2: Module 2 (scam screening and moderation)

Only after push 1 is merged:

```powershell
cd C:\Users\UseR\Desktop\Projects\cse471-kaaj
git switch main
git pull
git switch -c member3/module-2
powershell -ExecutionPolicy Bypass -File ..\studentapi\Doc\Guides\github-files\web\Copy-Push.ps1 -Push 2 -To .
git status                                   # 32 new files (??) and 25 changed (M)
git diff --stat                              # the changed files and how many lines each
.\mvnw.cmd test
cd web; npm test; npm run build; cd ..
git add -A
git status --short | Select-String '\.env$'  # must print NOTHING
git commit -m "Member-3 Module 2: scam screening and moderation (rule checks, optional AI check, reports, admin queue, suspension)"
git push -u origin member3/module-2
```

Pull request "Member-3 Module 2" → merge → optional tag `member3-module-2`.

#### Step 4 — Push 3: Module 3 (AI job-post assistant)

Only after push 2 is merged:

```powershell
git switch main
git pull
git switch -c member3/module-3
powershell -ExecutionPolicy Bypass -File ..\studentapi\Doc\Guides\github-files\web\Copy-Push.ps1 -Push 3 -To .
git status                                   # 10 new, 12 changed
.\mvnw.cmd test
cd web; npm test; npm run build; cd ..
git add -A
git commit -m "Member-3 Module 3: AI job-post assistant (one sentence to a two-language draft, with templates when there is no AI key)"
git push -u origin member3/module-3
```

Pull request "Member-3 Module 3" → merge → optional tag `member3-module-3`.

**If a module is due later**, simply do its step later: the lists don't depend on the date. If you change a file in `studentapi`
after today, copy it into the group clone by hand on a new branch (for example `member3/fix-report-form`) and open a small pull
request.

**No pull requests in your group?** Replace `git switch -c member3/module-N` with nothing (stay on `main`) and use
`git push` instead of `git push -u origin member3/module-N`.

### W7. Showing your work (CSE471 evaluation)

| Module | Show live | Say where it is |
|---|---|---|
| 1 | Business profile → document → admin approves; post a job with the 4-step form; pause, close, repost | `employer/`, `job/`, `JobFormPage.tsx` |
| 2 | Sign in as `quickjobs@demo.kaaj`: one post blocked, one in review (the yellow notice says why). Create a new job seeker account (**Create account → Find a job**), open `http://localhost:5173/jobs/01936a00-0000-7000-8000-0000000000b3` ("Warehouse helper", already reported by rahim and sumi) and report it: the third report takes it out of search. As admin, open **Moderation**: red flags highlighted; try "Remove the post" without a note (error); suspend → that employer is signed out | `trust/`, `JobRuleChecks.java`, `ScamPatterns.java`, `ModerationPage.tsx` |
| 3 | **Post a job** → type `2 jon pickup driver lagbe, Tongi, beton 16-20 hajar, sokal 8ta theke sondha 6ta` → **Draft my post** → both languages + questions → **Use this draft** → the form is filled. Then a sentence with "registration fee" → blocked | `JobDrafts.java`, `SentenceFacts.java`, `AssistantPanel.tsx` |

Without an AI key the assistant says "Written from your sentence (no AI)" and screening uses the rules only. With a free Gemini
key in `.env` (`GEMINI_API_KEY`, from aistudio.google.com) it says "Drafted by AI".

### W8. When the group repository already has files

| Situation | What to do |
|---|---|
| A teammate already pushed **this same project** (same `pom.xml`, `src/main/java/app/kaaj`) | Do steps 2–4 as written. Git shows conflicts only where you both changed the same file (most likely `SecurityConfig.java`, `application.properties`, `messages*.properties`, `router.tsx`, `AppLayout.tsx`, the `i18n` files): keep **both** sides' lines, then `git add <file>` and `git commit`. |
| The group has **its own** Spring Boot project with a different package or layout | Don't copy the setup files (`pom.xml`, `application.properties`, `SecurityConfig.java`, ...). Copy only your module's folders and new files from the lists, change the `package` lines to the group's, and add your settings and messages to the group's files by hand. Ask the teammate who owns the setup to review. |
| The group has **no** repository yet | Create it (guide 02, Part A step 3: name it, for example, `Kaaj-The-Job-CSE471`), add your two teammates as collaborators (Settings → Collaborators), then do steps 1–4. |

### W9. When something goes wrong (web)

| What you see | Fix |
|---|---|
| `running scripts is disabled on this system` | You ran `.\Copy-Push.ps1` directly. Use the `powershell -ExecutionPolicy Bypass -File ...` line exactly as above. |
| `Folder not found` / `is not a Git repository` | Run the script from the group clone with `-To .`, or give the clone's full path. Clone first (step 1). |
| `Copy into your clone of the GROUP repository` | You ran it with `-To` pointing at `studentapi` itself. Point it at `cse471-kaaj`. |
| `N files were not found in studentapi` | `mvnw`, `mvnw.cmd`, `.mvn\` come from your PC's folder; make sure they are there. For any other file, your `studentapi` is not the updated one. |
| `Detected applied migration not resolved locally: 150` | The group copy is using your `kaaj` database. Set `DB_NAME=kaaj_group` in the group copy's `.env` (step 2 note). |
| `! [rejected] ... (fetch first)` on push | Someone pushed to the same branch. `git pull --rebase`, then `git push`. |
| Pull request shows "This branch has conflicts" | `git switch member3/module-N`, `git pull origin main`, fix the files Git marks, `git add`, `git commit`, `git push`. |
| You copied the wrong push | Before committing: `git restore .` and `git clean -fd -n` (shows what would be deleted), then `git clean -fd`. Then copy the right push. |

---

## Part A — Android app → your own `Kaaj-The-Job` repository

Here you commit straight from `studentapi`. The Kotlin course looks at **two tagged snapshots**: `milestone-2` and `final`.
Guide 02 has the full routine (repository, collaborator `rahman9909`, video link, tags); this part says exactly **which files**
go in each and replaces guide 02's single Android commit (Part A step 6, commit 3) with one commit per feature.

### A1. Why the whole `android/` folder goes in at Milestone 2

The app is one Gradle project: `KaajApi.kt`, `Models.kt`, `Repositories.kt`, `Routes.kt` and `KaajRoot.kt` already know every
screen, including the job seeker side. Leaving the seeker screens out would break the build, and only the tagged snapshot is
graded. Milestone 2 needs "registration and login + at least 2–3 features", and **more is allowed**. So: push all of `android/`
before 5 Dec, in five feature commits so your history shows the work step by step, and **show** the Milestone 2 features in the
Milestone 2 video and the job seeker side in the final video.

### A2. Milestone 2 (deadline Sat 5 Dec 2026, 11:59 pm) — which files

| Commit | List file (in `Doc/Guides/github-files/android/`) | Files | What it is |
|---|---|---|---|
| Backend | — (`pom.xml`, `mvnw`, `mvnw.cmd`, `.mvn/`, `src/`, `.gitignore`, `.gitattributes`, `.env.example`, `compose.yaml`) | whole | The Spring Boot backend the app talks to, **including** today's screening and assistant |
| Android 1 | `1-app-shell-and-sign-in.txt` | 67 | Gradle files, manifest, networking (session cookie + CSRF), models, repositories, navigation, theme, Bangla/English strings, settings, **registration and sign-in** |
| Android 2 | `2-employer-profile-and-verification.txt` | 7 | Business profile, map pin, licence/NID photo or PDF, verification status |
| Android 3 | `3-post-and-manage-jobs.txt` | 9 | The 4-step job form, My jobs, job detail, pause/resume/close/repost |
| Android 4 | `4-admin-verification-queue.txt` | 3 | Admin queue: open the document, approve or reject with a reason |
| Android 5 | `5-job-seeker-and-applicants.txt` | 13 | Job seeker profile, search, saved jobs, apply and track; ranked applicants (shown in the **final** video) |
| Docs | `Doc/` (AI declaration **signed**, AI prompts, source documents, guides), `README.md` with the **Milestone 2 video link**, `postman/` | | What the course asks for in `/Doc` |
| Optional | `web/` | | The CSE471 web app; not needed for this course |

Commands (instead of guide 02, Part A step 6, commit 3):

```powershell
cd C:\Users\UseR\Desktop\Projects\studentapi
function Add-List($name) {
  $paths = Get-Content "Doc\Guides\github-files\android\$name.txt" | Where-Object { $_ -and -not $_.StartsWith('#') }
  git add -- $paths
}
Add-List '1-app-shell-and-sign-in';             git commit -m "Android: app shell, networking with session cookie + CSRF, registration and sign-in"
Add-List '2-employer-profile-and-verification'; git commit -m "Android: employer business profile and verification"
Add-List '3-post-and-manage-jobs';              git commit -m "Android: post and manage jobs"
Add-List '4-admin-verification-queue';          git commit -m "Android: admin verification queue"
Add-List '5-job-seeker-and-applicants';         git commit -m "Android: job seeker side and ranked applicants"
git status --short android                       # must print NOTHING (every Android file is committed)
```

(The `function` line only lasts until you close the PowerShell window.) The backend, docs and web commits are the same as in
guide 02, Part A step 6. Then continue with guide 02: push, video link in `README.md`, and tag:

```powershell
git push
git tag -a milestone-2 -m "Milestone 2: registration/login, employer verification, post and manage jobs, admin queue"
git push origin milestone-2
```

Before tagging, check nothing is missing or secret:

```powershell
git ls-files android | Measure-Object                   # 99
git ls-files Doc/AI_Declaration.md Doc/AI_Prompts.md README.md .env.example android/README.md
git ls-files | Select-String '(^|/)\.env$'              # must print NOTHING
git ls-files | Select-String 'local\.properties$'      # must print NOTHING
```

### A3. Final (deadline Sat 12 Dec 2026, 11:59 pm) — which files

The code is already in. The final snapshot adds documents and any fixes:

| Path | What |
|---|---|
| `Doc/Report.pdf` (+ `.docx`) | The report on the course template |
| `Doc/diagrams/` | Wireframe and schema diagrams (Milestone 1 ones + `Kaaj_Schema_As_Built`) |
| `Doc/AI_Prompts.md`, `Doc/AI_Declaration.md` | Every prompt since Milestone 2; signed again |
| `README.md` | The **final video** link (8–10 min) next to the Milestone 2 one |
| Any file you fixed after 5 Dec | For example `android/app/src/main/...` after testing on a real phone |

```powershell
git status
git add -- Doc README.md
git add -- android src                 # only if you changed code after Milestone 2
git commit -m "Final: report, diagrams, updated AI declaration and the final video link"
git push
git tag -a final -m "Final submission: job seeker side, applicants, report and diagrams"
git push origin final
```

### A4. Things to know about the Android app with today's backend

- Posts now go through **scam screening**. An honest post is published at once, as before. A post with a phone number or
  "WhatsApp" in its text waits "in review" (the app shows the reason), and an admin decides on the **web** Moderation page; the
  Android app has no moderation screen. For your Milestone 2 video, write posts without phone numbers.
- The job search endpoints (`/api/v1/jobs`, saved jobs) did not change; they now live in `JobSearchController.java`.

---

## Command summary

| When | Web (group repository) | Android (your repository) |
|---|---|---|
| Once | Clone the group repo next to `studentapi`; `.env` with `DB_NAME=kaaj_group` | Guide 02, Part A |
| Each module / milestone | `git switch main` → `git pull` → `git switch -c member3/module-N` → `Copy-Push.ps1 -Push N -To .` → test → `git add -A` → commit → `git push -u origin member3/module-N` → pull request → merge | `Add-List` per feature → commit → push → `git tag -a milestone-2` / `final` → `git push origin <tag>` |
| Always check | `git status --short \| Select-String '\.env$'` prints nothing | `git ls-files \| Select-String '(^\|/)\.env$'` prints nothing |

### Sources

- GitHub Docs, creating a pull request: <https://docs.github.com/en/pull-requests/collaborating-with-pull-requests/proposing-changes-to-your-work-with-pull-requests/creating-a-pull-request>
- GitHub Docs, inviting collaborators to a personal repository: <https://docs.github.com/en/repositories/managing-your-repositorys-settings-and-features/repository-access-and-collaboration/inviting-collaborators-to-a-personal-repository>
- Microsoft Learn, about execution policies (`-ExecutionPolicy Bypass` for one run): <https://learn.microsoft.com/en-us/powershell/module/microsoft.powershell.core/about/about_execution_policies>
