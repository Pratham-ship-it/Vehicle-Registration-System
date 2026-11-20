package com.vrs.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

@Entity
@Table(name = "spare_part_orders")
public class SparePartOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "sparePartOrders"})
    private Vehicle vehicle;

    @NotBlank(message = "Part name is required")
    @Column(nullable = false)
    private String partName;

    @Min(value = 1, message = "Quantity must be at least 1")
    @Column(nullable = false)
    private Integer quantity = 1;

    private Double unitPrice;

    private String status;

    private LocalDate orderDate;

    public SparePartOrder() {
    }

    @PrePersist
    protected void onCreate() {
        if (this.orderDate == null) {
            this.orderDate = LocalDate.now();
        }
        if (this.status == null || this.status.isBlank()) {
            this.status = "ORDERED";
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public String getPartName() {
        return partName;
    }

    public void setPartName(String partName) {
        this.partName = partName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }
}
