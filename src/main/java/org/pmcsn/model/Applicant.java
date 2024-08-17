package org.pmcsn.model;

import org.pmcsn.configuration.ConfigurationManager;
import org.pmcsn.libraries.Rngs;

public class Applicant {

    public Applicant(boolean haAnzianitaDiLavoro, boolean isRapportoRataRedditoOk, boolean haContrattoIndeterminato, boolean haRichiesteORifiutiRecenti, boolean haCorrispondenzaInBancaDati) {
        this.haAnzianitaDiLavoro = haAnzianitaDiLavoro;
        this.isRapportoRataRedditoOk = isRapportoRataRedditoOk;
        this.haContrattoIndeterminato = haContrattoIndeterminato;
        this.haRichiesteORifiutiRecenti = haRichiesteORifiutiRecenti;
        this.haCorrispondenzaInBancaDati = haCorrispondenzaInBancaDati;
    }

    boolean haAnzianitaDiLavoro;
    boolean isRapportoRataRedditoOk;
    boolean haContrattoIndeterminato;
    boolean haRichiesteORifiutiRecenti;
    boolean haCorrispondenzaInBancaDati;

    public Applicant(Rngs rngs) {
        // RIFIUTO O RICHIESTE RECENTE -> preventivi su internet che vengono rifiutati o sono comunque sulla banca dati CRIEF
        // e quindi risulta una richiesta con rifiuto o richieste multiple con una somma totale dei vari contratti
        // troppo elevata e quindi non si può pensare a un altro contratto

        // TIPO DI CONTRATTO -> spesso le persone credono di essere in regola ma non gli vengono versati i contributi (rip sistema italiano dei contratti)
        // spesso quindi si presentano con contratti che danno poche garanzie

        // ANZIANITA DI LAVORO -> accade che è il primo lavoro o la frase solita è "mi hanno messo in regola da poco"

        // RAPPORTO RATA/REDDITO -> è la cosa che dà meno problemi perché è più raro che se vengo a chiedere un prestito so già che posso pagare una rata
        ConfigurationManager config = new ConfigurationManager();
        int streamIndex = config.getInt("general", "applicantStreamIndex");
        rngs.selectStream(streamIndex);
        this.haAnzianitaDiLavoro = rngs.random() < 0.889;
        this.isRapportoRataRedditoOk = rngs.random() < 0.75;
        this.haRichiesteORifiutiRecenti = rngs.random() < 0.1;
        this.haContrattoIndeterminato = rngs.random() < 0.84989;
        this.haCorrispondenzaInBancaDati = rngs.random() < 0.82;
    }

    public boolean hasValidaData(){
        return haAnzianitaDiLavoro &&
                isRapportoRataRedditoOk &&
                haContrattoIndeterminato &&
                !haRichiesteORifiutiRecenti;
    }

    public Applicant copy(Rngs rngs) {
        ConfigurationManager config = new ConfigurationManager();
        int streamIndex = config.getInt("general", "applicantStreamIndex");
        rngs.selectStream(streamIndex);
        return new Applicant(haAnzianitaDiLavoro,
                isRapportoRataRedditoOk,
                haContrattoIndeterminato,
                haRichiesteORifiutiRecenti,
                rngs.random() < 0.82);
    }

    public boolean hasCorrespondingData(){
        return haCorrispondenzaInBancaDati;
    }

    public static void main(String[] args) {
        System.out.println(testProbabilityCC(4096*128));
    }

    public static double testHasValidData(double n) {
        Rngs rngs = new Rngs();
        ConfigurationManager config = new ConfigurationManager();
        rngs.selectStream(config.getInt("general", "applicantStreamIndex"));
        double s = 0.0;
        for (int i = 0; i < n; i++) {
            Applicant applicant = new Applicant(rngs);
            if(applicant.hasValidaData()){
                s += 1;
            }
        }
        return s / n;
    }

    public static double testHasCorrespondingData(double n) {
        Rngs rngs = new Rngs();
        ConfigurationManager config = new ConfigurationManager();
        rngs.selectStream(config.getInt("general", "applicantStreamIndex"));
        double s = 0.0;
        for (int i = 0; i < n; i++) {
            Applicant applicant = new Applicant(rngs);
            if (applicant.hasValidaData() && applicant.hasCorrespondingData()) {
                s += 1;
            }
        }
        return s / n;
    }

    public static double testProbabilityCC(double n) {
        Rngs rngs = new Rngs();
        ConfigurationManager config = new ConfigurationManager();
        rngs.selectStream(config.getInt("general", "applicantStreamIndex"));
        double s = 0.0;
        double m = 0.0;
        for (int i = 0; i < n; i++) {
            Applicant applicant = new Applicant(rngs);
            if(applicant.hasValidaData()) {
                m += 1;
                if (applicant.hasCorrespondingData()) {
                    s += 1;
                }
            }
        }
        return s / m;
    }
}
