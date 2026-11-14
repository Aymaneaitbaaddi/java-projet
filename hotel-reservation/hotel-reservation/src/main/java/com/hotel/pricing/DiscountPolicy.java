package com.hotel.pricing;

/**
 * A rule that may reduce the price of a stay (Strategy pattern).
 *
 * An interface rather than an abstract class: a discount is an interchangeable
 * behaviour with no shared state, and new rules can be added as new classes
 * without modifying the pricing code (Open/Closed principle).
 */
public interface DiscountPolicy {

    /** Returns the price after the discount has been applied. */
    double apply(double price, long nights);

    /** Short description displayed to the customer. */
    String getDescription();
}
