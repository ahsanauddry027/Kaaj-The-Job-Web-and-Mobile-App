# Filters javac output (compiled without the Spring/JPA jars) down to errors that point at real bugs
# in our own code: wrong method names or argument types on our classes, typos in our own fields, etc.
import re, os, sys
log = open('/tmp/javac.log').read()
own = set(); static_imports = {}
for root, _, files in os.walk('src'):
    for f in files:
        if f.endswith('.java'):
            p = os.path.join(root, f); s = open(p).read()
            own.update(re.findall(r'\b(?:class|record|enum|interface)\s+([A-Z]\w*)', s))
            static_imports[p] = set(re.findall(r'import static [\w.]+\.(\w+);', s))
blocks = re.split(r'\n(?=src/)', log)
sus = []
for b in blocks:
    if 'error:' not in b: continue
    first = b.split('\n')[0]; path = first.split(':')[0]
    if 'does not exist' in first or 'static import only' in first: continue
    if 'does not override' in first:
        continue  # @Override on methods of library interfaces (UserDetailsService, Persistable, ...)
    m = re.search(r'symbol:\s+(\w+)\s+(\S+)', b); loc = re.search(r'location:\s+(.*)', b)
    if 'cannot find symbol' in first and m:
        kind, name = m.group(1), m.group(2)
        base = name.split('(')[0].split('<')[0]
        l = loc.group(1).strip() if loc else ''
        if kind in ('class', 'interface') and base not in own: continue
        if kind == 'variable' and base[:1].isupper() and base not in own: continue
        if kind == 'method' and base in static_imports.get(path, set()): continue
        lt = re.search(r'(?:of type|class|interface|record|enum)\s+([\w.<>?, ]+)', l)
        t = lt.group(1).split('<')[0].split('.')[-1].strip() if lt else ''
        if kind in ('method', 'variable') and t and t not in own: continue
    sus.append(b.strip()[:700])
print(len(sus), 'possible problems')
for s in sus: print(s); print('-----')
