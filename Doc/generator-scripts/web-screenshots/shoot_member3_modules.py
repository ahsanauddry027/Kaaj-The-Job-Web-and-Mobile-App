# Screenshots of the Member-3 Module 2 and Module 3 web screens, against the mock API.
import sys
from playwright.sync_api import sync_playwright, expect

BASE = 'http://localhost:5173'
OUT = '/home/claude/shots/'
problems = []

def shot(page, name, full=False):
    page.wait_for_timeout(600)
    page.screenshot(path=OUT + name + '.png', full_page=full)

with sync_playwright() as p:
    browser = p.chromium.launch(executable_path='/usr/bin/google-chrome', args=['--no-sandbox'])
    ctx = browser.new_context(viewport={'width': 1440, 'height': 900}, device_scale_factor=1)
    ctx.route('**/tile.openstreetmap.org/**', lambda r: r.fulfill(status=200, content_type='image/png', body=b''))
    page = ctx.new_page()
    page.on('pageerror', lambda e: problems.append('pageerror: ' + str(e)))
    page.on('console', lambda m: m.type == 'error' and 'tile' not in m.text and problems.append('console: ' + m.text))

    def login(email):
        page.goto(BASE + '/login')
        page.get_by_label('ইমেইল').fill(email)
        page.get_by_label('পাসওয়ার্ড').fill('Kaaj@12345')
        page.get_by_role('button', name='সাইন ইন').click()
        page.wait_for_load_state('networkidle')

    def logout():
        page.get_by_role('button', name='লগআউট').first.click()
        page.wait_for_url('**/login')

    # ---- Module 3: the job-post assistant
    login('karim@demo.kaaj'); page.wait_for_url('**/employer')
    page.goto(BASE + '/employer/jobs/new'); page.wait_for_load_state('networkidle')
    shot(page, '20-assistant-empty')
    page.locator('#assistant-text').fill('2 jon pickup driver lagbe, Tongi, beton 16-20 hajar, sokal 8ta theke sondha 6ta')
    page.get_by_role('button', name='খসড়া বানান').click()
    expect(page.get_by_role('button', name='এই খসড়াটি নিন')).to_be_visible(timeout=5000)
    shot(page, '21-assistant-draft', full=True)
    page.get_by_role('button', name='এই খসড়াটি নিন').click()
    page.wait_for_timeout(500)
    expect(page.get_by_label('চাকরির শিরোনাম (বাংলা)')).to_have_value('পিকআপ ড্রাইভার')
    page.get_by_label('চাকরির শিরোনাম (বাংলা)').scroll_into_view_if_needed()
    shot(page, '22-assistant-filled-form')
    page.get_by_role('button', name='পরবর্তী ধাপ').click()
    expect(page.get_by_label('সর্বনিম্ন বেতন (৳)')).to_have_value('16000')
    shot(page, '23-assistant-filled-step2')

    page.goto(BASE + '/employer/jobs/new'); page.wait_for_load_state('networkidle')
    page.locator('#assistant-text').fill('Security guard lagbe Tongi, 12000 taka, registration fee 500 taka, call 01712345678')
    page.get_by_role('button', name='খসড়া বানান').click()
    expect(page.get_by_text('এই পোস্ট প্রকাশ করা যাবে না')).to_be_visible(timeout=5000)
    shot(page, '24-assistant-blocked', full=True)

    # ---- Module 2, employer side: why my post is in review
    page.goto(BASE + '/employer/jobs/k5'); page.wait_for_load_state('networkidle')
    expect(page.get_by_text('কাজ এই পোস্টটি যাচাই করছে')).to_be_visible()
    shot(page, '25-screening-notice')
    logout()

    # ---- Module 2, seeker side: report a post
    login('rahim@demo.kaaj')
    page.goto(BASE + '/jobs/b3'); page.wait_for_load_state('networkidle')
    page.get_by_role('button', name='পোস্টটি রিপোর্ট করুন').click()
    page.get_by_label('আমার কাছে টাকা চেয়েছে').check()
    page.get_by_label('বিস্তারিত').fill('ইন্টারভিউতে ইউনিফর্মের জন্য ১,৫০০ টাকা চেয়েছে।')
    shot(page, '26-report-form', full=True)
    page.get_by_role('button', name='রিপোর্ট পাঠান').click()
    expect(page.get_by_text('ধন্যবাদ। একজন অ্যাডমিন পোস্টটি দেখবেন।')).to_be_visible()
    shot(page, '27-report-sent')
    ctx.clear_cookies()  # the public job page has no sign-out button

    # ---- Module 2, admin side: the moderation queue
    login('admin@kaaj.local')
    page.goto(BASE + '/admin/cases'); page.wait_for_load_state('networkidle')
    shot(page, '28-moderation-queue')
    page.get_by_role('button', name='মালয়েশিয়ায় কারখানার চাকরি').first.click()
    page.wait_for_load_state('networkidle')
    shot(page, '29-moderation-case-highlights', full=True)
    page.get_by_role('button', name='পোস্ট সরান').click()
    expect(page.get_by_text('পোস্ট সরাতে বা নিয়োগকর্তাকে স্থগিত করতে একটি ছোট নোট লিখুন।')).to_be_visible()
    page.locator('#moderation-note').scroll_into_view_if_needed()
    shot(page, '30-moderation-note-required')
    page.locator('#moderation-note').fill('চাকরিপ্রার্থীদের কাছে প্রসেসিং ফি চাওয়া নিষেধ।')
    page.get_by_role('button', name='নিয়োগকর্তাকে স্থগিত করুন').click()
    page.wait_for_timeout(300)
    shot(page, '31-moderation-confirm-suspend')
    page.get_by_role('button', name='হ্যাঁ, স্থগিত করুন').click()
    page.wait_for_load_state('networkidle')
    shot(page, '32-moderation-decided', full=True)

    # English (close the toast first so it does not cover the next shot)
    page.wait_for_timeout(5000)
    page.get_by_role('combobox', name='ভাষা').first.select_option('en')
    page.get_by_role('tab').first.click(); page.wait_for_load_state('networkidle')
    page.get_by_role('button', name='Online data entry').first.click(); page.wait_for_load_state('networkidle')
    shot(page, '33-moderation-english', full=True)

    browser.close()

print('\n'.join(problems) if problems else 'NO PAGE ERRORS')
