package com.hotel.model;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CustomerTest {

    @Test
    void customersWithSameEmailAreEqualWhateverTheCase() {
        Customer first = new Customer("Alice", "Alice@Mail.com");
        Customer second = new Customer("Alice Martin", "alice@mail.com");

        Set<Customer> customers = new HashSet<>();
        customers.add(first);
        customers.add(second);

        assertEquals(first, second);
        assertEquals(1, customers.size());
    }

    @Test
    void emptyNameIsRefused() {
        assertThrows(IllegalArgumentException.class, () -> new Customer("  ", "alice@mail.com"));
    }

    @Test
    void emailWithoutAtSignIsRefused() {
        assertThrows(IllegalArgumentException.class, () -> new Customer("Alice", "alice.mail.com"));
    }
}
