# Rebuilds what the group repository will hold after push 1, 2 and 3, to check each one builds on its own.
import os, shutil, filecmp
ROOT = '/home/claude/kaaj'; L = ROOT + '/Doc/Guides/github-files/web'; OUT = '/home/claude/pushtrees'
def read(name): return [l.strip() for l in open(f'{L}/{name}') if l.strip() and not l.startswith('#')]
shutil.rmtree(OUT, ignore_errors=True)
prev = None
for n, name in enumerate(['push-1-setup-and-module-1.txt', 'push-2-module-2.txt', 'push-3-module-3.txt'], 1):
    tree = f'{OUT}/after-push-{n}'
    if prev: shutil.copytree(prev, tree)
    else: os.makedirs(tree)
    for rel in read(name):
        src = f'{L}/versions/push-{n}/{rel}'
        if not os.path.exists(src): src = f'{ROOT}/{rel}'
        if not os.path.exists(src): continue  # mvnw etc. live only on the PC
        os.makedirs(os.path.dirname(f'{tree}/{rel}'), exist_ok=True); shutil.copy(src, f'{tree}/{rel}')
    prev = tree
# After push 3 every file must be today's version.
bad = []
for d, _, fs in os.walk(prev):
    for f in fs:
        rel = os.path.relpath(os.path.join(d, f), prev)
        if not filecmp.cmp(os.path.join(d, f), f'{ROOT}/{rel}', shallow=False): bad.append(rel)
print('after push 3, files that differ from today:', bad or 'none')
