package com.cbiegay.points.model;

import java.util.Date;

/**
 * Represents a transaction of points.
 */
public class Transaction {
    private final String payer;
    private final Integer points;
    private final Date timestamp;

    public Transaction(String payer, Integer points, Date timestamp) {
        this.payer = payer;
        this.points = points;
        this.timestamp = timestamp;
    }

    public String getPayer() {
        return payer;
    }

    public Integer getPoints() {
        return points;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public MutableTransaction toMutableTransaction() {
        return new MutableTransaction(this);
    }
}
