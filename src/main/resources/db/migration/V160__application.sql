-- One-click apply and application tracking (M2-2), and the employer's applicant review (M3-2, without AI).
-- Same tables as the project context file (§15 application, application_event).
CREATE TABLE application (
  id               uuid PRIMARY KEY,                       -- UUIDv7, made in Java
  job_id           uuid NOT NULL REFERENCES job (id),
  seeker_id        uuid NOT NULL REFERENCES seeker_profile (user_id),
  status           text NOT NULL DEFAULT 'APPLIED'
                   CHECK (status IN ('APPLIED', 'VIEWED', 'SHORTLISTED', 'INTERVIEW', 'HIRED', 'DECLINED', 'WITHDRAWN')),
  message          text CHECK (length(message) <= 500),    -- optional note from the job seeker
  match_score      real CHECK (match_score BETWEEN 0 AND 1),
  interview_at     timestamptz,
  interview_place  text CHECK (length(interview_place) <= 200),
  idempotency_key  uuid UNIQUE,                            -- makes "apply" safe to retry
  created_at       timestamptz NOT NULL DEFAULT now(),
  updated_at       timestamptz NOT NULL DEFAULT now(),
  UNIQUE (job_id, seeker_id)                               -- one application per job and person
);
CREATE INDEX application_job_idx ON application (job_id, created_at);
CREATE INDEX application_seeker_idx ON application (seeker_id, updated_at DESC);

-- Every status change, so both sides can see the timeline.
CREATE TABLE application_event (
  id              uuid PRIMARY KEY,
  application_id  uuid NOT NULL REFERENCES application (id) ON DELETE CASCADE,
  from_status     text,
  to_status       text NOT NULL,
  actor           text NOT NULL CHECK (actor IN ('SEEKER', 'EMPLOYER', 'SYSTEM')),
  note            text CHECK (length(note) <= 500),
  created_at      timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX application_event_idx ON application_event (application_id, created_at);
