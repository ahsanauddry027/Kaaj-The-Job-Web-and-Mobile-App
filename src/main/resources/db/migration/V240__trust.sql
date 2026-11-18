-- Scam screening and moderation · Member-3 (Ahsan Habib) · Module 2 in the final requirements
-- (feature "M2-4", so Flyway range V240–V249). Context file §15 "trust": reports from users, and moderation
-- cases opened by screening or by reports. One OPEN case per subject; new reports join it.

CREATE TABLE trust_case (
  id             uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  subject_type   text NOT NULL CHECK (subject_type IN ('JOB', 'MESSAGE', 'EMPLOYER')),
  subject_id     uuid NOT NULL,
  source         text NOT NULL CHECK (source IN ('SCREENING', 'REPORT')),
  findings       jsonb NOT NULL DEFAULT '[]',     -- rule hits: [{"kind", "code", "quote"}]
  ai_assessment  jsonb,                           -- the optional AI check, when it ran
  screening      text CHECK (screening IN ('REVIEW', 'BLOCK')),
  report_count   integer NOT NULL DEFAULT 0 CHECK (report_count >= 0),
  status         text NOT NULL DEFAULT 'OPEN' CHECK (status IN ('OPEN', 'REMOVED', 'DISMISSED')),
  decision       text CHECK (decision IN ('REMOVE_POST', 'SUSPEND_EMPLOYER', 'DISMISS')),
  note           text CHECK (char_length(note) <= 500),
  decided_by     uuid REFERENCES app_user (id),
  decided_at     timestamptz,
  created_at     timestamptz NOT NULL DEFAULT now(),
  updated_at     timestamptz NOT NULL DEFAULT now(),
  CHECK ((status = 'OPEN') = (decision IS NULL)),
  CHECK ((status = 'OPEN') = (decided_at IS NULL))
);

-- One open case per post (or message, or employer); decided cases stay as history.
CREATE UNIQUE INDEX trust_case_one_open_per_subject ON trust_case (subject_type, subject_id) WHERE status = 'OPEN';
CREATE INDEX trust_case_status_created_idx ON trust_case (status, created_at);
CREATE INDEX trust_case_subject_idx ON trust_case (subject_type, subject_id);

CREATE TABLE report (
  id             uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  case_id        uuid NOT NULL REFERENCES trust_case (id) ON DELETE CASCADE,
  reporter_id    uuid NOT NULL REFERENCES app_user (id),
  subject_type   text NOT NULL CHECK (subject_type IN ('JOB', 'MESSAGE', 'EMPLOYER')),
  subject_id     uuid NOT NULL,
  reason         text NOT NULL CHECK (reason IN ('ASKED_FOR_MONEY', 'FAKE_JOB', 'HARASSMENT', 'OTHER')),
  details        text CHECK (char_length(details) <= 500),
  created_at     timestamptz NOT NULL DEFAULT now(),
  UNIQUE (reporter_id, subject_type, subject_id)   -- one report per person per post
);

CREATE INDEX report_case_idx ON report (case_id);
CREATE INDEX report_reporter_created_idx ON report (reporter_id, created_at);   -- the daily limit per user
