package org.example.proyecto_baseee.service;

import org.example.proyecto_baseee.client.WarehouseClient;
import org.example.proyecto_baseee.client.SalesClient;
import org.example.proyecto_baseee.client.HrClient;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    private final WarehouseClient warehouseClient;
    private final SalesClient salesClient;
    private final HrClient hrClient;

    public DashboardService(WarehouseClient warehouseClient, SalesClient salesClient, HrClient hrClient) {
        this.warehouseClient = warehouseClient;
        this.salesClient = salesClient;
        this.hrClient = hrClient;
    }

    public Map<String, Object> getKpis() {
        Map<String, Object> kpis = new HashMap<>();

        try {
            // 1. Warehouse KPIs
            List<Map<String, Object>> products = warehouseClient.getProducts();
            int totalProducts = 0;
            int lowStockProducts = 0;
            if (products != null) {
                totalProducts = products.size();
                for (Map<String, Object> p : products) {
                    if (p.get("stock") != null && Integer.parseInt(p.get("stock").toString()) < 10) {
                        lowStockProducts++;
                    }
                }
            }
            kpis.put("totalProducts", totalProducts);
            kpis.put("lowStockProducts", lowStockProducts);

            // 2. Sales KPIs
            List<Map<String, Object>> sales = salesClient.getSales();
            double totalRevenue = 0.0;
            int totalSales = 0;
            if (sales != null) {
                totalSales = sales.size();
                for (Map<String, Object> s : sales) {
                    if (s.get("total") != null) {
                        totalRevenue += Double.parseDouble(s.get("total").toString());
                    }
                }
            }
            kpis.put("totalSales", totalSales);
            kpis.put("totalRevenue", totalRevenue);

            // 3. HR KPIs
            List<Map<String, Object>> employees = hrClient.getEmployees();
            int totalEmployees = (employees != null) ? employees.size() : 0;
            kpis.put("totalEmployees", totalEmployees);

        } catch (Exception e) {
            kpis.put("error", "Error connecting to internal services: " + e.getMessage());
        }

        return kpis;
    }
}
