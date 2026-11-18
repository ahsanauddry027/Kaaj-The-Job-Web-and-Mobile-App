-- Demo data for Member-3's features (dev profile only; repeatable and safe to run again).
-- Made-up people and businesses only. Every demo account's password is  Kaaj@12345
--   karim@demo.kaaj           employer, VERIFIED   (Karim Transport, Tongi) with four posts
--   tongigarments@demo.kaaj   employer, UNVERIFIED (profile filled in, no document yet)
--   rahim@demo.kaaj           job seeker
-- The admin is created from KAAJ_ADMIN_EMAIL / KAAJ_ADMIN_PASSWORD at startup, never here.

INSERT INTO app_user (id, email, email_verified, password_hash, role, locale) VALUES
  ('01936a00-0000-7000-8000-000000000001', 'karim@demo.kaaj', true, '$2a$10$Ii5zHlbT0pd57srhGnjCEu40bxD5eBJGF.0x8Oe.QY1yz1YKPwJGa', 'EMPLOYER', 'bn'),
  ('01936a00-0000-7000-8000-000000000002', 'tongigarments@demo.kaaj', true, '$2a$10$Ii5zHlbT0pd57srhGnjCEu40bxD5eBJGF.0x8Oe.QY1yz1YKPwJGa', 'EMPLOYER', 'bn'),
  ('01936a00-0000-7000-8000-000000000003', 'rahim@demo.kaaj', true, '$2a$10$Ii5zHlbT0pd57srhGnjCEu40bxD5eBJGF.0x8Oe.QY1yz1YKPwJGa', 'SEEKER', 'bn')
ON CONFLICT DO NOTHING;

INSERT INTO employer (id, owner_id, name, kind, address, latitude, longitude, contact_person, contact_phone,
                      contact_email, document_name, document_type, verification_status, verification_note,
                      submitted_at, verified_at, trust_score)
VALUES
  ('01936a00-0000-7000-8000-0000000000e1', '01936a00-0000-7000-8000-000000000001', 'করিম ট্রান্সপোর্ট', 'SME',
   'স্টেশন রোড, টঙ্গী, গাজীপুর', 23.8916, 90.4023, 'করিম উদ্দিন', '01712345678', 'info@karimtransport.example',
   'trade-licence.pdf', 'TRADE_LICENCE', 'VERIFIED', 'সব তথ্য সঠিক আছে। ধন্যবাদ!',
   now() - interval '14 days', now() - interval '13 days', 0.8),
  ('01936a00-0000-7000-8000-0000000000e2', '01936a00-0000-7000-8000-000000000002', 'Tongi Garments Ltd.', 'COMPANY',
   'Cherag Ali, Tongi, Gazipur', 23.9050, 90.4000, 'Nasrin Akter', '01812345678', NULL,
   NULL, NULL, 'UNVERIFIED', NULL, NULL, NULL, 0.5)
ON CONFLICT DO NOTHING;

INSERT INTO job (id, employer_id, occupation_id, title_bn, title_en, description_bn, description_en, job_type, shift,
                 hours, openings, wage_min, wage_max, wage_period, wage_monthly_max, latitude, longitude, area_name,
                 min_experience_years, skill_ids, required_documents, status, status_reason, published_at,
                 expires_at, closed_at, created_at, updated_at)
VALUES
  ('01936a00-0000-7000-8000-0000000000a1', '01936a00-0000-7000-8000-0000000000e1',
   (SELECT id FROM occupation WHERE name_en = 'Driver (car, pickup, microbus)'),
   'পিকআপ ড্রাইভার', 'Pickup driver',
   'টঙ্গী ও গাজীপুরে ডেলিভারি রুটে পিকআপ চালাতে হবে। সপ্তাহে একদিন ছুটি।',
   'Drive a pickup on delivery routes around Tongi and Gazipur. One day off a week.',
   'FULL_TIME', 'DAY', '08:00-18:00', 2, 16000, 20000, 'MONTH', 20000, 23.8916, 90.4023, 'টঙ্গী, গাজীপুর', 1,
   ARRAY(SELECT id FROM skill WHERE name_en IN ('Light vehicle driving', 'Knowing Dhaka roads')),
   '{DRIVING_PRO_LIGHT}', 'PUBLISHED', NULL, now() - interval '3 days', now() + interval '27 days', NULL,
   now() - interval '3 days', now() - interval '3 days'),
  ('01936a00-0000-7000-8000-0000000000a2', '01936a00-0000-7000-8000-0000000000e1',
   (SELECT id FROM occupation WHERE name_en = 'Security guard'),
   'নিরাপত্তা প্রহরী', 'Night security guard',
   'গুদামের রাতের পাহারা। থাকার ব্যবস্থা আছে।', 'Night watch at our warehouse. Accommodation provided.',
   'FULL_TIME', 'NIGHT', '20:00-06:00', 1, 12000, 15000, 'MONTH', 15000, 23.8950, 90.4100, 'টঙ্গী, গাজীপুর', 1,
   ARRAY(SELECT id FROM skill WHERE name_en IN ('Security patrol')),
   '{}', 'PAUSED', NULL, now() - interval '20 days', now() + interval '10 days', NULL,
   now() - interval '20 days', now() - interval '2 days'),
  ('01936a00-0000-7000-8000-0000000000a3', '01936a00-0000-7000-8000-0000000000e1',
   (SELECT id FROM occupation WHERE name_en = 'Driver (car, pickup, microbus)'),
   'মাইক্রোবাস ড্রাইভার', 'Microbus driver',
   'অফিস স্টাফ আনা-নেওয়ার জন্য মাইক্রোবাস চালক।', 'Microbus driver for office staff pick-up and drop-off.',
   'FULL_TIME', 'DAY', '07:00-17:00', 1, 18000, 22000, 'MONTH', 22000, 23.8740, 90.3980, 'উত্তরা, ঢাকা', 2,
   ARRAY(SELECT id FROM skill WHERE name_en IN ('Light vehicle driving')),
   '{DRIVING_PRO_LIGHT}', 'CLOSED', 'EXPIRED', now() - interval '45 days', now() - interval '15 days',
   now() - interval '15 days', now() - interval '45 days', now() - interval '15 days'),
  ('01936a00-0000-7000-8000-0000000000a4', '01936a00-0000-7000-8000-0000000000e1',
   (SELECT id FROM occupation WHERE name_en = 'Loader'),
   'লোডার', 'Loader', NULL, NULL,
   'DAILY', 'DAY', NULL, 3, 600, 800, 'DAY', 20800, NULL, NULL, NULL, NULL,
   '{}', '{}', 'DRAFT', NULL, NULL, NULL, NULL, now() - interval '1 day', now() - interval '1 day')
ON CONFLICT DO NOTHING;
