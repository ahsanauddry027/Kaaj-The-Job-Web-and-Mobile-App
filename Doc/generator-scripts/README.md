# Generator and check scripts

These are the helper scripts Claude (the AI) wrote and ran in its own Linux workspace while building Kaaj. They are kept here as part of
the AI declaration, so anyone can see how the guides, the Android string files, the screenshots and the checks were made.
They are **not** part of the app and are not needed to build or run it.

Paths inside them point at that workspace (for example `/home/claude/kaaj` for the project and `/home/claude/shots` for screenshots).
To run one on your PC, change those paths first.

| Folder | Script | What it does | Needs |
|---|---|---|---|
| `build-guide/` | `build_guide.py` | Writes the HTML of `docs/Kaaj_Member3_Build_Guide.pdf` (the request-1 guide), with the screenshots embedded | Python 3 |
| `build-guide/` | `render.py` | Prints that HTML to the PDF with headless Chrome (Chrome shapes Bangla text correctly) | Python 3, Playwright, Google Chrome |
| `guides/` | `render_md.py` | Turns `Doc/Guides/*.md` (01–07) into the matching PDFs (GitHub-style Markdown, same anchors) | Python 3, `markdown-it-py`, Playwright, Google Chrome |
| `android-strings/` | `strings_m2.py` | Writes `android/app/src/main/res/values/strings.xml` and `values-bn/strings.xml` from one table, so English and Bangla always have the same keys (298 keys) | Python 3 |
| `android-strings/` | `strings_seeker.py` | Same for `strings_seeker.xml` (job seeker side and applicants, 118 keys) | Python 3, `strings_m2.py` next to it |
| `web-screenshots/` | `mock-api-server.mjs` | A small Node.js mock of the Kaaj API with the same JSON shapes, used because Spring Boot couldn't be downloaded in the AI's workspace | Node.js 24 |
| `web-screenshots/` | `shoot.py` | Drives the web app (against the mock API) through every page and saves `Doc/screenshots/web/*.png`; also reports page errors | Python 3, Playwright, Google Chrome, the web app on port 5173 |
| `web-screenshots/` | `sample-licence.png` | The sample document image the mock API serves | — |
| `web-screenshots/` | `shoot_member3_modules.py` | Screenshots 20–33: the job-post assistant, the screening notice, the report form and the moderation queue (restart the mock API before each run: it keeps the decisions in memory) | Python 3, Playwright, Google Chrome, the web app on port 5173 |
| `github-files/` | `make_github_lists.py` | Writes `Doc/Guides/github-files/web/push-*.txt` (which files go to the CSE471 group repository in each push) and the earlier versions of shared files in `versions/` | Python 3, the earlier versions in `/home/claude/versions` |
| `github-files/` | `build_push_trees.py` | Rebuilds the group repository after push 1, 2 and 3 in a scratch folder (as `Copy-Push.ps1` would), so each can be type-checked, tested, built and migrated on its own | Python 3 |
| `github-files/` | `make_android_lists.py` | Writes `Doc/Guides/github-files/android/*.txt`: one list per feature commit, covering every file in `android/` exactly once | Python 3 |
| `checks/` | `reset_db.sh` | Recreates a scratch database and runs every Flyway migration in version order with `psql` (how the SQL was tested) | PostgreSQL, Linux shell |
| `checks/` | `javac_filter.py` | Filters `javac` output (compiled without the Spring jars) down to errors in the project's own code | Python 3, a `javac` log |
| `checks/` | `kcheck.py` | Static check on the Kotlin sources: lists class names and common Compose/coroutine functions used in a file without an import or a declaration in the same package (the Android app could not be compiled in the AI's workspace). Run: `python kcheck.py android/app/src` | Python 3 |
| `checks/` | `taxonomy_search_test.sql` | The query used to try the Bangla/Banglish/English occupation search in `psql` (`psql -v q=driver -f taxonomy_search_test.sql`) | PostgreSQL with the migrations applied |
| `checks/` | `web_page_errors.py` | Opens the web app in headless Chrome and prints any page errors (a quick debugging aid) | Python 3, Playwright, Google Chrome |

The schema diagram's script is in `Doc/diagrams/draw_schema.py`; the Graphviz source it wrote is `Doc/diagrams/Kaaj_Schema_As_Built.dot`.
