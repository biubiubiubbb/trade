package com.example.trade.simulate;

import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * 多次买入，一次卖出
 */
@Data
public class TradingResult {
    private List<TradingPoint> buyPoints;
    private TradingPoint sellPoint;

    public BigDecimal getBuyAvgPrice() {
        BigDecimal buyAmount = buyPoints.stream()
                .map(TradingPoint::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal buyQuantity = buyPoints.stream()
                .map(TradingPoint::getQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return buyAmount.divide(buyQuantity, 2, RoundingMode.HALF_UP);
    }

    public BigDecimal getBuyQuantity() {
        return buyPoints.stream()
                .map(TradingPoint::getQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}