# Builds docs/Kaaj_Member3_Build_Guide.pdf: HTML + CSS printed by headless Chrome (proper Bangla shaping).
import html, os, re, base64, pathlib

ROOT = pathlib.Path('/home/claude/kaaj')
SHOTS = pathlib.Path('/home/claude/shots')
FONT_CSS = ROOT / 'web/node_modules/@fontsource/noto-sans-bengali'

def esc(s): return html.escape(s, quote=False)

def lines(path, start, end=None):
    """Lines of a real project file, from the first line containing `start` to the first after it containing `end`."""
    text = (ROOT / path).read_text(encoding='utf-8').splitlines()
    i = next(n for n, l in enumerate(text) if start in l)
    if end is None:
        j = len(text) - 1
    else:
        j = next(n for n in range(i + 1, len(text)) if end in text[n])
    return '\n'.join(text[i:j + 1])

def code(src, title=None, lang=''):
    head = f'<div class="code-title">{esc(title)}</div>' if title else ''
    return f'<div class="code">{head}<pre><code>{esc(src)}</code></pre></div>'

def file_code(path, start=None, end=None, title=None):
    src = (ROOT / path).read_text(encoding='utf-8').rstrip('\n') if start is None else lines(path, start, end)
    return code(src, title or path)

def shell(src, title='PowerShell'):
    return code(src.strip('\n'), title)

def img(name, caption, width=100):
    data = base64.b64encode((SHOTS / name).read_bytes()).decode()
    return f'<figure style="width:{width}%"><img src="data:image/png;base64,{data}"><figcaption>{caption}</figcaption></figure>'

def table(headers, rows, widths=None, cls=''):
    cols = ''.join(f'<col style="width:{w}">' for w in widths) if widths else ''
    th = ''.join(f'<th>{h}</th>' for h in headers)
    trs = ''.join('<tr>' + ''.join(f'<td>{c}</td>' for c in r) + '</tr>' for r in rows)
    return f'<table class="{cls}"><colgroup>{cols}</colgroup><thead><tr>{th}</tr></thead><tbody>{trs}</tbody></table>'

def note(text, kind='info', title=None):
    t = f'<b>{title}</b> ' if title else ''
    return f'<div class="note {kind}">{t}{text}</div>'

def step(n, title, body):
    return f'<div class="step"><div class="step-head"><span class="num">{n}</span><h3>{title}</h3></div>{body}</div>'

# ------------------------------------------------------------------ diagrams (inline SVG)

def box(x, y, w, h, text, fill='#eef4ff', stroke='#1d4ed8', bold=False, size=12, sub=None, color='#0f172a'):
    weight = '700' if bold else '500'
    out = f'<rect x="{x}" y="{y}" width="{w}" height="{h}" rx="10" fill="{fill}" stroke="{stroke}" stroke-width="1.5"/>'
    ty = y + h / 2 + (size / 3 if not sub else -2)
    out += f'<text x="{x + w / 2}" y="{ty}" text-anchor="middle" font-size="{size}" font-weight="{weight}" fill="{color}">{esc(text)}</text>'
    if sub:
        out += f'<text x="{x + w / 2}" y="{y + h / 2 + 14}" text-anchor="middle" font-size="10" fill="#475569">{esc(sub)}</text>'
    return out

def arrow(x1, y1, x2, y2, label=None, dy=-6, color='#334155', dash=False, lx=None, ly=None):
    d = ' stroke-dasharray="5 4"' if dash else ''
    out = f'<line x1="{x1}" y1="{y1}" x2="{x2}" y2="{y2}" stroke="{color}" stroke-width="1.6" marker-end="url(#ah)"{d}/>'
    if label:
        mx = lx if lx is not None else (x1 + x2) / 2
        my = ly if ly is not None else (y1 + y2) / 2 + dy
        out += f'<text x="{mx}" y="{my}" text-anchor="middle" font-size="10.5" fill="#334155">{esc(label)}</text>'
    return out

def svg(w, h, body):
    return (f'<svg viewBox="0 0 {w} {h}" width="100%" xmlns="http://www.w3.org/2000/svg" font-family="Inter, Noto Sans Bengali, sans-serif">'
            '<defs><marker id="ah" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">'
            '<path d="M0,0 L10,5 L0,10 z" fill="#334155"/></marker></defs>' + body + '</svg>')

ARCH = svg(760, 330,
    box(20, 30, 170, 70, 'Browser', bold=True, sub='React + TypeScript app') +
    box(20, 140, 170, 70, 'Vite dev server', sub='localhost:5173 (development)') +
    arrow(105, 100, 105, 140, 'opens', dy=4, lx=130) +
    box(250, 20, 300, 290, '', fill='#f8fafc', stroke='#94a3b8') +
    f'<text x="400" y="42" text-anchor="middle" font-size="13" font-weight="700">Spring Boot 4.1 · Java 25 · localhost:1027</text>' +
    box(270, 60, 260, 44, 'job  (Feature 4)', fill='#dcfce7', stroke='#16a34a', sub='posts, status rules, expiry, events') +
    box(270, 120, 260, 44, 'employer  (Feature 3)', fill='#dcfce7', stroke='#16a34a', sub='profile, document, verification queue') +
    box(270, 180, 260, 44, 'identity  (shared setup)', sub='users, roles, session sign-in, audit log') +
    box(270, 240, 260, 44, 'shared', sub='FileStore, taxonomy, errors, common types') +
    arrow(400, 104, 400, 120) + arrow(400, 164, 400, 180) + arrow(400, 224, 400, 240) +
    arrow(190, 175, 250, 175, '/api → proxy', dy=-8) +
    box(600, 70, 140, 70, 'PostgreSQL', fill='#fef3c7', stroke='#d97706', bold=True, sub='database "kaaj" · Flyway') +
    box(600, 200, 140, 70, 'Disk', fill='#fef3c7', stroke='#d97706', bold=True, sub='data/uploads (documents)') +
    arrow(550, 105, 600, 105) + arrow(550, 235, 600, 235) +
    '<text x="250" y="325" font-size="10" fill="#475569">Arrows inside Spring Boot = allowed module dependencies (checked by ModularityTests)</text>')

VERIFY = svg(800, 220,
    box(20, 85, 130, 50, 'UNVERIFIED', fill='#f1f5f9', stroke='#64748b', bold=True) +
    box(250, 85, 130, 50, 'PENDING', fill='#fef3c7', stroke='#d97706', bold=True) +
    box(520, 15, 130, 50, 'VERIFIED', fill='#dcfce7', stroke='#16a34a', bold=True) +
    box(520, 160, 130, 50, 'REJECTED', fill='#fee2e2', stroke='#dc2626', bold=True) +
    arrow(150, 110, 250, 110, 'document uploaded') +
    arrow(380, 98, 520, 45, None) + '<text x="400" y="62" font-size="10.5" fill="#334155">admin approves</text>' +
    arrow(380, 122, 520, 172, None) + '<text x="455" y="136" font-size="10.5" fill="#334155">admin rejects (reason)</text>' +
    arrow(520, 200, 318, 137, None, dash=True) + '<text x="340" y="190" font-size="10.5" fill="#334155">fixes it and sends again</text>' +
    '<polyline points="650,40 720,40 720,110 382,110" fill="none" stroke="#334155" stroke-width="1.6" stroke-dasharray="5 4" marker-end="url(#ah)"/>' +
    '<text x="712" y="82" text-anchor="end" font-size="10.5" fill="#334155">business name</text>' +
    '<text x="712" y="96" text-anchor="end" font-size="10.5" fill="#334155">or document changed</text>')

JOBFLOW = svg(760, 250,
    box(10, 100, 110, 46, 'DRAFT', fill='#fef3c7', stroke='#d97706', bold=True) +
    box(190, 100, 120, 46, 'IN_REVIEW', fill='#e0f2fe', stroke='#0284c7', bold=True) +
    box(390, 100, 120, 46, 'PUBLISHED', fill='#dcfce7', stroke='#16a34a', bold=True) +
    box(390, 10, 120, 46, 'PAUSED', fill='#ffedd5', stroke='#ea580c', bold=True) +
    box(620, 100, 120, 46, 'CLOSED', fill='#e2e8f0', stroke='#475569', bold=True) +
    box(190, 196, 120, 46, 'BLOCKED', fill='#fee2e2', stroke='#dc2626', bold=True) +
    arrow(120, 123, 190, 123, 'submit') +
    arrow(310, 123, 390, 123, 'publish') +
    arrow(250, 146, 250, 196, 'screening (M3-6)', dy=0, lx=315, ly=175) +
    arrow(430, 100, 430, 56, 'pause', dy=0, lx=405, ly=82) +
    arrow(470, 56, 470, 100, 'resume', dy=0, lx=500, ly=82) +
    arrow(510, 123, 620, 123, 'close / 30 days') +
    arrow(510, 33, 640, 100, 'close', dy=-4) +
    arrow(680, 146, 680, 190, None) + f'<line x1="680" y1="190" x2="65" y2="190" stroke="#334155" stroke-width="1.6" stroke-dasharray="5 4"/>' +
    arrow(65, 190, 65, 148, None, dash=True) +
    '<text x="470" y="205" text-anchor="middle" font-size="10.5" fill="#334155">repost = new DRAFT copy</text>' +
    arrow(450, 146, 310, 146, 'edit', dy=14, dash=True))

SEQ = svg(760, 300,
    ''.join(f'<text x="{x}" y="18" text-anchor="middle" font-size="12" font-weight="700">{t}</text><line x1="{x}" y1="26" x2="{x}" y2="292" stroke="#cbd5e1" stroke-dasharray="4 4"/>'
            for x, t in [(80, 'Employer'), (280, 'employer module'), (480, 'job module'), (680, 'Admin')]) +
    arrow(80, 45, 478, 45, 'POST /employer/jobs?submit=true') +
    '<text x="560" y="64" font-size="10.5" fill="#92400e">→ DRAFT, AWAITING_VERIFICATION</text>' +
    arrow(80, 85, 278, 85, 'upload trade licence') +
    '<text x="290" y="104" font-size="10.5" fill="#92400e">→ PENDING (in the admin queue)</text>' +
    arrow(680, 130, 282, 130, 'APPROVE') +
    '<text x="290" y="149" font-size="10.5" fill="#166534">→ VERIFIED, audit log, trust 0.8</text>' +
    arrow(280, 172, 478, 172, 'EmployerVerified (after commit)') +
    '<text x="490" y="192" font-size="10.5" fill="#334155">submit waiting drafts → JobSubmitted</text>' +
    arrow(480, 212, 480, 230, None) +
    '<text x="490" y="230" font-size="10.5" fill="#334155">auto-publish stand-in (until M3-6)</text>' +
    '<text x="490" y="250" font-size="10.5" fill="#166534">→ PUBLISHED, expires in 30 days, JobPublished</text>' +
    arrow(478, 275, 82, 275, 'GET /employer/jobs/{id}: PUBLISHED with the Verified badge'))

# ------------------------------------------------------------------ content

S = []
add = S.append

add(f'''
<section class="cover">
  <div class="cover-top"><span class="logo">কাজ</span><span class="logo-sub">Kaaj – The Job</span></div>
  <h1>Member-3 build guide</h1>
  <p class="cover-sub">Employer profile and verification · Post and manage jobs<br>Spring Boot 4.1 (Java 25) + React with TypeScript</p>
  <table class="cover-meta">
    <tr><td>Course</td><td>CSE471 System Analysis and Design · Fall 2026 · Group 08 · Lab Section 01</td></tr>
    <tr><td>Member</td><td>Member-3: Ahsan Habib (22201027)</td></tr>
    <tr><td>Project folder</td><td><code>C:\\Users\\UseR\\Desktop\\Projects\\studentapi</code></td></tr>
    <tr><td>Date</td><td>18 November 2026</td></tr>
  </table>
  <div class="cover-box">
    <b>What this guide gives you</b>
    <ol>
      <li>What I found in your <i>Kaaj The Job</i> folder, and which two features are yours.</li>
      <li>How the finished code in <code>studentapi</code> works: modules, database, status flows, API and pages.</li>
      <li><b>Part A</b>: start the finished project on Windows 11 in about 15 minutes.</li>
      <li><b>Part B</b>: build the whole project yourself from an empty folder, step by step.</li>
      <li>A demo script, the tests, troubleshooting, and what is still left for later modules.</li>
    </ol>
  </div>
</section>
''')

add('''<h1 class="chapter">Contents</h1>
<ol class="toc">
<li>Summary</li><li>What is in your folders</li><li>Your two features</li><li>How the solution works</li>
<li>Part A · Start the finished project</li><li>Part B · Build it from scratch, step by step</li>
<li>Demo walkthrough (with screenshots)</li><li>Tests</li><li>Troubleshooting</li>
<li>What I checked, and what you should check first</li><li>Stand-ins and next steps</li>
<li>Appendix A · Every file and what it does</li><li>Appendix B · API examples</li><li>Appendix C · Settings</li>
</ol>''')

# 1 Summary
add('<h1 class="chapter">1. Summary</h1>')
add('''<p>Your requirements file (<code>CSE471_Group08_Functional_Requirements_Kaaj_The_Job.docx</code>) gives Member-3 four features.
The first two, both in Module 1, are now built end to end in <code>Desktop\\Projects\\studentapi</code>:</p>''')
add(table(['#', 'Feature', 'Backend (Spring Boot)', 'Frontend (TypeScript)'], [
    ['3', '<b>Employer profile and verification</b>', 'module <code>app.kaaj.employer</code> · table <code>employer</code> (V130) · 9 endpoints', 'Business profile page, verification panel, admin queue'],
    ['4', '<b>Post and manage jobs</b>', 'module <code>app.kaaj.job</code> · table <code>job</code> (V140) · 8 endpoints · hourly expiry job', '4-step "Post a job", My jobs, job page, dashboard'],
], ['5%', '25%', '40%', '30%']))
add('''<p>To make the two features usable on their own, the folder also has Member-3's shared-setup pieces (database migrations,
taxonomy search in Bangla, Banglish and English, the first admin, the audit log and the shared file upload) and a simple
email-and-password sign-in that <b>stands in</b> for Member-1's Google sign-in and Member-2's email codes.</p>''')
add(table(['Item', 'Result'], [
    ['Old Assignment 03 code (<code>com.example.studentapi</code>, <code>X-Employer-Id</code> header)', 'Removed from the folder. It is still in your Git history (branch <code>Main</code>).'],
    ['Deleted', '<code>target/</code>, <code>HELP.md</code>, old test uploads in <code>data/uploads</code>, the old Postman collection'],
    ['Kept as they were', '<code>.git</code>, <code>.env</code>, <code>mvnw</code>, <code>mvnw.cmd</code>, <code>.mvn</code>, <code>.vscode</code>, <code>Claude outputs/</code> (your report), <code>postman/sample-trade-licence.png</code>'],
    ['Added to <code>.env</code>', '<code>KAAJ_ADMIN_EMAIL=admin@kaaj.local</code> and <code>KAAJ_ADMIN_PASSWORD=Admin@12345</code>. Your <code>DB_PASSWORD</code> is unchanged.'],
    ['New', '84 Java files, 6 SQL migrations, 41 TypeScript files, a new Postman collection (31 requests), README, plan notes and this guide'],
    ['Not done', 'Nothing was committed or pushed to GitHub. Section 6, step 16 shows the Git commands.'],
], ['30%', '70%']))
add(note('''My workspace cannot reach Maven Central, so I could not compile the Java side here. I checked it in other ways: the Java
sources parse cleanly, the risky Spring Boot 4.1.2, Spring Security 7, Testcontainers 2.0.5 and Modulith 2.1.2 APIs were matched
against their real source code, every SQL migration ran on PostgreSQL, and the React app was built, unit-tested (23 tests) and
clicked through in Chrome. <b>Your first command should be <code>.\\mvnw.cmd test</code></b> (section 10). If it shows a compile
error, paste it to me and I will fix it.''', 'warn', 'Please read:'))

# 2 Folder analysis
add('<h1 class="chapter">2. What is in your folders</h1>')
add('<h2>2.1 Desktop\\Kaaj The Job (read only, nothing changed)</h2>')
add(table(['Item', 'What it is', 'How I used it'], [
    ['<code>Documents/CSE471_Group08_Functional_Requirements_Kaaj_The_Job.docx</code>', 'The final requirements: 12 features, 4 per member, plus the tech stack', '<b>Source of truth</b> for your two features'],
    ['<code>Documents/New folder/…Final_v2.docx</code>, <code>…Assignment 01….pdf</code>', 'Older versions of the same requirements', 'Checked for differences only'],
    ['<code>Documents/Kaaj_The_Job_Project_Context_1.md</code> (v1.5)', 'Single source of truth: architecture, data model (§15), API (§16), config (§17), decisions (§27)', 'Module layout, table design, endpoints, rules'],
    ['<code>Documents/Kaaj_Module_1/2/3_Build_Prompts.md</code>', 'Step-by-step prompts per feature (plan, data, logic, API, UI)', 'Acceptance checks for M1-3 and M2-3'],
    ['<code>Documents/Kaaj_The_Job_Project_Proposal_and_Technical_Design_1.docx</code>', '106-page full proposal (vision, Android app)', 'Background only'],
    ['<code>Documents/Kaaj_Windows11_Setup_Guide.pdf</code>', 'How to install JDK 25, Node, Docker, IntelliJ, VS Code', 'Part A and Part B point to it for installs'],
    ['<code>Documents/*.png</code> (3 Figma screens)', 'Employer dashboard, business info + verification, Post a job wizard + My jobs', 'The UI follows these screens'],
    ['<code>Kaaj The Job Database Schema/</code>', 'ERD (draw.io, SVG, PDF) generated from §15', 'Table and column names'],
    ['<code>Kaaj The Job WireFrame/</code>', '46-screen Android wireframe (E01–E14 employer, A04–A05 admin)', 'Screen flow, e.g. E14 "post stays a draft until approved"'],
], ['36%', '36%', '28%']))
add(note('''<b>Numbering differs between your files.</b> The final docx puts <i>Employer profile and verification</i> (feature 3) and
<i>Post and manage jobs</i> (feature 4) both in Module 1. Context file v1.5 still has job posts in Module 1 (M1-3) and verification
in Module 2 (M2-3). I followed the docx, so <b>only verified employers' posts go live from the start</b>, and the Flyway versions
are V130 (feature 3) and V140 (feature 4). Please update context file §7 and §22 so teammates use the same numbers.''', 'info'))
add('<h2>2.2 Desktop\\Projects\\studentapi (your Spring Boot project): before and after</h2>')
add(table(['Before', 'After'], [
    ['Spring Boot 4.1.2, Java 25, port 1027, PostgreSQL <code>student_db</code>, <code>ddl-auto=update</code>', 'Same Boot, Java and port; database <code>kaaj</code>; <b>Flyway</b> creates the tables (<code>ddl-auto=none</code>)'],
    ['One package <code>com.example.studentapi</code> (controller, service, repository, model, dto)', 'Modules <code>app.kaaj.shared</code>, <code>identity</code>, <code>employer</code>, <code>job</code>, each with a public API and an <code>internal</code> package'],
    ['The caller names itself in an <code>X-Employer-Id</code> header (no login)', 'Session cookie sign-in with CSRF protection and roles (EMPLOYER, ADMIN, SEEKER)'],
    ['No frontend (Postman only)', '<code>web/</code>: React 19 + TypeScript + Vite + Tailwind, Bangla first with English'],
    ['A draft goes live when its employer is approved', 'Kept: a complete post from an unverified employer waits and goes live on approval'],
], ['50%', '50%']))

# 3 Features
add('<h1 class="chapter">3. Your two features</h1>')
add('<h2>3.1 Feature 3 · Employer profile and verification</h2>')
add('<blockquote>Employers can create a business profile with the business name, type, address and contact person, and upload a trade licence or national ID. Admins approve or reject it, and approved employers get a "Verified" badge on all their job posts.</blockquote>')
add(table(['Requirement / acceptance check', 'How it is met', 'Proved by'], [
    ['Business profile: name, type, address, contact person', '<code>PUT /employer/profile</code>; also map pin, phone (Bangladeshi mobile check), email', 'EmployerTest, flow test, UI'],
    ['Upload trade licence or NID (JPG, PNG, PDF ≤ 3 MB)', 'Shared <code>FileStore</code>: real type by content (Apache Tika), size limit, images re-encoded (GPS data removed)', 'DiskFileStoreTest'],
    ['Admins approve or reject; a rejection has a reason the employer sees', '<code>POST /admin/employers/{id}/verification</code>; reason required (400 without)', 'Flow test, EmployerTest'],
    ['Only admins can open the document', '<code>EmployerDocumentAccessPolicy</code>: employer gets 404, admin 200', 'Flow test'],
    ['Every decision in the audit log', '<code>AuditLog.record(...)</code> in the same transaction', 'Code review'],
    ['Changing the name or document after approval → review again', '<code>Employer.updateDetails</code> / <code>attachDocument</code> → PENDING', 'EmployerTest'],
    ['"Verified" badge on all posts', 'Every job view carries <code>employer.verified</code>; the <code>VerifiedBadge</code> component shows it', 'Flow test, screenshots'],
], ['34%', '44%', '22%']))
add('<h2>3.2 Feature 4 · Post and manage jobs</h2>')
add('<blockquote>Verified employers can post a job with a title, category, number of openings, wage range (required), location, working hours, shift and requirements. They can edit, pause, close or repost it, and a post closes automatically after 30 days.</blockquote>')
add(table(['Requirement / acceptance check', 'How it is met', 'Proved by'], [
    ['All fields: title (bn/en), category, openings, wage range + period, location or remote, hours, shift, requirements (skills, documents, experience)', '<code>JobInput</code> → <code>JobRules</code> → <code>job</code> table', 'JobRulesTest, schema.test.ts'],
    ['Wage is required; min ≤ max', 'Java rule <b>and</b> database CHECK: no status after DRAFT without a wage', 'JobRulesTest, SQL check, flow test'],
    ['Only verified employers publish', 'Unverified → stays DRAFT (<code>AWAITING_VERIFICATION</code>), goes live on <code>EmployerVerified</code>', 'Flow test'],
    ['Edit, pause, resume, close, repost; delete drafts', '<code>PUT</code> / <code>PATCH {action}</code> / <code>DELETE</code>; one status table (<code>JobStatus.canMoveTo</code>)', 'JobStatusTest, JobTest, flow test'],
    ['Closes 30 days after publishing', '<code>expires_at</code> = first publish + 30 days; hourly <code>@Scheduled</code> job; editing never extends it', 'JobTest, flow test'],
    ['Another employer gets 404', 'Every lookup is <code>findByIdAndEmployerId</code>', 'Flow test'],
    ['Safe retries', '<code>Idempotency-Key</code> header on create', 'Flow test'],
], ['34%', '44%', '22%']))

# 4 How it works
add('<h1 class="chapter">4. How the solution works</h1>')
add('<h2>4.1 Architecture</h2>')
add(ARCH)
add('''<p>It is a <b>modular monolith</b> (context file §11): one Spring Boot app, split into packages that may only use each other's
top-level types. <code>job</code> asks <code>employer</code> through <code>EmployerDirectory</code>; <code>employer</code> never
touches <code>job</code>. When something happens that another module cares about, the module publishes an <b>event</b>
(<code>EmployerVerified</code>, <code>JobSubmitted</code>, <code>JobPublished</code>), handled after the database commit.
<code>ModularityTests</code> (Spring Modulith) fails the build if a module reaches into another's <code>internal</code> package or if two modules depend on each other.</p>''')
add('<h2>4.2 How one request flows through the layers</h2>')
add(table(['Layer', 'Example: "pause this job"', 'Files'], [
    ['React page', 'My jobs → ⋯ → Pause; <code>api.patch(\'/employer/jobs/{id}\', {action:\'PAUSE\'})</code> with the CSRF header', '<code>MyJobsPage.tsx</code>, <code>JobActions.tsx</code>, <code>lib/api.ts</code>'],
    ['Security', 'Session cookie → user; <code>/api/v1/employer/**</code> needs role EMPLOYER; CSRF token checked', '<code>SecurityConfig.java</code>'],
    ['Controller', 'Reads JSON, finds the signed-in user (<code>CurrentUser</code>)', '<code>EmployerJobController.java</code>'],
    ['Service', 'Loads the job <i>through the user\'s employer</i> (404 for others) and calls <code>job.pause(now)</code>', '<code>JobService.java</code>'],
    ['Entity', 'Checks the move in <code>JobStatus.canMoveTo</code>; 409 if not allowed', '<code>Job.java</code>, <code>JobStatus.java</code>'],
    ['Database', 'Hibernate saves the change at commit; CHECK constraints guard the rules again', '<code>V140__job.sql</code>'],
    ['Errors', 'Any failure → <code>ProblemDetail</code> JSON in Bangla or English (<code>Accept-Language</code>)', '<code>ApiExceptionHandler.java</code>, <code>messages*.properties</code>'],
], ['14%', '56%', '30%']))
add('<h2>4.3 Verification status flow (Feature 3)</h2>')
add(VERIFY)
add('<h2>4.4 Job status flow (Feature 4)</h2>')
add(JOBFLOW)
add('<h2>4.5 How the two features work together</h2>')
add(SEQ)
add('<h2>4.6 Database</h2>')
add(table(['Migration', 'Owner', 'Creates'], [
    ['<code>V1__core.sql</code>', 'Shared setup', '<code>pg_trgm</code> extension; <code>app_user</code>, <code>email_code</code>, <code>audit_log</code> (exactly as §15)'],
    ['<code>V2__taxonomy.sql</code>, <code>V3__seed_taxonomy.sql</code>', 'Shared setup (S2, Member-3)', '<code>occupation</code>, <code>skill</code> and their synonyms; 32 occupations and 55 skills in Bangla/English'],
    ['<code>V130__employer.sql</code>', 'Feature 3', '<code>employer</code> + queue index; CHECKs: PENDING needs submitted_at, REJECTED needs a reason'],
    ['<code>V140__job.sql</code>', 'Feature 4', '<code>job</code> + indexes; CHECKs: wage, description and location required after DRAFT; min ≤ max'],
    ['<code>db/demo/R__demo_member3.sql</code>', 'dev profile only', 'Demo employers (Karim Transport verified, Tongi Garments unverified), 4 posts, a job seeker'],
], ['32%', '20%', '48%']))
add('''<p>Differences from context file §15, all on purpose: map pins are stored as <code>latitude</code>/<code>longitude</code> (no
PostGIS needed yet, so your normal Windows PostgreSQL works; Member-1's distance search adds the PostGIS column later);
IDs are UUIDv7 made in Java (works on PostgreSQL 13+); drafts may be incomplete; and a few columns were added for the screens
(contact phone/email, document name, area name, timestamps). <code>docs/plans/M1-3_M1-4_Member3.md</code> lists them for the team.</p>''')
add('<h2>4.7 Pages</h2>')
add(table(['Route', 'Who', 'Page'], [
    ['<code>/login</code>, <code>/register</code>', 'anyone', 'Sign in / create account (stand-in)'],
    ['<code>/employer</code>', 'employer', 'Dashboard: business card, verification state, post counts, recent posts'],
    ['<code>/employer/profile</code>', 'employer', 'Feature 3: business information, map pin, document upload, verification panel'],
    ['<code>/employer/jobs/new</code>, <code>/employer/jobs/:id/edit</code>', 'employer', 'Feature 4: four steps (details, wage &amp; time, location, requirements) with a live preview card'],
    ['<code>/employer/jobs</code>', 'employer', 'My jobs: tabs per status with counts, search, ⋯ menu (edit, pause, resume, close, repost, delete)'],
    ['<code>/employer/jobs/:id</code>', 'employer', 'Job details and management, expiry date, actions panel'],
    ['<code>/admin/verification</code>', 'admin', 'Queue (waiting, approved, rejected), business details, document preview, approve/reject'],
    ['<code>/jobs/:id</code>', 'anyone', 'A published post with the Verified badge'],
], ['34%', '12%', '54%']))

# 5 Part A
add('<h1 class="chapter">5. Part A · Start the finished project</h1>')
add('<p>Use this when you just want to run what is already in <code>studentapi</code>. Commands are for <b>PowerShell</b> in Windows Terminal.</p>')
add(step(1, 'Check the tools', shell('''java -version        # must say 25 (Temurin)
node -v              # v24 or newer
npm -v
psql --version       # PostgreSQL 16+ (18 recommended); or use pgAdmin''') +
    '<p>Missing one? Install it as in <code>Kaaj_Windows11_Setup_Guide.pdf</code> §3, for example <code>winget install --id EclipseAdoptium.Temurin.25.JDK -e</code> and <code>winget install --id OpenJS.NodeJS.LTS -e</code>.</p>'))
add(step(2, 'Create the database "kaaj" (once)', '<p>Your old project used <code>student_db</code>; the new one gets its own empty database so Flyway can build it cleanly.</p>' + shell('''psql -U postgres -c "CREATE DATABASE kaaj;"''') +
    '<p>Or in pgAdmin: right-click <b>Databases → Create → Database…</b>, name <code>kaaj</code>, owner <code>postgres</code>, Save.</p>'))
add(step(3, 'Check .env', '<p>Open <code>studentapi\\.env</code>. It already has your <code>DB_PASSWORD</code> and the admin account I added. If your database or user is not the default, add <code>DB_NAME</code> / <code>DB_USER</code> (see <code>.env.example</code>).</p>' + code('''DB_PASSWORD=your-postgres-password
# First admin account (created at startup when there is no admin yet)
KAAJ_ADMIN_EMAIL=admin@kaaj.local
KAAJ_ADMIN_PASSWORD=Admin@12345''', '.env')))
add(step(4, 'Run the tests, then start the backend', shell('''cd C:\\Users\\UseR\\Desktop\\Projects\\studentapi
.\\mvnw.cmd test
.\\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"''') +
    '<p>The first run downloads Spring Boot and the libraries (a few minutes). Wait for <code>Started KaajApplication … port 1027</code> and <code>Created the first admin account: admin@kaaj.local</code>. Flyway has now created every table and the <code>dev</code> profile has loaded the demo data. API docs: <a>http://localhost:1027/swagger-ui.html</a>.</p>'))
add(step(5, 'Start the web app (second terminal)', shell('''cd C:\\Users\\UseR\\Desktop\\Projects\\studentapi\\web
npm install
npm run dev''') + '<p>Open <b>http://localhost:5173</b>. Vite forwards every <code>/api</code> call to Spring Boot on port 1027.</p>'))
add(step(6, 'Sign in', table(['Account', 'Password', 'What you see'], [
    ['<code>karim@demo.kaaj</code>', 'Kaaj@12345', 'Verified employer with 4 posts (published, paused, closed, draft)'],
    ['<code>tongigarments@demo.kaaj</code>', 'Kaaj@12345', 'Employer with a profile but no document (not verified)'],
    ['<code>admin@kaaj.local</code>', 'Admin@12345', 'The verification queue'],
], ['36%', '18%', '46%']) + '<p>Then follow the demo in section 7.</p>'))

# 6 Part B
add('<h1 class="chapter">6. Part B · Build it from scratch, step by step</h1>')
add('''<p>This is the order I built it in. Every file named here is in <code>studentapi</code>; open it next to this guide and type or
copy it in the same order. After each group of steps there is a check, so you always know the project still works.</p>''')
add(step(1, 'Install the tools', '<p>JDK 25 (Temurin), Node.js 24 or 26 LTS, PostgreSQL (EDB installer with pgAdmin), Git, IntelliJ IDEA (backend) and VS Code (frontend). The exact <code>winget</code> commands and checks are in <code>Kaaj_Windows11_Setup_Guide.pdf</code> §3. Docker Desktop is only needed for the automatic API tests (step 14).</p>'))
add(step(2, 'Create the Spring Boot project', '''<p>Go to <b>start.spring.io</b> and choose:</p>''' + table(['Setting', 'Value'], [
    ['Project / Language', 'Maven / Java'], ['Spring Boot', '4.1.2'], ['Group / Artifact / Package', '<code>app.kaaj</code> / <code>kaaj</code> / <code>app.kaaj</code>'],
    ['Packaging / Java', 'Jar / 25'],
    ['Dependencies', 'Spring Web MVC, Validation, Spring Security, Spring Data JPA, Flyway Migration, PostgreSQL Driver, Spring Boot DevTools, Testcontainers'],
], ['35%', '65%']) + '''<p>Generate, unzip into a short path without spaces (for example <code>C:\\dev\\kaaj</code>), and open it in IntelliJ
(set the SDK to Temurin 25). Your existing <code>studentapi</code> was created the same way, which is why it keeps its own <code>mvnw</code>.</p>'''))
add(step(3, 'Complete pom.xml', '<p>Spring Boot 4 renamed starters (context file §12.2): the web starter is <code>spring-boot-starter-webmvc</code>, Flyway needs <code>spring-boot-starter-flyway</code> plus <code>flyway-database-postgresql</code>, and each test area has its own starter. Add Apache Tika (file types), springdoc (Swagger UI), Spring Modulith (module test) and Testcontainers:</p>' + code(lines('pom.xml', '<properties>', '</dependencyManagement>') + '\n\n' + lines('pom.xml', '    <dependencies>', '    </dependencies>'), 'pom.xml (properties, BOM and dependencies)') +
    '<p>The <code>web</code> profile at the end of <code>pom.xml</code> builds the React app into the jar (step 15).</p>'))
add(step(4, 'Database, .env and application.properties', '<p>Create the <code>kaaj</code> database (Part A step 2) and a <code>.env</code> next to <code>pom.xml</code> (copy <code>.env.example</code>). Then replace <code>src/main/resources/application.properties</code>:</p>' + file_code('src/main/resources/application.properties') +
    '<p>Also add <code>application-dev.properties</code> (demo data, more logging, a default admin) and the two message files <code>messages.properties</code> (English) and <code>messages_bn.properties</code> (Bangla).</p>'))
add(step(5, 'Create the module packages', code('''src/main/java/app/kaaj/
├── KaajApplication.java          @SpringBootApplication @ConfigurationPropertiesScan @EnableScheduling
├── shared/      + internal/      common types, errors, FileStore, taxonomy
├── identity/    + internal/      users, roles, security, admin, audit log
├── employer/    + internal/      Feature 3
└── job/         + internal/      Feature 4''', 'Package layout') +
    '<p>Rule: put everything in <code>internal</code> unless another module needs it. Public (top-level) types are the module\'s API: for example <code>EmployerDirectory</code>, <code>EmployerSummary</code>, <code>EmployerVerified</code>, <code>JobSubmitted</code>.</p>'))
add(step(6, 'Flyway migrations', '<p>Create <code>src/main/resources/db/migration/</code> with <code>V1__core.sql</code>, <code>V2__taxonomy.sql</code>, <code>V3__seed_taxonomy.sql</code>, then your two feature tables. Never edit a migration after it has run; add a new one instead.</p>' +
    file_code('src/main/resources/db/migration/V130__employer.sql', 'CREATE TABLE employer', None, 'V130__employer.sql (Feature 3)') +
    file_code('src/main/resources/db/migration/V140__job.sql', 'CREATE TABLE job', None, 'V140__job.sql (Feature 4)') +
    note('<b>Check:</b> start the app once (<code>.\\mvnw.cmd spring-boot:run</code>). In pgAdmin, <code>kaaj → Schemas → public → Tables</code> now shows <code>app_user, audit_log, email_code, employer, flyway_schema_history, job, occupation, …</code>.', 'ok')))
add(step(7, 'The shared module', table(['File', 'What it does'], [
    ['<code>UuidV7</code>, <code>WagePeriod</code>, <code>Wages</code>, <code>DocumentCode</code>, <code>Term</code>, <code>Cursor</code>, <code>CursorPage</code>, <code>Json</code>', 'Common types: time-ordered IDs, the monthly-wage rule (hour×208, day×26, week×52÷12), licence codes, pagination'],
    ['<code>KaajException</code>', 'A business error: HTTP status + message code (+ field errors)'],
    ['<code>FileStore</code>, <code>FileKind</code>, <code>FileAccessPolicy</code> → <code>internal/DiskFileStore</code>, <code>FileController</code>', 'Shared upload: real type check with Tika, size limits, image re-encoding, private serving'],
    ['<code>Taxonomy</code> → <code>internal/JdbcTaxonomy</code>, <code>TaxonomyController</code>', 'Spelling-tolerant search with <code>pg_trgm</code> similarity over names and synonyms'],
    ['<code>internal/ApiExceptionHandler</code>', 'Every error → <code>ProblemDetail</code> JSON in the request\'s language'],
    ['<code>internal/SharedConfiguration</code>, <code>SpaForwardController</code>', 'The UTC clock bean; sends browser routes to <code>index.html</code> when the app is one jar'],
], ['48%', '52%'])))
add(step(8, 'The identity module and security', '<p>Entity <code>AppUser</code> (table <code>app_user</code>), its repository, <code>KaajUserDetailsService</code> (the session principal\'s name is the user ID), <code>CurrentUser</code>, <code>AuditLog</code>/<code>JdbcAuditLog</code>, <code>UserDirectory</code>, <code>FirstAdminInitializer</code> (creates the admin from <code>.env</code>), <code>AuthController</code> and the security chain:</p>' +
    file_code('src/main/java/app/kaaj/identity/internal/SecurityConfig.java', 'SecurityFilterChain securityFilterChain', 'return http.build();', 'identity/internal/SecurityConfig.java') +
    '<p><code>csrf.spa()</code> keeps the token in the <code>XSRF-TOKEN</code> cookie; the web app sends it back in the <code>X-XSRF-TOKEN</code> header on every change. Role rules are by path; ownership is checked in the services.</p>'))
add(step(9, 'Feature 3 backend: employer profile and verification', table(['Order', 'File', 'Purpose'], [
    ['1', '<code>employer/EmployerKind</code>, <code>VerificationStatus</code>', 'Business types; the status table <code>canMoveTo</code>'],
    ['2', '<code>employer/EmployerSummary</code>, <code>EmployerDirectory</code>, <code>EmployerVerified</code>, <code>EmployerRejected</code>', 'Public API and events'],
    ['3', '<code>internal/Employer</code>', 'Entity; all status changes happen here'],
    ['4', '<code>internal/EmployerRepository</code>', 'Queries incl. the keyset-paged admin queue'],
    ['5', '<code>internal/EmployerProfileService</code>', 'Save profile, upload document, resubmit; implements <code>EmployerDirectory</code>'],
    ['6', '<code>internal/EmployerVerificationService</code>', 'Queue, approve, reject + audit log + events'],
    ['7', '<code>internal/EmployerDocumentAccessPolicy</code>', 'Documents: admins only'],
    ['8', '<code>internal/EmployerProfileController</code>, <code>AdminEmployerController</code>', 'REST endpoints'],
], ['8%', '50%', '42%']) + file_code('src/main/java/app/kaaj/employer/internal/Employer.java', 'public void updateDetails', 'public List<String> missingForReview', 'employer/internal/Employer.java (the status rules)')))
add(step(10, 'Feature 4 backend: post and manage jobs', table(['Order', 'File', 'Purpose'], [
    ['1', '<code>job/JobStatus</code>, <code>JobType</code>, <code>Shift</code>', 'Status table and enums'],
    ['2', '<code>job/JobSubmitted</code>, <code>JobPublished</code>, <code>JobSummary</code>, <code>JobDirectory</code>, <code>JobModeration</code>', 'Public API and events (M3-6 uses <code>JobModeration</code>)'],
    ['3', '<code>internal/JobDetails</code>, <code>Job</code>', 'Form content; entity with publish/pause/resume/close/repost'],
    ['4', '<code>internal/JobRules</code>', 'Draft rules and submit rules (field → message code)'],
    ['5', '<code>internal/JobRepository</code>, <code>JobProperties</code>', 'Queries; settings <code>kaaj.jobs.*</code>'],
    ['6', '<code>internal/JobService</code>', 'Create, edit, actions, list, counts, expiry, waiting drafts'],
    ['7', '<code>internal/JobEventListeners</code>, <code>JobExpiryScheduler</code>', 'Auto-publish stand-in; drafts go live on approval; hourly expiry'],
    ['8', '<code>internal/JobViews</code>, <code>EmployerJobController</code>, <code>PublicJobController</code>', 'JSON shapes and endpoints'],
], ['8%', '50%', '42%']) + file_code('src/main/java/app/kaaj/job/internal/JobService.java', 'private void submit', 'private void requireVerified', 'job/internal/JobService.java (submitting)') +
    file_code('src/main/java/app/kaaj/job/internal/JobEventListeners.java', '@TransactionalEventListener', None, 'job/internal/JobEventListeners.java')))
add(step(11, 'Try the API', '<p>Start the backend and open <b>http://localhost:1027/swagger-ui.html</b>, or import <code>postman/Kaaj_Member3.postman_collection.json</code> into Postman and run its four folders in order (sign up → profile → admin approves → manage the post). The first request of each folder stores the CSRF token; Postman keeps the session cookie by itself.</p>' +
    note('<b>Check:</b> the Postman runner shows every test green, including the refusals: no wage → 400, reject without reason → 400, pause twice → 409.', 'ok')))
add(step(12, 'Create the React + TypeScript app', shell('''cd C:\\Users\\UseR\\Desktop\\Projects\\studentapi
npm create vite@latest web -- --template react-ts
cd web
npm install react-router @tanstack/react-query react-hook-form zod @hookform/resolvers i18next react-i18next leaflet lucide-react @fontsource/noto-sans-bengali tailwindcss @tailwindcss/vite
npm install -D @types/leaflet vitest @testing-library/react @testing-library/jest-dom @testing-library/user-event jsdom''') +
    '<p>Then: delete the template\'s <code>App.tsx</code>, <code>App.css</code> and <code>assets/</code>; add <code>"resolveJsonModule": true</code> and <code>"vitest/globals"</code> to <code>tsconfig.app.json</code>; add the <code>test</code> scripts to <code>package.json</code>; and set up <code>vite.config.ts</code>:</p>' + file_code('web/vite.config.ts')))
add(step(13, 'Frontend files, in order', table(['Folder', 'Files', 'Purpose'], [
    ['<code>src/</code>', '<code>index.css</code>, <code>main.tsx</code>, <code>router.tsx</code>', 'Tailwind + Bangla font + colours; React Query; the routes'],
    ['<code>src/i18n/</code>', '<code>index.ts</code>, <code>bn.json</code>, <code>en.json</code>', 'Bangla by default, English switch, remembered choice'],
    ['<code>src/lib/</code>', '<code>types.ts</code>, <code>api.ts</code>, <code>format.ts</code>, <code>useLang.ts</code>, <code>cn.ts</code>', 'API types; fetch client with CSRF; ৳ and Bangla digits'],
    ['<code>src/components/</code>', '<code>ui.tsx</code>, <code>AppLayout.tsx</code>, <code>StatusBadge.tsx</code>, <code>MapPicker.tsx</code>, <code>FileUpload.tsx</code>, <code>TaxonomyPicker.tsx</code>, <code>Toast.tsx</code>', 'Shared building blocks (Leaflet map, upload with checks, Verified badge)'],
    ['<code>features/auth/</code>', '<code>useCurrentUser.ts</code>, <code>AuthPages.tsx</code>, <code>RequireRole.tsx</code>', 'Sign-in (stand-in) and role guards'],
    ['<code>features/employer-profile/</code>', '<code>api.ts</code>, <code>schema.ts</code>, <code>VerificationPanel.tsx</code>, <code>EmployerProfilePage.tsx</code>', '<b>Feature 3</b> UI'],
    ['<code>features/employer-jobs/</code>', '<code>api.ts</code>, <code>schema.ts</code>, <code>JobCard.tsx</code>, <code>JobActions.tsx</code>, <code>JobFormPage.tsx</code>, <code>MyJobsPage.tsx</code>, <code>JobDetailPage.tsx</code>, <code>DashboardPage.tsx</code>', '<b>Feature 4</b> UI'],
    ['<code>features/admin-trust/</code>, <code>jobs/</code>, <code>home/</code>', '<code>AdminVerificationPage.tsx</code>, <code>PublicJobPage.tsx</code>, <code>HomePage.tsx</code>', 'Admin queue; public job page; home'],
], ['24%', '46%', '30%']) + file_code('web/src/lib/api.ts', 'async function request', 'return data as T', 'web/src/lib/api.ts (CSRF and errors)') +
    file_code('web/src/features/employer-jobs/schema.ts', 'export function validateJobForm', 'return errors', 'web/src/features/employer-jobs/schema.ts (same rules as JobRules.java)')))
add(step(14, 'Run all the tests', shell('''cd C:\\Users\\UseR\\Desktop\\Projects\\studentapi
.\\mvnw.cmd test
cd web
npm test''') + '<p>Details in section 8.</p>'))
add(step(15, 'Build one jar (optional, for the demo PC)', shell('''.\\mvnw.cmd -Pweb package
java -jar target\\kaaj-0.1.0-SNAPSHOT.jar''') + '<p>The <code>web</code> Maven profile installs Node, runs <code>npm ci</code> and <code>npm run build</code>, and copies <code>web/dist</code> into the jar. One process then serves the app and the API at http://localhost:1027.</p>'))
add(step(16, 'Save it in Git', shell('''cd C:\\Users\\UseR\\Desktop\\Projects\\studentapi
git switch -c feat/m3-employer-verification-and-jobs
git add -A
git commit -m "Member-3: employer profile and verification, post and manage jobs (Spring Boot + React)"
git push -u origin feat/m3-employer-verification-and-jobs''') + '<p><code>.env</code>, <code>data/</code>, <code>web/node_modules</code> and <code>Claude outputs/</code> are git-ignored. If Git complains about <code>index.lock</code> or <code>HEAD.lock</code>, close other Git tools and delete those lock files inside <code>.git</code>. The <code>stale-*-lock</code> files there are harmless leftovers.</p>'))

# 7 Demo
add('<h1 class="chapter">7. Demo walkthrough</h1>')
add('<p>These screenshots come from the real React app running in Chrome. (Map tiles are grey here only because my workspace has no internet; on your PC they show OpenStreetMap.)</p>')
demo = [
    ('02-dashboard.png', 'Sign in as karim@demo.kaaj: the employer dashboard with the verified business and post counts.'),
    ('03-profile-verified.png', 'Feature 3: business information (left) and the verification panel (right), as in the Figma screen.'),
    ('06-wizard-step1-errors.png', 'Post a job, step 1: pressing Next on an empty form shows each problem next to its field.'),
    ('08-wizard-step2.png', 'Step 2: wage range (required), pay period, shift and working hours.'),
    ('09-wizard-step3.png', 'Step 3: tap the map to pin the location, or mark the job remote.'),
    ('10-wizard-step4.png', 'Step 4: skills from the taxonomy, required licences, experience; the preview shows the Verified badge.'),
    ('11-job-detail-published.png', 'Published straight away (verified employer): expiry date 30 days later and the actions panel.'),
    ('05-my-jobs-menu.png', 'My jobs: tabs with counts per status and the ⋯ menu (edit, pause, close).'),
    ('12-profile-pending.png', 'tongigarments@demo.kaaj after uploading its trade licence: waiting for review.'),
    ('13-draft-unverified.png', 'Its post waits as a draft until the business is approved.'),
    ('14-admin-queue.png', 'admin@kaaj.local: the queue, the business details and the document preview.'),
    ('15-admin-reason-required.png', 'Rejecting without a reason is refused; quick reasons fill the box.'),
    ('16-admin-approved.png', 'After approval the employer moves to the Approved tab, and its waiting posts go live.'),
    ('17-public-job.png', 'The public job page with the Verified badge.'),
    ('18-profile-english.png', 'The same profile page in English (language switch at the bottom left).'),
]
for name, cap in demo:
    add(img(name, cap))
add(img('19-mobile-my-jobs.png', 'My jobs on a phone-sized screen (390 px).', 45))

# 8 Tests
add('<h1 class="chapter">8. Tests</h1>')
add(table(['Test', 'Kind', 'What it proves'], [
    ['<code>ModularityTests</code>', 'Spring Modulith', 'No module uses another module\'s internal classes; no cycles'],
    ['<code>EmployerTest</code>', 'Unit (9 tests)', 'Every verification move, the reason rule, back-to-PENDING on name/document change'],
    ['<code>JobStatusTest</code>, <code>JobTest</code>, <code>JobRulesTest</code>', 'Unit', 'Every allowed and refused job move; 30-day window kept on edit; resume after expiry closes; wage, location, hours rules'],
    ['<code>WagesTest</code>, <code>DocumentCodeTest</code>, <code>UuidV7Test</code>, <code>CursorTest</code>, <code>JsonTest</code>', 'Unit', 'Shared types'],
    ['<code>DiskFileStoreTest</code>', 'Unit', 'Wrong type with a fake extension refused; size limit; GPS/EXIF removed; path tricks refused'],
    ['<code>Member3FlowTests</code>', 'End-to-end API (MockMvc + real PostgreSQL 18 in Docker)', 'The whole story: profile → waiting post → upload → admin approves → post live with badge; other employer gets 404; pause/resume/close/repost; no wage → 400; idempotency; expiry; taxonomy "draiver/ড্রাইভার/chalok"; CSRF required'],
    ['<code>web: npm test</code> (23 tests)', 'Vitest', 'Job form rules match the server; phone and email checks; ৳ and Bangla digits; the verification banner in every state; bn/en translation keys match; upload checks'],
], ['30%', '22%', '48%']))
add(note('<code>Member3FlowTests</code> starts PostgreSQL 18 in Docker (Testcontainers). Without Docker Desktop running, it is <b>skipped</b>, not failed, and the other tests still run.', 'info'))

# 9 Troubleshooting
add('<h1 class="chapter">9. Troubleshooting</h1>')
add(table(['Problem', 'Fix'], [
    ['<code>Could not resolve placeholder \'DB_PASSWORD\'</code>', 'Run the command from the <code>studentapi</code> folder (where <code>.env</code> is), and check that <code>.env</code> has <code>DB_PASSWORD=…</code> with no comment on the same line.'],
    ['<code>FATAL: database "kaaj" does not exist</code>', 'Create it (Part A step 2), or set <code>DB_NAME</code> in <code>.env</code>.'],
    ['<code>password authentication failed for user "postgres"</code>', 'The password in <code>.env</code> is not your PostgreSQL password; set <code>DB_USER</code> if you use another user.'],
    ['Flyway: <code>Found non-empty schema(s) "public" but no schema history table</code>', 'You pointed it at <code>student_db</code>, which already has tables. Use the new empty <code>kaaj</code> database.'],
    ['Flyway: <code>permission denied to create extension "pg_trgm"</code>', 'Connect as <code>postgres</code>, or run <code>CREATE EXTENSION pg_trgm;</code> in the <code>kaaj</code> database as a superuser once.'],
    ['<code>Port 1027 was already in use</code>', 'Another copy is running; stop it, or start with <code>$env:PORT=8081</code> and set <code>KAAJ_BACKEND</code> for Vite.'],
    ['No admin account / can\'t sign in as admin', 'Check <code>KAAJ_ADMIN_EMAIL</code> and <code>KAAJ_ADMIN_PASSWORD</code> (8+ characters) in <code>.env</code>; the admin is created at startup only when none exists.'],
    ['Postman: <code>403 Forbidden</code> on POST/PUT/PATCH', 'Run "Get CSRF token" first (and again after signing out); the request must send <code>X-XSRF-TOKEN: {{xsrf}}</code>.'],
    ['Web app shows "Can\'t reach the server"', 'The backend isn\'t running on 1027, or it is still starting.'],
    ['<code>npm</code>: "running scripts is disabled"', '<code>Set-ExecutionPolicy -Scope CurrentUser RemoteSigned</code> once.'],
    ['A Java compile error on the first <code>mvnw test</code>', 'Copy the first error (file, line, message) and send it to me. Section 10 explains why this is possible.'],
    ['Map is grey', 'No internet, or a firewall blocks <code>tile.openstreetmap.org</code>; the pin still works.'],
], ['40%', '60%']))

# 10 Verification
add('<h1 class="chapter">10. What I checked, and what you should check first</h1>')
add(table(['Part', 'Checked here', 'How'], [
    ['SQL migrations, constraints, demo data, taxonomy search', '<b>Yes</b>', 'Ran on PostgreSQL with every CHECK tested (wage, location, rejection reason) and the Bangla/Banglish search queries'],
    ['React + TypeScript app', '<b>Yes</b>', '<code>tsc</code> clean, production build OK, 23 Vitest tests pass, whole flow clicked through in Chrome (screenshots in section 7)'],
    ['Java syntax and our own types', '<b>Yes</b>', 'All 84 files compiled with <code>javac</code> without the libraries; the only errors left are "unknown library class", as expected'],
    ['Spring Boot 4.1.2 / Security 7 / Testcontainers 2.0.5 / Modulith 2.1.2 APIs', '<b>Yes</b>', 'Checked against their real source code on GitHub (for example <code>csrf.spa()</code>, <code>DaoAuthenticationProvider(UserDetailsService)</code>, test package names)'],
    ['Image re-encoding (EXIF removal)', '<b>Yes</b>', 'Ran the same Java code on a JPEG with a GPS tag'],
    ['Full Maven build and Java tests', '<b>No</b>: Maven Central is blocked in my workspace', 'You: <code>.\\mvnw.cmd test</code>'],
], ['34%', '18%', '48%']))
add(note('If <code>.\\mvnw.cmd test</code> passes, start the app (Part A) and run the Postman collection. Together they cover every acceptance check in section 3.', 'ok', 'Your first check:'))

# 11 Next
add('<h1 class="chapter">11. Stand-ins and next steps</h1>')
add(table(['Stand-in in this code', 'Replaced by', 'What to change then'], [
    ['Email + password sign-in and <code>/auth/register</code>', 'Member-1 Google sign-in (S4), Member-2 email codes (S5)', '<code>kaaj.auth.simple-sign-up=false</code>; keep <code>CurrentUser</code> (same session)'],
    ['Submitted posts are published at once', 'Your M3-6 scam screening', '<code>kaaj.jobs.auto-publish=false</code>; the trust module listens to <code>JobSubmitted</code> and calls <code>JobModeration</code>'],
    ['Applicants count shows "—"', 'Member-2 applying (M2-2), Member-1 ranked applicants', 'Fill it from the hiring module'],
    ['Lat/lng only, no PostGIS', 'Member-1 job search (M2-1)', 'Their migration adds <code>location geography</code> generated from latitude/longitude'],
], ['32%', '30%', '38%']))
add('''<p><b>Your next two features</b> build directly on this code: the <b>AI job-post assistant (M3-5)</b> fills the same
<code>JobDetails</code>/<code>JobFormValues</code> and shows <code>JobFormPage</code> pre-filled; <b>scam screening (M3-6)</b>
adds a <code>trust</code> module that listens to <code>JobSubmitted</code> and decides publish, review or block through
<code>JobModeration</code>. Also tell the team about the context-file updates in <code>docs/plans/M1-3_M1-4_Member3.md</code>.</p>''')

# Appendix A
add('<h1 class="chapter">Appendix A · Every file and what it does</h1>')
def describe(path):
    text = (ROOT / path).read_text(encoding='utf-8')
    m = re.search(r'/\*\*\s*\n?\s*\*?\s*(.+?)(?:\n\s*\*\s*\n|\*/)', text, re.S)
    if path.endswith('.java') and m:
        d = re.sub(r'\s*\*\s*', ' ', m.group(1)).strip()
        d = re.sub(r'\{@(?:code|link) ([^}]+)\}', r'\1', d)
        d = d.split('<pre>')[0].rstrip(': ')
        return esc(d.split('. ')[0].rstrip('.') + '.')
    m = re.search(r'^\s*--\s*(.+)$', text, re.M)
    if path.endswith('.sql') and m: return esc(m.group(1))
    m = re.search(r'^/\*\*\s*(.+?)\*/', text, re.M | re.S) or re.search(r'^// (.+)$', text, re.M)
    if m:
        d = re.sub(r'\s*\*\s*', ' ', m.group(1)).strip()
        d = re.sub(r'\{@(?:code|link) ([^}]+)\}', r'\1', d)
        return esc(d.split('. ')[0])
    return ''
rows = []
for base in ['src/main/java', 'src/main/resources', 'src/test/java', 'web/src']:
    for p in sorted((ROOT / base).rglob('*')):
        if p.is_file():
            rel = str(p.relative_to(ROOT))
            rows.append([f'<code>{esc(rel.replace("src/main/java/app/kaaj/", "…/").replace("src/test/java/app/kaaj/", "test/…/"))}</code>', describe(rel)])
add(table(['File', 'Purpose'], rows, ['46%', '54%'], 'small'))

# Appendix B
add('<h1 class="chapter">Appendix B · API examples</h1>')
add(code('''POST /api/v1/employer/jobs?submit=true
X-XSRF-TOKEN: <cookie value>     Idempotency-Key: 6f1d…     Accept-Language: bn
{
  "titleBn": "পিকআপ ড্রাইভার", "titleEn": "Pickup driver",
  "descriptionBn": "টঙ্গী ও গাজীপুরে ডেলিভারি রুটে পিকআপ চালাতে হবে।",
  "occupationId": "<id from GET /taxonomy/occupations?q=driver>",
  "jobType": "FULL_TIME", "shift": "DAY", "hours": "08:00-18:00", "openings": 2,
  "wageMin": 16000, "wageMax": 20000, "wagePeriod": "MONTH",
  "latitude": 23.8916, "longitude": 90.4023, "areaName": "টঙ্গী, গাজীপুর",
  "minExperienceYears": 1, "skillIds": [], "requiredDocuments": ["DRIVING_PRO_LIGHT"]
}
→ 201 { "id": "…", "status": "PUBLISHED", "publishedAt": "…", "expiresAt": "… (+30 days)",
        "employer": { "name": "করিম ট্রান্সপোর্ট", "verified": true },
        "allowedActions": ["EDIT", "PAUSE", "CLOSE"], … }
   (unverified employer: "status": "DRAFT", "awaitingVerification": true)''', 'Create and publish a job'))
add(code('''POST /api/v1/employer/jobs?submit=true      (same body without wageMin / wageMax)
→ 400 { "status": 400, "code": "error.validation", "detail": "কিছু তথ্য ঠিক করতে হবে।",
        "errors": { "wageMin": "বেতন অবশ্যই দিতে হবে। কাজ সবসময় বেতন দেখায়।",
                    "wageMax": "বেতন অবশ্যই দিতে হবে। কাজ সবসময় বেতন দেখায়।" } }''', 'Wage missing (Bangla error)'))
add(code('''PATCH /api/v1/employer/jobs/{id}   { "action": "PAUSE" }    → 200 { "status": "PAUSED", … }
PATCH /api/v1/employer/jobs/{id}   { "action": "PAUSE" }    → 409 { "code": "job.status.invalid-move", … }
PATCH /api/v1/employer/jobs/{id}   { "action": "REPOST" }   → 200 { "status": "DRAFT", "repostedFrom": "{id}", … }''', 'Manage a job'))
add(code('''POST /api/v1/employer/profile/document   (multipart: file=<trade-licence.png>, documentType=TRADE_LICENCE)
→ 200 { "status": "PENDING", "documentName": "trade-licence.png", "hasDocument": true, … }

POST /api/v1/admin/employers/{id}/verification   { "decision": "REJECT", "reason": "The licence is not readable" }
→ 200 { "status": "REJECTED", "verificationNote": "The licence is not readable", … }''', 'Verification'))

# Appendix C
add('<h1 class="chapter">Appendix C · Settings</h1>')
add(table(['Key', 'Default', 'Meaning'], [
    ['<code>server.port</code>', '<code>1027</code> (or <code>PORT</code>)', 'Backend port (last four digits of your ID)'],
    ['<code>DB_HOST</code>, <code>DB_PORT</code>, <code>DB_NAME</code>, <code>DB_USER</code>, <code>DB_PASSWORD</code>', 'localhost, 5432, kaaj, postgres, (required)', 'Database connection, from <code>.env</code>'],
    ['<code>kaaj.admin.email</code> / <code>.password</code>', 'from <code>KAAJ_ADMIN_*</code>', 'First admin, created at startup if none exists'],
    ['<code>kaaj.auth.simple-sign-up</code>', '<code>true</code>', 'Stand-in sign-up; set false when S5 is merged'],
    ['<code>kaaj.jobs.expiry-days</code>', '<code>30</code>', 'Days from first publish to automatic close'],
    ['<code>kaaj.jobs.expiry-cron</code>', '<code>0 5 * * * *</code>', 'When the expiry check runs (every hour at :05)'],
    ['<code>kaaj.jobs.auto-publish</code>', '<code>true</code>', 'Publish submitted posts at once (until M3-6)'],
    ['<code>kaaj.jobs.require-verified-employer</code>', '<code>true</code>', 'Only verified employers\' posts go live'],
    ['<code>kaaj.files.dir</code>', '<code>./data/uploads</code>', 'Where uploads are kept (git-ignored)'],
    ['<code>spring.profiles.active=dev</code>', 'off', 'Demo data, out-of-order Flyway, debug logs, default admin'],
], ['36%', '26%', '38%']))

CSS = f'''
@import url("file://{FONT_CSS}/400.css"); @import url("file://{FONT_CSS}/600.css"); @import url("file://{FONT_CSS}/700.css");
@page {{ size: A4; margin: 18mm 16mm 18mm 16mm; }}
* {{ box-sizing: border-box; }}
body {{ font-family: Inter, "Noto Sans Bengali", sans-serif; font-size: 10pt; line-height: 1.5; color: #0f172a; }}
h1.chapter {{ font-size: 19pt; color: #1d4ed8; margin: 0 0 10px; padding-bottom: 6px; border-bottom: 2px solid #dbeafe; break-before: page; }}
h2 {{ font-size: 13pt; margin: 18px 0 8px; color: #0f172a; break-after: avoid; }}
h3 {{ font-size: 11.5pt; margin: 0; }}
p {{ margin: 6px 0 8px; }}
code {{ font-family: "DejaVu Sans Mono", monospace; font-size: 8.4pt; background: #f1f5f9; padding: 0 3px; border-radius: 3px; }}
a {{ color: #1d4ed8; }}
blockquote {{ margin: 8px 0; padding: 8px 12px; border-left: 4px solid #1d4ed8; background: #eff6ff; font-style: italic; }}
table {{ width: 100%; border-collapse: collapse; margin: 8px 0 12px; font-size: 9pt; table-layout: fixed; }}
th {{ background: #1e3a8a; color: white; text-align: left; padding: 5px 6px; font-weight: 600; }}
td {{ border-bottom: 1px solid #e2e8f0; padding: 5px 6px; vertical-align: top; overflow-wrap: anywhere; }}
tr:nth-child(even) td {{ background: #f8fafc; }}
table.small {{ font-size: 7.8pt; }} table.small td {{ padding: 3px 5px; }}
tr {{ break-inside: avoid; }}
.code {{ margin: 8px 0 12px; border: 1px solid #cbd5e1; border-radius: 6px; overflow: hidden; break-inside: auto; }}
.code-title {{ background: #e2e8f0; padding: 3px 8px; font-size: 8pt; font-weight: 600; color: #334155; font-family: "DejaVu Sans Mono", monospace; }}
.code pre {{ margin: 0; padding: 7px 9px; background: #0f172a; color: #e2e8f0; font-size: 7.6pt; line-height: 1.38; white-space: pre-wrap; overflow-wrap: anywhere; }}
.code pre code {{ background: none; padding: 0; font-size: inherit; color: inherit; font-family: "DejaVu Sans Mono", "Noto Sans Bengali", monospace; }}
.note {{ margin: 10px 0; padding: 9px 12px; border-radius: 8px; font-size: 9.4pt; break-inside: avoid; }}
.note.info {{ background: #eff6ff; border: 1px solid #bfdbfe; }}
.note.warn {{ background: #fffbeb; border: 1px solid #fcd34d; }}
.note.ok {{ background: #ecfdf5; border: 1px solid #a7f3d0; }}
.step {{ margin: 12px 0 14px; }}
.step-head {{ display: flex; align-items: center; gap: 9px; margin-bottom: 4px; break-after: avoid; }}
.num {{ display: inline-grid; place-items: center; width: 24px; height: 24px; border-radius: 50%; background: #1d4ed8; color: white; font-weight: 700; font-size: 10pt; flex: none; }}
figure {{ margin: 10px auto 16px; break-inside: avoid; }}
figure img {{ width: 100%; border: 1px solid #cbd5e1; border-radius: 6px; }}
figcaption {{ font-size: 8.8pt; color: #475569; margin-top: 4px; }}
svg {{ margin: 6px 0 12px; break-inside: avoid; }}
.toc {{ font-size: 11.5pt; line-height: 2; }}
.cover {{ height: 255mm; display: flex; flex-direction: column; justify-content: center; }}
.cover-top {{ display: flex; align-items: baseline; gap: 12px; }}
.logo {{ font-size: 34pt; font-weight: 700; color: #1d4ed8; }}
.logo-sub {{ font-size: 14pt; color: #475569; font-weight: 600; }}
.cover h1 {{ font-size: 30pt; margin: 12px 0 4px; }}
.cover-sub {{ font-size: 13pt; color: #334155; }}
.cover-meta {{ margin-top: 18px; font-size: 10pt; }} .cover-meta td:first-child {{ width: 26%; font-weight: 600; }}
.cover-meta tr:nth-child(even) td {{ background: none; }}
.cover-box {{ margin-top: 20px; padding: 14px 18px; background: #eff6ff; border-radius: 12px; border: 1px solid #bfdbfe; }}
.cover-box ol {{ margin: 6px 0 0 18px; padding: 0; }}
'''

html_doc = f'<!doctype html><html lang="en"><head><meta charset="utf-8"><title>Kaaj – Member-3 build guide</title><style>{CSS}</style></head><body>{"".join(S)}</body></html>'
out = pathlib.Path('/home/claude/guide/guide.html'); out.write_text(html_doc, encoding='utf-8')
print('html', len(html_doc) // 1024, 'KB')
