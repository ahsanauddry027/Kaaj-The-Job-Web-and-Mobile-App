package app.kaaj.job.internal;

import app.kaaj.job.JobType;
import app.kaaj.job.Shift;
import app.kaaj.shared.WagePeriod;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Writes the title and description of a draft in Bangla and English from the stated facts only (context
 * file §10.4: "Never invent a wage, benefit or requirement"). Used when no AI is available, and to fill
 * anything the AI left empty. The employer adds the duties; the assistant asks for them.
 */
final class DraftTemplate {

    /** Common job words, for a Bangla title when the employer wrote the job in Banglish or English. */
    private static final Map<String, String> WORDS = Map.ofEntries(
            Map.entry("pickup", "পিকআপ"), Map.entry("driver", "ড্রাইভার"), Map.entry("truck", "ট্রাক"),
            Map.entry("lorry", "লরি"), Map.entry("microbus", "মাইক্রোবাস"), Map.entry("car", "কার"),
            Map.entry("van", "ভ্যান"), Map.entry("bike", "বাইক"), Map.entry("delivery", "ডেলিভারি"),
            Map.entry("rider", "রাইডার"), Map.entry("helper", "হেল্পার"), Map.entry("garment", "গার্মেন্টস"),
            Map.entry("garments", "গার্মেন্টস"), Map.entry("sewing", "সেলাই"), Map.entry("machine", "মেশিন"),
            Map.entry("operator", "অপারেটর"), Map.entry("security", "সিকিউরিটি"), Map.entry("guard", "গার্ড"),
            Map.entry("office", "অফিস"), Map.entry("assistant", "সহকারী"), Map.entry("cook", "বাবুর্চি"),
            Map.entry("cleaner", "ক্লিনার"), Map.entry("electrician", "ইলেকট্রিশিয়ান"), Map.entry("plumber", "প্লাম্বার"),
            Map.entry("mechanic", "মেকানিক"), Map.entry("motor", "মোটর"), Map.entry("welder", "ওয়েল্ডার"),
            Map.entry("mason", "রাজমিস্ত্রি"), Map.entry("carpenter", "কাঠমিস্ত্রি"), Map.entry("painter", "রং মিস্ত্রি"),
            Map.entry("loader", "লোডার"), Map.entry("cashier", "ক্যাশিয়ার"), Map.entry("salesman", "সেলসম্যান"),
            Map.entry("tailor", "দর্জি"), Map.entry("worker", "কর্মী"), Map.entry("factory", "কারখানা"),
            Map.entry("warehouse", "গুদাম"), Map.entry("shop", "দোকান"), Map.entry("quality", "কোয়ালিটি"),
            Map.entry("inspector", "ইন্সপেক্টর"), Map.entry("caregiver", "কেয়ারগিভার"), Map.entry("labourer", "শ্রমিক"));

    private DraftTemplate() {
    }

    /** [Bangla, English] titles from the occupation found in the sentence, or [null, null]. */
    static String[] titles(OccupationFinder.Match match) {
        if (match == null) {
            return new String[] {null, null};
        }
        String name = match.matchedName();
        if (name.chars().allMatch(c -> c < 128)) {
            String english = Character.toUpperCase(name.charAt(0)) + name.substring(1);
            List<String> bangla = new ArrayList<>();
            for (String word : name.split("\\s+")) {
                String translated = WORDS.get(word);
                if (translated == null) {
                    return new String[] {withoutBrackets(match.term().nameBn()), english};
                }
                bangla.add(translated);
            }
            return new String[] {String.join(" ", bangla), english};
        }
        return new String[] {name, withoutBrackets(match.term().nameEn())};
    }

    static String descriptionEn(String title, SentenceFacts facts) {
        List<String> lines = new ArrayList<>();
        if (title != null) {
            lines.add("We are hiring: " + title + ".");
        }
        if (facts.openings() != null) {
            lines.add("Openings: " + facts.openings() + ".");
        }
        if (facts.place() != null) {
            lines.add("Place of work: " + facts.place() + ".");
        }
        if (facts.wageMin() != null) {
            lines.add("Wage: Tk " + takaEn(facts.wageMin(), facts.wageMax()) + " " + periodEn(facts.wagePeriod()) + ".");
        }
        if (facts.hours() != null) {
            lines.add("Working hours: " + facts.hours().replace("-", "–") + ".");
        }
        if (facts.shift() == Shift.NIGHT) {
            lines.add("Night shift.");
        }
        if (facts.jobType() == JobType.PART_TIME) {
            lines.add("Part-time job.");
        } else if (facts.jobType() == JobType.DAILY) {
            lines.add("Daily work.");
        }
        return lines.isEmpty() ? null : String.join("\n", lines);
    }

    static String descriptionBn(String title, SentenceFacts facts) {
        List<String> lines = new ArrayList<>();
        if (title != null) {
            lines.add("নিয়োগ: " + title + "।");
        }
        if (facts.openings() != null) {
            lines.add("পদ: " + bangla(String.valueOf(facts.openings())) + " জন।");
        }
        if (facts.place() != null) {
            lines.add("কাজের জায়গা: " + SentenceFacts.placeBn(facts.place()) + "।");
        }
        if (facts.wageMin() != null) {
            lines.add("বেতন: " + periodBn(facts.wagePeriod()) + " ৳" + takaBn(facts.wageMin(), facts.wageMax()) + "।");
        }
        if (facts.hours() != null) {
            lines.add("কাজের সময়: " + bangla(facts.hours().replace("-", "–")) + "।");
        }
        if (facts.shift() == Shift.NIGHT) {
            lines.add("রাতের শিফট।");
        }
        if (facts.jobType() == JobType.PART_TIME) {
            lines.add("খণ্ডকালীন কাজ।");
        } else if (facts.jobType() == JobType.DAILY) {
            lines.add("দৈনিক ভিত্তিতে কাজ।");
        }
        return lines.isEmpty() ? null : String.join("\n", lines);
    }

    static String withoutBrackets(String name) {
        if (name == null) {
            return null;
        }
        int bracket = name.indexOf('(');
        return bracket > 0 ? name.substring(0, bracket).strip() : name.strip();
    }

    // ---------------------------------------------------------------- numbers

    static String takaEn(int min, Integer max) {
        String low = String.format(Locale.ROOT, "%,d", min);
        return max == null || max == min ? low : low + "–" + String.format(Locale.ROOT, "%,d", max);
    }

    static String takaBn(int min, Integer max) {
        String low = bangla(lakh(min));
        return max == null || max == min ? low : low + "–" + bangla(lakh(max));
    }

    /** Bangladeshi grouping: 1,20,000. */
    static String lakh(int value) {
        String digits = Integer.toString(value);
        if (digits.length() <= 3) {
            return digits;
        }
        String last3 = digits.substring(digits.length() - 3);
        String rest = digits.substring(0, digits.length() - 3);
        StringBuilder grouped = new StringBuilder();
        while (rest.length() > 2) {
            grouped.insert(0, "," + rest.substring(rest.length() - 2));
            rest = rest.substring(0, rest.length() - 2);
        }
        return rest + grouped + "," + last3;
    }

    static String bangla(String text) {
        StringBuilder out = new StringBuilder(text.length());
        for (char c : text.toCharArray()) {
            out.append(c >= '0' && c <= '9' ? (char) ('০' + (c - '0')) : c);
        }
        return out.toString();
    }

    private static String periodEn(WagePeriod period) {
        return switch (period == null ? WagePeriod.MONTH : period) {
            case HOUR -> "an hour";
            case DAY -> "a day";
            case WEEK -> "a week";
            case MONTH -> "a month";
            case PIECE -> "per piece";
        };
    }

    private static String periodBn(WagePeriod period) {
        return switch (period == null ? WagePeriod.MONTH : period) {
            case HOUR -> "ঘণ্টায়";
            case DAY -> "দিনে";
            case WEEK -> "সপ্তাহে";
            case MONTH -> "মাসে";
            case PIECE -> "প্রতি পিস";
        };
    }
}
