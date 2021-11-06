package com.cbiegay.points.service;

import com.cbiegay.points.InsufficientPointsException;
import com.cbiegay.points.model.PayerPointDelta;
import com.cbiegay.points.model.Transaction;

import java.util.List;
import java.util.Map;

/**
 * Service for managing operations on points.
 */
public interface PointsService {

    /**
     * Add a transaction representing points earned for a payer.
     *
     * @param transaction an object representing the points earned for a payer in a transaction.
     */
    void addTransaction(final Transaction transaction);

    /**
     * Spend points accumulated via transactions.
     *
     * @param pointsToSpend the number of points to spend.
     * @return a list of objects indicating how many points were spent per payer, sorted by points in descending order.
     * @throws InsufficientPointsException if trying to spend more points than are available.
     * @throws IllegalArgumentException if pointsToSpend is less than 1.
     */
    List<PayerPointDelta> spend(final int pointsToSpend) throws InsufficientPointsException;

    /**
     * Get the current point balances for each payer.
     *
     * @return a Map of payer names to point balances.
     */
    Map<String, Integer> getBalances();
}
