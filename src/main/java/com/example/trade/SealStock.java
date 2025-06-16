package com.example.trade;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class SealStock implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @ExcelProperty("交易日")
    private LocalDate tradeDate;

    @ExcelProperty("连板数")
    @JSONField(name = "连板数")
    private int consecutiveBoardCount;

    @ExcelProperty("代码")
    @JSONField(name = "代码")
    private String code;

    @ExcelProperty("名称")
    @JSONField(name = "名称")
    private String name;

    @ExcelProperty("涨跌额")
    @JSONField(name = "涨跌幅")
    private BigDecimal changeRate;

    @ExcelProperty("最新价")
    @JSONField(name = "最新价")
    private BigDecimal latestPrice;

    @ExcelProperty("成交额")
    @JSONField(name = "成交额")
    private BigDecimal tradeAmount;

    @ExcelProperty("流通市值")
    @JSONField(name = "流通市值")
    private BigDecimal circulatingMarketValue;

    @ExcelProperty("换手率")
    @JSONField(name = "换手率")
    private BigDecimal turnoverRate;

    @ExcelProperty("封板资金")
    @JSONField(name = "封板资金")
    private BigDecimal sealBoardFunds;

    @ExcelProperty("首次封板时间")
    @JSONField(name = "首次封板时间")
    private String firstSealTime;

    @ExcelProperty("最后封板时间")
    @JSONField(name = "最后封板时间")
    private String lastSealTime;

    @ExcelProperty("炸板次数")
    @JSONField(name = "炸板次数")
    private int breakBoardCount;

    @ExcelProperty("涨停统计")
    @JSONField(name = "涨停统计")
    private String limitUpStats;

    @ExcelProperty("所属行业")
    @JSONField(name = "所属行业")
    private String industry;

}