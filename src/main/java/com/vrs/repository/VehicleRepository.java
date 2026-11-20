package com.vrs.repository;

import com.vrs.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    boolean existsByRegistrationNumberIgnoreCase(String registrationNumber);

    boolean existsByChassisNumberIgnoreCase(String chassisNumber);

    List<Vehicle> findByMakeContainingIgnoreCaseOrModelContainingIgnoreCaseOrRegistrationNumberContainingIgnoreCase(
            String make, String model, String registrationNumber);

    List<Vehicle> findAllByOrderByIdDesc();
}
