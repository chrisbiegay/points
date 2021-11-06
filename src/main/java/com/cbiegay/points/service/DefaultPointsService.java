package com.cbiegay.points.service;

import com.cbiegay.points.InsufficientPointsException;
import com.cbiegay.points.model.MutableTransaction;
import com.cbiegay.points.model.PayerPointDelta;
import com.cbiegay.points.model.Transaction;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Default implementation for PointsService.
 */
@Component
public class DefaultPointsService implements PointsService {

    // Singleton Spring beans should typically be stateless and not contain mutable fields.
    // Using a stateful field for the purposes of this exercise, however, in lieu of a persistent data store.
    // Using synchronized methods to manage concurrent access.
    /**
     * The transaction history, from which points can be spent.
     */
    private final List<MutableTransaction> transactions;

    public DefaultPointsService() {
        transactions = new ArrayList<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void addTransaction(final Transaction transaction) {
        transactions.add(transaction.toMutableTransaction());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized List<PayerPointDelta> spend(final int pointsToSpend) throws InsufficientPointsException {
        checkIfPointsToSpendIsValid(pointsToSpend);
        checkIfSufficientPoints(pointsToSpend);

        // Spending the oldest points first by sorting the transactions by timestamp
        transactions.sort(Comparator.comparing(MutableTransaction::getTimestamp));

        final Map<String, Integer> pointsSpentPerPayer = spendFromOrderedTransactions(transactions, pointsToSpend);

        return pointsSpentMapToList(pointsSpentPerPayer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized Map<String, Integer> getBalances() {
        final Map<String, Integer> balances = new HashMap<>();

        for (final MutableTransaction transaction : transactions) {
            final String payer = transaction.getPayer();

            if (!balances.containsKey(payer)) {
                balances.put(payer, transaction.getPoints());
            } else {
                final int payerBalance = balances.get(payer);
                balances.put(payer, payerBalance + transaction.getPoints());
            }
        }

        return balances;
    }

    private void checkIfPointsToSpendIsValid(final int pointsToSpend) {
        // Not in the instructions but we'll assume this rule
        if (pointsToSpend < 1) {
            throw new IllegalArgumentException("Cannot spend less than 1 point");
        }
    }

    /**
     * Throw an exception if trying to spend more points than are available.
     */
    private void checkIfSufficientPoints(final int pointsToSpend) throws InsufficientPointsException {
        int totalAvailablePoints = 0;
        for (final MutableTransaction transaction : transactions) {
            totalAvailablePoints += transaction.getPoints();
        }

        if (pointsToSpend > totalAvailablePoints) {
            throw new InsufficientPointsException();
        }
    }

    /**
     * Spend the specified points from the given ordered list of transactions.
     *
     * @param transactions transactions, in chronological order.
     * @param pointsToSpend number of points to spend.
     * @return a map of payer names to points spent.
     */
    private Map<String, Integer> spendFromOrderedTransactions(
        final List<MutableTransaction> transactions,
        final int pointsToSpend)
    throws InsufficientPointsException {

        int pointsLeftToSpend = pointsToSpend;
        final Map<String, Integer> pointsSpentPerPayer = new HashMap<>();

        for (final MutableTransaction transaction : transactions) {
            final String transactionPayer = transaction.getPayer();
            final int transactionPoints = transaction.getPoints();

            final int spentFromTransaction = Math.min(transactionPoints, pointsLeftToSpend);
            pointsLeftToSpend -= spentFromTransaction;
            transaction.spendPoints(spentFromTransaction);

            if (!pointsSpentPerPayer.containsKey(transactionPayer)) {
                pointsSpentPerPayer.put(transactionPayer, spentFromTransaction * -1);
            } else {
                final int pointsSpentForPayer = pointsSpentPerPayer.get(transactionPayer);
                pointsSpentPerPayer.put(transactionPayer, pointsSpentForPayer - spentFromTransaction);
            }

            if (pointsLeftToSpend == 0) {
                break;
            }
        }

        return pointsSpentPerPayer;
    }

    /**
     * Convert a Map of points spent per payer to a list of PayerPointDeltas.
     * List entries are sorted by points in descending order, to match the example in the instructions.
     */
    private List<PayerPointDelta> pointsSpentMapToList(Map<String, Integer> pointsSpentPerPayer) {
        final List<PayerPointDelta> result = new ArrayList<>();

        for (final String payer : pointsSpentPerPayer.keySet()) {
            final int delta = pointsSpentPerPayer.get(payer);
            result.add(new PayerPointDelta(payer, delta));
        }

        result.sort((deltaA, deltaB) -> deltaB.getPoints() - deltaA.getPoints());

        return result;
    }
}
