-- Shared setup (S2, Member-3): the identity tables (context file §15).
-- pg_trgm (spelling-tolerant search) is part of every standard PostgreSQL install, including the
-- Windows installer. PostGIS and pgvector are added later by the features that need them
-- (Member-1's distance search and AI matching), so this runs on a plain local PostgreSQL.
-- IDs are generated in Java as time-ordered UUIDv7; the gen_random_uuid() defaults are only used by
-- rows inserted with plain SQL (the taxonomy seed) and work on PostgreSQL 13 and newer.
CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE TABLE app_user (
  id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  email           text NOT NULL UNIQUE,
  email_verified  boolean NOT NULL DEFAULT false,
  password_hash   text,                         -- null for Google-only accounts
  google_sub      text UNIQUE,                  -- Google account id
  role            text NOT NULL CHECK (role IN ('SEEKER', 'EMPLOYER', 'ADMIN')),
  locale          text NOT NULL DEFAULT 'bn',
  status          text NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'SUSPENDED')),
  created_at      timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE email_code (
  id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  email       text NOT NULL,
  purpose     text NOT NULL,                    -- VERIFY_EMAIL, SIGN_IN, RESET_PASSWORD
  code_hash   text NOT NULL,                    -- HMAC-SHA256 of the code, never the code
  expires_at  timestamptz NOT NULL,             -- 10 minutes after sending
  attempts    smallint NOT NULL DEFAULT 0,      -- refuse after 5
  used_at     timestamptz,
  created_at  timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX ON email_code (email, purpose, created_at DESC);

CREATE TABLE audit_log (
  id          bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  actor_id    uuid REFERENCES app_user (id),
  action      text NOT NULL,                    -- USER_SUSPENDED, EMPLOYER_VERIFIED, POST_REMOVED, ...
  target      text NOT NULL,
  details     jsonb,
  created_at  timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX ON audit_log (target, created_at DESC);
