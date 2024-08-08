package org.pmcsn.configuration;

import org.pmcsn.centers.*;

public class CenterFactory {
    private final ConfigurationManager configurationManager = new ConfigurationManager();

    public CenterFactory() {}


    public RepartoIstruttorie_MAACFinance createRepartoIstruttorie(boolean approximateServiceAsExponential) {
        return new RepartoIstruttorie_MAACFinance(
                configurationManager.getString("repartoIstruttorieMAAC", "centerName"),
                configurationManager.getDouble("repartoIstruttorieMAAC", "meanServiceTime"),
                configurationManager.getInt("repartoIstruttorieMAAC", "serversNumber"),
                configurationManager.getInt("repartoIstruttorieMAAC", "streamIndex"),
                approximateServiceAsExponential);
    }

    public SysScoringAutomatico_SANTANDER createSysScoringAutomatico(boolean approximateServiceAsExponential) {
        return new SysScoringAutomatico_SANTANDER(
                configurationManager.getString("sysScoringAutomaticoSANTANDER", "centerName"),
                configurationManager.getDouble("sysScoringAutomaticoSANTANDER", "meanServiceTime"),
                configurationManager.getInt("sysScoringAutomaticoSANTANDER", "streamIndex"),
                approximateServiceAsExponential);
    }

    public ComitatoCredito_SANTANDER createComitatoCredito(boolean approximateServiceAsExponential) {
        return new ComitatoCredito_SANTANDER(
                configurationManager.getString("comitatoCreditoSANTANDER", "centerName"),
                configurationManager.getDouble("comitatoCreditoSANTANDER", "meanServiceTime"),
                configurationManager.getInt("comitatoCreditoSANTANDER", "streamIndex"),
                approximateServiceAsExponential);
    }

    public RepartoLiquidazioni_MAACFinance createRepartoLiquidazioni(boolean approximateServiceAsExponential) {
        return new RepartoLiquidazioni_MAACFinance(
                configurationManager.getString("repartoLiquidazioniMAAC", "centerName"),
                configurationManager.getDouble("repartoLiquidazioniMAAC", "meanServiceTime"),
                configurationManager.getInt("repartoLiquidazioniMAAC", "streamIndex"),
                approximateServiceAsExponential);
    }
}
