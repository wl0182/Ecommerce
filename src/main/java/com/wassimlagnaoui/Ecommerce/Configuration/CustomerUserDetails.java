package com.wassimlagnaoui.Ecommerce.Configuration;

import com.wassimlagnaoui.Ecommerce.Domain.Customer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class CustomerUserDetails implements UserDetails {

    private final Customer customer;

    public CustomerUserDetails(Customer customer) {
        this.customer = customer;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(
            new SimpleGrantedAuthority("ROLE_" + customer.getRole().name())
        );
    }

    @Override
    public String getPassword() {
        return customer.getPassword();
    }

    @Override
    public String getUsername() {
        return customer.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return customer.getEnabled();
    }

    // Getter to access the underlying customer entity
    public Customer getCustomer() {
        return customer;
    }

    public String getId() {
        return customer.getId();
    }

    public String getName() {
        return customer.getName();
    }

    public String getRole() {
        return customer.getRole().name();
    }
}
