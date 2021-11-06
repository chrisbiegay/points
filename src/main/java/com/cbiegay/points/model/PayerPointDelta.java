package com.cbiegay.points.model;

/**
 * Represents a change in points for a particular payer.
 */
public class PayerPointDelta {
    private final String payer;
    private final int points;

    public PayerPointDelta(final String payer, final int points) {
        this.payer = payer;
        this.points = points;
    }

    public String getPayer() {
        return payer;
    }

    public int getPoints() {
        return points;
    }
}
