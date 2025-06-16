package com.example.trade.profit;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class StockProfit implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @ExcelProperty("股票代码")
    private String code;
    @ExcelProperty("股票名称")
    private String name;
    @ExcelProperty("买入日期")
    private LocalDate buyDate;
    @ExcelProperty("卖出日期")
    private LocalDate sellDate;
    @ExcelProperty("买入金额")
    private BigDecimal buyAmount;
    @ExcelProperty("卖出金额")
    private BigDecimal sellAmount;
    @ExcelProperty("收益")
    private BigDecimal profit;
    @ExcelProperty("收益率")
    private BigDecimal profitRate;
    @ExcelProperty("买入均价")
    private BigDecimal buyAvgPrice;
    @ExcelProperty("卖出均价")
    private BigDecimal sellAvgPrice;
    @ExcelProperty("买入点位")
    private String buyPoints;
    @ExcelProperty("买入价格")
    private String buyPrices;
    @ExcelProperty("卖出点位")
    private String sellPoints;
    @ExcelProperty("卖出价格")
    private String sellPrices;
    @ExcelProperty("持有天数")
    private int holdingDays;

}
