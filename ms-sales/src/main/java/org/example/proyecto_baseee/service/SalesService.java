package org.example.proyecto_baseee.service;

import org.example.proyecto_baseee.dto.SaleItemRequest;
import org.example.proyecto_baseee.dto.SaleRequest;
import org.example.proyecto_baseee.model.Sale;
import org.example.proyecto_baseee.model.SaleDetail;
import org.example.proyecto_baseee.repository.SaleRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.example.proyecto_baseee.client.WarehouseClient;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SalesService {

    private final SaleRepository saleRepository;
    private final WarehouseClient warehouseClient;

    public SalesService(SaleRepository saleRepository, WarehouseClient warehouseClient) {
        this.saleRepository = saleRepository;
        this.warehouseClient = warehouseClient;
    }

    public List<Sale> getAllSales() {
        return saleRepository.findAll();
    }

    public Sale getSaleById(Long id) {
        return saleRepository.findById(id).orElseThrow(() -> new RuntimeException("Sale not found"));
    }

    @Transactional
    public Sale processSale(SaleRequest request) {
        Sale sale = new Sale();
        sale.setSaleDate(LocalDateTime.now());
        sale.setCreatedBy(request.getCreatedBy() != null ? request.getCreatedBy() : "System");
        
        double total = 0.0;
        boolean requiresApproval = false;
        
        for (SaleItemRequest item : request.getItems()) {
            // 1. Fetch product price from ms-warehouse
            ResponseEntity<Map> productResponse = warehouseClient.getProductById(item.getProductId());
            if (!productResponse.getStatusCode().is2xxSuccessful() || productResponse.getBody() == null) {
                throw new RuntimeException("Product not found: " + item.getProductId());
            }
            
            Double originalPrice = Double.valueOf(productResponse.getBody().get("price").toString());
            Double priceToUse = originalPrice;
            
            if (item.getUnitPrice() != null) {
                double diff = Math.abs(item.getUnitPrice() - originalPrice);
                if (diff > 0.01) {
                    priceToUse = item.getUnitPrice();
                    requiresApproval = true;
                }
            }
            
            Double subtotal = priceToUse * item.getQuantity();
            
            // 2. Request inventory out
            Map<String, Object> outPayload = new HashMap<>();
            outPayload.put("productId", item.getProductId());
            outPayload.put("quantity", item.getQuantity());
            outPayload.put("reason", "Venta generada en ms-sales");
            outPayload.put("createdBy", sale.getCreatedBy());
            
            try {
                warehouseClient.deductInventory(outPayload);
            } catch (Exception e) {
                throw new RuntimeException("Error deducting stock for product " + item.getProductId() + ": " + e.getMessage());
            }
            
            // 3. Build sale detail
            SaleDetail detail = new SaleDetail();
            detail.setProductId(item.getProductId());
            detail.setQuantity(item.getQuantity());
            detail.setUnitPrice(priceToUse);
            detail.setSubtotal(subtotal);
            
            sale.addDetail(detail);
            total += subtotal;
        }
        
        sale.setTotal(total);
        sale.setRequiresApproval(requiresApproval);
        return saleRepository.save(sale);
    }
}
