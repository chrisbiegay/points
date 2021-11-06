package com.cbiegay.points.model;

/**
 * Payload for a point spend operation.
 */
public class PointSpend {

    // Jackson requires a default constructor for single-property classes, otherwise we could make this final
    // and initialize it in the constructor.
    private int points;

    public int getPoints() {
        return points;
    }

    public void setPoints(final int points) {
        this.points = points;
    }
}