package org.pmcsn.configuration;

import org.pmcsn.centers.*;

public class CenterFactory {
    private final ConfigurationManager configurationManager = new ConfigurationManager();
    private static int batchSize;
    private static int numBatches;

    public CenterFactory(boolean isImprovedSimulation) {
        ConfigurationManager config = new ConfigurationManager();
        if (isImprovedSimulation) {
            batchSize = config.getInt("general", "batchSizeImproved");
            numBatches = config.getInt("general", "numBatchesImproved");
        }else{
            batchSize = config.getInt("general", "batchSize");
            numBatches = config.getInt("general", "numBatches");
        }
    }


    public RepartoIstruttorie_MAACFinance createRepartoIstruttorie(boolean approximateServiceAsExponential, boolean isBatch) {
        return new RepartoIstruttorie_MAACFinance(
                configurationManager.getString("repartoIstruttorieMAAC", "centerName"),
                configurationManager.getDouble("repartoIstruttorieMAAC", "meanServiceTime"),
                configurationManager.getInt("repartoIstruttorieMAAC", "serversNumber"),
                configurationManager.getInt("repartoIstruttorieMAAC", "streamIndex"),
                approximateServiceAsExponential, isBatch, batchSize, numBatches);
    }

    public RepartoIstruttorie_MAACFinance createRepartoIstruttorieImproved(boolean approximateServiceAsExponential, boolean isBatch) {
        return new RepartoIstruttorie_MAACFinance(
                configurationManager.getString("repartoIstruttorieMAAC", "centerName"),
                configurationManager.getDouble("repartoIstruttorieMAAC", "meanServiceTimeImproved"),
                configurationManager.getInt("repartoIstruttorieMAAC", "serversNumberImproved"),
                configurationManager.getInt("repartoIstruttorieMAAC", "streamIndex"),
                approximateServiceAsExponential, isBatch, batchSize, numBatches);
    }

    public PreScoring_MAACFinance createPreScoring(boolean approximateServiceAsExponential, boolean isBatch) {
        return new PreScoring_MAACFinance(
                configurationManager.getString("preScoringMAAC", "centerName"),
                configurationManager.getDouble("preScoringMAAC", "meanServiceTime"),
                configurationManager.getInt("preScoringMAAC", "serversNumber"),
                configurationManager.getInt("preScoringMAAC", "streamIndex"),
                approximateServiceAsExponential, isBatch, batchSize, numBatches);
    }

    public SysScoringAutomatico_SANTANDER createSysScoringAutomatico(boolean isImprovedSimulation, boolean approximateServiceAsExponential, boolean isBatch) {
        return new SysScoringAutomatico_SANTANDER(
                configurationManager.getString("sysScoringAutomaticoSANTANDER", "centerName"),
                configurationManager.getDouble("sysScoringAutomaticoSANTANDER", "meanServiceTime"),
                configurationManager.getInt("sysScoringAutomaticoSANTANDER", "streamIndex"),
                approximateServiceAsExponential, isBatch, isImprovedSimulation, batchSize, numBatches);
    }

    public ComitatoCredito_SANTANDER createComitatoCredito(boolean approximateServiceAsExponential, boolean isBatch) {
        return new ComitatoCredito_SANTANDER(
                configurationManager.getString("comitatoCreditoSANTANDER", "centerName"),
                configurationManager.getDouble("comitatoCreditoSANTANDER", "meanServiceTime"),
                configurationManager.getInt("comitatoCreditoSANTANDER", "streamIndex"),
                approximateServiceAsExponential, isBatch, batchSize, numBatches);
    }

    public RepartoLiquidazioni_MAACFinance createRepartoLiquidazioni(boolean approximateServiceAsExponential,  boolean isDigitalSignature, boolean isBatch) {

        if(isDigitalSignature){
            return new RepartoLiquidazioni_MAACFinance(
                    configurationManager.getString("repartoLiquidazioniMAAC", "centerName"),
                    configurationManager.getDouble("repartoLiquidazioniMAAC", "meanServiceTimeWithDigitalSignature"),
                    configurationManager.getInt("repartoLiquidazioniMAAC", "streamIndex"),
                    approximateServiceAsExponential, isBatch, batchSize, numBatches);
        } else {
            return new RepartoLiquidazioni_MAACFinance(
                    configurationManager.getString("repartoLiquidazioniMAAC", "centerName"),
                    configurationManager.getDouble("repartoLiquidazioniMAAC", "meanServiceTime"),
                    configurationManager.getInt("repartoLiquidazioniMAAC", "streamIndex"),
                    approximateServiceAsExponential, isBatch, batchSize, numBatches);
        }
    }
}
