package com.cbiegay.points.web;

import com.cbiegay.points.InsufficientPointsException;
import com.cbiegay.points.model.PayerPointDelta;
import com.cbiegay.points.model.PointSpend;
import com.cbiegay.points.model.Transaction;
import com.cbiegay.points.service.DefaultPointsService;
import com.cbiegay.points.service.PointsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Controller with endpoints for managing points.
 */
@Controller
@RequestMapping(path="/points")
public class PointsController {

    private final PointsService pointsService;

    public PointsController(DefaultPointsService pointsService) {
        this.pointsService = Objects.requireNonNull(pointsService);
    }

    @PostMapping(path="/transaction")
    public @ResponseBody void addTransaction(@RequestBody Transaction transaction) {
        pointsService.addTransaction(transaction);
    }

    @PostMapping(path="/spend")
    public ResponseEntity spend(@RequestBody PointSpend pointSpend) {
        try {
            List<PayerPointDelta> response = pointsService.spend(pointSpend.getPoints());
            return ResponseEntity.ok().body(response);
        } catch (InsufficientPointsException e) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body("Insufficient points");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Cannot spend less than one point");
        }
    }

    @GetMapping(path="/balances")
    public @ResponseBody Map<String, Integer> getBalances() {
        return pointsService.getBalances();
    }
}
