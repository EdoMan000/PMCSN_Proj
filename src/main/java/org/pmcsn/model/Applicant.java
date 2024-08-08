package org.pmcsn.model;

import org.pmcsn.libraries.Rngs;

import java.util.Random;

class Applicant {
    /*
    boolean isLavoroOk;
    boolean isRapportoRataRedditoOk;
    boolean haEsperienzeDiCreditoPregresse;
    boolean haRifiutiRecenti;
    boolean haCorrispondenzaInBancaDati;
     */
    boolean haDatiFornitiValidi;

    boolean haCorrispondenzaInBancaDati;


    public Applicant(boolean haDatiFornitiValidi, boolean haCorrispondenzaInBancaDati) {
        Random rand = new Random();
        this.haDatiFornitiValidi = rand.nextBoolean();
        this.haCorrispondenzaInBancaDati = rand.nextBoolean();
    }

}
