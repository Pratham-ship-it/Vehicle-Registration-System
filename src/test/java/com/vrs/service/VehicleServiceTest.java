package com.vrs.service;

import com.vrs.entity.Customer;
import com.vrs.entity.Vehicle;
import com.vrs.repository.CustomerRepository;
import com.vrs.repository.VehicleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private VehicleService vehicleService;

    @Test
    void registerVehicle_rejectsDuplicateRegistrationNumber() {
        Vehicle vehicle = new Vehicle();
        vehicle.setRegistrationNumber("PB10AB1234");
        vehicle.setChassisNumber("MA3ERLF1S00123456");
        vehicle.setMake("Honda");
        vehicle.setModel("City");

        when(vehicleRepository.existsByRegistrationNumberIgnoreCase("PB10AB1234")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> vehicleService.registerVehicle(vehicle, null, new Customer("Ravi Kumar", "9876543210", null, null)));

        assertTrue(ex.getMessage().contains("registration number 'PB10AB1234' is already registered"));
        verify(vehicleRepository, never()).save(any());
    }

    @Test
    void registerVehicle_rejectsDuplicateChassisNumber() {
        Vehicle vehicle = new Vehicle();
        vehicle.setRegistrationNumber("PB10AB9999");
        vehicle.setChassisNumber("MA3ERLF1S00123456");
        vehicle.setMake("Honda");
        vehicle.setModel("City");

        when(vehicleRepository.existsByRegistrationNumberIgnoreCase("PB10AB9999")).thenReturn(false);
        when(vehicleRepository.existsByChassisNumberIgnoreCase("MA3ERLF1S00123456")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> vehicleService.registerVehicle(vehicle, null, new Customer("Ravi Kumar", "9876543210", null, null)));

        assertTrue(ex.getMessage().contains("chassis number 'MA3ERLF1S00123456' is already registered"));
        verify(vehicleRepository, never()).save(any());
    }

    @Test
    void registerVehicle_savesUniqueVehicleWithNewCustomer() {
        Vehicle vehicle = new Vehicle();
        vehicle.setRegistrationNumber("pb10ab1234");
        vehicle.setChassisNumber("ma3erlf1s00123456");
        vehicle.setMake("Honda");
        vehicle.setModel("City");

        Customer customer = new Customer("Ravi Kumar", "9876543210", null, null);
        customer.setId(1L);

        when(vehicleRepository.existsByRegistrationNumberIgnoreCase("PB10AB1234")).thenReturn(false);
        when(vehicleRepository.existsByChassisNumberIgnoreCase("MA3ERLF1S00123456")).thenReturn(false);
        when(customerRepository.findByNameIgnoreCaseAndContactNumber("Ravi Kumar", "9876543210"))
                .thenReturn(Optional.empty());
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(inv -> inv.getArgument(0));

        Vehicle saved = vehicleService.registerVehicle(vehicle, null, customer);

        assertEquals("PB10AB1234", saved.getRegistrationNumber());
        assertEquals("MA3ERLF1S00123456", saved.getChassisNumber());
        assertNotNull(saved.getRegistrationDate());
        assertEquals("Ravi Kumar", saved.getCustomer().getName());
    }
}
