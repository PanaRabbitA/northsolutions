package org.example.proyecto_baseee.controller;

import org.example.proyecto_baseee.model.InventoryTransaction;
import org.example.proyecto_baseee.model.Product;
import org.example.proyecto_baseee.service.WarehouseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/warehouse")
@CrossOrigin(origins = "*")
public class WarehouseController {

    private final WarehouseService warehouseService;

    public WarehouseController(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }

    @GetMapping("/products")
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(warehouseService.getAllProducts());
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(warehouseService.getProductById(id));
    }

    @GetMapping("/products/sku/{sku}")
    public ResponseEntity<Product> getProductBySku(@PathVariable String sku) {
        return ResponseEntity.ok(warehouseService.getProductBySku(sku));
    }

    @PostMapping("/products")
    public ResponseEntity<Product> addProduct(@RequestBody Product product) {
        return ResponseEntity.ok(warehouseService.addProduct(product));
    }

    @PostMapping("/inventory/in")
    public ResponseEntity<InventoryTransaction> registerEntry(@RequestBody Map<String, Object> payload) {
        Long productId = Long.valueOf(payload.get("productId").toString());
        Integer quantity = Integer.valueOf(payload.get("quantity").toString());
        String reason = payload.getOrDefault("reason", "Stock Entry").toString();
        String createdBy = payload.getOrDefault("createdBy", "System").toString();
        return ResponseEntity.ok(warehouseService.registerEntry(productId, quantity, reason, createdBy));
    }

    @PostMapping("/inventory/out")
    public ResponseEntity<InventoryTransaction> registerExit(@RequestBody Map<String, Object> payload) {
        Long productId = Long.valueOf(payload.get("productId").toString());
        Integer quantity = Integer.valueOf(payload.get("quantity").toString());
        String reason = payload.getOrDefault("reason", "Stock Exit").toString();
        String createdBy = payload.getOrDefault("createdBy", "System").toString();
        return ResponseEntity.ok(warehouseService.registerExit(productId, quantity, reason, createdBy));
    }

    @GetMapping("/products/{id}/transactions")
    public ResponseEntity<List<InventoryTransaction>> getTransactions(@PathVariable Long id) {
        return ResponseEntity.ok(warehouseService.getTransactionsForProduct(id));
    }

    @GetMapping("/inventory/transactions")
    public ResponseEntity<List<InventoryTransaction>> getAllTransactions() {
        return ResponseEntity.ok(warehouseService.getAllTransactions());
    }
}
