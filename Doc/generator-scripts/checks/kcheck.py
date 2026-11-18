import glob,re,sys,collections
root=sys.argv[1]
files=sorted(glob.glob(root+'/**/*.kt', recursive=True))
pkg_decls=collections.defaultdict(set)
def strip(t):
    t=re.sub(r'/\*.*?\*/', '', t, flags=re.S)
    t=re.sub(r'"""(.*?)"""', '""', t, flags=re.S)
    t=re.sub(r'//[^\n]*', '', t)
    t=re.sub(r'"(?:\\.|\$\{[^}]*\}|[^"\\])*"', '""', t)
    return t
data={}
for f in files:
    raw=open(f).read(); t=strip(raw)
    pkg=re.search(r'^package\s+([\w.]+)', raw, re.M).group(1)
    data[f]=(pkg,raw,t)
    for m in re.finditer(r'\b(?:class|object|interface|fun|val|var|typealias)\s+(?:<[^>]*>\s*)?(?:[\w.]+\.)?(\w+)', t):
        pkg_decls[pkg].add(m.group(1))
builtin=set('''String Int Long Boolean Double Float Char Byte Short Unit Any Nothing List MutableList Map MutableMap Set MutableSet Pair Triple Exception
Throwable Regex Array ByteArray IntArray Comparable Iterable Sequence Lazy StringBuilder Number IllegalStateException IllegalArgumentException
Suppress OptIn Synchronized JvmStatic Deprecated Volatile Transient ReplaceWith Override Serializable StringRes Composable ReadOnlyComposable SuppressLint Throws
T R K V E VM'''.split())
for f,(pkg,raw,t) in data.items():
    imports=set()
    for m in re.finditer(r'^import\s+([\w.]+)(?:\s+as\s+(\w+))?', raw, re.M):
        imports.add(m.group(2) or m.group(1).split('.')[-1])
    body=re.sub(r'^(package|import)[^\n]*', '', t, flags=re.M)
    local=set(re.findall(r'\b(?:class|object|interface|fun|val|var)\s+(\w+)', body))
    local|=set(re.findall(r'\b(\w+)\s*:', body))  # params
    used=set(re.findall(r'(?<![\w.])([A-Z]\w*)\b', body))
    missing=sorted(u for u in used if u not in imports and u not in pkg_decls[pkg] and u not in builtin and u not in local and not u.isupper())
    # function calls commonly needing import
    calls=set(re.findall(r'(?<![\w.])(stringResource|painterResource|remember|rememberSaveable|mutableStateOf|mutableIntStateOf|LaunchedEffect|collectAsStateWithLifecycle|viewModel|launch|async|delay|withContext|coroutineScope|rememberCoroutineScope|rememberScrollState|rememberUpdatedState|items|item|toRoute|key|getValue|setValue|toMediaType|toRequestBody|toHttpUrlOrNull|asImageBitmap|consumeWindowInsets|imePadding|safeDrawingPadding|verticalScroll|clickable|background|clip|size|padding|fillMaxWidth|fillMaxSize|height|heightIn|weight|align|edit|asConverterFactory|findStartDestination|hasRoute|hierarchy|navigateTab|label|text|colors|banner|isBangla|errorText|unit)\s*[({<]', body))
    used_names=calls - imports - pkg_decls[pkg] - local
    # methods like .padding are extension usage with '.', excluded by lookbehind
    if missing or used_names: print(f, 'TYPES:', missing, 'CALLS:', sorted(used_names))
print('done')
