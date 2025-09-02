package com.wassimlagnaoui.Ecommerce.Controller;

import com.wassimlagnaoui.Ecommerce.DTO.*;
import com.wassimlagnaoui.Ecommerce.Service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/customers")
@CrossOrigin(origins = "*")
@Tag(name = "Customer Management", description = "APIs for managing customers in the ecommerce system")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @Operation(summary = "Get all customers", description = "Retrieve a list of all customers")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved customers"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<List<CustomerDTO>> getAllCustomers() {
        List<CustomerDTO> customers = customerService.getAllCustomers();
        return ResponseEntity.ok(customers);
    }

    @Operation(summary = "Get customer by ID", description = "Retrieve a specific customer by their ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer found"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CustomerDTO> getCustomerById(
            @Parameter(description = "Customer ID", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable String id) {
        Optional<CustomerDTO> customer = customerService.getCustomerById(id);
        return customer.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Create a new customer", description = "Add a new customer to the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Customer created"),
            @ApiResponse(responseCode = "400", description = "Invalid customer data")
    })
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

    @Operation(summary = "Update an existing customer", description = "Modify the details of an existing customer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer updated"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<CustomerDTO> updateCustomer(@PathVariable String id, @RequestBody CustomerDTO customer) {
        try {
            CustomerDTO updatedCustomer = customerService.updateCustomer(id, customer);
            return ResponseEntity.ok(updatedCustomer);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Delete a customer", description = "Remove a customer from the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Customer deleted"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable String id) {
        try {
            customerService.deleteCustomer(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Get customer by email", description = "Retrieve a specific customer by their email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer found"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @GetMapping("/email/{email}")
    public ResponseEntity<CustomerDTO> getCustomerByEmail(@PathVariable String email) {
        Optional<CustomerDTO> customer = customerService.findByEmail(email);
        return customer.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get customer by name", description = "Retrieve a specific customer by their name")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer found"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @GetMapping("/name/{name}")
    public ResponseEntity<CustomerDTO> getCustomerByName(@PathVariable String name) {
        Optional<CustomerDTO> customer = customerService.findByName(name);
        return customer.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get high value customers", description = "Retrieve a list of customers who have spent above a certain amount")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved customers"),
            @ApiResponse(responseCode = "400", description = "Invalid amount"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/high-value")
    public ResponseEntity<List<CustomerSummaryDTO>> getHighValueCustomers(@RequestParam Double minAmount) {
        List<CustomerSummaryDTO> customers = customerService.getHighValueCustomers(minAmount);
        return ResponseEntity.ok(customers);
    }

    @Operation(summary = "Check if email exists", description = "Verify if a customer email is already registered")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email existence checked"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @GetMapping("/exists/email/{email}")
    public ResponseEntity<Boolean> checkEmailExists(@PathVariable String email) {
        boolean exists = customerService.existsByEmail(email);
        return ResponseEntity.ok(exists);
    }

    @Operation(summary = "Get customer addresses", description = "Retrieve a list of addresses for a specific customer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved addresses"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @GetMapping("/{customerId}/addresses")
    public ResponseEntity<List<AddressDTO>> getCustomerAddresses(@PathVariable String customerId) {
        try {
            List<AddressDTO> addresses = customerService.getCustomerAddresses(customerId);
            return ResponseEntity.ok(addresses);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Add address to customer", description = "Associate a new address with a specific customer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Address added to customer"),
            @ApiResponse(responseCode = "404", description = "Customer not found"),
            @ApiResponse(responseCode = "400", description = "Invalid address data")
    })
    @PostMapping("/{customerId}/addresses")
    public ResponseEntity<AddressDTO> addAddressToCustomer(@PathVariable String customerId, @RequestBody AddressDTO address) {
        try {
            AddressDTO savedAddress = customerService.addAddressToCustomer(customerId, address);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedAddress);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Get customer orders", description = "Retrieve a list of orders for a specific customer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved orders"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @GetMapping("/{customerId}/orders")
    public ResponseEntity<List<OrderDTO>> getCustomerOrders(@PathVariable String customerId) {
        List<OrderDTO> orders = customerService.getCustomerOrders(customerId);
        return ResponseEntity.ok(orders);
    }

    @Operation(summary = "Get customer orders by status", description = "Retrieve a list of orders for a specific customer filtered by order status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved orders"),
            @ApiResponse(responseCode = "404", description = "Customer or orders not found")
    })
    @GetMapping("/{customerId}/orders/status/{status}")
    public ResponseEntity<List<OrderDTO>> getCustomerOrdersByStatus(@PathVariable String customerId, @PathVariable String status) {
        List<OrderDTO> orders = customerService.getCustomerOrdersByStatus(customerId, status);
        return ResponseEntity.ok(orders);
    }

    @Operation(summary = "Update total spent by customer", description = "Modify the total amount spent by a specific customer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer total spent updated"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @PutMapping("/{customerId}/total-spent")
    public ResponseEntity<CustomerDTO> updateTotalSpent(@PathVariable String customerId, @RequestParam Double amount) {
        try {
            CustomerDTO updatedCustomer = customerService.updateTotalSpent(customerId, amount);
            return ResponseEntity.ok(updatedCustomer);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Validate customer data", description = "Check if the provided customer data is valid")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer data is valid"),
            @ApiResponse(responseCode = "400", description = "Invalid customer data")
    })
    @PostMapping("/validate")
    public ResponseEntity<Boolean> validateCustomer(@RequestBody CustomerDTO customer) {
        boolean isValid = customerService.validateCustomer(customer);
        return ResponseEntity.ok(isValid);
    }
}
