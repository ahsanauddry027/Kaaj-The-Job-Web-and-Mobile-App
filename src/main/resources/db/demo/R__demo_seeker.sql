-- Demo data for the job seeker side (dev profile only, loaded after R__demo_member3.sql).
-- Every demo account's password is Kaaj@12345.
--   rahim@demo.kaaj  driver in Tongi, shortlisted for Karim's pickup-driver post
--   sumi@demo.kaaj   sewing operator in Uttara, applied to the office-assistant post (viewed)
--   jamal@demo.kaaj  new driver in Gazipur, applied to the pickup-driver post

INSERT INTO app_user (id, email, email_verified, password_hash, role, locale) VALUES
  ('01936a00-0000-7000-8000-000000000004', 'sumi@demo.kaaj', true, '$2a$10$Ii5zHlbT0pd57srhGnjCEu40bxD5eBJGF.0x8Oe.QY1yz1YKPwJGa', 'SEEKER', 'bn'),
  ('01936a00-0000-7000-8000-000000000005', 'jamal@demo.kaaj', true, '$2a$10$Ii5zHlbT0pd57srhGnjCEu40bxD5eBJGF.0x8Oe.QY1yz1YKPwJGa', 'SEEKER', 'en')
ON CONFLICT DO NOTHING;

INSERT INTO seeker_profile (user_id, display_name, phone, occupation_ids, skill_ids, years_experience, education,
                            documents, home_latitude, home_longitude, home_area_name, travel_radius_km,
                            expected_wage, wage_period, shifts, created_at, updated_at)
VALUES
  ('01936a00-0000-7000-8000-000000000003', 'রহিম মিয়া', '01811-223344',
   ARRAY(SELECT id FROM occupation WHERE name_en = 'Driver (car, pickup, microbus)'),
   ARRAY(SELECT id FROM skill WHERE name_en IN ('Light vehicle driving', 'Knowing Dhaka roads')),
   5, 'SSC', '{DRIVING_PRO_LIGHT}', 23.890, 90.405, 'টঙ্গী, গাজীপুর', 10, 18000, 'MONTH', '{DAY}',
   now() - interval '10 days', now() - interval '2 days'),
  ('01936a00-0000-7000-8000-000000000004', 'সুমি আক্তার', '01911-556677',
   ARRAY(SELECT id FROM occupation WHERE name_en IN ('Sewing machine operator', 'Office assistant')),
   ARRAY(SELECT id FROM skill WHERE name_en IN ('Single-needle machine', 'Overlock machine')),
   3, 'HSC', '{}', 23.875, 90.400, 'উত্তরা, ঢাকা', 8, 12000, 'MONTH', '{DAY,ROTATING}',
   now() - interval '8 days', now() - interval '1 day'),
  ('01936a00-0000-7000-8000-000000000005', 'Jamal Uddin', NULL,
   ARRAY(SELECT id FROM occupation WHERE name_en = 'Driver (car, pickup, microbus)'),
   ARRAY(SELECT id FROM skill WHERE name_en IN ('Light vehicle driving')),
   1, NULL, '{}', 24.000, 90.420, 'গাজীপুর সদর', 15, 800, 'DAY', '{DAY,FLEXIBLE}',
   now() - interval '6 days', now() - interval '6 days')
ON CONFLICT DO NOTHING;

-- Two more live posts from Karim Transport, so search has something to find and sort.
INSERT INTO job (id, employer_id, occupation_id, title_bn, title_en, description_bn, description_en, job_type, shift,
                 hours, openings, wage_min, wage_max, wage_period, wage_monthly_max, latitude, longitude, area_name,
                 min_experience_years, skill_ids, required_documents, status, status_reason, published_at,
                 expires_at, closed_at, created_at, updated_at)
VALUES
  ('01936a00-0000-7000-8000-0000000000a5', '01936a00-0000-7000-8000-0000000000e1',
   (SELECT id FROM occupation WHERE name_en = 'Delivery rider'),
   'ডেলিভারি রাইডার', 'Delivery rider',
   'উত্তরা এলাকায় মোটরসাইকেলে পার্সেল ডেলিভারি। মোটরসাইকেল আমাদের।',
   'Deliver parcels around Uttara by motorcycle. We provide the motorcycle.',
   'DAILY', 'DAY', '09:00-18:00', 3, 700, 900, 'DAY', 23400, 23.8740, 90.3980, 'উত্তরা, ঢাকা', 0,
   ARRAY(SELECT id FROM skill WHERE name_en IN ('Motorcycle riding')),
   '{DRIVING_MOTORCYCLE}', 'PUBLISHED', NULL, now() - interval '1 day', now() + interval '29 days', NULL,
   now() - interval '1 day', now() - interval '1 day'),
  ('01936a00-0000-7000-8000-0000000000a6', '01936a00-0000-7000-8000-0000000000e1',
   (SELECT id FROM occupation WHERE name_en = 'Office assistant'),
   'অফিস সহকারী', 'Office assistant',
   'অফিসের কাগজপত্র গোছানো ও ফোন ধরা। দুপুর পর্যন্ত কাজ।',
   'Keep office papers in order and answer the phone. Mornings only.',
   'PART_TIME', 'FLEXIBLE', '10:00-14:00', 1, 6000, 8000, 'MONTH', 8000, 23.8916, 90.4023, 'টঙ্গী, গাজীপুর', 0,
   '{}', '{}', 'PUBLISHED', NULL, now() - interval '5 days', now() + interval '25 days', NULL,
   now() - interval '5 days', now() - interval '5 days')
ON CONFLICT DO NOTHING;

INSERT INTO application (id, job_id, seeker_id, status, message, match_score, created_at, updated_at) VALUES
  ('01936a00-0000-7000-8000-0000000000c1', '01936a00-0000-7000-8000-0000000000a1',
   '01936a00-0000-7000-8000-000000000003', 'SHORTLISTED', 'আমার ৫ বছর পিকআপ চালানোর অভিজ্ঞতা আছে।', 1.0,
   now() - interval '2 days', now() - interval '1 day'),
  ('01936a00-0000-7000-8000-0000000000c2', '01936a00-0000-7000-8000-0000000000a1',
   '01936a00-0000-7000-8000-000000000005', 'APPLIED', NULL, 0.69,
   now() - interval '1 day', now() - interval '1 day'),
  ('01936a00-0000-7000-8000-0000000000c3', '01936a00-0000-7000-8000-0000000000a6',
   '01936a00-0000-7000-8000-000000000004', 'VIEWED', 'I can start next week.', 0.85,
   now() - interval '3 days', now() - interval '2 days')
ON CONFLICT DO NOTHING;

INSERT INTO application_event (id, application_id, from_status, to_status, actor, note, created_at) VALUES
  ('01936a00-0000-7000-8000-0000000000d1', '01936a00-0000-7000-8000-0000000000c1', NULL, 'APPLIED', 'SEEKER',
   'আমার ৫ বছর পিকআপ চালানোর অভিজ্ঞতা আছে।', now() - interval '2 days'),
  ('01936a00-0000-7000-8000-0000000000d2', '01936a00-0000-7000-8000-0000000000c1', 'APPLIED', 'VIEWED', 'SYSTEM',
   NULL, now() - interval '36 hours'),
  ('01936a00-0000-7000-8000-0000000000d3', '01936a00-0000-7000-8000-0000000000c1', 'VIEWED', 'SHORTLISTED', 'EMPLOYER',
   'কাল সকাল ১০টায় অফিসে আসুন।', now() - interval '1 day'),
  ('01936a00-0000-7000-8000-0000000000d4', '01936a00-0000-7000-8000-0000000000c2', NULL, 'APPLIED', 'SEEKER',
   NULL, now() - interval '1 day'),
  ('01936a00-0000-7000-8000-0000000000d5', '01936a00-0000-7000-8000-0000000000c3', NULL, 'APPLIED', 'SEEKER',
   'I can start next week.', now() - interval '3 days'),
  ('01936a00-0000-7000-8000-0000000000d6', '01936a00-0000-7000-8000-0000000000c3', 'APPLIED', 'VIEWED', 'SYSTEM',
   NULL, now() - interval '2 days')
ON CONFLICT DO NOTHING;
