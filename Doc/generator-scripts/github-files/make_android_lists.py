# Splits android/ into one list per feature, for one commit per feature (guide 05, part A).
import os, sys
ROOT = '/home/claude/kaaj'; OUT = ROOT + '/Doc/Guides/github-files/android'
A = 'android/app/src/main/java/app/kaaj/android/'; RES = 'android/app/src/main/res/'; TEST = 'android/app/src/test/java/app/kaaj/android/'
GROUPS = [
    ('1-app-shell-and-sign-in', 'The app shell, networking (session cookie + CSRF), Bangla/English, settings, and registration + sign-in', [
        'android/.gitignore', 'android/README.md', 'android/build.gradle.kts', 'android/settings.gradle.kts', 'android/gradle.properties',
        'android/gradle/', 'android/gradlew', 'android/gradlew.bat', 'android/app/build.gradle.kts', 'android/app/proguard-rules.pro',
        'android/app/src/debug/', 'android/app/src/main/AndroidManifest.xml',
        A + 'KaajApp.kt', A + 'MainActivity.kt', A + 'data/AppContainer.kt', A + 'data/Repositories.kt', A + 'data/model/',
        A + 'data/net/', A + 'domain/Validation.kt', A + 'ui/KaajRoot.kt', A + 'ui/nav/', A + 'ui/auth/', A + 'ui/settings/',
        A + 'ui/theme/', A + 'ui/common/Common.kt', A + 'ui/common/KaajViewModel.kt', A + 'ui/common/Labels.kt',
        A + 'ui/common/TermPicker.kt', RES + 'drawable/', RES + 'mipmap-anydpi-v26/', RES + 'values/colors.xml',
        RES + 'values/themes.xml', RES + 'values/strings.xml', RES + 'values-bn/strings.xml', RES + 'xml/',
        TEST + 'domain/ValidationTest.kt', TEST + 'data/net/NetworkTest.kt']),
    ('2-employer-profile-and-verification', 'Business profile with a map pin, licence/NID upload, verification status', [
        A + 'domain/ProfileForm.kt', A + 'data/DocumentFiles.kt', A + 'ui/common/DocumentPicker.kt', A + 'ui/common/MapPicker.kt',
        A + 'ui/employer/']),
    ('3-post-and-manage-jobs', 'The 4-step job form, My jobs, job detail, pause/resume/close/repost', [
        A + 'domain/JobForm.kt', A + 'domain/Format.kt', A + 'ui/jobs/', TEST + 'domain/JobFormTest.kt', TEST + 'domain/FormatTest.kt']),
    ('4-admin-verification-queue', 'The admin queue: open a document, approve or reject with a reason', [A + 'ui/admin/']),
    ('5-job-seeker-and-applicants', 'Job seeker profile, search near me, saved jobs, apply and track; ranked applicants for employers', [
        A + 'domain/SeekerForm.kt', A + 'ui/common/SeekerLabels.kt', A + 'ui/seeker/', A + 'ui/applicants/',
        RES + 'values/strings_seeker.xml', RES + 'values-bn/strings_seeker.xml', TEST + 'domain/SeekerFormTest.kt']),
]
files = []
for d, dirs, fs in os.walk(ROOT + '/android'):
    dirs[:] = [x for x in dirs if x not in {'build', '.gradle', '.idea'}]
    files += [os.path.relpath(os.path.join(d, f), ROOT).replace(os.sep, '/') for f in fs]
files = sorted(f for f in files if not f.endswith('local.properties'))
seen = {}
os.makedirs(OUT, exist_ok=True)
for old in os.listdir(OUT):
    if old.endswith('.txt'): os.remove(f'{OUT}/{old}')
for name, title, prefixes in GROUPS:
    chosen = [f for f in files if any(f == p or (p.endswith('/') and f.startswith(p)) for p in prefixes)]
    for p in prefixes:
        if not any(f == p or (p.endswith('/') and f.startswith(p)) for f in files): sys.exit('nothing for ' + p)
    for f in chosen:
        if f in seen: sys.exit(f'{f} in both {seen[f]} and {name}')
        seen[f] = name
    with open(f'{OUT}/{name}.txt', 'w', newline='\n') as fh:
        fh.write(f'# Android commit {name[0]}: {title}\n# One path per line, relative to the studentapi folder.\n\n')
        fh.writelines(f + '\n' for f in chosen)
    print(name, len(chosen))
left = [f for f in files if f not in seen]
print('not in any list:', left or 'none', '| total', len(files))
