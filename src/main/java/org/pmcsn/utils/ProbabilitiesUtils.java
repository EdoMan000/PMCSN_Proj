package org.pmcsn.utils;

import org.pmcsn.configuration.ConfigurationManager;
import org.pmcsn.libraries.Rngs;

public class ProbabilitiesUtils {

    public static boolean isAcceptedSysScoring(Rngs rngs, int streamIndex){
        ConfigurationManager conf = new ConfigurationManager();
        return generateProbability(conf.getDouble("sysScoringAutomaticoSANTANDER", "pAccept"), rngs, streamIndex);
    }

    public static boolean isAccepetdComitato(Rngs rngs, int streamIndex) {
        ConfigurationManager conf = new ConfigurationManager();
        return generateProbability(conf.getDouble("comitatoCreditoSANTANDER", "pAccept"), rngs, streamIndex);
    }

    public static boolean isFeedback(Rngs rngs, int streamIndex){
        ConfigurationManager conf = new ConfigurationManager();
        return generateProbability(conf.getDouble("comitatoCreditoSANTANDER", "pFeedback"), rngs, streamIndex);
    }

    public static int getRandomValueUpToMax(Rngs rngs, int streamIndex, int maxValue) {
        double prob = 1.0 / maxValue;
        rngs.selectStream(streamIndex);
        double random = rngs.random();

        // Mappa il valore casuale a un numero intero tra 1 e maxValue
        for (int i = 1; i <= maxValue; i++) {
            if (random < i * prob) {
                return i;
            }
        }

        // Questo punto non dovrebbe mai essere raggiunto, ma è qui come fallback
        return maxValue;
    }

    private static boolean generateProbability(double beta, Rngs rngs, int streamIndex) {
        rngs.selectStream(streamIndex);
        return rngs.random() < beta;
    }
}
