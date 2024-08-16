package org.pmcsn.configuration;

import org.pmcsn.centers.*;

public class CenterFactory {
    private final ConfigurationManager configurationManager = new ConfigurationManager();

    public CenterFactory() {}


    public RepartoIstruttorie_MAACFinance createRepartoIstruttorie(boolean approximateServiceAsExponential, boolean isDigitalSignature,  boolean isBatch) {
        return new RepartoIstruttorie_MAACFinance(
                configurationManager.getString("repartoIstruttorieMAAC", "centerName"),
                configurationManager.getDouble("repartoIstruttorieMAAC", "meanServiceTime"),
                configurationManager.getInt("repartoIstruttorieMAAC", "serversNumber"),
                configurationManager.getInt("repartoIstruttorieMAAC", "streamIndex"),
                approximateServiceAsExponential, isDigitalSignature, isBatch);
    }

    public RepartoIstruttorie_MAACFinance createRepartoIstruttorieImproved(boolean approximateServiceAsExponential, boolean isDigitalSignature,  boolean isBatch) {
        return new RepartoIstruttorie_MAACFinance(
                configurationManager.getString("repartoIstruttorieMAAC", "centerName"),
                configurationManager.getDouble("repartoIstruttorieMAAC", "meanServiceTimeImproved"),
                configurationManager.getInt("repartoIstruttorieMAAC", "serversNumberImproved"),
                configurationManager.getInt("repartoIstruttorieMAAC", "streamIndex"),
                approximateServiceAsExponential, isDigitalSignature, isBatch);
    }

    public PreScoring_MAACFinance createPreScoring(boolean approximateServiceAsExponential, boolean isDigitalSignature,  boolean isBatch) {
        return new PreScoring_MAACFinance(
                configurationManager.getString("preScoringMAAC", "centerName"),
                configurationManager.getDouble("preScoringMAAC", "meanServiceTime"),
                configurationManager.getInt("preScoringMAAC", "serversNumber"),
                configurationManager.getInt("preScoringMAAC", "streamIndex"),
                approximateServiceAsExponential, isDigitalSignature, isBatch);
    }

    public SysScoringAutomatico_SANTANDER createSysScoringAutomatico(boolean isImprovedSimulation, boolean approximateServiceAsExponential, boolean isDigitalSignature, boolean isBatch) {
        return new SysScoringAutomatico_SANTANDER(
                configurationManager.getString("sysScoringAutomaticoSANTANDER", "centerName"),
                configurationManager.getDouble("sysScoringAutomaticoSANTANDER", "meanServiceTime"),
                configurationManager.getInt("sysScoringAutomaticoSANTANDER", "streamIndex"),
                approximateServiceAsExponential, isDigitalSignature, isBatch, isImprovedSimulation);
    }

    public ComitatoCredito_SANTANDER createComitatoCredito(boolean approximateServiceAsExponential,  boolean isDigitalSignature, boolean isBatch) {
        return new ComitatoCredito_SANTANDER(
                configurationManager.getString("comitatoCreditoSANTANDER", "centerName"),
                configurationManager.getDouble("comitatoCreditoSANTANDER", "meanServiceTime"),
                configurationManager.getInt("comitatoCreditoSANTANDER", "streamIndex"),
                approximateServiceAsExponential, isDigitalSignature, isBatch);
    }

    public RepartoLiquidazioni_MAACFinance createRepartoLiquidazioni(boolean approximateServiceAsExponential,  boolean isDigitalSignature, boolean isBatch) {

        if(isDigitalSignature){
            return new RepartoLiquidazioni_MAACFinance(
                    configurationManager.getString("repartoLiquidazioniMAAC", "centerName"),
                    configurationManager.getDouble("repartoLiquidazioniMAAC", "meanServiceTimeWithDigitalSignature"),
                    configurationManager.getInt("repartoLiquidazioniMAAC", "streamIndex"),
                    approximateServiceAsExponential, isDigitalSignature, isBatch);
        } else {
            return new RepartoLiquidazioni_MAACFinance(
                    configurationManager.getString("repartoLiquidazioniMAAC", "centerName"),
                    configurationManager.getDouble("repartoLiquidazioniMAAC", "meanServiceTime"),
                    configurationManager.getInt("repartoLiquidazioniMAAC", "streamIndex"),
                    approximateServiceAsExponential, isDigitalSignature, isBatch);
        }
    }
}
