package app.kaaj.job.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import app.kaaj.job.Finding;
import app.kaaj.job.JobType;
import app.kaaj.job.Shift;
import app.kaaj.shared.AiText;
import app.kaaj.shared.KaajException;
import app.kaaj.shared.Taxonomy;
import app.kaaj.shared.Term;
import app.kaaj.shared.TermNames;
import app.kaaj.shared.WagePeriod;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticMessageSource;

/**
 * The job-post assistant without AI (context file §10.2: the template fallback). Mr Karim's sentence must
 * give the result in §10.4: 2 openings, Tongi, Tk 16,000–20,000 a month, 08:00–18:00, the questions, and
 * the 10-hour day flagged.
 */
class JobDraftsTest {

    static final String KARIM = "2 jon pickup driver lagbe, Tongi, beton 16-20 hajar, sokal 8ta theke sondha 6ta";
    static final Term DRIVER = new Term(UUID.randomUUID(), "ড্রাইভার (কার, পিকআপ, মাইক্রোবাস)", "Driver (car, pickup, microbus)");
    static final Term GUARD = new Term(UUID.randomUUID(), "নিরাপত্তা প্রহরী", "Security guard");

    @Test
    void karimsSentenceGivesTheExpectedFacts() {
        SentenceFacts facts = SentenceFacts.read(KARIM);
        assertThat(facts.openings()).isEqualTo(2);
        assertThat(facts.wageMin()).isEqualTo(16_000);
        assertThat(facts.wageMax()).isEqualTo(20_000);
        assertThat(facts.wagePeriod()).isEqualTo(WagePeriod.MONTH);
        assertThat(facts.hours()).isEqualTo("08:00-18:00");
        assertThat(facts.shift()).isEqualTo(Shift.DAY);
        assertThat(facts.jobType()).isEqualTo(JobType.FULL_TIME);
        assertThat(facts.place()).isEqualTo("Tongi");
        assertThat(facts.mentionsHoliday()).isFalse();
    }

    @Test
    void banglaAndEnglishSentencesToo() {
        SentenceFacts bangla = SentenceFacts.read("টঙ্গীতে ৩ জন নিরাপত্তা প্রহরী দরকার, বেতন ১২০০০ টাকা, রাত ১০টা থেকে সকাল ৬টা");
        assertThat(bangla.openings()).isEqualTo(3);
        assertThat(bangla.wageMin()).isEqualTo(12_000);
        assertThat(bangla.wagePeriod()).isEqualTo(WagePeriod.MONTH);
        assertThat(bangla.hours()).isEqualTo("22:00-06:00");
        assertThat(bangla.shift()).isEqualTo(Shift.NIGHT);
        assertThat(bangla.place()).isEqualTo("Tongi");

        SentenceFacts english = SentenceFacts.read("Need two delivery riders in Mirpur, Tk 600 per day, 9am to 5pm");
        assertThat(english.openings()).isEqualTo(2);
        assertThat(english.wageMin()).isEqualTo(600);
        assertThat(english.wagePeriod()).isEqualTo(WagePeriod.DAY);
        assertThat(english.hours()).isEqualTo("09:00-17:00");
        assertThat(english.place()).isEqualTo("Mirpur");
    }

    @Test
    void karimsDraftWithoutAi() {
        JobDrafts.Draft draft = drafts().draft(UUID.randomUUID(), KARIM, Locale.ENGLISH);
        assertThat(draft.source()).isEqualTo("TEMPLATE");
        assertThat(draft.titleBn()).isEqualTo("পিকআপ ড্রাইভার");
        assertThat(draft.titleEn()).isEqualTo("Pickup driver");
        assertThat(draft.occupation()).isEqualTo(DRIVER);
        assertThat(draft.openings()).isEqualTo(2);
        assertThat(draft.areaName()).isEqualTo("Tongi");
        assertThat(draft.wageMin()).isEqualTo(16_000);
        assertThat(draft.wageMax()).isEqualTo(20_000);
        assertThat(draft.hours()).isEqualTo("08:00-18:00");
        assertThat(draft.descriptionBn()).contains("৳১৬,০০০–২০,০০০");
        assertThat(draft.descriptionEn()).contains("Tk 16,000–20,000 a month");
        assertThat(draft.questions()).contains("holiday?", "overtime?", "licence?");
        assertThat(draft.findings()).extracting(Finding::code).contains(Finding.LONG_DAY);
        assertThat(draft.blocked()).isFalse();
    }

    @Test
    void aFeeRequestIsBlockedAndAPhoneNumberRemoved() {
        JobDrafts.Draft draft = drafts().draft(UUID.randomUUID(),
                "Security guard lagbe Tongi, beton 12000, registration fee 500 taka, call 01712345678", Locale.ENGLISH);
        assertThat(draft.blocked()).isTrue();
        assertThat(draft.findings()).extracting(Finding::kind).contains(Finding.Kind.BLOCK, Finding.Kind.REMOVED);
        assertThat(draft.descriptionEn()).doesNotContain("01712345678");
    }

    @Test
    void tooShortTextAndTheDailyLimit() {
        JobDrafts drafts = drafts();
        assertThatThrownBy(() -> drafts.draft(UUID.randomUUID(), "x", Locale.ENGLISH)).isInstanceOf(KaajException.class);
        UUID employer = UUID.randomUUID();
        drafts.draft(employer, KARIM, Locale.ENGLISH);
        drafts.draft(employer, KARIM, Locale.ENGLISH);
        assertThatThrownBy(() -> drafts.draft(employer, KARIM, Locale.ENGLISH))
                .isInstanceOfSatisfying(KaajException.class, e -> assertThat(e.code()).isEqualTo("job.draft.limit"));
    }

    @Test
    void lakhGrouping() {
        assertThat(DraftTemplate.lakh(16_000)).isEqualTo("16,000");
        assertThat(DraftTemplate.lakh(120_000)).isEqualTo("1,20,000");
        assertThat(DraftTemplate.bangla("08:00")).isEqualTo("০৮:০০");
    }

    // ---------------------------------------------------------------- fakes

    static JobDrafts drafts() {
        Clock clock = Clock.fixed(Instant.parse("2026-11-18T06:00:00Z"), ZoneOffset.UTC);
        StaticMessageSource messages = new StaticMessageSource();
        for (String code : List.of("title", "openings", "wage", "hours", "overtime", "place", "holiday", "licence", "duties")) {
            messages.addMessage("draft.question." + code, Locale.ENGLISH, code + "?");
        }
        AiText noAi = new AiText() {
            @Override
            public boolean available() {
                return false;
            }

            @Override
            public Optional<String> json(String systemPrompt, String userPrompt) {
                return Optional.empty();
            }
        };
        return new JobDrafts(noAi, new OccupationFinder(new FakeTaxonomy(), clock), messages,
                new JobProperties(30, false, true, 2), clock);
    }

    static class FakeTaxonomy implements Taxonomy {
        @Override
        public Optional<Term> occupation(UUID id) {
            return Optional.empty();
        }

        @Override
        public List<Term> skills(Collection<UUID> ids) {
            return List.of();
        }

        @Override
        public List<Term> searchOccupations(String query, int limit) {
            return List.of();
        }

        @Override
        public List<Term> searchSkills(String query, int limit) {
            return List.of();
        }

        @Override
        public List<TermNames> occupationNames() {
            return List.of(
                    new TermNames(DRIVER, List.of(DRIVER.nameEn(), DRIVER.nameBn(), "pickup driver", "driver", "চালক")),
                    new TermNames(GUARD, List.of(GUARD.nameEn(), GUARD.nameBn(), "security guard", "guard")));
        }

        @Override
        public List<TermNames> skillNames() {
            return List.of();
        }
    }
}
