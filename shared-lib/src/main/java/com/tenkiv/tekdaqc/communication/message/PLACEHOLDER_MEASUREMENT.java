package com.tenkiv.tekdaqc.communication.message;

import org.jetbrains.annotations.NotNull;
import org.tenkiv.coral.ValueInstant;
import tec.uom.se.ComparableQuantity;

import javax.measure.Quantity;
import java.time.Instant;

/**
 * DO NOT COMMIT THIS
 */
public class PLACEHOLDER_MEASUREMENT<Q extends Quantity<Q>> implements ValueInstant<ComparableQuantity<Q>> {

    public PLACEHOLDER_MEASUREMENT(ComparableQuantity<Q> quantity, long instant) {
        this.qunatity = quantity;
        this.instant = Instant.ofEpochMilli(instant);
    }

    public PLACEHOLDER_MEASUREMENT(ComparableQuantity<Q> quantity, Instant instant) {
        this.qunatity = quantity;
        this.instant = instant;
    }

    public PLACEHOLDER_MEASUREMENT(ComparableQuantity<Q> quantity){
        this.qunatity = quantity;
        this.instant = Instant.now();
    }

    public ComparableQuantity<Q> getQunatity() {
        return qunatity;
    }

    public void setQunatity(ComparableQuantity<Q> qunatity) {
        this.qunatity = qunatity;
    }

    @NotNull
    @Override
    public Instant getInstant() {
        return instant;
    }

    public void setInstant(Instant instant) {
        this.instant = instant;
    }

    private ComparableQuantity<Q> qunatity;

    private Instant instant;

    @NotNull
    @Override
    public ComparableQuantity<Q> getValue() {
        return this.qunatity;
    }
}
