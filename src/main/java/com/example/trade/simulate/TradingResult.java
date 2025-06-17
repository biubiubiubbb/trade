package com.example.trade.simulate;

import com.example.trade.DataCenter;
import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
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

    public BigDecimal getBuyAmount() {
        return buyPoints.stream()
                .map(TradingPoint::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getSellAvgPrice() {
        return sellPoint.getAmount().divide(sellPoint.getQuantity(), 2, RoundingMode.HALF_UP);
    }

    public BigDecimal getSellAmount() {
        return sellPoint.getAmount();
    }

    public BigDecimal getProfit() {
        return getSellAmount().subtract(getBuyAmount());
    }

    public BigDecimal getProfitRate() {
        return getProfit().divide(getBuyAmount(), 2, RoundingMode.HALF_UP);
    }

    public int getHoldingDays() {
        return DataCenter.getHoldDays(buyPoints.get(0).getTradeDate(), sellPoint.getTradeDate());
    }

    public String getBuyPoints() {
        return buyPoints.stream()
                .map(TradingPoint::getPoint)
                .map(Object::toString)
                .reduce("", (a, b) -> a + "," + b);
    }

    public String getBuyPrices() {
        return buyPoints.stream()
                .map(TradingPoint::getPrice)
                .map(Object::toString)
                .reduce("", (a, b) -> a + "," + b);
    }

    public String getSellPoints() {
        return "";
    }

    public String getSellPrices() {
        return sellPoint.getPrice().toString();
    }

    public LocalDate getBuyDate() {
        return buyPoints.get(0).getTradeDate();
    }

    public LocalDate getSellDate() {
        return sellPoint.getTradeDate();
    }
}