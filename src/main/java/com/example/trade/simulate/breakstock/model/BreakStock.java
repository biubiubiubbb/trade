package com.example.trade.simulate.breakstock.model;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class BreakStock implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    @ExcelProperty("股票代码")
    private String code;
    @ExcelProperty("股票名称")
    private String name;
    @ExcelProperty("交易日")
    private LocalDate tradeDate;
    @ExcelProperty("前一个交易日")
    private LocalDate tradeDatePrev;
    @ExcelProperty("流通市值")
    private String circulationMarketValue;
    @ExcelProperty("集合竞价涨跌幅")
    private BigDecimal callAuctionChg;
    @ExcelProperty("今日集合竞价成交额")
    private String callAuctionTradeAmount;
    @ExcelProperty("昨日集合竞价成交额")
    private String callAuctionTradeAmountPrev;
}
