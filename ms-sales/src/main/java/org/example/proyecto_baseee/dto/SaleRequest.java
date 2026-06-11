package org.example.proyecto_baseee.dto;

import java.util.List;

public class SaleRequest {
    private List<SaleItemRequest> items;
    private String createdBy;
    
    public List<SaleItemRequest> getItems() { return items; }
    public void setItems(List<SaleItemRequest> items) { this.items = items; }
    
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
}
