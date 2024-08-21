package org.pmcsn.model;

import org.pmcsn.configuration.ConfigurationManager;
import org.pmcsn.libraries.Rngs;

public class Applicant {
    private static final double P_C = 0.82;
    final int streamIndex;
    boolean haAnzianitaDiLavoro;
    boolean isRapportoRataRedditoOk;
    boolean haContrattoIndeterminato;
    boolean haRichiesteORifiutiRecenti;
    boolean haCorrispondenzaInBancaDati;

    public static Applicant create(Rngs rngs) {
        ConfigurationManager config = new ConfigurationManager();
        int streamIndex = config.getInt("general", "applicantStreamIndex");
        rngs.selectStream(streamIndex);
        boolean haRichiesteORifiutiRecenti = rngs.random() <= 0.1; // Problema più frequente
        boolean haContrattoIndeterminato = rngs.random() <= 0.75; // Secondo problema più comune
        boolean haAnzianitaDiLavoro = rngs.random() <= 0.84989; // Problema meno comune
        boolean isRapportoRataRedditoOk = rngs.random() <= 0.889; // Problema raro
        return new Applicant(streamIndex, haAnzianitaDiLavoro, isRapportoRataRedditoOk, haContrattoIndeterminato, haRichiesteORifiutiRecenti);
    }

    private Applicant(
            int streamIndex,
            boolean haAnzianitaDiLavoro,
            boolean isRapportoRataRedditoOk,
            boolean haContrattoIndeterminato,
            boolean haRichiesteORifiutiRecenti) {
        this(streamIndex, haAnzianitaDiLavoro, isRapportoRataRedditoOk, haContrattoIndeterminato, haRichiesteORifiutiRecenti, false);
    }

    private Applicant(
            int streamIndex,
            boolean haAnzianitaDiLavoro,
            boolean isRapportoRataRedditoOk,
            boolean haContrattoIndeterminato,
            boolean haRichiesteORifiutiRecenti,
            boolean haCorrispondenzaInBancaDati) {
        this.streamIndex = streamIndex;
        this.haAnzianitaDiLavoro = haAnzianitaDiLavoro;
        this.isRapportoRataRedditoOk = isRapportoRataRedditoOk;
        this.haContrattoIndeterminato = haContrattoIndeterminato;
        this.haRichiesteORifiutiRecenti = haRichiesteORifiutiRecenti;
        this.haCorrispondenzaInBancaDati = haCorrispondenzaInBancaDati;
    }

    public boolean isAcceptedByPreScoring() {
        return haAnzianitaDiLavoro
                && isRapportoRataRedditoOk
                && haContrattoIndeterminato
                && !haRichiesteORifiutiRecenti;
    }

    public boolean isAcceptedBySysScoring() {
        return haAnzianitaDiLavoro
                && isRapportoRataRedditoOk
                && haContrattoIndeterminato
                && !haRichiesteORifiutiRecenti
                && haCorrispondenzaInBancaDati;
    }

    public boolean isAcceptedBySysScoring2() {
        return haCorrispondenzaInBancaDati;
    }

    public Applicant feedback(Rngs rngs) {
        rngs.selectStream(streamIndex);
        return Applicant.create(rngs);
    }

    public Applicant improvedFeedback(Rngs rngs) {
        rngs.selectStream(streamIndex);
        boolean haCorrispondenza = rngs.random() <= P_C;
        return copy(haCorrispondenza);
    }

    private Applicant copy(boolean haCorrispondenzaInBancaDati) {
        return new Applicant(
                streamIndex,
                haAnzianitaDiLavoro,
                isRapportoRataRedditoOk,
                haContrattoIndeterminato,
                haRichiesteORifiutiRecenti,
                haCorrispondenzaInBancaDati);
    }

    public void setHasCorrispondenzaInBancaDati(Rngs rngs) {
        haCorrispondenzaInBancaDati = rngs.random() <= P_C;
    }
}
