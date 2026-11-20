package com.vrs.repository;

import com.vrs.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByNameIgnoreCaseAndContactNumber(String name, String contactNumber);
}
