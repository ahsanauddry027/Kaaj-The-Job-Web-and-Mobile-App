# UI smoke test against the mock API, taking screenshots for the guide.
import io, sys
from PIL import Image
from playwright.sync_api import sync_playwright, expect

BASE = 'http://localhost:5173'
OUT = '/home/claude/shots/'
buf = io.BytesIO(); Image.new('RGB', (256, 256), '#e8eef3').save(buf, 'PNG'); TILE = buf.getvalue()
problems = []

def shot(page, name, full=False):
    page.wait_for_timeout(600)
    page.screenshot(path=OUT + name + '.png', full_page=full)

with sync_playwright() as p:
    browser = p.chromium.launch(executable_path='/usr/bin/google-chrome', args=['--no-sandbox'])
    ctx = browser.new_context(viewport={'width': 1440, 'height': 900}, device_scale_factor=1)
    ctx.route('**/tile.openstreetmap.org/**', lambda r: r.fulfill(status=200, content_type='image/png', body=TILE))
    page = ctx.new_page()
    page.on('pageerror', lambda e: problems.append('pageerror: ' + str(e)))
    page.on('console', lambda m: m.type == 'error' and problems.append('console: ' + m.text))

    def login(email):
        page.goto(BASE + '/login')
        page.get_by_label('ইমেইল').fill(email)
        page.get_by_label('পাসওয়ার্ড').fill('Kaaj@12345')
        page.get_by_role('button', name='সাইন ইন').click()
        page.wait_for_load_state('networkidle')

    def logout():
        page.get_by_role('button', name='লগআউট').first.click()
        page.wait_for_url('**/login')

    page.goto(BASE + '/login'); shot(page, '01-login')
    login('karim@demo.kaaj'); page.wait_for_url('**/employer'); shot(page, '02-dashboard')
    page.goto(BASE + '/employer/profile'); page.wait_for_load_state('networkidle'); shot(page, '03-profile-verified', full=True)
    page.goto(BASE + '/employer/jobs'); page.wait_for_load_state('networkidle'); shot(page, '04-my-jobs')
    page.get_by_role('button', name='করণীয় কাজ').first.click(); shot(page, '05-my-jobs-menu')
    page.keyboard.press('Escape'); page.mouse.click(5, 5)

    # The four-step wizard
    page.goto(BASE + '/employer/jobs/new'); page.wait_for_load_state('networkidle')
    page.get_by_role('button', name='পরবর্তী ধাপ').click()
    shot(page, '06-wizard-step1-errors')
    page.get_by_label('চাকরির শিরোনাম (বাংলা)').fill('ড্রাইভার')
    page.get_by_role('combobox', name='ক্যাটাগরি').fill('driver'); page.wait_for_timeout(700)
    page.get_by_role('listbox').get_by_role('option').first.click()
    page.get_by_label('পদ সংখ্যা').fill('২')
    page.get_by_label('বিবরণ (বাংলা)').fill('কারখানার মালামাল পৌঁছে দিতে পিকআপ চালাতে হবে। শুক্রবার ছুটি।')
    shot(page, '07-wizard-step1')
    page.get_by_role('button', name='পরবর্তী ধাপ').click()
    page.get_by_label('সর্বনিম্ন বেতন (৳)').fill('16000')
    page.get_by_label('সর্বোচ্চ বেতন (৳)').fill('20000')
    page.get_by_label('কাজের সময়: শুরু').fill('08:00')
    page.get_by_label('শেষ').fill('18:00')
    shot(page, '08-wizard-step2')
    page.get_by_role('button', name='পরবর্তী ধাপ').click()
    page.locator('.leaflet-container').click(position={'x': 300, 'y': 120})
    page.get_by_label('এলাকার নাম').fill('টঙ্গী, গাজীপুর')
    shot(page, '09-wizard-step3')
    page.get_by_role('button', name='পরবর্তী ধাপ').click()
    page.get_by_role('combobox', name='প্রয়োজনীয় দক্ষতা').fill('driv'); page.wait_for_timeout(700)
    page.get_by_role('listbox').get_by_role('option').first.click(); page.mouse.click(5, 5)
    page.get_by_label('পেশাদার লাইসেন্স (হালকা)').check()
    page.get_by_label('ন্যূনতম অভিজ্ঞতা (বছর)').fill('1')
    shot(page, '10-wizard-step4')
    page.get_by_role('button', name='প্রকাশ করুন').click()
    page.wait_for_url('**/employer/jobs/*'); page.wait_for_load_state('networkidle')
    shot(page, '11-job-detail-published', full=True)
    job_url = page.url
    logout()

    # An employer waiting for verification
    login('tongigarments@demo.kaaj')
    page.goto(BASE + '/employer/profile'); page.wait_for_load_state('networkidle'); shot(page, '12-profile-pending', full=True)
    page.goto(BASE + '/employer/jobs/new'); page.wait_for_load_state('networkidle')
    page.get_by_label('চাকরির শিরোনাম (বাংলা)').fill('সেলাই মেশিন অপারেটর')
    page.get_by_role('button', name='খসড়া হিসেবে সংরক্ষণ').click()
    page.wait_for_url('**/employer/jobs/*'); page.wait_for_load_state('networkidle')
    shot(page, '13-draft-unverified')
    logout()

    # Admin
    login('admin@kaaj.local'); page.wait_for_url('**/admin/verification'); page.wait_for_load_state('networkidle')
    page.get_by_role('button', name='Tongi Garments Ltd.').click(); page.wait_for_timeout(800)
    shot(page, '14-admin-queue', full=True)
    page.get_by_role('button', name='বাতিল করুন').click(); shot(page, '15-admin-reason-required')
    page.get_by_role('button', name='অনুমোদন দিন').click(); page.wait_for_timeout(800)
    page.get_by_role('tab', name='অনুমোদিত (২)').click(); page.wait_for_timeout(500)
    shot(page, '16-admin-approved')
    logout()

    # Public page and English
    page.goto(job_url.replace('/employer', '')); page.wait_for_load_state('networkidle'); shot(page, '17-public-job')
    login('karim@demo.kaaj')
    page.get_by_role('combobox', name='ভাষা').first.select_option('en')
    page.goto(BASE + '/employer/profile'); page.wait_for_load_state('networkidle'); shot(page, '18-profile-english')

    # Phone size
    mobile = browser.new_context(viewport={'width': 390, 'height': 844}, device_scale_factor=2, storage_state=ctx.storage_state())
    mobile.route('**/tile.openstreetmap.org/**', lambda r: r.fulfill(status=200, content_type='image/png', body=TILE))
    m = mobile.new_page(); m.goto(BASE + '/employer/jobs'); m.wait_for_load_state('networkidle'); m.wait_for_timeout(600)
    m.screenshot(path=OUT + '19-mobile-my-jobs.png')
    browser.close()

print('\n'.join(problems) if problems else 'no browser errors')
