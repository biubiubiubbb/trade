package com.example.trade.profit;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class StatisticProfit implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    @ExcelProperty("月份")
    private String month;
    @ExcelProperty("总收益")
    private BigDecimal profit;
    @ExcelProperty("总收益率")
    private BigDecimal profitRate;
    @ExcelProperty("盈利次数")
    private int profitCount;
    @ExcelProperty("亏损次数")
    private int lossCount;
    @ExcelProperty("买入总金额")
    private BigDecimal buyAmount;
    @ExcelProperty("卖出总金额")
    private BigDecimal sellAmount;
    @ExcelProperty("最大盈利金额股票")
    private String maxProfitStock;
    @ExcelProperty("最大盈利金额")
    private BigDecimal maxProfit;
    @ExcelProperty("最大盈利比例")
    private BigDecimal maxProfitRate;
    @ExcelProperty("最大盈利比例股票")
    private String maxProfitRateStock;
    @ExcelProperty("最大亏损金额")
    private BigDecimal maxLoss;
    @ExcelProperty("最大亏损金额股票")
    private String maxLossStock;
    @ExcelProperty("最大亏损比例")
    private BigDecimal maxLossRate;
    @ExcelProperty("最大亏损比例股票")
    private String maxLossRateStock;

}
