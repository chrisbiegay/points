package com.cbiegay.points.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.cbiegay.points.InsufficientPointsException;
import com.cbiegay.points.model.PayerPointDelta;
import com.cbiegay.points.model.Transaction;
import com.cbiegay.points.service.DefaultPointsService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Unit tests for PointsController.
 */
@WebMvcTest(PointsController.class)
public class PointsControllerTest {

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DefaultPointsService pointsService;

    @Captor
    private ArgumentCaptor<Transaction> transactionCaptor;

    @Test
    public void addTransaction_handlesAddedTransactionSuccessfully() throws Exception {
        mockMvc.perform(
            post("/points/transaction")
                .contentType(APPLICATION_JSON)
                .content("{ \"payer\": \"DANNON\", \"points\": 1000, \"timestamp\": \"2020-11-02T14:00:00Z\" }"))
            .andExpect(status().isOk());

        verify(pointsService).addTransaction(transactionCaptor.capture());
        assertEquals("DANNON", transactionCaptor.getValue().getPayer());
        assertEquals(1000, transactionCaptor.getValue().getPoints());
        assertEquals(toDate("2020-11-02T14:00:00Z"), transactionCaptor.getValue().getTimestamp());
    }

    @Test
    public void spend_handlesPointSpendSuccessfully() throws Exception {
        final List<PayerPointDelta> payerPointDeltas = Arrays.asList(
            new PayerPointDelta("ALPHA", 2000),
            new PayerPointDelta("BRAVO", 3000)
        );

        when(pointsService.spend(5000)).thenReturn(payerPointDeltas);

        mockMvc.perform(
            post("/points/spend")
                .contentType(APPLICATION_JSON)
                .content("{ \"points\": 5000 }"))
            .andExpect(status().isOk())
            .andExpect(content().json("[{\"payer\":\"ALPHA\",\"points\":2000},{\"payer\":\"BRAVO\",\"points\":3000}]"));

        verify(pointsService).spend(5000);
    }

    @Test
    public void spend_returnsErrorOnOverspend() throws Exception {
        when(pointsService.spend(5000)).thenThrow(InsufficientPointsException.class);

        mockMvc.perform(
            post("/points/spend")
                .contentType(APPLICATION_JSON)
                .content("{ \"points\": 5000 }"))
            .andExpect(status().is(HttpStatus.UNPROCESSABLE_ENTITY.value()))
            .andExpect(content().string("Insufficient points"));
    }

    @Test
    public void spend_returnsErrorIfInvalidPointsValue() throws Exception {
        when(pointsService.spend(0)).thenThrow(IllegalArgumentException.class);

        mockMvc.perform(
            post("/points/spend")
                .contentType(APPLICATION_JSON)
                .content("{ \"points\": 0 }"))
            .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
            .andExpect(content().string("Cannot spend less than one point"));
    }

    @Test
    public void getBalances_returnsBalanceData() throws Exception {
        when(pointsService.getBalances()).thenReturn(
            Map.of("ALPHA", 100, "BRAVO", 200));

        mockMvc.perform(
            get("/points/balances"))
            .andExpect(status().isOk())
            .andExpect(content().json("{\"ALPHA\":100,\"BRAVO\":200}"));
    }

    private Date toDate(final String isoDate) throws ParseException {
        return dateFormat.parse(
            isoDate
                .replace("T", " ")      // SimpleDateFormat doesn't parse ISO 8601 format :(
                .replace("Z", " GMT"));
    }
}
