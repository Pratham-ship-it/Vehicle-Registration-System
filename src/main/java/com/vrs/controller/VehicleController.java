package com.vrs.controller;

import com.vrs.dto.VehicleRegistrationRequest;
import com.vrs.entity.Customer;
import com.vrs.entity.Vehicle;
import com.vrs.service.VehicleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vehicles")
@CrossOrigin(origins = "*")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @PostMapping
    public ResponseEntity<?> registerVehicle(@RequestBody VehicleRegistrationRequest request) {
        try {
            Vehicle vehicle = new Vehicle();
            vehicle.setRegistrationNumber(request.getRegistrationNumber());
            vehicle.setChassisNumber(request.getChassisNumber());
            vehicle.setMake(request.getMake());
            vehicle.setModel(request.getModel());
            vehicle.setManufactureYear(request.getManufactureYear());
            vehicle.setVehicleType(request.getVehicleType());
            vehicle.setColor(request.getColor());
            vehicle.setEngineNumber(request.getEngineNumber());

            Customer newCustomer = null;
            if (request.getCustomerId() == null) {
                newCustomer = new Customer();
                newCustomer.setName(request.getCustomerName());
                newCustomer.setContactNumber(request.getCustomerContact());
                newCustomer.setEmail(request.getCustomerEmail());
                newCustomer.setAddress(request.getCustomerAddress());
            }

            Vehicle saved = vehicleService.registerVehicle(vehicle, request.getCustomerId(), newCustomer);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public List<Vehicle> getAllVehicles() {
        return vehicleService.getAllVehicles();
    }

    @GetMapping("/search")
    public List<Vehicle> searchVehicles(@RequestParam(required = false, defaultValue = "") String keyword) {
        return vehicleService.searchVehicles(keyword);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getVehicleById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(vehicleService.getVehicleById(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteVehicle(@PathVariable Long id) {
        try {
            vehicleService.deleteVehicle(id);
            return ResponseEntity.ok(Map.of("message", "Vehicle deleted successfully."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }
}
