package org.sky.service.model;

public class CorporateBoatRental extends Boat {
    private final double corporateDiscount;

    public CorporateBoatRental(String ownerName, double basePrice,
                               double balance, double corporateDiscount) {
        super(ownerName, basePrice, balance);
        if (corporateDiscount < 0 || corporateDiscount >= 1) {
            throw new IllegalArgumentException("Корпоративная скидка должна быть в [0;1).");
        }
        this.corporateDiscount = corporateDiscount;
    }

    @Override
    public double calculateBillingPrice() {
        double total = getBasePrice() + sumOptions();
        total = applyPromos(total);
        return total * (1 - corporateDiscount);
    }
}
