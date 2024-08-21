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
        boolean haRichiesteORifiutiRecenti = rngs.random() <= 0.1;
        boolean haAnzianitaDiLavoro = rngs.random() <= 0.889;
        boolean isRapportoRataRedditoOk = rngs.random() <= 0.75;
        boolean haContrattoIndeterminato = rngs.random() <= 0.84989;
        boolean haCorrispondenzaInBancaDati = rngs.random() <= P_C;
        return new Applicant(streamIndex, haAnzianitaDiLavoro, isRapportoRataRedditoOk, haContrattoIndeterminato, haRichiesteORifiutiRecenti, haCorrispondenzaInBancaDati);
    }

/*
    Per mappare i motivi di rifiuto delle richieste di credito in quattro variabili booleane con probabilità differenti,
    dove ciascuna rappresenta un problema specifico, possiamo attribuire le probabilità in modo proporzionale alla frequenza delle problematiche.
    Il risultato finale sarà una variabile booleana che indicherà il rifiuto della richiesta in base alla combinazione di questi fattori.

    Mappatura delle variabili:
    ===============================

        boolean rifiutoRichiesteRecenti = rngs.random() <= 0.9;     // Problema più frequente
        boolean tipologiaContratto = rngs.random() <= 0.7;          // Secondo problema più comune
        boolean anzianitaLavoro = rngs.random() <= 0.6667;          // Problema meno comune
        boolean rapportoRataReddito = rngs.random() <= 0.5;         // Problema raro

        boolean richiestaRifiutata = rifiutoRichiesteRecenti && tipologiaContratto && anzianitaLavoro && rapportoRataReddito;

    Spiegazione:
    ===============================
    Quando una di queste condizioni è true, la richiesta viene rifiutata (richiestaRifiutata diventa true).
*/
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
        haCorrispondenzaInBancaDati = rngs.random() <= 0.822;
    }
}
