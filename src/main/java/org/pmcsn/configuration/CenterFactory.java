package org.pmcsn.configuration;

import org.pmcsn.centers.*;

public class CenterFactory {
    private final ConfigurationManager configurationManager = new ConfigurationManager();
    private static int batchSize;
    private static int numBatches;
    private final boolean isImprovedSimulation;

    public CenterFactory(boolean isImprovedSimulation) {
        this.isImprovedSimulation = isImprovedSimulation;
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
        int serversNumber;
        double meanServiceTime;
        double sigma;
        double truncationPoint;
        if (isImprovedSimulation) {
            serversNumber = configurationManager.getInt("repartoIstruttorieMAAC", "serversNumberImproved");
            meanServiceTime = configurationManager.getDouble("repartoIstruttorieMAAC", "meanServiceTimeImproved");
            sigma = configurationManager.getDouble("repartoIstruttorieMAAC", "sigma");
            truncationPoint = configurationManager.getDouble("repartoIstruttorieMAAC", "truncationPointImproved");
        } else {
            serversNumber = configurationManager.getInt("repartoIstruttorieMAAC", "serversNumber");
            meanServiceTime = configurationManager.getDouble("repartoIstruttorieMAAC", "meanServiceTime");
            sigma = configurationManager.getDouble("repartoIstruttorieMAAC", "sigmaImproved");
            truncationPoint = configurationManager.getDouble("repartoIstruttorieMAAC", "truncationPoint");
        }
        return new RepartoIstruttorie_MAACFinance(
                configurationManager.getString("repartoIstruttorieMAAC", "centerName"),
                meanServiceTime,
                sigma,
                truncationPoint,
                serversNumber,
                configurationManager.getInt("repartoIstruttorieMAAC", "streamIndex"),
                approximateServiceAsExponential,
                isBatch,
                batchSize,
                numBatches);
    }

    public PreScoring_MAACFinance createPreScoring(boolean approximateServiceAsExponential, boolean isBatch) {
        return new PreScoring_MAACFinance(
                configurationManager.getString("preScoringMAAC", "centerName"),
                configurationManager.getDouble("preScoringMAAC", "meanServiceTime"),
                configurationManager.getDouble("preScoringMAAC", "sigma"),
                configurationManager.getDouble("preScoringMAAC", "truncationPoint"),
                configurationManager.getInt("preScoringMAAC", "serversNumber"),
                configurationManager.getInt("preScoringMAAC", "streamIndex"),
                approximateServiceAsExponential,
                isBatch,
                batchSize,
                numBatches);
    }

    public SysScoringAutomatico_SANTANDER createSysScoringAutomatico(boolean approximateServiceAsExponential, boolean isBatch) {
        return new SysScoringAutomatico_SANTANDER(
                configurationManager.getString("sysScoringAutomaticoSANTANDER", "centerName"),
                configurationManager.getDouble("sysScoringAutomaticoSANTANDER", "meanServiceTime"),
                configurationManager.getInt("sysScoringAutomaticoSANTANDER", "streamIndex"),
                approximateServiceAsExponential,
                isBatch,
                isImprovedSimulation,
                batchSize,
                numBatches);
    }

    public ComitatoCredito_SANTANDER createComitatoCredito(boolean approximateServiceAsExponential, boolean isBatch) {
        return new ComitatoCredito_SANTANDER(
                configurationManager.getString("comitatoCreditoSANTANDER", "centerName"),
                configurationManager.getDouble("comitatoCreditoSANTANDER", "meanServiceTime"),
                configurationManager.getDouble("comitatoCreditoSANTANDER", "sigma"),
                configurationManager.getDouble("comitatoCreditoSANTANDER", "truncationPoint"),
                configurationManager.getInt("comitatoCreditoSANTANDER", "streamIndex"),
                approximateServiceAsExponential,
                isBatch,
                batchSize,
                numBatches,
                isImprovedSimulation);
    }

    public RepartoLiquidazioni_MAACFinance createRepartoLiquidazioni(boolean approximateServiceAsExponential,  boolean isDigitalSignature, boolean isBatch) {
        double meanServiceTime;
        double sigma;
        double truncationPoint;
        if (isDigitalSignature) {
            meanServiceTime = configurationManager.getDouble("repartoLiquidazioniMAAC", "meanServiceTimeWithDigitalSignature");
            sigma = configurationManager.getDouble("repartoLiquidazioniMAAC", "sigmaWithDigitalSignature");
            truncationPoint = configurationManager.getDouble("repartoLiquidazioniMAAC", "truncationPointWithDigitalSignature");
        } else {
            meanServiceTime = configurationManager.getDouble("repartoLiquidazioniMAAC", "meanServiceTime");
            sigma = configurationManager.getDouble("repartoLiquidazioniMAAC", "sigma");
            truncationPoint = configurationManager.getDouble("repartoLiquidazioniMAAC", "truncationPoint");

        }
        return new RepartoLiquidazioni_MAACFinance(
                configurationManager.getString("repartoLiquidazioniMAAC", "centerName"),
                meanServiceTime,
                sigma,
                truncationPoint,
                configurationManager.getInt("repartoLiquidazioniMAAC", "streamIndex"),
                approximateServiceAsExponential,
                isBatch,
                batchSize,
                numBatches);
    }
}
