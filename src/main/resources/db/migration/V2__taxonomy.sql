-- Shared setup (S2, Member-3): the occupation and skill taxonomy with Bangla and English names.
-- Synonyms hold the other ways people write a term: Bangla, Banglish and English.
CREATE TABLE occupation (
  id         uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  isco_code  text,
  name_en    text NOT NULL UNIQUE,
  name_bn    text NOT NULL
);

CREATE TABLE occupation_synonym (
  occupation_id  uuid NOT NULL REFERENCES occupation (id) ON DELETE CASCADE,
  term           text NOT NULL,
  PRIMARY KEY (occupation_id, term)
);

CREATE TABLE skill (
  id       uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  name_en  text NOT NULL UNIQUE,
  name_bn  text NOT NULL
);

CREATE TABLE skill_synonym (
  skill_id  uuid NOT NULL REFERENCES skill (id) ON DELETE CASCADE,
  term      text NOT NULL,                      -- "ড্রাইভার", "চালক", "gari chalai", ...
  PRIMARY KEY (skill_id, term)
);

-- Trigram indexes for spelling-tolerant search (pg_trgm)
CREATE INDEX ON occupation USING gin (name_en gin_trgm_ops);
CREATE INDEX ON occupation USING gin (name_bn gin_trgm_ops);
CREATE INDEX ON occupation_synonym USING gin (term gin_trgm_ops);
CREATE INDEX ON skill USING gin (name_en gin_trgm_ops);
CREATE INDEX ON skill USING gin (name_bn gin_trgm_ops);
CREATE INDEX ON skill_synonym USING gin (term gin_trgm_ops);
