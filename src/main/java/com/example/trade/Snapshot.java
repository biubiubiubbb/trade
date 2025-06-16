package com.example.trade;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class Snapshot implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @ExcelProperty("日期")
    @JSONField(name = "日期")
    private LocalDate date; // 日期

    @ExcelProperty("股票代码")
    @JSONField(name = "股票代码")
    private String code; // 股票代码

    @ExcelProperty("开盘")
    @JSONField(name = "开盘")
    private BigDecimal openPrice; // 开盘价

    @ExcelProperty("收盘")
    @JSONField(name = "收盘")
    private BigDecimal closePrice; // 收盘价

    @ExcelProperty("最高")
    @JSONField(name = "最高")
    private BigDecimal highestPrice; // 最高价

    @ExcelProperty("最低")
    @JSONField(name = "最低")
    private BigDecimal lowestPrice; // 最低价

    @ExcelProperty("成交量")
    @JSONField(name = "成交量")
    private long volume; // 成交量

    @ExcelProperty("成交额")
    @JSONField(name = "成交额")
    private BigDecimal turnover; // 成交额

    @ExcelProperty("振幅")
    @JSONField(name = "振幅")
    private BigDecimal amplitude; // 振幅

    @ExcelProperty("涨跌幅")
    @JSONField(name = "涨跌幅")
    private BigDecimal changeRate; // 涨跌幅

    @ExcelProperty("涨跌额")
    @JSONField(name = "涨跌额")
    private BigDecimal changeAmount; // 涨跌额

    @ExcelProperty("换手率")
    @JSONField(name = "换手率")
    private BigDecimal turnoverRate; // 换手率

}
