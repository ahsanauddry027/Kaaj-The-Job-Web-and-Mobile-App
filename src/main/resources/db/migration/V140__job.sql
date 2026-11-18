-- Feature 4 (Module 1) · Post and manage jobs · Member-3 (Ahsan Habib)
-- The job table from context file §15. Differences, all for drafts and the manage actions:
--   * a draft may be incomplete; the CHECKs require the wage, description and location
--     for every status after DRAFT, so a post can never go live without a wage;
--   * latitude/longitude for the map pin (Member-1 adds the PostGIS point for distance search), and the area name;
--   * status_reason, closed_at, reposted_from, idempotency_key and timestamps.
CREATE TABLE job (
  id                    uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  employer_id           uuid NOT NULL REFERENCES employer (id),
  occupation_id         uuid REFERENCES occupation (id),
  title_bn              text NOT NULL CHECK (length(title_bn) BETWEEN 2 AND 120),
  title_en              text NOT NULL CHECK (length(title_en) BETWEEN 2 AND 120),
  description_bn        text CHECK (length(description_bn) <= 4000),
  description_en        text CHECK (length(description_en) <= 4000),
  job_type              text NOT NULL DEFAULT 'FULL_TIME' CHECK (job_type IN ('FULL_TIME', 'PART_TIME', 'DAILY')),
  shift                 text NOT NULL DEFAULT 'DAY' CHECK (shift IN ('DAY', 'NIGHT', 'ROTATING', 'FLEXIBLE')),
  hours                 text CHECK (hours ~ '^[0-2][0-9]:[0-5][0-9]-[0-2][0-9]:[0-5][0-9]$'),   -- e.g. 08:00-18:00
  openings              integer NOT NULL DEFAULT 1 CHECK (openings BETWEEN 1 AND 1000),
  wage_min              integer CHECK (wage_min > 0),
  wage_max              integer CHECK (wage_max > 0),
  wage_period           text CHECK (wage_period IN ('HOUR', 'DAY', 'WEEK', 'MONTH', 'PIECE')),
  wage_monthly_max      integer,                    -- for filters and sorting; null for piece rates
  latitude              double precision CHECK (latitude BETWEEN -90 AND 90),
  longitude             double precision CHECK (longitude BETWEEN -180 AND 180),
  area_name             text,                       -- e.g. "Tongi, Gazipur"
  remote                boolean NOT NULL DEFAULT false,
  min_experience_years  integer CHECK (min_experience_years BETWEEN 0 AND 50),
  skill_ids             uuid[] NOT NULL DEFAULT '{}',
  required_documents    text[] NOT NULL DEFAULT '{}',   -- DocumentCode values: DRIVING_PRO_LIGHT, CERT:<skill id>, ...
  status                text NOT NULL DEFAULT 'DRAFT'
                        CHECK (status IN ('DRAFT', 'IN_REVIEW', 'PUBLISHED', 'PAUSED', 'CLOSED', 'BLOCKED')),
  status_reason         text,                       -- EXPIRED, CLOSED_BY_EMPLOYER, or a screening reason
  published_at          timestamptz,
  expires_at            timestamptz,                -- published_at + 30 days
  closed_at             timestamptz,
  reposted_from         uuid REFERENCES job (id),
  idempotency_key       uuid UNIQUE,                -- makes "create" safe to retry
  created_at            timestamptz NOT NULL DEFAULT now(),
  updated_at            timestamptz NOT NULL DEFAULT now(),
  CHECK (wage_min IS NULL OR wage_max IS NULL OR wage_min <= wage_max),
  CHECK (status = 'DRAFT' OR (wage_min IS NOT NULL AND wage_max IS NOT NULL AND wage_period IS NOT NULL)),
  CHECK (status = 'DRAFT' OR (description_bn IS NOT NULL AND description_en IS NOT NULL)),
  CHECK (status = 'DRAFT' OR remote OR (latitude IS NOT NULL AND longitude IS NOT NULL)),
  CHECK (status NOT IN ('PUBLISHED', 'PAUSED') OR (published_at IS NOT NULL AND expires_at IS NOT NULL))
);

CREATE INDEX job_employer_idx ON job (employer_id, created_at DESC, id DESC);
CREATE INDEX job_expiry_idx ON job (expires_at) WHERE status IN ('PUBLISHED', 'PAUSED');
CREATE INDEX job_published_idx ON job (published_at DESC) WHERE status = 'PUBLISHED';
CREATE INDEX job_title_bn_trgm_idx ON job USING gin (title_bn gin_trgm_ops);
CREATE INDEX job_title_en_trgm_idx ON job USING gin (title_en gin_trgm_ops);
