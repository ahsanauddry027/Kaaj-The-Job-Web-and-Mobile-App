# Plan · Member-3 (Ahsan Habib) · Features 3 and 4 (Module 1)

Source: `CSE471_Group08_Functional_Requirements_Kaaj_The_Job.docx` (final). Feature 3 is employer profile and
verification; feature 4 is post and manage jobs. Both are in Module 1, so verification is in force from the start.

## Decisions

| # | Decision | Why |
|---|---|---|
| 1 | Flyway ranges: `V130–V139` for feature 3 (employer), `V140–V149` for feature 4 (job) | Context file §22 rule (feature MX-Y → VXY0) with the docx numbering |
| 2 | Status flow UNVERIFIED → PENDING (document sent) → VERIFIED or REJECTED (reason required); a rejected employer who saves a fix or a new document goes back to PENDING; changing the business name or document after approval also goes back to PENDING | Context file M2-3 rules |
| 3 | Trust score 0.5 before verification, 0.8 after | Context file M2-3 suggestion; used by matching later |
| 4 | Documents: admins only; the employer sees the file name and status | §7, §19 |
| 5 | Job status flow DRAFT → IN_REVIEW → PUBLISHED / BLOCKED; PUBLISHED ↔ PAUSED; PUBLISHED or PAUSED → CLOSED; editing a live post sends it to IN_REVIEW again; repost copies a closed post into a new draft | Context file M1-3 plan |
| 6 | A complete post from an unverified employer stays a DRAFT marked `AWAITING_VERIFICATION`; on `EmployerVerified` the job module submits it | Wireframe E14 ("Your post stays a DRAFT until an admin approves your business") and the Assignment 03 behaviour |
| 7 | Screening stand-in: a listener publishes every `JobSubmitted` while `kaaj.jobs.auto-publish=true`; M3-6 turns it off and decides through `JobModeration` | Context file decision 25 |
| 8 | The 30-day window starts at the first publication and is kept when an edited post is published again | Editing must not extend a post's life |
| 9 | Drafts may be incomplete (title only); the database CHECKs require wage, description and location for every other status | "Save as draft" in the Figma wizard, and the wage rule can never be bypassed |
| 10 | Map pins stored as latitude/longitude; no PostGIS needed yet. Member-1's search migration adds a generated `geography` column from them | Runs on a plain local PostgreSQL; distance search still gets PostGIS later |
| 11 | IDs: UUIDv7 made in Java; table defaults use `gen_random_uuid()` | Works on PostgreSQL 13+, not only 18 |
| 12 | Expiry check every hour (cron `0 5 * * * *`) instead of daily | A post never stays open much more than 30 days |

## Events

| Event | From | To |
|---|---|---|
| `EmployerVerified`, `EmployerRejected` | employer | job (submits waiting drafts); notifications later (M3-4) |
| `JobSubmitted` | job | job's auto-publish stand-in now; trust (M3-6) later |
| `JobPublished` | job | matching and notifications (Module 3) |

## Context-file updates to propose to the team

- §7 and §22: the docx numbering (features 3 and 4 in Module 1) and the Flyway ranges V130 and V140.
- §15: `employer` gains contact phone/email, document name/type, submitted_at, created_at/updated_at, latitude/longitude;
  `job` gains latitude/longitude, area_name, status_reason, closed_at, reposted_from, idempotency_key, created_at/updated_at.
  Both drop the PostGIS `location` column until distance search adds it (generated from latitude/longitude).
- §16: `PUT /employer/jobs/{id}` for edits, `PATCH` for actions, `DELETE` for drafts, `GET /employer/jobs/summary`,
  `GET /admin/employers/counts`, `POST /employer/profile/submit`, `GET /api/v1/jobs/{id}` (public page), `GET /api/v1/auth/csrf`.
- §17: server port 1027 (assignment requirement); `kaaj.jobs.expiry-cron`, `kaaj.auth.simple-sign-up`.
