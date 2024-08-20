package org.pmcsn.model;

import org.pmcsn.configuration.ConfigurationManager;
import org.pmcsn.libraries.Rngs;

public class Applicant {
    final int streamIndex;
    final boolean acceptedByPreScoring;
    final boolean acceptedBySysScoring;
    final int acceptedByCC;

    public static Applicant createBaseApplicant(Rngs rngs) {
        ConfigurationManager config = new ConfigurationManager();
        int streamIndex = config.getInt("general", "applicantStreamIndex");
        rngs.selectStream(streamIndex);
        boolean acceptedBySysScoring = rngs.random() <= 0.42;
        int acceptedByCC = getNextRoute(rngs);
        return new Applicant(streamIndex, false, acceptedBySysScoring, acceptedByCC);
    }

    public static Applicant createImprovedApplicant(Rngs rngs) {
        ConfigurationManager config = new ConfigurationManager();
        int streamIndex = config.getInt("general", "applicantStreamIndex");
        rngs.selectStream(streamIndex);
        boolean acceptedByPreScoring = rngs.random() <= 0.51;
        boolean acceptedBySysScoring = rngs.random() <= 0.82;
        int acceptedByCC = getNextRoute(rngs);
        return new Applicant(streamIndex, acceptedByPreScoring, acceptedBySysScoring, acceptedByCC);
    }

    private Applicant(int streamIndex, boolean acceptedByPreScoring, boolean acceptedBySysScoring, int acceptedByCC) {
        this.streamIndex = streamIndex;
        this.acceptedByPreScoring = acceptedByPreScoring;
        this.acceptedBySysScoring = acceptedBySysScoring;
        this.acceptedByCC = acceptedByCC;
    }

    private static int getNextRoute(Rngs rngs) {
        double x = rngs.random();
        if (x < 0.06) {
            return -1;
        } else if (x >= 0.06 && x < 0.71) {
            return 0;
        } else {
            return 1;
        }
    }

    public boolean isAcceptedByPreScoring() {
        return acceptedByPreScoring;
    }

    public boolean isAcceptedBySysScoring() {
        return acceptedBySysScoring;
    }

    public int getNextRoute() {
        return acceptedByCC;
    }

    public Applicant feedback(Rngs rngs) {
        rngs.selectStream(streamIndex);
        boolean acceptedBySysScoring = rngs.random() <= 0.42;
        int acceptedByCC = getNextRoute(rngs);
        return new Applicant(streamIndex, true, acceptedBySysScoring, acceptedByCC);
    }

    public Applicant improvedFeedback(Rngs rngs) {
        rngs.selectStream(streamIndex);
        boolean acceptedBySysScoring = rngs.random() <= 0.82;
        int acceptedByCC = getNextRoute(rngs);
        return new Applicant(streamIndex, true, acceptedBySysScoring, acceptedByCC);
    }
}
