package com.hotel.service;

import com.hotel.model.Room;
import com.hotel.pricing.DiscountPolicy;

/**
 * Computes the final price of a stay: room price (depends on the room type)
 * then the discount policy received from outside (dependency injection).
 */
public class PricingService {

    private final DiscountPolicy discountPolicy;

    public PricingService(DiscountPolicy discountPolicy) {
        this.discountPolicy = discountPolicy;
    }

    public double calculateTotal(Room room, int nights, int guests, boolean breakfast) {
        // Polymorphism: each Room subclass applies its own pricing rule.
        double basePrice = room.calculatePrice(nights, guests, breakfast);
        double finalPrice = discountPolicy.apply(basePrice, nights);
        return roundToCents(finalPrice);
    }

    public DiscountPolicy getDiscountPolicy() {
        return discountPolicy;
    }

    private double roundToCents(double amount) {
        return Math.round(amount * 100) / 100.0;
    }
}
