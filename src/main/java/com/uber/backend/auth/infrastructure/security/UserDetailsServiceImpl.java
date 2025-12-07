package com.uber.backend.auth.infrastructure.security;

import com.uber.backend.driver.infrastructure.persistence.AccountEntity;
import com.uber.backend.driver.infrastructure.repository.DriverRepository;
import com.uber.backend.passenger.infrastructure.repository.PassengerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Custom UserDetailsService implementation for loading user-specific data.
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final PassengerRepository passengerRepository;
    private final DriverRepository driverRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        AccountEntity account = passengerRepository.findByEmail(email)
                .map(passenger -> (AccountEntity) passenger)
                .orElse(null);
        if (account == null) {
            account = driverRepository.findByEmail(email)
                    .map(driver -> (AccountEntity) driver)
                    .orElseThrow(() -> new UsernameNotFoundException(
                            "User not found with email: " + email));
        }

        return User.builder()
                .username(account.getEmail())
                .password(account.getPassword())
                .authorities(Collections.singletonList(
                        new SimpleGrantedAuthority(account.getRole().getAuthority())))
                .build();
    }
}
