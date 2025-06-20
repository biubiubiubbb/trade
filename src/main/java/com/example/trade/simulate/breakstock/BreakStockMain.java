package com.example.trade.simulate.breakstock;

import com.example.trade.DataCenter;
import com.example.trade.Exec;
import com.example.trade.profit.StatisticProfit;
import com.example.trade.simulate.breakstock.model.BreakStock;
import com.example.trade.simulate.breakstock.model.BreakStockStockProfit;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class BreakStockMain {

    public static void main(String[] args) {
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 6, 17);
        BreakStockProfitCalculate calculator = new BreakStockProfitCalculate();
        List<BigDecimal> supportGaps = BreakStockDataCenter.getSupportGaps();
        List<String> sheetNameList = supportGaps.stream().map(gap -> "高开" + gap + "点").toList();
        List<List<BreakStockStockProfit>> profitList = new ArrayList<>();
        List<List<StatisticProfit>> statisticProfitList = new ArrayList<>();
        for (BigDecimal gap : supportGaps) {
            Map<String, List<BreakStock>> breakStockMap = BreakStockDataCenter.getBreakStockList(gap);
            List<BreakStockStockProfit> allProfitList = new ArrayList<>();
            LocalDate date = endDate;
            log.info("gap {} date {} 开始计算", gap, date);
            while (date.isAfter(startDate) || date.isEqual(startDate)) {
                if (!DataCenter.isTradeDate(date)) {
                    date = date.minusDays(1);
                    continue;
                }
                List<BreakStock> breakStocks = breakStockMap.get(date.toString());
                if (breakStocks == null || breakStocks.isEmpty()) {
                    log.info("gap {} date {} 无数据", gap, date);
                    date = DataCenter.getPrevTradeDate(date);
                    continue;
                }
                List<BreakStockStockProfit> profits = calculator.calculateBreakStockProfit(breakStocks);
                if (profits.isEmpty()) {
                    date = DataCenter.getPrevTradeDate(date);
                    continue;
                }
                allProfitList.addAll(profits);
                date = DataCenter.getPrevTradeDate(date);
            }
            List<StatisticProfit> statisticProfits = calculator.calMonthlyProfit(allProfitList);
            profitList.add(allProfitList);
            statisticProfitList.add(statisticProfits);
        }
        Exec.write("昨日上影线曾涨停-优化-收益明细.xlsx", sheetNameList, profitList, BreakStockStockProfit.class);
        Exec.write("昨日上影线曾涨停-优化-收益统计.xlsx", sheetNameList, statisticProfitList, StatisticProfit.class);
    }


}
