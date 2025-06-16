package com.example.trade;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class SealProfit {

    @ExcelProperty("股票代码")
    private String code;
    @ExcelProperty("股票名称")
    private String name;
    @ExcelProperty("高开点位")
    private BigDecimal gap;
    @ExcelProperty("买入点位")
    private String buyPoints;
    @ExcelProperty("买入价格")
    private String buyPrices;
    @ExcelProperty("买入日期")
    private LocalDate buyDate;
    @ExcelProperty("卖出日期")
    private LocalDate sellDate;
    @ExcelProperty("买入均价")
    private BigDecimal buyAvgPrice;
    @ExcelProperty("卖出价格")
    private BigDecimal sellPrice;
    @ExcelProperty("持有天数")
    private int holdingDays;
    @ExcelProperty("买入金额")
    private BigDecimal buyAmount;
    @ExcelProperty("卖出金额")
    private BigDecimal sellAmount;
    @ExcelProperty("收益")
    private BigDecimal profit;
    @ExcelProperty("收益率")
    private BigDecimal profitRate;
//    @ExcelProperty("买入当天收益")
//    private BigDecimal holdingProfitOfBuyDate;
//    @ExcelProperty("买入当天收益率")
//    private BigDecimal holdingProfitRateOfBuyDate;
}
