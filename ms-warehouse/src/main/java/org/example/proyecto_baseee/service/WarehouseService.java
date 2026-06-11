package org.example.proyecto_baseee.service;

import org.example.proyecto_baseee.model.InventoryTransaction;
import org.example.proyecto_baseee.model.Product;
import org.example.proyecto_baseee.repository.InventoryTransactionRepository;
import org.example.proyecto_baseee.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class WarehouseService {

    private final ProductRepository productRepository;
    private final InventoryTransactionRepository transactionRepository;

    public WarehouseService(ProductRepository productRepository, InventoryTransactionRepository transactionRepository) {
        this.productRepository = productRepository;
        this.transactionRepository = transactionRepository;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
    }

    public Product getProductBySku(String sku) {
        return productRepository.findBySku(sku).orElseThrow(() -> new RuntimeException("Product not found"));
    }

    @Transactional
    public Product addProduct(Product product) {
        if (product.getStock() == null) {
            product.setStock(0);
        }
        return productRepository.save(product);
    }

    @Transactional
    public InventoryTransaction registerEntry(Long productId, Integer quantity, String reason, String createdBy) {
        Product product = getProductById(productId);
        product.setStock(product.getStock() + quantity);
        productRepository.save(product);

        InventoryTransaction transaction = new InventoryTransaction(product, "IN", quantity, LocalDateTime.now(), reason, createdBy);
        return transactionRepository.save(transaction);
    }

    @Transactional
    public InventoryTransaction registerExit(Long productId, Integer quantity, String reason, String createdBy) {
        Product product = getProductById(productId);
        if (product.getStock() < quantity) {
            throw new RuntimeException("Insufficient stock for product: " + product.getSku());
        }
        product.setStock(product.getStock() - quantity);
        productRepository.save(product);

        InventoryTransaction transaction = new InventoryTransaction(product, "OUT", quantity, LocalDateTime.now(), reason, createdBy);
        return transactionRepository.save(transaction);
    }

    public List<InventoryTransaction> getTransactionsForProduct(Long productId) {
        return transactionRepository.findByProductIdOrderByTransactionDateDesc(productId);
    }

    public List<InventoryTransaction> getAllTransactions() {
        return transactionRepository.findAll();
    }
}
