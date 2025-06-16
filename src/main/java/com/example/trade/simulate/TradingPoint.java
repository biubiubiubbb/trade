package com.example.trade.simulate;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class TradingPoint {
    private BigDecimal price;
    private BigDecimal point;
    private BigDecimal amount;
    private BigDecimal quantity;
    private TradeType tradeType;
    private LocalDate tradeDate;
}