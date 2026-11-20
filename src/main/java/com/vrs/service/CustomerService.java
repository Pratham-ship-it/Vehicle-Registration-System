package com.vrs.service;

import com.vrs.entity.Customer;
import com.vrs.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public Customer getCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found with ID: " + id));
    }

    @Transactional
    public Customer createCustomer(Customer customer) {
        if (customer.getName() == null || customer.getName().isBlank()) {
            throw new IllegalArgumentException("Customer name is required.");
        }
        if (customer.getContactNumber() == null || customer.getContactNumber().isBlank()) {
            throw new IllegalArgumentException("Contact number is required.");
        }
        return customerRepository.save(customer);
    }

    @Transactional
    public void deleteCustomer(Long id) {
        if (!customerRepository.existsById(id)) {
            throw new IllegalArgumentException("Customer not found with ID: " + id);
        }
        customerRepository.deleteById(id);
    }
}
