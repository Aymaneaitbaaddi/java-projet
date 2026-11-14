package com.hotel.pricing;

/** Applies a percentage discount when the stay reaches a minimum number of nights. */
public class LongStayDiscount implements DiscountPolicy {

    private final int minimumNights;
    private final double rate;

    /**
     * @param minimumNights number of nights from which the discount applies (inclusive)
     * @param rate          discount rate between 0 and 1, e.g. 0.10 for 10 %
     */
    public LongStayDiscount(int minimumNights, double rate) {
        if (minimumNights <= 0) {
            throw new IllegalArgumentException("Minimum nights must be positive");
        }
        if (rate <= 0 || rate >= 1) {
            throw new IllegalArgumentException("Rate must be between 0 and 1 (exclusive)");
        }
        this.minimumNights = minimumNights;
        this.rate = rate;
    }

    @Override
    public double apply(double price, long nights) {
        if (nights >= minimumNights) {
            return price * (1 - rate);
        }
        return price;
    }

    @Override
    public String getDescription() {
        return String.format("-%d%% from %d nights", Math.round(rate * 100), minimumNights);
    }
}
