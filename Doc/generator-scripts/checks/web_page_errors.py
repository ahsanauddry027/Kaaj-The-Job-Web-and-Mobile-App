from playwright.sync_api import sync_playwright
with sync_playwright() as p:
    b = p.chromium.launch(executable_path='/usr/bin/google-chrome', args=['--no-sandbox'])
    pg = b.new_page(viewport={'width': 1440, 'height': 900})
    errs = []
    pg.on('pageerror', lambda e: errs.append(str(e)))
    pg.on('console', lambda m: errs.append(m.type + ': ' + m.text))
    pg.goto('http://localhost:5173/login')
    pg.get_by_label('ইমেইল').fill('karim@demo.kaaj'); pg.get_by_label('পাসওয়ার্ড').fill('x')
    pg.get_by_role('button', name='সাইন ইন').click(); pg.wait_for_url('**/employer')
    pg.goto('http://localhost:5173/employer/jobs/new'); pg.wait_for_timeout(2500)
    pg.screenshot(path='/home/claude/shots/dbg.png')
    print('\n'.join(errs[:10]))
    b.close()
