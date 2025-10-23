package mx.unam.ciencias.myp.pumabank;

import mx.unam.ciencias.myp.pumabank.facade.PumaBankFacade;
import java.util.Arrays;
import java.util.Map;

/**
 * Clase principal que demuestra el funcionamiento completo del sistema PumaBank
 * con todos los patrones de diseño implementados.
 * 
 * <p>Esta simulación incluye:
 * - Registro de clientes
 * - Creación de cuentas con diferentes servicios
 * - Operaciones bancarias (depósitos, retiros)
 * - Procesamiento mensual completo
 * - Consulta de portafolios
 * - Generación de reporte detallado
 * </p>
 */
public class Main {
    
    public static void main(String[] args) {
        System.out.println("=== INICIANDO SISTEMA PUMA BANK CON LOGGING COMPLETO ===\n");
        
        PumaBankFacade pumaBank = new PumaBankFacade();
        
        try {
            System.out.println("1. REGISTRANDO CLIENTES");
            pumaBank.registerClient("Juan Pérez", "CL001");
            pumaBank.registerClient("María García", "CL002"); 
            pumaBank.registerClient("Carlos López", "CL003");
            System.out.println();
            
            System.out.println("2. CREANDO CUENTAS CON DIFERENTES SERVICIOS");
            
            pumaBank.createAccount(
                "CL001", 
                5000.0, 
                "1234", 
                "MONTHLY", 
                null
            );

            pumaBank.createAccount(
                "CL002",
                15000.0,
                "5678", 
                "ANNUAL",
                Arrays.asList("ANTI_FRAUD", "PREMIUM_ALERTS", "REWARDS")
            );

            pumaBank.createAccount(
                "CL003",
                100000.0,
                "9999",
                "PREMIUM", 
                Arrays.asList("PREMIUM_ALERTS")
            );
            
            System.out.println();
            

            System.out.println("3. SIMULANDO OPERACIONES DEL MES");
            

            pumaBank.deposit("CL001-ACC-1", 1000.0, "1234");
            pumaBank.withdraw("CL001-ACC-1", 500.0, "1234");
            pumaBank.checkBalance("CL001-ACC-1", "1234");
            

            pumaBank.deposit("CL002-ACC-1", 15000.0, "5678");
            

            pumaBank.withdraw("CL001-ACC-1", 6000.0, "1234");
            

            pumaBank.deposit("CL003-ACC-1", 5000.0, "9999");
            pumaBank.withdraw("CL003-ACC-1", 2000.0, "9999");
            

            try {
                pumaBank.withdraw("CL001-ACC-1", 100.0, "0000");
            } catch (Exception e) {
                System.out.println("[ACCESS DENIED] " + e.getMessage());
            }
            
            System.out.println();
            

            System.out.println("4. PROCESANDO FIN DE MES");
            pumaBank.processMonthlyOperations();
            System.out.println();
            

            System.out.println("5. CONSULTANDO PORTAFOLIOS");
            
            consultarPortafolio(pumaBank, "CL001");
            consultarPortafolio(pumaBank, "CL002"); 
            consultarPortafolio(pumaBank, "CL003");
            

            System.out.println("6. MÉTRICAS FINALES DEL SISTEMA");
            System.out.printf("Transacciones mensuales: %d%n", pumaBank.getMonthlyTransactions());
            System.out.printf("Total cargos aplicados: $%.2f%n", pumaBank.getTotalFeesCollected());
            System.out.printf("Total intereses pagados: $%.2f%n", pumaBank.getTotalInterestPaid());
            System.out.printf("Total cuentas en sistema: %d%n", pumaBank.getAllClients().size());
            
        } catch (Exception e) {
            System.err.println(" Error durante la simulación: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("\n=== SIMULACIÓN COMPLETADA ===");
        System.out.println("Revisa el archivo 'monthly_operations_log.txt' para el reporte detallado.");
        System.out.println("El archivo incluye:");
        System.out.println("  Intereses aplicados");
        System.out.println("  Cargos por sobregiro"); 
        System.out.println("  Cargos por servicios (Anti-Fraud, Premium Alerts, Rewards)");
        System.out.println("  Cambios de estado detallados");
        System.out.println("  Todas las operaciones con balances antes/después");
    }
    
    /**
     * Consulta y muestra el portafolio de un cliente
     */
    private static void consultarPortafolio(PumaBankFacade pumaBank, String clientId) {
        try {
            Map<String, Object> portafolio = pumaBank.getClientPortfolio(clientId);
            
            String nombreCliente = ((mx.unam.ciencias.myp.pumabank.model.Client) 
                                  portafolio.get("client")).getName();
            int totalCuentas = (int) portafolio.get("totalAccounts");
            double saldoTotal = (double) portafolio.get("totalBalance");
            
            System.out.printf(" Portafolio de %s:%n", nombreCliente);
            System.out.printf(" Cuentas: %d | Saldo Total: $%.2f%n", totalCuentas, saldoTotal);
            
        } catch (Exception e) {
            System.err.println("Error consultando portafolio para " + clientId + ": " + e.getMessage());
        }
    }
}