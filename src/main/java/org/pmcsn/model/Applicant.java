package org.pmcsn.model;

import org.pmcsn.libraries.Rngs;

import java.util.Random;

public class Applicant {

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
        this.haRichiesteORifiutiRecenti = rngs.random() < 0.1;
        this.haContrattoIndeterminato = rngs.random() < 0.85;
        this.haAnzianitaDiLavoro = rngs.random() < 0.889;
        this.isRapportoRataRedditoOk = rngs.random() < 0.75;
        this.haCorrispondenzaInBancaDati = rngs.random() < 0.82;
    }
    
    public Applicant() {
        // RIFIUTO O RICHIESTE RECENTE -> preventivi su internet che vengono rifiutati o sono comunque sulla banca dati CRIEF
        // e quindi risulta una richiesta con rifiuto o richieste multiple con una somma totale dei vari contratti
        // troppo elevata e quindi non si può pensare a un altro contratto

        // TIPO DI CONTRATTO -> spesso le persone credono di essere in regola ma non gli vengono versati i contributi (rip sistema italiano dei contratti)
        // spesso quindi si presentano con contratti che danno poche garanzie

        // ANZIANITA DI LAVORO -> accade che è il primo lavoro o la frase solita è "mi hanno messo in regola da poco"

        // RAPPORTO RATA/REDDITO -> è la cosa che dà meno problemi perché è più raro che se vengo a chiedere un prestito so già che posso pagare una rata

        Random rand = new Random();
        this.haRichiesteORifiutiRecenti = rand.nextDouble() < 0.1;
        this.haContrattoIndeterminato = rand.nextDouble() < 0.85;
        this.haAnzianitaDiLavoro = rand.nextDouble() < 0.889;
        this.isRapportoRataRedditoOk = rand.nextDouble() < 0.75;

        this.haCorrispondenzaInBancaDati = rand.nextDouble() < 0.82;
    }

    public boolean hasValidaData(){
        return haAnzianitaDiLavoro &&
                isRapportoRataRedditoOk &&
                haContrattoIndeterminato &&
                !haRichiesteORifiutiRecenti;
    }

    public boolean hasCorrespondingData(){
        return haCorrispondenzaInBancaDati;
    }

}
