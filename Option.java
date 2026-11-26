package org.sky.service.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/** Дополнительная услуга (спасательный жилет, страховка, гид...). */
@Getter
@ToString
@AllArgsConstructor
public class Option {
    private final String name;
    private final double price;
}
