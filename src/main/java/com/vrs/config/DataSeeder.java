package com.vrs.config;

import com.vrs.entity.Customer;
import com.vrs.entity.SparePartOrder;
import com.vrs.entity.Vehicle;
import com.vrs.repository.CustomerRepository;
import com.vrs.repository.SparePartOrderRepository;
import com.vrs.repository.VehicleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * DataSeeder — runs once at application startup.
 * Populates realistic demo data only when the database is completely empty,
 * ensuring it never duplicates records on subsequent restarts or cloud redeploys.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final CustomerRepository customerRepository;
    private final VehicleRepository vehicleRepository;
    private final SparePartOrderRepository orderRepository;

    public DataSeeder(CustomerRepository customerRepository,
                      VehicleRepository vehicleRepository,
                      SparePartOrderRepository orderRepository) {
        this.customerRepository = customerRepository;
        this.vehicleRepository = vehicleRepository;
        this.orderRepository = orderRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {

        // Guard: only seed when the database is completely empty
        if (customerRepository.count() != 0 || vehicleRepository.count() != 0) {
            log.info("DataSeeder: existing data found — skipping seed.");
            return;
        }

        log.info("DataSeeder: empty database detected — seeding demo data...");

        // ── Customers ─────────────────────────────────────────────────────────────

        // Customer 1: Aarav Sharma (Punjab) — will own 2 vehicles
        Customer aarav = customerRepository.save(new Customer(
                "Aarav Sharma",
                "9876543210",
                "aarav.sharma@email.com",
                "42, Civil Lines, Ludhiana, Punjab"
        ));

        // Customer 2: Priya Verma (Chandigarh)
        Customer priya = customerRepository.save(new Customer(
                "Priya Verma",
                "9988776655",
                "priya.verma@email.com",
                "12, Sector 17, Chandigarh"
        ));

        // Customer 3: Vikramjit Singh (Haryana)
        Customer vikram = customerRepository.save(new Customer(
                "Vikramjit Singh",
                "9123456789",
                "vikram.singh@email.com",
                "88, Model Town, Gurugram, Haryana"
        ));

        // ── Vehicles ──────────────────────────────────────────────────────────────

        // Vehicle 1 — Aarav's Maruti Swift (Punjab plate)
        Vehicle swift = new Vehicle();
        swift.setRegistrationNumber("PB10AB1234");
        swift.setChassisNumber("MA3ERLF1S00112345");
        swift.setMake("Maruti Suzuki");
        swift.setModel("Swift");
        swift.setManufactureYear(2021);
        swift.setVehicleType("Hatchback");
        swift.setColor("Pearl White");
        swift.setEngineNumber("K12MN112345");
        swift.setRegistrationDate(LocalDate.of(2021, 6, 15));
        swift.setCustomer(aarav);
        vehicleRepository.save(swift);

        // Vehicle 2 — Aarav's Honda Activa (Punjab plate) — demonstrates 1:N
        Vehicle activa = new Vehicle();
        activa.setRegistrationNumber("PB65GH3456");
        activa.setChassisNumber("MA1YD2S1XNM654321");
        activa.setMake("Honda");
        activa.setModel("Activa 6G");
        activa.setManufactureYear(2023);
        activa.setVehicleType("Two-Wheeler");
        activa.setColor("Matte Black");
        activa.setEngineNumber("JF50EW654321");
        activa.setRegistrationDate(LocalDate.of(2023, 3, 20));
        activa.setCustomer(aarav);
        vehicleRepository.save(activa);

        // Vehicle 3 — Priya's Hyundai Creta (Chandigarh plate)
        Vehicle creta = new Vehicle();
        creta.setRegistrationNumber("CH01CD5678");
        creta.setChassisNumber("MAKDE6S1X0M223344");
        creta.setMake("Hyundai");
        creta.setModel("Creta");
        creta.setManufactureYear(2022);
        creta.setVehicleType("SUV");
        creta.setColor("Titan Grey");
        creta.setEngineNumber("G4FCKU223344");
        creta.setRegistrationDate(LocalDate.of(2022, 9, 5));
        creta.setCustomer(priya);
        vehicleRepository.save(creta);

        // Vehicle 4 — Vikram's Tata Nexon EV (Haryana plate)
        Vehicle nexon = new Vehicle();
        nexon.setRegistrationNumber("HR26EF9012");
        nexon.setChassisNumber("MBLFA8BB0G8556677");
        nexon.setMake("Tata Motors");
        nexon.setModel("Nexon EV");
        nexon.setManufactureYear(2023);
        nexon.setVehicleType("SUV (Electric)");
        nexon.setColor("Intensi-Teal");
        nexon.setEngineNumber("RNNXEV556677");
        nexon.setRegistrationDate(LocalDate.of(2023, 11, 1));
        nexon.setCustomer(vikram);
        vehicleRepository.save(nexon);

        // ── Spare Part Orders ──────────────────────────────────────────────────────

        // Order 1: Brake Pad Set for Aarav's Swift
        SparePartOrder order1 = new SparePartOrder();
        order1.setVehicle(swift);
        order1.setPartName("Brake Pad Set");
        order1.setQuantity(1);
        order1.setUnitPrice(2450.00);
        order1.setStatus("ORDERED");
        order1.setOrderDate(LocalDate.now());
        orderRepository.save(order1);

        // Order 2: Engine Oil Filter for Priya's Creta
        SparePartOrder order2 = new SparePartOrder();
        order2.setVehicle(creta);
        order2.setPartName("Engine Oil Filter");
        order2.setQuantity(2);
        order2.setUnitPrice(875.50);
        order2.setStatus("ORDERED");
        order2.setOrderDate(LocalDate.now());
        orderRepository.save(order2);

        // Order 3: Clutch Assembly for Aarav's Activa
        SparePartOrder order3 = new SparePartOrder();
        order3.setVehicle(activa);
        order3.setPartName("Clutch Assembly");
        order3.setQuantity(1);
        order3.setUnitPrice(3200.00);
        order3.setStatus("SHIPPED");
        order3.setOrderDate(LocalDate.now().minusDays(2));
        orderRepository.save(order3);

        log.info("DataSeeder: seeded {} customers, {} vehicles, {} orders.",
                customerRepository.count(),
                vehicleRepository.count(),
                orderRepository.count());
    }
}
