package app.kaaj.shared;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The shared list of warning patterns, in Bangla, Banglish and English (context file §10.4 and §10.6):
 * requests for money from job seekers, contact details that move people off Kaaj, requirements that may
 * exclude people unfairly, easy-money offers and overseas jobs without a recruiting licence.
 * <p>
 * Member-3 wrote it for the job-post assistant and scam screening; Member-2's chat flags (messages and
 * notifications) use the same list. Everything here is plain Java, so the rules are tested, not prompted.
 * <p>
 * Bangla words are matched as plain text (Java's {@code \b} doesn't understand Bangla vowel signs); English
 * and Banglish words use word boundaries. Bangla digits (০–৯) are turned into 0–9 before matching, so
 * "০১৭১২-৩৪৫৬৭৮" is found as a phone number.
 */
public final class ScamPatterns {

    public enum Kind {
        /** Asks job seekers for money: registration, processing, training, visa or medical fees, deposits. */
        MONEY_REQUEST,
        /** Phone numbers, e-mail addresses, "WhatsApp only": contact that bypasses Kaaj. */
        CONTACT,
        /** Gender, age, religion, marital status or appearance. */
        UNFAIR_REQUIREMENT,
        /** "Earn Tk 2,000 a day from home", multi-level marketing. */
        EASY_MONEY,
        /** A job abroad with no recruiting licence number in the post. */
        OVERSEAS_NO_LICENCE
    }

    /** One match: what kind, and the words from the text, for the employer and the moderator to see. */
    public record Hit(Kind kind, String quote) {
    }

    private static final int FLAGS = Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;

    // ---------------------------------------------------------------- requests for money

    private static final List<Pattern> MONEY = List.of(
            p("\\b(?:registration|processing|training|visa|medical|application|joining|admission|"
                    + "interview|form|uniform)\\s+(?:fees?|charges?)\\b"),
            p("\\b(?:registration|processing)\\s+(?:costs?|money)\\b"),
            p("\\b(?:security|refundable)\\s+(?:deposit|money)\\b"),
            p("\\bdeposit\\s+(?:of\\s+)?(?:tk\\.?|taka|bdt|৳)\\s*\\d"),
            p("\\badvance\\s+payment\\b|\\bpay\\s+in\\s+advance\\b"),
            p("\\b(?:send|transfer)\\s+(?:us\\s+)?(?:tk\\.?|taka|bdt|৳)?\\s*\\d[\\d,]*"),
            p("\\bpay\\s+(?:tk\\.?|taka|bdt|৳)?\\s*\\d[\\d,]*\\s*(?:tk|taka)?\\s+(?:to|for)\\s+"
                    + "(?:apply|register|registration|join|joining|the\\s+form)\\b"),
            p("\\bfees?\\s+(?:of\\s+)?(?:tk\\.?|taka|bdt|৳)\\s*\\d[\\d,]*"),
            p("(?:\\btk\\.?|\\btaka|\\bbdt|৳)\\s*\\d[\\d,]*\\s*(?:fees?|charges?)\\b"),
            p("\\b(?:jamanot|jamanat|joma\\s+dite|taka\\s+(?:dite|pathate|joma)|fee\\s+(?:lagbe|dite\\s+hobe))\\b"),
            p("\\b(?:bkash|bikash|nagad|rocket)\\b[^.\\n]{0,25}?\\b(?:pathan|pathiye|send)\\b"),
            p("(?:রেজিস্ট্রেশন|নিবন্ধন|প্রসেসিং|প্রশিক্ষণ|ট্রেনিং|ভিসা|মেডিকেল|আবেদন|ফরম|ভর্তি|জয়েনিং)\\s*(?:ফি|চার্জ)"),
            p("(?:রেজিস্ট্রেশন|প্রসেসিং)\\s*খরচ|জামানত"),
            p("অগ্রিম\\s*টাকা\\s*(?:দিতে|জমা|পাঠা)|টাকা\\s*জমা\\s*(?:দিতে|দিন|রাখতে)|ফি\\s*দিতে\\s*হবে|টাকা\\s*দিতে\\s*হবে"),
            p("(?:বিকাশ|নগদ|রকেট)[^।.\\n]{0,25}?(?:পাঠান|পাঠিয়ে|সেন্ড)"));

    /** "No registration fee", "কোনো ফি নেই": a match right after or before these words is not a request. */
    private static final Pattern NEGATION_BEFORE = p(
            "(?:\\bno|\\bwithout|\\bfree\\s+of|\\bzero|কোনো|কোন|বিনা|ছাড়া)\\s*\\S*\\s*$");
    private static final Pattern NEGATION_AFTER = p(
            "^\\s*\\S*\\s*(?:নেই|লাগবে\\s*না|নাই|lagbe\\s+na|nei|not\\s+(?:required|needed)|free\\b)");

    // ---------------------------------------------------------------- contact details

    private static final List<Pattern> CONTACT = List.of(
            // Bangladeshi mobile numbers: 01[3-9] and 8 more digits, with +880 or 0, spaces or dashes allowed
            p("(?<![\\d])(?:\\+?880[\\s-]?|0)1[3-9](?:[\\s-]?\\d){8}(?![\\d])"),
            p("[\\w.+-]+@[\\w-]+(?:\\.[\\w-]+)+"),
            p("\\b(?:whats\\s?app|imo|viber|telegram)\\b"),
            p("হোয়াটসঅ্যাপ|হোয়াটস অ্যাপ|ইমো|টেলিগ্রাম"));

    // ---------------------------------------------------------------- requirements that may exclude people

    private static final List<Pattern> UNFAIR = List.of(
            p("\\b(?:male|female|men|women|boys?|girls?)\\s+only\\b"),
            p("\\bonly\\s+(?:male|female|men|women|boys?|girls?)\\b"),
            p("\\b(?:male|female)\\s+(?:candidates?|applicants?|workers?)\\b"),
            p("\\b(?:sudhu|shudhu|shudhumatro)\\s+(?:chele|meye|purush|mohila)\\b"),
            p("শুধু(?:মাত্র)?\\s*(?:ছেলে|মেয়ে|পুরুষ|মহিলা|নারী)|(?:পুরুষ|মহিলা|নারী)\\s*প্রার্থী"),
            p("\\bage\\s*(?:limit|:)?\\s*\\d{2}\\s*(?:-|–|to)\\s*\\d{2}\\b"),
            p("\\b(?:under|below|maximum|max)\\s+(?:age\\s+)?\\d{2}\\s*(?:years|yrs)\\b"),
            // "above 18" is the legal minimum age, not a nudge
            p("\\b(?:above|over)\\s+(?:age\\s+)?(?!1[0-8]\\b)\\d{2}\\s*(?:years|yrs)\\b"),
            p("\\bboyos\\b|বয়স\\s*(?:সীমা|:)?\\s*(?!1[0-8]\\b)\\d{2}"),
            p("\\b(?:muslim|hindu|christian|buddhist)\\b|মুসলিম|মুসলমান|হিন্দু|খ্রিস্টান|বৌদ্ধ|ধর্ম"),
            p("\\b(?:unmarried|married|obibahito|bibahito)\\b|অবিবাহিত|বিবাহিত"),
            p("\\b(?:good[\\s-]looking|fair\\s+(?:skin|complexion)|beautiful|attractive)\\b|ফর্সা|সুন্দরী|চেহারা|উচ্চতা"));

    // ---------------------------------------------------------------- easy money and overseas jobs

    private static final List<Pattern> EASY_MONEY = List.of(
            p("\\b(?:work|job|earn|income)\\s+from\\s+home\\b"),
            p("\\bearn\\s+(?:up\\s+to\\s+)?(?:tk\\.?|taka|bdt|৳)?\\s*\\d[\\d,]*\\s*(?:daily|per\\s+day|a\\s+day|every\\s+day)\\b"),
            p("\\b(?:mlm|multi[\\s-]level\\s+marketing|network\\s+marketing|refer\\s+and\\s+earn|join\\s+and\\s+earn)\\b"),
            p("\\bonline\\s+(?:income|earning)\\b|\\bghore\\s+bose\\b"),
            p("ঘরে\\s*বসে|অনলাইনে\\s*আয়|অনলাইন\\s*ইনকাম|নেটওয়ার্ক\\s*মার্কেটিং|দৈনিক\\s*[\\d,]+\\s*টাকা\\s*আয়"));

    private static final Pattern OVERSEAS = p(
            "\\b(?:malaysia|saudi(?:\\s+arabia)?|dubai|uae|qatar|oman|kuwait|bahrain|singapore|romania|jordan|"
                    + "abroad|overseas)\\b|মালয়েশিয়া|সৌদি|দুবাই|কাতার|ওমান|কুয়েত|সিঙ্গাপুর|রোমানিয়া|বিদেশ|প্রবাস");
    private static final Pattern RECRUITING_LICENCE = p(
            "\\bRL[\\s.-]*(?:no\\.?)?\\s*\\d{2,5}\\b|recruiting\\s+licen[cs]e|রিক্রুটিং\\s*লাইসেন্স");

    private ScamPatterns() {
    }

    /**
     * Compiles a pattern case-insensitively. Bangla letters such as য় can be stored as one character or as
     * two (য + nukta); both the patterns and the text are put in the same Unicode form (NFC) first.
     */
    private static Pattern p(String regex) {
        return Pattern.compile(Normalizer.normalize(regex, Normalizer.Form.NFC), FLAGS);
    }

    /** Every warning in the text, in the order of the kinds above. The same quote is listed once. */
    public static List<Hit> find(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        String normal = normalise(text);
        Set<Hit> hits = new LinkedHashSet<>();
        for (Pattern pattern : MONEY) {
            Matcher m = pattern.matcher(normal);
            while (m.find()) {
                if (!negated(normal, m.start(), m.end())) {
                    hits.add(new Hit(Kind.MONEY_REQUEST, quote(normal, m)));
                }
            }
        }
        addAll(hits, Kind.CONTACT, CONTACT, normal);
        addAll(hits, Kind.UNFAIR_REQUIREMENT, UNFAIR, normal);
        addAll(hits, Kind.EASY_MONEY, EASY_MONEY, normal);
        Matcher abroad = OVERSEAS.matcher(normal);
        if (abroad.find() && !RECRUITING_LICENCE.matcher(normal).find()) {
            hits.add(new Hit(Kind.OVERSEAS_NO_LICENCE, quote(normal, abroad)));
        }
        return List.copyOf(hits);
    }

    public static boolean has(String text, Kind kind) {
        return find(text).stream().anyMatch(hit -> hit.kind() == kind);
    }

    /**
     * The text with phone numbers, e-mail addresses and messaging-app names replaced by "…", so contact
     * goes through Kaaj. Also used to strip personal details before text is sent to a free AI tier (§10.7).
     * Everything else, Bangla digits included, stays as it was.
     */
    public static String removeContacts(String text) {
        if (text == null) {
            return null;
        }
        String base = Normalizer.normalize(text, Normalizer.Form.NFC);
        String shadow = asciiDigits(base);           // same length as base, so the positions match
        boolean[] remove = new boolean[base.length()];
        boolean found = false;
        for (Pattern pattern : CONTACT) {
            Matcher m = pattern.matcher(shadow);
            while (m.find()) {
                for (int i = m.start(); i < m.end(); i++) {
                    remove[i] = true;
                }
                found = true;
            }
        }
        if (!found) {
            return text;
        }
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < base.length(); i++) {
            if (!remove[i]) {
                out.append(base.charAt(i));
            } else if (i == 0 || !remove[i - 1]) {
                out.append('…');
            }
        }
        return out.toString().replaceAll("…(?:[\\s,;/]*…)+", "…").strip();
    }

    public static boolean hasContacts(String text) {
        return find(text).stream().anyMatch(hit -> hit.kind() == Kind.CONTACT);
    }

    /** Bangla digits to 0–9 and runs of spaces to one space; nothing else changes. */
    public static String normalise(String text) {
        return asciiDigits(Normalizer.normalize(text, Normalizer.Form.NFC)).replaceAll("[ \\t\\x{00A0}]+", " ");
    }

    private static String asciiDigits(String text) {
        StringBuilder out = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            out.append(c >= '০' && c <= '৯' ? (char) ('0' + (c - '০')) : c);
        }
        return out.toString();
    }

    private static void addAll(Set<Hit> hits, Kind kind, List<Pattern> patterns, String text) {
        for (Pattern pattern : patterns) {
            Matcher m = pattern.matcher(text);
            while (m.find()) {
                hits.add(new Hit(kind, quote(text, m)));
            }
        }
    }

    private static boolean negated(String text, int start, int end) {
        String before = text.substring(Math.max(0, start - 20), start);
        String after = text.substring(end, Math.min(text.length(), end + 20));
        return NEGATION_BEFORE.matcher(before).find() || NEGATION_AFTER.matcher(after).find();
    }

    private static String quote(String text, Matcher m) {
        String found = text.substring(m.start(), m.end()).strip();
        return found.length() <= 80 ? found : found.substring(0, 80) + "…";
    }

    /** For log lines and tests: the kinds found, lower case. */
    public static List<String> kinds(List<Hit> hits) {
        List<String> names = new ArrayList<>();
        for (Hit hit : hits) {
            String name = hit.kind().name().toLowerCase(Locale.ROOT);
            if (!names.contains(name)) {
                names.add(name);
            }
        }
        return names;
    }
}
