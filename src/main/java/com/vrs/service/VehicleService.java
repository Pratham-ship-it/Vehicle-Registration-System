package com.vrs.service;

import com.vrs.entity.Customer;
import com.vrs.entity.Vehicle;
import com.vrs.repository.CustomerRepository;
import com.vrs.repository.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final CustomerRepository customerRepository;

    public VehicleService(VehicleRepository vehicleRepository, CustomerRepository customerRepository) {
        this.vehicleRepository = vehicleRepository;
        this.customerRepository = customerRepository;
    }

    @Transactional
    public Vehicle registerVehicle(Vehicle vehicle, Long customerId, Customer newCustomerData) {
        if (vehicle.getRegistrationNumber() == null || vehicle.getRegistrationNumber().isBlank()) {
            throw new IllegalArgumentException("Registration number is required.");
        }
        if (vehicle.getChassisNumber() == null || vehicle.getChassisNumber().isBlank()) {
            throw new IllegalArgumentException("Chassis number is required.");
        }
        if (vehicle.getMake() == null || vehicle.getMake().isBlank()) {
            throw new IllegalArgumentException("Vehicle make is required.");
        }
        if (vehicle.getModel() == null || vehicle.getModel().isBlank()) {
            throw new IllegalArgumentException("Vehicle model is required.");
        }

        String regNo = vehicle.getRegistrationNumber().trim().toUpperCase();
        String chassisNo = vehicle.getChassisNumber().trim().toUpperCase();
        vehicle.setRegistrationNumber(regNo);
        vehicle.setChassisNumber(chassisNo);

        if (vehicle.getEngineNumber() != null && !vehicle.getEngineNumber().isBlank()) {
            vehicle.setEngineNumber(vehicle.getEngineNumber().trim().toUpperCase());
        }

        if (vehicleRepository.existsByRegistrationNumberIgnoreCase(regNo)) {
            throw new IllegalArgumentException(
                    "A vehicle with registration number '" + regNo + "' is already registered.");
        }

        if (vehicleRepository.existsByChassisNumberIgnoreCase(chassisNo)) {
            throw new IllegalArgumentException(
                    "A vehicle with chassis number '" + chassisNo + "' is already registered.");
        }

        Customer owner;
        if (customerId != null) {
            owner = customerRepository.findById(customerId)
                    .orElseThrow(() -> new IllegalArgumentException("Selected customer not found (ID: " + customerId + ")."));
        } else {
            if (newCustomerData == null
                    || newCustomerData.getName() == null || newCustomerData.getName().isBlank()
                    || newCustomerData.getContactNumber() == null || newCustomerData.getContactNumber().isBlank()) {
                throw new IllegalArgumentException("Customer full name and contact number are required.");
            }
            owner = customerRepository.findByNameIgnoreCaseAndContactNumber(
                            newCustomerData.getName().trim(),
                            newCustomerData.getContactNumber().trim())
                    .orElseGet(() -> {
                        Customer c = new Customer();
                        c.setName(newCustomerData.getName().trim());
                        c.setContactNumber(newCustomerData.getContactNumber().trim());
                        c.setEmail(newCustomerData.getEmail() != null ? newCustomerData.getEmail().trim() : null);
                        c.setAddress(newCustomerData.getAddress() != null ? newCustomerData.getAddress().trim() : null);
                        return customerRepository.save(c);
                    });
        }

        vehicle.setCustomer(owner);
        if (vehicle.getRegistrationDate() == null) {
            vehicle.setRegistrationDate(LocalDate.now());
        }

        return vehicleRepository.save(vehicle);
    }

    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAllByOrderByIdDesc();
    }

    public List<Vehicle> searchVehicles(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return getAllVehicles();
        }
        String term = keyword.trim();
        return vehicleRepository
                .findByMakeContainingIgnoreCaseOrModelContainingIgnoreCaseOrRegistrationNumberContainingIgnoreCase(
                        term, term, term);
    }

    public Vehicle getVehicleById(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found with ID: " + id));
    }

    @Transactional
    public void deleteVehicle(Long id) {
        if (!vehicleRepository.existsById(id)) {
            throw new IllegalArgumentException("Vehicle not found with ID: " + id);
        }
        vehicleRepository.deleteById(id);
    }
}
