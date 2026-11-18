# Pushing the two apps separately: the web app per module, the Android app per milestone

**Kaaj – The Job** · Ahsan Habib (22201027) · GitHub: `ahsanauddry027` · written 19 November 2026

You now have **three repositories**, each with its own job:

| Repository | What goes in | Pushed | Part |
|---|---|---|---|
| **`SpringBoot_PostgreSQL_Api`** (already exists) | **Everything**: backend, web app, Android app, all documents. Your full copy and backup | Once now, then whenever you like | 0 |
| **Android course repository** (new, for example `Kaaj-The-Job-Android`) | The Android app + the backend it needs + `Doc/` | **Per milestone**: `milestone-2`, then `final` | A |
| **CSE471 group repository** (your group's) | Only your web modules: shared setup + Module 1, Module 2, Module 3 | **Per module**: one branch and pull request each | W |

You keep **working in one place only**: `C:\Users\UseR\Desktop\Projects\studentapi`. The Android repository and the group
repository are filled **from** it, in a separate folder, `C:\Users\UseR\Desktop\Projects\Kaaj-Submissions`.

```
C:\Users\UseR\Desktop\Projects\
├── studentapi\                ← where you work · Git remote: SpringBoot_PostgreSQL_Api
└── Kaaj-Submissions\
    ├── kaaj-android\          ← Android course repository (Part A)
    └── cse471-kaaj\           ← clone of the CSE471 group repository (Part W)
```

Every command is for **PowerShell** on Windows 11. Run them one block at a time and read what they print.

---

## Part 0 — Finish the upload to `SpringBoot_PostgreSQL_Api` (once)

Everything in `studentapi` is already **committed** on a new branch, **`kaaj-the-job`**, in five commits (Assignment 03 code removed;
backend; Android app; web app; documents). Claude could not push it because it has no access to your GitHub account, so you push it:

```powershell
cd C:\Users\UseR\Desktop\Projects\studentapi
git status                      # "On branch kaaj-the-job ... nothing to commit, working tree clean"
git log --oneline -6            # the five new commits on top of 84a5a39 (Assignment 03)
git push -u origin kaaj-the-job
```

The first push may open a browser window to sign in to GitHub; sign in as `ahsanauddry027`.

Then make it the repository's main content, with a pull request:

1. Open <https://github.com/ahsanauddry027/SpringBoot_PostgreSQL_Api>. Click **Compare & pull request** (yellow banner).
2. Title "Kaaj – The Job: full project", then **Create pull request** → **Merge pull request** → **Confirm merge**.
3. The repository page now shows Kaaj and the new `README.md`.

> **Shortcut without a pull request:** `git push origin kaaj-the-job:Main` puts the commits straight onto `Main` (it only adds
> commits, so nothing is lost). Use one of the two ways, not both.

Bring your own `Main` branch up to date and remove the temporary branch:

```powershell
git fetch origin
git branch -f Main origin/Main
git switch Main
git branch -d kaaj-the-job
git status                      # "On branch Main ... up to date with 'origin/Main'"
```

> **Assignment 03's code** is no longer on the repository's front page, but it is safe in the history. If that instructor still
> needs it, send this link: <https://github.com/ahsanauddry027/SpringBoot_PostgreSQL_Api/tree/84a5a39>

**From now on**, after you change something in `studentapi`: `git add -A`, `git commit -m "Area: what changed"`, `git push`.

**What is not uploaded** (by `.gitignore`, on purpose): `.env` (your passwords), `data/` (uploaded files), `Claude outputs/`
(the Assignment 03 report), `Generated Documents/` (copies of `Doc/` and `docs/`, which are uploaded), and build folders
(`target/`, `web/node_modules/`, `android/build/`).

---

## Part 1 — Make the submissions folder (once)

```powershell
$src = "C:\Users\UseR\Desktop\Projects\studentapi"
$dst = "C:\Users\UseR\Desktop\Projects\Kaaj-Submissions"
New-Item -ItemType Directory -Force $dst | Out-Null
```

`$src` and `$dst` last only until you close the window. In a new window, run these two lines again first.

---

## Part A — The Android app, per milestone (Kotlin course)

### A1. Which files go into the Android repository

| Path | In the Android repository? | Why |
|---|---|---|
| `android/` (99 files) | **Yes** | The app |
| `src/`, `pom.xml`, `mvnw`, `mvnw.cmd`, `.mvn/` | **Yes** | The backend the app talks to (also has the web-only modules; that is fine) |
| `.gitignore`, `.gitattributes`, `.env.example`, `compose.yaml` | **Yes** | Ignore rules, line endings, settings template, optional database |
| `Doc/` | **Yes** | The course asks for the AI declaration, prompts and documents in `/Doc` |
| `postman/` | **Yes** (optional) | API requests for testing |
| `README.md` | **Yes, the Android version**: `Doc/Guides/github-files/android/README_for_the_Android_repository.md` | It describes the app, the milestones and how to run it |
| `web/`, `docs/` | No | The CSE471 web app and its first build guide |
| `.env`, `data/`, `Generated Documents/`, `Claude outputs/` | **Never** | Passwords, uploads, copies |

### A2. Copy them

```powershell
$a = "$dst\kaaj-android"
robocopy $src $a pom.xml mvnw mvnw.cmd .gitignore .gitattributes .env.example compose.yaml
robocopy "$src\.mvn"    "$a\.mvn"    /E
robocopy "$src\src"     "$a\src"     /E
robocopy "$src\android" "$a\android" /E /XD build .gradle .idea /XF local.properties
robocopy "$src\Doc"     "$a\Doc"     /E
robocopy "$src\postman" "$a\postman" /E
Copy-Item "$src\Doc\Guides\github-files\android\README_for_the_Android_repository.md" "$a\README.md"
Copy-Item "$src\.env" "$a\.env"     # only to run it from here; .gitignore keeps it out of Git
```

`robocopy` prints a summary table. Exit codes 0–7 mean success (1 = files copied); 8 or more is an error.

### A3. Create the empty repository on GitHub

<https://github.com/new> → owner `ahsanauddry027`, name **`Kaaj-The-Job-Android`** → Public (or Private and then add `rahman9909`
under **Settings → Collaborators**) → do **not** add a README, `.gitignore` or license → **Create repository**.

### A4. Milestone 2 (deadline Sat 5 Dec 2026, 11:59 pm)

One commit for the backend, one per Android feature (so the history shows the work step by step), one for the documents:

```powershell
cd $a
git init -b main
git config user.name "ahsanauddry027"
git config user.email "ahsan.habib1@g.bracu.ac.bd"
git check-ignore -v .env        # must print a line ending in ".env": it is ignored

git add -- pom.xml mvnw mvnw.cmd .mvn src .gitignore .gitattributes .env.example compose.yaml
git update-index --chmod=+x mvnw
git commit -m "Backend: Spring Boot API (Java 25, PostgreSQL) used by the Android app"

function Add-List($name) {
  $paths = Get-Content "Doc\Guides\github-files\android\$name.txt" | Where-Object { $_ -and -not $_.StartsWith('#') }
  git add -- $paths
}
Add-List '1-app-shell-and-sign-in'
git update-index --chmod=+x android/gradlew
git commit -m "Android: app shell, networking with session cookie + CSRF, registration and sign-in"
Add-List '2-employer-profile-and-verification'; git commit -m "Android: employer business profile and verification"
Add-List '3-post-and-manage-jobs';              git commit -m "Android: post and manage jobs"
Add-List '4-admin-verification-queue';          git commit -m "Android: admin verification queue"
Add-List '5-job-seeker-and-applicants';         git commit -m "Android: job seeker side and ranked applicants"

git add -- Doc postman README.md
git commit -m "Docs: README, AI declaration and prompts, diagrams, guides"
```

Check before pushing:

```powershell
git status                                       # nothing to commit, working tree clean
git ls-files android | Measure-Object            # Count: 99
git ls-files | Select-String '(^|/)\.env$'       # must print NOTHING
git log --oneline                                # 7 commits: backend, five Android features, documents
```

Push, then record the video, add its link and tag the milestone:

```powershell
git remote add origin https://github.com/ahsanauddry027/Kaaj-The-Job-Android.git
git push -u origin main
notepad README.md                                # put the Milestone 2 video link at the top
git add README.md
git commit -m "Milestone 2: add the video link"
git tag -a milestone-2 -m "Milestone 2: sign-in, employer verification, post and manage jobs, admin queue"
git push
git push origin milestone-2
```

**Submit:** `https://github.com/ahsanauddry027/Kaaj-The-Job-Android/tree/milestone-2` and the video link.

> **Why the whole `android/` folder goes in at Milestone 2.** The app is one Gradle project; the shared files (`KaajApi.kt`,
> `Models.kt`, `Routes.kt`, `KaajRoot.kt`) already know every screen, so leaving the job seeker screens out would break the build,
> and the instructor may build the tagged snapshot. Milestone 2 asks for "at least 2–3 features"; more is allowed. Show the
> Milestone 2 features in that video and the job seeker side in the final video.

### A5. Final (deadline Sat 12 Dec 2026, 11:59 pm)

Write the report and make any fixes **in `studentapi`** (and push them there as usual), then bring the changes across:

```powershell
cd $a
robocopy "$src\Doc"     "$a\Doc"     /E
robocopy "$src\android" "$a\android" /E /XD build .gradle .idea /XF local.properties
robocopy "$src\src"     "$a\src"     /E
git status                                       # shows what changed
git add -- Doc android src
git commit -m "Final: report, diagrams, updated AI declaration and fixes"
notepad README.md                                # add the final video link next to the Milestone 2 one
git add README.md
git commit -m "Final: add the final video link"
git tag -a final -m "Final submission: job seeker side, applicants, report and diagrams"
git push
git push origin final
```

**Submit:** `https://github.com/ahsanauddry027/Kaaj-The-Job-Android/tree/final` and the video link.

`robocopy /E` copies new and changed files but never deletes. If you **deleted** a file in `studentapi`, delete it in `kaaj-android`
too, then `git add -A -- android src Doc`.

---

## Part W — The web app, per module (CSE471 group repository)

The exact files for each push are already listed (`Doc/Guides/github-files/web/`), and `Copy-Push.ps1` copies them for you. Full
details, what each file does, and fixes: **guide 05, Part W**. In short:

| Push | Module | Files | Branch |
|---|---|---|---|
| 1 | Shared setup + Module 1: employer profile and verification; post and manage jobs | 161 ([list](github-files/web/push-1-setup-and-module-1.txt)) | `member3/module-1` |
| 2 | Module 2: scam screening and moderation | 57: 32 new, 25 changed ([list](github-files/web/push-2-module-2.txt)) | `member3/module-2` |
| 3 | Module 3: AI job-post assistant | 22: 10 new, 12 changed ([list](github-files/web/push-3-module-3.txt)) | `member3/module-3` |

Not pushed to the group repository: the job seeker, applications and search code (teammates' features), `android/`, `Doc/`,
`docs/`, `postman/`, your `README.md`, and of course `.env`.

### W1. Clone the group repository (once)

```powershell
cd $dst
git clone https://github.com/<owner>/<group-repo>.git cse471-kaaj
cd cse471-kaaj
git config user.name "ahsanauddry027"
git config user.email "ahsan.habib1@g.bracu.ac.bd"
```

### W2. One module (repeat for 1, 2 and 3, each only after the previous one is merged)

```powershell
cd "$dst\cse471-kaaj"
git switch main
git pull
git switch -c member3/module-1                     # module-2, module-3 next time
powershell -ExecutionPolicy Bypass -File "$src\Doc\Guides\github-files\web\Copy-Push.ps1" -Push 1 -To .
git status                                         # only that module's files
```

Run it once before committing (the group copy needs its **own** database, because your `kaaj` database has tables it does not):

```powershell
Copy-Item "$src\.env" .env                         # git-ignored
notepad .env                                       # change DB_NAME=kaaj to DB_NAME=kaaj_group (create that empty database once)
.\mvnw.cmd test
cd web; npm ci; npm test; npm run build; cd ..
```

Commit, push and open the pull request:

```powershell
git add -A
git status --short | Select-String '\.env$'        # must print NOTHING
git commit -m "Member-3 Module 1: employer profile and verification, post and manage jobs (with the shared setup)"
git push -u origin member3/module-1
```

On GitHub: **Compare & pull request** → describe the module → **Create pull request** → a teammate reviews → **Merge**.

| Module | Commit message |
|---|---|
| 1 | `Member-3 Module 1: employer profile and verification, post and manage jobs (with the shared setup)` |
| 2 | `Member-3 Module 2: scam screening and moderation (rule checks, optional AI check, reports, admin queue, suspension)` |
| 3 | `Member-3 Module 3: AI job-post assistant (one sentence to a two-language draft, templates without an AI key)` |

For the group's README, add a short section (edit it to match the team's style):

```markdown
### Member-3 — Ahsan Habib (22201027)
- Module 1: employer profile and verification; post and manage jobs (`employer/`, `job/`, web `features/employer-*`)
- Module 2: scam screening and moderation (`trust/`, `JobRuleChecks`, web `ModerationPage`, `ReportButton`)
- Module 3: AI job-post assistant (`JobDrafts`, web `AssistantPanel`)
- Demo: `karim@demo.kaaj`, `quickjobs@demo.kaaj`, password `Kaaj@12345`; admin from `.env`
```

---

## Which file goes where — one table

| Path | `SpringBoot_PostgreSQL_Api` | Android repository | CSE471 group repository |
|---|---|---|---|
| `src/` (backend) | All | All | Only the shared setup and Member-3's modules (push lists) |
| `web/` | Yes | No | Yes (pushes 1–3) |
| `android/` | Yes | Yes (Milestone 2) | No |
| `Doc/` | Yes | Yes | No |
| `docs/` | Yes | No | No |
| `postman/` | Yes | Yes (optional) | No |
| `pom.xml`, `mvnw*`, `.mvn/`, `.gitignore`, `.gitattributes`, `.env.example`, `compose.yaml` | Yes | Yes | Yes (push 1) |
| `README.md` | The full-project README | The Android README (template above) | The group's own, plus your section |
| `.env`, `data/`, `Generated Documents/`, `Claude outputs/` | Never | Never | Never |

## When something goes wrong

| What you see | Fix |
|---|---|
| `src refspec kaaj-the-job does not match any` (Part 0) | You are in the wrong folder, or the branch was already deleted. `cd` to `studentapi` and run `git branch`. |
| `remote: Permission ... denied` (403) | Windows saved another GitHub account. Credential Manager → Windows Credentials → remove `git:https://github.com`, push again. |
| `! [rejected] main -> main (fetch first)` | The new GitHub repository wasn't empty. `git pull --rebase origin main`, then push again. |
| `fatal: not a git repository` | You ran a Git command outside `kaaj-android` or `cse471-kaaj`. Check with `pwd`. |
| `.env` appears in `git status` | `.gitignore` was not copied. Copy it again (Part A2, first line) and check with `git check-ignore -v .env`. |
| `robocopy` printed "ERROR 2" or "ERROR 3" | A path is wrong (often `$src`/`$dst` were not set in this window). Run Part 1 again. |
| `running scripts is disabled on this system` | Use the `powershell -ExecutionPolicy Bypass -File ...` line exactly as written. |
| `Detected applied migration not resolved locally` | The group copy is using your `kaaj` database; set `DB_NAME=kaaj_group` in its `.env`. |
