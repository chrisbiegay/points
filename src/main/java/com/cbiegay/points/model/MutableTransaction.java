package com.cbiegay.points.model;

import com.cbiegay.points.InsufficientPointsException;

import java.util.Date;

// Having a separate mutable transaction class may be overkill for this exercise, but I think it's
// important to distinguish an immutable transaction from one that points can be deducted from.
/**
 * A mutable version of the Transaction class representing a transaction of points for a payer.
 */
public class MutableTransaction {
    private final String payer;
    private int points;
    private final Date timestamp;

    public MutableTransaction(Transaction transaction) {
        this.payer = transaction.getPayer();
        this.points = transaction.getPoints();
        this.timestamp = transaction.getTimestamp();
    }

    public String getPayer() {
        return payer;
    }

    public int getPoints() {
        return points;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    /**
     * Spend (deduct) a given number of points from this transaction.
     *
     * @param points number of points to spend.
     * @throws InsufficientPointsException if attempting to spend more points than are currently available
     *         in this transaction.
     */
    public void spendPoints(final int points) throws InsufficientPointsException {
        if (points > this.points) {
            throw new InsufficientPointsException();
        }

        this.points -= points;
    }
}
