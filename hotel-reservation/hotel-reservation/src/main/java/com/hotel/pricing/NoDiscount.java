package com.hotel.pricing;

/** Default policy: the price is left unchanged. */
public class NoDiscount implements DiscountPolicy {

    @Override
    public double apply(double price, long nights) {
        return price;
    }

    @Override
    public String getDescription() {
        return "No discount";
    }
}
