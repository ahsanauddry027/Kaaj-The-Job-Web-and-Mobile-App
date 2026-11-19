# Every file in the project folder

**Folder:** `C:\Users\UseR\Desktop\Projects\studentapi` · 522 files in 121 folders · listed on 19 November 2026

Paths are relative to that folder. Not listed one by one: the `.git` folder (Git's own history) and the build output that the
tools make and Git ignores: `target\` (Maven), `android\.gradle\` (Gradle) and, once you build them, `web\node_modules\`,
`web\dist\`, `android\build\`, `android\app\build\` and `data\uploads\` (files uploaded while the backend runs).

| Top folder | Files | What it is |
|---|---:|---|
| `(root files)` | 9 | Build files, settings, README |
| `.mvn` | 1 | Maven Wrapper |
| `.vscode` | 1 | VS Code settings (git-ignored) |
| `Claude outputs` | 1 | Assignment 03 report (git-ignored) |
| `Doc` | 146 | Course documents, guides, diagrams, screenshots, scripts |
| `Generated Documents` | 36 | Copies of every PDF and Markdown document (git-ignored) |
| `android` | 99 | Android app (Kotlin + Jetpack Compose) |
| `docs` | 2 | First build guide and plan |
| `postman` | 2 | API requests for testing |
| `src` | 161 | Spring Boot backend and its tests |
| `web` | 64 | Web app (React + TypeScript) |


## Project root

**`(root)/`** — Project root: build files, settings, README  
`.env`, `.env.example`, `.gitattributes`, `.gitignore`, `README.md`, `compose.yaml`, `mvnw`, `mvnw.cmd`, `pom.xml`


## `.mvn`

**`.mvn/wrapper/`** — Maven Wrapper settings (Maven 3.10.0)  
`maven-wrapper.properties`


## `.vscode`

**`.vscode/`** — VS Code settings (git-ignored)  
`settings.json`


## `android`

**`android/`** — Android project settings (Gradle)  
`.gitignore`, `README.md`, `build.gradle.kts`, `gradle.properties`, `gradlew`, `gradlew.bat`, `settings.gradle.kts`

**`android/app/`**  
`build.gradle.kts`, `proguard-rules.pro`

**`android/gradle/`**  
`libs.versions.toml`

**`android/gradle/wrapper/`**  
`gradle-wrapper.jar`, `gradle-wrapper.properties`


## `android/app/src`

**`android/app/src/debug/res/xml/`**  
`network_security_config.xml`

**`android/app/src/main/`**  
`AndroidManifest.xml`

**`android/app/src/main/java/app/kaaj/android/`**  
`KaajApp.kt`, `MainActivity.kt`

**`android/app/src/main/java/app/kaaj/android/data/`**  
`AppContainer.kt`, `DocumentFiles.kt`, `Repositories.kt`

**`android/app/src/main/java/app/kaaj/android/data/model/`**  
`Models.kt`

**`android/app/src/main/java/app/kaaj/android/data/net/`**  
`Interceptors.kt`, `KaajApi.kt`, `KaajCookieJar.kt`, `Network.kt`, `PrefsCookieStorage.kt`

**`android/app/src/main/java/app/kaaj/android/domain/`**  
`Format.kt`, `JobForm.kt`, `ProfileForm.kt`, `SeekerForm.kt`, `Validation.kt`

**`android/app/src/main/java/app/kaaj/android/ui/`**  
`KaajRoot.kt`

**`android/app/src/main/java/app/kaaj/android/ui/admin/`**  
`AdminApp.kt`, `AdminEmployerScreen.kt`, `AdminQueueScreen.kt`

**`android/app/src/main/java/app/kaaj/android/ui/applicants/`**  
`ApplicantsScreens.kt`

**`android/app/src/main/java/app/kaaj/android/ui/auth/`**  
`AuthFlow.kt`

**`android/app/src/main/java/app/kaaj/android/ui/common/`**  
`Common.kt`, `DocumentPicker.kt`, `KaajViewModel.kt`, `Labels.kt`, `MapPicker.kt`, `SeekerLabels.kt`, `TermPicker.kt`

**`android/app/src/main/java/app/kaaj/android/ui/employer/`**  
`DashboardScreen.kt`, `EmployerApp.kt`, `ProfileScreen.kt`

**`android/app/src/main/java/app/kaaj/android/ui/jobs/`**  
`JobActions.kt`, `JobCard.kt`, `JobDetailScreen.kt`, `JobFormScreen.kt`, `MyJobsScreen.kt`

**`android/app/src/main/java/app/kaaj/android/ui/nav/`**  
`Routes.kt`

**`android/app/src/main/java/app/kaaj/android/ui/seeker/`**  
`ApplicationsScreen.kt`, `SavedScreen.kt`, `SearchScreen.kt`, `SeekerApp.kt`, `SeekerJobCard.kt`, `SeekerJobScreen.kt`, `SeekerProfileScreen.kt`

**`android/app/src/main/java/app/kaaj/android/ui/settings/`**  
`SettingsScreen.kt`

**`android/app/src/main/java/app/kaaj/android/ui/theme/`**  
`Theme.kt`

**`android/app/src/main/res/drawable/`**  
`ic_business.xml`, `ic_description.xml`, `ic_edit.xml`, `ic_history.xml`, `ic_image.xml`, `ic_language.xml`, `ic_launcher_background.xml`, `ic_launcher_foreground.xml`, `ic_logout.xml`, `ic_map.xml`, `ic_pause.xml`, `ic_pending.xml`, `ic_photo_camera.xml`, `ic_picture_as_pdf.xml`, `ic_play_arrow.xml`, `ic_send.xml`, `ic_star.xml`, `ic_upload_file.xml`, `ic_verified_user.xml`, `ic_visibility.xml`, `ic_visibility_off.xml`, `ic_work.xml`

**`android/app/src/main/res/mipmap-anydpi-v26/`**  
`ic_launcher.xml`, `ic_launcher_round.xml`

**`android/app/src/main/res/values/`**  
`colors.xml`, `strings.xml`, `strings_seeker.xml`, `themes.xml`

**`android/app/src/main/res/values-bn/`**  
`strings.xml`, `strings_seeker.xml`

**`android/app/src/main/res/xml/`**  
`file_paths.xml`, `locales_config.xml`, `network_security_config.xml`

**`android/app/src/test/java/app/kaaj/android/data/net/`**  
`NetworkTest.kt`

**`android/app/src/test/java/app/kaaj/android/domain/`**  
`FormatTest.kt`, `JobFormTest.kt`, `SeekerFormTest.kt`, `ValidationTest.kt`


## `Claude outputs`

**`Claude outputs/`** — Assignment 03 report (git-ignored)  
`Assignment_03_API_Postman_Collection_22201027.docx`


## `Doc`

**`Doc/`** — Course documents: project context, AI declaration and prompts, submission checklist, team update, this index  
`AI_Declaration.md`, `AI_Prompts.md`, `FILE_INDEX.md`, `Kaaj_Web_App_Member3_Update.md`, `PROJECT_CONTEXT.md`, `README.md`, `Submission_Checklist.md`

**`Doc/assets/`** — Images for the README (banner)  
`kaaj-banner.svg`

**`Doc/diagrams/`** — Milestone 1 wireframe and database diagrams; the schema as built (and the script that draws it)  
`Kaaj_Schema_As_Built.dot`, `Kaaj_Schema_As_Built.pdf`, `Kaaj_Schema_As_Built.png`, `Kaaj_Schema_As_Built.svg`, `Kaaj_The_Job_Android_Wireframe.drawio`, `Kaaj_The_Job_Android_Wireframe_One_Page.pdf`, `Kaaj_The_Job_Android_Wireframe_Preview.png`, `Kaaj_The_Job_Database_ERD.drawio`, `Kaaj_The_Job_Database_ERD_One_Page.pdf`, `Kaaj_The_Job_Database_ERD_Preview.png`, `draw_schema.py`

**`Doc/generator-scripts/`** — Scripts the AI used to make guides, strings, screenshots, file lists and checks  
`README.md`

**`Doc/generator-scripts/android-strings/`**  
`strings_m2.py`, `strings_seeker.py`

**`Doc/generator-scripts/build-guide/`**  
`build_guide.py`, `render.py`

**`Doc/generator-scripts/checks/`**  
`javac_filter.py`, `kcheck.py`, `reset_db.sh`, `taxonomy_search_test.sql`, `web_page_errors.py`

**`Doc/generator-scripts/github-files/`**  
`build_push_trees.py`, `make_android_lists.py`, `make_github_lists.py`

**`Doc/generator-scripts/guides/`**  
`render_md.py`

**`Doc/generator-scripts/web-screenshots/`**  
`mock-api-server.mjs`, `sample-licence.png`, `shoot.py`, `shoot_member3_modules.py`

**`Doc/Guides/`** — Guides 01–07 (Markdown + PDF)  
`01_One_Backend_Two_Apps.md`, `01_One_Backend_Two_Apps.pdf`, `02_GitHub_Milestone_Pushes.md`, `02_GitHub_Milestone_Pushes.pdf`, `03_Business_Model.md`, `03_Business_Model.pdf`, `04_Run_Backend_Web_Android.md`, `04_Run_Backend_Web_Android.pdf`, `05_GitHub_Files_Per_Module.md`, `05_GitHub_Files_Per_Module.pdf`, `06_MVC_Architecture.md`, `06_MVC_Architecture.pdf`, `07_Push_Both_Apps_Separately.md`, `07_Push_Both_Apps_Separately.pdf`

**`Doc/Guides/github-files/android/`** — Android commit lists (one per feature) and the README for the Android repository  
`1-app-shell-and-sign-in.txt`, `2-employer-profile-and-verification.txt`, `3-post-and-manage-jobs.txt`, `4-admin-verification-queue.txt`, `5-job-seeker-and-applicants.txt`, `README_for_the_Android_repository.md`

**`Doc/Guides/github-files/web/`** — Web push lists (one per CSE471 module) and Copy-Push.ps1  
`Copy-Push.ps1`, `push-1-setup-and-module-1.txt`, `push-2-module-2.txt`, `push-3-module-3.txt`

**`Doc/Guides/img/`** — Pictures used inside the guides  
`mvc_web_app.svg`, `one_backend_two_apps.svg`

**`Doc/screenshots/readme/`** — Six screenshots used in the README gallery  
`01-employer-dashboard.png`, `02-post-a-job.png`, `03-ai-job-post-assistant.png`, `04-moderation-queue.png`, `05-why-in-review.png`, `06-verification-queue.png`

**`Doc/screenshots/web/`** — All 33 web app screenshots  
`01-login.png`, `02-dashboard.png`, `03-profile-verified.png`, `04-my-jobs.png`, `05-my-jobs-menu.png`, `06-wizard-step1-errors.png`, `07-wizard-step1.png`, `08-wizard-step2.png`, `09-wizard-step3.png`, `10-wizard-step4.png`, `11-job-detail-published.png`, `12-profile-pending.png`, `13-draft-unverified.png`, `14-admin-queue.png`, `15-admin-reason-required.png`, `16-admin-approved.png`, `17-public-job.png`, `18-profile-english.png`, `19-mobile-my-jobs.png`, `20-assistant-empty.png`, `21-assistant-draft.png`, `22-assistant-filled-form.png`, `23-assistant-filled-step2.png`, `24-assistant-blocked.png`, `25-screening-notice.png`, `26-report-form.png`, `27-report-sent.png`, `28-moderation-queue.png`, `29-moderation-case-highlights.png`, `30-moderation-note-required.png`, `31-moderation-confirm-suspend.png`, `32-moderation-decided.png`, `33-moderation-english.png`

**`Doc/source-documents/`** — The planning documents the project was built from  
`CSE471_Group08_Functional_Requirements_Kaaj_The_Job.docx`, `Kaaj_Module_1_Build_Prompts.md`, `Kaaj_Module_2_Build_Prompts.md`, `Kaaj_Module_3_Build_Prompts.md`, `Kaaj_The_Job_Project_Context_1.md`, `Kaaj_The_Job_Project_Proposal_and_Technical_Design_1.docx`, `Kaaj_Windows11_Setup_Guide.pdf`


## `Doc/Guides/github-files/web/versions`

**`Doc/Guides/github-files/web/versions/push-1/`**  
`.env.example`

**`Doc/Guides/github-files/web/versions/push-1/src/main/java/app/kaaj/identity/internal/`**  
`AppUser.java`, `SecurityConfig.java`

**`Doc/Guides/github-files/web/versions/push-1/src/main/java/app/kaaj/job/`**  
`JobModeration.java`, `JobStatus.java`, `package-info.java`

**`Doc/Guides/github-files/web/versions/push-1/src/main/java/app/kaaj/job/internal/`**  
`Job.java`, `JobEventListeners.java`, `JobProperties.java`, `JobService.java`

**`Doc/Guides/github-files/web/versions/push-1/src/main/java/app/kaaj/shared/`**  
`KaajException.java`, `Taxonomy.java`

**`Doc/Guides/github-files/web/versions/push-1/src/main/java/app/kaaj/shared/internal/`**  
`JdbcTaxonomy.java`

**`Doc/Guides/github-files/web/versions/push-1/src/main/resources/`**  
`application.properties`, `messages.properties`, `messages_bn.properties`

**`Doc/Guides/github-files/web/versions/push-1/src/test/java/app/kaaj/job/`**  
`JobStatusTest.java`

**`Doc/Guides/github-files/web/versions/push-1/web/src/`**  
`router.tsx`

**`Doc/Guides/github-files/web/versions/push-1/web/src/components/`**  
`AppLayout.tsx`

**`Doc/Guides/github-files/web/versions/push-1/web/src/features/admin-trust/`**  
`api.ts`

**`Doc/Guides/github-files/web/versions/push-1/web/src/features/employer-jobs/`**  
`JobDetailPage.tsx`, `JobFormPage.tsx`, `api.ts`, `schema.ts`

**`Doc/Guides/github-files/web/versions/push-1/web/src/features/jobs/`**  
`PublicJobPage.tsx`, `api.ts`

**`Doc/Guides/github-files/web/versions/push-1/web/src/i18n/`**  
`bn.json`, `en.json`

**`Doc/Guides/github-files/web/versions/push-1/web/src/lib/`**  
`types.ts`

**`Doc/Guides/github-files/web/versions/push-2/src/main/java/app/kaaj/job/internal/`**  
`JobProperties.java`

**`Doc/Guides/github-files/web/versions/push-2/src/main/resources/`**  
`application.properties`, `messages.properties`, `messages_bn.properties`

**`Doc/Guides/github-files/web/versions/push-2/web/src/features/employer-jobs/`**  
`api.ts`

**`Doc/Guides/github-files/web/versions/push-2/web/src/i18n/`**  
`bn.json`, `en.json`

**`Doc/Guides/github-files/web/versions/push-2/web/src/lib/`**  
`types.ts`


## `docs`

**`docs/`** — The first build guide (PDF)  
`Kaaj_Member3_Build_Guide.pdf`

**`docs/plans/`** — Plan for Member-3's first two features  
`M1-3_M1-4_Member3.md`


## `Generated Documents`

**`Generated Documents/`** — Copies of every PDF and Markdown document, for reading (git-ignored)  
`FILE_INDEX.md`, `Kaaj_Web_App_Member3_Update.md`, `README.md`

**`Generated Documents/1 Start here/`**  
`PROJECT_CONTEXT.md`, `Project_README.md`, `Submission_Checklist.md`

**`Generated Documents/2 Guides/`**  
`01_One_Backend_Two_Apps.md`, `01_One_Backend_Two_Apps.pdf`, `02_GitHub_Milestone_Pushes.md`, `02_GitHub_Milestone_Pushes.pdf`, `03_Business_Model.md`, `03_Business_Model.pdf`, `04_Run_Backend_Web_Android.md`, `04_Run_Backend_Web_Android.pdf`, `05_GitHub_Files_Per_Module.md`, `05_GitHub_Files_Per_Module.pdf`, `06_MVC_Architecture.md`, `06_MVC_Architecture.pdf`, `07_Push_Both_Apps_Separately.md`, `07_Push_Both_Apps_Separately.pdf`, `Android_README.md`, `Kaaj_Member3_Build_Guide.pdf`, `README_for_the_Android_repository.md`

**`Generated Documents/2 Guides/img/`**  
`mvc_web_app.svg`, `one_backend_two_apps.svg`

**`Generated Documents/3 AI declaration and prompts/`**  
`AI_Declaration.md`, `AI_Prompts.md`

**`Generated Documents/4 Diagrams/`**  
`Kaaj_Schema_As_Built.pdf`, `Kaaj_The_Job_Android_Wireframe_One_Page.pdf`, `Kaaj_The_Job_Database_ERD_One_Page.pdf`

**`Generated Documents/5 Project design/`**  
`Kaaj_Module_1_Build_Prompts.md`, `Kaaj_Module_2_Build_Prompts.md`, `Kaaj_Module_3_Build_Prompts.md`, `Kaaj_The_Job_Project_Context_1.md`, `Kaaj_Windows11_Setup_Guide.pdf`, `M1-3_M1-4_Member3_plan.md`


## `postman`

**`postman/`** — API requests for Postman, and a sample trade licence image  
`Kaaj_Member3.postman_collection.json`, `sample-trade-licence.png`


## `src`

**`src/main/resources/`** — Backend settings and API messages (English, Bangla)  
`application-dev.properties`, `application.properties`, `messages.properties`, `messages_bn.properties`

**`src/main/resources/db/demo/`** — Demo data (dev profile only)  
`R__demo_member3.sql`, `R__demo_seeker.sql`, `R__demo_trust.sql`

**`src/main/resources/db/migration/`** — Flyway migrations: every database table  
`V130__employer.sql`, `V140__job.sql`, `V150__seeker.sql`, `V160__application.sql`, `V1__core.sql`, `V240__trust.sql`, `V2__taxonomy.sql`, `V3__seed_taxonomy.sql`


## `src/main/java/app/kaaj`

**`src/main/java/app/kaaj/`**  
`KaajApplication.java`

**`src/main/java/app/kaaj/application/`**  
`package-info.java`

**`src/main/java/app/kaaj/application/internal/`**  
`Application.java`, `ApplicationEvents.java`, `ApplicationRepository.java`, `ApplicationService.java`, `ApplicationStatus.java`, `ApplicationViews.java`, `EmployerApplicantController.java`, `Match.java`, `SeekerApplicationController.java`

**`src/main/java/app/kaaj/employer/`**  
`EmployerDirectory.java`, `EmployerKind.java`, `EmployerRejected.java`, `EmployerSummary.java`, `EmployerVerified.java`, `VerificationStatus.java`, `package-info.java`

**`src/main/java/app/kaaj/employer/internal/`**  
`AdminEmployerController.java`, `Employer.java`, `EmployerDocumentAccessPolicy.java`, `EmployerProfileController.java`, `EmployerProfileService.java`, `EmployerRepository.java`, `EmployerVerificationService.java`

**`src/main/java/app/kaaj/identity/`**  
`AuditLog.java`, `CurrentUser.java`, `Role.java`, `UserAdmin.java`, `UserDirectory.java`, `package-info.java`

**`src/main/java/app/kaaj/identity/internal/`**  
`AdminProperties.java`, `AppUser.java`, `AppUserRepository.java`, `AuthController.java`, `AuthProperties.java`, `AuthService.java`, `FirstAdminInitializer.java`, `JdbcAuditLog.java`, `JpaUserDirectory.java`, `KaajUserDetailsService.java`, `SecurityConfig.java`, `SuspendedUserFilter.java`, `UserAdminService.java`, `UserStatusCache.java`

**`src/main/java/app/kaaj/job/`**  
`Finding.java`, `JobContent.java`, `JobDirectory.java`, `JobModeration.java`, `JobPublished.java`, `JobRuleChecks.java`, `JobStatus.java`, `JobSubmitted.java`, `JobSummary.java`, `JobType.java`, `Shift.java`, `package-info.java`

**`src/main/java/app/kaaj/job/internal/`**  
`DraftTemplate.java`, `EmployerJobController.java`, `Job.java`, `JobDetails.java`, `JobDraftController.java`, `JobDrafts.java`, `JobEventListeners.java`, `JobExpiryScheduler.java`, `JobProperties.java`, `JobRepository.java`, `JobRules.java`, `JobSearch.java`, `JobSearchController.java`, `JobSearchService.java`, `JobService.java`, `JobViews.java`, `OccupationFinder.java`, `PublicJobController.java`, `SavedJobs.java`, `SentenceFacts.java`

**`src/main/java/app/kaaj/seeker/`**  
`SeekerDirectory.java`, `SeekerSummary.java`, `package-info.java`

**`src/main/java/app/kaaj/seeker/internal/`**  
`Completeness.java`, `SeekerProfile.java`, `SeekerProfileController.java`, `SeekerProfileRepository.java`, `SeekerProfileService.java`

**`src/main/java/app/kaaj/shared/`**  
`AiText.java`, `Cursor.java`, `CursorPage.java`, `DocumentCode.java`, `FileAccessPolicy.java`, `FileKind.java`, `FileStore.java`, `Json.java`, `KaajException.java`, `ScamPatterns.java`, `Taxonomy.java`, `Term.java`, `TermNames.java`, `UuidV7.java`, `WagePeriod.java`, `Wages.java`, `package-info.java`

**`src/main/java/app/kaaj/shared/internal/`**  
`AiProperties.java`, `ApiExceptionHandler.java`, `DiskFileStore.java`, `FileController.java`, `FileProperties.java`, `JdbcTaxonomy.java`, `OpenAiCompatibleText.java`, `SharedConfiguration.java`, `SpaForwardController.java`, `TaxonomyController.java`

**`src/main/java/app/kaaj/trust/`**  
`JobRemovedAsScam.java`, `package-info.java`

**`src/main/java/app/kaaj/trust/internal/`**  
`CaseStore.java`, `ScamAssessment.java`, `ScamReviewer.java`, `Screening.java`, `TrustControllers.java`, `TrustProperties.java`, `TrustService.java`, `TrustViews.java`


## `src/test/java/app/kaaj`

**`src/test/java/app/kaaj/`**  
`JobDraftFlowTests.java`, `Member3FlowTests.java`, `ModularityTests.java`, `MvcLayersTest.java`, `SeekerFlowTests.java`, `TrustFlowTests.java`

**`src/test/java/app/kaaj/application/internal/`**  
`ApplicationStatusTest.java`, `MatchTest.java`

**`src/test/java/app/kaaj/employer/internal/`**  
`EmployerTest.java`

**`src/test/java/app/kaaj/job/`**  
`JobRuleChecksTest.java`, `JobStatusTest.java`

**`src/test/java/app/kaaj/job/internal/`**  
`JobDraftsTest.java`, `JobRulesTest.java`, `JobSearchTest.java`, `JobTest.java`

**`src/test/java/app/kaaj/seeker/internal/`**  
`SeekerProfileTest.java`

**`src/test/java/app/kaaj/shared/`**  
`CursorTest.java`, `DocumentCodeTest.java`, `JsonTest.java`, `ScamPatternsTest.java`, `UuidV7Test.java`, `WagesTest.java`

**`src/test/java/app/kaaj/shared/internal/`**  
`DiskFileStoreTest.java`

**`src/test/java/app/kaaj/trust/internal/`**  
`ScreeningTest.java`


## `web`

**`web/`** — Web app settings (React + TypeScript + Vite)  
`.gitignore`, `.oxlintrc.json`, `index.html`, `package-lock.json`, `package.json`, `tsconfig.app.json`, `tsconfig.json`, `tsconfig.node.json`, `vite.config.ts`

**`web/public/`** — Web app icon  
`favicon.svg`


## `web/src`

**`web/src/`**  
`architecture.test.ts`, `index.css`, `main.tsx`, `router.tsx`

**`web/src/components/`**  
`AppLayout.tsx`, `FileUpload.test.ts`, `FileUpload.tsx`, `FindingList.tsx`, `MapPicker.tsx`, `ReportButton.tsx`, `StatusBadge.tsx`, `TaxonomyPicker.tsx`, `Toast.tsx`, `ui.tsx`

**`web/src/features/admin-trust/`**  
`AdminVerificationPage.tsx`, `ModerationPage.tsx`, `api.ts`

**`web/src/features/auth/`**  
`AuthPages.tsx`, `RequireRole.tsx`, `useCurrentUser.ts`

**`web/src/features/employer-jobs/`**  
`AssistantPanel.tsx`, `DashboardPage.tsx`, `JobActions.tsx`, `JobCard.tsx`, `JobDetailPage.tsx`, `JobFormPage.tsx`, `MyJobsPage.tsx`, `api.ts`, `assistant.test.tsx`, `schema.test.ts`, `schema.ts`

**`web/src/features/employer-profile/`**  
`EmployerProfilePage.tsx`, `VerificationPanel.test.tsx`, `VerificationPanel.tsx`, `api.ts`, `schema.test.ts`, `schema.ts`

**`web/src/features/home/`**  
`HomePage.tsx`

**`web/src/features/jobs/`**  
`PublicJobPage.tsx`, `api.ts`

**`web/src/features/taxonomy/`**  
`api.ts`

**`web/src/i18n/`**  
`bn.json`, `en.json`, `i18n.test.ts`, `index.ts`

**`web/src/lib/`**  
`api.ts`, `cn.ts`, `format.test.ts`, `format.ts`, `trust.test.ts`, `trust.ts`, `types.ts`, `useLang.ts`

**`web/src/test/`**  
`setup.ts`

