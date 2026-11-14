package com.hotel.model;

import java.util.Objects;

/** A hotel customer, identified by their email address. */
public class Customer {

    private final String name;
    private final String email;

    public Customer(String name, String email) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Customer name cannot be empty");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Invalid email: " + email);
        }
        this.name = name.trim();
        // Emails are compared case-insensitively, so we store them in lower case.
        this.email = email.trim().toLowerCase();
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    /** Two customers are the same person if they have the same email. */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Customer other)) {
            return false;
        }
        return email.equals(other.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
    }

    @Override
    public String toString() {
        return name + " <" + email + ">";
    }
}
