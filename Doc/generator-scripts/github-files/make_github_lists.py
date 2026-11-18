# Writes the CSE471 group-repository file lists (one per push) and the earlier versions of the shared
# files, from the project tree. Run from the project folder: python make_github_lists.py
import os, shutil, sys

ROOT = '/home/claude/kaaj'
OUT = ROOT + '/Doc/Guides/github-files/web'
VERSIONS = '/home/claude/versions'          # push-1 = the files as they were after Module 1, push-2 = after Module 2

J = 'src/main/java/app/kaaj/'
T = 'src/test/java/app/kaaj/'
R = 'src/main/resources/'
W = 'web/src/'

# Files that exist only on the PC (not in this workspace's copy) but belong to the shared setup.
EXTRA_SETUP = ['mvnw', 'mvnw.cmd', '.mvn/wrapper/maven-wrapper.properties']

# Teammates' features in the CSE471 plan (built here for the Android course): never in Member-3's pushes.
TEAMMATES = [J + 'seeker/', J + 'application/', J + 'job/internal/JobSearch.java', J + 'job/internal/SavedJobs.java',
             J + 'job/internal/JobSearchController.java', J + 'job/internal/JobSearchService.java', R + 'db/migration/V150__seeker.sql',
             R + 'db/migration/V160__application.sql', R + 'db/demo/R__demo_seeker.sql',
             T + 'SeekerFlowTests.java', T + 'seeker/', T + 'application/', T + 'job/internal/JobSearchTest.java']
# Not code: kept out of the group repository (the Android course, documents, Postman).
NOT_FOR_GROUP = ['android/', 'Doc/', 'docs/', 'postman/', 'README.md']

MODULE2 = {
    'new': [J + 'identity/UserAdmin.java', J + 'identity/internal/SuspendedUserFilter.java',
            J + 'identity/internal/UserAdminService.java', J + 'identity/internal/UserStatusCache.java',
            J + 'job/Finding.java', J + 'job/JobContent.java', J + 'job/JobRuleChecks.java',
            J + 'shared/AiText.java', J + 'shared/ScamPatterns.java',
            J + 'shared/internal/AiProperties.java', J + 'shared/internal/OpenAiCompatibleText.java',
            J + 'trust/', R + 'db/migration/V240__trust.sql', R + 'db/demo/R__demo_trust.sql',
            W + 'components/FindingList.tsx', W + 'components/ReportButton.tsx',
            W + 'features/admin-trust/ModerationPage.tsx', W + 'lib/trust.ts', W + 'lib/trust.test.ts',
            T + 'TrustFlowTests.java', T + 'job/JobRuleChecksTest.java', T + 'shared/ScamPatternsTest.java',
            T + 'trust/'],
    'changed': ['.env.example', J + 'identity/internal/AppUser.java', J + 'identity/internal/SecurityConfig.java',
                J + 'job/JobModeration.java', J + 'job/JobStatus.java', J + 'job/internal/Job.java',
                J + 'job/internal/JobEventListeners.java', J + 'job/internal/JobProperties.java',
                J + 'job/internal/JobService.java', J + 'job/package-info.java', J + 'shared/KaajException.java',
                R + 'application.properties', R + 'messages.properties', R + 'messages_bn.properties',
                T + 'job/JobStatusTest.java',
                W + 'components/AppLayout.tsx', W + 'features/admin-trust/api.ts',
                W + 'features/employer-jobs/JobDetailPage.tsx', W + 'features/employer-jobs/api.ts',
                W + 'features/jobs/PublicJobPage.tsx', W + 'features/jobs/api.ts', W + 'i18n/bn.json', W + 'i18n/en.json', W + 'lib/types.ts',
                W + 'router.tsx'],
}
MODULE3 = {
    'new': [J + 'job/internal/DraftTemplate.java', J + 'job/internal/JobDraftController.java',
            J + 'job/internal/JobDrafts.java', J + 'job/internal/OccupationFinder.java',
            J + 'job/internal/SentenceFacts.java', J + 'shared/TermNames.java',
            W + 'features/employer-jobs/AssistantPanel.tsx', W + 'features/employer-jobs/assistant.test.tsx',
            T + 'job/internal/JobDraftsTest.java', T + 'JobDraftFlowTests.java'],
    'changed': [J + 'shared/Taxonomy.java', J + 'shared/internal/JdbcTaxonomy.java',
                J + 'job/internal/JobProperties.java', R + 'application.properties', R + 'messages.properties',
                R + 'messages_bn.properties', W + 'features/employer-jobs/JobFormPage.tsx',
                W + 'features/employer-jobs/schema.ts', W + 'features/employer-jobs/api.ts', W + 'lib/types.ts',
                W + 'i18n/bn.json', W + 'i18n/en.json'],
}
SKIP_DIRS = {'node_modules', 'dist', '.git', 'target', 'build', '.gradle'}

def tree():
    files = []
    for d, dirs, fs in os.walk(ROOT):
        dirs[:] = [x for x in dirs if x not in SKIP_DIRS]
        for f in fs:
            files.append(os.path.relpath(os.path.join(d, f), ROOT).replace(os.sep, '/'))
    return sorted(set(files + EXTRA_SETUP))

def under(path, prefixes):
    return any(path == p or (p.endswith('/') and path.startswith(p)) for p in prefixes)

def expand(prefixes, files):
    out = [f for f in files if under(f, prefixes)]
    missing = [p for p in prefixes if not p.endswith('/') and p not in files]
    if missing: sys.exit('missing: %s' % missing)
    return out

files = tree()
m2_new, m2_changed = expand(MODULE2['new'], files), expand(MODULE2['changed'], files)
m3_new, m3_changed = expand(MODULE3['new'], files), expand(MODULE3['changed'], files)
excluded = set(expand(TEAMMATES, files)) | set(expand(NOT_FOR_GROUP, files))
push1 = [f for f in files if f not in excluded and f not in m2_new and f not in m3_new]

# Sanity: every file that later pushes change must already be in push 1 or be new in push 2.
for f in m2_changed + m3_changed:
    assert f in push1 or f in m2_new, f
# Files changed after Module 1 need their Module-1 version in push 1.
for f in set(m2_changed + m3_changed) & set(push1):
    assert os.path.exists(f'{VERSIONS}/push-1/{f}'), 'no Module-1 version of ' + f
# Files Module 3 changes again need their Module-2 version in push 2.
for f in set(m2_changed) & set(m3_changed):
    assert os.path.exists(f'{VERSIONS}/push-2/{f}'), 'no Module-2 version of ' + f

def write(name, title, groups):
    with open(f'{OUT}/{name}', 'w', newline='\n') as fh:
        fh.write(f'# {title}\n# One path per line, relative to the studentapi folder. Lines starting with # are ignored.\n')
        for heading, items in groups:
            fh.write(f'\n# ---- {heading} ({len(items)})\n')
            fh.writelines(i + '\n' for i in items)
    return sum(len(i) for _, i in groups)

# Rewrite the lists and the versions folder; keep Copy-Push.ps1.
shutil.rmtree(OUT + '/versions', ignore_errors=True)
for old in os.listdir(OUT) if os.path.exists(OUT) else []:
    if old.endswith('.txt'): os.remove(f'{OUT}/{old}')
os.makedirs(OUT, exist_ok=True)
m2_only_changed = [f for f in m2_changed if f not in m2_new]
m3_only_changed = m3_changed
counts = {
    'push-1': write('push-1-setup-and-module-1.txt', 'Push 1: shared setup + Module 1 (employer profile and verification; post and manage jobs)',
                    [('backend', [f for f in push1 if not f.startswith('web/')]), ('web app', [f for f in push1 if f.startswith('web/')])]),
    'push-2': write('push-2-module-2.txt', 'Push 2: Module 2 (scam screening and moderation)',
                    [('new files', m2_new), ('files Module 2 changes', m2_only_changed)]),
    'push-3': write('push-3-module-3.txt', 'Push 3: Module 3 (AI job-post assistant)',
                    [('new files', m3_new), ('files Module 3 changes', m3_only_changed)]),
}
for push in ('push-1', 'push-2'):
    src = f'{VERSIONS}/{push}'
    keep = set(push1) if push == 'push-1' else set(m2_changed)
    for d, _, fs in os.walk(src):
        for f in fs:
            rel = os.path.relpath(os.path.join(d, f), src).replace(os.sep, '/')
            if rel in keep:
                dst = f'{OUT}/versions/{push}/{rel}'
                os.makedirs(os.path.dirname(dst), exist_ok=True); shutil.copy(os.path.join(d, f), dst)
print(counts)
print('push-1 versions:', sum(len(f) for _, _, f in os.walk(f'{OUT}/versions/push-1')),
      'push-2 versions:', sum(len(f) for _, _, f in os.walk(f'{OUT}/versions/push-2')))
print('excluded (teammates + docs):', len(excluded))
