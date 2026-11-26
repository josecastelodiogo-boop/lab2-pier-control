package org.sky.service.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.ToString;

import java.util.List;

/** Корпоративная аренда: дополнительно применяется скидка corporateDiscount (доля от 0 до 1). */
@Getter
@ToString(callSuper = true)
public class CorporateBoatRental extends Boat {
    private final double corporateDiscount;

    @Builder
    public CorporateBoatRental(String ownerName, double basePrice, double balance,
                               double corporateDiscount,
                               @Singular("option") List<Option> options,
                               @Singular("promo") List<PromoCode> promos) {
        super(ownerName, basePrice, balance);
        if (corporateDiscount < 0 || corporateDiscount >= 1) {
            throw new IllegalArgumentException("corporateDiscount должен быть в диапазоне [0; 1): " + corporateDiscount);
        }
        this.corporateDiscount = corporateDiscount;
        options.forEach(this::addOption);
        promos.forEach(this::addPromo);
    }

    @Override
    public double calculateBillingPrice() {
        return round2(applyPromos(getBasePrice() + sumOptions()) * (1 - corporateDiscount));
    }
}
