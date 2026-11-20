package com.vrs.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "vehicles", uniqueConstraints = {
    @UniqueConstraint(columnNames = "registrationNumber"),
    @UniqueConstraint(columnNames = "chassisNumber")
})
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Registration number is required")
    @Column(nullable = false, unique = true)
    private String registrationNumber;

    @NotBlank(message = "Chassis number is required")
    @Column(nullable = false, unique = true)
    private String chassisNumber;

    @NotBlank(message = "Make is required")
    private String make;

    @NotBlank(message = "Model is required")
    private String model;

    private Integer manufactureYear;

    private String vehicleType;

    private String color;

    private String engineNumber;

    private LocalDate registrationDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "vehicles"})
    private Customer customer;

    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<SparePartOrder> sparePartOrders = new ArrayList<>();

    public Vehicle() {
    }

    @PrePersist
    protected void onCreate() {
        if (this.registrationDate == null) {
            this.registrationDate = LocalDate.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public String getChassisNumber() {
        return chassisNumber;
    }

    public void setChassisNumber(String chassisNumber) {
        this.chassisNumber = chassisNumber;
    }

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Integer getManufactureYear() {
        return manufactureYear;
    }

    public void setManufactureYear(Integer manufactureYear) {
        this.manufactureYear = manufactureYear;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getEngineNumber() {
        return engineNumber;
    }

    public void setEngineNumber(String engineNumber) {
        this.engineNumber = engineNumber;
    }

    public LocalDate getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDate registrationDate) {
        this.registrationDate = registrationDate;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public List<SparePartOrder> getSparePartOrders() {
        return sparePartOrders;
    }

    public void setSparePartOrders(List<SparePartOrder> sparePartOrders) {
        this.sparePartOrders = sparePartOrders;
    }
}
