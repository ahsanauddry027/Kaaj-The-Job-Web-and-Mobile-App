"""Draws the as-built database schema (migrations V1–V240) from a live PostgreSQL database with Graphviz.
Run: python draw_schema.py [database]; then dot -Tpdf/-Tpng/-Tsvg kaaj_schema.dot."""
import sys
DB = sys.argv[1] if len(sys.argv) > 1 else 'kaaj_test'
import subprocess, html, collections
def q(sql):
    out = subprocess.run(['sudo','-u','postgres','psql','-d',DB,'-At','-F','\t','-c',sql], capture_output=True, text=True, check=True).stdout
    return [l.split('\t') for l in out.strip().split('\n') if l]
cols = q("""SELECT c.table_name, c.column_name, CASE WHEN c.data_type='ARRAY' THEN replace(c.udt_name,'_','')||'[]' WHEN c.data_type LIKE 'timestamp%' THEN 'timestamptz' WHEN c.data_type='character varying' THEN 'varchar' WHEN c.data_type='double precision' THEN 'double' ELSE c.data_type END, c.is_nullable
FROM information_schema.columns c JOIN information_schema.tables t USING (table_schema, table_name)
WHERE c.table_schema='public' AND t.table_type='BASE TABLE' ORDER BY c.table_name, c.ordinal_position""")
pks = {(t,c) for t,c in q("""SELECT tc.table_name, kcu.column_name FROM information_schema.table_constraints tc JOIN information_schema.key_column_usage kcu USING (constraint_schema, constraint_name) WHERE tc.constraint_type='PRIMARY KEY' AND tc.table_schema='public'""")}
uqs = {(t,c) for t,c in q("""SELECT tc.table_name, kcu.column_name FROM information_schema.table_constraints tc JOIN information_schema.key_column_usage kcu USING (constraint_schema, constraint_name) WHERE tc.constraint_type='UNIQUE' AND tc.table_schema='public'""")}
fks = q("""SELECT tc.table_name, kcu.column_name, ccu.table_name, ccu.column_name FROM information_schema.table_constraints tc
JOIN information_schema.key_column_usage kcu USING (constraint_schema, constraint_name)
JOIN information_schema.constraint_column_usage ccu USING (constraint_schema, constraint_name)
WHERE tc.constraint_type='FOREIGN KEY' AND tc.table_schema='public'""")
fkcols = {(a,b) for a,b,_,_ in fks}
tables = collections.OrderedDict()
for t,c,ty,nul in cols: tables.setdefault(t, []).append((c,ty,nul))
groups = {
 'Shared setup (identity, taxonomy, audit)': ['app_user','email_code','audit_log','occupation','occupation_synonym','skill','skill_synonym'],
 'Feature 3 · Employer profile and verification': ['employer'],
 'Feature 4 · Post and manage jobs': ['job'],
 'Job seeker side (Android build)': ['seeker_profile','saved_job','application','application_event'],
 'Module 2 · Scam screening and moderation': ['trust_case','report'],
}
colors = ['#EFF6FF','#ECFDF5','#FFFBEB','#FDF2F8','#FEF2F2']
heads = ['#1D4ED8','#047857','#B45309','#BE185D','#B91C1C']
def node(t, head):
    rows = [f'<TR><TD BGCOLOR="{head}" COLSPAN="3" ALIGN="CENTER"><FONT COLOR="white" POINT-SIZE="13"><B>{t}</B></FONT></TD></TR>']
    for c,ty,nul in tables[t]:
        key = 'PK' if (t,c) in pks else ('FK' if (t,c) in fkcols else ('UQ' if (t,c) in uqs else ''))
        name = f'<B>{html.escape(c)}</B>' if key=='PK' else html.escape(c)
        rows.append(f'<TR><TD PORT="{c}" ALIGN="LEFT">{name}</TD><TD ALIGN="LEFT"><FONT COLOR="#475569">{html.escape(ty)}{"" if nul=="YES" else " not null"}</FONT></TD><TD ALIGN="CENTER">{f'<FONT COLOR="#B91C1C">{key}</FONT>' if key else ' '}</TD></TR>')
    return f'"{t}" [label=<<TABLE BORDER="1" CELLBORDER="0" CELLSPACING="0" CELLPADDING="3" BGCOLOR="white" COLOR="#94A3B8">{"".join(rows)}</TABLE>>];'
out = ['digraph kaaj {','graph [rankdir=LR, splines=spline, nodesep=0.45, ranksep=1.6, forcelabels=true, fontname="Helvetica", labelloc=t, fontsize=26, label=<<B>Kaaj – The Job · database schema as built</B><BR/><FONT POINT-SIZE="14">PostgreSQL · Flyway migrations V1–V3, V130, V140, V150, V160, V240 · Spring Boot backend shared by the web and Android apps<BR/>Crow-foot end = the many side; each line is labelled with its foreign-key column</FONT>>];','node [shape=plaintext, fontname="Helvetica", fontsize=11];','edge [color="#64748B", arrowhead=tee, arrowtail=crow, dir=both, penwidth=1.3];']
for i,(g,ts) in enumerate(groups.items()):
    out.append(f'subgraph cluster_{i} {{ label=<<B>{html.escape(g)}</B>>; fontsize=15; style="rounded,filled"; fillcolor="{colors[i]}"; color="{heads[i]}";')
    for t in ts:
        if t in tables: out.append(node(t, heads[i]))
    out.append('}')
missing = [t for t in tables if not any(t in ts for ts in groups.values())]
for t in missing: out.append(node(t, '#334155'))
for a,ac,b,bc in fks:
    out.append(f'"{a}" -> "{b}" [xlabel=<<FONT POINT-SIZE="10" COLOR="#334155">{ac}</FONT>>];')
out.append('}')
open('kaaj_schema.dot','w').write('\n'.join(out))
print(len(tables), 'tables', sum(len(v) for v in tables.values()), 'columns', len(fks), 'foreign keys; ungrouped:', missing)
