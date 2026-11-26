package org.sky.service.model;

import lombok.Builder;
import lombok.Singular;
import lombok.ToString;

import java.util.List;

/** Аренда физическим лицом. Объект создаётся через Lombok @Builder. */
@ToString(callSuper = true)
public class IndividualBoatRental extends Boat {

    @Builder
    public IndividualBoatRental(String ownerName, double basePrice, double balance,
                                @Singular("option") List<Option> options,
                                @Singular("promo") List<PromoCode> promos) {
        super(ownerName, basePrice, balance);
        options.forEach(this::addOption);
        promos.forEach(this::addPromo);
    }

    @Override
    public double calculateBillingPrice() {
        return round2(applyPromos(getBasePrice() + sumOptions()));
    }
}
