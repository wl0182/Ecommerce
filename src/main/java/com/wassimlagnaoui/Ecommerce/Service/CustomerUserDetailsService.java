package com.wassimlagnaoui.Ecommerce.Service;

import com.wassimlagnaoui.Ecommerce.Configuration.CustomerUserDetails;
import com.wassimlagnaoui.Ecommerce.Domain.Customer;
import com.wassimlagnaoui.Ecommerce.Repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomerUserDetailsService implements UserDetailsService {

    private final CustomerRepository customerRepository;

    @Autowired
    public CustomerUserDetailsService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                    "User not found with email: " + email
                ));

        return new CustomerUserDetails(customer);
    }

    // Additional method to load user by ID if needed
    public UserDetails loadUserById(String userId) throws UsernameNotFoundException {
        Customer customer = customerRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException(
                    "User not found with ID: " + userId
                ));

        return new CustomerUserDetails(customer);
    }

    // Method to check if user exists by email
    public boolean existsByEmail(String email) {
        return customerRepository.findByEmail(email).isPresent();
    }

    // Method to get customer entity by email
    public Customer getCustomerByEmail(String email) throws UsernameNotFoundException {
        return customerRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                    "Customer not found with email: " + email
                ));
    }
}
