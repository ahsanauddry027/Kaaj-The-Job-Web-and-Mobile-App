# Pushing each milestone to GitHub, step by step

**Kaaj – The Job** (Kotlin/Android version + Spring Boot backend) · Ahsan Habib (22201027) · GitHub: `ahsanauddry027` · written 18 November 2026

Every command below is for **PowerShell** on Windows 11. Copy one block at a time and read what it prints before going on.

> **Plan changed on 19 November 2026.** The whole folder is now in `Kaaj-The-Job-Web-and-Mobile-App` (your full copy; `SpringBoot_PostgreSQL_Api` stays as Assignment 03), and the Android
> app goes to its own repository from a separate copy, per milestone. Follow **`07_Push_Both_Apps_Separately.md`** for that.
> This guide's Part B step 3 (video), Part B step 5 (APK release), Part C, Part D step 3 and Part E (fixes) still apply.

## The course rules this guide follows

| Rule | How this guide handles it |
|---|---|
| Submissions go to Git, **public**, or shared with **`rahman9909`** | Step 3 (choose public, or private + collaborator) |
| Milestone 2: **Sat 5 Dec 2026, 11:59 pm** — registration/login + 2–3 features + 5–8 min video | Part B, tag `milestone-2` |
| Final: **Sat 12 Dec 2026, 11:59 pm** — code, report, wireframe and schema diagrams + 8–10 min video | Part D, tag `final` |
| AI declaration with all prompts and documents in `/Doc` | Already in `Doc/`; Part B step 2 and Part D step 1 check it |
| Milestone 1 | Already submitted; nothing to do |

**Where things stand today (checked 18 Nov 2026):**

- Your `studentapi` folder is a Git repository on branch **`Main`**, with 6 commits from Assignment 03.
- Its `origin` points to **`https://github.com/ahsanauddry027/SpringBoot_PostgreSQL_Api.git`**, your Assignment 03 repository.
- All the Kaaj work is **not committed yet**: 19 old Assignment 03 files are deleted, 5 files are changed, and `android/`, `Doc/`, `web/`, `src/main/java/app/`, `src/test/java/app/` and others are new.
- `.env` has **never** been committed (good), and it is in `.gitignore`.

> **Recommendation: use a new repository.** If you pushed Kaaj into `SpringBoot_PostgreSQL_Api`, the Assignment 03 code your
> instructor may still look at would disappear from its main page. A new repository named `Kaaj-The-Job` keeps Assignment 03 untouched.
> Your old commits come along as history, which is fine.

---

## Part A — One-time setup (today)

### Step 1 — Check Git and set your name

```powershell
git --version
git config --global user.name "ahsanauddry027"
git config --global user.email "ahsan.habib1@g.bracu.ac.bd"
git config --global init.defaultBranch main
git config --global --list
```

The email must be one that is added to your GitHub account (GitHub → Settings → Emails), or your commits won't show your profile picture.

### Step 2 — Tidy the repository before the first commit

```powershell
cd C:\Users\UseR\Desktop\Projects\studentapi
git gc --prune=now          # removes a few leftover temporary files inside .git
git status
```

`git status` should list the deleted `com/example/studentapi` files, the changed files and the new folders. It must **not** list
`.env`, `data/`, `Claude outputs/`, `web/node_modules/`, `target/` or `android/local.properties`.

Prove the secret and generated files are ignored (each line printed means "ignored, and by which rule"):

```powershell
git check-ignore -v .env data "Claude outputs" android/local.properties
```

You should see four lines, for example `.gitignore:36:.env    .env`. If `.env` is **not** printed, stop and add a line `.env` to
`.gitignore` before going on. (Folders that don't exist yet, such as `target` or `web/node_modules`, are not listed by this command
even though `.gitignore` covers them.)

### Step 3 — Create the empty repository on GitHub

1. Go to <https://github.com/new>.
2. Owner `ahsanauddry027`, repository name **`Kaaj-The-Job`**, description "Kaaj – The Job: Android (Kotlin/Compose) + web (React) + Spring Boot".
3. Choose the visibility:
   - **Public**: the simplest; the instructor can open it at any time with no invitation. Anyone else can see your code too.
   - **Private**: then add the instructor in step 6. Your code stays hidden from other students, but the instructor must accept the invitation.
4. **Do not** tick "Add a README", ".gitignore" or "license". The repository must be empty, or your first push will be rejected.
5. Click **Create repository**.

(If you have the GitHub CLI, `gh repo create Kaaj-The-Job --private` does the same.)

### Step 4 — Point your folder at the new repository

```powershell
git remote rename origin assignment3
git remote add origin https://github.com/ahsanauddry027/Kaaj-The-Job.git
git remote -v
```

You should see `origin` → `Kaaj-The-Job.git` and `assignment3` → `SpringBoot_PostgreSQL_Api.git`. To make sure you never push
Kaaj into Assignment 03 by mistake, you can remove the old one: `git remote remove assignment3`.

Rename the branch from `Main` to `main`. Windows ignores letter case in file names, so do it in two moves:

```powershell
git branch -m Main temp-main
git branch -m temp-main main
git branch --unset-upstream
git branch
```

### Step 5 — Run the tests (so the first push is a working one)

```powershell
.\mvnw.cmd test
cd android; .\gradlew.bat test; cd ..
cd web; npm ci; npm test; cd ..
```

- The backend tests that need Docker are skipped if Docker Desktop is not running; that's fine.
- If the Android build fails on `compileSdk { version = release(37) }`, change it to `compileSdk = 37` in `android/app/build.gradle.kts`.
- If something fails and you can't fix it today, push anyway (it is a backup), fix it, and push again before the deadline.

### Step 6 — Make the first commits and push

Commit in a few logical pieces instead of one giant commit. Your instructor sees a history that explains itself.

```powershell
# 1. Remove the Assignment 03 sample code
git add -A -- src/main/java/com src/test/java/com postman/Kaaj_API_Assignment_03.postman_collection.json
git commit -m "Remove the Assignment 03 sample code"

# 2. The Spring Boot backend shared by both apps
git add -- pom.xml .gitignore .gitattributes .env.example compose.yaml src
git commit -m "Backend: sign-in, employer verification, jobs, job seeker profile and applications (Spring Boot 4.1)"

# 3. The Android app (or one commit per feature: guide 05, Part A2)
git add -- android
git commit -m "Android app: Kotlin + Jetpack Compose client for job seekers, employers and admins"

# 4. The web app (CSE471 group project)
git add -- web
git commit -m "Web app: React + TypeScript client for employers and admins"

# 5. Documents, diagrams, AI declaration, Postman
git add -- Doc docs postman README.md
git commit -m "Docs: README, AI declaration and prompts, diagrams, guides, Postman collection"
```

Before pushing, check that nothing is left out and nothing secret went in:

```powershell
git status                                     # should say "nothing to commit, working tree clean"
git log --oneline -8
git ls-files | Select-String -Pattern '(^|/)\.env$'   # must print NOTHING
git ls-files | Measure-Object                  # how many files are tracked
```

Push and connect your `main` to GitHub's `main`:

```powershell
git push -u origin main
```

The first push opens a browser window to sign in to GitHub (Git Credential Manager). After that, `git push` alone is enough.

Open `https://github.com/ahsanauddry027/Kaaj-The-Job` and check that `android/`, `src/`, `web/`, `Doc/` and `README.md` are there and that `.env` is **not**.

### Step 7 — Give the instructor access (private repository only)

1. On the repository page: **Settings → Collaborators** (under "Access") → **Add people**.
2. Type **`rahman9909`**, pick the account, click **Add rahman9909 to this repository**.
3. The instructor gets an invitation and must accept it. You can see "Pending invite" on the same page until then.

If the invitation is still pending two days before a deadline, ask the instructor, or switch the repository to public:
**Settings → General → Danger Zone → Change visibility**.

---

## Part B — Milestone 2 (deadline Sat 5 Dec 2026, 11:59 pm)

### Step 1 — Keep committing while you work (every day)

```powershell
git status
git add -- android/app/src/main/java/app/kaaj/android/ui/jobs   # or whichever files you changed
git commit -m "Jobs: show the days left on each card"
git push
```

Short messages in the form "Area: what changed" are easy to read later. Push at least once a day: GitHub is your backup.

### Step 2 — Check the Milestone 2 files are in the repository

The full list is in `Doc/Submission_Checklist.md`. The required ones:

| Path | What |
|---|---|
| `android/` (+ `android/README.md`) | The Kotlin + Jetpack Compose app |
| `src/`, `pom.xml`, `mvnw`, `mvnw.cmd`, `.mvn/` | The backend the app needs |
| `.env.example` | Settings template (the real `.env` stays on your PC) |
| `README.md` | Overview, how to run, the API list, **the video link** |
| `Doc/AI_Declaration.md` | **Filled in and signed** (section 2 and section 5) |
| `Doc/AI_Prompts.md` | Every prompt, with links to the chats (add any new ones) |
| `Doc/source-documents/` | The documents given to the AI |

```powershell
git ls-files android/README.md README.md .env.example Doc/AI_Declaration.md Doc/AI_Prompts.md
```

All five paths must be printed.

### Step 3 — Record the video and add its link

1. Record 5–8 minutes (the outline is in `Doc/Submission_Checklist.md`): registration and login, employer profile and verification,
   post and manage jobs, the admin queue, a short code tour, and the next plan.
2. Upload to YouTube as **Unlisted** (or Google Drive with "Anyone with the link can view").
3. Put the link at the top of `README.md`, under the title:
   ```markdown
   **Milestone 2 video (5–8 min):** https://youtu.be/XXXXXXXXXXX
   ```
4. Commit and push:
   ```powershell
   git add README.md
   git commit -m "Milestone 2: add the video link"
   git push
   ```

### Step 4 — Tag the Milestone 2 snapshot

A tag is a permanent name for one commit, so the instructor can see exactly what you had at the deadline even after you keep working.

```powershell
git status                       # must be clean
git log --oneline -3             # the top commit is the one you want to submit
git tag -a milestone-2 -m "Milestone 2: login + employer verification + post/manage jobs + admin queue"
git push origin milestone-2
git ls-remote --tags origin      # shows refs/tags/milestone-2
```

**Submit this link:** `https://github.com/ahsanauddry027/Kaaj-The-Job/tree/milestone-2`
(and the video link, wherever the course asks for it).

### Step 5 — Optional: a release with the APK

Built app files must not be committed (`*.apk` is in `android/.gitignore`), but you can attach one to a GitHub release so the instructor can install it without building:

```powershell
cd android
.\gradlew.bat assembleDebug
cd ..
# The file is android\app\build\outputs\apk\debug\app-debug.apk
```

On GitHub: **Releases → Draft a new release → Choose a tag: `milestone-2` → title "Milestone 2" → drag `app-debug.apk` into the box → Publish release**.
With the GitHub CLI: `gh release create milestone-2 android\app\build\outputs\apk\debug\app-debug.apk --title "Milestone 2" --notes "Debug APK; the backend must be running"`.

---

## Part C — Between the milestones (6–12 Dec)

- Work on `main` and push every day, as in Part B step 1.
- Want to try something risky? Use a branch, so `main` always works:
  ```powershell
  git switch -c try/voice-input      # new branch
  # ... work, commit ...
  git push -u origin try/voice-input
  git switch main
  git merge try/voice-input          # only when it works
  git push
  git branch -d try/voice-input
  ```
- Never move or delete the `milestone-2` tag after 5 Dec: it is your submitted snapshot.

---

## Part D — Final submission (deadline Sat 12 Dec 2026, 11:59 pm)

### Step 1 — Add the final documents

| Path | What |
|---|---|
| `Doc/Report.pdf` (+ `Doc/Report.docx`) | The report on the course template (get the template from the course page or the instructor) |
| `Doc/diagrams/Kaaj_The_Job_Android_Wireframe_One_Page.pdf` (+ `.drawio`, `.png`) | Wireframe diagram |
| `Doc/diagrams/Kaaj_The_Job_Database_ERD_One_Page.pdf` (+ `.drawio`, `.png`) | Schema diagram |
| `Doc/diagrams/Kaaj_Schema_As_Built.pdf` (+ `.png`, `.svg`) | The schema as built (recommended) |
| `Doc/AI_Prompts.md`, `Doc/AI_Declaration.md` | Updated with every prompt since Milestone 2, signed again |
| `README.md` | The **final** video link (8–10 min) added next to the Milestone 2 one |

```powershell
git add -- Doc README.md
git commit -m "Final: report, diagrams, updated AI declaration and the final video link"
git push
```

### Step 2 — Tag the final snapshot

```powershell
git status
git tag -a final -m "Final submission: job seeker side, applicants, report and diagrams"
git push origin final
git ls-remote --tags origin      # shows milestone-2 and final
```

**Submit this link:** `https://github.com/ahsanauddry027/Kaaj-The-Job/tree/final`
Optionally make a release from the `final` tag with the APK, as in Part B step 5.

### Step 3 — Check it as the instructor will see it

- Open the repository in a private/incognito window (public repository), or check the collaborator shows as accepted (private repository).
- Click **Tags**: `milestone-2` and `final` are both there.
- Open `README.md` on GitHub: both video links work.
- Download the ZIP of the `final` tag (Code → Download ZIP while viewing the tag), unzip it somewhere else and follow `README.md` once.
  This is the surest way to find a missing file.

---

## Part E — When something goes wrong

| What you see | Fix |
|---|---|
| `! [rejected] main -> main (fetch first)` | The GitHub repository isn't empty (it was created with a README). Run `git pull --rebase origin main`, then `git push`. |
| `fatal: refusing to merge unrelated histories` | Same cause. Run `git pull origin main --allow-unrelated-histories`, fix any conflict, commit, push. |
| `error: src refspec main does not match any` | There is no `main` branch yet (no commit, or the branch is still called `Main`). Check with `git branch`, then redo Part A step 4 or step 6. |
| `remote: Permission to ahsanauddry027/Kaaj-The-Job.git denied` (403) | Windows saved a different GitHub account. Open **Credential Manager → Windows Credentials**, remove `git:https://github.com`, push again and sign in as `ahsanauddry027`. |
| `Unable to create '.git/index.lock': File exists` | Another Git program (VS Code, Android Studio) was busy or crashed. Close them, then `Remove-Item .git\index.lock`. |
| `warning: LF will be replaced by CRLF` | Harmless. `.gitattributes` decides line endings (`gradlew` and `mvnw` keep LF so they still run on Linux and macOS). |
| `File ... is 1xx.xx MB; this exceeds GitHub's file size limit of 100.00 MB` | A big file (a video, an APK, a database dump) was committed. Undo the last commit with `git reset --soft HEAD~1`, then `git restore --staged <file>`, add the file's name to `.gitignore`, and commit again. Share big files through Drive or a release. |
| You committed `.env` and have **not** pushed | `git rm --cached .env`, then `git commit --amend --no-edit`. Check with `git ls-files \| Select-String "\.env$"`. |
| You committed `.env` and **pushed** it | Treat the passwords as public: change your PostgreSQL password and the admin password now, and update `.env`. Then `git rm --cached .env`, commit, push. (The old copy is still in the history; for a course project, changing the passwords is the part that matters.) |
| Wrong commit message, not pushed yet | `git commit --amend -m "Better message"` |
| Last commit was a mistake, not pushed yet | `git reset --soft HEAD~1` (your changes stay in the files) |
| You tagged the wrong commit **before** the deadline | `git tag -d milestone-2`, `git push origin :refs/tags/milestone-2`, then tag the right commit and push the tag again. Never do this after the deadline. |
| A file you deleted should come back | `git restore <path>` (only for files Git knew about) |
| `git push` goes to the wrong repository | `git remote -v` shows where; fix with `git remote set-url origin https://github.com/ahsanauddry027/Kaaj-The-Job.git` |

---

## Part F — Your web code in the CSE471 group repository

Don't push this whole folder to the group repository. `05_GitHub_Files_Per_Module.md` (Part W) lists exactly which files go in for
each of your modules (shared setup + Module 1, Module 2, Module 3), copies them into a clone of the group repository with one command,
and gives the branch, commit, pull request and tag commands for each.

---

## Command summary

| When | Commands |
|---|---|
| Once | `git config --global user.name ...`, `user.email ...`; create the empty repo; `git remote rename origin assignment3`; `git remote add origin <new url>`; rename `Main` → `main` |
| First push | `git check-ignore -v .env`; commits in pieces; `git push -u origin main` |
| Every day | `git status` → `git add <files>` → `git commit -m "Area: what changed"` → `git push` |
| Milestone 2 | video link in README → commit → push → `git tag -a milestone-2 -m "..."` → `git push origin milestone-2` |
| Final | report + diagrams + AI files → commit → push → `git tag -a final -m "..."` → `git push origin final` |
| Check | `git log --oneline -5`, `git ls-remote --tags origin`, view the repo in a private window |

### Sources

- GitHub Docs, inviting collaborators to a personal repository: <https://docs.github.com/en/repositories/managing-your-repositorys-settings-and-features/repository-access-and-collaboration/inviting-collaborators-to-a-personal-repository>
