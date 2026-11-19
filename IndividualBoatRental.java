package org.sky.service.model;

public class IndividualBoatRental extends Boat {

    public IndividualBoatRental(String ownerName, double basePrice, double balance) {
        super(ownerName, basePrice, balance);
    }

    @Override
    public double calculateBillingPrice() {
        double total = getBasePrice() + sumOptions();
        return applyPromos(total);
    }
}
