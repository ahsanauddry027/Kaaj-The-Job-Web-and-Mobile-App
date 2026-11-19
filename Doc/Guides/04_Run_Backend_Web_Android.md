# How to run Kaaj: the backend, the web app and the Android app

**Kaaj – The Job** · Ahsan Habib (22201027) · Windows 11 · written 18 November 2026 · updated 19 November 2026 (PostgreSQL and pgAdmin from scratch)

This guide takes you from a fresh start to all three parts running together on your PC:

| Part | Folder | Runs on | You open it at |
|---|---|---|---|
| **Backend** (Spring Boot + PostgreSQL) | `studentapi\` | port **1027** | <http://localhost:1027/swagger-ui.html> |
| **Web app** (React + TypeScript) | `studentapi\web\` | port **5173** | <http://localhost:5173> |
| **Android app** (Kotlin + Jetpack Compose) | `studentapi\android\` | Android emulator or your phone | the Kaaj app |

The backend must be running before either app can do anything. The usual order is: PostgreSQL (with the `kaaj` database, made in
pgAdmin) → backend → web app and/or Android app.

> **Important: this is the first time the code will be built.** The backend and the Android app were written without being compiled
> (the AI's workspace could not download the build tools). The web app was built and tested, but only against a stand-in API. Expect a few
> errors on the first build. Part E shows how to save the error output so it can be fixed quickly.

All commands are for **PowerShell**. Open it from the Start menu ("Windows PowerShell" or "Terminal"). Each part says which folder to be in.

## Contents

1. [Part 0 — Install and check the tools (once)](#part-0--install-and-check-the-tools-once)
2. [Part 1 — Set up PostgreSQL and the database with pgAdmin (once)](#part-1--set-up-postgresql-and-the-database-with-pgadmin-once)
3. [Part A — Run the backend](#part-a--run-the-backend)
4. [Part B — Run the web app](#part-b--run-the-web-app)
5. [Part C — Run the Android app](#part-c--run-the-android-app)
6. [Part D — All three together: a 5-minute demo](#part-d--all-three-together-a-5-minute-demo)
7. [Part E — When something fails](#part-e--when-something-fails)
8. [Everyday cheat sheet](#everyday-cheat-sheet)

---

## Part 0 — Install and check the tools (once)

Open PowerShell and run:

```powershell
java -version      # must say version 25
node -v            # must be v24 or newer
npm -v
git --version
```

| Tool | Needed for | If it's missing |
|---|---|---|
| **JDK 25** (Eclipse Temurin) | Backend | `winget install --id EclipseAdoptium.Temurin.25.JDK -e`, or download from <https://adoptium.net> |
| **Node.js 24 LTS** | Web app | `winget install --id OpenJS.NodeJS.LTS -e`, or <https://nodejs.org> |
| **PostgreSQL** with **pgAdmin 4** | Backend | Part 1, step 1.1 |
| **Android Studio** (latest stable) | Android app | <https://developer.android.com/studio> |
| Git | GitHub | `winget install --id Git.Git -e` |
| Docker Desktop | *Optional*: only for the backend's end-to-end tests | <https://www.docker.com/products/docker-desktop/> |

You don't install Maven: the project's `mvnw.cmd` downloads Maven 3.10.0 by itself.
More detail on installing each tool is in `Doc/source-documents/Kaaj_Windows11_Setup_Guide.pdf`.

**If `java -version` shows an older version** (17 or 21), see Part E, row 2.

---

## Part 1 — Set up PostgreSQL and the database with pgAdmin (once)

Everything here is done in **pgAdmin 4**, the graphical tool that comes with PostgreSQL. Typed commands (`psql`) are given as an
alternative at the end of each step, if you prefer them.

> **Which PostgreSQL version?** **14 or newer** works. The project uses nothing newer than PostgreSQL 14 offers, and its database tool
> (Flyway) supports PostgreSQL 9 to 17 ([Redgate](https://documentation.red-gate.com/fd/introducing-flyway-pipelines-for-postgresql-your-new-window-into-database-deployment-health-and-history-261325691.html)).
> It was tested on PostgreSQL 16. If you are installing it fresh, take the newest version the installer offers. In pgAdmin the server
> appears as "PostgreSQL 14" (or 16, 17, 18): it is the same steps whatever the number.

### Step 1.1 — Install PostgreSQL and pgAdmin (skip if you already have them)

1. Go to <https://www.postgresql.org/download/windows/> and click **Download the installer** (by EDB).
2. Run it and keep the defaults, with these checks:
   - **Components:** tick **PostgreSQL Server**, **pgAdmin 4** and **Command Line Tools**. Stack Builder isn't needed.
   - **Password:** type a password for the database superuser `postgres`. **Write it down**: Kaaj needs it in step 1.5.
   - **Port:** keep **5432**. (If it suggests 5433, another PostgreSQL is already installed; note the number for step 1.5.)
   - **Locale:** keep the default.
3. Finish. Untick "Launch Stack Builder" if it asks.

PostgreSQL now runs as a Windows service and starts with Windows.

### Step 1.2 — Open pgAdmin and connect to your server

1. Start menu → **pgAdmin 4**. The first time it asks you to set a **master password**: this protects the passwords pgAdmin saves
   for you. It is pgAdmin's own password, not the database's; choose one and remember it.
2. In the **Object Explorer** on the left, expand **Servers**. You should see **PostgreSQL 14** (or your version).
3. Click it. When asked, type the `postgres` password from step 1.1, tick **Save password**, and click **OK**.
   The server's icon changes (no red cross) and **Databases**, **Login/Group Roles** and **Tablespaces** appear under it.

**No server in the list?** Add it yourself: right-click **Servers → Register → Server…**

| Tab | Field | Value |
|---|---|---|
| General | Name | `Local PostgreSQL` |
| Connection | Host name/address | `localhost` |
| Connection | Port | `5432` (or the port from step 1.1) |
| Connection | Maintenance database | `postgres` |
| Connection | Username | `postgres` |
| Connection | Password | your `postgres` password, and tick **Save password?** |

Click **Save**.

**"Could not connect to server" / "Connection refused"?** The PostgreSQL service is stopped. Start menu → type **Services** → find
**postgresql-x64-14** (your version number) → right-click → **Start**. Or in PowerShell *as Administrator*:
`Get-Service postgresql*` then `Start-Service postgresql-x64-14`.

### Step 1.3 — Create the `kaaj` database

Kaaj needs its own, empty database. (Your Assignment 03 used another one, `student_db`; leave that alone.)

1. Under your server, right-click **Databases → Create → Database…**
2. **General** tab: **Database** `kaaj` (lowercase), **Owner** `postgres`.
3. **Definition** tab: check that **Encoding** is **UTF8** (needed for Bangla text). If it shows anything else, set **Template** to
   `template0` and **Encoding** to `UTF8`.
4. Click **Save**. `kaaj` now appears under **Databases**.

Don't create any tables: the backend creates all of them (15 tables) the first time it starts.

*With commands instead:* `psql -U postgres -c "CREATE DATABASE kaaj ENCODING 'UTF8' TEMPLATE template0;"`
(if `psql` is "not recognized", use its full path, for example `& "C:\Program Files\PostgreSQL\14\bin\psql.exe" -U postgres -c "..."`).

### Step 1.4 — Optional: check the search extension

Kaaj's typo-tolerant search uses PostgreSQL's `pg_trgm` extension. The backend switches it on by itself, as the `postgres` user. To
check it is available: right-click **kaaj → Query Tool**, type the line below and press **F5** (or the ▶ **Execute** button):

```sql
SELECT name, default_version FROM pg_available_extensions WHERE name = 'pg_trgm';
```

One row means it's there (it comes with every PostgreSQL installed by the Windows installer).

### Step 1.5 — Put the database password in `.env`

The backend reads its settings from a file named `.env` in the project folder. It is private: git-ignored, never uploaded.

```powershell
cd C:\Users\UseR\Desktop\Projects\studentapi
if (-not (Test-Path .env)) { Copy-Item .env.example .env }
notepad .env
```

Make sure these lines are there (your real `postgres` password after `DB_PASSWORD=`, no spaces, no comment on the same line):

```properties
DB_PASSWORD=your-postgres-password
DB_NAME=kaaj
DB_USER=postgres
KAAJ_ADMIN_EMAIL=admin@kaaj.local
KAAJ_ADMIN_PASSWORD=Admin@12345
```

Only if your PostgreSQL is **not** on port 5432 (step 1.1), add `DB_PORT=5433` (your port). Optional: `GEMINI_API_KEY=` with a free
key from <https://aistudio.google.com> makes the job-post assistant and the scam check use AI. Save and close Notepad.

`DB_PASSWORD` must be exactly the password pgAdmin accepted in step 1.2.

---

## Part A — Run the backend

Use a PowerShell window in the project folder. Keep this window open while you use the apps.

```powershell
cd C:\Users\UseR\Desktop\Projects\studentapi
```

### Step A1 — Build and test it (first time, and after changes)

```powershell
.\mvnw.cmd test
```

- The first run downloads Maven and all the libraries: allow several minutes and a working internet connection.
- **Success looks like:** `Tests run: …, Failures: 0, Errors: 0` and `BUILD SUCCESS` at the end.
- The end-to-end tests need Docker Desktop. Without it they are reported as skipped: that's fine.
- **If you see `BUILD FAILURE` or `COMPILATION ERROR`:** go to Part E and save the log.

### Step A2 — Start the backend with the demo data

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

- Keep the quotes around `-Dspring-boot.run.profiles=dev`: without them PowerShell breaks the argument at the dot.
- The `dev` profile loads the demo accounts and jobs, and turns on more logging.
- **Success looks like:** on the first start Flyway prints `Successfully applied … migrations`, and at the end you see
  `Started KaajApplication in … seconds`. The window then stays busy: that means the server is running.

### Step A3 — Check that it answers

Open a **second** PowerShell window and run:

```powershell
curl.exe -i http://localhost:1027/api/v1/auth/csrf
curl.exe http://localhost:1027/api/v1/jobs
```

- The first should show `HTTP/1.1 204` and a line starting `Set-Cookie: XSRF-TOKEN=`.
- The second should print JSON with the demo jobs (for example a pickup driver post from Karim Transport).
- Type `curl.exe`, not `curl`: in Windows PowerShell, `curl` is a different command.

You can also open <http://localhost:1027/swagger-ui.html> in a browser to see and try every API endpoint.

### Step A4 — See the database in pgAdmin

After the first start, the backend has created the tables and filled in the demo data. In pgAdmin:

1. Right-click **kaaj → Refresh**, then open **kaaj → Schemas → public → Tables**. You should see **16** tables: Kaaj's 15
   (`app_user`, `audit_log`, `email_code`, `occupation`, `occupation_synonym`, `skill`, `skill_synonym`, `employer`, `job`,
   `seeker_profile`, `saved_job`, `application`, `application_event`, `trust_case`, `report`) plus `flyway_schema_history`,
   where Flyway records which migrations have run.
2. To look inside a table: right-click it → **View/Edit Data → All Rows**.
3. To ask questions: right-click **kaaj → Query Tool**, paste one query, select it and press **F5**:

```sql
-- every migration Flyway ran (success should be true everywhere)
SELECT version, description, success FROM flyway_schema_history ORDER BY installed_rank;

-- the demo accounts and the admin
SELECT email, role, status FROM app_user ORDER BY email;

-- job posts, and why some are held or blocked
SELECT title_en, status, status_reason FROM job ORDER BY created_at DESC;

-- employers: verified or not
SELECT name, verification_status FROM employer;

-- the moderation queue
SELECT status, screening, report_count FROM trust_case;
```

Look, but don't change data or tables by hand while the backend is running: Flyway owns the structure, and the apps expect the data
to follow their rules. To start again with a clean database, see Part E, row 16.

### The demo accounts (password `Kaaj@12345` for all of them)

| Account | Role | What it has |
|---|---|---|
| `karim@demo.kaaj` | Employer | **Verified** (Karim Transport), six job posts, applicants |
| `tongigarments@demo.kaaj` | Employer | Profile filled in, **not verified**, no document yet: good for showing verification |
| `quickjobs@demo.kaaj` | Employer | **Verified** (Quick Jobs & Overseas): one post **blocked** by scam screening, one **in review**, one published post reported twice: good for showing moderation |
| `rahim@demo.kaaj` | Job seeker | Driver in Tongi, **shortlisted** for Karim's pickup-driver post |
| `sumi@demo.kaaj` | Job seeker | Sewing operator in Uttara, applied to the office-assistant post (**viewed**) |
| `jamal@demo.kaaj` | Job seeker | New driver in Gazipur, **applied** to the pickup-driver post |
| `admin@kaaj.local` / `Admin@12345` | Admin | From your `.env` (use your own values if you changed them) |

### Stopping the backend

Press **Ctrl+C** in its window, then `Y` if it asks "Terminate batch job". Every restart signs everyone out of both apps (sign-ins are kept in memory).

---

## Part B — Run the web app

The backend (Part A) must be running. Open a **new** PowerShell window:

### Step B1 — Install the web app's libraries (first time only)

```powershell
cd C:\Users\UseR\Desktop\Projects\studentapi\web
npm ci
```

`npm ci` installs the exact versions listed in `package-lock.json` into `web\node_modules` (git-ignored). It takes a minute or two.

### Step B2 — Optional: run its tests

```powershell
npm test
```

**Success looks like:** every test file `passed`.

### Step B3 — Start it

```powershell
npm run dev
```

**Success looks like:** `Local: http://localhost:5173/`. Leave this window open too.

### Step B4 — Use it

1. Open <http://localhost:5173> in Chrome or Edge.
2. **Sign in** as `karim@demo.kaaj` / `Kaaj@12345`. You see the employer **dashboard**.
3. Try: **Business profile** (with the map pin and the Verified status), **My jobs** (filters and the ⋮ menu: pause, close, repost),
   **Post a job** (the 4-step form; try leaving the wage empty to see the error).
4. Switch the language between বাংলা and English from the top bar.
5. **Verification:** sign out, sign in as `tongigarments@demo.kaaj`, open **Business profile**, upload a document
   (you can use `studentapi\postman\sample-trade-licence.png`). The status becomes **Under review**.
6. Sign out, sign in as the admin, open the **verification queue**, open Tongi Garments, look at the document and **Approve**
   (or **Reject**, which needs a reason). Sign in as Tongi again: the profile shows **Verified**.
7. **Job-post assistant (Module 3):** as Karim, open **Post a job**. In **Write it for me**, click the first example sentence (or type
   your own, in Bangla, Banglish or English) and **Draft my post**: you get the title and description in both languages, the facts it
   found, questions to answer, and red flags. **Use this draft** fills the form. A sentence asking for a "registration fee" is blocked.
8. **Scam screening and moderation (Module 2):** sign in as `quickjobs@demo.kaaj` and open **My jobs**: "Malaysia factory job" is
   **blocked** and "Online data entry" is **in review**; open one to read why. Sign in as the admin and open **Moderation**: the red
   flags are highlighted in the post. **Remove the post** needs a note; **Suspend the employer** asks you to confirm and signs that
   employer out at their next click. **Dismiss** publishes a post that was fine.
9. **Reporting:** create a new job seeker account (**Create account → Find a job**), open
   <http://localhost:5173/jobs/01936a00-0000-7000-8000-0000000000b3> ("Warehouse helper") and **Report this post**. It was already reported
   twice, so your report takes it out of search until the admin decides.

Without an AI key the assistant says "Drafted from your sentence (no AI)" and screening uses its rules. To use the free AI, get a key at
<https://aistudio.google.com> (**Get API key**), put `GEMINI_API_KEY=...` in `.env` and restart the backend.

The web app has the employer and admin side only. A job seeker who signs in on the web sees a placeholder page. The job seeker side is in the Android app.

### Stopping the web app

**Ctrl+C** in its window.

### Optional — The web app inside the backend (one program, one port)

This is how it would run on a server. Stop the backend from Part A and the web app first (they use the same ports), then in the project folder:

```powershell
cd C:\Users\UseR\Desktop\Projects\studentapi
.\mvnw.cmd -Pweb package -DskipTests
java -jar target\kaaj-0.1.0-SNAPSHOT.jar --spring.profiles.active=dev
```

Now <http://localhost:1027> shows the web app and serves the API. The first `-Pweb` build downloads its own copy of Node.js into `target\`.
Run the `java -jar` command from the `studentapi` folder, because `.env` is read from the current folder.

---

## Part C — Run the Android app

The backend (Part A) must be running.

### Step C1 — Open the project

1. Start Android Studio.
2. **File → Open…**, choose `C:\Users\UseR\Desktop\Projects\studentapi\android` (the **`android`** folder, not `studentapi`), click **OK**.
3. If asked, click **Trust Project**.

### Step C2 — Let Gradle sync

- Android Studio starts a **Gradle sync** (progress at the bottom). The first one downloads Gradle 9.6 and the libraries: allow several minutes.
- If it says **Android SDK Platform 37** (or another SDK part) is missing, click the **Install** link in the message, or open
  **Tools → SDK Manager → SDK Platforms**, tick the newest Android version (API 37), and click **Apply**.
- **Gradle's JDK:** **File → Settings → Build, Execution, Deployment → Build Tools → Gradle → Gradle JDK** should be Android Studio's own
  JDK ("jbr-…" / "JetBrains Runtime"). The backend uses JDK 25; the Android build uses Android Studio's JDK. Two JDKs side by side is normal.
- **Success looks like:** "Gradle sync finished" (or no red error) in the **Build** window, and a green ▶ **Run** button with `app` next to it.
- **Sync failed?** See Part E (rows 7–9), then save the error as in Part E.

### Step C3 — Build it once

**Build → Make Project** (Ctrl+F9). **Success looks like:** `BUILD SUCCESSFUL` in the **Build** window.
Compile errors show in red with a file name and line number: save them as in Part E.

### Step C4 — Run the unit tests

Either in Android Studio: in the **Project** panel open `app/src/test/java`, right-click the `app.kaaj.android` package → **Run 'Tests in …'**.

Or in PowerShell, using Android Studio's JDK:

```powershell
cd C:\Users\UseR\Desktop\Projects\studentapi\android
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
.\gradlew.bat test
```

**Success looks like:** all tests green, or `BUILD SUCCESSFUL`. The test report is at `android\app\build\reports\tests\testDebugUnitTest\index.html`.

### Step C5 — Create an emulator (once)

1. **Tools → Device Manager** (or the phone icon on the right) → **+** → **Create Virtual Device**.
2. Pick a phone (for example **Pixel 8**) → **Next**.
3. Pick a system image: a recent one (API 35, 36 or 37) with **Google APIs**; click the download icon next to it if needed → **Next** → **Finish**.
4. Start it with ▶ in Device Manager.

**Put the emulator in Bangladesh** (so "jobs near me" makes sense): in the emulator's side bar click **⋯ (Extended controls) → Location**,
search for **Tongi** (or Dhaka), click **Set location**. Otherwise the emulator thinks it is in California and every job is very far away.

### Step C6 — Run the app

1. Check the backend window still says it is running (Part A).
2. In the toolbar, choose your emulator next to `app`, then click the green ▶ **Run** (Shift+F10).
3. The Kaaj sign-in screen opens. At the bottom it shows **Server: http://10.0.2.2:1027**. Inside the emulator, `10.0.2.2` means "this PC"; leave it as it is.

**If you see "Can't reach the server"**, the backend isn't running or is still starting. Start it, then tap **Try again**.

### Step C7 — Use it

Debug builds list the **demo accounts** under the sign-in form: tap one to fill in the email and password, then **Sign in**.

**As a job seeker** (`rahim@demo.kaaj`):

1. **Find jobs**: search (for example "driver"), set the distance, wage, type and shift, and sort by newest, nearest or wage.
   Allow location when asked. Pull down to refresh.
2. Open a job, **save** it (bookmark) and **Apply**.
3. **Applications**: open one to see its timeline (Applied → Viewed → Shortlisted …); you can withdraw an open application.
4. **Profile**: the completeness meter shows what is missing; set your home location on the map.

**As an employer** (`karim@demo.kaaj`): dashboard, **Business profile** (map pin, document by camera, gallery or PDF), **My jobs** with actions,
**Post a job** (4 steps), open a job → **Applicants**: ranked by match score with reasons and gaps; open one to shortlist, invite to interview, hire or decline.
The applicant's phone number appears only after you shortlist them.

**As the admin** (`admin@kaaj.local`): the verification queue; open an employer to see the document; approve, or reject with a reason.

**Settings** (the gear icon at the top of the main screens): language বাংলা / English, server address with **Test connection**, sign out.

### Step C8 — Run it on your own phone (optional)

**Easiest: USB cable.**

1. On the phone: **Settings → About phone →** tap **Build number** 7 times; then **Developer options → USB debugging** on.
2. Connect the cable and allow the PC when the phone asks.
3. In PowerShell:
   ```powershell
   & "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" reverse tcp:1027 tcp:1027
   ```
4. In Android Studio choose your phone next to `app` and click ▶ **Run**.
5. On the sign-in screen tap **Server: …** at the bottom and enter `http://localhost:1027`. (Run the `adb reverse` command again every time you reconnect the cable.)

**Same Wi-Fi, no cable:** find your PC's address with `ipconfig` ("IPv4 Address", for example `192.168.0.105`), allow port 1027 in the firewall
(PowerShell as Administrator, once):

```powershell
New-NetFirewallRule -DisplayName "Kaaj backend 1027" -Direction Inbound -Protocol TCP -LocalPort 1027 -Action Allow -Profile Private
```

then set the server in the app to `http://192.168.0.105:1027` (your own address). University Wi-Fi often blocks this; use the cable instead.
More options (a public link with a Cloudflare tunnel) are in `01_One_Backend_Two_Apps`, step 6.

### Step C9 — Make an APK to share or install (optional)

**Build → Build App Bundle(s) / APK(s) → Build APK(s)**. The file is `android\app\build\outputs\apk\debug\app-debug.apk`.
Don't commit it to Git (it is ignored); attach it to a GitHub release instead (`02_GitHub_Milestone_Pushes`, Part B step 5).

---

## Part D — All three together: a 5-minute demo

Good for the Milestone 2 and Final videos. Have the backend running, the web app open in the browser, and the Android app on the emulator.

1. **Web**, as `karim@demo.kaaj`: **Post a job** ("Delivery helper", wage Tk 12,000–14,000 a month, near Tongi) and publish it.
2. **Phone**, as `rahim@demo.kaaj`: **Find jobs**, pull to refresh. The new job is there with the **Verified** badge and its distance.
3. **Phone**: open it and **Apply**. **Applications** shows it as *Applied*.
4. **Phone**: sign out, sign in as `karim@demo.kaaj`, open the job → **Applicants**: Rahim is there with his match score and reasons. Open him (*Viewed*) and **Shortlist**.
5. **Phone**: sign in as Rahim again → **Applications**: the timeline now shows *Viewed* and *Shortlisted*.

Say it in the video: *two apps, one backend, the same data*.

---

## Part E — When something fails

### Save the error so it can be fixed

Save the output to a file on your Desktop (outside the project, so it never gets committed), then share the file or paste its first error lines:

```powershell
cd C:\Users\UseR\Desktop\Projects\studentapi
.\mvnw.cmd test *> "$env:USERPROFILE\Desktop\backend-log.txt"

cd web
npm run build *> "$env:USERPROFILE\Desktop\web-log.txt"

cd ..\android
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
.\gradlew.bat assembleDebug test --stacktrace *> "$env:USERPROFILE\Desktop\android-log.txt"
```

In Android Studio you can also right-click inside the **Build** window → **Copy All** and paste it into a text file.
The useful part is usually the **first** error: a file name, a line number and a message.

### Common problems

| # | What you see | Fix |
|---|---|---|
| 1 | `'java' is not recognized` or `JAVA_HOME is not defined correctly` | Install JDK 25 (Part 0), close and reopen PowerShell |
| 2 | `release version 25 not supported` or `invalid target release: 25` | Maven is using an older JDK. Set JAVA_HOME to JDK 25 for your user, then reopen PowerShell: `[Environment]::SetEnvironmentVariable("JAVA_HOME", "C:\Program Files\Eclipse Adoptium\jdk-25…", "User")` (use the real folder name) |
| 3 | `Could not resolve placeholder 'DB_PASSWORD'` | Run the command from the `studentapi` folder; check `.env` has `DB_PASSWORD=...` (step 1.5) |
| 4 | `password authentication failed for user "postgres"` | `DB_PASSWORD` in `.env` is not the password pgAdmin accepts (step 1.2). Fix `.env` and start the backend again |
| 5 | `database "kaaj" does not exist` / `Connection refused` on port 5432 | Create it (step 1.3) / start the PostgreSQL service (step 1.2, "Could not connect"); if your server uses another port, set `DB_PORT` in `.env` |
| 6 | `Port 1027 was already in use` | Another backend is running. Find it with `Get-NetTCPConnection -LocalPort 1027 \| Select-Object OwningProcess`, stop it with `Stop-Process -Id <number>` |
| 7 | Gradle sync error on `compileSdk { version = release(37) }` | Open `android\app\build.gradle.kts` and change it to `compileSdk = 37` |
| 8 | Gradle error mentioning the `org.jetbrains.kotlin.android` plugin | Don't add that plugin: Android Gradle Plugin 9 has Kotlin built in |
| 9 | "No Gradle project" or the Run button is missing | You opened `studentapi` instead of `studentapi\android` (Step C1) |
| 10 | Web page loads but every action fails, or `Unexpected token '<'` | The backend isn't running (Part A) |
| 11 | Web: signed in, but you're sent back to sign-in | Open `http://localhost:5173`, not `127.0.0.1:5173` |
| 12 | Android: "Can't reach the server" | Backend not running; on a real phone, the server address or `adb reverse` (Step C8) |
| 13 | Android on a phone: `CLEARTEXT communication … not permitted` | Use the debug build (the normal ▶ Run), or an `https://` address |
| 14 | Upload fails with 413 | The file is over 3 MB; use a smaller one (the Android app shrinks photos itself) |
| 15 | Signed out after restarting the backend | Normal: sign-ins are kept in memory |
| 16 | `Migration checksum mismatch`, or you just want a clean start | A migration that had already run was changed. For your local demo database only: stop the backend (Ctrl+C); in pgAdmin open a **Query Tool on the `postgres` database** (not `kaaj`) and run `DROP DATABASE kaaj WITH (FORCE);`; then do step 1.3 again and start the backend |
| 17 | pgAdmin asks for a **master password** you forgot | It is pgAdmin's own password. Click **Reset Master Password** (pgAdmin forgets the saved server passwords), set a new one, then type the `postgres` password again |
| 18 | pgAdmin: `password authentication failed` for `postgres` | That is the password from the PostgreSQL installer. If it is lost, reinstalling is the simplest fix for a student PC |
| 19 | Bangla text shows as `???` | The database was created without UTF8. Drop it (row 16) and create it again with **Template** `template0` and **Encoding** `UTF8` (step 1.3) |
| 20 | `permission denied to create extension "pg_trgm"` | Use the `postgres` user in `.env` (`DB_USER=postgres`), or run `CREATE EXTENSION pg_trgm;` once in a Query Tool on `kaaj` as `postgres` |
| 21 | Two PostgreSQL servers in pgAdmin (for example 14 and 18) | Each has its own port (5432, 5433). Create `kaaj` on one of them and put that server's port in `.env` as `DB_PORT` |

More problems and their causes: `01_One_Backend_Two_Apps`, section 5.

---

## Everyday cheat sheet

| Step | Window | Command / action |
|---|---|---|
| 1 | — | PostgreSQL runs by itself (check: `Get-Service postgresql*`, or pgAdmin connects without a red cross) |
| 2 | PowerShell 1, in `studentapi` | `.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"` → wait for `Started KaajApplication` |
| 3 | PowerShell 2, in `studentapi\web` | `npm run dev` → <http://localhost:5173> |
| 4 | Android Studio, project `studentapi\android` | Start the emulator → ▶ Run |
| Stop | each window | Ctrl+C (backend, web); ■ Stop in Android Studio |
| After changing backend code | PowerShell 1 | Ctrl+C, then `.\mvnw.cmd test`, then start it again |
| Look at the data | pgAdmin | **kaaj → Schemas → public → Tables** → right-click → **View/Edit Data**, or **kaaj → Query Tool** |
| Clean database | pgAdmin, Query Tool on `postgres` | Stop the backend, `DROP DATABASE kaaj WITH (FORCE);`, create `kaaj` again (step 1.3) |

| Address | What |
|---|---|
| <http://localhost:1027/swagger-ui.html> | Backend API docs |
| <http://localhost:5173> | Web app (development) |
| `http://10.0.2.2:1027` | The backend, as seen from the Android emulator |
| `http://localhost:1027` | The backend, from a phone connected with `adb reverse` |

Related guides in `Doc/Guides/`: `01_One_Backend_Two_Apps` (how the two apps share the backend, more fixes), `02_GitHub_Milestone_Pushes`
(pushing and tagging the milestones), `03_Business_Model`. The whole project is summarised in `Doc/PROJECT_CONTEXT.md`.
