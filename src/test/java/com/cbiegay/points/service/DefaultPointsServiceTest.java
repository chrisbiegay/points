package com.cbiegay.points.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import com.cbiegay.points.InsufficientPointsException;
import com.cbiegay.points.model.PayerPointDelta;
import com.cbiegay.points.model.Transaction;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Unit tests for DefaultPointsService.
 */
public class DefaultPointsServiceTest {

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");

    @Test
    public void getBalances_initialState() {
        final DefaultPointsService service = new DefaultPointsService();
        assertEquals(0, service.getBalances().size());
    }

    @Test
    public void getBalances_afterTransactionsAdded() throws Exception {
        final DefaultPointsService service = new DefaultPointsService();

        service.addTransaction(new Transaction("DANNON", 1000, parseDate("2020-11-02 14:00:00 GMT")));
        service.addTransaction(new Transaction("UNILEVER", 200, parseDate("2020-10-31 11:00:00 GMT")));
        service.addTransaction(new Transaction("DANNON", -200, parseDate("2020-10-31 15:00:00 GMT")));
        service.addTransaction(new Transaction("MILLER COORS", 10000, parseDate("2020-11-01 14:00:00 GMT")));
        service.addTransaction(new Transaction("MILLER COORS", -50, parseDate("2020-11-01 14:00:00 GMT")));
        service.addTransaction(new Transaction("DANNON", 300, parseDate("2020-10-31 10:00:00 GMT")));

        final Map<String, Integer> balances = service.getBalances();

        assertEquals(3, balances.size());
        assertEquals(1100, balances.get("DANNON"));
        assertEquals(200, balances.get("UNILEVER"));
        assertEquals(9950, balances.get("MILLER COORS"));
    }

    @Test
    public void spend_singlePayer() throws Exception {
        final DefaultPointsService service = new DefaultPointsService();

        service.addTransaction(new Transaction("DANNON", 100, parseDate("2020-11-02 14:00:00 GMT")));
        service.addTransaction(new Transaction("DANNON", 200, parseDate("2020-11-03 15:00:00 GMT")));

        final List<PayerPointDelta> result = service.spend(250);

        assertEquals(1, result.size());
        assertEquals("DANNON", result.get(0).getPayer());
        assertEquals(-250, result.get(0).getPoints());

        final Map<String, Integer> balances = service.getBalances();

        assertEquals(1, balances.size());
        assertEquals(50, balances.get("DANNON"));
    }

    @Test
    public void spend_multiplePayersInChronologicalOrder() throws Exception {
        final DefaultPointsService service = new DefaultPointsService();

        service.addTransaction(new Transaction("DANNON", 300, parseDate("2020-10-31 10:00:00 GMT")));
        service.addTransaction(new Transaction("UNILEVER", 200, parseDate("2020-10-31 11:00:00 GMT")));
        service.addTransaction(new Transaction("DANNON", -200, parseDate("2020-10-31 15:00:00 GMT")));
        service.addTransaction(new Transaction("MILLER COORS", 10000, parseDate("2020-11-01 14:00:00 GMT")));
        service.addTransaction(new Transaction("DANNON", 1000, parseDate("2020-11-02 14:00:00 GMT")));

        final List<PayerPointDelta> result = service.spend(5000);

        assertEquals(3, result.size());
        assertEquals("DANNON", result.get(0).getPayer());
        assertEquals(-100, result.get(0).getPoints());
        assertEquals("UNILEVER", result.get(1).getPayer());
        assertEquals(-200, result.get(1).getPoints());
        assertEquals("MILLER COORS", result.get(2).getPayer());
        assertEquals(-4700, result.get(2).getPoints());

        final Map<String, Integer> balances = service.getBalances();

        assertEquals(3, balances.size());
        assertEquals(1000, balances.get("DANNON"));
        assertEquals(0, balances.get("UNILEVER"));
        assertEquals(5300, balances.get("MILLER COORS"));
    }

    @Test
    public void spend_multiplePayersNotChronologicalOrder() throws Exception {
        final DefaultPointsService service = new DefaultPointsService();

        service.addTransaction(new Transaction("DANNON", 1000, parseDate("2020-11-02 14:00:00 GMT")));
        service.addTransaction(new Transaction("UNILEVER", 200, parseDate("2020-10-31 11:00:00 GMT")));
        service.addTransaction(new Transaction("DANNON", -200, parseDate("2020-10-31 15:00:00 GMT")));
        service.addTransaction(new Transaction("MILLER COORS", 10000, parseDate("2020-11-01 14:00:00 GMT")));
        service.addTransaction(new Transaction("DANNON", 300, parseDate("2020-10-31 10:00:00 GMT")));

        final List<PayerPointDelta> result = service.spend(5000);

        assertEquals(3, result.size());
        assertEquals("DANNON", result.get(0).getPayer());
        assertEquals(-100, result.get(0).getPoints());
        assertEquals("UNILEVER", result.get(1).getPayer());
        assertEquals(-200, result.get(1).getPoints());
        assertEquals("MILLER COORS", result.get(2).getPayer());
        assertEquals(-4700, result.get(2).getPoints());

        final Map<String, Integer> balances = service.getBalances();

        assertEquals(3, balances.size());
        assertEquals(1000, balances.get("DANNON"));
        assertEquals(0, balances.get("UNILEVER"));
        assertEquals(5300, balances.get("MILLER COORS"));
    }

    @Test
    public void spend_responseDoesntContainPayersWithNoDeductions() throws Exception {
        final DefaultPointsService service = new DefaultPointsService();

        service.addTransaction(new Transaction("ALPHA", 200, parseDate("2020-11-01 11:00:00 GMT")));
        service.addTransaction(new Transaction("BRAVO", 100, parseDate("2020-11-01 12:00:00 GMT")));
        service.addTransaction(new Transaction("CHARLIE", 300, parseDate("2020-11-01 13:00:00 GMT")));

        final List<PayerPointDelta> result = service.spend(150);

        assertEquals(1, result.size());
        assertEquals("ALPHA", result.get(0).getPayer());
        assertEquals(-150, result.get(0).getPoints());
    }

    @Test
    public void spend_multipleSpends() throws Exception {
        final DefaultPointsService service = new DefaultPointsService();

        service.addTransaction(new Transaction("ALPHA", 200, parseDate("2020-11-01 11:00:00 GMT")));
        service.addTransaction(new Transaction("BRAVO", 100, parseDate("2020-11-01 12:00:00 GMT")));
        service.addTransaction(new Transaction("CHARLIE", 300, parseDate("2020-11-01 13:00:00 GMT")));

        final List<PayerPointDelta> result1 = service.spend(150);

        assertEquals(1, result1.size());
        assertEquals("ALPHA", result1.get(0).getPayer());
        assertEquals(-150, result1.get(0).getPoints());

        final List<PayerPointDelta> result2 = service.spend(205);

        assertEquals(3, result2.size());
        assertEquals("ALPHA", result2.get(0).getPayer());
        assertEquals(-50, result2.get(0).getPoints());
        assertEquals("CHARLIE", result2.get(1).getPayer());
        assertEquals(-55, result2.get(1).getPoints());
        assertEquals("BRAVO", result2.get(2).getPayer());
        assertEquals(-100, result2.get(2).getPoints());

        final Map<String, Integer> balances = service.getBalances();

        assertEquals(3, balances.size());
        assertEquals(0, balances.get("ALPHA"));
        assertEquals(0, balances.get("BRAVO"));
        assertEquals(245, balances.get("CHARLIE"));
    }

    @Test
    public void spend_overSpendingThrowsException() throws Exception {
        final DefaultPointsService service = new DefaultPointsService();

        service.addTransaction(new Transaction("ALPHA", 100, parseDate("2020-11-02 14:00:00 GMT")));
        service.addTransaction(new Transaction("BRAVO", 200, parseDate("2020-11-03 15:00:00 GMT")));

        assertThrows(InsufficientPointsException.class, () -> service.spend(301));
    }

    @Test
    public void spend_throwsExceptionIfSpendingLessThan1Point() throws Exception {
        final DefaultPointsService service = new DefaultPointsService();

        service.addTransaction(new Transaction("ALPHA", 100, parseDate("2020-11-02 14:00:00 GMT")));

        assertThrows(IllegalArgumentException.class, () -> service.spend(0));
        assertThrows(IllegalArgumentException.class, () -> service.spend(-1));
    }

    private Date parseDate(String dateString) throws ParseException {
        return dateFormat.parse(dateString);
    }
}
