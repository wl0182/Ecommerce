package com.wassimlagnaoui.Ecommerce.Service;

import com.wassimlagnaoui.Ecommerce.Domain.Address;
import com.wassimlagnaoui.Ecommerce.Domain.Customer;
import com.wassimlagnaoui.Ecommerce.Domain.Order;
import com.wassimlagnaoui.Ecommerce.DTO.*;
import com.wassimlagnaoui.Ecommerce.Exception.CustomerNotFoundException;
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

    @Autowired
    private DTOMapper dtoMapper;

    // Basic CRUD operations - now returning DTOs
    public List<CustomerDTO> getAllCustomers() {
        List<Customer> customers = customerRepository.findAll();
        return dtoMapper.toCustomerDTOList(customers);
    }

    public Optional<CustomerDTO> getCustomerById(Long id) {
        Optional<Customer> customer = customerRepository.findById(id);
        return customer.map(dtoMapper::toCustomerDTO);
    }

    public CustomerDTO saveCustomer(CustomerDTO customerDTO) {
        Customer customer = dtoMapper.toCustomerEntity(customerDTO);
        Customer savedCustomer = customerRepository.save(customer);
        return dtoMapper.toCustomerDTO(savedCustomer);
    }

    public void deleteCustomer(Long id) {
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

    // Address management - now returning DTOs
    public List<AddressDTO> getCustomerAddresses(Long customerId) {
        List<Address> addresses = addressRepository.findByCustomerId(customerId);
        return dtoMapper.toAddressDTOList(addresses);
    }

    public AddressDTO addAddressToCustomer(Long customerId, AddressDTO addressDTO) {
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
    public List<OrderDTO> getCustomerOrders(Long customerId) {
        List<Order> orders = orderRepository.findByCustomerId(customerId);
        return dtoMapper.toOrderDTOList(orders);
    }

    public List<OrderDTO> getCustomerOrdersByStatus(Long customerId, String status) {
        List<Order> orders = orderRepository.findByCustomerIdAndStatus(customerId, status);
        return dtoMapper.toOrderDTOList(orders);
    }

    // Update customer total spent - internal method, entity manipulation
    public CustomerDTO updateTotalSpent(Long customerId, Double amount) {
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

    // Update customer information - now using DTOs
    public CustomerDTO updateCustomer(Long id, CustomerDTO updatedCustomerDTO) {
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

    // Validate customer before operations - now using DTO
    public boolean validateCustomer(CustomerDTO customerDTO) {
        if (customerDTO.getName() == null || customerDTO.getName().trim().isEmpty()) {
            return false;
        }
        if (customerDTO.getEmail() == null || customerDTO.getEmail().trim().isEmpty()) {
            return false;
        }
        return !existsByEmail(customerDTO.getEmail());
    }
}
