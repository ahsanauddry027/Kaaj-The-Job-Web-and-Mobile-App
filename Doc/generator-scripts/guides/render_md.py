"""Render the Markdown guides in Doc/Guides to PDF with headless Chrome (same look as the build guide)."""
import pathlib
import sys

import html as htmllib
import re

from markdown_it import MarkdownIt
from playwright.sync_api import sync_playwright

GUIDES = pathlib.Path('/home/claude/kaaj/Doc/Guides')
FONTS = 'file:///home/claude/kaaj/web/node_modules/@fontsource/noto-sans-bengali'

CSS = f"""
@import url("{FONTS}/400.css"); @import url("{FONTS}/600.css"); @import url("{FONTS}/700.css");
* {{ box-sizing: border-box; }}
body {{ font-family: Inter, "Noto Sans Bengali", sans-serif; font-size: 9.6pt; line-height: 1.5; color: #0f172a; margin: 0; }}
h1 {{ font-size: 21pt; color: #1d4ed8; margin: 0 0 6px; padding-bottom: 8px; border-bottom: 3px solid #dbeafe; }}
h2 {{ font-size: 14pt; color: #1e3a8a; margin: 22px 0 8px; padding-bottom: 4px; border-bottom: 1px solid #e2e8f0; break-after: avoid; }}
h3 {{ font-size: 11.2pt; margin: 16px 0 6px; color: #0f172a; break-after: avoid; }}
p {{ margin: 6px 0 8px; }}
ul, ol {{ margin: 4px 0 8px; padding-left: 22px; }}
li {{ margin: 2px 0; }}
li > p {{ margin: 2px 0; }}
li pre {{ margin: 4px 0 6px; }}
a {{ color: #1d4ed8; text-decoration: none; overflow-wrap: anywhere; }}
code {{ font-family: "DejaVu Sans Mono", monospace; font-size: 8.2pt; background: #f1f5f9; padding: 0 3px; border-radius: 3px; overflow-wrap: anywhere; }}
pre {{ background: #0f172a; color: #e2e8f0; padding: 8px 10px; border-radius: 6px; font-size: 7.8pt; line-height: 1.4;
       white-space: pre-wrap; overflow-wrap: anywhere; margin: 6px 0 10px; break-inside: avoid; }}
pre code {{ background: none; color: inherit; padding: 0; font-size: inherit; }}
blockquote {{ margin: 10px 0; padding: 8px 12px; border-left: 4px solid #1d4ed8; background: #eff6ff; border-radius: 0 8px 8px 0; }}
blockquote p {{ margin: 3px 0; }}
table {{ width: 100%; border-collapse: collapse; margin: 8px 0 12px; font-size: 8.4pt; }}
th code {{ background: rgba(255,255,255,.18); color: #fff; }}
th {{ background: #1e3a8a; color: white; text-align: left; padding: 5px 6px; font-weight: 600; }}
td {{ border-bottom: 1px solid #e2e8f0; padding: 4px 6px; vertical-align: top; }}
tr:nth-child(even) td {{ background: #f8fafc; }}
tr {{ break-inside: avoid; }}
img {{ max-width: 100%; display: block; margin: 8px auto 14px; border: 1px solid #cbd5e1; border-radius: 8px; }}
hr {{ border: none; border-top: 1px solid #cbd5e1; margin: 18px 0; }}
.meta {{ color: #475569; }}
"""


def slug(heading_html: str) -> str:
    """GitHub-style heading anchor: lower case, punctuation removed, spaces to hyphens."""
    text = htmllib.unescape(re.sub(r'<[^>]+>', '', heading_html)).lower()
    text = re.sub(r'[^\w\- ]', '', text)
    return text.replace(' ', '-')


def render(md_path: pathlib.Path, footer_title: str) -> pathlib.Path:
    text = md_path.read_text(encoding='utf-8')
    html_body = MarkdownIt('commonmark').enable(['table', 'strikethrough']).render(text)
    html_body = re.sub(r'<(h[1-4])>(.*?)</\1>', lambda m: f'<{m.group(1)} id="{slug(m.group(2))}">{m.group(2)}</{m.group(1)}>', html_body)
    html = (f'<!doctype html><html lang="en"><head><meta charset="utf-8"><base href="{GUIDES.as_uri()}/">'
            f'<title>{footer_title}</title><style>{CSS}</style></head><body>{html_body}</body></html>')
    html_path = pathlib.Path('/home/claude/guide') / (md_path.stem + '.html')
    html_path.write_text(html, encoding='utf-8')
    pdf_path = md_path.with_suffix('.pdf')
    foot = ('<div style="font-family: Inter, sans-serif; font-size:7.5pt; color:#64748b; width:100%; padding:0 15mm; '
            'display:flex; justify-content:space-between;">'
            f'<span>Kaaj – The Job · {footer_title}</span>'
            '<span><span class="pageNumber"></span> / <span class="totalPages"></span></span></div>')
    with sync_playwright() as p:
        browser = p.chromium.launch(executable_path='/usr/bin/google-chrome',
                                    args=['--no-sandbox', '--allow-file-access-from-files'])
        page = browser.new_page()
        page.goto(html_path.as_uri())
        page.wait_for_timeout(1200)
        page.pdf(path=str(pdf_path), format='A4', print_background=True, display_header_footer=True,
                 header_template='<div></div>', footer_template=foot,
                 margin={'top': '15mm', 'bottom': '16mm', 'left': '15mm', 'right': '15mm'})
        browser.close()
    return pdf_path


if __name__ == '__main__':
    titles = {
        '01_One_Backend_Two_Apps': 'One backend for the web app and the Android app',
        '02_GitHub_Milestone_Pushes': 'Pushing each milestone to GitHub',
        '03_Business_Model': 'Business model (web app + Android app)',
        '04_Run_Backend_Web_Android': 'How to run the backend, the web app and the Android app',
        '05_GitHub_Files_Per_Module': 'Which files to push, module by module (web) and milestone by milestone (Android)',
        '06_MVC_Architecture': 'How the web app follows MVC (Model-View-Controller)',
        '07_Push_Both_Apps_Separately': 'Pushing the two apps separately: web per module, Android per milestone',
    }
    names = sys.argv[1:] or list(titles)
    for name in names:
        print(render(GUIDES / f'{name}.md', titles[name]))
