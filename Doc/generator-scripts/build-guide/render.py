from playwright.sync_api import sync_playwright
FOOT = '''<div style="font-family: Inter, sans-serif; font-size:7.5pt; color:#64748b; width:100%; padding:0 16mm; display:flex; justify-content:space-between;">
<span>Kaaj – The Job · Member-3 build guide (Ahsan Habib, 22201027)</span><span><span class="pageNumber"></span> / <span class="totalPages"></span></span></div>'''
with sync_playwright() as p:
    b = p.chromium.launch(executable_path='/usr/bin/google-chrome', args=['--no-sandbox', '--allow-file-access-from-files'])
    page = b.new_page()
    page.goto('file:///home/claude/guide/guide.html'); page.wait_for_timeout(1500)
    page.pdf(path='/home/claude/guide/Kaaj_Member3_Build_Guide.pdf', format='A4', print_background=True,
             display_header_footer=True, header_template='<div></div>', footer_template=FOOT,
             margin={'top': '16mm', 'bottom': '16mm', 'left': '16mm', 'right': '16mm'})
    b.close()
print('pdf ok')
