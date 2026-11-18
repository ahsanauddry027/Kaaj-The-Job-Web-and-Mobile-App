# Kaaj – The Job · Build prompts 2: Module 2

> **Use with:** `Kaaj_The_Job_Project_Context.md`, version 1.5 or later. Give Claude the context file first, then this file.
> **Covers:** Module 2, Search, applications and employer verification: features 4–6 (M2-1, M2-2, M2-3).
> **Before this:** `Kaaj_Module_1_Build_Prompts.md` (shared setup and Module 1) must be finished and merged. **Next file:** `Kaaj_Module_3_Build_Prompts.md`
> **Version:** 1.0 · 4 November 2026

---

## How to use this file

**For the team**

1. Start a new Claude Code session in the `kaaj` repository, or a new chat. Give it the context file first, then this file.
2. Say who you are and which prompt to run, for example: *"I'm Member-3 (Ahsan). Run M2-3-plan."* Every prompt has an ID: `M2-1-plan`, `M2-1-data` and so on for feature M2-1, `FINISH` for any feature, and `M2-WRAP` at the end of the module.
3. Run a feature's prompts in the order listed. The `-plan` prompt writes no code: read the plan, answer its questions, and approve it. Claude saves the approved plan in `docs/plans/`, and the later prompts follow it.
4. After each prompt: read the code, run the commands it gives you, commit, then run the next prompt. When a session gets long, start a new one with both files and say which prompt you're on.
5. In a plain chat instead of Claude Code, paste the files the prompt needs, and copy the files it writes into the repository yourself.
6. You can also paste a prompt's text block as your message. Replace anything in [square brackets].

**For the AI assistant reading this file**

- This file is a set of prompts, not one task. When you receive it, don't start building. Wait until the user says who they are and which prompt to run; if they don't say, ask.
- Run only the prompt the user names. When it's done, stop and give: what you changed, the commands to run, what to check by hand, and the ID of the next prompt.
- The context file is the source of truth. If this file disagrees with it, follow the context file and point out the difference.
- Follow the standing rules below in every prompt, even when a prompt doesn't repeat them.
- Before writing code that depends on another member's work, check whether that code exists in the repository. If it doesn't, say so, and use the stand-in the prompt describes.
- If you start in the middle of a feature, read its plan in `docs/plans/` and the code already written before you continue.

---

## Standing rules for every prompt

1. **Stack and versions:** §12 of the context file. Use the Spring Boot 4 starter names in §12.2; Spring AI 2.0 property keys have no `.options` segment. If you're not sure an API exists in these versions, say so instead of guessing.
2. **Modules:** put code in the module the prompt names (§11). Other modules may use only a module's top-level package; everything else goes in `internal`. Modules talk through public interfaces and events. `ModularityTests` must pass.
3. **Database:** Flyway only. One new file per change, inside the feature's version range (§22); never edit a migration that has been merged. JPA for ordinary tables, `JdbcClient` for vector and spatial SQL.
4. **API:** REST under `/api/v1`, as in §16: `ProblemDetail` errors in the language of `Accept-Language`, an `Idempotency-Key` header on creates, cursor pagination, Bean Validation on every input. After an API change, regenerate the frontend types (`npm run gen:api`).
5. **Security:** the path rules in §17 plus ownership checks in services (§19). Test every role that must be refused, not only the one that's allowed.
6. **Frontend:** pages in `web/src/features/<folder>/` (§18); TanStack Query with the generated client; React Hook Form with Zod; every visible string in `bn.json` and `en.json` (Bangla is the default); Bangla digits and taka through the `lib/` helpers; mobile-first at 360 px; WCAG 2.2 AA.
7. **AI:** the principles in §10.1 and the tips in §23: typed records, Java rules after the model, user text fenced with `PromptText.fence`, no tools in those prompts, a fallback for every AI call, AI clients behind interfaces and mocked in tests. Sample data only with free tiers (§13).
8. **Tests:** unit tests for every rule; integration tests with Testcontainers (the database image from §17) and MockMvc; Vitest for components with logic. Tests never call real AI services, except the `ai-eval` tests, which are run by hand.
9. **Output:** complete files with their paths, no placeholders or TODOs. Explain non-obvious choices in a sentence or two. End with the commands to run and what to check.
10. **Changes to the plan:** if you add an event, endpoint, table, library, setting or decision that isn't in the context file, list it at the end, with the exact lines to add to the context file.
11. **Git:** one branch per feature (for example `feat/m2-3-employer-verification`) and a pull request that another member reviews. Never commit `.env` or any secret.

---

## This module at a glance

| # | ID | Feature | Owner | Builds on |
|---|---|---|---|---|
| 4 | M2-1 | Job search and filters | Member-1 (Imran) | Job posts (M1-3); the employer's verified flag (M2-3, same module) |
| 5 | M2-2 | One-click apply and application tracking | Member-2 (Shafin) | Job posts (M1-3) and profiles (M1-2) |
| 6 | M2-3 | Employer profile and verification | Member-3 (Ahsan) | The basic employer record and job posts (M1-3); the shared upload (S7); the audit log (S6) |

**Order of work:** the three features in parallel, each in its own branch. Finish with M2-WRAP together.

**Agree on day 1 of Module 2**

- Member-1 and Member-3: the verified flag that job cards and the job page read from `EmployerDirectory`, and the `VerifiedBadge` component.
- Member-1 and Member-2: the place on the job page where Member-2's `ApplyButton` goes, and the names in the public `Hiring` API that Member-1's ranked applicants page (M3-2) calls in Module 3.

**Stand-ins in this module**

- Application statuses beyond "applied" and "withdrawn" are set by employers only from Module 3 (M3-2). Until then, M2-2 adds demo applications in every status.
- Posts still publish as soon as they're submitted (`kaaj.jobs.auto-publish: true`) until screening (M3-6). From this module on, only verified employers can submit them (M2-3).

**Flyway versions in this file** (§22)

| Versions | Used by |
|---|---|
| `V210`–`V219` | M2-1, Member-1 |
| `V220`–`V229` | M2-2, Member-2 |
| `V230`–`V239` | M2-3, Member-3 |
| `db/demo/R__*.sql` | Demo data: repeatable, idempotent, dev and demo profiles only |

---

## Prompt index

| Prompt | Who | What it builds | Run after |
|---|---|---|---|
| M2-1-plan, -data, -logic, -api, -ui | Member-1 | Job search and filters | M1-WRAP |
| M2-2-plan, -data, -logic, -api, -ui | Member-2 | One-click apply and application tracking | M1-WRAP |
| M2-3-plan, -data, -logic, -api, -ui | Member-3 | Employer profile and verification | M1-WRAP |
| FINISH | Each member | Finishes any feature for its pull request | The feature's last prompt |
| M2-WRAP | Everyone | Module 2 integration, timing check, demo data and lab notes | All three features |

---

## Feature 4 · M2-1 · Job search and filters · Member-1 (Imran)

**Builds on:** job posts (M1-3) through `JobDirectory` and the job table; the employer's verified flag (M2-3).
**Agree first:** the verified flag with Member-3; the `ApplyButton` slot with Member-2.
**Done when:**
- Guests and job seekers can search in Bangla, Banglish and English, filter and sort, and scroll through pages with no repeats or gaps.
- Distance uses the profile area for signed-in job seekers, or a point on the map; piece-rate posts are labelled and never wrongly filtered out.
- Saved jobs work; closed and expired posts never appear in results.
- Search stays under 500 ms with 1,000 posts (§20).

### M2-1-plan

```text
Prompt M2-1-plan · Member-1 (Imran) · Follow the standing rules in the build-prompts file.

<read>§7 M2-1 · §10.5 (the hard filters; matching reuses the distance query) · §11 (`matching`) · §15 `job`, `saved_job`, `employer` · §16 job and saved-job rows · §18 `jobs/` · §19 (data harvesting) · §20 (performance)</read>

<task>
Plan feature M2-1, job search and filters. Don't write code yet.
1. Check the repository: is Module 1 merged, with job posts and `JobDirectory` (M1-3), the employer directory and the taxonomy search? List anything that's missing.
2. Give the plan: the files, the migration, the search SQL, the cursor format for each sort order, the endpoints with request and response shapes, the pages and the tests.
3. Settle these and say what you chose:
   - Keyword matching: `pg_trgm` on `title_bn` and `title_en`, plus the taxonomy synonyms, so "draiver" finds "ড্রাইভার"; and the similarity threshold.
   - Distance: from the job seeker's profile area, or from a point picked on the map (for guests too); `ST_DWithin` to filter and `ST_Distance` to show km. Write it so matching (M3-1), in the same module, can reuse it.
   - The wage filter: the amount and period the person enters, turned into a monthly amount with `Wages.monthly` and compared with `wage_monthly_max`. Piece-rate posts pass the filter with a "piece rate" label.
   - Cursor pagination for newest, nearest and highest wage (a keyset of the sort value plus the ID).
   - The "Verified" badge: read the employer's verification status, which M2-3 starts setting in this module; agree the field with Member-3.
   - Protection against scraping: rate limits for guests (§19).
4. List your questions and wait for my OK. Once I approve, save the plan as `docs/plans/M2-1.md`.
</task>
```

### M2-1-data

```text
Prompt M2-1-data · Member-1 (Imran) · Follow the standing rules in the build-prompts file. Follow docs/plans/M2-1.md.

<task>
Add saved jobs and the indexes search needs.
1. `V210__saved_job_and_search.sql`: `saved_job` from §15, and any index the plan needs (for example a partial index on published posts by `published_at`, or trigram indexes on the descriptions if you search them).
2. The `SavedJob` entity and repository in `matching`.
3. A test fixture (not a migration) with about 40 posts in Bangla and English around Dhaka and Gazipur, with different wages, periods, shifts and job types, for the integration tests.
4. An integration test for saving and removing a job.
</task>
```

### M2-1-logic

```text
Prompt M2-1-logic · Member-1 (Imran) · Follow the standing rules in the build-prompts file. Follow docs/plans/M2-1.md.

<task>
Build the search.
1. `JobSearch` in `matching`: one SQL query built with `JdbcClient` and bound parameters only (never paste user text into SQL). It returns published, unexpired posts filtered by keyword, category, distance, wage, job type and shift, sorted by newest, nearest or highest wage, 20 per page with a cursor.
2. Each result has what a job card needs: titles in both languages, the employer's name and verified flag, the wage range and period, the monthly equivalent, the area, the distance in km (when a location is known), shift, job type and the date posted.
3. `SavedJobs`: save, remove, and list (open posts only, with a note for closed ones).
4. Integration tests with Testcontainers: each filter alone and combined; "draiver", "ড্রাইভার" and "driver" all find the driver posts; the distance limit; piece rates; cursor pages that never repeat or skip; closed and expired posts never appear.
5. A timing check with 1,000 generated posts against the §20 target (under 500 ms), with the `EXPLAIN ANALYZE` output for the main query.
</task>
```

### M2-1-api

```text
Prompt M2-1-api · Member-1 (Imran) · Follow the standing rules in the build-prompts file. Follow docs/plans/M2-1.md.

<task>
Add the search endpoints.
1. `GET /api/v1/jobs?q=&occupation=&lat=&lng=&radiusKm=&wageMin=&wagePeriod=&type=&shift=&sort=&cursor=` (open to everyone). For a signed-in job seeker who sends no point, use their profile area.
2. `GET /api/v1/jobs/{id}` (open to everyone): the full post with the employer's name and verified flag and the requirements, and for a signed-in job seeker the distance from their area. Closed posts return a clear "closed" state instead of `404`, so saved links still make sense.
3. `PUT` and `DELETE /api/v1/me/saved-jobs/{jobId}`, and `GET /api/v1/me/saved-jobs` (job seekers only).
4. Rate limits for guests (for example 60 searches a minute per IP address) with the helper from S4.
5. MockMvc tests: parameter validation, guests and job seekers, saved jobs for job seekers only, and `429`.
</task>
```

### M2-1-ui

```text
Prompt M2-1-ui · Member-1 (Imran) · Follow the standing rules in the build-prompts file. Follow docs/plans/M2-1.md.

<task>
Build the search pages in `features/jobs/`.
1. `/jobs`: a search box (Bangla, Banglish or English); a filter sheet that slides up on phones (category, distance with a slider from the profile area or a map point, minimum pay with its period, job type, shift); sort; and the number of results.
2. A `JobCard` component that other pages reuse (the feed in M3-1, saved jobs): the title in the current language, the employer's name and `VerifiedBadge` (Member-3's component; a placeholder until it's merged), the wage in taka with Bangla digits, distance, shift, job type and the date posted.
3. Infinite scroll with the cursor, loading skeletons, and empty states with suggestions ("try a wider distance").
4. `/jobs/:id`: the full post, the requirements, a map of the area (the shared map component), save or unsave, a place for Member-2's `ApplyButton` (M2-2), and a place for Member-3's `ReportButton` (M3-6).
5. `/saved`: saved jobs, with closed posts marked.
6. Filters live in the URL, so a search can be shared and the back button works.
7. Vitest tests for the filter sheet and the card.
</task>
```

Then run `FINISH` for M2-1.

---

## Feature 5 · M2-2 · One-click apply and application tracking · Member-2 (Shafin)

**Builds on:** job posts (M1-3, through `JobDirectory`) and profiles (M1-2, through `ProfileDirectory`).
**Agree first:** the `Hiring` API names with Member-1 (used by M3-2 in Module 3); the `ApplyButton` slot on Member-1's job page.
**Stand-in:** demo applications in every status, until employers can change statuses in M3-2.
**Done when:**
- One tap applies from the job page; a second tap or a retried request doesn't create a second application.
- A profile without a name or an occupation can't apply, and the person is told what to add.
- "My applications" shows every status correctly (with the demo data), with the timeline and the interview details.
- Withdrawing works, and the employer sees nothing more about that person.
- `Hiring.changeStatus` refuses moves §9 doesn't allow, and employers who don't own the job.

### M2-2-plan

```text
Prompt M2-2-plan · Member-2 (Shafin) · Follow the standing rules in the build-prompts file.

<read>§7 M2-2 · §9 (application lifecycle and allowed moves) · §11 (`hiring`, the events `ApplicationSubmitted` and `ApplicationStatusChanged`) · §15 `application`, `application_event` · §16 application rows · §19 (employers see a profile only after the person applies)</read>

<task>
Plan feature M2-2, one-click apply and application tracking. Don't write code yet.
1. Check the repository: are `JobDirectory` (M1-3) and `ProfileDirectory` (M1-2) available? List anything that's missing.
2. Give the plan: the files, the migration, the status rules, the public `Hiring` API, the events, the endpoints, the pages and the tests.
3. Settle these and say what you chose:
   - The status rules from §9, in one enum with `canMoveTo`; who may make each move (job seeker, employer or system); and what each move needs (an interview needs a date, time and place).
   - The public `Hiring` API that Member-1's ranked applicants page (M3-2) calls in Module 3: list a job's applications, mark one viewed, and change a status as the employer, with the ownership check inside. Agree the method names with Member-1 now.
   - Idempotency: the same `Idempotency-Key` returns the same application, and a second application to the same job is refused.
   - Demo statuses: employers change statuses only from Module 3, so plan a dev-profile way to put demo applications into every status.
   - What the employer may see after someone applies (the profile summary and photo) and after they withdraw (nothing more).
4. List your questions and wait for my OK. Once I approve, save the plan as `docs/plans/M2-2.md`.
</task>
```

### M2-2-data

```text
Prompt M2-2-data · Member-2 (Shafin) · Follow the standing rules in the build-prompts file. Follow docs/plans/M2-2.md.

<task>
Add the application tables in the `hiring` module.
1. `V220__application.sql`: `application` and `application_event` from §15, with a check constraint for the statuses and an index on `(seeker_id, created_at)`.
2. The entities, `ApplicationStatus`, and the repositories.
3. Integration tests: one application per job and person; the idempotency key is unique; events are read back in order.
</task>
```

### M2-2-logic

```text
Prompt M2-2-logic · Member-2 (Shafin) · Follow the standing rules in the build-prompts file. Follow docs/plans/M2-2.md.

<task>
Build the application rules and the public `Hiring` API.
1. `ApplicationStatus` with `canMoveTo` and the actor allowed for each move (§9), with a test for every pair of statuses.
2. `ApplicationService.apply`: the profile has a name and at least one occupation (`ProfileDirectory`); the post is published and not expired (`JobDirectory`); idempotent by key; one per job and person. It writes an `application_event` and publishes `ApplicationSubmitted` with the job, employer and job seeker IDs.
3. `withdraw`, by the job seeker, from any open status; afterwards the employer sees only that the person withdrew.
4. The public `Hiring` API from the plan: `applicationsForJob(employerUserId, jobId)`, `markViewed(employerUserId, applicationId)` and `changeStatus(employerUserId, applicationId, newStatus, details)`. Each checks that the post belongs to the employer, applies the rules, records the event and publishes `ApplicationStatusChanged`.
5. Extend your profile photo access rule from M1-2: an employer may see the photo of someone who applied to one of their posts, but not after that person withdraws.
6. A dev-profile-only runner (or demo data in `db/demo`) that puts demo applications into every status, so the tracking page can be built before M3-2.
7. Unit tests for the rules and the service, and an integration test for two simultaneous applies with the same key.
</task>
```

### M2-2-api

```text
Prompt M2-2-api · Member-2 (Shafin) · Follow the standing rules in the build-prompts file. Follow docs/plans/M2-2.md.

<task>
Add the application endpoints.
1. `POST /api/v1/jobs/{id}/applications` with an `Idempotency-Key` header (job seekers): `201` with the application, or the same application again for the same key; `409` if the person already applied; `422` with the missing profile fields if the profile isn't ready; a clear error for a closed post.
2. `GET /api/v1/me/applications?status=&cursor=` (each application with the job summary, its current status and the last change), `GET /api/v1/me/applications/{id}` (the timeline) and `POST /api/v1/me/applications/{id}/withdraw`.
3. MockMvc tests: roles, ownership, idempotency, a profile that isn't ready, applying to a closed post, withdrawing twice.
</task>
```

### M2-2-ui

```text
Prompt M2-2-ui · Member-2 (Shafin) · Follow the standing rules in the build-prompts file. Follow docs/plans/M2-2.md.

<task>
Build applying and tracking in `features/applications/`.
1. `ApplyButton`, exported for Member-1's job page: it opens a sheet showing exactly what the employer will see (the profile summary), then applies in one tap. If the profile isn't ready, it lists what's missing and links to the profile. Afterwards it shows "Applied", with a link to the application.
2. `/applications`, "My applications": a status chip on each item in Bangla and English (`DECLINED` shown as "not selected"), filters by status, and the employer and wage.
3. The application page: the timeline of status changes, the interview date, time and place when there is one, and Withdraw with a confirmation.
4. Optimistic updates with TanStack Query, and the list refreshing when the person comes back to the page.
5. Vitest tests for the status chips and the apply sheet.
</task>
```

Then run `FINISH` for M2-2.

---

## Feature 6 · M2-3 · Employer profile and verification · Member-3 (Ahsan)

**Builds on:** the basic employer record and job posts (M1-3), the shared upload (S7) and the audit log (S6).
**Agree first:** the verified flag and the `VerifiedBadge` component with Member-1.
**Switches on:** the publishing check (`kaaj.jobs.require-verified-employer: true`), so only verified employers can submit posts.
**Done when:**
- An employer completes the profile and uploads a sample trade licence; the admin approves it, and the "Verified" badge appears on their posts in search.
- An unverified employer can't publish and is told why; a rejected employer sees the reason, fixes it and resubmits.
- Changing the business name or document after approval sends the employer back to `PENDING`.
- Only admins can open the document, and every decision is in the audit log.

### M2-3-plan

```text
Prompt M2-3-plan · Member-3 (Ahsan) · Follow the standing rules in the build-prompts file.

<read>§5 · §7 M2-3, and M1-1 (so the two features don't overlap) · §10.6 ("New, unverified employer: can't publish") · §11 (`employer`, `EmployerVerified`, `EmployerRejected`) · §15 `employer` · §16 employer and admin verification rows · §19 · §27 decisions 18 and 22</read>

<task>
Plan feature M2-3, employer profile and verification. Don't write code yet.
1. Check the repository: the basic employer record and job posts (M1-3), the shared upload (S7) and the audit log (S6). List anything that's missing.
2. Give the plan: the files, any migration, the full business profile, the status flow, the publishing check, the events, the endpoints, the pages and the tests.
3. Settle these and say what you chose:
   - The status flow: UNVERIFIED → PENDING (business details and a document sent) → VERIFIED, or REJECTED with a reason; a rejected employer fixes and resubmits; changing the business name or the document after approval sends them back to PENDING.
   - Switching on the publishing check: from this module, only verified employers can submit posts (`kaaj.jobs.require-verified-employer`); the others' posts stay as drafts, with a clear message. Decide what happens to the test posts that unverified employers published in Module 1.
   - The trust score: for example 0.8 once verified and 0.5 before; matching uses it in Module 3.
   - The "Verified" badge on job cards: the field Member-1's search (M2-1) reads, agreed with Member-1.
   - Who sees the document: admins only; the employer sees the file name and the status.
4. List your questions and wait for my OK. Once I approve, save the plan as `docs/plans/M2-3.md`.
</task>
```

### M2-3-data

```text
Prompt M2-3-data · Member-3 (Ahsan) · Follow the standing rules in the build-prompts file. Follow docs/plans/M2-3.md.

<task>
Prepare the employer table for verification.
1. `V230__employer_verification.sql`, only if the plan needs it: for example a `submitted_at` column for the queue order, and an index on `verification_status`.
2. Update the `Employer` entity and repository; add `VerificationStatus` and `EmployerKind`.
3. An integration test for the queue query (oldest first).
</task>
```

### M2-3-logic

```text
Prompt M2-3-logic · Member-3 (Ahsan) · Follow the standing rules in the build-prompts file. Follow docs/plans/M2-3.md.

<task>
Build the business profile, the verification flow and the publishing check.
1. `EmployerProfileService`: the full business profile (name, kind, address, map point, contact person) with validation, and the document through `FileStore` (DOCUMENT). Sending the document moves the employer to PENDING.
2. The status flow from the plan in one place, with a test for every move, including going back to PENDING after a change.
3. `EmployerVerification`, for admins: approve, or reject with a reason; each decision writes the audit log, sets the trust score, and publishes `EmployerVerified` or `EmployerRejected` (for notifications in M3-4).
4. The publishing check in `job`: with `kaaj.jobs.require-verified-employer` on, a post from an unverified employer stays a draft, with the reason. Approval doesn't publish anything by itself.
5. `EmployerDirectory` returns the verified flag for search (M2-1), job cards and matching.
6. A `FileAccessPolicy` for employer documents: admins only.
7. Unit tests for the flow and the publishing check.
</task>
```

### M2-3-api

```text
Prompt M2-3-api · Member-3 (Ahsan) · Follow the standing rules in the build-prompts file. Follow docs/plans/M2-3.md.

<task>
Add the verification endpoints.
1. `GET` and `PUT /api/v1/employer/profile` (now the full profile), and `POST /api/v1/employer/profile/document` (multipart; sends the employer for verification).
2. `GET /api/v1/admin/employers?status=PENDING&cursor=` (the queue, with the business details and a link to the document) and `POST /api/v1/admin/employers/{id}/verification` (`APPROVE`, or `REJECT` with a reason).
3. MockMvc tests: roles; an employer can't see another employer; the queue is for admins only; a rejection needs a reason; the publishing check.
</task>
```

### M2-3-ui

```text
Prompt M2-3-ui · Member-3 (Ahsan) · Follow the standing rules in the build-prompts file. Follow docs/plans/M2-3.md.

<task>
Build the employer profile and the admin queue.
1. `/employer/profile` in `features/employer-profile/`: the business profile form with the shared map picker, the document upload, and a status banner for each state: not verified (what to do), pending (what happens next), verified, and rejected (the reason and how to fix it).
2. A `VerifiedBadge` component for job cards and job pages (Member-1's `JobCard` uses it), with a short explanation when tapped.
3. On "Post a job" and "My jobs" (M1-3): a clear message when the employer isn't verified yet, linking to the profile.
4. The admin "Verification" page in `features/admin-trust/`, in the admin layout: the queue, the business details next to the document, and Approve or Reject with a reason (common reasons as quick choices).
5. Vitest tests for the status banner.
</task>
```

Then run `FINISH` for M2-3.

---

## FINISH · any feature

```text
Prompt FINISH for [feature ID, for example M2-3] · [Member-n (name)] · Follow the standing rules in the build-prompts file. Follow docs/plans/[feature ID].md.

<task>
Finish feature [feature ID] and get it ready for review.
1. Go through every acceptance bullet for [feature ID] in §7 of the context file and its "Done when" list in the build-prompts file. For each one, say how it's met (a test, a file or a check by hand), and fix whatever isn't met.
2. Run `./mvnw verify` and `cd web && npm test && npm run build`, and fix any failure. `ModularityTests` passes, the API types are regenerated, and no visible text is missing from `bn.json` or `en.json`.
3. Add one Playwright test for the feature's main flow.
4. Review the change as a strict reviewer would: security (OWASP Top 10), ownership checks, validation, error messages, missing tests, the module rules (§11) and the decisions (§27). Fix what you find.
5. Add the feature's demo data to `db/demo` if the module demo needs it (repeatable and idempotent).
6. Write the pull-request description: what it does, how to test it by hand, the screenshots to take, and the context-file updates (standing rule 10).
</task>
```

---

## M2-WRAP · Everyone · Module 2 check, timing, demo data and lab notes

```text
Prompt M2-WRAP · Everyone (one person drives, the others review) · Follow the standing rules in the build-prompts file.

<task>
Check that Modules 1 and 2 work as one product before the lab demo.
1. Merge the three features into `main`. Run `./mvnw verify` and `npm test`. Then drop the local database and migrate a fresh one with out-of-order off, to prove the versions apply in order.
2. Extend the demo data in `db/demo/R__demo_module2.sql` (repeatable and idempotent): Mr Karim verified; one unverified employer; Rahim with saved jobs; and applications in every status for the tracking demo. Made-up details only.
3. Walk through these and fix anything that breaks:
   - A guest searches "pickup driver" in Bangla and in Banglish, and filters by distance and pay.
   - Rahim saves a job and applies in one tap, then sees "Applied" in his applications.
   - Mr Karim's business is verified by the admin, and the "Verified" badge appears on his posts in search.
   - An unverified employer can't publish, and sees why.
   - Rahim withdraws an application.
4. Playwright tests for these flows.
5. Search timing with 1,000 generated posts (§20); fix the slow parts.
6. Lab notes: for each member, a five-line explanation of their feature (what it does, how a request flows through the layers, one design decision and why, and one test that proves it works), and a demo script with timings.
7. List the context-file updates from this module (standing rule 10), the questions to bring to the teacher, and anything Module 3 needs to know (especially the `Hiring` API for M3-2).
</task>
```
