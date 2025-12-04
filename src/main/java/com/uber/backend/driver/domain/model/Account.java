package com.uber.backend.driver.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class Account {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String phoneNumber;
    private LocalDateTime createdAt;

    public void initializeCreationTime() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    public boolean isValid() {
        return firstName != null && !firstName.isBlank()
                && lastName != null && !lastName.isBlank()
                && email != null && !email.isBlank()
                && phoneNumber != null && !phoneNumber.isBlank();
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
