package com.wassimlagnaoui.Ecommerce.Service;

import com.wassimlagnaoui.Ecommerce.Domain.Address;
import com.wassimlagnaoui.Ecommerce.Domain.Customer;
import com.wassimlagnaoui.Ecommerce.Domain.Order;
import com.wassimlagnaoui.Ecommerce.Repository.AddressRepository;
import com.wassimlagnaoui.Ecommerce.Repository.CustomerRepository;
import com.wassimlagnaoui.Ecommerce.Repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private OrderRepository orderRepository;

    // Basic CRUD operations
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public Optional<Customer> getCustomerById(Long id) {
        return customerRepository.findById(id);
    }

    public Customer saveCustomer(Customer customer) {
        return customerRepository.save(customer);
    }

    public void deleteCustomer(Long id) {
        customerRepository.deleteById(id);
    }

    // Custom business methods
    public Optional<Customer> findByEmail(String email) {
        return customerRepository.findByEmail(email);
    }

    public Optional<Customer> findByName(String name) {
        return customerRepository.findByName(name);
    }

    public boolean existsByEmail(String email) {
        return customerRepository.existsByEmail(email);
    }

    public List<Customer> getHighValueCustomers(Double minAmount) {
        return customerRepository.findByTotalSpentGreaterThan(minAmount);
    }

    // Address management
    public List<Address> getCustomerAddresses(Long customerId) {
        return addressRepository.findByCustomerId(customerId);
    }

    public Address addAddressToCustomer(Long customerId, Address address) {
        Optional<Customer> customer = customerRepository.findById(customerId);
        if (customer.isPresent()) {
            address.setCustomer(customer.get());
            return addressRepository.save(address);
        }
        throw new RuntimeException("Customer not found with id: " + customerId);
    }

    // Order management
    public List<Order> getCustomerOrders(Long customerId) {
        return orderRepository.findByCustomerId(customerId);
    }

    public List<Order> getCustomerOrdersByStatus(Long customerId, String status) {
        return orderRepository.findByCustomerIdAndStatus(customerId, status);
    }

    // Update customer total spent
    public Customer updateTotalSpent(Long customerId, Double amount) {
        Optional<Customer> customerOpt = customerRepository.findById(customerId);
        if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();
            Double currentTotal = customer.getTotalSpent() != null ? customer.getTotalSpent() : 0.0;
            customer.setTotalSpent(currentTotal + amount);
            return customerRepository.save(customer);
        }
        throw new RuntimeException("Customer not found with id: " + customerId);
    }

    // Update customer information
    public Customer updateCustomer(Long id, Customer updatedCustomer) {
        Optional<Customer> customerOpt = customerRepository.findById(id);
        if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();
            customer.setName(updatedCustomer.getName());
            customer.setEmail(updatedCustomer.getEmail());
            if (updatedCustomer.getTotalSpent() != null) {
                customer.setTotalSpent(updatedCustomer.getTotalSpent());
            }
            return customerRepository.save(customer);
        }
        throw new RuntimeException("Customer not found with id: " + id);
    }

    // Validate customer before operations
    public boolean validateCustomer(Customer customer) {
        if (customer.getName() == null || customer.getName().trim().isEmpty()) {
            return false;
        }
        if (customer.getEmail() == null || customer.getEmail().trim().isEmpty()) {
            return false;
        }
        return !existsByEmail(customer.getEmail());
    }
}
