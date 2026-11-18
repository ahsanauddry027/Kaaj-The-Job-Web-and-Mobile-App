# Doc — Kaaj – The Job (Kotlin/Android version)

**Start here:** [`PROJECT_CONTEXT.md`](PROJECT_CONTEXT.md) — the whole project in one file: what was built (backend, web app,
Android app), how it was checked, every generated file, and what is still left.

| File | What it is |
|---|---|
| [`PROJECT_CONTEXT.md`](PROJECT_CONTEXT.md) | Project context and work log for the Android app, the web app and the backend; what is left |
| [`Submission_Checklist.md`](Submission_Checklist.md) | Every file to submit for Milestone 2 and the Final, deadlines, and video outlines |
| [`AI_Declaration.md`](AI_Declaration.md) | AI usage declaration, with sources — **fill in and sign before submitting** |
| [`AI_Prompts.md`](AI_Prompts.md) | Every prompt given to the AI, in order, with links to the conversations |
| `source-documents/` | The project documents the AI worked from (requirements, context file, proposal, build prompts) |
| `diagrams/` | Wireframe and schema diagrams: Milestone 1 (ERD, Android wireframe) and the schema as built |
| [`Guides/01_One_Backend_Two_Apps.md`](Guides/01_One_Backend_Two_Apps.md) (+ `.pdf`) | How one Spring Boot backend serves the web app and the Android app: running both, the differences, problems and fixes |
| [`Guides/02_GitHub_Milestone_Pushes.md`](Guides/02_GitHub_Milestone_Pushes.md) (+ `.pdf`) | PowerShell steps to push to GitHub and tag Milestone 2 and the Final |
| [`Guides/03_Business_Model.md`](Guides/03_Business_Model.md) (+ `.pdf`) | Business model for the web app and the Android app: canvas, prices, economics, go-to-market, KPIs, risks |
| [`Guides/04_Run_Backend_Web_Android.md`](Guides/04_Run_Backend_Web_Android.md) (+ `.pdf`) | Step by step: run the backend, the web app and the Android app on Windows 11, with fixes for first-build errors |
| [`Guides/05_GitHub_Files_Per_Module.md`](Guides/05_GitHub_Files_Per_Module.md) (+ `.pdf`) | Which files to push per module (web, CSE471 group repository) and per milestone (Android), with the Git commands; lists and `Copy-Push.ps1` in `Guides/github-files/` |
| [`Guides/07_Push_Both_Apps_Separately.md`](Guides/07_Push_Both_Apps_Separately.md) (+ `.pdf`) | The full project in `SpringBoot_PostgreSQL_Api`; the Android app per milestone in its own repository; the web app per module in the group repository: what to copy, which files go where, the commands |
| [`Guides/06_MVC_Architecture.md`](Guides/06_MVC_Architecture.md) (+ `.pdf`) | How the web app follows MVC: which file is Model, View or Controller, per module, with the tests that keep the layers apart and viva answers |
| `screenshots/web/` | 33 screenshots of the web app's pages, including the Module 2 and 3 screens (useful for the report and the video) |
| [`generator-scripts/`](generator-scripts/README.md) | The scripts the AI used to make the guides, the Android strings, the screenshots and its checks |
| `Report.pdf` | *(Final)* the report on the course template |

`diagrams/draw_schema.py` redraws `Kaaj_Schema_As_Built.*` from a database that has run the migrations
(needs PostgreSQL and Graphviz).
