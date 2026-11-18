-- Demo data for scam screening and moderation (dev profile only; repeatable and safe to run again).
-- Made-up people and businesses only. Password for the demo account:  Kaaj@12345
--   quickjobs@demo.kaaj   employer, VERIFIED (Quick Jobs & Overseas, Uttara) with three posts:
--     b1  "Malaysia factory job" asking for a processing fee   → BLOCKED by screening (open case)
--     b2  "Online data entry, earn from home" with WhatsApp     → IN REVIEW (open case)
--     b3  "Warehouse helper", published, reported twice        → open case; a third report hides it
-- Reports come from rahim (R__demo_member3.sql) and sumi; jamal (R__demo_seeker.sql) can send the third one.
-- sumi's account is added here too (skipped if R__demo_seeker.sql already made it), so this file also works
-- in a repository without the job seeker module.

INSERT INTO app_user (id, email, email_verified, password_hash, role, locale) VALUES
  ('01936a00-0000-7000-8000-000000000006', 'quickjobs@demo.kaaj', true, '$2a$10$Ii5zHlbT0pd57srhGnjCEu40bxD5eBJGF.0x8Oe.QY1yz1YKPwJGa', 'EMPLOYER', 'en'),
  ('01936a00-0000-7000-8000-000000000004', 'sumi@demo.kaaj', true, '$2a$10$Ii5zHlbT0pd57srhGnjCEu40bxD5eBJGF.0x8Oe.QY1yz1YKPwJGa', 'SEEKER', 'bn')
ON CONFLICT DO NOTHING;

INSERT INTO employer (id, owner_id, name, kind, address, latitude, longitude, contact_person, contact_phone,
                      contact_email, document_name, document_type, verification_status, verification_note,
                      submitted_at, verified_at, trust_score)
VALUES
  ('01936a00-0000-7000-8000-0000000000e3', '01936a00-0000-7000-8000-000000000006', 'Quick Jobs & Overseas', 'AGENCY',
   'Sector 7, Uttara, Dhaka', 23.8690, 90.3990, 'Rafiq Hasan', '01611223344', NULL,
   'trade-licence.jpg', 'TRADE_LICENCE', 'VERIFIED', NULL,
   now() - interval '9 days', now() - interval '8 days', 0.8)
ON CONFLICT DO NOTHING;

INSERT INTO job (id, employer_id, occupation_id, title_bn, title_en, description_bn, description_en, job_type, shift,
                 hours, openings, wage_min, wage_max, wage_period, wage_monthly_max, latitude, longitude, area_name,
                 remote, min_experience_years, skill_ids, required_documents, status, status_reason, published_at,
                 expires_at, closed_at, created_at, updated_at)
VALUES
  ('01936a00-0000-7000-8000-0000000000b1', '01936a00-0000-7000-8000-0000000000e3',
   (SELECT id FROM occupation WHERE name_en = 'Factory worker'),
   'মালয়েশিয়ায় কারখানার চাকরি', 'Malaysia factory job',
   'মালয়েশিয়ায় কারখানার কাজ। প্রসেসিং ফি ৫০,০০০ টাকা বিকাশে পাঠান। ৩০ দিনে ভিসা।',
   'Malaysia factory job. Processing fee Tk 50,000 by bKash. Visa in 30 days.',
   'FULL_TIME', 'DAY', '08:00-17:00', 20, 45000, 60000, 'MONTH', 60000, 23.8690, 90.3990, 'উত্তরা, ঢাকা',
   false, NULL, '{}', '{}', 'BLOCKED', 'SCREENING_BLOCKED', NULL, NULL, NULL,
   now() - interval '2 days', now() - interval '2 days'),
  ('01936a00-0000-7000-8000-0000000000b2', '01936a00-0000-7000-8000-0000000000e3',
   (SELECT id FROM occupation WHERE name_en = 'Office assistant'),
   'অনলাইন ডেটা এন্ট্রি', 'Online data entry',
   'ঘরে বসে আয় করুন। দৈনিক ২০০০ টাকা। যোগাযোগ হোয়াটসঅ্যাপে।',
   'Work from home and earn Tk 2,000 daily. Contact us on WhatsApp.',
   'PART_TIME', 'FLEXIBLE', '10:00-14:00', 50, 2000, 2000, 'DAY', 52000, NULL, NULL, NULL,
   true, NULL, '{}', '{}', 'IN_REVIEW', 'SCREENING_REVIEW', NULL, NULL, NULL,
   now() - interval '1 day', now() - interval '1 day'),
  ('01936a00-0000-7000-8000-0000000000b3', '01936a00-0000-7000-8000-0000000000e3',
   (SELECT id FROM occupation WHERE name_en = 'Warehouse worker'),
   'গুদাম সহকারী', 'Warehouse helper',
   'উত্তরার গুদামে মালামাল ওঠানো-নামানো ও প্যাকিং।', 'Loading, unloading and packing at our Uttara warehouse.',
   'FULL_TIME', 'DAY', '09:00-17:00', 4, 11000, 13000, 'MONTH', 13000, 23.8700, 90.4010, 'উত্তরা, ঢাকা',
   false, NULL, '{}', '{}', 'PUBLISHED', NULL, now() - interval '5 days', now() + interval '25 days', NULL,
   now() - interval '5 days', now() - interval '5 days')
ON CONFLICT DO NOTHING;

INSERT INTO trust_case (id, subject_type, subject_id, source, findings, screening, report_count, status, created_at, updated_at)
VALUES
  ('01936a00-0000-7000-8000-0000000000f1', 'JOB', '01936a00-0000-7000-8000-0000000000b1', 'SCREENING',
   '[{"kind": "BLOCK", "code": "money-request", "quote": "প্রসেসিং ফি"},
     {"kind": "BLOCK", "code": "money-request", "quote": "Processing fee"},
     {"kind": "REVIEW", "code": "overseas-no-licence", "quote": "মালয়েশিয়া"}]',
   'BLOCK', 0, 'OPEN', now() - interval '2 days', now() - interval '2 days'),
  ('01936a00-0000-7000-8000-0000000000f2', 'JOB', '01936a00-0000-7000-8000-0000000000b2', 'SCREENING',
   '[{"kind": "REVIEW", "code": "contact-details", "quote": "হোয়াটসঅ্যাপ"},
     {"kind": "REVIEW", "code": "contact-details", "quote": "WhatsApp"},
     {"kind": "REVIEW", "code": "easy-money", "quote": "ঘরে বসে"},
     {"kind": "REVIEW", "code": "easy-money", "quote": "Work from home"}]',
   'REVIEW', 0, 'OPEN', now() - interval '1 day', now() - interval '1 day'),
  ('01936a00-0000-7000-8000-0000000000f3', 'JOB', '01936a00-0000-7000-8000-0000000000b3', 'REPORT',
   '[]', NULL, 2, 'OPEN', now() - interval '3 days', now() - interval '1 day')
ON CONFLICT DO NOTHING;

INSERT INTO report (id, case_id, reporter_id, subject_type, subject_id, reason, details, created_at) VALUES
  ('01936a00-0000-7000-8000-0000000000d1', '01936a00-0000-7000-8000-0000000000f3', '01936a00-0000-7000-8000-000000000003',
   'JOB', '01936a00-0000-7000-8000-0000000000b3', 'ASKED_FOR_MONEY',
   'At the interview they asked for Tk 1,500 for the uniform and the ID card.', now() - interval '3 days'),
  ('01936a00-0000-7000-8000-0000000000d2', '01936a00-0000-7000-8000-0000000000f3', '01936a00-0000-7000-8000-000000000004',
   'JOB', '01936a00-0000-7000-8000-0000000000b3', 'FAKE_JOB',
   'The phone number on the gate is switched off and there is no warehouse at this address.', now() - interval '1 day')
ON CONFLICT DO NOTHING;
