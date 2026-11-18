# Business model: Kaaj – The Job (web app + Android app)

**Kaaj – The Job** · Ahsan Habib (22201027) · written 18 November 2026

> This is the business plan for Kaaj as a real product, beyond the course. Prices and figures come from the project's own
> proposal (section 17) and context file (sections 3 and 25), plus market sources listed at the end. All money figures are
> **planning assumptions, not forecasts**. Prices of outside services change often: check them again before relying on them.

## 1. Summary

Kaaj connects Bangladeshi job seekers, especially blue-collar and first-time workers, with **verified employers**, in Bangla, on the phone.

- **Job seekers never pay.** Charging the people who most need work would repeat the broker (dalal) problem Kaaj exists to fix.
- **Employers pay**, with a free plan and a small fee per confirmed hire (**Tk 500**, the first hire free), or a monthly plan (**Tk 1,500** Business, **Tk 7,500** Pro), plus optional boosted posts.
- **Training and placement programmes pay** for placement tracking (from **Tk 60,000** a year per programme).
- **Two apps, one backend:** job seekers and small business owners mainly use the **Android app**; companies, HR staff, Kaaj's own
  moderators and training partners mainly use the **web app**. All payments happen on the **web**. Both apps run on the same Spring Boot backend,
  so every rule (verification, scam checks, matching) is built once.
- **Year-one scenario** (two districts, month 12): about **Tk 1,020,000** revenue against about **Tk 1,029,000** costs a month, roughly **break-even**.

## 2. The problem and the market

| Fact | Figure | Source |
|---|---|---|
| Employed people (Labour Force Survey 2024) | 69.09 million; about 2.62 million unemployed; rate **3.66%**, a three-year high | [1] |
| Informal work | About **84%** of employed people work informally; most jobs are never advertised (relatives, brokers, wall notices, Facebook groups) | [1][2] |
| Graduates | Highest unemployment of any education level in 2024: **13.5%** (about 885,000 people) | [2] |
| Young people not in education, employment or training | 8.56 million; 61.7% are women | [3] |
| Phones | **75.1%** of households have a smartphone, 89.6% of people use a mobile phone, but only **59.2%** use the internet | [4] |
| Overseas jobs | 1,128,423 workers went abroad for work in 2025 | [5] |
| Scams | Fake recruiters charge "processing fees"; thousands of digital-recruitment-scam victims | [6] |

**What this means for the two apps:** most job seekers have a smartphone but many are new to the internet. They need an **Android app in Bangla**
that works with little typing (and, later, by voice). Employers who hire often sit at a desk and need a **web app** for many posts, many applicants and
the paperwork.

### Why the current options fall short

| Option | Strength | Gap | Source |
|---|---|---|---|
| Bdjobs | Largest database; strong for office jobs | Built around CVs and English; job seekers pay for visibility (Bdjobs Pro Tk 99–199 a month); SME job posts from Tk 500 each, corporate packages from Tk 2,900 | [8][11] |
| Shomvob (blue-collar) | 1.8 million registered job seekers, 30,000+ hires | Employers pay 80–100% of the first month's salary per hire, plus Tk 6,000 a month for ads | [7] |
| Facebook groups and pages | Free, local, huge reach | Unverified, often no wage, no matching, scam-prone, phone numbers public | — |
| Brokers (dalals) and agencies | Personal trust; they do the legwork | Often charge the worker; unclear terms | — |
| Google Kormo | Reached about 4.5 million job seekers | **Closed on 30 June 2022**: a free job platform must earn money from employers early | [9] |

## 3. Customers, and which app each one uses

| Customer | Main app | Why that app | The other app |
|---|---|---|---|
| **Job seeker** (garment, delivery, security, retail, office assistant, fresh graduate) | **Android** | The phone is their only computer; camera, location for "jobs near me", and later voice and push alerts | Web: public job pages (`/jobs/{id}`) that open from Facebook links |
| **Small employer** (shop, transport, household, workshop) | **Android** | Runs the business from a phone; snaps the trade licence with the camera; posts a job in a minute | Web for the first long business profile, if easier |
| **Company HR / SME office** | **Web** | Desk and keyboard, many posts and applicants, team seats, exports | Android for checking applicants on the move |
| **Kaaj moderators and admins** (internal) | **Web** | Big screen to inspect documents and review queues | Android for a quick approval away from the desk |
| **Training and placement partners** (NGOs, technical institutes) | **Web** | Bulk invites, placement reports for their funders | — |
| **Field agents / Union Digital Centre operators** | Android or a shared PC | Help people sign up in person | — |

## 4. Value propositions

**For job seekers (free, always):**

- Real jobs from **verified** employers, with the **wage shown**; no "processing fees", ever.
- Bangla first; a profile built in minutes from the phone (later by speaking).
- Jobs ranked for them by occupation, skills, distance, shift and pay, with the reasons shown ("Near you · 2.1 km", "Wage meets your minimum").
- Every application tracked: applied → viewed → shortlisted → interview → hired, with the date of each step.
- Their phone number stays private until an employer shortlists them.

**For employers:**

- A complete job post in about a minute, on the phone or the web.
- Ranked applicants with a match score, instead of a pile of CVs or phone calls.
- A **verified badge** that brings more and better applicants.
- A price far below an agency: **Tk 500 per hire** is 4% of a Tk 12,500 monthly wage, against **Tk 10,000–12,500** (80–100% of the first month) through a blue-collar recruitment platform.

**For training partners:** proof of placement for funders, with each trainee's consent.

## 5. Business Model Canvas

| Block | Kaaj |
|---|---|
| **Customer segments** | Job seekers (blue-collar first, then graduates); small employers; SMEs and companies; training and placement programmes; later government, donors and researchers (insights) |
| **Value propositions** | Seekers: free, verified, Bangla, wage shown, tracked applications. Employers: one-minute posts, ranked applicants, verified badge, Tk 500 per hire. Partners: placement tracking and reports |
| **Channels** | Android app (Google Play; APK for pilots); web app (employers, admins, partners); verified jobs shared into Facebook groups with a link back; field staff at factory gates and markets; NGOs, technical institutes, Union Digital Centres; business associations |
| **Customer relationships** | Self-service on both apps; assisted sign-up through partners and field staff; a support line for employers; moderators who check every employer and every post |
| **Revenue streams** | Per-hire fee (Tk 500); Business plan (Tk 1,500/month); Pro plan (Tk 7,500/month); boosted posts (Tk 300 for 7 days, labelled "sponsored"); partner programmes (from Tk 60,000/year); later, anonymous labour-market reports |
| **Key resources** | One Spring Boot backend and PostgreSQL database; the two apps; outcome data (who got hired) that improves matching; the employer verification records and scam index; Bangla skills and occupation lists |
| **Key activities** | Verifying employers; screening posts for scams; matching; building both apps on one API; onboarding employers and partners; support |
| **Key partners** | Training NGOs and technical institutes; Union Digital Centres; business and industry associations; payment gateway (bKash, Nagad, cards); the government's Overseas Employment Platform (as a referral partner, not a competitor) |
| **Cost structure** | Team (largest cost); community and field operations; marketing; cloud hosting; AI services (small, about 3% of costs); SMS; payment gateway fees; one-time store and gateway set-up fees |

## 6. Revenue model and prices

| Stream | Price | What the customer gets |
|---|---|---|
| Free plan | Tk 0 | One active job, ranked applicants; **Tk 500 per confirmed hire**, the first hire free |
| Business plan | **Tk 1,500 a month** | Five active jobs, hires included, verified badge, talent search (20 invites a month), interview scheduling |
| Pro plan | **Tk 7,500 a month** | 25 active jobs, team seats, bulk hiring tools, exports, hiring analytics |
| Per-hire fee | **Tk 500 per hire** | Charged only when the person starts work; **refunded if they leave within 14 days** |
| Boosted post | **Tk 300 for 7 days** | Higher placement in matching feeds, labelled "sponsored"; the post must still pass every check |
| Training and placement partners | **From Tk 60,000 a year** per programme | Bulk invites, placement tracking with consent, reports for funders |
| Insights (later) | By agreement | Anonymous wage and demand reports |

**When does an employer move up a plan?** On the free plan, three hires a month cost 3 × Tk 500 = Tk 1,500, the same as the Business plan.
So an employer who hires **four or more people a month**, or needs more than one active job, saves money on Business. That is the natural upgrade moment,
and the web dashboard should point it out.

### Where payments happen: on the web, not in the Android app

- Job seekers never pay, so the Android app has **no purchases at all** for them.
- Employer plans, per-hire fees and boosts are paid on the **web app** with bKash, Nagad, Rocket or a card through a local payment gateway
  (aggregators such as SSLCommerz offer all of these through one integration).
- Google Play requires its own billing system when an app sells digital goods or services inside the app, but it allows "consumption-only"
  apps, where users sign in to use something paid for elsewhere [13][14]. The Android app therefore shows the employer's current plan but has no
  "buy" button. This also avoids the store's service fee on every plan. Store payment rules have been changing in 2026 in some regions, so check them again before launch.
- In the backend this means one more module later (`billing`): plans, invoices and gateway callbacks, used only by the web app's pages, while the
  Android app only reads "which plan am I on".

## 7. Unit economics

| Measure | Figure | How it is worked out |
|---|---|---|
| Agency cost for one Tk 12,500/month worker | Tk 10,000–12,500 | 80–100% of the first month's salary [7] |
| Kaaj per-hire fee | Tk 500 | 4% of that wage |
| Variable cost per hire | about Tk 55 | Gateway fee 2.5% of Tk 500 (Tk 12.50) + an equal share of SMS (Tk 31,500 ÷ 1,500 hires ≈ Tk 21) and AI (Tk 32,000 ÷ 1,500 ≈ Tk 21) |
| Contribution per paid hire | about Tk 445 | Tk 500 − Tk 55 |
| Average revenue per paying employer | about Tk 2,140 a month | (Tk 375,000 + Tk 225,000) ÷ 280 paying employers (year-one scenario) |
| Paying employers | 14% | 280 of 2,000 employers on Business or Pro |
| Revenue per hire, all streams | about Tk 680 | Tk 1,020,000 ÷ 1,500 hires a month |

## 8. Year-one scenario (two districts, month 12)

Assumptions from the proposal: **60,000** registered job seekers (18,000 active a month), **2,000** employers, about **1,500** hires a month.

| Revenue | Tk a month | Share |
|---|---:|---:|
| Business plan: 250 employers × Tk 1,500 | 375,000 | 36.8% |
| Pro plan: 30 employers × Tk 7,500 | 225,000 | 22.1% |
| Per-hire fees: 600 hires × Tk 500 (free-plan employers) | 300,000 | 29.4% |
| Boosted posts: 300 × Tk 300 | 90,000 | 8.8% |
| Training partners: 6 × Tk 60,000 a year | 30,000 | 2.9% |
| **Total revenue** | **1,020,000** | 100% |

| Costs | Tk a month |
|---|---:|
| Team: 7 people at an average of Tk 90,000 | 630,000 |
| Community and field operations | 150,000 |
| Marketing | 100,000 |
| Cloud hosting: 2 app instances, managed PostgreSQL, CDN and storage | 60,000 |
| AI services | 32,000 |
| SMS: 90,000 segments at about Tk 0.35 | 31,500 |
| Payment gateway fees, about 2.5% of revenue | 25,500 |
| **Total costs** | **1,029,000** |
| **Result** | **about −9,000: roughly break-even** |

Each new district adds revenue faster than cost, because the team, the backend, the two apps and the AI are shared; field operations are the cost
that grows with each district.

### What two apps cost compared with one

| Item | Web app | Android app | Shared (backend) |
|---|---|---|---|
| Building a feature | Screens in React | Screens in Compose | Rules, database and API built **once** |
| Release | Instant: deploy the jar | Store review and user updates; old versions stay in use for months | API must stay backward compatible (see guide 01, section 4.7) |
| One-time fees | A domain name | Google Play developer account: **US$25, one time** [12] | Payment gateway set-up (for example SSLCommerz Basic: Tk 25,500 one time, about 2.5% per payment) [15] |
| Running cost | Served by the same jar | — | Hosting, database, SMS, AI |

Splitting the audiences (seekers on Android, employers and staff on the web) keeps the double work small: each app only needs the screens its users really use.
For the course, the Android app has all three roles (job seeker, employer, admin) and the web app has the employer and admin side. After the course,
new employer and admin features go to the web first and job seeker features to Android first.

### Cost at each stage

| Stage | Monthly cost | What runs where |
|---|---|---|
| **Course demo (now)** | **Tk 0** | Laptop runs the backend and PostgreSQL; the web app from the same jar; Android debug build on an emulator or phone; Cloudflare quick tunnel for a public link [16]; GitHub free |
| **Pilot** (one district, a few hundred users) | Low: a small cloud server or free tier | One server running the jar over HTTPS; managed or self-run PostgreSQL; the Android app on Google Play |
| **Year one** (two districts) | About Tk 1,029,000 (table above) | Two app instances, managed PostgreSQL, CDN and storage |

## 9. Go-to-market

1. **Jobs first.** Before opening to job seekers, sign up **30–50 anchor employers** in **Gazipur and Dhaka North** that hire often: garment
   factories, transport and delivery firms, security companies and retail chains. Field staff help them fill in the business profile and verification
   on the **web** (or on their phone). A feed with real jobs on day one is what keeps a job seeker.
2. **Job seekers through partners.** Training NGOs and technical institutes bring graduates who need placements; Union Digital Centres help people
   sign up; hiring days at factory gates and markets onboard people in person with a QR code to the **Android** app.
3. **Use Facebook rather than fight it.** Verified jobs are shared into local Facebook groups as **web links** (`/jobs/{id}`); the page shows the wage
   and asks people to apply in the app, where their number stays private.
4. **Employers through the free plan.** Small businesses try it free and pay per hire; the web dashboard suggests Business once they hire four or more a month.
5. **Stay narrow.** Two districts and a few dozen occupations until both sides are healthy, measured every week by time to first response and hires per active job.

## 10. Why it is hard to copy

- **Outcome data:** every hire, decline and no-show improves matching; a new competitor starts without that history.
- **Trust:** verified employers, confirmed work records and a growing list of confirmed scams take time to build.
- **Bangla skills and voice:** occupation lists, synonyms and (later) voice data collected with consent.
- **Partners:** training programmes, Union Digital Centres and associations bring both sides and stay for the reports.
- **Cost:** a free, open-source stack and one backend for two apps let Kaaj charge a fraction of agency prices and still break even.

## 11. What to measure (KPIs)

| Area | Measure | Mainly from |
|---|---|---|
| Liquidity | Hires per active job; time from posting to first application; time to first employer response | Backend (both apps) |
| Job seekers | Weekly active job seekers; profile completeness; applications per seeker; share who get at least one shortlist | Android |
| Employers | Verified employers; posts per employer; free → Business conversion; monthly churn | Web |
| Trust and safety | Verification turnaround time; scam reports per 1,000 posts; posts blocked | Web (admin) |
| Fairness and impact | Hires of women and first-time workers; share of posts that show a wage; appointment letters issued | Backend |
| App quality | Crash-free sessions; Play Store rating; share of users on an app version older than 3 months | Android |
| Money | Monthly revenue by stream; revenue per hire; gateway fees; cost per district | Web (billing) |

## 12. Risks and how to handle them

| Risk | What to do |
|---|---|
| **Revenue arrives too slowly** (the Kormo lesson) | Charge employers from the first month through the per-hire fee; keep costs near zero until the pilot proves hires |
| **Cold start**: no jobs, no seekers | Anchor employers before launch; partners for job seekers; stay in two districts |
| **Scammers adapt** | Employer verification, post screening, user reports, moderators, and a clear zero-fee rule for job seekers |
| **Employers hire once, then go around Kaaj** | Keep the per-hire fee small; make repeat hiring easier inside Kaaj (saved applicants, verified badge, plans) |
| **Facebook is free** | Use it as a channel: share verified jobs there with a link back |
| **App store payment rules** | Take all payments on the web; the Android app stays consumption-only for paid plans |
| **Old Android versions break when the API changes** | Only add fields; version the API; a minimum-version check (guide 01, section 4.7) |
| **Two apps double the work** | One backend for every rule; each app only gets the screens its main users need |
| **Personal data** | Share a phone number only after shortlisting; consent for partner tracking; minimal data in AI calls; follow Bangladesh's data-protection law [10] |
| **People new to the internet** | Bangla first, little typing, assisted sign-up at Union Digital Centres; voice input later |

## 13. Roadmap

| Phase | When | Web app | Android app | Business goal |
|---|---|---|---|---|
| **0. Course** | Nov–Dec 2026 | Employer profile and verification, post and manage jobs, admin queue | Sign-in, employer and admin screens, job search, saved jobs, applying, applications timeline, applicants | Working demo: two apps on one backend |
| **1. Pilot** | First 3 months after launch | Public job pages for Facebook sharing, employer dashboard, moderator tools | Google Play release, push alerts, Bangla voice profile | 30–50 anchor employers; first few hundred hires |
| **2. Year one** | Months 4–12 | Payments (bKash, Nagad, cards), Business and Pro features, partner dashboard | Chat, interview reminders, works on slow networks | Roughly break-even in two districts |
| **3. Growth** | Year 2 onwards | Insights reports | More districts and occupations | Each new district profitable |

---

## Sources

From the project's context file (section 29) and proposal (section 17):

1. The Financial Express, *BD jobless rate hits 3-year high at 3.66% in 2024*: <https://thefinancialexpress.com.bd/home/bd-jobless-rate-hits-3-yr-high-at-366pc-in-2024>
2. CPD, *Employment growth remains stalled as opportunities fail to expand*: <https://cpd.org.bd/employment-growth-remains-stalled-as-opportunities-fail-to-expand/>
3. CPD, *Activating the potential of NEET youth*: <https://cpd.org.bd/activating-the-potential-of-neet-youth-a-case-for-bangladesh/>
4. Dhaka Tribune, BBS survey on mobile phone ownership: <https://www.dhakatribune.com/business/420730/bbs-survey-66%25-of-population-own-a-mobile-phone>
5. Bangladesh Monitor, *Around 1.13 million Bangladeshis fly abroad for jobs in 2025*: <https://bangladeshmonitor.com.bd/en/around-113-million-bangladeshis-fly-abroad-for-jobs-in-2025>
6. Global Voices, *When dreams meet digital recruitment scams*: <https://globalvoices.org/2025/11/30/when-dreams-meet-digital-recruitment-scams-bangladeshi-workers-in-crisis/>
7. Prothom Alo, *30,000 jobs made possible with Shomvob*: <https://en.prothomalo.com/bangladesh/lieb9fbcfk>
8. Future Startup, *Bdjobs Pro*: <https://futurestartup.com/2024/05/28/bdjobs-pro-radically-improving-job-search-experience-and-career-prospects-for-job-seekers-in-bangladesh/>
9. Dhaka Tribune, *Google's Kormo Jobs shuts down*: <https://www.dhakatribune.com/business/274349/google%E2%80%99s-kormo-jobs-shuts-down>
10. The Daily Observer, on the Personal Data Protection Act, 2026: <https://observerbd.com/news/578852>

Checked for this document (18 November 2026):

11. Future Startup, *Bdjobs launches recruitment solution for SMEs* (from Tk 500 per job post; Tk 2,900 standard package): <https://futurestartup.com/2024/05/13/bdjobs-launches-recruitment-solution-for-smes/>
12. Google Play Console Help, registration fee (US$25, one time): <https://support.google.com/googleplay/android-developer/answer/6112435>
13. Google Play policy, Payments: <https://play.google.com/about/monetization-ads/payments>
14. Google Play Console Help, understanding the Payments policy (consumption-only apps): <https://support.google.com/googleplay/android-developer/answer/10281818>
15. Quasa, SSLCommerz vs PortPos fee comparison (Bangla): <https://quasa.io/bn/media/sslcommerz-na-portpos-prthm-bchre-30012-taka-farak>
16. Cloudflare, Quick Tunnels: <https://developers.cloudflare.com/tunnel/get-started/quick-tunnels/>

Internal: `Doc/source-documents/Kaaj_The_Job_Project_Proposal_and_Technical_Design_1.docx` (section 17, Business model) and
`Doc/source-documents/Kaaj_The_Job_Project_Context_1.md` (sections 3, 25 and 26).
