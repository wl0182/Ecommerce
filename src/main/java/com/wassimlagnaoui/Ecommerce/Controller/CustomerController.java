package com.wassimlagnaoui.Ecommerce.Controller;

import com.wassimlagnaoui.Ecommerce.DTO.*;
import com.wassimlagnaoui.Ecommerce.Service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/customers")
@CrossOrigin(origins = "*")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    // Basic CRUD operations - now using DTOs
    @GetMapping
    public ResponseEntity<List<CustomerDTO>> getAllCustomers() {
        List<CustomerDTO> customers = customerService.getAllCustomers();
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerDTO> getCustomerById(@PathVariable Long id) {
        Optional<CustomerDTO> customer = customerService.getCustomerById(id);
        return customer.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CustomerDTO> createCustomer(@RequestBody CustomerDTO customer) {
        try {
            if (!customerService.validateCustomer(customer)) {
                return ResponseEntity.badRequest().build();
            }
            CustomerDTO savedCustomer = customerService.saveCustomer(customer);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedCustomer);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerDTO> updateCustomer(@PathVariable Long id, @RequestBody CustomerDTO customer) {
        try {
            CustomerDTO updatedCustomer = customerService.updateCustomer(id, customer);
            return ResponseEntity.ok(updatedCustomer);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        try {
            customerService.deleteCustomer(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Customer search endpoints - now using DTOs
    @GetMapping("/email/{email}")
    public ResponseEntity<CustomerDTO> getCustomerByEmail(@PathVariable String email) {
        Optional<CustomerDTO> customer = customerService.findByEmail(email);
        return customer.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<CustomerDTO> getCustomerByName(@PathVariable String name) {
        Optional<CustomerDTO> customer = customerService.findByName(name);
        return customer.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/high-value")
    public ResponseEntity<List<CustomerSummaryDTO>> getHighValueCustomers(@RequestParam Double minAmount) {
        List<CustomerSummaryDTO> customers = customerService.getHighValueCustomers(minAmount);
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/exists/email/{email}")
    public ResponseEntity<Boolean> checkEmailExists(@PathVariable String email) {
        boolean exists = customerService.existsByEmail(email);
        return ResponseEntity.ok(exists);
    }

    // Address management endpoints - now using DTOs
    @GetMapping("/{customerId}/addresses")
    public ResponseEntity<List<AddressDTO>> getCustomerAddresses(@PathVariable Long customerId) {
        try {
            List<AddressDTO> addresses = customerService.getCustomerAddresses(customerId);
            return ResponseEntity.ok(addresses);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{customerId}/addresses")
    public ResponseEntity<AddressDTO> addAddressToCustomer(@PathVariable Long customerId, @RequestBody AddressDTO address) {
        try {
            AddressDTO savedAddress = customerService.addAddressToCustomer(customerId, address);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedAddress);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Order management endpoints - now using DTOs
    @GetMapping("/{customerId}/orders")
    public ResponseEntity<List<OrderDTO>> getCustomerOrders(@PathVariable Long customerId) {
        List<OrderDTO> orders = customerService.getCustomerOrders(customerId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{customerId}/orders/status/{status}")
    public ResponseEntity<List<OrderDTO>> getCustomerOrdersByStatus(@PathVariable Long customerId, @PathVariable String status) {
        List<OrderDTO> orders = customerService.getCustomerOrdersByStatus(customerId, status);
        return ResponseEntity.ok(orders);
    }

    // Customer financial operations - now using DTOs
    @PutMapping("/{customerId}/total-spent")
    public ResponseEntity<CustomerDTO> updateTotalSpent(@PathVariable Long customerId, @RequestParam Double amount) {
        try {
            CustomerDTO updatedCustomer = customerService.updateTotalSpent(customerId, amount);
            return ResponseEntity.ok(updatedCustomer);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Validation endpoint - now using DTOs
    @PostMapping("/validate")
    public ResponseEntity<Boolean> validateCustomer(@RequestBody CustomerDTO customer) {
        boolean isValid = customerService.validateCustomer(customer);
        return ResponseEntity.ok(isValid);
    }
}
