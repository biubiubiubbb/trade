package com.example.trade.simulate.breakstock.model;

import com.alibaba.excel.annotation.ExcelProperty;
import com.example.trade.profit.StockProfit;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@Data
public class BreakStockStockProfit extends StockProfit implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    @ExcelProperty("高开点位")
    private BigDecimal gap;
    @ExcelProperty("今日集合竞价成交额")
    private String callAuctionTradeAmount;
    @ExcelProperty("昨日集合竞价成交额")
    private String callAuctionTradeAmountPrev;

}
