package org.pmcsn.model;

import org.pmcsn.configuration.ConfigurationManager;
import org.pmcsn.libraries.Rngs;

public class Applicant {
    final int streamIndex;
    boolean haAnzianitaDiLavoro;
    boolean isRapportoRataRedditoOk;
    boolean haContrattoIndeterminato;
    boolean haRichiesteORifiutiRecenti;
    boolean haCorrispondenzaInBancaDati;
    int nextRoute;


    public static Applicant createBaseApplicant(Rngs rngs) {
        ConfigurationManager config = new ConfigurationManager();
        int streamIndex = config.getInt("general", "applicantStreamIndex");
        rngs.selectStream(streamIndex);
        boolean haRichiesteORifiutiRecenti = rngs.random() < 0.1;
        boolean haAnzianitaDiLavoro = rngs.random() < 0.889;
        boolean isRapportoRataRedditoOk = rngs.random() < 0.75;
        boolean haContrattoIndeterminato = rngs.random() < 0.849899;
        boolean haCorrispondenzaInBancaDati = rngs.random() < 0.82355;
        int acceptedByCC = getNextRoute(rngs);
        return new Applicant(streamIndex, haAnzianitaDiLavoro, isRapportoRataRedditoOk, haContrattoIndeterminato, haRichiesteORifiutiRecenti, haCorrispondenzaInBancaDati, acceptedByCC);
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



/*
    public static Applicant createImprovedApplicant(Rngs rngs) {
        ConfigurationManager config = new ConfigurationManager();
        int streamIndex = config.getInt("general", "applicantStreamIndex");
        rngs.selectStream(streamIndex);
        boolean acceptedByPreScoring = rngs.random() <= 0.51;
        boolean acceptedBySysScoring = rngs.random() <= 0.82;
        int acceptedByCC = getNextRoute(rngs);
        return new Applicant(streamIndex, acceptedByPreScoring, acceptedBySysScoring, acceptedByCC);
    }
 */

    private Applicant(
            int streamIndex,
            boolean haAnzianitaDiLavoro,
            boolean isRapportoRataRedditoOk,
            boolean haContrattoIndeterminato,
            boolean haRichiesteORifiutiRecenti,
            boolean presenteInBancheDatiInterbancarie,
            int nextRoute) {
        this.streamIndex = streamIndex;
        this.haAnzianitaDiLavoro = haAnzianitaDiLavoro;
        this.isRapportoRataRedditoOk = isRapportoRataRedditoOk;
        this.haContrattoIndeterminato = haContrattoIndeterminato;
        this.haRichiesteORifiutiRecenti = haRichiesteORifiutiRecenti;
        this.haCorrispondenzaInBancaDati = presenteInBancheDatiInterbancarie;
        this.nextRoute = nextRoute;
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

    public int getNextRoute() {
        return nextRoute;
    }

    public Applicant feedback(Rngs rngs) {
        rngs.selectStream(streamIndex);
        return Applicant.createBaseApplicant(rngs);
    }

    public Applicant improvedFeedback(Rngs rngs) {
        rngs.selectStream(streamIndex);
        boolean haCorrispondenza = rngs.random() <= 0.82;
        int acceptedByCC = getNextRoute(rngs);
        return copy(haCorrispondenza, acceptedByCC);
    }

    private Applicant copy(boolean haCorrispondenzaInBancaDati, int nextRoute) {
        return new Applicant(
                streamIndex,
                haAnzianitaDiLavoro,
                isRapportoRataRedditoOk,
                haContrattoIndeterminato,
                haRichiesteORifiutiRecenti,
                haCorrispondenzaInBancaDati,
                nextRoute
                );
    }
}
