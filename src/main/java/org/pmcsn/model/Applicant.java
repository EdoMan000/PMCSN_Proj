package org.pmcsn.model;

import org.pmcsn.libraries.Rngs;

import java.util.Random;

public class Applicant {
    /*
    boolean isLavoroOk;
    boolean isRapportoRataRedditoOk;
    boolean haEsperienzeDiCreditoPregresse;
    boolean haRifiutiRecenti;
    boolean haCorrispondenzaInBancaDati;
     */
    public boolean haDatiFornitiValidi;

    public boolean haCorrispondenzaInBancaDati;


    public Applicant() {
        Random rand = new Random();
        this.haDatiFornitiValidi = rand.nextBoolean();
        this.haCorrispondenzaInBancaDati = rand.nextBoolean();
    }

}
