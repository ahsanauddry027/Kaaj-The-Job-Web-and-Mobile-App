# AI prompts used for Kaaj – The Job

Every prompt I (Ahsan Habib, 22201027) gave an AI tool for this project, in order, with a link to the
full conversation. The links open in my Claude account (claude.ai); I can show any of them in the viva.

Tool for all of them: **Claude** by Anthropic (claude.ai, with its file, code and computer tools).

---

## 1. Idea, proposal, requirements and planning documents

**Chat:** "SpringBoot AI project ideas" — https://claude.ai/chat/7be4e531-6186-4356-a86e-f7cc5df859d3
(October–November 2026)

Main prompts (shortened where the chat went back and forth many times):

> I like the KajKhoj Idea, name it as Kaaj-The Job, And create a draft like FloodLens, And Do not do it
> only for the blue colars, they might get the priority, but design it for people in general with spring
> Boot and Kotlin implementations. with database and AI integration.

> Should I submit this idea then? What I copy pasted some time ago?

> Create A docx file

Later prompts in the same chat asked for: the CSE471 functional-requirements document (12 features in
3 modules), the project context file (`Kaaj_The_Job_Project_Context_1.md`), the three module build-prompt
files (`Kaaj_Module_1/2/3_Build_Prompts.md`), a UI/UX design kit for my two features, and a Windows 11
setup guide. Those documents are in `Doc/source-documents/`.

Outputs used in this repository: the feature list, data model and API design (context file §7, §15, §16).

## 2. Figma screens and a clickable employer console

**Chat:** "Claude and Figma integration" — https://claude.ai/chat/447a3ee9-f555-4b22-9494-d2a947e8d731
(November 2026)

I asked Claude to connect Figma and design my two features (Employer profile and verification; Post and
manage jobs), rename the sample data (contact Ahsan Habib, business "Mayer Dowa Transport") and use our
logo; it then built an HTML "Kaaj Employer Console" when the Figma plan ran out of calls. The screen
layouts guided the web and Android screens.

## 3. Lab Assignment 03 (first Spring Boot REST API)

**Chat:** "Spring Boot MVC assignment" — https://claude.ai/chat/28258d9c-a00f-47d8-a584-f9911414f4c6
(November 2026)

I asked for my two features as a Spring Boot MVC REST API in `studentapi`, with detailed comments
because I was new to Spring Boot, plus a Postman collection and the assignment document. That code
(`com.example.studentapi`) was later replaced by the modular backend in this repository.

## 4. Milestone 1: wireframe and schema diagrams (already submitted)

**Chat:** "Wireframe diagram generation" — https://claude.ai/chat/6ad21479-0d1c-4cca-b680-818c7162a8db
(15 November 2026)

> Generate the Wireframe diagram in this manner as well.

(after asking for the database ERD as an editable draw.io file in the style of a sample diagram).
Outputs: `Kaaj_The_Job_Database_ERD.*`, `Kaaj_The_Job_Android_Wireframe.*` and their generator scripts.
That chat also produced the Milestone 1 AI declaration.

## 5. This build: backend, web app and the Kotlin Android app (Milestone 2 and Final)

**Chat:** the current conversation (18 November 2026) — the one that wrote this file.
Prompts, word for word:

> Can you go through the entire folder? all the items? analyze them, for member 3 ahsan in the function
> requirements.docx file, you have to complete the first two features using Typescript and SpringBoot,
> and tell me how can i manually start the project from scratch step by step. Give me a detailed
> document PDF.

> Check the projects/studentApi folder on Desktop, I have already done spring Boot setup there, delete
> all the unnecessary files, and implement the entire project there.

> Continue from where you left off.

> I am also planning to build a kotlin version with this same backend, and keep both the android and the
> web App version, can you please build the kotlin version entirely? In kotlin version there is only one
> member. These are the instructions: [the lab plan for the rest of the semester: milestone deadlines,
> GitHub access for rahman9909, video lengths, report, wireframe and schema diagrams, and "Ai declaration
> with all prompts and documents should be present with source in /Doc folder"] … Mention each file that
> I need to submit for each milestone. I have already submitted Milestone 1.

Claude then asked three questions; my answers:

| Question | My answer |
|---|---|
| How much should the Kotlin app cover? | Milestone 2 first, then final |
| Which Android UI toolkit? | Jetpack Compose |
| Where should the Android code live? | `android/` folder in studentapi (one repo for backend + web + Android) |

Outputs: everything under `src/`, `web/`, `android/`, `postman/`, `docs/` and this `Doc/` folder.
See `AI_Declaration.md` for exactly what was AI-written and how I checked it.

Later prompts in the same conversation (18 November 2026), word for word:

> Make sure to create a detailed guide of step by step process about, how Can i use the same spring Backend
> for both the WebApp and the Mobile version. What are or might be the exceptions for the WebApp and Mobile
> App in backend, What problems I might face and How to resolve them. And write the github code for pushing
> each of the milestone step by step in github. Also prepare a business model for this WebApp and mobileApp
> as well. And save everything in the directory.

Outputs: `Doc/Guides/01_One_Backend_Two_Apps.*`, `Doc/Guides/02_GitHub_Milestone_Pushes.*`, `Doc/Guides/03_Business_Model.*`.

> make a markdown file of everything that you have done. It should serve as a context for this whole project
> android and WebApp and save every that you have generated in this folder. ALso mention what is still left
> for the Web App version.

Outputs: `Doc/PROJECT_CONTEXT.md`, `Doc/screenshots/web/`, `Doc/generator-scripts/`, and updates to this file,
`AI_Declaration.md`, `Doc/README.md` and the root `README.md`.

> So the Mobile app is completed?

Output: an answer only (every planned screen is written, but the app has never been compiled or run).

> Okay so write a pdf that contains the instruction of step by step procedure to run the backend and both for
> the mobile App and The WebApp.

Output: `Doc/Guides/04_Run_Backend_Web_Android.*`.

> Have you saved everything in that directory? that you generated

Output: a file-by-file check of the folder, and the few missing generated files saved.

> Now Complete the WebApp as well, Only the remaining features of Member 3. And create a guide that tells me
> Which files to push on github for a particular Module along with github instructions. Same for Mobile App as well.

(Sent twice; done once.) Outputs: Module 2 (scam screening and moderation) and Module 3 (AI job-post assistant) in the backend
(`trust/`, `job/JobRuleChecks.java`, `job/internal/JobDrafts.java` and the files listed in `Doc/Guides/github-files/web/push-2-*.txt`
and `push-3-*.txt`) and the web app; `Doc/Guides/05_GitHub_Files_Per_Module.*`; `Doc/Guides/github-files/`; screenshots 20–33 in
`Doc/screenshots/web/`; and updates to this file, `AI_Declaration.md`, `PROJECT_CONTEXT.md`, guides 02 and 04, and the root `README.md`.

> So my mobile version is complete? only testing is what is left? Create a new folder naming Generated Documents
> and store every pdf, markdown file that you generated, that will help me there.

Outputs: an answer, the `Generated Documents/` folder (copies, git-ignored), and the schema diagram redrawn with the Module 2 tables
(`Doc/diagrams/Kaaj_Schema_As_Built.*`).

> Make sure you have followed MVC architecture for the WebApp

Outputs: the MVC check and fixes (`AuthService.java`, `JobSearchService.java`, `web/src/features/jobs/api.ts`,
`web/src/features/taxonomy/api.ts`, and the changed controllers and components), the tests `MvcLayersTest.java` and
`web/src/architecture.test.ts`, `Doc/Guides/06_MVC_Architecture.*`, and updated guide 05 lists.

> Upload this entire folder in github: https://github.com/ahsanauddry027/SpringBoot_PostgreSQL_Api.git, and update the
> readme.MD file. I am planning to copy this folder somewhere else. And push both apps individually there. There I will push it
> based on Module for WebApp and MileStone for Mobile APP. Give me the Guide line as well, which file/ folder to push and what
> should be the instruction.

Outputs: the rewritten `README.md`; the whole folder committed on branch `kaaj-the-job` (pushed by me); `Doc/Guides/07_Push_Both_Apps_Separately.*`;
`Doc/Guides/github-files/android/README_for_the_Android_repository.md`; pointers in guides 02 and 05.

> (Pasted the PowerShell output: `fatal: a branch named 'main' already exists`, `error: remote origin already exists`) How can i fix it?

Output: an answer only (delete the old `Main` branch, rename the branch to `main`, rename the old remote to `assignment3`, add the new
repository `Kaaj-The-Job-Web-and-Mobile-App` as `origin`, push).

> Make the readme.md file look more fancy.

Outputs: the new `README.md`, `Doc/assets/kaaj-banner.svg`, `Doc/screenshots/readme/`; guide 07 and `PROJECT_CONTEXT.md` updated for the
new repository.

> Now make a final .MD file that I can share with my friend who are Working with me on kaaj the Job, Only the Web App version.
> The mobile version is individual for me only. I want to let them know what i have done with the Web App version.

Output: `Doc/Kaaj_Web_App_Member3_Update.md` (also in `Generated Documents/`).

> Have you mentioned all the version of typescript, java, springboot, Maven and stuff?

Output: a "Tech stack and exact versions" section in `Doc/Kaaj_Web_App_Member3_Update.md` (from `pom.xml`, the Maven wrapper,
`web/package-lock.json` and Spring Boot 4.1.2's managed versions), and a build row in `PROJECT_CONTEXT.md`, section 5.

> Have you included how to start/ run both of the along with the backend with POSTGRESQL GUI pgadmin14 from scratch?

Outputs: guide 04 Part 1 rewritten as a pgAdmin 4 walkthrough from installing PostgreSQL, Step A4 (the tables in pgAdmin), more fixes in
Part E; pgAdmin steps in `Doc/Kaaj_Web_App_Member3_Update.md`, `README.md` and the Android repository's README; PostgreSQL 14+ stated as supported.
