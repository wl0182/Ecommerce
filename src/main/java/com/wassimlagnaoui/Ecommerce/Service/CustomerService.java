package com.wassimlagnaoui.Ecommerce.Service;

import com.wassimlagnaoui.Ecommerce.Domain.Address;
import com.wassimlagnaoui.Ecommerce.Domain.Customer;
import com.wassimlagnaoui.Ecommerce.Domain.Order;
import com.wassimlagnaoui.Ecommerce.DTO.*;
import com.wassimlagnaoui.Ecommerce.Exception.CustomerNotFoundException;
import com.wassimlagnaoui.Ecommerce.Repository.AddressRepository;
import com.wassimlagnaoui.Ecommerce.Repository.CustomerRepository;
import com.wassimlagnaoui.Ecommerce.Repository.OrderRepository;
import jakarta.validation.Valid;
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

    @Autowired
    private DTOMapper dtoMapper;

    // Basic CRUD operations - now returning DTOs
    public List<CustomerDTO> getAllCustomers() {
        List<Customer> customers = customerRepository.findAll();
        return dtoMapper.toCustomerDTOList(customers);
    }

    public Optional<CustomerDTO> getCustomerById(String id) {
        Optional<Customer> customer = customerRepository.findById(id);
        return customer.map(dtoMapper::toCustomerDTO);
    }

    // Updated to accept Request DTOs with built-in validation
    public CustomerDTO saveCustomer(CustomerCreateRequest customerRequest) {
        // Validation is now handled in service layer
        if (!validateCustomer(customerRequest)) {
            throw new IllegalArgumentException("Invalid customer data");
        }
        Customer customer = dtoMapper.toCustomerEntity(customerRequest);
        Customer savedCustomer = customerRepository.save(customer);
        return dtoMapper.toCustomerDTO(savedCustomer);
    }

    // Keep the old method for backward compatibility
    public CustomerDTO saveCustomer(CustomerDTO customerDTO) {
        Customer customer = dtoMapper.toCustomerEntity(customerDTO);
        Customer savedCustomer = customerRepository.save(customer);
        return dtoMapper.toCustomerDTO(savedCustomer);
    }

    public void deleteCustomer(String id) {
        customerRepository.deleteById(id);
    }

    // Custom business methods - now returning DTOs
    public Optional<CustomerDTO> findByEmail(String email) {
        Optional<Customer> customer = customerRepository.findByEmail(email);
        return customer.map(dtoMapper::toCustomerDTO);
    }

    public Optional<CustomerDTO> findByName(String name) {
        Optional<Customer> customer = customerRepository.findByName(name);
        return customer.map(dtoMapper::toCustomerDTO);
    }

    public boolean existsByEmail(String email) {
        return customerRepository.existsByEmail(email);
    }

    public List<CustomerSummaryDTO> getHighValueCustomers(Double minAmount) {
        List<Customer> customers = customerRepository.findByTotalSpentGreaterThan(minAmount);
        return customers.stream()
                .map(dtoMapper::toCustomerSummaryDTO)
                .toList();
    }

    // Address management - updated to accept Request DTOs
    public List<AddressDTO> getCustomerAddresses(String customerId) {
        List<Address> addresses = addressRepository.findByCustomerId(customerId);
        return dtoMapper.toAddressDTOList(addresses);
    }

    public AddressDTO addAddressToCustomer(String customerId, AddressCreateRequest addressRequest) {
        // Validate address data
        if (addressRequest.getStreet() == null || addressRequest.getStreet().trim().isEmpty()) {
            throw new IllegalArgumentException("Street address is required");
        }
        if (addressRequest.getCity() == null || addressRequest.getCity().trim().isEmpty()) {
            throw new IllegalArgumentException("City is required");
        }
        if (addressRequest.getState() == null || addressRequest.getState().trim().isEmpty()) {
            throw new IllegalArgumentException("State is required");
        }
        if (addressRequest.getCountry() == null || addressRequest.getCountry().trim().isEmpty()) {
            throw new IllegalArgumentException("Country is required");
        }

        Optional<Customer> customer = customerRepository.findById(customerId);
        if (customer.isPresent()) {
            Address address = dtoMapper.toAddressEntity(addressRequest);
            address.setCustomer(customer.get());
            Address savedAddress = addressRepository.save(address);
            return dtoMapper.toAddressDTO(savedAddress);
        }
        throw new CustomerNotFoundException(customerId);
    }

    // Keep the old method for backward compatibility
    public AddressDTO addAddressToCustomer(String customerId, AddressDTO addressDTO) {
        Optional<Customer> customer = customerRepository.findById(customerId);
        if (customer.isPresent()) {
            Address address = dtoMapper.toAddressEntity(addressDTO);
            address.setCustomer(customer.get());
            Address savedAddress = addressRepository.save(address);
            return dtoMapper.toAddressDTO(savedAddress);
        }
        throw new CustomerNotFoundException(customerId);
    }

    // Order management - now returning DTOs
    public List<OrderDTO> getCustomerOrders(String customerId) {
        List<Order> orders = orderRepository.findByCustomerId(customerId);
        return dtoMapper.toOrderDTOList(orders);
    }

    public List<OrderDTO> getCustomerOrdersByStatus(String customerId, String status) {
        List<Order> orders = orderRepository.findByCustomerIdAndStatus(customerId, status);
        return dtoMapper.toOrderDTOList(orders);
    }

    // Update customer total spent - internal method, entity manipulation
    public CustomerDTO updateTotalSpent(String customerId, Double amount) {
        Optional<Customer> customerOpt = customerRepository.findById(customerId);
        if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();
            Double currentTotal = customer.getTotalSpent() != null ? customer.getTotalSpent() : 0.0;
            customer.setTotalSpent(currentTotal + amount);
            Customer savedCustomer = customerRepository.save(customer);
            return dtoMapper.toCustomerDTO(savedCustomer);
        }
        throw new CustomerNotFoundException(customerId);
    }

    // Update customer information - updated to accept Request DTOs with validation
    public CustomerDTO updateCustomer(String id, CustomerUpdateRequest customerRequest) {
        // Validation for update request (less strict than create)
        if (customerRequest.getName() == null || customerRequest.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Customer name cannot be empty");
        }
        if (customerRequest.getEmail() == null || customerRequest.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Customer email cannot be empty");
        }

        Optional<Customer> customerOpt = customerRepository.findById(id);
        if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();

            // Check if email is already taken by another customer
            if (!customer.getEmail().equals(customerRequest.getEmail()) && existsByEmail(customerRequest.getEmail())) {
                throw new IllegalArgumentException("Email already exists");
            }

            customer.setName(customerRequest.getName());
            customer.setEmail(customerRequest.getEmail());
            if (customerRequest.getTotalSpent() != null) {
                customer.setTotalSpent(customerRequest.getTotalSpent());
            }
            Customer savedCustomer = customerRepository.save(customer);
            return dtoMapper.toCustomerDTO(savedCustomer);
        }
        throw new CustomerNotFoundException(id);
    }

    // Keep the old method for backward compatibility
    public CustomerDTO updateCustomer(String id, CustomerDTO updatedCustomerDTO) {
        Optional<Customer> customerOpt = customerRepository.findById(id);
        if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();
            customer.setName(updatedCustomerDTO.getName());
            customer.setEmail(updatedCustomerDTO.getEmail());
            if (updatedCustomerDTO.getTotalSpent() != null) {
                customer.setTotalSpent(updatedCustomerDTO.getTotalSpent());
            }
            Customer savedCustomer = customerRepository.save(customer);
            return dtoMapper.toCustomerDTO(savedCustomer);
        }
        throw new CustomerNotFoundException(id);
    }

    // Validate customer - updated to accept Request DTOs
    public boolean validateCustomer(@Valid CustomerCreateRequest customerRequest) {
        if (customerRequest.getName() == null || customerRequest.getName().trim().isEmpty()) {
            return false;
        }
        if (customerRequest.getEmail() == null || customerRequest.getEmail().trim().isEmpty()) {
            return false;
        }
        return !existsByEmail(customerRequest.getEmail());
    }





    // Keep the old method for backward compatibility
    public boolean validateCustomer(CustomerDTO customerDTO) {
        if (customerDTO.getName() == null || customerDTO.getName().trim().isEmpty()) {
            return false;
        }
        if (customerDTO.getEmail() == null || customerDTO.getEmail().trim().isEmpty()) {
            return false;
        }
        return !existsByEmail(customerDTO.getEmail());
    }

    public CustomerDTO updateCustomerEmail(String id, String email) {
        Optional<Customer> customerOpt = customerRepository.findById(id);
        if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();
            // Check if email is already taken by another customer
            if (!customer.getEmail().equals(email) && existsByEmail(email)) {
                throw new IllegalArgumentException("Email already exists");
            }
            customer.setEmail(email);
            Customer savedCustomer = customerRepository.save(customer);
            return dtoMapper.toCustomerDTO(savedCustomer);
        }
        throw new CustomerNotFoundException(id);
    }
}
