package app.kaaj.job.internal;

import app.kaaj.job.JobType;
import app.kaaj.job.Shift;
import app.kaaj.shared.ScamPatterns;
import app.kaaj.shared.WagePeriod;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The facts an employer stated in one sentence, read with plain rules in Bangla, Banglish and English:
 * openings, wage and period, working hours, shift, job type and place. This is the job-post assistant's
 * fallback when no AI is available (context file §10.2: "Form with templates"), and a check on what the
 * AI returns: a wage the employer never wrote is not used.
 * <p>
 * Example: {@code "2 jon pickup driver lagbe, Tongi, beton 16-20 hajar, sokal 8ta theke sondha 6ta"} gives
 * 2 openings, Tk 16,000–20,000 a month, 08:00–18:00, day shift, full time, Tongi.
 */
record SentenceFacts(
        Integer openings,
        Integer wageMin,
        Integer wageMax,
        WagePeriod wagePeriod,
        String hours,
        Shift shift,
        JobType jobType,
        String place,
        boolean mentionsHoliday,
        boolean mentionsOvertime,
        boolean mentionsLicence,
        boolean hasDigits) {

    private static final int FLAGS = Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;

    private static final String NUMBER = "(\\d{1,3}(?:,\\d{2,3})+|\\d+(?:\\.\\d+)?)";
    private static final String MULTIPLIER = "(k|hajar|hazar|হাজার|thousand|lakh|lac|লাখ)?";
    private static final Pattern WAGE_RANGE = Pattern.compile(
            NUMBER + "\\s*" + MULTIPLIER + "\\s*(?:-|–|to|theke|থেকে|hote|হতে)\\s*" + NUMBER + "\\s*" + MULTIPLIER, FLAGS);
    private static final Pattern WAGE_SINGLE = Pattern.compile(
            "(?:tk\\.?|taka|৳|beton|salary|wage|pay|বেতন|মজুরি|মাইনে)\\s*:?\\s*" + NUMBER + "\\s*" + MULTIPLIER
                    + "|" + NUMBER + "\\s*" + MULTIPLIER + "\\s*(?:tk|taka|টাকা|৳)", FLAGS);

    /** "15k", "15 hajar": a number with a thousand-word is a wage even without "tk" or "beton". */
    private static final Pattern WAGE_THOUSANDS = Pattern.compile(
            "(?<![\\d.,])" + NUMBER + "\\s*(k|hajar|hazar|হাজার|thousand|lakh|lac|লাখ)(?![a-z])", FLAGS);

    private static final String PART = "(sokal|sakal|dupur|bikal|bikel|sondha|sondhya|shondha|rat|raat|morning|evening|night|"
            + "সকাল|দুপুর|বিকাল|বিকেল|সন্ধ্যা|রাত)";
    private static final String TIME = "(\\d{1,2})(?:[:.](\\d{2}))?\\s*(am|pm|a\\.m\\.|p\\.m\\.|ta|টা)?";
    private static final Pattern HOURS = Pattern.compile(
            "(?:" + PART + "\\s*)?" + TIME + "\\s*(?:-|–|to|theke|থেকে|porjonto|till|until)\\s*(?:" + PART + "\\s*)?" + TIME, FLAGS);

    private static final Map<String, Integer> NUMBER_WORDS = Map.ofEntries(
            Map.entry("one", 1), Map.entry("two", 2), Map.entry("three", 3), Map.entry("four", 4), Map.entry("five", 5),
            Map.entry("six", 6), Map.entry("seven", 7), Map.entry("eight", 8), Map.entry("nine", 9), Map.entry("ten", 10),
            Map.entry("ek", 1), Map.entry("dui", 2), Map.entry("tin", 3), Map.entry("char", 4), Map.entry("pach", 5),
            Map.entry("এক", 1), Map.entry("দুই", 2), Map.entry("তিন", 3), Map.entry("চার", 4), Map.entry("পাঁচ", 5));
    private static final Pattern OPENINGS_PEOPLE = Pattern.compile(
            "(\\d{1,3}|one|two|three|four|five|six|seven|eight|nine|ten|ek|dui|tin|char|pach|এক|দুই|তিন|চার|পাঁচ)\\s*"
                    + "(?:jon|জন|zon|people|persons|workers|staff|nos?\\.?|posts?|vacanc(?:y|ies)|পদ)(?![a-z])", FLAGS);
    private static final Pattern OPENINGS_PLURAL = Pattern.compile(
            "(?<![\\d,.])(\\d{1,3}|two|three|four|five|six|seven|eight|nine|ten)\\s+((?:[a-z-]+\\s+){0,2}[a-z-]+s)(?![a-z])", FLAGS);
    private static final List<String> NOT_PEOPLE = List.of("hours", "days", "years", "months", "weeks", "minutes",
            "thousands", "taka", "yrs", "hrs", "times", "pieces", "shifts", "rooms");

    /** Places in and around Dhaka and the big cities, in English and Bangla (the canonical English name first). */
    private static final Map<String, List<String>> PLACES = places();

    static SentenceFacts read(String sentence) {
        String text = ScamPatterns.normalise(sentence == null ? "" : sentence).toLowerCase(Locale.ROOT);
        int[] wage = wage(text);
        Integer wageMin = wage == null ? null : wage[0];
        Integer wageMax = wage == null ? null : wage[1];
        WagePeriod period = wage == null ? null : period(text, wageMax);
        String hours = hours(text);
        Shift shift = has(text, "\\b(?:night|rat|raat)\\b|রাত") || startsAtNight(hours) ? Shift.NIGHT : Shift.DAY;
        JobType type = has(text, "part[\\s-]?time|খণ্ডকালীন|পার্ট টাইম") ? JobType.PART_TIME
                : has(text, "daily basis|\\bhajira\\b|হাজিরা|দৈনিক ভিত্তি|day labou?r")
                  || (period == WagePeriod.DAY && wageMax != null && wageMax < 3000) ? JobType.DAILY
                : JobType.FULL_TIME;
        return new SentenceFacts(openings(text), wageMin, wageMax, period, hours, shift, type, place(text),
                has(text, "holiday|\\bchuti\\b|ছুটি|off day|day off|weekly off|\\bbondho\\b"),
                has(text, "overtime|\\bot\\b|ওভারটাইম"),
                has(text, "licen[cs]e|লাইসেন্স|\\blisence\\b"),
                text.chars().anyMatch(Character::isDigit));
    }

    // ---------------------------------------------------------------- openings

    private static Integer openings(String text) {
        Matcher people = OPENINGS_PEOPLE.matcher(text);
        if (people.find()) {
            return count(people.group(1));
        }
        Matcher plural = OPENINGS_PLURAL.matcher(text);
        while (plural.find()) {
            String[] words = plural.group(2).strip().split("\\s+");
            if (!NOT_PEOPLE.contains(words[words.length - 1])) {
                return count(plural.group(1));
            }
        }
        return null;
    }

    private static Integer count(String token) {
        Integer value = NUMBER_WORDS.get(token);
        if (value == null) {
            value = Integer.parseInt(token);
        }
        return value >= 1 && value <= 1000 ? value : null;
    }

    // ---------------------------------------------------------------- wage

    /** [min, max] in taka, or null. A range like "8-6" without a multiplier or a value of 100+ is not a wage. */
    private static int[] wage(String text) {
        Matcher range = WAGE_RANGE.matcher(text);
        while (range.find()) {
            String multiplier = range.group(4) != null ? range.group(4) : range.group(2);
            double factor = factor(multiplier);
            double low = number(range.group(1)) * (range.group(2) != null ? factor(range.group(2)) : factor);
            double high = number(range.group(3)) * (range.group(4) != null ? factor(range.group(4)) : factor);
            if ((multiplier != null || high >= 100) && low > 0 && high > 0 && !isTime(text, range.end())) {
                return order((int) Math.round(low), (int) Math.round(high));
            }
        }
        Matcher single = WAGE_SINGLE.matcher(text);
        while (single.find()) {
            String number = single.group(1) != null ? single.group(1) : single.group(3);
            String multiplier = single.group(1) != null ? single.group(2) : single.group(4);
            int value = (int) Math.round(number(number) * factor(multiplier));
            if (value >= 50) {
                return new int[] {value, value};
            }
        }
        Matcher thousands = WAGE_THOUSANDS.matcher(text);
        if (thousands.find()) {
            int value = (int) Math.round(number(thousands.group(1)) * factor(thousands.group(2)));
            return new int[] {value, value};
        }
        return null;
    }

    private static boolean isTime(String text, int end) {
        String after = text.substring(end, Math.min(text.length(), end + 6));
        return after.matches("^\\s*(?:(?:ta|am|pm)(?![a-z])|টা(?!কা)).*");
    }

    private static int[] order(int a, int b) {
        return a <= b ? new int[] {a, b} : new int[] {b, a};
    }

    private static double number(String text) {
        return Double.parseDouble(text.replace(",", ""));
    }

    private static double factor(String multiplier) {
        if (multiplier == null) {
            return 1;
        }
        return switch (multiplier) {
            case "lakh", "lac", "লাখ" -> 100_000;
            default -> 1000;
        };
    }

    /** The wage period the sentence names; an amount too big for that period (Tk 15,000 "a day") means a month. */
    private static WagePeriod period(String text, int amount) {
        WagePeriod named = namedPeriod(text, amount);
        boolean tooBig = switch (named) {
            case HOUR -> amount > 1_000;
            case DAY, PIECE -> amount > 3_000;
            case WEEK -> amount > 20_000;
            case MONTH -> false;
        };
        return tooBig ? WagePeriod.MONTH : named;
    }

    private static WagePeriod namedPeriod(String text, int amount) {
        if (has(text, "\\b(?:per hour|hourly|ghonta|ghonta[iy])\\b|ঘণ্টা")) {
            return WagePeriod.HOUR;
        }
        if (has(text, "\\b(?:per week|weekly|soptah|soptahik)\\b|সপ্তাহ")) {
            return WagePeriod.WEEK;
        }
        if (has(text, "\\b(?:per piece|piece rate|chukti)\\b|পিস|চুক্তি")) {
            return WagePeriod.PIECE;
        }
        if (has(text, "\\b(?:per month|monthly|a month|mash|masik|mase)\\b|মাস")) {
            return WagePeriod.MONTH;
        }
        if (has(text, "\\b(?:per day|daily|a day|dainik|roj|rojana|hajira)\\b|দৈনিক|রোজ|হাজিরা")) {
            return WagePeriod.DAY;
        }
        return amount >= 3000 ? WagePeriod.MONTH : WagePeriod.DAY;
    }

    // ---------------------------------------------------------------- hours

    /** "HH:mm-HH:mm", or null. Needs am/pm, "ta", a part of the day or minutes, so "16-20" is never read as hours. */
    private static String hours(String text) {
        Matcher m = HOURS.matcher(text);
        while (m.find()) {
            String part1 = m.group(1);
            String part2 = m.group(5);
            boolean marked = part1 != null || part2 != null || m.group(3) != null || m.group(4) != null
                    || m.group(7) != null || m.group(8) != null;
            if (!marked) {
                continue;
            }
            int startHour = Integer.parseInt(m.group(2));
            int endHour = Integer.parseInt(m.group(6));
            int startMinute = m.group(3) == null ? 0 : Integer.parseInt(m.group(3));
            int endMinute = m.group(7) == null ? 0 : Integer.parseInt(m.group(7));
            if (startHour > 23 || endHour > 23 || startMinute > 59 || endMinute > 59) {
                continue;
            }
            startHour = clock(startHour, part1, m.group(4));
            endHour = clock(endHour, part2 != null ? part2 : null, m.group(8));
            boolean endIsAmPm = m.group(8) != null && m.group(8).replace(".", "").matches("am|pm");
            if (part2 == null && !endIsAmPm && endHour <= startHour && endHour < 12) {
                endHour += 12;   // "8ta theke 6ta": the end is in the evening
            }
            if (endHour == startHour && endMinute == startMinute) {
                continue;
            }
            return "%02d:%02d-%02d:%02d".formatted(startHour, startMinute, endHour % 24, endMinute);
        }
        return null;
    }

    /** A 12-hour clock reading to 24 hours, from am/pm or the part of the day. */
    private static int clock(int hour, String partOfDay, String suffix) {
        String marker = suffix == null ? "" : suffix.replace(".", "");
        if (marker.equals("pm") && hour < 12) {
            return hour + 12;
        }
        if (marker.equals("am") && hour == 12) {
            return 0;
        }
        if (partOfDay == null || hour >= 12) {
            return hour;
        }
        return switch (partOfDay) {
            case "dupur", "দুপুর" -> hour <= 5 ? hour + 12 : hour;
            case "bikal", "bikel", "sondha", "sondhya", "shondha", "evening", "বিকাল", "বিকেল", "সন্ধ্যা" -> hour + 12;
            case "rat", "raat", "night", "রাত" -> hour >= 6 ? hour + 12 : hour;
            default -> hour;      // sokal, morning
        };
    }

    private static boolean startsAtNight(String hours) {
        if (hours == null) {
            return false;
        }
        int start = Integer.parseInt(hours.substring(0, 2));
        return start >= 20 || start < 4;
    }

    // ---------------------------------------------------------------- place

    private static String place(String text) {
        String found = null;
        int foundLength = 0;
        for (Map.Entry<String, List<String>> place : PLACES.entrySet()) {
            for (String name : place.getValue()) {
                boolean ascii = name.chars().allMatch(c -> c < 128);
                boolean match = ascii
                        ? Pattern.compile("(?<![a-z])" + Pattern.quote(name) + "(?![a-z])").matcher(text).find()
                        : text.contains(name);
                if (match && name.length() > foundLength) {
                    found = place.getKey();
                    foundLength = name.length();
                }
            }
        }
        return found;
    }

    /** The Bangla name of a place found by {@link #read}, for the Bangla description. */
    static String placeBn(String place) {
        List<String> names = place == null ? null : PLACES.get(place);
        if (names == null) {
            return place;
        }
        return names.stream().filter(name -> name.chars().anyMatch(c -> c >= 128)).findFirst().orElse(place);
    }

    private static Map<String, List<String>> places() {
        Map<String, List<String>> places = new LinkedHashMap<>();
        String[][] rows = {
                {"Tongi", "tongi", "টঙ্গী", "টঙ্গি"}, {"Gazipur", "gazipur", "গাজীপুর"}, {"Board Bazar", "board bazar", "বোর্ড বাজার"},
                {"Joydebpur", "joydebpur", "জয়দেবপুর"}, {"Kaliakair", "kaliakair", "কালিয়াকৈর"},
                {"Uttara", "uttara", "উত্তরা"}, {"Mirpur", "mirpur", "মিরপুর"}, {"Dhanmondi", "dhanmondi", "ধানমন্ডি"},
                {"Mohakhali", "mohakhali", "মহাখালী"}, {"Gulshan", "gulshan", "গুলশান"}, {"Banani", "banani", "বনানী"},
                {"Badda", "badda", "বাড্ডা"}, {"Motijheel", "motijheel", "মতিঝিল"}, {"Farmgate", "farmgate", "ফার্মগেট"},
                {"Tejgaon", "tejgaon", "তেজগাঁও"}, {"Mohammadpur", "mohammadpur", "মোহাম্মদপুর"},
                {"Jatrabari", "jatrabari", "যাত্রাবাড়ী"}, {"Khilgaon", "khilgaon", "খিলগাঁও"}, {"Rampura", "rampura", "রামপুরা"},
                {"Savar", "savar", "সাভার"}, {"Ashulia", "ashulia", "আশুলিয়া"}, {"Keraniganj", "keraniganj", "কেরানীগঞ্জ"},
                {"Narayanganj", "narayanganj", "নারায়ণগঞ্জ"}, {"Dhaka", "dhaka", "ঢাকা"},
                {"Chattogram", "chattogram", "chittagong", "চট্টগ্রাম"}, {"Sylhet", "sylhet", "সিলেট"},
                {"Rajshahi", "rajshahi", "রাজশাহী"}, {"Khulna", "khulna", "খুলনা"}, {"Cumilla", "cumilla", "comilla", "কুমিল্লা"},
        };
        for (String[] row : rows) {
            places.put(row[0], List.of(row).subList(1, row.length));
        }
        return places;
    }

    private static boolean has(String text, String regex) {
        return Pattern.compile(regex, FLAGS).matcher(text).find();
    }
}
