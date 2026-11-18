"""Strings for the job seeker side and the applicant review (final build): values/strings_seeker.xml and
values-bn/strings_seeker.xml, same keys in both."""
import sys
from pathlib import Path
from strings_m2 import android_escape

S = [
    ("nav_find_jobs", "Find jobs", "কাজ খুঁজুন"),
    ("nav_saved", "Saved", "সংরক্ষিত"),
    ("nav_applications", "Applications", "আবেদন"),
    ("nav_profile", "Profile", "প্রোফাইল"),

    ("search_placeholder", "Search: driver, ড্রাইভার, garments…", "খুঁজুন: ড্রাইভার, driver, গার্মেন্টস…"),
    ("search_filters", "Filters", "ফিল্টার"),
    ("search_near_me", "Near my area", "আমার এলাকার কাছে"),
    ("search_near_me_hint", "Uses the area on your profile, or the phone's location.", "প্রোফাইলের এলাকা বা ফোনের লোকেশন ব্যবহার করে।"),
    ("search_radius", "Within %1$s km", "%1$s কিমির মধ্যে"),
    ("search_any_type", "Any type", "যেকোনো ধরন"),
    ("search_any_shift", "Any shift", "যেকোনো শিফট"),
    ("search_wage_min", "At least ৳ per month", "মাসে কমপক্ষে ৳"),
    ("search_sort", "Sort", "সাজান"),
    ("search_sort_newest", "Newest", "নতুন আগে"),
    ("search_sort_nearest", "Nearest", "কাছের আগে"),
    ("search_sort_wage", "Highest wage", "বেশি বেতন আগে"),
    ("search_clear", "Clear filters", "ফিল্টার মুছুন"),
    ("search_empty", "No jobs match. Try fewer filters or another word.", "কোনো চাকরি মেলেনি। কম ফিল্টার বা অন্য শব্দ দিয়ে চেষ্টা করুন।"),
    ("search_km_away", "%1$s km away", "%1$s কিমি দূরে"),
    ("search_no_location", "Add your area to your profile, or allow location, to search near you.",
     "কাছের কাজ খুঁজতে প্রোফাইলে এলাকা দিন, অথবা লোকেশনের অনুমতি দিন।"),
    ("saved_empty", "No saved jobs yet. Tap the star on a job to keep it here.", "এখনও কোনো চাকরি সংরক্ষণ করেননি। চাকরির তারকা চিহ্নে ট্যাপ করুন।"),
    ("job_save", "Save", "সংরক্ষণ"),
    ("job_unsave", "Saved", "সংরক্ষিত"),
    ("job_saved_done", "Saved for later.", "পরে দেখার জন্য সংরক্ষিত হয়েছে।"),
    ("job_unsaved_done", "Removed from saved jobs.", "সংরক্ষিত তালিকা থেকে সরানো হয়েছে।"),

    ("apply_button", "Apply with my profile", "আমার প্রোফাইল দিয়ে আবেদন"),
    ("apply_title", "Apply for this job", "এই চাকরিতে আবেদন"),
    ("apply_message", "Message to the employer (optional)", "নিয়োগকর্তার জন্য বার্তা (ঐচ্ছিক)"),
    ("apply_hint", "The employer sees your profile: name, occupations, skills, experience and area. Your phone number stays hidden until they shortlist you.",
     "নিয়োগকর্তা আপনার প্রোফাইল দেখবেন: নাম, পেশা, দক্ষতা, অভিজ্ঞতা ও এলাকা। শর্টলিস্ট না করা পর্যন্ত আপনার ফোন নম্বর গোপন থাকবে।"),
    ("apply_send", "Send application", "আবেদন পাঠান"),
    ("apply_done", "Application sent.", "আবেদন পাঠানো হয়েছে।"),
    ("apply_already", "You applied: %1$s", "আপনি আবেদন করেছেন: %1$s"),
    ("apply_need_profile", "Add your name and at least one occupation to your profile first.", "আগে প্রোফাইলে আপনার নাম ও অন্তত একটি পেশা দিন।"),
    ("apply_open_profile", "Complete my profile", "প্রোফাইল সম্পূর্ণ করুন"),
    ("apply_again", "Apply again", "আবার আবেদন করুন"),

    ("app_status_applied", "Applied", "আবেদন করা হয়েছে"),
    ("app_status_viewed", "Viewed", "দেখা হয়েছে"),
    ("app_status_shortlisted", "Shortlisted", "শর্টলিস্টেড"),
    ("app_status_interview", "Interview", "ইন্টারভিউ"),
    ("app_status_hired", "Hired", "নিয়োগ পেয়েছেন"),
    ("app_status_declined", "Not selected", "বাছাই হয়নি"),
    ("app_status_withdrawn", "Withdrawn", "প্রত্যাহার করা হয়েছে"),
    ("applications_title", "My applications", "আমার আবেদন"),
    ("applications_empty", "You haven't applied to any job yet.", "আপনি এখনও কোনো চাকরিতে আবেদন করেননি।"),
    ("application_title", "Application", "আবেদন"),
    ("application_applied_on", "Applied: %1$s", "আবেদন: %1$s"),
    ("application_interview", "Interview: %1$s", "ইন্টারভিউ: %1$s"),
    ("application_timeline", "What happened", "কী কী হয়েছে"),
    ("application_withdraw", "Withdraw application", "আবেদন প্রত্যাহার"),
    ("application_withdraw_confirm", "Withdraw this application? The employer will no longer see it.",
     "আবেদনটি প্রত্যাহার করবেন? নিয়োগকর্তা আর এটি দেখতে পাবেন না।"),
    ("application_withdrawn_done", "Application withdrawn.", "আবেদন প্রত্যাহার করা হয়েছে।"),
    ("application_job_closed", "This post is no longer open.", "এই পোস্টটি আর খোলা নেই।"),
    ("actor_seeker", "You", "আপনি"),
    ("actor_employer", "Employer", "নিয়োগকর্তা"),
    ("actor_system", "Kaaj", "কাজ"),

    ("seeker_profile_title", "My profile", "আমার প্রোফাইল"),
    ("seeker_profile_subtitle", "Employers see this when you apply. No CV needed.", "আবেদন করলে নিয়োগকর্তারা এটি দেখবেন। সিভি লাগবে না।"),
    ("seeker_complete", "Profile %1$s%% complete", "প্রোফাইল %1$s%% সম্পূর্ণ"),
    ("seeker_display_name", "Your name", "আপনার নাম"),
    ("seeker_phone", "Mobile number (optional)", "মোবাইল নম্বর (ঐচ্ছিক)"),
    ("seeker_phone_hint", "Hidden until an employer shortlists you.", "কোনো নিয়োগকর্তা শর্টলিস্ট না করা পর্যন্ত গোপন থাকবে।"),
    ("seeker_occupations", "Occupations (up to 5)", "পেশা (সর্বোচ্চ ৫টি)"),
    ("seeker_skills", "Skills", "দক্ষতা"),
    ("seeker_years", "Years of experience", "অভিজ্ঞতা (বছর)"),
    ("seeker_education", "Education (optional)", "শিক্ষা (ঐচ্ছিক)"),
    ("seeker_documents", "Licences I hold", "আমার লাইসেন্স"),
    ("seeker_documents_hint", "Not verified yet: skill certification checks them later.", "এখনও যাচাই হয়নি: পরে দক্ষতা সনদ যাচাইয়ে দেখা হবে।"),
    ("seeker_home", "Where I live (rounded to about 500 m)", "আমি যেখানে থাকি (প্রায় ৫০০ মিটারে গোল করা)"),
    ("seeker_area", "Area name", "এলাকার নাম"),
    ("seeker_radius", "How far I can travel: %1$s km", "কতদূর যেতে পারি: %1$s কিমি"),
    ("seeker_expected_wage", "Expected wage (৳, optional)", "প্রত্যাশিত বেতন (৳, ঐচ্ছিক)"),
    ("seeker_shifts", "Shifts I can work", "যে শিফটে কাজ করতে পারি"),
    ("seeker_shift_part_time", "Part-time", "পার্ট-টাইম"),
    ("seeker_save", "Save profile", "প্রোফাইল সংরক্ষণ"),
    ("seeker_saved", "Profile saved.", "প্রোফাইল সংরক্ষিত হয়েছে।"),
    ("seeker_missing_display_name", "Name", "নাম"),
    ("seeker_missing_occupations", "Occupation", "পেশা"),
    ("seeker_missing_skills", "Skills", "দক্ষতা"),
    ("seeker_missing_experience", "Experience or education", "অভিজ্ঞতা বা শিক্ষা"),
    ("seeker_missing_home", "Home area on the map", "ম্যাপে বাড়ির এলাকা"),
    ("seeker_missing_expected_wage", "Expected wage", "প্রত্যাশিত বেতন"),
    ("seeker_missing_shifts", "Shifts", "শিফট"),
    ("v_years_range", "Between 0 and 60.", "০ থেকে ৬০ এর মধ্যে।"),
    ("v_wage_period_needed", "Choose how the wage is paid.", "বেতন কীভাবে দেওয়া হবে বেছে নিন।"),
    ("v_occupation_needed", "Choose at least one occupation to apply for jobs.", "চাকরিতে আবেদনের জন্য অন্তত একটি পেশা বেছে নিন।"),

    ("applicants_title", "Applicants", "আবেদনকারী"),
    ("applicants_open", "See applicants", "আবেদনকারী দেখুন"),
    ("applicants_empty", "No applications yet.", "এখনও কোনো আবেদন আসেনি।"),
    ("applicants_ranked", "Best match first. The score is explained under each person.", "সবচেয়ে মানানসই আগে। প্রতিজনের নিচে স্কোরের কারণ দেখানো হয়েছে।"),
    ("applicant_title", "Applicant", "আবেদনকারী"),
    ("applicant_match", "Match %1$s%%", "মিল %1$s%%"),
    ("applicant_reasons", "Why they fit", "কেন মানানসই"),
    ("applicant_gaps", "What's missing", "যা নেই"),
    ("applicant_phone", "Phone", "ফোন"),
    ("applicant_phone_hidden", "Shown after you shortlist", "শর্টলিস্ট করলে দেখা যাবে"),
    ("applicant_message", "Their message", "তাঁর বার্তা"),
    ("applicant_decision", "Your decision", "আপনার সিদ্ধান্ত"),
    ("applicant_note", "Note for the applicant (optional)", "আবেদনকারীর জন্য মন্তব্য (ঐচ্ছিক)"),
    ("applicant_interview_place", "Interview place (optional)", "ইন্টারভিউয়ের জায়গা (ঐচ্ছিক)"),
    ("applicant_interview_time", "Interview date and time", "ইন্টারভিউয়ের তারিখ ও সময়"),
    ("applicant_updated", "Status updated. The applicant can see it.", "অবস্থা হালনাগাদ হয়েছে। আবেদনকারী এটি দেখতে পাবেন।"),
    ("applicant_do_shortlisted", "Shortlist", "শর্টলিস্ট করুন"),
    ("applicant_do_interview", "Call for interview", "ইন্টারভিউতে ডাকুন"),
    ("applicant_do_hired", "Hire", "নিয়োগ দিন"),
    ("applicant_do_declined", "Not selected", "বাছাই হয়নি"),
    ("applicant_years", "%1$s years of experience", "%1$s বছরের অভিজ্ঞতা"),
    ("applicant_expects", "Expects %1$s", "প্রত্যাশা %1$s"),

    ("reason_occupation_match", "Works in this occupation", "এই পেশায় কাজ করেন"),
    ("reason_skills_match", "Has %1$s of %2$s skills asked", "চাওয়া %2$sটি দক্ষতার %1$sটি আছে"),
    ("reason_experience_ok", "%1$s years of experience (asked %2$s)", "%1$s বছরের অভিজ্ঞতা (চাওয়া %2$s)"),
    ("reason_documents_ok", "Has the required licence", "প্রয়োজনীয় লাইসেন্স আছে"),
    ("reason_near_home", "%1$s km from home", "বাড়ি থেকে %1$s কিমি"),
    ("reason_remote", "Remote job", "রিমোট কাজ"),
    ("gap_other_occupation", "Usually does other work", "সাধারণত অন্য কাজ করেন"),
    ("gap_skills_missing", "%1$s asked skills missing", "চাওয়া দক্ষতার %1$sটি নেই"),
    ("gap_experience_short", "Only %1$s years (asked %2$s)", "মাত্র %1$s বছর (চাওয়া %2$s)"),
    ("gap_documents_missing", "Licence not on profile", "প্রোফাইলে লাইসেন্স নেই"),
    ("gap_far_from_home", "%1$s km from home, beyond their travel distance", "বাড়ি থেকে %1$s কিমি, যাতায়াতের সীমার বাইরে"),
]


def write(path: Path, column: int, header: str) -> None:
    keys = [row[0] for row in S]
    assert len(keys) == len(set(keys)), "duplicate keys"
    lines = ['<?xml version="1.0" encoding="utf-8"?>', f"<!-- {header} -->", "<resources>"]
    for row in S:
        formatted = "%1$" in row[1]
        attrs = ""
        lines.append(f'    <string name="{row[0]}"{attrs}>{android_escape(row[column])}</string>')
    lines.append("</resources>")
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text("\n".join(lines) + "\n", encoding="utf-8")


if __name__ == "__main__":
    res = Path(sys.argv[1])
    write(res / "values" / "strings_seeker.xml", 1, "Job seeker side and applicant review (English). Bangla: values-bn/strings_seeker.xml.")
    write(res / "values-bn" / "strings_seeker.xml", 2, "চাকরিপ্রার্থীর অংশ ও আবেদনকারী পর্যালোচনা (বাংলা)।")
    print(len(S), "strings")
