-- Job seeker profile (M1-2), built for the single-member Kotlin/Android version of Kaaj.
-- Same columns as the project context file (§15 seeker_profile), with two changes:
--   * no PostGIS: the home point is two numbers, rounded to about 500 m (0.005°) before it is stored;
--   * documents are the licences the person says they hold, until skill certification (M1-1) verifies them.
CREATE TABLE seeker_profile (
  user_id           uuid PRIMARY KEY REFERENCES app_user (id) ON DELETE CASCADE,
  display_name      text NOT NULL CHECK (length(display_name) BETWEEN 2 AND 80),
  phone             text,                                  -- hidden until the employer shortlists
  occupation_ids    uuid[] NOT NULL DEFAULT '{}',
  skill_ids         uuid[] NOT NULL DEFAULT '{}',
  years_experience  smallint NOT NULL DEFAULT 0 CHECK (years_experience BETWEEN 0 AND 60),
  education         text CHECK (length(education) <= 200),
  documents         text[] NOT NULL DEFAULT '{}',          -- DRIVING_PRO_LIGHT, ... (DocumentCode)
  home_latitude     double precision CHECK (home_latitude BETWEEN -90 AND 90),
  home_longitude    double precision CHECK (home_longitude BETWEEN -180 AND 180),
  home_area_name    text CHECK (length(home_area_name) <= 120),
  travel_radius_km  real NOT NULL DEFAULT 10 CHECK (travel_radius_km BETWEEN 1 AND 100),
  expected_wage     integer CHECK (expected_wage > 0),
  wage_period       text CHECK (wage_period IN ('HOUR', 'DAY', 'WEEK', 'MONTH', 'PIECE')),
  shifts            text[] NOT NULL DEFAULT '{}',          -- DAY, NIGHT, ROTATING, PART_TIME, FLEXIBLE
  created_at        timestamptz NOT NULL DEFAULT now(),
  updated_at        timestamptz NOT NULL DEFAULT now(),
  CHECK ((home_latitude IS NULL) = (home_longitude IS NULL)),
  CHECK ((expected_wage IS NULL) = (wage_period IS NULL))
);

-- Saved jobs (M2-1): a job seeker's "for later" list.
CREATE TABLE saved_job (
  user_id   uuid NOT NULL REFERENCES app_user (id) ON DELETE CASCADE,
  job_id    uuid NOT NULL REFERENCES job (id) ON DELETE CASCADE,
  saved_at  timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (user_id, job_id)
);

-- Search (M2-1) uses the indexes V140 already made: job_published_idx and the title trigram indexes.
CREATE INDEX job_occupation_published_idx ON job (occupation_id) WHERE status = 'PUBLISHED';
