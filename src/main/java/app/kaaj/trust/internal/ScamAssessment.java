package app.kaaj.trust.internal;

import java.util.List;

/** The optional AI check's answer (context file §10.6). Java decides what to do with it. */
record ScamAssessment(
        Risk risk,
        Boolean asksApplicantsForMoney,
        Boolean movesContactOffPlatform,
        Boolean overseasJob,
        String licenceNumber,
        List<String> redFlags,
        String reasonForModerator) {

    enum Risk { LOW, MEDIUM, HIGH }

    /** HIGH risk, or the model saw a request for money: an admin looks first. The model never blocks. */
    boolean worthAReview() {
        return risk == Risk.HIGH || Boolean.TRUE.equals(asksApplicantsForMoney);
    }
}
