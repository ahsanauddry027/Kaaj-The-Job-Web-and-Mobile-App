-- Feature 3 (Module 1) · Employer profile and verification · Member-3 (Ahsan Habib)
-- The employer table from context file §15, plus:
--   * latitude/longitude for the map pin (Member-1's distance search adds a PostGIS point generated
--     from them in its own migration, so this table needs no PostGIS today);
--   * contact phone and email, and the document's name and type, shown on the business profile;
--   * submitted_at and created_at/updated_at for the admin queue (oldest first) and the status panel.
CREATE TABLE employer (
  id                   uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  owner_id             uuid NOT NULL UNIQUE REFERENCES app_user (id),
  name                 text NOT NULL CHECK (length(name) BETWEEN 2 AND 120),
  kind                 text NOT NULL CHECK (kind IN ('HOUSEHOLD', 'SME', 'COMPANY', 'AGENCY', 'NGO')),
  address              text,
  latitude             double precision CHECK (latitude BETWEEN -90 AND 90),
  longitude            double precision CHECK (longitude BETWEEN -180 AND 180),
  contact_person       text,
  contact_phone        text,                    -- shown to admins only, never on job posts
  contact_email        text,
  document_key         text,                    -- trade licence or NID scan, through FileStore; admins only
  document_name        text,                    -- the original file name, shown to the employer
  document_type        text CHECK (document_type IN ('TRADE_LICENCE', 'NATIONAL_ID')),
  verification_status  text NOT NULL DEFAULT 'UNVERIFIED'
                       CHECK (verification_status IN ('UNVERIFIED', 'PENDING', 'VERIFIED', 'REJECTED')),
  verification_note    text,                    -- the reason shown on rejection, or the admin's note
  submitted_at         timestamptz,             -- last time the profile was sent for review
  verified_by          uuid REFERENCES app_user (id),
  verified_at          timestamptz,
  trust_score          real NOT NULL DEFAULT 0.5,
  created_at           timestamptz NOT NULL DEFAULT now(),
  updated_at           timestamptz NOT NULL DEFAULT now(),
  CHECK (verification_status = 'UNVERIFIED' OR submitted_at IS NOT NULL),
  CHECK (verification_status <> 'REJECTED' OR verification_note IS NOT NULL)
);

-- The admin verification queue: one status, oldest submission first
CREATE INDEX employer_queue_idx ON employer (verification_status, submitted_at, id);
